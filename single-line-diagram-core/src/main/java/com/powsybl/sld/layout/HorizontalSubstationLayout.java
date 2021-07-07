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

    public HorizontalSubstationLayout(SubstationGraph graph, VoltageLevelLayoutFactory vLayoutFactory) {
        super(graph, vLayoutFactory);
    }

    @Override
    protected List<Point> calculatePolylineSnakeLine(LayoutParameters layoutParam, Node node1, Node node2,
                                                     InfosNbSnakeLines infosNbSnakeLines, boolean increment) {
        return calculatePolylineSnakeLineForHorizontalLayout(layoutParam, node1, node2, infosNbSnakeLines, increment);
    }

    /**
     * Calculate relative coordinate of voltageLevel in the substation
     */
    @Override
    protected Coord calculateCoordVoltageLevel(LayoutParameters layoutParam, VoltageLevelGraph vlGraph) {
        double elementaryWidth = layoutParam.getCellWidth() / 2; // the elementary step within a voltageLevel Graph is half a cell width
        int maxH = vlGraph.getMaxH();
        int betweenVlHSpan = 4; // to leave enough space for lines and transformer between voltage levels
        return new Coord(layoutParam.getInitialXBus() + (maxH + betweenVlHSpan) * elementaryWidth, 0);
    }

    @Override
    protected double getHorizontalSubstationPadding(LayoutParameters layoutParameters) {
        return layoutParameters.getHorizontalSubstationPadding();
    }

    @Override
    protected double getVerticalSubstationPadding(LayoutParameters layoutParameters) {
        return 0;
    }
}
