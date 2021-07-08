/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 */

public interface HorizontalBusLaneManager {

    void mergeHorizontalBusLanes(LBSCluster leftCluster, LBSCluster rightCluster);

    default void mergeLanesWithNoLink(LBSCluster leftCluster, LBSCluster rightCluster) {
        rightCluster.getHorizontalBusLanes().forEach(lane -> {
            lane.shift(leftCluster.getLength());
            lane.setLbsCluster(leftCluster);
        });
        leftCluster.getHorizontalBusLanes().addAll(rightCluster.getHorizontalBusLanes());
        rightCluster.getHorizontalBusLanes().removeAll(rightCluster.getHorizontalBusLanes());
    }
}
