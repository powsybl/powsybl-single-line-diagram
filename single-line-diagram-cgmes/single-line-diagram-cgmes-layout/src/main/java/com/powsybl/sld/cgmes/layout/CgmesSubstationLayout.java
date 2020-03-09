/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.cgmes.layout;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.powsybl.sld.layout.VoltageLevelLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.sld.layout.LayoutParameters;
import com.powsybl.sld.layout.SubstationLayout;
import com.powsybl.sld.model.FictitiousNode;
import com.powsybl.sld.model.Graph;
import com.powsybl.sld.model.Node;
import com.powsybl.sld.model.SubstationGraph;
import com.powsybl.sld.model.TwtEdge;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public class CgmesSubstationLayout extends AbstractCgmesLayout implements SubstationLayout {

    private static final Logger LOG = LoggerFactory.getLogger(CgmesSubstationLayout.class);

    private final SubstationGraph graph;

    public CgmesSubstationLayout(SubstationGraph graph, Network network) {
        this.network = Objects.requireNonNull(network);
        Objects.requireNonNull(graph);
        for (Graph vlGraph : graph.getNodes()) {
            removeFictitiousNodes(vlGraph, network.getVoltageLevel(vlGraph.getVoltageLevelId()));
        }
        fixTransformersLabel = true;
        this.graph = graph;
    }

    @Override
    public void run(LayoutParameters layoutParam, boolean applyVLLayouts,
                    boolean manageSnakeLines, Map<Graph, VoltageLevelLayout> mapVLayouts) {
        String diagramName = layoutParam.getDiagramName();
        if (!checkDiagram(diagramName, "substation " + graph.getSubstationId())) {
            return;
        }
        LOG.info("Applying CGMES-DL layout to network {}, substation {}, diagram name {}", network.getId(), graph.getSubstationId(), diagramName);
        for (Graph vlGraph : graph.getNodes()) {
            VoltageLevel vl = network.getVoltageLevel(vlGraph.getVoltageLevelId());
            setNodeCoordinates(vl, vlGraph, diagramName);
        }
        for (Graph vlGraph : graph.getNodes()) {
            vlGraph.getNodes().forEach(node -> shiftNodeCoordinates(node, layoutParam.getScaleFactor()));
        }
        if (layoutParam.getScaleFactor() != 1) {
            for (Graph vlGraph : graph.getNodes()) {
                vlGraph.getNodes().forEach(node -> scaleNodeCoordinates(node, layoutParam.getScaleFactor()));
            }
        }
        for (Graph vlGraph : graph.getNodes()) {
            setVoltageLevelCoord(vlGraph);
        }
        splitTwtEdges();
    }

    private void splitTwtEdges() {
        // we split the original edge in two parts, with a new fictitious node between the two new edges
        List<TwtEdge> newEdges = new ArrayList<>();
        for (TwtEdge edge : graph.getEdges()) {
            // Creation of a new fictitious node outside vl graphs
            String idNodeFict = edge.getNode1().getId() + "_" + edge.getNode2().getId();
            if (edge.getNodes().size() == 3) {
                idNodeFict = edge.getNode1().getId() + "_" + edge.getNode2().getId() + "_" + edge.getNode3().getId();
            }
            Node nodeFict = new FictitiousNode(null, idNodeFict, edge.getComponentType());
            nodeFict.setX(edge.getNode1().getX(), false, false);
            nodeFict.setY(edge.getNode1().getY(), false, false);

            // Creation of a new edge between node1 and the new fictitious node
            TwtEdge edge1 = new TwtEdge(edge.getComponentType(), edge.getNode1(), nodeFict);
            newEdges.add(edge1);
            nodeFict.addAdjacentEdge(edge1);

            // Creation of a new edge between the new fictitious node and node2
            TwtEdge edge2 = new TwtEdge(edge.getComponentType(), nodeFict, edge.getNode2());
            newEdges.add(edge2);
            nodeFict.addAdjacentEdge(edge2);

            if (edge.getNodes().size() == 3) {
                // Creation of a new edge between the new fictitious node and node3
                TwtEdge edge3 = new TwtEdge(edge.getComponentType(), nodeFict, edge.getNode3());
                newEdges.add(edge3);
                nodeFict.addAdjacentEdge(edge3);
            }

            // the new fictitious node is stored in the substation graph
            graph.addMultiTermNode(nodeFict);
        }
        // replace the old edges with the new edges in the substation graph
        graph.setEdges(newEdges);
    }
}
