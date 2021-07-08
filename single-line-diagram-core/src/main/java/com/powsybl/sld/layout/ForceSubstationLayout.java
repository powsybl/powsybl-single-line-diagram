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
import java.util.stream.Collectors;

import static com.powsybl.sld.model.Coord.Dimension.X;
import static com.powsybl.sld.model.Coord.Dimension.Y;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class ForceSubstationLayout extends AbstractSubstationLayout {
    private InfosNbSnakeLinesForce infosNbSnakeLines;
    private ForceSubstationLayoutFactory.CompactionType compactionType;

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

        infosNbSnakeLines = InfosNbSnakeLinesForce.create(getGraph(), compactionType);
        manageSnakeLines(layoutParameters);
    }

    @Override
    public void manageSnakeLines(LayoutParameters layoutParameters) {
        getGraph().getNodes().forEach(g -> manageSnakeLines(g, layoutParameters));
        manageSnakeLines(getGraph(), layoutParameters);
    }

    protected List<Point> calculatePolylineSnakeLine(LayoutParameters layoutParam, Node node1, Node node2,
                                                     boolean increment) {
        List<Point> polyline = new ArrayList<>();
        polyline.add(node1.getCoordinates());
        addMiddlePoints(layoutParam, node1, node2, increment, polyline);
        polyline.add(node2.getCoordinates());
        return polyline;
    }

    protected void addMiddlePoints(LayoutParameters layoutParam, Node node1, Node node2, boolean increment, List<Point> polyline) {
        BusCell.Direction dNode1 = getNodeDirection(node1, 1);
        BusCell.Direction dNode2 = getNodeDirection(node2, 2);

        // increment not needed for 3WT for the common node
        String vl1 = node1.getGraph().getVoltageLevelInfos().getId();
        int nbSnakeLinesH1 = increment
            ? infosNbSnakeLines.incrementAndGetNbSnakeLinesTopBottom(vl1, dNode1)
            : infosNbSnakeLines.getNbSnakeLinesTopBottom(vl1, dNode1);
        double decal1V = nbSnakeLinesH1 * layoutParam.getVerticalSnakeLinePadding();

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
            int nbSnakeLinesH2 = infosNbSnakeLines.incrementAndGetNbSnakeLinesTopBottom(vl2, dNode2);
            double decal2V = nbSnakeLinesH2 * layoutParam.getVerticalSnakeLinePadding();

            double ySnakeLine1 = getYSnakeLine(node1, dNode1, decal1V, layoutParam);
            double ySnakeLine2 = getYSnakeLine(node2, dNode2, decal2V, layoutParam);

            VoltageLevelGraph rightestVl = node1.getGraph().getX() > node2.getGraph().getX() ? node1.getGraph() : node2.getGraph();
            int nbSnakeLinesV = infosNbSnakeLines.incrementAndGetNbSnakeLinesLeft(rightestVl.getId());
            double decalH = nbSnakeLinesV * layoutParam.getHorizontalSnakeLinePadding();
            double xSnakeLine = rightestVl.getX() - decalH;
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
            if (compactionType != ForceSubstationLayoutFactory.CompactionType.VERTICAL) {
                List<String> vls = infosNbSnakeLines.getYSortedVls();
                int iVl = vls.indexOf(node.getGraph().getId());
                if (iVl == 0) {
                    return node.getCoordinates().getY() - decalV;
                } else {
                    String vlAboveId = vls.get(iVl - 1);
                    VoltageLevelGraph vlAbove = getGraph().getNodeStream().filter(voltageLevelGraph -> voltageLevelGraph.getId().equals(vlAboveId)).findFirst().orElseThrow();
                    return vlAbove.getY() + layoutParam.getInitialYBus() + layoutParam.getVerticalSpaceBus() * (vlAbove.getMaxV() - 1) + layoutParam.getStackHeight() + layoutParam.getExternCellHeight()
                        + decalV;
                }
            } else {
                return node.getCoordinates().getY() - decalV;
            }
        }
    }

    private boolean facingNodes(Node node1, Node node2) {
        if (compactionType == ForceSubstationLayoutFactory.CompactionType.VERTICAL) {
            return false;
        }
        BusCell.Direction dNode1 = getNodeDirection(node1, 1);
        BusCell.Direction dNode2 = getNodeDirection(node2, 2);
        return (dNode1 == BusCell.Direction.BOTTOM && dNode2 == BusCell.Direction.TOP && adjacentGraphs(node1, node2))
            || (dNode1 == BusCell.Direction.TOP && dNode2 == BusCell.Direction.BOTTOM && adjacentGraphs(node2, node1));
    }

    private boolean adjacentGraphs(Node node1, Node node2) {
        List<String> ySortedVl = infosNbSnakeLines.getYSortedVls();
        int i1 = ySortedVl.indexOf(node1.getGraph().getId());
        int i2 = ySortedVl.indexOf(node2.getGraph().getId());
        return i2 - i1 == 1;
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

}
