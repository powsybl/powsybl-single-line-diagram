/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout;

import com.powsybl.sld.model.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * LBSCluster contains a list of LegBusSets (LBS) that is orderly build by successively merging LBSCluster initially
 * containing a single LBS.
 * LBSCluster handles the building of the horizontalBusLanes that are an horizontal strings of busNodes.
 *
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 */
public class LBSCluster {

    private final List<LegBusSet> lbsList = new ArrayList<>();
    private final List<HorizontalBusLane> horizontalBusLanes = new ArrayList<>();
    private final int nb;

    public LBSCluster(LegBusSet lbs, int nb) {
        Objects.requireNonNull(lbs);
        lbsList.add(lbs);
        lbs.getBusNodeSet().forEach(nodeBus -> horizontalBusLanes.add(new HorizontalBusLane(nodeBus, this)));

        this.nb = nb;
    }

    public static List<LBSCluster> createLBSClusters(List<LegBusSet> legBusSets) {
        List<LBSCluster> lbsClusters = new ArrayList<>();
        int nb = 0;
        for (LegBusSet lbs : legBusSets) {
            lbsClusters.add(new LBSCluster(lbs, nb++));
        }
        return lbsClusters;
    }

    public void merge(Side myConcernedSide, LBSCluster otherLbsCluster, Side otherSide, HorizontalBusLaneManager hblManager) {
        if (myConcernedSide == Side.LEFT) {
            reverse();
        }
        if (otherSide == Side.RIGHT) {
            otherLbsCluster.reverse();
        }
        hblManager.mergeHorizontalBusLanes(this, otherLbsCluster);
        lbsList.addAll(otherLbsCluster.lbsList);
    }

    public List<BusNode> laneSideBuses(Side side) {
        return laneSideBuses(side, horizontalBusLanes);
    }

    public static List<BusNode> laneSideBuses(Side side, List<HorizontalBusLane> horizontalBusLaneList) {
        return horizontalBusLaneList.stream()
                .map(hl -> hl.getSideNode(side)).collect(Collectors.toList());
    }

    public void removeHorizontalBusLane(HorizontalBusLane lane) {
        horizontalBusLanes.remove(lane);
    }

    public void establishBusNodePosition() {
        int v = 1;
        for (HorizontalBusLane lane : horizontalBusLanes) {
            lane.establishBusPosition(v);
            v++;
        }
    }

    VoltageLevelGraph getGraph() {
        return horizontalBusLanes.get(0).getBusNodes().get(0).getGraph();
    }

    public HorizontalBusLane getHorizontalLaneFromSideBus(BusNode busNode, Side side) {
        return horizontalBusLanes
                .stream()
                .filter(horizontalBusLane -> horizontalBusLane.getSideNode(side) == busNode)
                .findAny()
                .orElse(null);
    }

    List<BusNode> getVerticalBuseNodes(int i) {
        return horizontalBusLanes.stream().map(hbl -> hbl.getBusNode(i)).collect(Collectors.toList());
    }

    private void reverse() {
        Collections.reverse(lbsList);
        horizontalBusLanes.forEach(lane -> lane.reverse(lbsList.size()));
    }

    private LegBusSet getLbsSideFromBusNode(BusNode busNode, Side side) {
        if (side != Side.RIGHT && side != Side.LEFT) {
            return null;
        }
        for (int i = 0; i < lbsList.size(); i++) {
            int j = side == Side.LEFT ? i : lbsList.size() - i - 1;
            if (lbsList.get(j).getBusNodeSet().contains(busNode)) {
                return lbsList.get(j);
            }
        }
        return null;
    }

    public List<InternCell> getSideCandidateFlatCell(Side side) {
        return laneSideBuses(side).stream()
                .map(busNode -> getLbsSideFromBusNode(busNode, side))
                .distinct().filter(Objects::nonNull)
                .flatMap(lbs -> lbs.getCellsSideMapFromShape(InternCell.Shape.MAYBEFLAT).keySet().stream())
                .collect(Collectors.toList());
    }

    public List<InternCell> getInternCellsFromShape(InternCell.Shape shape) {
        return lbsList.stream().flatMap(legBusSet -> legBusSet.getInternCellsFromShape(shape)
                .stream()).collect(Collectors.toList());
    }

    public void sortHorizontalBusLanesByVPos() {
        horizontalBusLanes.sort(Comparator.comparingInt(hbl -> hbl.getBusNodes().get(0).getBusbarIndex()));
    }

    public int getLength() {
        return lbsList.size();
    }

    public List<HorizontalBusLane> getHorizontalBusLanes() {
        return horizontalBusLanes;
    }

    public List<LegBusSet> getLbsList() {
        return lbsList;
    }

    @Override
    public String toString() {
        return lbsList.toString() + "\n" + horizontalBusLanes.toString();
    }

    int getNb() {
        return nb;
    }
}
