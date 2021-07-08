/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout;

import com.powsybl.sld.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A LegBusSet contains the set of BusNodes that shall be vertically presented, and the cells that have a pattern of
 * connection included in the busNodeSet. It is embedded into a LBSCluster. It contains links to all the other
 * LegBusSet of the Graph.
 *
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 */
public final class LegBusSet {

    private static final Logger LOGGER = LoggerFactory.getLogger(LegBusSet.class);

    private final Set<BusNode> busNodeSet;
    private final Set<BusNode> extendedNodeSet;
    private final Set<ExternCell> externCells = new LinkedHashSet<>();

    private final Set<InternCellSide> internCellSides = new LinkedHashSet<>();

    private LegBusSet(Map<BusNode, Integer> nodeToNb, List<BusNode> busNodes) {
        busNodeSet = new TreeSet<>(Comparator.comparingInt(nodeToNb::get));
        extendedNodeSet = new TreeSet<>(Comparator.comparingInt(nodeToNb::get));
        busNodeSet.addAll(busNodes);
    }

    private LegBusSet(Map<BusNode, Integer> nodeToNb, ExternCell cell) {
        this(nodeToNb, cell.getBusNodes());
        externCells.add(cell);
    }

    private LegBusSet(Map<BusNode, Integer> nodeToNb, ShuntCell cell) {
        this(nodeToNb, cell.getParentBusNodes());
        externCells.addAll(cell.getCells());
    }

    private LegBusSet(Map<BusNode, Integer> nodeToNb, InternCell internCell, Side side) {
        this(nodeToNb, internCell.getSideBusNodes(side));
        addInternCell(internCell, side);
    }

    private LegBusSet(Map<BusNode, Integer> nodeToNb, BusNode busNode) {
        this(nodeToNb, Collections.singletonList(busNode));
    }

    void addInternCell(InternCell internCell, Side side) {
        internCellSides.add(new InternCellSide(internCell, side));
    }

    private boolean contains(Collection<BusNode> busNodeCollection) {
        return busNodeSet.containsAll(busNodeCollection);
    }

    private boolean contains(LegBusSet lbs) {
        return contains(lbs.getBusNodeSet());
    }

    private void absorbs(LegBusSet lbsToAbsorb) {
        busNodeSet.addAll(lbsToAbsorb.busNodeSet);
        externCells.addAll(lbsToAbsorb.externCells);
        internCellSides.addAll(lbsToAbsorb.internCellSides);
    }

    Map<InternCell, Side> getCellsSideMapFromShape(InternCell.Shape shape) {
        return internCellSides.stream().filter(ics -> ics.getCell().checkisShape(shape))
                .collect(Collectors.toMap(InternCellSide::getCell, InternCellSide::getSide));
    }

    List<InternCell> getInternCellsFromShape(InternCell.Shape shape) {
        return internCellSides.stream().map(InternCellSide::getCell).distinct().collect(Collectors.toList());
    }

    public Set<BusNode> getBusNodeSet() {
        return busNodeSet;
    }

    public Set<ExternCell> getExternCells() {
        return externCells;
    }

    Set<InternCellSide> getInternCellSides() {
        return internCellSides;
    }

    void setExtendedNodeSet(Collection<BusNode> busNodes) {
        if (busNodes.containsAll(busNodeSet)) {
            extendedNodeSet.addAll(busNodes.stream().filter(Objects::nonNull).collect(Collectors.toList()));
        } else {
            LOGGER.error("ExtendedNodeSet inconsistent with NodeBusSet");
        }
    }

    Set<BusNode> getExtendedNodeSet() {
        return extendedNodeSet;
    }

    static List<LegBusSet> createLegBusSets(VoltageLevelGraph graph, Map<BusNode, Integer> nodeToNb, boolean handleShunts) {
        List<LegBusSet> legBusSets = new ArrayList<>();

        List<ExternCell> externCells = graph.getCells().stream()
                .filter(cell -> cell.getType() == Cell.CellType.EXTERN)
                .map(ExternCell.class::cast)
                .sorted(Comparator.comparing(Cell::getFullId)) // if order is not yet defined & avoid randomness
                .collect(Collectors.toList());

        if (handleShunts) {
            manageShunts(graph, externCells, legBusSets, nodeToNb);
        }

        externCells.forEach(cell -> pushNewLBS(legBusSets, nodeToNb, cell, Side.UNDEFINED));

        graph.getCells().stream()
                .filter(cell -> cell.getType() == Cell.CellType.INTERN && ((InternCell) cell).checkisShape(InternCell.Shape.UNILEG))
                .map(InternCell.class::cast)
                .sorted(Comparator.comparing(Cell::getFullId)) // if order is not yet defined & avoid randomness
                .forEachOrdered(cell -> pushNewLBS(legBusSets, nodeToNb, cell, Side.UNDEFINED));

        graph.getCells().stream()
                .filter(cell -> cell.getType() == Cell.CellType.INTERN
                        && ((InternCell) cell).checkIsNotShape(InternCell.Shape.UNILEG, InternCell.Shape.UNHANDLEDPATTERN))
                .map(InternCell.class::cast)
                .sorted(Comparator.comparing(cell -> -((InternCell) cell).getBusNodes().size())         // bigger first to identify encompassed InternCell at the end with the smaller one
                        .thenComparing(cell -> ((InternCell) cell).getFullId()))                        // avoid randomness
                .forEachOrdered(cell -> pushNonUnilegInternCell(legBusSets, nodeToNb, cell));

        // find orphan busNodes and build their LBS
        List<BusNode> allBusNodes = new ArrayList<>(graph.getNodeBuses());
        allBusNodes.removeAll(legBusSets.stream().
                flatMap(legBusSet -> legBusSet.getBusNodeSet().stream()).collect(Collectors.toList()));
        allBusNodes.stream()
                .sorted(Comparator.comparing(Node::getId))              //avoid randomness
                .forEach(busNode -> legBusSets.add(new LegBusSet(nodeToNb, busNode)));
        return legBusSets;
    }

    private static void manageShunts(VoltageLevelGraph graph, List<ExternCell> externCells, List<LegBusSet> legBusSets, Map<BusNode, Integer> nodeToNb) {
        List<ShuntCell> shuntCells = graph.getCells().stream()
                .filter(c -> c.getType() == Cell.CellType.SHUNT).map(ShuntCell.class::cast).collect(Collectors.toList());
        List<List<ShuntCell>> sameBusNodesShuntCells = shuntCells.stream()
                .map(sc -> {
                    List<ShuntCell> list = new ArrayList<>();
                    list.add(sc);
                    return list;
                }).collect(Collectors.toList());
        int i = 0;
        while (i < sameBusNodesShuntCells.size()) {
            int j = i + 1;
            while (j < sameBusNodesShuntCells.size()) {
                if (crossContains(sameBusNodesShuntCells.get(i).get(0).getParentBusNodes(),
                        sameBusNodesShuntCells.get(j).get(0).getParentBusNodes())) {
                    sameBusNodesShuntCells.get(i).addAll(sameBusNodesShuntCells.get(j));
                    sameBusNodesShuntCells.remove(j);
                } else {
                    j++;
                }
            }
            i++;
        }

        sameBusNodesShuntCells.stream().filter(scs -> scs.size() > 2).flatMap(List::stream)
                .forEach(sc -> {
                    pushNewLBS(legBusSets, nodeToNb, sc, Side.UNDEFINED);
                    externCells.removeAll(sc.getCells());
                });
    }

    private static boolean crossContains(List<BusNode> busNodes1, List<BusNode> busNodes2) {
        return busNodes1.containsAll(busNodes2) && busNodes2.containsAll(busNodes1);
    }

    private static void pushNewLBS(List<LegBusSet> legBusSets, Map<BusNode, Integer> nodeToNb, Cell cell, Side side) {
        LegBusSet legBusSet;
        if (cell.getType() == Cell.CellType.EXTERN) {
            legBusSet = new LegBusSet(nodeToNb, (ExternCell) cell);
        } else if (cell.getType() == Cell.CellType.SHUNT) {
            legBusSet = new LegBusSet(nodeToNb, (ShuntCell) cell);
        } else {
            legBusSet = new LegBusSet(nodeToNb, (InternCell) cell, side);
        }

        for (LegBusSet lbs : legBusSets) {
            if (lbs.contains(legBusSet)) {
                lbs.absorbs(legBusSet);
                return;
            }
        }
        List<LegBusSet> absorbedLBS = new ArrayList<>();
        legBusSets.forEach(lbs -> {
            if (legBusSet.contains(lbs)) {
                absorbedLBS.add(lbs);
                legBusSet.absorbs(lbs);
            }
        });
        legBusSets.removeAll(absorbedLBS);
        legBusSets.add(legBusSet);
    }

    private static void pushNonUnilegInternCell(List<LegBusSet> legBusSets, Map<BusNode, Integer> nodeToNb, InternCell internCell) {
        for (LegBusSet lbs : legBusSets) {
            if (lbs.contains(internCell.getBusNodes())) {
                lbs.addInternCell(internCell, Side.UNDEFINED);
                internCell.setShape(InternCell.Shape.VERTICAL);
                return;
            }
        }
        pushNewLBS(legBusSets, nodeToNb, internCell, Side.LEFT);
        pushNewLBS(legBusSets, nodeToNb, internCell, Side.RIGHT);
    }
}
