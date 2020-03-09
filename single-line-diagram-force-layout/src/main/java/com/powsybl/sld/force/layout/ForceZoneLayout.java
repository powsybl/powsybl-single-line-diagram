/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.force.layout;

import com.powsybl.sld.layout.AbstractSubstationLayout;
import com.powsybl.sld.layout.CompactionType;
import com.powsybl.sld.layout.InfoCalcPoints;
import com.powsybl.sld.layout.LayoutParameters;
import com.powsybl.sld.layout.SubstationLayout;
import com.powsybl.sld.layout.SubstationLayoutFactory;
import com.powsybl.sld.layout.VoltageLevelLayout;
import com.powsybl.sld.layout.VoltageLevelLayoutFactory;
import com.powsybl.sld.layout.ZoneLayout;
import com.powsybl.sld.model.BusCell;
import com.powsybl.sld.model.Coord;
import com.powsybl.sld.model.Graph;
import com.powsybl.sld.model.LineEdge;
import com.powsybl.sld.model.Node;
import com.powsybl.sld.model.SubstationGraph;
import com.powsybl.sld.model.ZoneGraph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class ForceZoneLayout extends AbstractSubstationLayout implements ZoneLayout {

    protected ZoneGraph zoneGraph;

    protected SubstationLayoutFactory sLayoutFactory;

    public ForceZoneLayout(ZoneGraph graph, SubstationLayoutFactory sLayoutFactory,
                           VoltageLevelLayoutFactory vLayoutFactory,
                           CompactionType compactionType) {
        super(null, vLayoutFactory, compactionType);
        this.zoneGraph = graph;
        this.sLayoutFactory = sLayoutFactory;
    }

    @Override
    public void run(LayoutParameters layoutParameters) {
        Map<SubstationGraph, Coord> coordsSubstations = new HashMap<>();

        ForceLayoutUtils.doZoneForceLayout(zoneGraph, coordsSubstations);

        // Creating and applying the substations layout with these coordinates
        Map<String, SubstationLayout> graphsLayouts = new HashMap<>();
        coordsSubstations.entrySet().forEach(e -> {
            SubstationLayout sLayout = sLayoutFactory.create(e.getKey(), vLayoutFactory);
            graphsLayouts.put(e.getKey().getSubstationId(), sLayout);
            sLayout.run(layoutParameters, true, false, null);  // we do not yet manage snakeLines of substations
        });

        // List of substations sorted by ascending x value
        List<SubstationGraph> graphsX = coordsSubstations.entrySet().stream()
                .sorted((e1, e2) -> Double.compare(e1.getValue().getX(), e2.getValue().getX()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        // List of substations sorted by ascending y value
        List<SubstationGraph> graphsY = coordsSubstations.entrySet().stream()
                .sorted((e1, e2) -> Double.compare(e1.getValue().getY(), e2.getValue().getY()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        // Narrowing / Spreading the substations in the horizontal direction
        // (if no compaction, one substation only in a column
        //  if horizontal compaction, a substation is positioned horizontally at the middle of the preceding one)
        double graphX = getHorizontalSubstationPadding(layoutParameters);
        for (SubstationGraph s : graphsX) {
            s.setX(graphX);
            for (Graph g : s.getNodes()) {
                graphX += layoutParameters.getInitialXBus() + (g.getMaxH() + 2) * layoutParameters.getCellWidth()
                        + getHorizontalSubstationPadding(layoutParameters);
            }

            if (compactionType == CompactionType.HORIZONTAL) {
                graphX /= 2;
            }
        }

        // Narrowing / Spreading the substations in the vertical direction
        // (if no compaction, one substation only in a line
        //  if vertical compaction, a substation is positioned vertically at the middle of the preceding one)
        double graphY = getVerticalSubstationPadding(layoutParameters);
        for (SubstationGraph s : graphsY) {
            s.setY(graphY);
            for (Graph g : s.getNodes()) {
                graphY += layoutParameters.getInitialYBus() + layoutParameters.getStackHeight()
                        + layoutParameters.getExternCellHeight()
                        + layoutParameters.getVerticalSpaceBus() * (g.getMaxV() + 2)
                        + getVerticalSubstationPadding(layoutParameters);
            }

            if (compactionType == CompactionType.VERTICAL) {
                graphY /= 2;
            }
        }

        // Re-applying the substations layout, but with the voltage levels layout already created before
        coordsSubstations.keySet().stream().forEach(s -> {
            SubstationLayout sLayout = graphsLayouts.get(s.getSubstationId());
            Map<Graph, VoltageLevelLayout> mapVLayouts = sLayout.getVlLayouts();

            // For each voltage level graph in the substation, we clear all the nodes coordinates
            s.getNodes().forEach(Graph::resetCoords);

            // Then, we run the substations layout a second time with the new adapted substations coordinates
            // (here, we keep the cells and blocks already detected before in the voltage levels graph,
            // and we only recompute the nodes coordinates)
            sLayout.run(layoutParameters, true, true, mapVLayouts);
        });

        // Calculate all the coordinates for the lines between the substation graphs
        manageSnakeLines(layoutParameters, graphsLayouts);
    }

    protected void manageSnakeLines(LayoutParameters layoutParameters,
                                    Map<String, SubstationLayout> graphsLayouts) {
        for (LineEdge edge : zoneGraph.getEdges()) {
            LineEdge edgeSorted = new LineEdge(edge);
            // Sorting the nodes in the edge by ascending x value, for the coordinates computation
            edgeSorted.getNodes().sort(Comparator.comparingDouble(Node::getX));

            List<LineEdge.Point> pol = calculatePolylineSnakeLine(layoutParameters, edgeSorted.getNode1(), edgeSorted.getNode2(), graphsLayouts);
            edge.setPoints(pol);
        }
    }

    public List<Double> calculatePolylinePoints(InfoCalcPoints info) {
        return Collections.emptyList();
    }

    protected List<LineEdge.Point> calculatePolylineSnakeLine(LayoutParameters layoutParameters, Node node1, Node node2, Map<String, SubstationLayout> graphsLayouts) {
        InfoCalcPoints info = new InfoCalcPoints(layoutParameters, node1, node2, true);
        info.setSubstationId1(node1.getGraph().getSubstationId());
        info.setSubstationId2(node2.getGraph().getSubstationId());
        info.setIdMaxSubstation(node1.getGraph().getX() > node2.getGraph().getX() ? node1.getGraph().getSubstationId() : node2.getGraph().getSubstationId());

        return calculatePolylinePoints(info, graphsLayouts);
    }

    public static List<LineEdge.Point> calculatePolylinePoints(InfoCalcPoints info, Map<String, SubstationLayout> graphsLayouts) {
        List<LineEdge.Point> pol = new ArrayList<>();

        SubstationLayout sLayout1 = graphsLayouts.get(info.getSubstationId1());
        SubstationLayout sLayout2 = graphsLayouts.get(info.getSubstationId2());
        SubstationLayout sLayoutMax = graphsLayouts.get(info.getIdMaxSubstation());

        switch (info.getdNode1()) {
            case BOTTOM:
                if (info.getdNode2() == BusCell.Direction.BOTTOM) {  // BOTTOM to BOTTOM
                    double decalV1 = sLayout1.addAndGetNbSnakeLinesTopBottom(info.getGraphId1(), info.getdNode1(), 1) * info.getLayoutParam().getVerticalSnakeLinePadding();
                    double decalV2 = sLayout2.addAndGetNbSnakeLinesTopBottom(info.getGraphId2(), info.getdNode2(), 1) * info.getLayoutParam().getVerticalSnakeLinePadding();

                    double yDecal = Math.max(info.getInitY1() + decalV1, info.getInitY2() + decalV2);

                    pol.addAll(Arrays.asList(new LineEdge.Point(info.getX1(), info.getY1()),
                            new LineEdge.Point(info.getX1(), yDecal),
                            new LineEdge.Point(info.getX2(), yDecal),
                            new LineEdge.Point(info.getX2(), info.getY2())));
                } else {  // BOTTOM to TOP
                    if (info.getY1() < info.getY2()) {
                        double decalV1 = sLayout1.addAndGetNbSnakeLinesTopBottom(info.getGraphId1(), info.getdNode1(), 1)  * info.getLayoutParam().getVerticalSnakeLinePadding();
                        double decalV2 = sLayout2.addAndGetNbSnakeLinesTopBottom(info.getGraphId2(), info.getdNode2(), 1) * info.getLayoutParam().getVerticalSnakeLinePadding();

                        double yDecal = Math.max(info.getInitY1() + decalV1, info.getInitY2() - decalV2);

                        pol.addAll(Arrays.asList(new LineEdge.Point(info.getX1(), info.getY1()),
                                new LineEdge.Point(info.getX1(), yDecal),
                                new LineEdge.Point(info.getX2(), yDecal),
                                new LineEdge.Point(info.getX2(), info.getY2())));
                    } else {
                        double decalV1 = sLayout1.addAndGetNbSnakeLinesTopBottom(info.getGraphId1(), info.getdNode1(), 1) * info.getLayoutParam().getVerticalSnakeLinePadding();
                        double decalV2 = sLayout2.addAndGetNbSnakeLinesTopBottom(info.getGraphId2(), info.getdNode2(), 1) * info.getLayoutParam().getVerticalSnakeLinePadding();
                        double xBetweenGraph = info.getxMaxGraph() - (sLayoutMax.addAndGetNbSnakeLinesBetween(info.getIdMaxGraph(), 1) * info.getLayoutParam().getHorizontalSnakeLinePadding());

                        pol.addAll(Arrays.asList(new LineEdge.Point(info.getX1(), info.getY1()),
                                new LineEdge.Point(info.getX1(), info.getInitY1() + decalV1),
                                new LineEdge.Point(xBetweenGraph, info.getInitY1() + decalV1),
                                new LineEdge.Point(xBetweenGraph, info.getInitY2() - decalV2),
                                new LineEdge.Point(info.getX2(), info.getInitY2() - decalV2),
                                new LineEdge.Point(info.getX2(), info.getY2())));
                    }
                }
                break;

            case TOP:
                if (info.getdNode2() == BusCell.Direction.TOP) {  // TOP to TOP
                    double decalV1 = sLayout1.addAndGetNbSnakeLinesTopBottom(info.getGraphId1(), info.getdNode1(), 1) * info.getLayoutParam().getVerticalSnakeLinePadding();
                    double decalV2 = sLayout2.addAndGetNbSnakeLinesTopBottom(info.getGraphId2(), info.getdNode2(), 1) * info.getLayoutParam().getVerticalSnakeLinePadding();

                    double yDecal = Math.min(info.getInitY1() - decalV1, info.getInitY2() - decalV2);

                    pol.addAll(Arrays.asList(new LineEdge.Point(info.getX1(), info.getY1()),
                            new LineEdge.Point(info.getX1(), yDecal),
                            new LineEdge.Point(info.getX2(), yDecal),
                            new LineEdge.Point(info.getX2(), info.getY2())));
                } else {  // TOP to BOTTOM
                    if (info.getY1() > info.getY2()) {
                        double decalV1 = sLayout1.addAndGetNbSnakeLinesTopBottom(info.getGraphId1(), info.getdNode1(), 1) * info.getLayoutParam().getVerticalSnakeLinePadding();
                        double decalV2 = sLayout2.addAndGetNbSnakeLinesTopBottom(info.getGraphId2(), info.getdNode2(), 1) * info.getLayoutParam().getVerticalSnakeLinePadding();

                        double yDecal = Math.min(info.getInitY1() - decalV1, info.getInitY2() + decalV2);

                        pol.addAll(Arrays.asList(new LineEdge.Point(info.getX1(), info.getY1()),
                                new LineEdge.Point(info.getX1(), yDecal),
                                new LineEdge.Point(info.getX2(), yDecal),
                                new LineEdge.Point(info.getX2(), info.getY2())));
                    } else {
                        double decalV1 = sLayout1.addAndGetNbSnakeLinesTopBottom(info.getGraphId1(), info.getdNode1(), 1) * info.getLayoutParam().getVerticalSnakeLinePadding();
                        double decalV2 = sLayout2.addAndGetNbSnakeLinesTopBottom(info.getGraphId2(), info.getdNode2(), 1) * info.getLayoutParam().getVerticalSnakeLinePadding();

                        double xBetweenGraph = info.getxMaxGraph() - (sLayoutMax.addAndGetNbSnakeLinesBetween(info.getIdMaxGraph(), 1) * info.getLayoutParam().getHorizontalSnakeLinePadding());

                        pol.addAll(Arrays.asList(new LineEdge.Point(info.getX1(), info.getY1()),
                                new LineEdge.Point(info.getX1(), info.getInitY1() - decalV1),
                                new LineEdge.Point(xBetweenGraph, info.getInitY1() - decalV1),
                                new LineEdge.Point(xBetweenGraph, info.getInitY2() + decalV2),
                                new LineEdge.Point(info.getX2(), info.getInitY2() + decalV2),
                                new LineEdge.Point(info.getX2(), info.getY2())));
                    }
                }
                break;
            default:
        }

        return pol;
    }
}

