/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout;

import com.powsybl.sld.model.*;

import java.util.*;
import java.util.stream.Collectors;

import static com.powsybl.sld.model.Position.Dimension.*;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 */
class BlockPositionner {

    void determineBlockPositions(VoltageLevelGraph graph, List<Subsection> subsections) {
        int hPos = 0;
        int prevHPos = 0;
        int hSpace = 0;
        int maxV = graph.getMaxVerticalBusPosition();
        List<InternCell> nonFlatCellsToClose = new ArrayList<>();

        Subsection prevSs = new Subsection(maxV);
        graph.getNodeBuses().forEach(nodeBus -> nodeBus.getPosition().set(V, nodeBus.getBusbarIndex()));

        for (Subsection ss : subsections) {
            updateNodeBuses(prevSs, ss, hPos, hSpace, Side.RIGHT); // close nodeBuses
            updateNodeBuses(prevSs, ss, hPos, hSpace, Side.LEFT); // open nodeBuses

            hPos = placeCrossOverInternCells(hPos, ss.getInternCells(InternCell.Shape.CROSSOVER, Side.RIGHT), Side.RIGHT, nonFlatCellsToClose);
            hPos = placeVerticalCells(hPos, new ArrayList<>(ss.getVerticalInternCells()));
            hPos = placeVerticalCells(hPos, new ArrayList<>(ss.getExternCells().stream()
                    .sorted(Comparator.comparingInt(ExternCell::getOrder)).collect(Collectors.toList())));
            hPos = placeCrossOverInternCells(hPos, ss.getInternCells(InternCell.Shape.CROSSOVER, Side.LEFT), Side.LEFT, nonFlatCellsToClose);
            if (hPos == prevHPos) {
                hPos++;
            }
            hSpace = placeFlatInternCells(hPos, ss.getInternCells(InternCell.Shape.FLAT, Side.LEFT)) - hPos;
            hPos += hSpace;
            prevHPos = hPos;
            prevSs = ss;
        }
        updateNodeBuses(prevSs, new Subsection(maxV), hPos, hSpace, Side.RIGHT); // close nodeBuses
        manageInternCellOverlaps(graph);
    }

    private void updateNodeBuses(Subsection prevSS, Subsection ss, int hPos, int hSpace, Side ssSide) {
        for (int v = 0; v < prevSS.getSize(); v++) {
            BusNode prevBusNode = prevSS.getBusNode(v);
            BusNode actualBusNode = ss.getBusNode(v);
            if (ssSide == Side.RIGHT && prevBusNode != null
                    && (actualBusNode == null || prevBusNode != actualBusNode)) {
                Position p = prevBusNode.getPosition();
                p.setSpan(H, hPos - Math.max(p.get(H), 0) - hSpace);
            } else if (ssSide == Side.LEFT && actualBusNode != null &&
                    (prevBusNode == null || prevBusNode != actualBusNode)) {
                actualBusNode.getPosition().set(H, hPos);
            }
        }
    }

    private int placeVerticalCells(int hPos, Collection<BusCell> busCells) {
        int hPosRes = hPos;
        for (BusCell cell : busCells) {
            hPosRes = cell.newHPosition(hPosRes);
        }
        return hPosRes;
    }

    private int placeFlatInternCells(int hPos, List<InternCell> cells) {
        int hPosRes = hPos;
        for (BusCell cell : cells) {
            hPosRes = Math.max(hPosRes, cell.newHPosition(hPos));
        }
        return hPosRes;
    }

    private int placeCrossOverInternCells(int hPos,
                                          List<InternCell> cells,
                                          Side side, List<InternCell> nonFlatCellsToClose) {
        // side, is the side from the InternCell standpoint. The left side of the internCell shall be on the right of the subsection
        int hPosRes = hPos;
        cells.sort(Comparator.comparingInt(c -> -nonFlatCellsToClose.indexOf(c)));
        for (InternCell cell : cells) {
            hPosRes = cell.newHPosition(hPosRes, side);
        }
        if (side == Side.LEFT) {
            nonFlatCellsToClose.addAll(cells);
        } else {
            nonFlatCellsToClose.removeAll(cells);
        }
        return hPosRes;
    }

    private void manageInternCellOverlaps(VoltageLevelGraph graph) {
        List<InternCell> cellsToHandle = graph.getCells().stream()
                .filter(cell -> cell.getType() == Cell.CellType.INTERN)
                .map(InternCell.class::cast)
                .filter(internCell -> internCell.checkIsNotShape(InternCell.Shape.FLAT, InternCell.Shape.UNDEFINED, InternCell.Shape.UNHANDLEDPATTERN))
                .collect(Collectors.toList());
        InternCellsLanes lane = new InternCellsLanes(cellsToHandle);
        lane.run();
    }

    /**
     * The class lane manages the overlaps of internCells.
     * After bundleToCompatibleLanes each lane contents non overlapping cells
     * arrangeLane at this stage balance the lanes on TOP and BOTTOM this could be improved by having various VPos per lane
     */
    private class InternCellsLanes {
        InternCellsLanes nextLane;
        List<InternCellsLanes> lanes;
        List<InternCell> cells;

        InternCellsLanes(List<InternCell> cells) {
            this.cells = new ArrayList<>(cells);
            lanes = new ArrayList<>();
            lanes.add(this);
        }

        InternCellsLanes(InternCell cell, List<InternCellsLanes> lanes) {
            this.lanes = lanes;
            lanes.add(this);
            cells = new ArrayList<>();
            addCell(cell);
        }

        void addCell(InternCell cell) {
            cells.add(cell);
        }

        void run() {
            bundleToCompatibleLanes();
            arrangeLanes();
        }

        private void bundleToCompatibleLanes() {
            Map<InternCell, List<InternCell>> incompatibilities = identifyIncompatibilities();
            while (!incompatibilities.isEmpty()) {
                shiftIncompatibilities(incompatibilities);
                incompatibilities = identifyIncompatibilities();
            }
            if (nextLane != null) {
                nextLane.bundleToCompatibleLanes();
            }
        }

        private Map<InternCell, List<InternCell>> identifyIncompatibilities() {
            Map<InternCell, List<InternCell>> incompatibilities = new LinkedHashMap<>();
            for (int i = 0; i < cells.size(); i++) {
                InternCell internCellA = cells.get(i);
                int hAmin = getHfromSide(internCellA, Side.LEFT);
                int hAmax = getHfromSide(internCellA, Side.RIGHT);

                for (int j = i + 1; j < cells.size(); j++) {
                    InternCell internCellB = cells.get(j);
                    int hBmin = getHfromSide(internCellB, Side.LEFT);
                    int hBmax = getHfromSide(internCellB, Side.RIGHT);
                    if (hAmin < hBmax && hBmin < hAmax) {
                        incompatibilities.putIfAbsent(internCellA, new ArrayList<>());
                        incompatibilities.get(internCellA).add(internCellB);
                        incompatibilities.putIfAbsent(internCellB, new ArrayList<>());
                        incompatibilities.get(internCellB).add(internCellA);
                    }
                }
            }
            return incompatibilities;
        }

        private int getHfromSide(InternCell cell, Side side) {
            if (cell.checkisShape(InternCell.Shape.UNILEG)) {
                return cell.getSideHPos(Side.UNDEFINED);
            }
            return cell.getSideHPos(side);
        }

        private void shiftIncompatibilities(Map<InternCell, List<InternCell>> incompatibilities) {
            incompatibilities.entrySet().stream()
                    .filter(e -> !e.getValue().isEmpty())
                    .max(Comparator.comparingInt(e -> e.getValue().size()))
                    .map(Map.Entry::getKey)
                    .ifPresent(cell -> {
                        cells.remove(cell);
                        if (nextLane == null) {
                            nextLane = new InternCellsLanes(cell, lanes);
                        } else {
                            nextLane.addCell(cell);
                        }
                    });
        }

        private void arrangeLanes() {
            int i = 0;
            for (InternCellsLanes lane : lanes) {
                final int j = i % 2;
                final int newV = i / 2;
                lane.cells.forEach(c -> {
                    c.setDirection(j == 0 ? BusCell.Direction.TOP : BusCell.Direction.BOTTOM);
                    if (!c.checkisShape(InternCell.Shape.UNILEG)) {
                        c.getBodyBlock().getPosition().set(V, newV);
                    }
                });
                i++;
            }
        }

        public String toString() {
            return (lanes.indexOf(this) + 1) + "/" + lanes.size() + " " + cells.toString();
        }
    }
}
