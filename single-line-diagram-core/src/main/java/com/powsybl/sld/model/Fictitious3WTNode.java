/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model;

import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;
import java.util.Objects;

import static com.powsybl.sld.library.ComponentTypeName.THREE_WINDINGS_TRANSFORMER;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class Fictitious3WTNode extends FictitiousNode {

    private final VoltageLevelInfos voltageLevelInfosLeg1;

    private final VoltageLevelInfos voltageLevelInfosLeg2;

    private final VoltageLevelInfos voltageLevelInfosLeg3;

    public Fictitious3WTNode(Graph graph, String id, VoltageLevelInfos voltageLevelInfosLeg1, VoltageLevelInfos voltageLevelInfosLeg2,
                             VoltageLevelInfos voltageLevelInfosLeg3) {
        super(graph, id, THREE_WINDINGS_TRANSFORMER);
        this.voltageLevelInfosLeg1 = Objects.requireNonNull(voltageLevelInfosLeg1);
        this.voltageLevelInfosLeg2 = Objects.requireNonNull(voltageLevelInfosLeg2);
        this.voltageLevelInfosLeg3 = Objects.requireNonNull(voltageLevelInfosLeg3);
    }

    public VoltageLevelInfos getVoltageLevelInfosLeg1() {
        return voltageLevelInfosLeg1;
    }

    public VoltageLevelInfos getVoltageLevelInfosLeg2() {
        return voltageLevelInfosLeg2;
    }

    public VoltageLevelInfos getVoltageLevelInfosLeg3() {
        return voltageLevelInfosLeg3;
    }

    @Override
    protected void writeJsonContent(JsonGenerator generator) throws IOException {
        super.writeJsonContent(generator);

        generator.writeFieldName("voltageLevelInfosLeg1");
        voltageLevelInfosLeg1.writeJsonContent(generator);

        generator.writeFieldName("voltageLevelInfosLeg2");
        voltageLevelInfosLeg2.writeJsonContent(generator);

        generator.writeFieldName("voltageLevelInfosLeg3");
        voltageLevelInfosLeg3.writeJsonContent(generator);
    }
}
