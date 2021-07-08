/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model;

import com.fasterxml.jackson.core.JsonGenerator;
import com.powsybl.sld.layout.LayoutParameters;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */

public abstract class AbstractCell implements Cell {
    final VoltageLevelGraph graph;
    private CellType type;
    private int number;
    protected final List<Node> nodes = new ArrayList<>();

    private Block rootBlock;

    AbstractCell(VoltageLevelGraph graph, CellType type) {
        this.graph = Objects.requireNonNull(graph);
        this.type = Objects.requireNonNull(type);
        number = graph.getNextCellIndex();
        graph.addCell(this);
    }

    public void addNodes(List<Node> nodesToAdd) {
        nodes.addAll(nodesToAdd);
    }

    public List<Node> getNodes() {
        return new ArrayList<>(nodes);
    }

    public void removeAllNodes(List<Node> nodeToRemove) {
        nodes.removeAll(nodeToRemove);
    }

    public void setNodes(List<Node> nodes) {
        this.nodes.addAll(nodes);
        // the cell of the node of a SHUNT node (which belongs to a SHUNT and an EXTERN cells)
        // is the cell of the EXTERN cell
        if (type != CellType.SHUNT) {
            nodes.forEach(node -> node.setCell(this));
        } else {
            nodes.stream().filter(node -> node.getType() != Node.NodeType.SHUNT).forEach(node -> node.setCell(this));
        }
    }

    public void setType(CellType type) {
        this.type = type;
    }

    public CellType getType() {
        return this.type;
    }

    public Block getRootBlock() {
        return rootBlock;
    }

    public void setRootBlock(Block rootBlock) {
        this.rootBlock = rootBlock;
    }

    public int getNumber() {
        return number;
    }

    protected void writeJsonContent(JsonGenerator generator) throws IOException {
        generator.writeStringField("type", type.name());
        generator.writeNumberField("number", number);
    }

    public void writeJson(JsonGenerator generator) throws IOException {
        generator.writeStartObject();
        writeJsonContent(generator);
        if (rootBlock != null) {
            generator.writeFieldName("rootBlock");
            rootBlock.writeJson(generator);
        }
        generator.writeEndObject();
    }

    public String getId() {
        return type + " " + number;
    }

    public String getFullId() {
        return type + nodes.stream().map(Node::getId).sorted().collect(Collectors.toList()).toString();
    }

    @Override
    public String toString() {
        return type + " " + nodes;
    }

    public VoltageLevelGraph getGraph() {
        return graph;
    }

    @Override
    public double calculateHeight(LayoutParameters layoutParam) {
        return 0.;
    }
}
