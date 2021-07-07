/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model;

import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;
import java.util.*;

/**
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public class ZoneGraph extends AbstractLineGraph {

    private final List<String> zone;
    private final List<SubstationGraph> nodes = new ArrayList<>();
    private final Map<String, SubstationGraph> nodesById = new HashMap<>();
    private final Map<String, BranchEdge> edgesById = new HashMap<>();

    protected ZoneGraph(List<String> zone) {
        this.zone = Objects.requireNonNull(zone);
    }

    public static ZoneGraph create(List<String> zone) {
        return new ZoneGraph(zone);
    }

    @Override
    public String getId() {
        return String.join("_", zone);
    }

    public void addNode(SubstationGraph sGraph) {
        nodes.add(sGraph);
        nodesById.put(sGraph.getSubstationId(), sGraph);
    }

    @Override
    public BranchEdge addLineEdge(String lineId, Node node1, Node node2) {
        BranchEdge edge = super.addLineEdge(lineId, node1, node2);
        edgesById.put(lineId, edge);
        return edge;
    }

    public List<String> getZone() {
        return zone;
    }

    public List<SubstationGraph> getNodes() {
        return nodes;
    }

    @Override
    public VoltageLevelGraph getVLGraph(String voltageLevelId) {
        Objects.requireNonNull(voltageLevelId);
        return nodes.stream().flatMap(SubstationGraph::getNodeStream).filter(g -> voltageLevelId.equals(g.getVoltageLevelInfos().getId())).findFirst().orElse(null);
    }

    public SubstationGraph getNode(String id) {
        Objects.requireNonNull(id);
        return nodesById.get(id);
    }

    public BranchEdge getLineEdge(String lineId) {
        Objects.requireNonNull(lineId);
        return edgesById.get(lineId);
    }

    public void writeJson(JsonGenerator generator) throws IOException {
        generator.writeStartObject();
        generator.writeArrayFieldStart("substations");
        for (SubstationGraph substationGraph : nodes) {
            substationGraph.setGenerateCoordsInJson(isGenerateCoordsInJson());
            substationGraph.writeJson(generator);
        }
        generator.writeEndArray();
        generator.writeArrayFieldStart("lineEdges");
        for (BranchEdge edge : getLineEdges()) {
            edge.writeJson(generator, isGenerateCoordsInJson());
        }
        generator.writeEndArray();
        generator.writeEndObject();
    }

}
