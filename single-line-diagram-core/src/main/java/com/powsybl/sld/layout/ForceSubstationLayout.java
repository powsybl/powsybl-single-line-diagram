/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout;

import com.powsybl.sld.force.layout.ForceLayout;
import com.powsybl.sld.force.layout.Vector;
import com.powsybl.sld.model.*;
import org.jgrapht.Graph;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.powsybl.sld.model.Coord.Dimension.X;
import static com.powsybl.sld.model.Coord.Dimension.Y;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class ForceSubstationLayout extends AbstractSubstationLayout {
    private ForceSubstationLayoutFactory.CompactionType compactionType;

    public static class ForceInfoCalcPoints extends InfoCalcPoints {
        private String vId1;
        private String vId2;
        private Map<String, Map<BusCell.Direction, Integer>> nbSnakeLinesTopBottom;
        private Map<String, Integer> nbSnakeLinesBetween;

        public String getVId1() {
            return vId1;
        }

        public void setVId1(String vId1) {
            this.vId1 = vId1;
        }

        public String getVId2() {
            return vId2;
        }

        public void setVId2(String vId2) {
            this.vId2 = vId2;
        }

        public Map<String, Map<BusCell.Direction, Integer>> getNbSnakeLinesTopBottom() {
            return nbSnakeLinesTopBottom;
        }

        public void setNbSnakeLinesTopBottom(Map<String, Map<BusCell.Direction, Integer>> nbSnakeLinesTopBottom) {
            this.nbSnakeLinesTopBottom = nbSnakeLinesTopBottom;
        }

        public Map<String, Integer> getNbSnakeLinesBetween() {
            return nbSnakeLinesBetween;
        }

        public void setNbSnakeLinesBetween(Map<String, Integer> nbSnakeLinesBetween) {
            this.nbSnakeLinesBetween = nbSnakeLinesBetween;
        }
    }

    public ForceSubstationLayout(SubstationGraph substationGraph,
                                 VoltageLevelLayoutFactory voltageLevelLayoutFactory,
                                 ForceSubstationLayoutFactory.CompactionType compactionType) {
        super(substationGraph, voltageLevelLayoutFactory);
        this.compactionType = compactionType;
    }

    @Override
    public void run(LayoutParameters layoutParameters) {
        // Creating the graph for the force layout algorithm
        Graph<VoltageLevelGraph, Object> graph = getGraph().toJgrapht();

        // Executing force layout algorithm
        ForceLayout<VoltageLevelGraph, Object> forceLayout = new ForceLayout<>(graph);
        forceLayout.execute();

        // Memorizing the voltage levels coordinates calculated by the force layout algorithm
        Map<VoltageLevelGraph, Coord> coordsVoltageLevels = new HashMap<>();
        for (VoltageLevelGraph voltageLevelGraph : getGraph().getNodes()) {
            Vector position = forceLayout.getStablePosition(voltageLevelGraph);
            coordsVoltageLevels.put(voltageLevelGraph, new Coord(position.getX(), position.getY()));
        }

        // Creating and applying the voltage levels layout with these coordinates
        Map<VoltageLevelGraph, VoltageLevelLayout> graphsLayouts = new HashMap<>();
        coordsVoltageLevels.entrySet().stream().forEach(e -> {
            VoltageLevelLayout vlLayout = vLayoutFactory.create(e.getKey());
            graphsLayouts.put(e.getKey(), vlLayout);
            vlLayout.run(layoutParameters);
        });

        // Changing the snakeline feeder cells direction using the coordinates calculated by the ForceAtlas algorithm
        changingCellsOrientation(getGraph(), coordsVoltageLevels);

        // List of voltage levels sorted by ascending x value
        List<VoltageLevelGraph> graphsX = coordsVoltageLevels.entrySet().stream()
                .sorted(Comparator.comparingDouble(e -> e.getValue().get(X)))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        // List of voltage levels sorted by ascending y value
        List<VoltageLevelGraph> graphsY = coordsVoltageLevels.entrySet().stream()
                .sorted(Comparator.comparingDouble(e -> e.getValue().get(Y)))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        // Narrowing / Spreading the voltage levels in the horizontal direction
        // (if no compaction, one voltage level only in a column
        //  if horizontal compaction, a voltage level is positioned horizontally at the middle of the preceding voltage level)
        double graphX = getHorizontalSubstationPadding(layoutParameters);
        for (VoltageLevelGraph g : graphsX) {
            g.setX(graphX);
            int maxH = g.getMaxH();
            graphX += layoutParameters.getInitialXBus() + (maxH + 2) * layoutParameters.getCellWidth()
                    + getHorizontalSubstationPadding(layoutParameters);
            if (compactionType == ForceSubstationLayoutFactory.CompactionType.HORIZONTAL) {
                graphX /= 2;
            }
        }

        // Narrowing / Spreading the voltage levels in the vertical direction
        // (if no compaction, one voltage level only in a line
        //  if vertical compaction, a voltage level is positioned vertically at the middle of the preceding voltage level)
        double graphY = getVerticalSubstationPadding(layoutParameters);
        for (VoltageLevelGraph g : graphsY) {
            g.setY(graphY);
            int maxV = g.getMaxV();
            graphY += layoutParameters.getInitialYBus() + layoutParameters.getStackHeight()
                    + layoutParameters.getExternCellHeight()
                    + layoutParameters.getVerticalSpaceBus() * (maxV + 2)
                    + getVerticalSubstationPadding(layoutParameters);
            if (compactionType == ForceSubstationLayoutFactory.CompactionType.VERTICAL) {
                graphY /= 2;
            }
        }

        // Finally, running the voltage levels layout a second time with the new adapted voltage levels coordinates
        // (here, we keep the cells and blocks already detected before, and we only recompute the nodes coordinates)
        coordsVoltageLevels.keySet().stream().forEach(g -> {
            g.resetCoords();
            graphsLayouts.get(g).run(layoutParameters);
        });

        manageSnakeLines(layoutParameters);
    }

    @Override
    public void manageSnakeLines(LayoutParameters layoutParameters) {
        Map<String, Map<BusCell.Direction, Integer>> nbSnakeLinesTopBottom = new HashMap<>();
        getGraph().getNodes().forEach(g -> nbSnakeLinesTopBottom.put(g.getVoltageLevelInfos().getId(), EnumSet.allOf(BusCell.Direction.class).stream().collect(Collectors.toMap(Function.identity(), v -> 0))));
        Map<String, Integer> nbSnakeLinesBetween = getGraph().getNodes().stream().collect(Collectors.toMap(g -> g.getVoltageLevelInfos().getId(), v -> 0));

        getGraph().getNodes().forEach(g -> manageSnakeLines(g, layoutParameters, nbSnakeLinesTopBottom, nbSnakeLinesBetween));
        manageSnakeLines(getGraph(), layoutParameters, nbSnakeLinesTopBottom, nbSnakeLinesBetween);
    }

    private void manageSnakeLines(AbstractBaseGraph graph, LayoutParameters layoutParameters,
                                  Map<String, Map<BusCell.Direction, Integer>> nbSnakeLinesTopBottom,
                                  Map<String, Integer> nbSnakeLinesBetween) {
        for (Node multiNode : graph.getMultiTermNodes()) {
            List<Edge> adjacentEdges = multiNode.getAdjacentEdges();
            List<Node> adjacentNodes = multiNode.getAdjacentNodes();
            if (adjacentNodes.size() == 2) {
                List<Double> pol = calculatePolylineSnakeLine(layoutParameters, adjacentNodes.get(0), adjacentNodes.get(1), nbSnakeLinesTopBottom, nbSnakeLinesBetween);
                Coord coordNodeFict = new Coord(-1, -1);
                ((TwtEdge) adjacentEdges.get(0)).setSnakeLine(splitPolyline2(pol, 1, coordNodeFict));
                ((TwtEdge) adjacentEdges.get(1)).setSnakeLine(splitPolyline2(pol, 2, null));
                multiNode.setX(coordNodeFict.get(X), false);
                multiNode.setY(coordNodeFict.get(Y), false);
            } else if (adjacentNodes.size() == 3) {
                List<Double> pol1 = calculatePolylineSnakeLine(layoutParameters, adjacentNodes.get(0), adjacentNodes.get(1), nbSnakeLinesTopBottom, nbSnakeLinesBetween);
                List<Double> pol2 = calculatePolylineSnakeLine(layoutParameters, adjacentNodes.get(1), adjacentNodes.get(2), nbSnakeLinesTopBottom, nbSnakeLinesBetween);
                Coord coordNodeFict = new Coord(-1, -1);
                ((TwtEdge) adjacentEdges.get(0)).setSnakeLine(splitPolyline3(pol1, pol2, 1, coordNodeFict));
                ((TwtEdge) adjacentEdges.get(1)).setSnakeLine(splitPolyline3(pol1, pol2, 2, null));
                ((TwtEdge) adjacentEdges.get(2)).setSnakeLine(splitPolyline3(pol1, pol2, 3, null));
                multiNode.setX(coordNodeFict.get(X), false);
                multiNode.setY(coordNodeFict.get(Y), false);
            }
        }

        for (LineEdge lineEdge : graph.getLineEdges()) {
            List<Node> adjacentNodes = lineEdge.getNodes();
            lineEdge.setSnakeLine(calculatePolylineSnakeLine(layoutParameters, adjacentNodes.get(0), adjacentNodes.get(1), nbSnakeLinesTopBottom, nbSnakeLinesBetween));
        }

    }

    private List<Double> calculatePolylineSnakeLine(LayoutParameters layoutParameters,
                                                    Node node1, Node node2,
                                                    Map<String, Map<BusCell.Direction, Integer>> nbSnakeLinesTopBottom,
                                                    Map<String, Integer> nbSnakeLinesBetween) {
        ForceInfoCalcPoints info = new ForceInfoCalcPoints();
        info.setLayoutParam(layoutParameters);
        info.setVId1(node1.getGraph().getVoltageLevelInfos().getId());
        info.setVId2(node2.getGraph().getVoltageLevelInfos().getId());
        info.setdNode1(getNodeDirection(node1, 1));
        info.setdNode2(getNodeDirection(node2, 2));
        info.setNbSnakeLinesTopBottom(nbSnakeLinesTopBottom);
        info.setNbSnakeLinesBetween(nbSnakeLinesBetween);
        info.setX1(node1.getX());
        info.setX2(node2.getX());
        info.setY1(node1.getY());
        info.setInitY1(node1.getInitY() != -1 ? node1.getInitY() : node1.getY());
        info.setY2(node2.getY());
        info.setInitY2(node2.getInitY() != -1 ? node2.getInitY() : node2.getY());
        info.setxMaxGraph(Math.max(node1.getGraph().getX(), node2.getGraph().getX()));
        info.setIdMaxGraph(node1.getGraph().getX() > node2.getGraph().getX() ? node1.getGraph().getVoltageLevelInfos().getId() : node2.getGraph().getVoltageLevelInfos().getId());

        return calculatePolylinePoints(info);
    }

    public static List<Double> calculatePolylinePoints(ForceInfoCalcPoints info) {
        List<Double> pol = new ArrayList<>();

        LayoutParameters layoutParam = info.getLayoutParam();
        BusCell.Direction dNode1 = info.getdNode1();
        BusCell.Direction dNode2 = info.getdNode2();
        String vId1 = info.getVId1();
        String vId2 = info.getVId2();
        Map<String, Map<BusCell.Direction, Integer>> nbSnakeLinesTopBottom = info.getNbSnakeLinesTopBottom();
        Map<String, Integer> nbSnakeLinesBetween = info.getNbSnakeLinesBetween();
        double x1 = info.getX1();
        double x2 = info.getX2();
        double y1 = info.getY1();
        double initY1 = info.getInitY1();
        double y2 = info.getY2();
        double initY2 = info.getInitY2();
        double xMaxGraph = info.getxMaxGraph();
        String idMaxGraph = info.getIdMaxGraph();

        switch (dNode1) {
            case BOTTOM:
                if (dNode2 == BusCell.Direction.BOTTOM) {  // BOTTOM to BOTTOM
                    nbSnakeLinesTopBottom.get(vId1).compute(dNode1, (k, v) -> v + 1);
                    nbSnakeLinesTopBottom.get(vId2).compute(dNode2, (k, v) -> v + 1);
                    double decalV1 = nbSnakeLinesTopBottom.get(vId1).get(dNode1) * layoutParam.getVerticalSnakeLinePadding();
                    double decalV2 = nbSnakeLinesTopBottom.get(vId2).get(dNode2) * layoutParam.getVerticalSnakeLinePadding();

                    double yDecal = Math.max(initY1 + decalV1, initY2 + decalV2);

                    pol.addAll(Arrays.asList(x1, y1,
                            x1, yDecal,
                            x2, yDecal,
                            x2, y2));
                } else {  // BOTTOM to TOP
                    if (y1 < y2) {
                        nbSnakeLinesTopBottom.get(vId1).compute(dNode1, (k, v) -> v + 1);
                        nbSnakeLinesTopBottom.get(vId2).compute(dNode2, (k, v) -> v + 1);
                        double decalV1 = nbSnakeLinesTopBottom.get(vId1).get(dNode1) * layoutParam.getVerticalSnakeLinePadding();
                        double decalV2 = nbSnakeLinesTopBottom.get(vId2).get(dNode2) * layoutParam.getVerticalSnakeLinePadding();

                        double yDecal = Math.max(initY1 + decalV1, initY2 - decalV2);

                        pol.addAll(Arrays.asList(x1, y1,
                                x1, yDecal,
                                x2, yDecal,
                                x2, y2));
                    } else {
                        nbSnakeLinesTopBottom.get(vId1).compute(dNode1, (k, v) -> v + 1);
                        nbSnakeLinesTopBottom.get(vId2).compute(dNode2, (k, v) -> v + 1);
                        nbSnakeLinesBetween.compute(idMaxGraph, (k, v) -> v + 1);

                        double decalV1 = nbSnakeLinesTopBottom.get(vId1).get(dNode1) * layoutParam.getVerticalSnakeLinePadding();
                        double decalV2 = nbSnakeLinesTopBottom.get(vId2).get(dNode2) * layoutParam.getVerticalSnakeLinePadding();
                        double xBetweenGraph = xMaxGraph - (nbSnakeLinesBetween.get(idMaxGraph) * layoutParam.getHorizontalSnakeLinePadding());

                        pol.addAll(Arrays.asList(x1, y1,
                                x1, initY1 + decalV1,
                                xBetweenGraph, initY1 + decalV1,
                                xBetweenGraph, initY2 - decalV2,
                                x2, initY2 - decalV2,
                                x2, y2));
                    }
                }
                break;

            case TOP:
                if (dNode2 == BusCell.Direction.TOP) {  // TOP to TOP
                    nbSnakeLinesTopBottom.get(vId1).compute(dNode1, (k, v) -> v + 1);
                    nbSnakeLinesTopBottom.get(vId2).compute(dNode2, (k, v) -> v + 1);
                    double decalV1 = nbSnakeLinesTopBottom.get(vId1).get(dNode1) * layoutParam.getVerticalSnakeLinePadding();
                    double decalV2 = nbSnakeLinesTopBottom.get(vId2).get(dNode2) * layoutParam.getVerticalSnakeLinePadding();

                    double yDecal = Math.min(initY1 - decalV1, initY2 - decalV2);

                    pol.addAll(Arrays.asList(x1, y1,
                            x1, yDecal,
                            x2, yDecal,
                            x2, y2));
                } else {  // TOP to BOTTOM
                    if (y1 > y2) {
                        nbSnakeLinesTopBottom.get(vId1).compute(dNode1, (k, v) -> v + 1);
                        nbSnakeLinesTopBottom.get(vId2).compute(dNode2, (k, v) -> v + 1);
                        double decalV1 = nbSnakeLinesTopBottom.get(vId1).get(dNode1) * layoutParam.getVerticalSnakeLinePadding();
                        double decalV2 = nbSnakeLinesTopBottom.get(vId2).get(dNode2) * layoutParam.getVerticalSnakeLinePadding();

                        double yDecal = Math.min(initY1 - decalV1, initY2 + decalV2);

                        pol.addAll(Arrays.asList(x1, y1,
                                x1, yDecal,
                                x2, yDecal,
                                x2, y2));
                    } else {
                        nbSnakeLinesTopBottom.get(vId1).compute(dNode1, (k, v) -> v + 1);
                        nbSnakeLinesTopBottom.get(vId2).compute(dNode2, (k, v) -> v + 1);
                        nbSnakeLinesBetween.compute(idMaxGraph, (k, v) -> v + 1);
                        double decalV1 = nbSnakeLinesTopBottom.get(vId1).get(dNode1) * layoutParam.getVerticalSnakeLinePadding();
                        double decalV2 = nbSnakeLinesTopBottom.get(vId2).get(dNode2) * layoutParam.getVerticalSnakeLinePadding();

                        double xBetweenGraph = xMaxGraph - (nbSnakeLinesBetween.get(idMaxGraph) * layoutParam.getHorizontalSnakeLinePadding());

                        pol.addAll(Arrays.asList(x1, y1,
                                x1, initY1 - decalV1,
                                xBetweenGraph, initY1 - decalV1,
                                xBetweenGraph, initY2 + decalV2,
                                x2, initY2 + decalV2,
                                x2, y2));
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

    @Override
    protected List<Double> calculatePolylineSnakeLine(LayoutParameters layoutParameters, Node node1, Node node2,
                                                      InfosNbSnakeLines infosNbSnakeLines, boolean increment) {
        return Collections.emptyList();
    }

    @Override
    protected Coord calculateCoordVoltageLevel(LayoutParameters layoutParameters, VoltageLevelGraph vlGraph) {
        return null;
    }

    @Override
    protected double getHorizontalSubstationPadding(LayoutParameters layoutParameters) {
        return layoutParameters.getHorizontalSubstationPadding();
    }

    @Override
    protected double getVerticalSubstationPadding(LayoutParameters layoutParameters) {
        return layoutParameters.getVerticalSubstationPadding();
    }

    private void changingCellsOrientation(SubstationGraph graph, Map<VoltageLevelGraph, Coord> coordsVoltageLevels) {
        for (Node multiNode : graph.getMultiTermNodes()) {
            List<Node> adjacentNodes = multiNode.getAdjacentNodes();

            FeederNode n1 = (FeederNode) adjacentNodes.get(0);
            ExternCell cell1 = (ExternCell) n1.getCell();
            FeederNode n2 = (FeederNode) adjacentNodes.get(1);
            ExternCell cell2 = (ExternCell) n2.getCell();

            Coord c1 = coordsVoltageLevels.get(n1.getGraph());
            Coord c2 = coordsVoltageLevels.get(n2.getGraph());

            // no change if cells are part of a shunt : they must keep the same orientation
            if (!cell1.isShunted() && !cell2.isShunted()) {
                if (c1.get(Y) < c2.get(Y)) {
                    // cell for node 1 with bottom orientation
                    // cell for node 2 with top orientation
                    cell1.setDirection(BusCell.Direction.BOTTOM);
                    n1.setDirection(BusCell.Direction.BOTTOM);
                    cell1.getRootBlock().setOrientation(Orientation.DOWN);

                    cell2.setDirection(BusCell.Direction.TOP);
                    n2.setDirection(BusCell.Direction.TOP);
                    cell2.getRootBlock().setOrientation(Orientation.UP);
                } else {
                    // cell for node 1 with top orientation
                    // cell for node 2 with bottom orientation
                    cell1.setDirection(BusCell.Direction.TOP);
                    n1.setDirection(BusCell.Direction.TOP);
                    cell1.getRootBlock().setOrientation(Orientation.UP);

                    cell2.setDirection(BusCell.Direction.BOTTOM);
                    n2.setDirection(BusCell.Direction.BOTTOM);
                    cell2.getRootBlock().setOrientation(Orientation.DOWN);
                }
            }

            if (adjacentNodes.size() == 3) {
                FeederNode n3 = (FeederNode) adjacentNodes.get(2);
                ExternCell cell3 = (ExternCell) n3.getCell();

                if (!cell3.isShunted()) {
                    Coord c3 = coordsVoltageLevels.get(n3.getGraph());

                    if (c3.get(Y) < c2.get(Y)) {
                        // cell for node 3 with bottom orientation
                        cell3.setDirection(BusCell.Direction.BOTTOM);
                        n3.setDirection(BusCell.Direction.BOTTOM);
                        cell3.getRootBlock().setOrientation(Orientation.DOWN);
                    } else {
                        // cell for node 3 with top orientation
                        cell3.setDirection(BusCell.Direction.TOP);
                        n3.setDirection(BusCell.Direction.TOP);
                        cell3.getRootBlock().setOrientation(Orientation.UP);
                    }
                }
            }
        }
    }
}
