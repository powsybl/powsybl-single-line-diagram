/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout.positionbyclustering;

import com.powsybl.sld.layout.*;
import com.powsybl.sld.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

import com.powsybl.sld.model.BusCell.Direction;

import static com.powsybl.sld.model.BusCell.Direction.*;
import static com.powsybl.sld.model.Cell.CellType.*;
import static com.powsybl.sld.model.Side.*;

/**
 * PositionByClustering finds adequate positions for the busBars with the following principles:
 * All the connections to the BusBar of the leg of an ExternCell, or each leg of an InternCell shall be stackable
 * (ie could be displayed with disconnectors to busbar vertically aligned).
 * This implies that the busBars of a leg shall be spread in different vertical structuralPosition,
 * (and in many case having the same horizontal structuralPosition).
 * The first step consists in building LegBusSets that contains busBars that shall be vertically aligned (considering
 * they have legs of cell that impose it).
 * Then LBSClusters are initiated by building one LBSCluster per LegBusSet.
 * The LBSClusters are then merged 2 by 2 starting by LBSclusters that have the strongest Link.
 * Two strategies of strength assessment of the links between clusters are implemented:
 * <ul>
 * <li>
 * if useLBSLinkOnly is true: the strength between LegBusSets is considered: this means that the strength of
 * the link between two clusters is the one of the strongest link between two LegBusSets (one per cluster). This
 * is a simple implementation that is limited as it it does not consider the difference between the side of a
 * cluster: if two clusters A and B are to be merged, the result can either be A-B or B-A.
 * </li>
 * <li>
 * if useLBSLinkOnly is false: the strength between LBSClusterSide is considered. This is similar
 * to what si done with LegBusSet but the assessment of the strength of the link considers both sides of the
 * cluster.
 * Therefore, with cluster A and B, there are 4 LBSClusterSide A-Right A-Left B-Right and B-Left. The links that
 * are considered are (A-Right, B-Left), (A-Right, B-Right), (B-Right, B-Left), (B-Right, B-Right). When merging,
 * alignment is required (meaning that clusters could be reversed to ensure the connection sides between the
 * 2 clusters are respected : 1st cluster-Right is merged with 2nd cluster-left).
 * </li>
 * </ul>
 *
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 */

// WE ASSUME THAT IT IS POSSIBLE TO STACK ALL CELLS AND BE ABLE TO ORGANIZE THE VOLTAGELEVEL ACCORDINGLY

public class PositionByClustering implements PositionFinder {

    private static final Logger LOGGER = LoggerFactory.getLogger(PositionByClustering.class);
    private static final HBLaneManagerByClustering HBLMANAGER = new HBLaneManagerByClustering();

    public Map<BusNode, Integer> indexBusPosition(List<BusNode> busNodes) {
        Map<BusNode, Integer> busToNb = new LinkedHashMap<>();
        int i = 1;
        for (BusNode n : busNodes.stream()
                .sorted(Comparator.comparing(BusNode::getId))
                .collect(Collectors.toList())) {
            busToNb.put(n, i);
            i++;
        }
        return busToNb;
    }

    public LBSCluster organizeLegBusSets(VoltageLevelGraph graph, List<LegBusSet> legBusSets) {
        List<LBSCluster> lbsClusters = LBSCluster.createLBSClusters(legBusSets);
        Links links = Links.create(lbsClusters, HBLMANAGER);
        while (!links.isEmpty()) {
            links.mergeLink(links.getStrongestLink());
        }
        LBSCluster lbsCluster = links.getFinalLBSCluster();

        tetrisHorizontalLanes(lbsCluster);
        lbsCluster.getHorizontalBusLanes().forEach(hl -> LOGGER.info(hl.toString()));
        lbsCluster.establishBusNodePosition();
        establishFeederPositions(lbsCluster);

        return lbsCluster;
    }

    private void tetrisHorizontalLanes(LBSCluster lbsCluster) {
        List<HorizontalBusLane> horizontalBusLanes = lbsCluster.getHorizontalBusLanes();
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("{}", horizontalBusLanes);
        }
        List<HorizontalBusLane> sortedLanes = horizontalBusLanes.stream()
                .sorted(Comparator.comparingInt(HorizontalBusLane::getStartingIndex)
                        .thenComparing(hl -> hl.getBusNodes().get(0).getId())) // cope with randomness
                .collect(Collectors.toList());
        int clusterLength = sortedLanes.stream()
                .mapToInt(HorizontalBusLane::getEndingIndex)
                .max().orElse(0);
        int i = 0;
        while (i < sortedLanes.size()) {
            HorizontalBusLane lane = sortedLanes.get(i);
            int actualMaxIndex = lane.getEndingIndex();
            while (actualMaxIndex < clusterLength) {
                int finalActualMax = actualMaxIndex;
                HorizontalBusLane laneToAdd = sortedLanes.stream()
                        .filter(l -> l.getStartingIndex() >= finalActualMax)
                        .findFirst().orElse(null);
                if (laneToAdd != null) {
                    lane.merge(laneToAdd);
                    sortedLanes.remove(laneToAdd);
                    horizontalBusLanes.remove(laneToAdd);
                    actualMaxIndex = lane.getEndingIndex();
                } else {
                    i++;
                    break;
                }
            }
            i++;
        }
    }

    private void establishFeederPositions(LBSCluster lbsCluster) {
        int cellOrder = 0;
        for (LegBusSet lbs : lbsCluster.getLbsList()) {
            for (ExternCell cell : lbs.getExternCells()) {
                cell.setOrder(cellOrder++);
            }
        }
    }

    public void organizeDirections(VoltageLevelGraph graph, List<Subsection> subsections) {
        List<ShuntCell> shuntCells = graph.getCells().stream()
                .filter(c -> c.getType() == SHUNT).map(ShuntCell.class::cast)
                .collect(Collectors.toList());
        List<ExternCell> shuntRightCells = shuntCells.stream()
                .map(sc -> sc.getSideCell(RIGHT)).map(ExternCell.class::cast)
                .collect(Collectors.toList());

        int cellPos = 0;
        int cellShuntShift = 0;

        for (Subsection ss : subsections) {
            for (ExternCell externCell : ss.getExternCells()) {
                if (shuntRightCells.contains(externCell)) {
                    cellShuntShift++; // an ExternCell on the right of a Shunt does not take its turn for flipping direction
                } else {
                    Direction direction = (cellPos + cellShuntShift) % 2 == 0 ? TOP : BOTTOM;
                    externCell.setDirection(direction);
                }
                cellPos++;
            }
        }
        shuntCells.forEach(sc -> sc.alignDirections(LEFT));
    }

}
