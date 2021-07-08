/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class FeederBranchNode extends FeederWithSideNode {

    protected FeederBranchNode(String id, String name, String equipmentId, String componentType, VoltageLevelGraph graph,
                               Side side, VoltageLevelInfos otherSideVoltageLevelInfos) {
        super(id, name, equipmentId, componentType, graph, side, otherSideVoltageLevelInfos, FeederType.BRANCH);
    }
}
