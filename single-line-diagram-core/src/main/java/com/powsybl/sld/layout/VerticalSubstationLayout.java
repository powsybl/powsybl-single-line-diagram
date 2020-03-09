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
public class VerticalSubstationLayout extends AbstractSubstationLayout {

    public VerticalSubstationLayout(SubstationGraph graph, VoltageLevelLayoutFactory vLayoutFactory) {
        super(graph, vLayoutFactory, CompactionType.NONE);
    }

    /**
     * Calculate relative coordinate of voltageLevels in the substation
     */
    @Override
    protected Coord calculateCoordVoltageLevel(LayoutParameters layoutParam, Graph vlGraph) {
        int maxV = vlGraph.getMaxV();
        return new Coord(0, layoutParam.getInitialYBus() + layoutParam.getStackHeight() + layoutParam.getExternCellHeight() + layoutParam.getVerticalSpaceBus() * (maxV + 2));
    }

    /*
     * Calculate polyline points of a snakeLine in the substation graph
     */
    @Override
    protected List<Double> calculatePolylineSnakeLine(LayoutParameters layoutParam, Node node1, Node node2, boolean increment) {
        InfoCalcPoints info = new InfoCalcPoints(layoutParam, node1, node2, increment);
        info.setAdjacentGraphs(graph.graphAdjacents(node1.getGraph(), node2.getGraph()));

        int maxH1 = node1.getGraph().getNodeBuses().stream()
                .mapToInt(nodeBus -> nodeBus.getPosition().getH() + nodeBus.getPosition().getHSpan())
                .max().orElse(0);
        int maxH2 = node2.getGraph().getNodeBuses().stream()
                .mapToInt(nodeBus -> nodeBus.getPosition().getH() + nodeBus.getPosition().getHSpan())
                .max().orElse(0);
        double maxH = layoutParam.getTranslateX() +
                addAndGetNbSnakeLinesLeftRight(Side.LEFT, 0) * layoutParam.getHorizontalSnakeLinePadding() +
                layoutParam.getInitialXBus() +
                (Math.max(maxH1, maxH2)) * layoutParam.getCellWidth();
        info.setMaxH(maxH);

        return calculatePolylinePoints(info);
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
                    double decal1V = addAndGetNbSnakeLinesBottomVL(info.getGraphId1(), valIncr) * info.getLayoutParam().getVerticalSnakeLinePadding();
                    double decal2V = addAndGetNbSnakeLinesBottomVL(info.getGraphId2(), valIncr) * info.getLayoutParam().getVerticalSnakeLinePadding();
                    double xSnakeLine = info.getMaxH() + addAndGetNbSnakeLinesLeftRight(Side.LEFT, 1) * info.getLayoutParam().getHorizontalSnakeLinePadding();

                    pol.addAll(Arrays.asList(info.getX1(), info.getY1(),
                            info.getX1(), info.getInitY1() + decal1V,
                            xSnakeLine, info.getInitY1() + decal1V,
                            xSnakeLine, info.getInitY2() + decal2V,
                            info.getX2(), info.getInitY2() + decal2V,
                            info.getX2(), info.getY2()));
                } else {  // BOTTOM to TOP
                    if (!info.isAdjacentGraphs()) {
                        double decal1V = addAndGetNbSnakeLinesBottomVL(info.getGraphId1(), valIncr) * info.getLayoutParam().getVerticalSnakeLinePadding();
                        double decal2V = addAndGetNbSnakeLinesTopVL(info.getGraphId2(), valIncr) * info.getLayoutParam().getVerticalSnakeLinePadding();
                        double xSnakeLine = info.getMaxH() + addAndGetNbSnakeLinesLeftRight(Side.RIGHT, 1) * info.getLayoutParam().getHorizontalSnakeLinePadding();

                        pol.addAll(Arrays.asList(info.getX1(), info.getY1(),
                                info.getX1(), info.getInitY1() + decal1V,
                                xSnakeLine, info.getInitY1() + decal1V,
                                xSnakeLine, info.getInitY2() - decal2V,
                                info.getX2(), info.getInitY2() - decal2V,
                                info.getX2(), info.getY2()));
                    } else {  // node1 and node2 adjacent and node1 before node2
                        double decal1V = addAndGetNbSnakeLinesBottomVL(info.getGraphId1(), valIncr) * info.getLayoutParam().getVerticalSnakeLinePadding();
                        double decal2V = addAndGetNbSnakeLinesTopVL(info.getGraphId2(), valIncr) * info.getLayoutParam().getVerticalSnakeLinePadding();
                        double ySnakeLine = Math.max(info.getInitY1() + decal1V, info.getInitY2() - decal2V);

                        pol.addAll(Arrays.asList(info.getX1(), info.getY1(),
                                info.getX1(), ySnakeLine,
                                info.getX2(), ySnakeLine,
                                info.getX2(), info.getY2()));
                    }
                }
                break;

            case TOP:
                if (info.getdNode2() == BusCell.Direction.TOP) {  // TOP to TOP
                    double decal1V = addAndGetNbSnakeLinesTopVL(info.getGraphId1(), valIncr) * info.getLayoutParam().getVerticalSnakeLinePadding();
                    double decal2V = addAndGetNbSnakeLinesTopVL(info.getGraphId2(), valIncr) * info.getLayoutParam().getVerticalSnakeLinePadding();
                    double xSnakeLine = info.getxMinGraph() - addAndGetNbSnakeLinesLeftRight(Side.LEFT, 1) * info.getLayoutParam().getHorizontalSnakeLinePadding();

                    pol.addAll(Arrays.asList(info.getX1(), info.getY1(),
                            info.getX1(), info.getInitY1() - decal1V,
                            xSnakeLine, info.getInitY1() - decal1V,
                            xSnakeLine, info.getInitY2() - decal2V,
                            info.getX2(), info.getInitY2() - decal2V,
                            info.getX2(), info.getY2()));
                } else {  // TOP to BOTTOM
                    if (!info.isAdjacentGraphs()) {
                        double decal1V = addAndGetNbSnakeLinesTopVL(info.getGraphId1(), valIncr) * info.getLayoutParam().getVerticalSnakeLinePadding();
                        double decal2V = addAndGetNbSnakeLinesBottomVL(info.getGraphId2(), valIncr) * info.getLayoutParam().getVerticalSnakeLinePadding();
                        double xSnakeLine = info.getxMinGraph() - addAndGetNbSnakeLinesLeftRight(Side.LEFT, 1) * info.getLayoutParam().getHorizontalSnakeLinePadding();

                        pol.addAll(Arrays.asList(info.getX1(), info.getY1(),
                                info.getX1(), info.getInitY1() - decal1V,
                                xSnakeLine, info.getInitY1() - decal1V,
                                xSnakeLine, info.getInitY2() + decal2V,
                                info.getX2(), info.getInitY2() + decal2V,
                                info.getX2(), info.getY2()));
                    } else {  // node1 and node2 adjacent and node2 before node1
                        double decal1V = addAndGetNbSnakeLinesTopVL(info.getGraphId1(), valIncr) * info.getLayoutParam().getVerticalSnakeLinePadding();
                        double decal2V = addAndGetNbSnakeLinesBottomVL(info.getGraphId2(), valIncr) * info.getLayoutParam().getVerticalSnakeLinePadding();
                        double ySnakeLine = Math.max(info.getInitY1() - decal1V, info.getInitY2() + decal2V);

                        pol.addAll(Arrays.asList(info.getX1(), info.getY1(),
                                info.getX1(), ySnakeLine,
                                info.getX2(), ySnakeLine,
                                info.getX2(), info.getY2()));
                    }
                }
                break;
            default:
        }
        return pol;
    }

    @Override
    protected double getHorizontalSubstationPadding(LayoutParameters layoutParameters) {
        return 0;
    }
}
