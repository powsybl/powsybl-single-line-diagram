/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout;

import com.powsybl.sld.model.VoltageLevelGraph;
import com.powsybl.sld.model.Node;

import java.util.List;

/**
 * @author Slimane Amar <slimane.amar at rte-france.com>
 */
public abstract class AbstractVoltageLevelLayout extends AbstractLayout implements VoltageLevelLayout {

    private final VoltageLevelGraph graph;

    protected AbstractVoltageLevelLayout(VoltageLevelGraph graph) {
        this.graph = graph;
    }

    public VoltageLevelGraph getGraph() {
        return graph;
    }

    @Override
    protected List<Double> calculatePolylineSnakeLine(LayoutParameters layoutParam, Node node1, Node node2,
                                                      InfosNbSnakeLines infosNbSnakeLines, boolean increment) {
        return calculatePolylineSnakeLineForHorizontalLayout(layoutParam, node1, node2, infosNbSnakeLines, increment);
    }

    @Override
    public void manageSnakeLines(LayoutParameters layoutParameters) {
        if (getGraph().isForVoltageLevelDiagram()) {
            manageSnakeLines(getGraph(), layoutParameters, InfosNbSnakeLines.create(getGraph()));
        }
    }
}
