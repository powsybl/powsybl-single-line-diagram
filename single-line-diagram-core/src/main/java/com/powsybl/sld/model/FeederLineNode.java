/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model;

import com.powsybl.sld.library.ComponentTypeName;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class FeederLineNode extends FeederBranchNode {

    protected FeederLineNode(String id, String name, String equipmentId, VoltageLevelGraph graph, Side side, VoltageLevelInfos otherSideVoltageLevelInfos) {
        super(id, name, equipmentId, ComponentTypeName.LINE, graph, side, otherSideVoltageLevelInfos);
    }

    public static FeederLineNode create(VoltageLevelGraph graph, String id, String name, String equipmentId, Side side, VoltageLevelInfos otherSideVoltageLevelInfos) {
        return new FeederLineNode(id, name, equipmentId, graph, side, otherSideVoltageLevelInfos);
    }
}
