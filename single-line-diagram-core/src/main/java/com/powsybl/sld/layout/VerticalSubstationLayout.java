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

import static com.powsybl.sld.model.Position.Dimension.H;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class VerticalSubstationLayout extends AbstractSubstationLayout {

    public VerticalSubstationLayout(SubstationGraph graph, VoltageLevelLayoutFactory vLayoutFactory) {
        super(graph, vLayoutFactory);
    }

    /**
     * Calculate relative coordinate of voltageLevels in the substation
     */
    @Override
    protected Coord calculateCoordVoltageLevel(LayoutParameters layoutParam, VoltageLevelGraph vlGraph) {
        int maxV = vlGraph.getMaxV();
        return new Coord(0, layoutParam.getInitialYBus() + layoutParam.getStackHeight() + layoutParam.getExternCellHeight() + layoutParam.getVerticalSpaceBus() * (maxV + 2));
    }

    /*
     * Calculate polyline points of a snakeLine in the substation graph
     */
    @Override
    protected List<Double> calculatePolylineSnakeLine(LayoutParameters layoutParam, Node node1, Node node2, InfosNbSnakeLines infosNbSnakeLines,
                                                      boolean increment) {

        if (node1.getGraph() == node2.getGraph()) { // in the same VL (so far always horizontal layout)
            String graphId = node1.getGraph().getId();

            // Reset the horizontal layout numbers to current graph numbers
            Integer currentNbBottom = infosNbSnakeLines.getNbSnakeLinesBottomVL().get(graphId);
            Integer currentNbTop = infosNbSnakeLines.getNbSnakeLinesTopVL().get(graphId);
            infosNbSnakeLines.getNbSnakeLinesTopBottom().put(BusCell.Direction.BOTTOM, currentNbBottom);
            infosNbSnakeLines.getNbSnakeLinesTopBottom().put(BusCell.Direction.TOP, currentNbTop);

            // Calculate the snakeline as an horizontal layout
            List<Double> snakeLine = calculatePolylineSnakeLineForHorizontalLayout(layoutParam, node1, node2, infosNbSnakeLines, increment);

            // Update the vertical layout maps
            Integer updatedNbLinesBottom = infosNbSnakeLines.getNbSnakeLinesTopBottom().get(BusCell.Direction.BOTTOM);
            Integer updatedNbLinesTop = infosNbSnakeLines.getNbSnakeLinesTopBottom().get(BusCell.Direction.TOP);
            infosNbSnakeLines.getNbSnakeLinesBottomVL().put(graphId, updatedNbLinesBottom);
            infosNbSnakeLines.getNbSnakeLinesTopVL().put(graphId, updatedNbLinesTop);

            return snakeLine;
        }

        BusCell.Direction dNode1 = getNodeDirection(node1, 1);
        BusCell.Direction dNode2 = getNodeDirection(node2, 2);

        double xMinGraph = node1.getGraph().getX() < node2.getGraph().getX() ? node1.getGraph().getX() : node2.getGraph().getX();

        double x1 = node1.getX();
        double y1 = node1.getY();
        double initY1 = node1.getInitY() != -1 ? node1.getInitY() : y1;
        double x2 = node2.getX();
        double y2 = node2.getY();
        double initY2 = node2.getInitY() != -1 ? node2.getInitY() : y2;

        int maxH1 = node1.getGraph().getNodeBuses().stream()
                .mapToInt(nodeBus -> nodeBus.getPosition().get(H) + nodeBus.getPosition().getSpan(H))
                .max().orElse(0);
        int maxH2 = node2.getGraph().getNodeBuses().stream()
                .mapToInt(nodeBus -> nodeBus.getPosition().get(H) + nodeBus.getPosition().getSpan(H))
                .max().orElse(0);
        double maxH = layoutParam.getTranslateX() +
                infosNbSnakeLines.getNbSnakeLinesLeftRight().get(Side.LEFT) * layoutParam.getHorizontalSnakeLinePadding() +
                layoutParam.getInitialXBus() +
                (Math.max(maxH1, maxH2)) * layoutParam.getCellWidth();

        List<Double> pol = new ArrayList<>();
        switch (dNode1) {
            case BOTTOM:
                if (dNode2 == BusCell.Direction.BOTTOM) {  // BOTTOM to BOTTOM
                    if (increment) {
                        infosNbSnakeLines.getNbSnakeLinesBottomVL().compute(node1.getGraph().getVoltageLevelInfos().getId(), (k, v) -> v + 1);
                        infosNbSnakeLines.getNbSnakeLinesBottomVL().compute(node2.getGraph().getVoltageLevelInfos().getId(), (k, v) -> v + 1);
                    }
                    double decal1V = infosNbSnakeLines.getNbSnakeLinesBottomVL().get(node1.getGraph().getVoltageLevelInfos().getId()) * layoutParam.getVerticalSnakeLinePadding();
                    double decal2V = infosNbSnakeLines.getNbSnakeLinesBottomVL().get(node2.getGraph().getVoltageLevelInfos().getId()) * layoutParam.getVerticalSnakeLinePadding();

                    infosNbSnakeLines.getNbSnakeLinesLeftRight().compute(Side.RIGHT, (k, v) -> v + 1);
                    double xSnakeLine = maxH + infosNbSnakeLines.getNbSnakeLinesLeftRight().get(Side.RIGHT) * layoutParam.getHorizontalSnakeLinePadding();
                    pol.addAll(Arrays.asList(x1, y1,
                        x1, initY1 + decal1V,
                        xSnakeLine, initY1 + decal1V,
                        xSnakeLine, initY2 + decal2V,
                        x2, initY2 + decal2V,
                        x2, y2));
                } else {  // BOTTOM to TOP
                    if (!getGraph().graphAdjacents(node1.getGraph(), node2.getGraph())) {
                        if (increment) {
                            infosNbSnakeLines.getNbSnakeLinesBottomVL().compute(node1.getGraph().getVoltageLevelInfos().getId(), (k, v) -> v + 1);
                            infosNbSnakeLines.getNbSnakeLinesTopVL().compute(node2.getGraph().getVoltageLevelInfos().getId(), (k, v) -> v + 1);
                        }
                        infosNbSnakeLines.getNbSnakeLinesLeftRight().compute(Side.RIGHT, (k, v) -> v + 1);
                        double decal1V = infosNbSnakeLines.getNbSnakeLinesBottomVL().get(node1.getGraph().getVoltageLevelInfos().getId()) * layoutParam.getVerticalSnakeLinePadding();
                        double decal2V = infosNbSnakeLines.getNbSnakeLinesTopVL().get(node2.getGraph().getVoltageLevelInfos().getId()) * layoutParam.getVerticalSnakeLinePadding();
                        double xSnakeLine = maxH + infosNbSnakeLines.getNbSnakeLinesLeftRight().get(Side.RIGHT) * layoutParam.getHorizontalSnakeLinePadding();

                        pol.addAll(Arrays.asList(x1, y1,
                                x1, initY1 + decal1V,
                                xSnakeLine, initY1 + decal1V,
                                xSnakeLine, initY2 - decal2V,
                                x2, initY2 - decal2V,
                                x2, y2));
                    } else {  // node1 and node2 adjacent and node1 before node2
                        if (increment) {
                            infosNbSnakeLines.getNbSnakeLinesBottomVL().compute(node1.getGraph().getVoltageLevelInfos().getId(), (k, v) -> v + 1);
                            infosNbSnakeLines.getNbSnakeLinesTopVL().compute(node2.getGraph().getVoltageLevelInfos().getId(), (k, v) -> v + 1);
                        }
                        double decal1V = infosNbSnakeLines.getNbSnakeLinesBottomVL().get(node1.getGraph().getVoltageLevelInfos().getId()) * layoutParam.getVerticalSnakeLinePadding();
                        double decal2V = infosNbSnakeLines.getNbSnakeLinesTopVL().get(node2.getGraph().getVoltageLevelInfos().getId()) * layoutParam.getVerticalSnakeLinePadding();
                        double ySnakeLine = Math.max(initY1 + decal1V, initY2 - decal2V);

                        pol.addAll(Arrays.asList(x1, y1,
                                x1, ySnakeLine,
                                x2, ySnakeLine,
                                x2, y2));
                    }
                }
                break;

            case TOP:
                if (dNode2 == BusCell.Direction.TOP) {  // TOP to TOP
                    if (increment) {
                        infosNbSnakeLines.getNbSnakeLinesTopVL().compute(node1.getGraph().getVoltageLevelInfos().getId(), (k, v) -> v + 1);
                        infosNbSnakeLines.getNbSnakeLinesTopVL().compute(node2.getGraph().getVoltageLevelInfos().getId(), (k, v) -> v + 1);
                    }
                    double decal1V = infosNbSnakeLines.getNbSnakeLinesTopVL().get(node1.getGraph().getVoltageLevelInfos().getId()) * layoutParam.getVerticalSnakeLinePadding();
                    double decal2V = infosNbSnakeLines.getNbSnakeLinesTopVL().get(node2.getGraph().getVoltageLevelInfos().getId()) * layoutParam.getVerticalSnakeLinePadding();

                    infosNbSnakeLines.getNbSnakeLinesLeftRight().compute(Side.LEFT, (k, v) -> v + 1);
                    double xSnakeLine = xMinGraph - infosNbSnakeLines.getNbSnakeLinesLeftRight().get(Side.LEFT) * layoutParam.getHorizontalSnakeLinePadding();
                    pol.addAll(Arrays.asList(x1, y1,
                        x1, initY1 - decal1V,
                        xSnakeLine, initY1 - decal1V,
                        xSnakeLine, initY2 - decal2V,
                        x2, initY2 - decal2V,
                        x2, y2));

                } else {  // TOP to BOTTOM
                    if (!getGraph().graphAdjacents(node2.getGraph(), node1.getGraph())) {
                        if (increment) {
                            infosNbSnakeLines.getNbSnakeLinesTopVL().compute(node1.getGraph().getVoltageLevelInfos().getId(), (k, v) -> v + 1);
                            infosNbSnakeLines.getNbSnakeLinesBottomVL().compute(node2.getGraph().getVoltageLevelInfos().getId(), (k, v) -> v + 1);
                        }
                        infosNbSnakeLines.getNbSnakeLinesLeftRight().compute(Side.LEFT, (k, v) -> v + 1);
                        double decal1V = infosNbSnakeLines.getNbSnakeLinesTopVL().get(node1.getGraph().getVoltageLevelInfos().getId()) * layoutParam.getVerticalSnakeLinePadding();
                        double decal2V = infosNbSnakeLines.getNbSnakeLinesBottomVL().get(node2.getGraph().getVoltageLevelInfos().getId()) * layoutParam.getVerticalSnakeLinePadding();
                        double xSnakeLine = xMinGraph - infosNbSnakeLines.getNbSnakeLinesLeftRight().get(Side.LEFT) * layoutParam.getHorizontalSnakeLinePadding();

                        pol.addAll(Arrays.asList(x1, y1,
                                x1, initY1 - decal1V,
                                xSnakeLine, initY1 - decal1V,
                                xSnakeLine, initY2 + decal2V,
                                x2, initY2 + decal2V,
                                x2, y2));
                    } else {  // node1 and node2 adjacent and node2 before node1
                        if (increment) {
                            infosNbSnakeLines.getNbSnakeLinesTopVL().compute(node1.getGraph().getVoltageLevelInfos().getId(), (k, v) -> v + 1);
                            infosNbSnakeLines.getNbSnakeLinesBottomVL().compute(node2.getGraph().getVoltageLevelInfos().getId(), (k, v) -> v + 1);
                        }
                        double decal1V = infosNbSnakeLines.getNbSnakeLinesTopVL().get(node1.getGraph().getVoltageLevelInfos().getId()) * layoutParam.getVerticalSnakeLinePadding();
                        double decal2V = infosNbSnakeLines.getNbSnakeLinesBottomVL().get(node2.getGraph().getVoltageLevelInfos().getId()) * layoutParam.getVerticalSnakeLinePadding();
                        double ySnakeLine = Math.max(initY1 - decal1V, initY2 + decal2V);

                        pol.addAll(Arrays.asList(x1, y1,
                                x1, ySnakeLine,
                                x2, ySnakeLine,
                                x2, y2));
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

    @Override
    protected double getVerticalSubstationPadding(LayoutParameters layoutParameters) {
        return layoutParameters.getVerticalSubstationPadding();
    }
}
