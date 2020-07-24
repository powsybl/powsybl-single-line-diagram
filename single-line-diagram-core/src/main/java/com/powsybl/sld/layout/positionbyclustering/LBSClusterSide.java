/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout.positionbyclustering;

import com.powsybl.sld.layout.HorizontalBusLane;
import com.powsybl.sld.layout.LBSCluster;
import com.powsybl.sld.model.BusNode;
import com.powsybl.sld.model.InternCell;
import com.powsybl.sld.model.Side;

import java.util.*;

/**
 * LBSClusterSide is a ClusterConnector defined by one Side (LEFT/RIGHT) of a LBSCluster.
 *
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 */
class LBSClusterSide {

    private final LBSCluster lbsCluster;
    private final Side side;
    private final List<Link> myLinks = new ArrayList<>();

    LBSClusterSide(LBSCluster lbsCluster, Side side) {
        this.lbsCluster = Objects.requireNonNull(lbsCluster);
        this.side = Objects.requireNonNull(side);
    }

    Set<BusNode> getBusNodeSet() {
        return new LinkedHashSet<>(lbsCluster.laneSideBuses(side));
    }

    List<InternCell> getCandidateFlatCellList() {
        return lbsCluster.getSideFlatCell(side);
    }

    List<InternCell> getCrossOverCellList() {
        return lbsCluster.getCrossoverCells();
    }

    LBSCluster getCluster() {
        return lbsCluster;
    }

    Side getMySideInCluster() {
        return side;
    }

    boolean hasSameRoot(Object other) {
        if (other.getClass() != LBSClusterSide.class) {
            return false;
        }
        return this.lbsCluster == ((LBSClusterSide) other).getCluster();
    }

    LBSClusterSide getOtherSameRoot(List<LBSClusterSide> clusterConnectors) {
        return clusterConnectors.stream().filter(clusterConnector ->
                clusterConnector.getCluster() == lbsCluster
                        && side.getFlip() == clusterConnector.getMySideInCluster()).findAny().orElse(null);
    }

    int getDistanceToEdge(InternCell internCell) {
        List<BusNode> buses = internCell.getBusNodes();
        buses.retainAll(getBusNodeSet());
        if (buses.isEmpty()) {
            return 0;
        }
        BusNode busNode = buses.get(0);
        HorizontalBusLane horizontalBusLane = lbsCluster.getHorizontalBusLanes()
                .stream()
                .filter(lane -> side == Side.LEFT && lane.getBusNodes().get(0) == busNode
                        || side == Side.RIGHT && lane.getBusNodes().get(lane.getBusNodes().size() - 1) == busNode)
                .findFirst().orElse(null);
        if (horizontalBusLane == null) {
            return 0;
        } else {
            if (side == Side.LEFT) {
                return horizontalBusLane.getStartingIndex();
            } else {
                return lbsCluster.getLbsList().size() - horizontalBusLane.getEndingIndex();
            }
        }
    }

    void addLink(Link link) {
        myLinks.add(link);
    }

    void removeLink(Link link) {
        myLinks.remove(link);
    }

    List<Link> getLinks() {
        return myLinks;
    }

    @Override
    public String toString() {
        return side.toString() + " " + lbsCluster.laneSideBuses(side).toString();
    }
}
