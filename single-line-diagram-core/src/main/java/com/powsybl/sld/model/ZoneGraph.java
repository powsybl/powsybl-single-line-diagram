/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model;

import com.fasterxml.jackson.core.JsonGenerator;
import com.powsybl.commons.PowsyblException;
import com.powsybl.sld.ZoneId;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public final class ZoneGraph extends AbstractGraph {

    private ZoneId zoneId;
    private List<SubstationGraph> nodes = new ArrayList<>();
    private List<LineEdge> edges = new ArrayList<>();
    private Map<String, SubstationGraph> nodesById = new HashMap<>();
    private Map<String, LineEdge> edgesById = new HashMap<>();

    private ZoneGraph(ZoneId zoneId) {
        if (zoneId.isEmpty()) {
            throw new PowsyblException("Zone without any substation");
        }
        this.zoneId = zoneId;
    }

    public static ZoneGraph create(ZoneId zoneId) {
        return new ZoneGraph(zoneId);
    }

    public void addNode(SubstationGraph sGraph) {
        nodes.add(sGraph);
        nodesById.put(sGraph.getSubstationId(), sGraph);
    }

    public void addEdge(String lineId, Node node1, Node node2) {
        LineEdge edge = new LineEdge(lineId, node1, node2);
        edges.add(edge);
        edgesById.put(lineId, edge);
    }

    public ZoneId getZoneId() {
        return zoneId;
    }

    public List<SubstationGraph> getNodes() {
        return nodes;
    }

    public List<LineEdge> getEdges() {
        return edges;
    }

    public SubstationGraph getNode(String id) {
        Objects.requireNonNull(id);
        return nodesById.get(id);
    }

    public LineEdge getEdge(String lineId) {
        Objects.requireNonNull(lineId);
        return edgesById.get(lineId);
    }

    @Override
    public void writeJson(JsonGenerator generator)  throws IOException {
        generator.writeStartArray();
        for (SubstationGraph graph : nodes) {
            graph.setGenerateCoordsInJson(generateCoordsInJson);
            graph.writeJson(generator);
        }
        generator.writeEndArray();

        generator.writeStartObject();
        if (generateCoordsInJson) {
            generator.writeNumberField("x", x);
            generator.writeNumberField("y", y);
        }
        generator.writeEndObject();
    }
}
