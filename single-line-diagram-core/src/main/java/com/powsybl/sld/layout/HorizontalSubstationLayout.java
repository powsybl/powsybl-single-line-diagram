/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout;

import com.powsybl.sld.model.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class HorizontalSubstationLayout extends AbstractSubstationLayout {

    public HorizontalSubstationLayout(SubstationGraph graph, VoltageLevelLayoutFactory vLayoutFactory) {
        super(graph, vLayoutFactory, CompactionType.NONE);
    }

    /**
     * Calculate relative coordinate of voltageLevel in the substation
     */
    @Override
    protected Coord calculateCoordVoltageLevel(LayoutParameters layoutParam, Graph vlGraph) {
        int maxH = vlGraph.getMaxH();
        return new Coord(layoutParam.getInitialXBus() + (maxH + 2) * layoutParam.getCellWidth(), 0);
    }

    /*
     * Calculate polyline points of a snakeLine in the substation graph
     */
    @Override
    public List<Double> calculatePolylinePoints(InfoCalcPoints info) {
        List<Double> pol = new ArrayList<>();

        int valIncr = info.isIncrement() ? 1 : 0;

        switch (info.getdNode1()) {
            case BOTTOM:
                if (info.getdNode2() == BusCell.Direction.BOTTOM) {  // BOTTOM to BOTTOM
                    double decalV = addAndGetNbSnakeLinesTopBottom(info.getdNode1(), valIncr) * info.getLayoutParam().getVerticalSnakeLinePadding();
                    double yDecal = Math.max(info.getInitY1() + decalV, info.getInitY2() + decalV);

                    pol.addAll(Arrays.asList(info.getX1(), info.getY1(),
                            info.getX1(), yDecal,
                            info.getX2(), yDecal,
                            info.getX2(), info.getY2()));

                } else {  // BOTTOM to TOP
                    double decal1V = addAndGetNbSnakeLinesTopBottom(info.getdNode1(), valIncr) * info.getLayoutParam().getVerticalSnakeLinePadding();
                    double decal2V = addAndGetNbSnakeLinesTopBottom(info.getdNode2(), valIncr) * info.getLayoutParam().getVerticalSnakeLinePadding();
                    double xBetweenGraph = info.getxMaxGraph() - (addAndGetNbSnakeLinesBetween(info.getIdMaxGraph(), 1) * info.getLayoutParam().getHorizontalSnakeLinePadding());

                    pol.addAll(Arrays.asList(info.getX1(), info.getY1(),
                            info.getX1(), info.getInitY1() + decal1V,
                            xBetweenGraph, info.getInitY1() + decal1V,
                            xBetweenGraph, info.getInitY2() - decal2V,
                            info.getX2(), info.getInitY2() - decal2V,
                            info.getX2(), info.getY2()));
                }
                break;

            case TOP:
                if (info.getdNode2() == BusCell.Direction.TOP) {  // TOP to TOP
                    double decalV = addAndGetNbSnakeLinesTopBottom(info.getdNode1(), valIncr) * info.getLayoutParam().getVerticalSnakeLinePadding();
                    double yDecal = Math.min(info.getInitY1() - decalV, info.getInitY2() - decalV);

                    pol.addAll(Arrays.asList(info.getX1(), info.getY1(),
                            info.getX1(), yDecal,
                            info.getX2(), yDecal,
                            info.getX2(), info.getY2()));
                } else {  // TOP to BOTTOM
                    double decal1V = addAndGetNbSnakeLinesTopBottom(info.getdNode1(), valIncr) * info.getLayoutParam().getVerticalSnakeLinePadding();
                    double decal2V = addAndGetNbSnakeLinesTopBottom(info.getdNode2(), valIncr) * info.getLayoutParam().getVerticalSnakeLinePadding();

                    double xBetweenGraph = info.getxMaxGraph() - (addAndGetNbSnakeLinesBetween(info.getIdMaxGraph(), 1) * info.getLayoutParam().getHorizontalSnakeLinePadding());

                    pol.addAll(Arrays.asList(info.getX1(), info.getY1(),
                            info.getX1(), info.getInitY1() - decal1V,
                            xBetweenGraph, info.getInitY1() - decal1V,
                            xBetweenGraph, info.getInitY2() + decal2V,
                            info.getX2(), info.getInitY2() + decal2V,
                            info.getX2(), info.getY2()));
                }
                break;
            default:
        }
        return pol;
    }

    @Override
    protected double getVerticalSubstationPadding(LayoutParameters layoutParameters) {
        return 0;
    }
}
