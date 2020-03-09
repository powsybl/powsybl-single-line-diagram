/*
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.sld.model;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.util.Objects;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public abstract class AbstractGraph {
    protected double x = 0;
    protected double y = 0;
    protected boolean generateCoordsInJson = true;

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void writeJson(Writer writer) {
        Objects.requireNonNull(writer);
        try (JsonGenerator generator = new JsonFactory()
                .createGenerator(writer)
                .useDefaultPrettyPrinter()) {
            writeJson(generator);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public abstract void writeJson(JsonGenerator generator) throws IOException;

    public boolean isGenerateCoordsInJson() {
        return generateCoordsInJson;
    }

    public void setGenerateCoordsInJson(boolean generateCoordsInJson) {
        this.generateCoordsInJson = generateCoordsInJson;
    }
}
