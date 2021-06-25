/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout;

import com.powsybl.sld.model.SubstationGraph;

import java.util.Objects;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public abstract class AbstractSubstationLayout extends AbstractLayout implements SubstationLayout {

    private final SubstationGraph graph;
    protected VoltageLevelLayoutFactory vLayoutFactory;

    public AbstractSubstationLayout(SubstationGraph graph, VoltageLevelLayoutFactory vLayoutFactory) {
        this.graph = graph;
        this.vLayoutFactory = Objects.requireNonNull(vLayoutFactory);
    }

    public SubstationGraph getGraph() {
        return graph;
    }

    @Override
    public void run(LayoutParameters layoutParameters) {
        // Calculate all the coordinates for the voltageLevel graphs in the substation graph
        calculateCoordVoltageLevels(layoutParameters);

        // Calculate all the coordinates for the middle nodes and the snake lines between the voltageLevel graphs
        manageSnakeLines(layoutParameters);
    }

    protected abstract void calculateCoordVoltageLevels(LayoutParameters layoutParameters);

    @Override
    public void manageSnakeLines(LayoutParameters layoutParameters) {
        getGraph().getNodes().forEach(g -> manageSnakeLines(g, layoutParameters));
        manageSnakeLines(graph, layoutParameters);
    }

}
