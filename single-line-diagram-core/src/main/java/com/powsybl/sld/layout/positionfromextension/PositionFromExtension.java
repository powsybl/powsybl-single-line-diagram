/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout.positionfromextension;

import com.google.common.collect.Comparators;
import com.powsybl.sld.layout.*;
import com.powsybl.sld.model.*;
import org.checkerframework.checker.nullness.Opt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class PositionFromExtension implements PositionFinder {
    private static final Logger LOGGER = LoggerFactory.getLogger(PositionFromExtension.class);
    private static final BusCell.Direction DEFAULTDIRECTION = BusCell.Direction.TOP;
    private static final HorizontalBusLaneManager HBLMANAGER = new HBLaneManagerByExtension();

    /**
     * Builds the layout of the bus nodes, and organises cells (order and directions)
     */

    @Override
    public Map<BusNode, Integer> indexBusPosition(List<BusNode> busNodes) {
        Map<BusNode, Integer> busToNb = new HashMap<>();
        int i = 1;
        for (BusNode busNode : busNodes.stream()
                .sorted((bn1, bn2) -> {
                    Position p1 = bn1.getStructuralPosition();
                    Position p2 = bn2.getStructuralPosition();
                    return p1.getV() == p2.getV() ? p1.getH() - p2.getH() : p1.getV() - p2.getV();
                })
                .collect(Collectors.toList())) {
            busToNb.put(busNode, i++);
        }
        return busToNb;
    }

    @Override
    public LBSCluster organizeLegBusSets(List<LegBusSet> legBusSets) {
        Graph graph = legBusSets.get(0).getBusNodeSet().iterator().next().getGraph();
        gatherLayoutExtensionInformation(graph);

        List<LBSCluster> lbsClusters = LBSCluster.createLBSClusters(
                legBusSets.stream().sorted(sortLBS).collect(Collectors.toList()));

        lbsClusters.forEach(lbsCluster -> {
            LegBusSet lbs = lbsCluster.getLbsList().get(0);
            BusNode node = lbs.getBusNodeSet().iterator().next();
            int order = lbs.getExternCells().stream().findAny().map(ExternCell::getOrder).orElse(0);
            LOGGER.info("MERGING LBS - Cluster ordered : {} {} {}", node.getId(), node.getStructuralPosition(), order);
        });

        LBSCluster lbsCluster = lbsClusters.get(0);

        while (lbsClusters.size() != 1) {
            lbsCluster.merge(Side.RIGHT, lbsClusters.get(1), Side.LEFT, HBLMANAGER);
            lbsClusters.remove(1);
        }
        lbsCluster.sortHorizontalBusLanesByVPos();
        return lbsCluster;
    }

    private void gatherLayoutExtensionInformation(Graph graph) {
        graph.getNodes().stream()
                .filter(node -> node.getType() == Node.NodeType.FEEDER)
                .map(FeederNode.class::cast)
                .forEach(feederNode -> {
                    ExternCell cell = (ExternCell) feederNode.getCell();
                    cell.setDirection(
                            feederNode.getDirection() == BusCell.Direction.UNDEFINED ? DEFAULTDIRECTION : feederNode.getDirection());
                    cell.setOrder(feederNode.getOrder());
                });
        graph.getCells().stream().filter(cell -> cell.getType() == Cell.CellType.EXTERN).map(ExternCell.class::cast)
                .forEach(ExternCell::orderFromFeederOrders);

        List<ExternCell> problematicCells = graph.getCells().stream()
                .filter(cell -> cell.getType().equals(Cell.CellType.EXTERN))
                .map(ExternCell.class::cast)
                .filter(cell -> cell.getOrder() == -1).collect(Collectors.toList());
        if (!problematicCells.isEmpty()) {
            LOGGER.warn("Unable to build the layout only with Extension\nproblematic cells :");
            problematicCells.forEach(cell -> LOGGER
                    .info("Cell Nb : {}, Order : {}, Type : {}",
                            cell.getNumber(),
                            cell.getOrder(),
                            cell.getType()));
        }
    }

    private Comparator<LegBusSet> sortLBS = new Comparator<LegBusSet>() {
        @Override
        public int compare(LegBusSet lbs1, LegBusSet lbs2) {
            Optional<Integer> order1 = externCellOrderNb(lbs1);
            Optional<Integer> order2 = externCellOrderNb(lbs2);
            if (order1.isPresent() && order2.isPresent()) {
                return order1.get() - order2.get();
            }

            for (BusNode busNode : lbs1.getBusNodeSet()) {
                final Position pos1 = busNode.getStructuralPosition();
                Optional<Position> pos2 = lbs2.getBusNodeSet().stream().map(BusNode::getStructuralPosition)
                        .filter(p -> p.getV() == pos1.getV()).findFirst();
                if (pos2.isPresent() && pos2.get().getH() != pos1.getH()) {
                    return pos1.getH() - pos2.get().getH();
                }
            }

            int h1max = getMaxPos(lbs1.getBusNodeSet(), Position::getH);
            int h2max = getMaxPos(lbs2.getBusNodeSet(), Position::getH);
            if (h1max != h2max) {
                return h1max - h2max;
            }

            int v1max = getMaxPos(lbs1.getBusNodeSet(), Position::getV);
            int v2max = getMaxPos(lbs2.getBusNodeSet(), Position::getV);
            if (v1max != v2max) {
                return v1max - v2max;
            }
            return lbs1.getBusNodeSet().size() - lbs2.getBusNodeSet().size();
        }

        private int getMaxPos(Set<BusNode> busNodes, Function<Position, Integer> fun) {
            return busNodes.stream()
                    .map(BusNode::getStructuralPosition).map(fun).max(Integer::compareTo).orElse(0);
        }


        private Optional<Integer> externCellOrderNb(LegBusSet lbs) {
            return lbs.getExternCells().stream().findAny().map(ExternCell::getOrder);
        }

    };
}
