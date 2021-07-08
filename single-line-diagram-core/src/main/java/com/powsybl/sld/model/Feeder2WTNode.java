/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model;

import static com.powsybl.sld.library.ComponentTypeName.PHASE_SHIFT_TRANSFORMER;
import static com.powsybl.sld.library.ComponentTypeName.TWO_WINDINGS_TRANSFORMER;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class Feeder2WTNode extends FeederBranchNode {

    protected Feeder2WTNode(String id, String name, String equipmentId, String componentType, VoltageLevelGraph graph, Side side,
                            VoltageLevelInfos otherSideVoltageLevelInfos) {
        super(id, name, equipmentId, componentType, graph, side, otherSideVoltageLevelInfos);
    }

    public static Feeder2WTNode create(VoltageLevelGraph graph, String id, String name, String equipmentId, Side side, VoltageLevelInfos otherSideVoltageLevelInfos) {
        return new Feeder2WTNode(id, name, equipmentId, TWO_WINDINGS_TRANSFORMER, graph, side, otherSideVoltageLevelInfos);
    }

    public static Feeder2WTNode createWithPhaseShifter(VoltageLevelGraph graph, String id, String name, String equipmentId, Side side, VoltageLevelInfos otherSideVoltageLevelInfos) {
        return new Feeder2WTNode(id, name, equipmentId, PHASE_SHIFT_TRANSFORMER, graph, side, otherSideVoltageLevelInfos);
    }
}
