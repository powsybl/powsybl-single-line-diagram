/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public class TwtEdge extends Edge {

    private String componentType;

    private List<Double> snakeLine = new ArrayList<>();

    public TwtEdge(String componentType, Node... nodes) {
        super(nodes);
        this.componentType = componentType;
    }

    public TwtEdge(TwtEdge other) {
        super(other);
        componentType = other.componentType;
        snakeLine = new ArrayList<>(other.snakeLine);
    }

    public List<Double> getSnakeLine() {
        return snakeLine;
    }

    public void setSnakeLine(List<Double> snakeLine) {
        this.snakeLine = Objects.requireNonNull(snakeLine);
    }

    public String getComponentType() {
        return componentType;
    }
}
