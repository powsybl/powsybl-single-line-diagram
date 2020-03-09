/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.force.layout;

import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.UndirectedGraph;
import org.gephi.graph.impl.EdgeImpl;
import org.gephi.graph.impl.GraphModelImpl;
import org.gephi.graph.impl.NodeImpl;
import org.gephi.layout.plugin.forceAtlas2.ForceAtlas2;
import org.gephi.layout.plugin.forceAtlas2.ForceAtlas2Builder;
import com.powsybl.sld.model.Coord;
import com.powsybl.sld.model.Graph;
import com.powsybl.sld.model.LineEdge;
import com.powsybl.sld.model.SubstationGraph;
import com.powsybl.sld.model.TwtEdge;
import com.powsybl.sld.model.ZoneGraph;

import java.util.Map;
import java.util.Random;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public final class ForceLayoutUtils {
    static final Random RANDOM = new Random();

    /*
     * Calculate the coordinates of the different voltage levels inside a substation
     * using the force layout algorithm
     */
    public static void doSubstationForceLayout(SubstationGraph graph, Map<Graph, Coord> coordsVoltageLevels) {
        // Creating the graph model for the ForceAtlas algorithm
        GraphModel graphModel = new GraphModelImpl();
        UndirectedGraph undirectedGraph = graphModel.getUndirectedGraph();
        for (Graph voltageLevelGraph : graph.getNodes()) {
            NodeImpl n = new NodeImpl(voltageLevelGraph.getVoltageLevelId());
            n.setPosition(RANDOM.nextFloat() * 1000, RANDOM.nextFloat() * 1000);
            undirectedGraph.addNode(n);
        }
        for (TwtEdge edge : graph.getEdges()) {
            NodeImpl node1 = (NodeImpl) undirectedGraph.getNode(edge.getNode1().getGraph().getVoltageLevelId());
            NodeImpl node2 = (NodeImpl) undirectedGraph.getNode(edge.getNode2().getGraph().getVoltageLevelId());
            undirectedGraph.addEdge(new EdgeImpl(edge.toString() + "_1_2", node1, node2, 0, 1, false));
            if (edge.getNode3() != null) {
                NodeImpl node3 = (NodeImpl) undirectedGraph.getNode(edge.getNode3().getGraph().getVoltageLevelId());
                undirectedGraph.addEdge(new EdgeImpl(edge.toString() + "_2_3", node2, node3, 0, 1, false));
            }
        }

        doForceAtlas2(graphModel);

        // Memorizing the voltage levels coordinates calculated by the ForceAtlas algorithm
        for (Graph voltageLevelGraph : graph.getNodes()) {
            org.gephi.graph.api.Node n = undirectedGraph.getNode(voltageLevelGraph.getVoltageLevelId());
            coordsVoltageLevels.put(voltageLevelGraph, new Coord(n.x(), n.y()));
        }
    }

    /*
     * Calculate the coordinates of the different substations inside a zone
     * using the force layout algorithm
     */
    public static void doZoneForceLayout(ZoneGraph graph, Map<SubstationGraph, Coord> coordsSubstations) {
        // Creating the graph model for the ForceAtlas algorithm
        GraphModel graphModel = new GraphModelImpl();
        UndirectedGraph undirectedGraph = graphModel.getUndirectedGraph();
        for (SubstationGraph substationGraph : graph.getNodes()) {
            NodeImpl n = new NodeImpl(substationGraph.getSubstationId());
            n.setPosition(RANDOM.nextFloat() * 1000, RANDOM.nextFloat() * 1000);
            undirectedGraph.addNode(n);
        }
        for (LineEdge edge : graph.getEdges()) {
            NodeImpl node1 = (NodeImpl) undirectedGraph.getNode(edge.getNode1().getGraph().getSubstationId());
            NodeImpl node2 = (NodeImpl) undirectedGraph.getNode(edge.getNode2().getGraph().getSubstationId());
            undirectedGraph.addEdge(new EdgeImpl(edge.toString() + "_1_2", node1, node2, 0, 1, false));
        }

        doForceAtlas2(graphModel);

        // Memorizing the substations coordinates calculated by the ForceAtlas algorithm
        for (SubstationGraph substationGraph : graph.getNodes()) {
            org.gephi.graph.api.Node n = undirectedGraph.getNode(substationGraph.getSubstationId());
            coordsSubstations.put(substationGraph, new Coord(n.x(), n.y()));
        }
    }

    private ForceLayoutUtils() { }

    private static void doForceAtlas2(GraphModel graphModel) {
        // Creating the ForceAtlas object and run the algorithm
        ForceAtlas2 forceAtlas2 = new ForceAtlas2Builder().buildLayout();
        forceAtlas2.setGraphModel(graphModel);
        forceAtlas2.resetPropertiesValues();
        forceAtlas2.setAdjustSizes(true);
        forceAtlas2.setOutboundAttractionDistribution(false);
        forceAtlas2.setEdgeWeightInfluence(1.5d);
        forceAtlas2.setGravity(10d);
        forceAtlas2.setJitterTolerance(.02);
        forceAtlas2.setScalingRatio(15.0);
        forceAtlas2.initAlgo();
        int maxSteps = 1000;

        for (int i = 0; i < maxSteps && forceAtlas2.canAlgo(); i++) {
            forceAtlas2.goAlgo();
        }
        forceAtlas2.endAlgo();
    }
}
