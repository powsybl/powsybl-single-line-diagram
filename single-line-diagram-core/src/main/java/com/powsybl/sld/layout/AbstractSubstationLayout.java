/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.powsybl.sld.model.BusCell;
import com.powsybl.sld.model.Coord;
import com.powsybl.sld.model.FictitiousNode;
import com.powsybl.sld.model.Graph;
import com.powsybl.sld.model.Node;
import com.powsybl.sld.model.Side;
import com.powsybl.sld.model.SubstationGraph;
import com.powsybl.sld.model.TwtEdge;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public abstract class AbstractSubstationLayout implements SubstationLayout {

    protected SubstationGraph graph;
    protected VoltageLevelLayoutFactory vLayoutFactory;
    protected Map<Graph, VoltageLevelLayout> vlLayouts = new HashMap<>();

    protected Map<String, Map<BusCell.Direction, Integer>> nbSnakeLinesTopBottom;
    protected Map<String, Integer> nbSnakeLinesBetween;

    private Map<Side, Integer> nbSnakeLinesLeftRight;
    private Map<String, Integer> nbSnakeLinesBottomVL;
    private Map<String, Integer> nbSnakeLinesTopVL;

    protected CompactionType compactionType;

    public AbstractSubstationLayout(SubstationGraph graph,
                                    VoltageLevelLayoutFactory vLayoutFactory,
                                    CompactionType compactionType) {
        this.graph = graph;
        this.vLayoutFactory = Objects.requireNonNull(vLayoutFactory);
        this.compactionType = compactionType;
    }

    @Override
    public void run(LayoutParameters layoutParameters, boolean applyVLLayouts,
                    boolean manageSnakeLines, Map<Graph, VoltageLevelLayout> mapVLayouts) {
        if (applyVLLayouts) {
            // Calculate all the coordinates for the voltageLevel graphs in the substation graph
            double graphX = graph.getX() + layoutParameters.getHorizontalSubstationPadding();
            double graphY = graph.getY() + layoutParameters.getVerticalSubstationPadding();

            if (mapVLayouts == null) {
                vlLayouts.clear();
            }
            for (Graph vlGraph : graph.getNodes()) {
                vlGraph.setX(graphX);
                vlGraph.setY(graphY);

                // Calculate the objects coordinates inside the voltageLevel graph
                VoltageLevelLayout vLayout = mapVLayouts == null
                        ? vLayoutFactory.create(vlGraph)
                        : mapVLayouts.get(vlGraph);
                vLayout.run(layoutParameters);
                vlLayouts.put(vlGraph, vLayout);

                // Calculate the global coordinate of the voltageLevel graph
                Coord posVLGraph = calculateCoordVoltageLevel(layoutParameters, vlGraph);

                graphX += posVLGraph.getX() + getHorizontalSubstationPadding(layoutParameters);
                graphY += posVLGraph.getY() + getVerticalSubstationPadding(layoutParameters);
            }
        }

        if (manageSnakeLines) {
            // Calculate all the coordinates for the links between the voltageLevel graphs
            // (new fictitious nodes and new edges are introduced in this stage)
            manageSnakeLines(layoutParameters);
        }
    }

    protected Coord calculateCoordVoltageLevel(LayoutParameters layoutParameters, Graph vlGraph) {
        return null;
    }

    protected double getHorizontalSubstationPadding(LayoutParameters layoutParameters) {
        return layoutParameters.getHorizontalSubstationPadding();
    }

    protected double getVerticalSubstationPadding(LayoutParameters layoutParameters) {
        return layoutParameters.getVerticalSubstationPadding();
    }

    protected List<Double> calculatePolylineSnakeLine(LayoutParameters layoutParameters, Node node1, Node node2, boolean increment) {
        InfoCalcPoints info = new InfoCalcPoints(layoutParameters, node1, node2, increment);

        return calculatePolylinePoints(info);
    }

    protected abstract List<Double> calculatePolylinePoints(InfoCalcPoints info);

    protected void initInfosSnakeLines() {
        nbSnakeLinesTopBottom = new HashMap<>();
        graph.getNodes().stream().forEach(g -> nbSnakeLinesTopBottom.put(g.getVoltageLevelId(), EnumSet.allOf(BusCell.Direction.class).stream().collect(Collectors.toMap(Function.identity(), v -> 0))));

        nbSnakeLinesBetween = graph.getNodes().stream().collect(Collectors.toMap(Graph::getVoltageLevelId, v -> 0));

        nbSnakeLinesLeftRight = EnumSet.allOf(Side.class).stream().collect(Collectors.toMap(Function.identity(), v -> 0));
        nbSnakeLinesBottomVL = graph.getNodes().stream().collect(Collectors.toMap(Graph::getVoltageLevelId, v -> 0));
        nbSnakeLinesTopVL = graph.getNodes().stream().collect(Collectors.toMap(Graph::getVoltageLevelId, v -> 0));
    }

    protected void manageSnakeLines(LayoutParameters layoutParameters) {
        initInfosSnakeLines();

        List<TwtEdge> newEdges = new ArrayList<>();

        for (TwtEdge edge : graph.getEdges()) {
            TwtEdge edgeSorted = new TwtEdge(edge);
            // Sorting the nodes in the edge by ascending x value, for the coordinates computation
            edgeSorted.getNodes().sort(Comparator.comparingDouble(Node::getX));

            if (edgeSorted.getNodes().size() == 2) {
                List<Double> pol = calculatePolylineSnakeLine(layoutParameters, edgeSorted.getNode1(), edgeSorted.getNode2(), true);
                // we split the original edge in two parts, with a new fictitious node between the two new edges
                splitEdge2(edgeSorted, newEdges, pol);
            } else if (edgeSorted.getNodes().size() == 3) {
                List<Double> pol1 = calculatePolylineSnakeLine(layoutParameters, edgeSorted.getNode1(), edgeSorted.getNode2(), true);
                List<Double> pol2 = calculatePolylineSnakeLine(layoutParameters, edgeSorted.getNode2(), edgeSorted.getNode3(), false);
                // we split the original edge in three parts, with a new fictitious node between the three new edges
                splitEdge3(edgeSorted, newEdges, pol1, pol2);
            }
        }

        // replace the old edges by the new edges in the substation graph
        graph.setEdges(newEdges);
    }

    protected void splitEdge2(TwtEdge edge, List<TwtEdge> edges, List<Double> pol) {
        // Creation of a new fictitious node outside any graph
        String idNodeFict = edge.getNode1().getId() + "_" + edge.getNode2().getId();
        Node nodeFict = new FictitiousNode(null, idNodeFict, edge.getComponentType());
        Coord coordNodeFict = new Coord(-1, -1);

        // Creation of a new edge between node1 and the new fictitious node
        TwtEdge edge1 = new TwtEdge(edge.getComponentType(), edge.getNode1(), nodeFict);
        edge1.setSnakeLine(splitPolyline2(pol, 1, coordNodeFict));
        edges.add(edge1);

        // Creation of a new edge between the new fictitious node and node2
        TwtEdge edge2 = new TwtEdge(edge.getComponentType(), nodeFict, edge.getNode2());
        edge2.setSnakeLine(splitPolyline2(pol, 2, null));
        edges.add(edge2);

        // Setting the coordinates of the new fictitious node
        nodeFict.setX(coordNodeFict.getX(), false, false);
        nodeFict.setY(coordNodeFict.getY(), false, false);
        nodeFict.addAdjacentEdge(edge1);
        nodeFict.addAdjacentEdge(edge2);

        // the new fictitious node is store in the substation graph
        graph.addMultiTermNode(nodeFict);
    }

    protected void splitEdge3(TwtEdge edge, List<TwtEdge> edges, List<Double> pol1, List<Double> pol2) {
        // Creation of a new fictitious node outside any graph
        String idNodeFict = edge.getNode1().getId() + "_" + edge.getNode2().getId() + "_" + edge.getNode3().getId();
        Node nodeFict = new FictitiousNode(null, idNodeFict, edge.getComponentType());
        Coord coordNodeFict = new Coord(-1, -1);

        // Creation of a new edge between node1 and the new fictitious node
        TwtEdge edge1 = new TwtEdge(edge.getComponentType(), edge.getNode1(), nodeFict);
        edge1.setSnakeLine(splitPolyline3(pol1, pol2, 1, coordNodeFict));
        edges.add(edge1);

        // Creation of a new edge between the new fictitious node and node2
        TwtEdge edge2 = new TwtEdge(edge.getComponentType(), nodeFict, edge.getNode2());
        edge2.setSnakeLine(splitPolyline3(pol1, pol2, 2, null));
        edges.add(edge2);

        // Creation of a new edge between the new fictitious node and node3
        TwtEdge edge3 = new TwtEdge(edge.getComponentType(), nodeFict, edge.getNode3());
        edge3.setSnakeLine(splitPolyline3(pol1, pol2, 3, null));
        edges.add(edge3);

        // Setting the coordinates of the new fictitious node
        nodeFict.setX(coordNodeFict.getX(), false, false);
        nodeFict.setY(coordNodeFict.getY(), false, false);
        nodeFict.addAdjacentEdge(edge1);
        nodeFict.addAdjacentEdge(edge2);
        nodeFict.addAdjacentEdge(edge3);

        // the new fictitious node is store in the substation graph
        graph.addMultiTermNode(nodeFict);
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
            coord.setX(xSplit);
            coord.setY(ySplit);
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
                coord.setX(pol1.get(pol1.size() - 4));
                coord.setY(pol1.get(pol1.size() - 3));
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

    @Override
    public Map<Graph, VoltageLevelLayout> getVlLayouts() {
        return vlLayouts;
    }

    protected Integer addAndGetNbSnakeLinesTopBottom(BusCell.Direction direction, Integer increment) {
        Integer value = 0;
        boolean first = true;
        for (Map.Entry<String, Map<BusCell.Direction, Integer>> entry : nbSnakeLinesTopBottom.entrySet()) {
            Integer val = nbSnakeLinesTopBottom.get(entry.getKey()).compute(direction, (k, v) -> v + increment);
            if (first) {
                value = val;
                first = false;
            }
        }
        return value;
    }

    @Override
    public Integer addAndGetNbSnakeLinesTopBottom(String vId, BusCell.Direction direction, Integer increment) {
        return nbSnakeLinesTopBottom.get(vId).compute(direction, (k, v) -> v + increment);
    }

    @Override
    public Integer addAndGetNbSnakeLinesBetween(String key, Integer increment) {
        return nbSnakeLinesBetween.compute(key, (k, v) -> v + increment);
    }

    @Override
    public Integer addAndGetNbSnakeLinesLeftRight(Side side, Integer increment) {
        return nbSnakeLinesLeftRight.compute(side, (k, v) -> v + increment);
    }

    @Override
    public Integer addAndGetNbSnakeLinesBottomVL(String key, Integer increment) {
        return nbSnakeLinesBottomVL.compute(key, (k, v) -> v + increment);
    }

    @Override
    public Integer addAndGetNbSnakeLinesTopVL(String key, Integer increment) {
        return nbSnakeLinesTopVL.compute(key, (k, v) -> v + increment);
    }
}
