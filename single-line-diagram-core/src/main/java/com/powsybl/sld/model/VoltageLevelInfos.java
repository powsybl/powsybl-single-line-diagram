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

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class VoltageLevelInfos {

    private final String id;

    private final String name;

    private final double nominalVoltage;

    public VoltageLevelInfos(String id, String name, double nominalVoltage) {
        this.id = Objects.requireNonNull(id);
        this.name = Objects.requireNonNull(name);
        this.nominalVoltage = nominalVoltage;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getNominalVoltage() {
        return nominalVoltage;
    }

    public void writeJsonContent(JsonGenerator generator) throws IOException {
        generator.writeStartObject();
        generator.writeStringField("id", id);
        generator.writeStringField("name", name);
        generator.writeNumberField("nominalVoltage", nominalVoltage);
        generator.writeEndObject();
    }
}
