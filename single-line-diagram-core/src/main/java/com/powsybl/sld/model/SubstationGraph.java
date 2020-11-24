/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model;

import com.fasterxml.jackson.core.JsonGenerator;
import org.jgrapht.Graph;
import org.jgrapht.graph.Pseudograph;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class builds the connectivity among the voltageLevels of a substation
 * buildSubstationGraph establishes the List of nodes, edges
 *
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class SubstationGraph extends AbstractBaseGraph {

    private String substationId;

    private final List<VoltageLevelGraph> nodes = new ArrayList<>();

    private final Map<String, VoltageLevelGraph> nodesById = new HashMap<>();

    /**
     * Constructor
     */
    protected SubstationGraph(String id) {
        this.substationId = Objects.requireNonNull(id);
    }

    public static SubstationGraph create(String id) {
        Objects.requireNonNull(id);
        return new SubstationGraph(id);
    }

    public void addNode(VoltageLevelGraph node) {
        nodes.add(node);
        nodesById.put(node.getId(), node);
    }

    public VoltageLevelGraph getNode(String id) {
        Objects.requireNonNull(id);
        return nodesById.get(id);
    }

    public BranchEdge addEdge(String id, Node node1, Node node2) {
        return addTwtEdge(id, node1, node2);
    }

    @Override
    public VoltageLevelGraph getVLGraph(String voltageLevelId) {
        Objects.requireNonNull(voltageLevelId);
        return nodes.stream().filter(g -> voltageLevelId.equals(g.getVoltageLevelInfos().getId())).findFirst().orElse(null);
    }

    public List<VoltageLevelGraph> getNodes() {
        return new ArrayList<>(nodes);
    }

    public Stream<VoltageLevelGraph> getNodeStream() {
        return getNodes().stream();
    }

    public List<BranchEdge> getEdges() {
        return Stream.concat(getLineEdges().stream(), twtEdges.stream()).collect(Collectors.toList());
    }

    public boolean graphAdjacents(VoltageLevelGraph g1, VoltageLevelGraph g2) {
        if (g1 == g2) {
            return true;
        } else {
            int nbNodes = nodes.size();
            for (int i = 0; i < nbNodes - 1; i++) {
                if (nodes.get(i) == g1 && nodes.get(i + 1) == g2) {
                    return true;
                }
            }
        }
        return false;
    }

    public String getSubstationId() {
        return substationId;
    }

    public Graph<VoltageLevelGraph, Object> toJgrapht() {
        Graph<VoltageLevelGraph, Object> graph = new Pseudograph<>(Object.class);

        for (VoltageLevelGraph voltageLevelGraph : getNodes()) {
            graph.addVertex(voltageLevelGraph);
        }

        for (Node multiNode : getMultiTermNodes()) {
            // the multiTermNode itself is not in the graph created
            // edges are added between its adjacent nodes
            List<Node> adjacentNodes = multiNode.getAdjacentNodes();

            graph.addEdge(adjacentNodes.get(0).getGraph(), adjacentNodes.get(1).getGraph());

            if (adjacentNodes.size() == 3) {
                graph.addEdge(adjacentNodes.get(0).getGraph(), adjacentNodes.get(2).getGraph());
                graph.addEdge(adjacentNodes.get(1).getGraph(), adjacentNodes.get(2).getGraph());
            }
        }

        return graph;
    }

    @Override
    public String getId() {
        return getSubstationId();
    }

    @Override
    public void writeJson(JsonGenerator generator) throws IOException {
        generator.writeStartObject();
        generator.writeStringField("substationId", substationId);
        generator.writeArrayFieldStart("voltageLevels");
        for (VoltageLevelGraph graph : nodes) {
            graph.setGenerateCoordsInJson(isGenerateCoordsInJson());
            graph.writeJson(generator);
        }
        generator.writeEndArray();

        writeBranchFields(generator);

        generator.writeEndObject();
    }
}
