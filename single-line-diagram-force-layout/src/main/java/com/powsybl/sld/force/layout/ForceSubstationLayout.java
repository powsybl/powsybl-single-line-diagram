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
import com.powsybl.sld.layout.VoltageLevelLayout;
import com.powsybl.sld.layout.VoltageLevelLayoutFactory;
import com.powsybl.sld.model.BusCell;
import com.powsybl.sld.model.Coord;
import com.powsybl.sld.model.ExternCell;
import com.powsybl.sld.model.FeederNode;
import com.powsybl.sld.model.Graph;
import com.powsybl.sld.model.SubstationGraph;
import com.powsybl.sld.model.TwtEdge;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class ForceSubstationLayout extends AbstractSubstationLayout {

    public ForceSubstationLayout(SubstationGraph substationGraph,
                                 VoltageLevelLayoutFactory voltageLevelLayoutFactory,
                                 CompactionType compactionType) {
        super(substationGraph, voltageLevelLayoutFactory, compactionType);
    }

    @Override
    public void run(LayoutParameters layoutParameters, boolean applyVLLayouts,
                    boolean manageSnakeLines, Map<Graph, VoltageLevelLayout> mapVLayouts) {
        if (applyVLLayouts) {
            Map<Graph, Coord> coordsVoltageLevels = new HashMap<>();

            ForceLayoutUtils.doSubstationForceLayout(graph, coordsVoltageLevels);

            if (mapVLayouts == null) {
                vlLayouts.clear();
            }
            // Creating and applying the voltage levels layout with these coordinates
            Map<Graph, VoltageLevelLayout> graphsLayouts = new HashMap<>();
            coordsVoltageLevels.entrySet().stream().forEach(e -> {
                VoltageLevelLayout vlLayout = mapVLayouts == null
                        ? vLayoutFactory.create(e.getKey())
                        : mapVLayouts.get(e.getKey());
                graphsLayouts.put(e.getKey(), vlLayout);
                vlLayout.run(layoutParameters);
                vlLayouts.put(e.getKey(), vlLayout);
            });

            // Changing the snakeline feeder cells direction using the coordinates calculated by the ForceAtlas algorithm
            changingCellsOrientation(graph, coordsVoltageLevels);

            // List of voltage levels sorted by ascending x value
            List<Graph> graphsX = coordsVoltageLevels.entrySet().stream()
                    .sorted((e1, e2) -> Double.compare(e1.getValue().getX(), e2.getValue().getX()))
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());

            // List of voltage levels sorted by ascending y value
            List<Graph> graphsY = coordsVoltageLevels.entrySet().stream()
                    .sorted((e1, e2) -> Double.compare(e1.getValue().getY(), e2.getValue().getY()))
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());

            // Narrowing / Spreading the voltage levels in the horizontal direction
            // (if no compaction, one voltage level only in a column
            //  if horizontal compaction, a voltage level is positioned horizontally at the middle of the preceding voltage level)
            double factHorizontalCompaction = compactionType == CompactionType.HORIZONTAL ? 2 : 1;
            double graphX = graph.getX() + getHorizontalSubstationPadding(layoutParameters);
            for (Graph g : graphsX) {
                g.setX(graphX);
                int maxH = g.getMaxH();
                graphX += layoutParameters.getInitialXBus() + (maxH + 2) * layoutParameters.getCellWidth()
                        + getHorizontalSubstationPadding(layoutParameters);
                graphX /= factHorizontalCompaction;
            }

            // Narrowing / Spreading the voltage levels in the vertical direction
            // (if no compaction, one voltage level only in a line
            //  if vertical compaction, a voltage level is positioned vertically at the middle of the preceding voltage level)
            double factVerticalCompaction = compactionType == CompactionType.VERTICAL ? 2 : 1;
            double graphY = graph.getY() + getVerticalSubstationPadding(layoutParameters);
            for (Graph g : graphsY) {
                g.setY(graphY);
                int maxV = g.getMaxV();
                graphY += layoutParameters.getInitialYBus() + layoutParameters.getStackHeight()
                        + layoutParameters.getExternCellHeight()
                        + layoutParameters.getVerticalSpaceBus() * (maxV + 2)
                        + getVerticalSubstationPadding(layoutParameters);
                graphY /= factVerticalCompaction;
            }

            // Finally, running the voltage levels layout a second time with the new adapted voltage levels coordinates
            // (here, we keep the cells and blocks already detected before, and we only recompute the nodes coordinates)
            coordsVoltageLevels.keySet().stream().forEach(g -> {
                g.resetCoords();
                graphsLayouts.get(g).run(layoutParameters);
            });
        }

        if (manageSnakeLines) {
            // Calculate all the coordinates for the links between the voltageLevel graphs
            // (new fictitious nodes and edges are created here, for the two and three windings transformers)
            manageSnakeLines(layoutParameters);
        }
    }

    @Override
    public List<Double> calculatePolylinePoints(InfoCalcPoints info) {
        List<Double> pol = new ArrayList<>();

        switch (info.getdNode1()) {
            case BOTTOM:
                if (info.getdNode2() == BusCell.Direction.BOTTOM) {  // BOTTOM to BOTTOM
                    double decalV1 = addAndGetNbSnakeLinesTopBottom(info.getGraphId1(), info.getdNode1(), 1) * info.getLayoutParam().getVerticalSnakeLinePadding();
                    double decalV2 = addAndGetNbSnakeLinesTopBottom(info.getGraphId2(), info.getdNode2(), 1) * info.getLayoutParam().getVerticalSnakeLinePadding();
                    double yDecal = Math.max(info.getInitY1() + decalV1, info.getInitY2() + decalV2);

                    pol.addAll(Arrays.asList(info.getX1(), info.getY1(),
                            info.getX1(), yDecal,
                            info.getX2(), yDecal,
                            info.getX2(), info.getY2()));
                } else {  // BOTTOM to TOP
                    if (info.getY1() < info.getY2()) {
                        double decalV1 = addAndGetNbSnakeLinesTopBottom(info.getGraphId1(), info.getdNode1(), 1) * info.getLayoutParam().getVerticalSnakeLinePadding();
                        double decalV2 = addAndGetNbSnakeLinesTopBottom(info.getGraphId2(), info.getdNode2(), 1) * info.getLayoutParam().getVerticalSnakeLinePadding();
                        double yDecal = Math.max(info.getInitY1() + decalV1, info.getInitY2() - decalV2);

                        pol.addAll(Arrays.asList(info.getX1(), info.getY1(),
                                info.getX1(), yDecal,
                                info.getX2(), yDecal,
                                info.getX2(), info.getY2()));
                    } else {
                        double decalV1 = addAndGetNbSnakeLinesTopBottom(info.getGraphId1(), info.getdNode1(), 1) * info.getLayoutParam().getVerticalSnakeLinePadding();
                        double decalV2 = addAndGetNbSnakeLinesTopBottom(info.getGraphId2(), info.getdNode2(), 1) * info.getLayoutParam().getVerticalSnakeLinePadding();
                        double xBetweenGraph = info.getxMaxGraph() - (addAndGetNbSnakeLinesBetween(info.getIdMaxGraph(), 1) * info.getLayoutParam().getHorizontalSnakeLinePadding());

                        pol.addAll(Arrays.asList(info.getX1(), info.getY1(),
                                info.getX1(), info.getInitY1() + decalV1,
                                xBetweenGraph, info.getInitY1() + decalV1,
                                xBetweenGraph, info.getInitY2() - decalV2,
                                info.getX2(), info.getInitY2() - decalV2,
                                info.getX2(), info.getY2()));
                    }
                }
                break;

            case TOP:
                if (info.getdNode2() == BusCell.Direction.TOP) {  // TOP to TOP
                    double decalV1 = addAndGetNbSnakeLinesTopBottom(info.getGraphId1(), info.getdNode1(), 1) * info.getLayoutParam().getVerticalSnakeLinePadding();
                    double decalV2 = addAndGetNbSnakeLinesTopBottom(info.getGraphId2(), info.getdNode2(), 1) * info.getLayoutParam().getVerticalSnakeLinePadding();

                    double yDecal = Math.min(info.getInitY1() - decalV1, info.getInitY2() - decalV2);

                    pol.addAll(Arrays.asList(info.getX1(), info.getY1(),
                            info.getX1(), yDecal,
                            info.getX2(), yDecal,
                            info.getX2(), info.getY2()));
                } else {  // TOP to BOTTOM
                    if (info.getY1() > info.getY2()) {
                        double decalV1 = addAndGetNbSnakeLinesTopBottom(info.getGraphId1(), info.getdNode1(), 1) * info.getLayoutParam().getVerticalSnakeLinePadding();
                        double decalV2 = addAndGetNbSnakeLinesTopBottom(info.getGraphId2(), info.getdNode2(), 1) * info.getLayoutParam().getVerticalSnakeLinePadding();

                        double yDecal = Math.min(info.getInitY1() - decalV1, info.getInitY2() + decalV2);

                        pol.addAll(Arrays.asList(info.getX1(), info.getY1(),
                                info.getX1(), yDecal,
                                info.getX2(), yDecal,
                                info.getX2(), info.getY2()));
                    } else {
                        double decalV1 = addAndGetNbSnakeLinesTopBottom(info.getGraphId1(), info.getdNode1(), 1) * info.getLayoutParam().getVerticalSnakeLinePadding();
                        double decalV2 = addAndGetNbSnakeLinesTopBottom(info.getGraphId2(), info.getdNode2(), 1) * info.getLayoutParam().getVerticalSnakeLinePadding();

                        double xBetweenGraph = info.getxMaxGraph() - (addAndGetNbSnakeLinesBetween(info.getIdMaxGraph(), 1) * info.getLayoutParam().getHorizontalSnakeLinePadding());

                        pol.addAll(Arrays.asList(info.getX1(), info.getY1(),
                                info.getX1(), info.getInitY1() - decalV1,
                                xBetweenGraph, info.getInitY1() - decalV1,
                                xBetweenGraph, info.getInitY2() + decalV2,
                                info.getX2(), info.getInitY2() + decalV2,
                                info.getX2(), info.getY2()));
                    }
                }
                break;
            default:
        }
        return pol;
    }

    @Override
    protected List<Double> splitPolyline3(List<Double> pol1, List<Double> pol2, int numPart, Coord coord) {
        List<Double> res = new ArrayList<>();

        if (numPart == 1 || numPart == 2) {
            res = super.splitPolyline3(pol1, pol2, numPart, coord);
        } else {
            // the third new edge now begins with the fictitious node point
            res.add(pol1.get(pol1.size() - 4));
            res.add(pol1.get(pol1.size() - 3));
            // then we add an intermediate point with the absciss of the third point in the original second polyline
            // and the ordinate of the fictitious node
            res.add(pol2.get(4));
            res.add(pol1.get(pol1.size() - 3));
            // then we had the last three or two points of the original second polyline
            if (pol2.size() > 8) {
                res.addAll(pol2.subList(pol2.size() - 6, pol2.size()));
            } else {
                res.addAll(pol2.subList(pol2.size() - 2, pol2.size()));
            }
        }

        return res;
    }

    private void changingCellsOrientation(SubstationGraph graph, Map<Graph, Coord> coordsVoltageLevels) {
        for (TwtEdge edge : graph.getEdges()) {
            FeederNode n1 = (FeederNode) edge.getNode1();
            ExternCell cell1 = (ExternCell) n1.getCell();
            FeederNode n2 = (FeederNode) edge.getNode2();
            ExternCell cell2 = (ExternCell) n2.getCell();

            Coord c1 = coordsVoltageLevels.get(n1.getGraph());
            Coord c2 = coordsVoltageLevels.get(n2.getGraph());

            if (c1.getY() < c2.getY()) {
                // cell for node 1 with bottom orientation
                // cell for node 2 with top orientation
                cell1.setDirection(BusCell.Direction.BOTTOM);
                n1.setDirection(BusCell.Direction.BOTTOM);
                cell2.setDirection(BusCell.Direction.TOP);
                n2.setDirection(BusCell.Direction.TOP);
            } else {
                // cell for node 1 with top orientation
                // cell for node 2 with bottom orientation
                cell1.setDirection(BusCell.Direction.TOP);
                n1.setDirection(BusCell.Direction.TOP);
                cell2.setDirection(BusCell.Direction.BOTTOM);
                n2.setDirection(BusCell.Direction.BOTTOM);
            }

            if (edge.getNode3() != null) {
                FeederNode n3 = (FeederNode) edge.getNode3();
                ExternCell cell3 = (ExternCell) n3.getCell();
                Coord c3 = coordsVoltageLevels.get(n3.getGraph());

                if (c3.getY() < c2.getY()) {
                    // cell for node 3 with bottom orientation
                    cell3.setDirection(BusCell.Direction.BOTTOM);
                    n3.setDirection(BusCell.Direction.BOTTOM);
                } else {
                    // cell for node 3 with top orientation
                    cell3.setDirection(BusCell.Direction.TOP);
                    n3.setDirection(BusCell.Direction.TOP);
                }
            }
        }
    }
}
