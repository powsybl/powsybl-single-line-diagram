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
            int currentNbBottom = infosNbSnakeLines.getNbSnakeLinesHorizontalBetween(graphId, BusCell.Direction.BOTTOM);
            int currentNbTop = infosNbSnakeLines.getNbSnakeLinesHorizontalBetween(graphId, BusCell.Direction.TOP);
            infosNbSnakeLines.getNbSnakeLinesTopBottom().put(BusCell.Direction.BOTTOM, currentNbBottom);
            infosNbSnakeLines.getNbSnakeLinesTopBottom().put(BusCell.Direction.TOP, currentNbTop);

            // Calculate the snakeline as an horizontal layout
            polyline = calculatePolylineSnakeLineForHorizontalLayout(layoutParam, node1, node2, infosNbSnakeLines, increment);

            // Update the vertical layout maps
            Integer updatedNbLinesBottom = infosNbSnakeLines.getNbSnakeLinesTopBottom().get(BusCell.Direction.BOTTOM);
            Integer updatedNbLinesTop = infosNbSnakeLines.getNbSnakeLinesTopBottom().get(BusCell.Direction.TOP);
            infosNbSnakeLines.setNbSnakeLinesHorizontalBetween(graphId, BusCell.Direction.BOTTOM, updatedNbLinesBottom);
            infosNbSnakeLines.setNbSnakeLinesHorizontalBetween(graphId, BusCell.Direction.TOP, updatedNbLinesTop);

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

        int maxVlH = getGraph().getNodes().stream().mapToInt(VoltageLevelGraph::getMaxH).max().orElse(0);
        double maxH = layoutParam.getTranslateX() +
            layoutParam.getInitialXBus() +
            (maxVlH + 1) * layoutParam.getCellWidth() / 2;

        // increment not needed for 3WT for the common node
        String vl1 = node1.getGraph().getVoltageLevelInfos().getId();
        int nbSnakeLines1 = increment
            ? infosNbSnakeLines.incrementAndGetNbSnakeLinesHorizontalBetween(vl1, dNode1)
            : infosNbSnakeLines.getNbSnakeLinesHorizontalBetween(vl1, dNode1);
        double decal1V = nbSnakeLines1 * layoutParam.getVerticalSnakeLinePadding();

        double x1 = node1.getCoordinates().getX();
        double x2 = node2.getCoordinates().getX();

        if (facingNodes(node1, node2)) {
            // if the two nodes are facing each other, no need to add more than 2 points (and one point is enough if same abscissa)
            double ySnakeLine = Math.min(node1.getCoordinates().getY(), node2.getCoordinates().getY()) + decal1V;
            if (x1 != x2) {
                polyline.add(new Point(x1, ySnakeLine));
                polyline.add(new Point(x2, ySnakeLine));
            } else {
                polyline.add(new Point(x1, ySnakeLine));
            }
        } else {
            String vl2 = node2.getGraph().getVoltageLevelInfos().getId();
            int nbSnakeLines2 = infosNbSnakeLines.incrementAndGetNbSnakeLinesHorizontalBetween(vl2, dNode2);
            double decal2V = nbSnakeLines2 * layoutParam.getVerticalSnakeLinePadding();

            double ySnakeLine1 = getYSnakeLine(node1, dNode1, decal1V, layoutParam);
            double ySnakeLine2 = getYSnakeLine(node2, dNode2, decal2V, layoutParam);

            Side side = ((increment && dNode1 == BusCell.Direction.BOTTOM) || (!increment && dNode1 == BusCell.Direction.TOP)) ? Side.RIGHT : Side.LEFT;
            infosNbSnakeLines.getNbSnakeLinesLeftRight().compute(side, (k, v) -> v + 1);
            double xSnakeLine = side == Side.LEFT
                ? layoutParam.getHorizontalSubstationPadding() - infosNbSnakeLines.getNbSnakeLinesLeftRight().get(side) * layoutParam.getHorizontalSnakeLinePadding()
                : maxH + infosNbSnakeLines.getNbSnakeLinesLeftRight().get(side) * layoutParam.getHorizontalSnakeLinePadding();
            polyline.addAll(Point.createPointsList(x1, ySnakeLine1,
                xSnakeLine, ySnakeLine1,
                xSnakeLine, ySnakeLine2,
                x2, ySnakeLine2));
        }
    }

    private double getYSnakeLine(Node node, BusCell.Direction dNode1, double decalV, LayoutParameters layoutParam) {
        if (dNode1 == BusCell.Direction.BOTTOM) {
            return node.getCoordinates().getY() + decalV;
        } else {
            List<VoltageLevelGraph> vls = getGraph().getNodes();
            if (vls.get(0) == node.getGraph()) {
                return node.getCoordinates().getY() - decalV;
            } else {
                int iVl = vls.indexOf(node.getGraph());
                VoltageLevelGraph vlAbove = vls.get(iVl - 1);
                return vlAbove.getY() + layoutParam.getInitialYBus() + layoutParam.getVerticalSpaceBus() * (vlAbove.getMaxV() - 1) + layoutParam.getStackHeight() + layoutParam.getExternCellHeight()
                    + decalV;
            }
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
