/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout;

import com.powsybl.commons.PowsyblException;
import com.powsybl.sld.model.*;
import com.powsybl.sld.model.BusCell.Direction;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.powsybl.sld.model.Coord.Dimension.X;
import static com.powsybl.sld.model.Coord.Dimension.Y;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public abstract class AbstractSubstationLayout implements Layout {

    protected SubstationGraph graph;
    protected VoltageLevelLayoutFactory vLayoutFactory;

    protected class InfosNbSnakeLines {
        public InfosNbSnakeLines(Map<Direction, Integer> nbSnakeLinesTopBottom,
                                 Map<String, Integer> nbSnakeLinesBetween,
                                 Map<Side, Integer> nbSnakeLinesLeftRight,
                                 Map<String, Integer> nbSnakeLinesBottomVL,
                                 Map<String, Integer> nbSnakeLinesTopVL) {
            this.nbSnakeLinesTopBottom = nbSnakeLinesTopBottom;
            this.nbSnakeLinesBetween = nbSnakeLinesBetween;
            this.nbSnakeLinesLeftRight = nbSnakeLinesLeftRight;
            this.nbSnakeLinesBottomVL = nbSnakeLinesBottomVL;
            this.nbSnakeLinesTopVL = nbSnakeLinesTopVL;
        }

        public Map<Direction, Integer> getNbSnakeLinesTopBottom() {
            return nbSnakeLinesTopBottom;
        }

        public Map<String, Integer> getNbSnakeLinesBetween() {
            return nbSnakeLinesBetween;
        }

        public Map<Side, Integer> getNbSnakeLinesLeftRight() {
            return nbSnakeLinesLeftRight;
        }

        public Map<String, Integer> getNbSnakeLinesBottomVL() {
            return nbSnakeLinesBottomVL;
        }

        public Map<String, Integer> getNbSnakeLinesTopVL() {
            return nbSnakeLinesTopVL;
        }

        private Map<BusCell.Direction, Integer> nbSnakeLinesTopBottom;
        private Map<String, Integer> nbSnakeLinesBetween;
        private Map<Side, Integer> nbSnakeLinesLeftRight;
        private Map<String, Integer> nbSnakeLinesBottomVL;
        private Map<String, Integer> nbSnakeLinesTopVL;
    }

    public AbstractSubstationLayout(SubstationGraph graph, VoltageLevelLayoutFactory vLayoutFactory) {
        this.graph = Objects.requireNonNull(graph);
        this.vLayoutFactory = Objects.requireNonNull(vLayoutFactory);
    }

    @Override
    public void run(LayoutParameters layoutParameters) {
        // Calculate all the coordinates for the voltageLevel graphs in the substation graph
        double graphX = layoutParameters.getHorizontalSubstationPadding();
        double graphY = layoutParameters.getVerticalSubstationPadding();

        for (Graph vlGraph : graph.getNodes()) {
            vlGraph.setX(graphX);
            vlGraph.setY(graphY);

            // Calculate the objects coordinates inside the voltageLevel graph
            Layout vLayout = vLayoutFactory.create(vlGraph);
            vLayout.run(layoutParameters);

            // Calculate the global coordinate of the voltageLevel graph
            Coord posVLGraph = calculateCoordVoltageLevel(layoutParameters, vlGraph);

            graphX += posVLGraph.get(X) + getHorizontalSubstationPadding(layoutParameters);
            graphY += posVLGraph.get(Y) + getVerticalSubstationPadding(layoutParameters);
        }

        // Calculate all the coordinates for the middle nodes and the snake lines between the voltageLevel graphs
        manageSnakeLines(layoutParameters);
    }

    protected abstract Coord calculateCoordVoltageLevel(LayoutParameters layoutParameters, Graph vlGraph);

    protected abstract double getHorizontalSubstationPadding(LayoutParameters layoutParameters);

    protected abstract double getVerticalSubstationPadding(LayoutParameters layoutParameters);

    protected abstract List<Double> calculatePolylineSnakeLine(LayoutParameters layoutParameters, Node node1, Node node2,
                                                               InfosNbSnakeLines infosNbSnakeLines, boolean increment);

    protected BusCell.Direction getNodeDirection(Node node, int nb) {
        if (node.getType() != Node.NodeType.FEEDER) {
            throw new PowsyblException("Node " + nb + " is not a feeder node");
        }
        BusCell.Direction dNode = node.getCell() != null ? ((ExternCell) node.getCell()).getDirection() : BusCell.Direction.TOP;
        if (dNode != BusCell.Direction.TOP && dNode != BusCell.Direction.BOTTOM) {
            throw new PowsyblException("Node " + nb + " cell direction not TOP or BOTTOM");
        }
        return dNode;
    }

    protected void manageSnakeLines(LayoutParameters layoutParameters) {
        // used only for horizontal layout
        Map<BusCell.Direction, Integer> nbSnakeLinesTopBottom = EnumSet.allOf(BusCell.Direction.class).stream().collect(Collectors.toMap(Function.identity(), v -> 0));
        Map<String, Integer> nbSnakeLinesBetween = graph.getNodes().stream().collect(Collectors.toMap(g -> g.getVoltageLevelInfos().getId(), v -> 0));

        // used only for vertical layout
        Map<Side, Integer> nbSnakeLinesLeftRight = EnumSet.allOf(Side.class).stream().collect(Collectors.toMap(Function.identity(), v -> 0));
        Map<String, Integer> nbSnakeLinesBottomVL = graph.getNodes().stream().collect(Collectors.toMap(g -> g.getVoltageLevelInfos().getId(), v -> 0));
        Map<String, Integer> nbSnakeLinesTopVL = graph.getNodes().stream().collect(Collectors.toMap(g -> g.getVoltageLevelInfos().getId(), v -> 0));

        InfosNbSnakeLines infos = new InfosNbSnakeLines(nbSnakeLinesTopBottom, nbSnakeLinesBetween,
                nbSnakeLinesLeftRight, nbSnakeLinesBottomVL, nbSnakeLinesTopVL);

        for (Node multiNode : graph.getMultiTermNodes()) {
            List<Edge> adjacentEdges = multiNode.getAdjacentEdges();
            List<Node> adjacentNodes = multiNode.getAdjacentNodes();
            if (adjacentNodes.size() == 2) {
                List<Double> pol = calculatePolylineSnakeLine(layoutParameters, adjacentNodes.get(0), adjacentNodes.get(1), infos, true);
                Coord coordNodeFict = new Coord(-1, -1);
                ((TwtEdge) adjacentEdges.get(0)).setSnakeLine(splitPolyline2(pol, 1, coordNodeFict));
                ((TwtEdge) adjacentEdges.get(1)).setSnakeLine(splitPolyline2(pol, 2, null));
                multiNode.setX(coordNodeFict.get(X), false);
                multiNode.setY(coordNodeFict.get(Y), false);
            } else if (adjacentNodes.size() == 3) {
                List<Double> pol1 = calculatePolylineSnakeLine(layoutParameters, adjacentNodes.get(0), adjacentNodes.get(1), infos, true);
                List<Double> pol2 = calculatePolylineSnakeLine(layoutParameters, adjacentNodes.get(1), adjacentNodes.get(2), infos, false);
                Coord coordNodeFict = new Coord(-1, -1);
                ((TwtEdge) adjacentEdges.get(0)).setSnakeLine(splitPolyline3(pol1, pol2, 1, coordNodeFict));
                ((TwtEdge) adjacentEdges.get(1)).setSnakeLine(splitPolyline3(pol1, pol2, 2, null));
                ((TwtEdge) adjacentEdges.get(2)).setSnakeLine(splitPolyline3(pol1, pol2, 3, null));
                multiNode.setX(coordNodeFict.get(X), false);
                multiNode.setY(coordNodeFict.get(Y), false);
            }
        }
    }

    protected List<Double> splitPolyline2(List<Double> pol, int numPart, Coord coord) {
        List<Double> res = new ArrayList<>();

        double xSplit = 0;
        double ySplit = 0;
        if (pol.size() == 8) {
            xSplit = (pol.get(2) + pol.get(4)) / 2;
            ySplit = (pol.get(3) + pol.get(5)) / 2;
            if (numPart == 1) {
                res.addAll(pol.subList(0, 4));
                res.addAll(Arrays.asList(xSplit, ySplit));
            } else {
                res.addAll(Arrays.asList(xSplit, ySplit));
                res.addAll(pol.subList(4, 8));
            }
        } else if (pol.size() == 12) {
            xSplit = (pol.get(4) + pol.get(6)) / 2;
            ySplit = (pol.get(5) + pol.get(7)) / 2;
            if (numPart == 1) {
                res.addAll(pol.subList(0, 6));
                res.addAll(Arrays.asList(xSplit, ySplit));
            } else {
                res.addAll(Arrays.asList(xSplit, ySplit));
                res.addAll(pol.subList(6, 12));
            }
        }

        if (coord != null) {
            coord.set(X, xSplit);
            coord.set(Y, ySplit);
        }

        return res;
    }

    protected List<Double> splitPolyline3(List<Double> pol1, List<Double> pol2, int numPart, Coord coord) {
        List<Double> res = new ArrayList<>();

        if (numPart == 1) {
            // for the first new edge, we keep all the original first polyline points, except the last one
            res.addAll(pol1.subList(0, pol1.size() - 2));
            if (coord != null) {
                // the fictitious node point is the last point of the new edge polyline
                coord.set(X, pol1.get(pol1.size() - 4));
                coord.set(Y, pol1.get(pol1.size() - 3));
            }
        } else if (numPart == 2) {
            // for the second new edge, we keep the last two points of the original first polyline
            res.addAll(pol1.subList(pol1.size() - 4, pol1.size()));
        } else {
            // the third new edge is made with the original second polyline, except the first point
            res.addAll(pol2.subList(2, pol2.size()));
        }

        return res;
    }
}
