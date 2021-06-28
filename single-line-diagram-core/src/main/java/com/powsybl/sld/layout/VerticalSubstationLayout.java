/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout;

import com.powsybl.sld.model.*;

import java.util.ArrayList;
import java.util.List;

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
    protected List<Point> calculatePolylineSnakeLine(LayoutParameters layoutParam, Node node1, Node node2, InfosNbSnakeLines infosNbSnakeLines,
                                                      boolean increment) {
        List<Point> polyline;
        if (node1.getGraph() == node2.getGraph()) { // in the same VL (so far always horizontal layout)
            String graphId = node1.getGraph().getId();

            // Reset the horizontal layout numbers to current graph numbers
            Integer currentNbBottom = infosNbSnakeLines.getNbSnakeLinesBottomVL().get(graphId);
            Integer currentNbTop = infosNbSnakeLines.getNbSnakeLinesTopVL().get(graphId);
            infosNbSnakeLines.getNbSnakeLinesTopBottom().put(BusCell.Direction.BOTTOM, currentNbBottom);
            infosNbSnakeLines.getNbSnakeLinesTopBottom().put(BusCell.Direction.TOP, currentNbTop);

            // Calculate the snakeline as an horizontal layout
            polyline = calculatePolylineSnakeLineForHorizontalLayout(layoutParam, node1, node2, infosNbSnakeLines, increment);

            // Update the vertical layout maps
            Integer updatedNbLinesBottom = infosNbSnakeLines.getNbSnakeLinesTopBottom().get(BusCell.Direction.BOTTOM);
            Integer updatedNbLinesTop = infosNbSnakeLines.getNbSnakeLinesTopBottom().get(BusCell.Direction.TOP);
            infosNbSnakeLines.getNbSnakeLinesBottomVL().put(graphId, updatedNbLinesBottom);
            infosNbSnakeLines.getNbSnakeLinesTopVL().put(graphId, updatedNbLinesTop);

            return polyline;

        } else {
            polyline = new ArrayList<>();
            polyline.add(node1.getCoordinates());
            addMiddlePoints(layoutParam, node1, node2, infosNbSnakeLines, increment, polyline);
            polyline.add(node2.getCoordinates());
            return polyline;
        }
    }

    private void addMiddlePoints(LayoutParameters layoutParam, Node node1, Node node2, InfosNbSnakeLines infosNbSnakeLines,
                                 boolean increment, List<Point> polyline) {
        BusCell.Direction dNode1 = getNodeDirection(node1, 1);
        BusCell.Direction dNode2 = getNodeDirection(node2, 2);

        double xMinGraph = Math.min(node1.getGraph().getX(), node2.getGraph().getX());

        double x1 = node1.getCoordinates().getX();
        double y1 = node1.getCoordinates().getY();
        double x2 = node2.getCoordinates().getX();
        double y2 = node2.getCoordinates().getY();

        int maxVlH = getGraph().getNodes().stream().mapToInt(VoltageLevelGraph::getMaxH).max().orElse(0);
        double maxH = layoutParam.getTranslateX() +
            layoutParam.getInitialXBus() +
            (maxVlH + 1) * layoutParam.getCellWidth() / 2;

        String vl1 = node1.getGraph().getVoltageLevelInfos().getId();
        String vl2 = node2.getGraph().getVoltageLevelInfos().getId();
        if (increment) {
            // increment not needed for 3WT for the common node
            infosNbSnakeLines.getNbSnakeLinesVL(dNode1).compute(vl1, (k, v) -> v + 1);
        }
        infosNbSnakeLines.getNbSnakeLinesVL(dNode2).compute(vl2, (k, v) -> v + 1);
        double decal1V = getSgn(dNode1) * infosNbSnakeLines.getNbSnakeLinesVL(dNode1).get(vl1) * layoutParam.getVerticalSnakeLinePadding();
        double decal2V = getSgn(dNode2) * infosNbSnakeLines.getNbSnakeLinesVL(dNode2).get(vl2) * layoutParam.getVerticalSnakeLinePadding();

        if (facingNodes(node1, node2)) {
            // if the two nodes are facing each other, no need to add more than 2 points (and one point is enough if same abscissa)
            double ySnakeLine = Math.max(y1 + decal1V, y2 + decal2V);
            if (x1 != x2) {
                polyline.add(new Point(x1, ySnakeLine));
                polyline.add(new Point(x2, ySnakeLine));
            } else {
                polyline.add(new Point(x1, ySnakeLine));
            }
        } else {
            Side side = ((increment && dNode1 == BusCell.Direction.BOTTOM) || (!increment && dNode1 == BusCell.Direction.TOP)) ? Side.RIGHT : Side.LEFT;
            infosNbSnakeLines.getNbSnakeLinesLeftRight().compute(side, (k, v) -> v + 1);
            double xSnakeLine = side == Side.LEFT
                ? xMinGraph - infosNbSnakeLines.getNbSnakeLinesLeftRight().get(side) * layoutParam.getHorizontalSnakeLinePadding()
                : maxH + infosNbSnakeLines.getNbSnakeLinesLeftRight().get(side) * layoutParam.getHorizontalSnakeLinePadding();
            polyline.addAll(Point.createPointsList(x1, y1 + decal1V,
                xSnakeLine, y1 + decal1V,
                xSnakeLine, y2 + decal2V,
                x2, y2 + decal2V));
        }
    }

    private boolean facingNodes(Node node1, Node node2) {
        BusCell.Direction dNode1 = getNodeDirection(node1, 1);
        BusCell.Direction dNode2 = getNodeDirection(node2, 2);
        return (dNode1 == BusCell.Direction.BOTTOM && dNode2 == BusCell.Direction.TOP && getGraph().graphAdjacents(node1.getGraph(), node2.getGraph()))
            || (dNode1 == BusCell.Direction.TOP && dNode2 == BusCell.Direction.BOTTOM && getGraph().graphAdjacents(node2.getGraph(), node1.getGraph()));
    }

    private static double getSgn(BusCell.Direction direction) {
        return direction == BusCell.Direction.BOTTOM ? 1 : -1;
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
