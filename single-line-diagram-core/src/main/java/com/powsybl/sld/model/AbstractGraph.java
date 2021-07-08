/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public abstract class AbstractGraph implements Graph {

    private boolean generateCoordsInJson = true;

    @Override
    public void setGenerateCoordsInJson(boolean generateCoordsInJson) {
        this.generateCoordsInJson = generateCoordsInJson;
    }

    @Override
    public boolean isGenerateCoordsInJson() {
        return generateCoordsInJson;
    }

    @Override
    public void writeJson(Path file) {
        try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            writeJson(writer);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
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

    protected abstract void writeJson(JsonGenerator generator) throws IOException;
}
