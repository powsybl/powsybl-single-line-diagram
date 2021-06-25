/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout;

import com.powsybl.sld.model.Node;
import com.powsybl.sld.model.Point;
import com.powsybl.sld.model.SubstationGraph;
import com.powsybl.sld.model.VoltageLevelGraph;

import java.util.List;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 * @author Slimane Amar <slimane.amar at rte-france.com>
 */
public class HorizontalSubstationLayout extends AbstractSubstationLayout {

    private final InfosNbSnakeLinesHorizontal infosNbSnakeLines;

    public HorizontalSubstationLayout(SubstationGraph graph, VoltageLevelLayoutFactory vLayoutFactory) {
        super(graph, vLayoutFactory);
        this.infosNbSnakeLines = InfosNbSnakeLinesHorizontal.create(graph);
    }

    @Override
    protected List<Point> calculatePolylineSnakeLine(LayoutParameters layoutParam, Node node1, Node node2,
                                                     boolean increment) {
        return calculatePolylineSnakeLineForHorizontalLayout(layoutParam, node1, node2, increment, infosNbSnakeLines);
    }

    /**
     * Calculate relative coordinate of voltageLevels in the substation
     */
    @Override
    protected void calculateCoordVoltageLevels(LayoutParameters layoutParameters) {
        double totalWidth = layoutParameters.getHorizontalSubstationPadding();
        double maxHeight = 0;
        for (VoltageLevelGraph vlGraph : getGraph().getNodes()) {
            vlGraph.setCoord(totalWidth, 0);

            // Calculate the objects coordinates inside the voltageLevel graph
            VoltageLevelLayout vLayout = vLayoutFactory.create(vlGraph);
            vLayout.run(layoutParameters);

            totalWidth += vlGraph.getWidth() + layoutParameters.getHorizontalSubstationPadding();
            maxHeight = Math.max(maxHeight, vlGraph.getHeight());
        }

        getGraph().setSize(totalWidth + layoutParameters.getTranslateX(), maxHeight + layoutParameters.getTranslateY());
    }
}
