/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout.positionfromextension;

import com.powsybl.sld.layout.HorizontalBusLane;
import com.powsybl.sld.layout.HorizontalBusLaneManager;
import com.powsybl.sld.layout.LBSCluster;
import com.powsybl.sld.model.BusNode;
import com.powsybl.sld.model.Side;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 */

public class HBLaneManagerByExtension implements HorizontalBusLaneManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(HBLaneManagerByExtension.class);

    public void mergeHorizontalBusLanes(LBSCluster leftCluster, LBSCluster rightCluster) {
        //for this implementation, the busBar structuralPosition are already defined,
        // we must ensure that structuralPosition vPos when merging left and right HorizontalPosition,
        // and structuralPosition hPos are ordered
        leftCluster.getHorizontalBusLanes().forEach(hbl -> {
            BusNode rightNodeOfLeftHbl = hbl.getSideNode(Side.RIGHT);
            Optional<HorizontalBusLane> rightHBL = rightCluster.getHorizontalBusLanes().stream()
                    .filter(hbl2 -> hbl2.getSideNode(Side.LEFT).getBusbarIndex() == rightNodeOfLeftHbl.getBusbarIndex())
                    .findFirst();
            if (rightHBL.isPresent()
                    && (rightHBL.get().getSideNode(Side.LEFT) == rightNodeOfLeftHbl
                    || rightNodeOfLeftHbl.getSectionIndex() < rightHBL.get().getSideNode(Side.LEFT).getSectionIndex())) {
                hbl.merge(rightHBL.get());
                rightCluster.removeHorizontalBusLane(rightHBL.get());
            } else {
                // TODO : not true !!!
                LOGGER.warn("incoherent structural horizontal positions in mergeHorizontalBusLanes");
            }
        });
        mergeLanesWithNoLink(leftCluster, rightCluster);
    }
}
