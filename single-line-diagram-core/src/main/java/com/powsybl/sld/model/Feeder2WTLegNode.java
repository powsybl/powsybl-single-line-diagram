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
public class Feeder2WTLegNode extends FeederWithSideNode {

    protected Feeder2WTLegNode(String id, String name, String equipmentId, VoltageLevelGraph graph, Side side,
                               VoltageLevelInfos otherSideVoltageLevelInfos) {
        super(id, name, equipmentId, ComponentTypeName.TWO_WINDINGS_TRANSFORMER_LEG, graph, side, otherSideVoltageLevelInfos,
                FeederType.TWO_WINDINGS_TRANSFORMER_LEG);
    }

    @Override
    public VoltageLevelInfos getVoltageLevelInfos() {
        // we consider this node represent the other side of the transformer so voltage level infos is the other
        // side one
        if (otherSideVoltageLevelInfos != null) {
            return otherSideVoltageLevelInfos;
        }
        return super.getVoltageLevelInfos();
    }

    public static Feeder2WTLegNode createForVoltageLevelDiagram(VoltageLevelGraph graph, String id, String name, String equipmentId, Side side,
                                                                VoltageLevelInfos otherSideVoltageLevelInfos) {
        return new Feeder2WTLegNode(id, name, equipmentId, graph, side, otherSideVoltageLevelInfos);
    }

    public static Feeder2WTLegNode createForSubstationDiagram(VoltageLevelGraph graph, String id, String name, String equipmentId, Side side) {
        return new Feeder2WTLegNode(id, name, equipmentId, graph, side, graph.getVoltageLevelInfos());
    }
}
