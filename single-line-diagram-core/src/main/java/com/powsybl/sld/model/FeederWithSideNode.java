/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model;

import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class FeederWithSideNode extends FeederNode {

    public enum Side {
        ONE(1),
        TWO(2),
        THREE(3);

        int intValue;

        Side(int intValue) {
            this.intValue = intValue;
        }

        public int getIntValue() {
            return intValue;
        }

    }

    protected final Side side;

    protected final VoltageLevelInfos otherSideVoltageLevelInfos;

    protected FeederWithSideNode(String id, String name, String equipmentId, String componentType,
                                 VoltageLevelGraph graph, Side side, VoltageLevelInfos otherSideVoltageLevelInfos, FeederType feederType) {
        super(id, name, equipmentId, componentType, graph, feederType);
        this.side = Objects.requireNonNull(side);
        this.otherSideVoltageLevelInfos = otherSideVoltageLevelInfos;
    }

    public Side getSide() {
        return side;
    }

    public VoltageLevelInfos getOtherSideVoltageLevelInfos() {
        return otherSideVoltageLevelInfos;
    }

    @Override
    protected void writeJsonContent(JsonGenerator generator) throws IOException {
        super.writeJsonContent(generator);
        generator.writeStringField("side", side.name());
        if (otherSideVoltageLevelInfos != null) {
            generator.writeFieldName("otherSideVoltageLevelInfos");
            otherSideVoltageLevelInfos.writeJsonContent(generator);
        }
    }
}
