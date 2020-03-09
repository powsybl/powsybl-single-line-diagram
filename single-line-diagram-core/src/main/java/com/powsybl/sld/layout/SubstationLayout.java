/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout;

import com.powsybl.sld.model.BusCell;
import com.powsybl.sld.model.Graph;
import com.powsybl.sld.model.Side;

import java.util.Map;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public interface SubstationLayout {

    /**
     * Calculate real coordinates of nodes in the substation graph
     */
    default void run(LayoutParameters layoutParameters) {
        run(layoutParameters, true, true, null);
    }

    void run(LayoutParameters layoutParameters, boolean applyVLLayouts, boolean manageSnakeLines, Map<Graph, VoltageLevelLayout> mapVLayouts);

    default Map<Graph, VoltageLevelLayout> getVlLayouts() {
        return null;
    }

    default Integer addAndGetNbSnakeLinesTopBottom(String vId, BusCell.Direction direction, Integer increment) {
        return null;
    }

    default Integer addAndGetNbSnakeLinesBetween(String key, Integer increment) {
        return null;
    }

    default Integer addAndGetNbSnakeLinesLeftRight(Side side, Integer increment) {
        return null;
    }

    default Integer addAndGetNbSnakeLinesBottomVL(String key, Integer increment) {
        return null;
    }

    default Integer addAndGetNbSnakeLinesTopVL(String key, Integer increment) {
        return null;
    }
}
