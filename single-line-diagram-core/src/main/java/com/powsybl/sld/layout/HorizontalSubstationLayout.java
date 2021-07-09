/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout;

import com.powsybl.sld.model.*;

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

        LayoutParameters.Padding diagramPadding = layoutParameters.getDiagramPadding();
        LayoutParameters.Padding voltageLevelPadding = layoutParameters.getVoltageLevelPadding();

        double yVoltageLevels = diagramPadding.getTop() + voltageLevelPadding.getTop();
        double totalWidth = diagramPadding.getLeft();
        double maxVlHeight = 0;

        for (VoltageLevelGraph vlGraph : getGraph().getNodes()) {
            totalWidth += voltageLevelPadding.getLeft();
            vlGraph.setCoord(totalWidth, yVoltageLevels);

            // Calculate the objects coordinates inside the voltageLevel graph
            VoltageLevelLayout vLayout = vLayoutFactory.create(vlGraph);
            vLayout.run(layoutParameters);

            totalWidth += vlGraph.getWidth() + voltageLevelPadding.getRight();
            maxVlHeight = Math.max(maxVlHeight, vlGraph.getHeight());
        }

        totalWidth += diagramPadding.getRight();

        double substationHeight = maxVlHeight
            + voltageLevelPadding.getTop() + voltageLevelPadding.getBottom()
            + diagramPadding.getTop() + diagramPadding.getBottom();
        getGraph().setSize(totalWidth, substationHeight);
    }

    @Override
    public void manageSnakeLines(LayoutParameters layoutParameters) {

        getGraph().getNodes().forEach(g -> manageSnakeLines(g, layoutParameters));
        manageSnakeLines(getGraph(), layoutParameters);

        double heightSnakeLinesTop = infosNbSnakeLines.getNbSnakeLinesTopBottom().get(BusCell.Direction.TOP) * layoutParameters.getVerticalSnakeLinePadding();
        double heightSnakeLinesBottom = infosNbSnakeLines.getNbSnakeLinesTopBottom().get(BusCell.Direction.BOTTOM) * layoutParameters.getVerticalSnakeLinePadding();
        getGraph().setSize(getGraph().getWidth(), getGraph().getHeight() + heightSnakeLinesTop + heightSnakeLinesBottom);

        if (heightSnakeLinesTop > 0) {
            getGraph().getNodes().forEach(g -> g.setCoord(g.getX(), g.getY() + heightSnakeLinesTop));

            infosNbSnakeLines.reset();
            getGraph().getNodes().forEach(g -> manageSnakeLines(g, layoutParameters));
            manageSnakeLines(getGraph(), layoutParameters);
        }
    }
}
