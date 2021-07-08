/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model;

import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Slimane Amar <slimane.amar at rte-france.com>
 */
public abstract class AbstractBaseGraph extends AbstractLineGraph implements BaseGraph {

    protected List<TwtEdge> twtEdges = new ArrayList<>();

    protected List<Node> multiTermNodes = new ArrayList<>();

    @Override
    public List<TwtEdge> getTwtEdges() {
        return new ArrayList<>(twtEdges);
    }

    @Override
    public List<Node> getMultiTermNodes() {
        return multiTermNodes;
    }

    @Override
    public TwtEdge addTwtEdge(Node node1, Node node2) {
        TwtEdge edge = new TwtEdge(node1, node2);
        twtEdges.add(edge);
        return edge;
    }

    @Override
    public void addMultiTermNode(Node node) {
        multiTermNodes.add(node);
    }

    protected void writeBranchFields(JsonGenerator generator) throws IOException {
        generator.writeArrayFieldStart("multitermNodes");
        for (Node multitermNode : multiTermNodes) {
            multitermNode.writeJson(generator);
        }
        generator.writeEndArray();
        generator.writeArrayFieldStart("twtEdges");
        for (TwtEdge edge : twtEdges) {
            edge.writeJson(generator, isGenerateCoordsInJson());
        }
        generator.writeEndArray();
        generator.writeArrayFieldStart("lineEdges");
        for (LineEdge edge : getLineEdges()) {
            edge.writeJson(generator, isGenerateCoordsInJson());
        }
        generator.writeEndArray();
    }
}
