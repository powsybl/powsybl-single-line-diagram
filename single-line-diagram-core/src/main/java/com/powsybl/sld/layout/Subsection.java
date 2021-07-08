/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout;

import com.powsybl.sld.model.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.powsybl.sld.model.Side.LEFT;
import static com.powsybl.sld.model.Side.RIGHT;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 */

public class Subsection {

    private int size;
    private BusNode[] busNodes;
    private Set<InternCellSide> internCellSides = new LinkedHashSet<>();
    private List<ExternCell> externCells = new LinkedList<>();
    private static Comparator<ExternCell> compareOrder = Comparator.comparingInt(ExternCell::getOrder);

    Subsection(int size) {
        this.size = size;
        busNodes = new BusNode[size];
    }

    private boolean checkAbsorbability(LegBusSet lbs) {
        return lbs.getExtendedNodeSet().stream().noneMatch(busNode -> {
            int vIndex = busNode.getBusbarIndex() - 1;
            return busNodes[vIndex] != null && busNodes[vIndex] != busNode;
        });
    }

    private void addLegBusSet(LegBusSet lbs) {
        lbs.getExtendedNodeSet().forEach(bus -> busNodes[bus.getBusbarIndex() - 1] = bus);
        externCells.addAll(lbs.getExternCells());
        externCells.sort(compareOrder);
        internCellSides.addAll(lbs.getInternCellSides());
    }

    public int getSize() {
        return size;
    }

    public BusNode[] getBusNodes() {
        return busNodes;
    }

    BusNode getBusNode(int index) {
        return busNodes[index];
    }

    List<InternCell> getInternCells(InternCell.Shape shape, Side side) {
        return internCellSides.stream()
                .filter(ics -> ics.getCell().checkisShape(shape) && ics.getSide() == side)
                .map(InternCellSide::getCell).collect(Collectors.toList());
    }

    List<InternCell> getVerticalInternCells() {
        return internCellSides.stream()
                .filter(ics -> ics.getCell().checkisShape(InternCell.Shape.VERTICAL)
                        || ics.getCell().checkisShape(InternCell.Shape.UNILEG))
                .map(InternCellSide::getCell).collect(Collectors.toList());
    }

    public List<ExternCell> getExternCells() {
        return externCells;
    }

    private boolean containsAllBusNodes(List<BusNode> nodes) {
        return Arrays.asList(busNodes).containsAll(nodes);
    }

    static List<Subsection> createSubsections(VoltageLevelGraph graph, LBSCluster lbsCluster, boolean handleShunts) {
        List<Subsection> subsections = new ArrayList<>();
        Optional<VoltageLevelGraph> oVLGraph = lbsCluster.getLbsList().get(0).getBusNodeSet().stream().filter(Objects::nonNull).findAny().map(BusNode::getGraph);
        if (!oVLGraph.isPresent()) {
            return subsections;
        }

        int vSize = oVLGraph.get().getMaxVerticalBusPosition();
        Subsection currentSubsection = new Subsection(vSize);
        subsections.add(currentSubsection);
        int i = 0;
        for (LegBusSet lbs : lbsCluster.getLbsList()) {
            lbs.setExtendedNodeSet(lbsCluster.getVerticalBuseNodes(i));
            if (!currentSubsection.checkAbsorbability(lbs)) {
                currentSubsection = new Subsection(vSize);
                subsections.add(currentSubsection);
            }
            currentSubsection.addLegBusSet(lbs);
            i++;
        }

        internCellCoherence(oVLGraph.get(), lbsCluster.getLbsList(), subsections);

        graph.getCells().stream()
                .filter(c -> c.getType() == Cell.CellType.SHUNT)
                .map(ShuntCell.class::cast).forEach(ShuntCell::alignExternCells);
        if (handleShunts) {
            shuntCellCoherence(oVLGraph.get(), subsections);
        }

        return subsections;
    }

    static List<Subsection> createSubsections(VoltageLevelGraph graph, LBSCluster lbsCluster) {
        return createSubsections(graph, lbsCluster, false);
    }

    private static void internCellCoherence(VoltageLevelGraph vlGraph, List<LegBusSet> lbsList, List<Subsection> subsections) {
        identifyVerticalInternCells(vlGraph, subsections);
        lbsList.stream()
                .flatMap(lbs -> lbs.getCellsSideMapFromShape(InternCell.Shape.MAYBEFLAT).keySet().stream()).distinct()
                .forEach(InternCell::identifyIfFlat);
        identifyCrossOverAndCheckOrientation(subsections);
        slipInternCellSideToEdge(subsections);
    }

    private static void identifyVerticalInternCells(VoltageLevelGraph graph, List<Subsection> subsections) {
        Map<InternCell, Subsection> verticalCells = new LinkedHashMap<>();

        graph.getCells().stream()
                .filter(c -> c.getType() == Cell.CellType.INTERN
                        && ((InternCell) c).checkIsNotShape(InternCell.Shape.UNILEG, InternCell.Shape.UNDEFINED, InternCell.Shape.UNHANDLEDPATTERN))
                .map(InternCell.class::cast)
                .forEach(c ->
                        subsections.stream()
                                .filter(subsection -> subsection.containsAllBusNodes(c.getBusNodes()))
                                .findAny().ifPresent(subsection -> verticalCells.putIfAbsent(c, subsection)));

        subsections.forEach(ss -> {
            List<InternCellSide> icsToRemove = ss.internCellSides.stream()
                    .filter(ics -> verticalCells.keySet().contains(ics.getCell())).collect(Collectors.toList());
            ss.internCellSides.removeAll(icsToRemove);
        });

        verticalCells.forEach((cell, sub) -> {
            cell.setShape(InternCell.Shape.VERTICAL);
            sub.internCellSides.add(new InternCellSide(cell, Side.UNDEFINED));
        });

    }

    private static void identifyCrossOverAndCheckOrientation(List<Subsection> subsections) {
        final class SideSs {
            private Side side;
            private Subsection ss;

            private SideSs(Side side, Subsection ss) {
                this.side = side;
                this.ss = ss;
            }
        }

        Map<InternCell, List<SideSs>> cellToSideSs = new LinkedHashMap<>();
        for (Subsection ss : subsections) {
            ss.internCellSides.stream()
                    .filter(ics -> {
                        InternCell.Shape shape = ics.getCell().getShape();
                        return shape == InternCell.Shape.UNDEFINED
                                || shape == InternCell.Shape.FLAT
                                || shape == InternCell.Shape.CROSSOVER;
                    })
                    .forEach(ics -> {
                        cellToSideSs.putIfAbsent(ics.getCell(), new ArrayList<>());
                        cellToSideSs.get(ics.getCell()).add(new SideSs(ics.getSide(), ss));
                    });
        }
        cellToSideSs.forEach((cell, sideSses) -> {
            if (sideSses.size() == 2) {
                if (!cell.checkisShape(InternCell.Shape.FLAT)) {
                    cell.setShape(InternCell.Shape.CROSSOVER);
                }
                if (sideSses.get(0).side == RIGHT) {
                    cell.reverseCell();
                    sideSses.stream().flatMap(sss -> sss.ss.internCellSides.stream())
                            .filter(ics -> ics.getCell() == cell)
                            .forEach(InternCellSide::flipSide);
                }
            }
        });
    }

    private static void slipInternCellSideToEdge(List<Subsection> subsections) {
        Map<InternCellSide, Subsection> cellSideToMove = new LinkedHashMap<>();
        new ArrayList<>(subsections).forEach(ss -> {
            List<InternCellSide> cellToRemove = new ArrayList<>();
            ss.internCellSides.stream()
                    .filter(ics -> ics.getCell().checkisShape(InternCell.Shape.FLAT, InternCell.Shape.CROSSOVER))
                    .forEach(ics -> {
                        List<BusNode> nodes = ics.getCell().getSideBusNodes(ics.getSide());
                        List<Subsection> candidateSss = subsections.stream().filter(ss2 -> ss2.containsAllBusNodes(nodes)).collect(Collectors.toList());
                        if (!candidateSss.isEmpty()) {
                            Subsection candidateSs = ics.getSide() == LEFT ? candidateSss.get(candidateSss.size() - 1) : candidateSss.get(0);
                            if (ss != candidateSs) {
                                cellToRemove.add(ics);
                                cellSideToMove.put(ics, candidateSs);
                            }
                        }
                    });
            ss.internCellSides.removeAll(cellToRemove);
        });
        cellSideToMove.forEach((cellSide, ss) -> ss.internCellSides.add(cellSide));
    }

    private static void shuntCellCoherence(VoltageLevelGraph vlGraph, List<Subsection> subsections) {
        Map<ShuntCell, List<BusNode>> shuntCells2Buses = vlGraph.getCells().stream()
                .filter(c -> c.getType() == Cell.CellType.SHUNT)
                .map(ShuntCell.class::cast)
                .collect(Collectors.toMap(Function.identity(), ShuntCell::getParentBusNodes, (u, v) -> {
                    throw new IllegalStateException(String.format("Duplicate key %s", u));
                }, LinkedHashMap::new));
        if (shuntCells2Buses.isEmpty()) {
            return;
        }

        List<ShuntCell> sameSubsectionShunts = identifySameSubsectionShuntCells(subsections, shuntCells2Buses);
        slipInternShuntedCellsToEdge(subsections, shuntCells2Buses.keySet(), sameSubsectionShunts);
        alignMultiFeederShunt(shuntCells2Buses.keySet());
        arrangeExternCellsOrders(subsections);
    }

    private static List<ShuntCell> identifySameSubsectionShuntCells(List<Subsection> subsections, Map<ShuntCell, List<BusNode>> shuntCells2Buses) {
        List<ShuntCell> modifiedShunts = new ArrayList<>();
        subsections.forEach(ss -> shuntCells2Buses.keySet().stream()
                .filter(sc -> ss.containsAllBusNodes(shuntCells2Buses.get(sc)))
                .forEach(sc -> {
                    sc.getCells().forEach(c -> moveExternCellToSubsection(c, ss, subsections, Side.UNDEFINED));
                    int iLeft = ss.externCells.indexOf(sc.getSideCell(LEFT));
                    int iRight = ss.externCells.indexOf(sc.getSideCell(RIGHT));
                    if (iRight != iLeft + 1) {
                        ExternCell leftCell = sc.getSideCell(LEFT);
                        ss.externCells.remove(leftCell);
                        ss.externCells.add(ss.externCells.indexOf(sc.getSideCell(RIGHT)), leftCell);
                    }
                    modifiedShunts.add(sc);
                })
        );
        return modifiedShunts;
    }

    private static void slipInternShuntedCellsToEdge(List<Subsection> subsections, Set<ShuntCell> shuntCells, List<ShuntCell> sameSubsectionShunts) {
        shuntCells.stream().filter(sc -> !sameSubsectionShunts.contains(sc))
                .forEach(sc -> {
                    for (Side side : Side.defined()) {
                        ExternCell cell = sc.getSideCell(side);
                        subsections.stream().filter(ss -> ss.containsAllBusNodes(cell.getBusNodes()))
                                .map(subsections::indexOf).mapToInt(j -> side == LEFT ? j : -j).max()
                                .ifPresent(j -> moveExternCellToSubsection(cell, subsections.get(Math.abs(j)), subsections,
                                        side.getFlip()));
                    }
                });
    }

    private static void moveExternCellToSubsection(ExternCell c, Subsection ss, List<Subsection> subsections, Side side) {
        if (ss.externCells.contains(c) && side == Side.UNDEFINED) {
            return;
        }
        for (Subsection sub : subsections) {
            if (sub.externCells.contains(c)) {
                sub.externCells.remove(c);
                break;
            }
        }
        if (side == LEFT) {
            ss.externCells.add(0, c);
        } else {
            ss.externCells.add(c);
        }
    }

    private static void alignMultiFeederShunt(Set<ShuntCell> shCells) {
        shCells.forEach(sc -> {
            for (Side side : Side.defined()) {
                ExternCell cell = sc.getSideCell(side);
                List<FeederNode> feeders = cell.getNodes().stream()
                        .filter(n -> n.getType() == Node.NodeType.FEEDER)
                        .map(FeederNode.class::cast).collect(Collectors.toList());
                if (feeders.size() > 1) {
                    FictitiousNode shNode = sc.getSideShuntNode(side);
                    List<Node> outsideNodes = new ArrayList<>();
                    outsideNodes.add(shNode);
                    List<FeederNode> shuntSideFeederNodes = shNode.getAdjacentNodes().stream().flatMap(node -> {
                        List<Node> gtResult = new ArrayList<>();
                        if (GraphTraversal.run(node, node1 -> node1.getType() == Node.NodeType.FEEDER, node1 -> node1.getType() == Node.NodeType.BUS, gtResult, outsideNodes)) {
                            return gtResult.stream().filter(n -> n.getType() == Node.NodeType.FEEDER).map(FeederNode.class::cast);
                        } else {
                            return new ArrayList<FeederNode>().stream();
                        }
                    }).collect(Collectors.toList());
                    feeders.removeAll(shuntSideFeederNodes);
                    List<FeederNode> newlyOrderdFeeders;
                    if (side == RIGHT) {
                        newlyOrderdFeeders = shuntSideFeederNodes;
                        newlyOrderdFeeders.addAll(feeders);
                    } else {
                        newlyOrderdFeeders = feeders;
                        newlyOrderdFeeders.addAll(shuntSideFeederNodes);
                    }
                    for (int i = 0; i < newlyOrderdFeeders.size(); i++) {
                        newlyOrderdFeeders.get(i).setOrder(i);
                    }
                }
            }
        });
    }

    private static void arrangeExternCellsOrders(List<Subsection> subsections) {
        subsections.forEach(ss -> {
            List<ExternCell> eCells = ss.getExternCells();
            for (int i = 1; i < eCells.size(); i++) {
                int prevIndex = eCells.get(i - 1).getOrder();
                if (eCells.get(i).getOrder() <= prevIndex) {
                    eCells.get(i).setOrder(prevIndex + 1);
                }
            }
        });
    }
}
