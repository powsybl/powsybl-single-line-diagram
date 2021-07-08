/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model;

import com.fasterxml.jackson.core.JsonGenerator;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class Node implements BaseNode {

    public enum NodeType {
        BUS,
        FEEDER,
        FICTITIOUS,
        SWITCH,
        SHUNT,
        OTHER
    }

    protected final VoltageLevelGraph graph;

    private NodeType type;

    private final String id;

    private final String name;

    private final String equipmentId;

    private final String componentType;

    private final boolean fictitious;

    private double x = -1;
    private double y = -1;

    private double initY = -1;  // y value before shifting the feeders height, if asked

    private Cell cell;

    private Double rotationAngle;

    private boolean open = false;

    private final List<Edge> adjacentEdges = new ArrayList<>();

    private String label;

    /**
     * Constructor
     */
    protected Node(NodeType type, String id, String name, String equipmentId, String componentType, boolean fictitious, VoltageLevelGraph graph) {
        this.type = Objects.requireNonNull(type);
        this.name = name;
        this.equipmentId = equipmentId;
        this.componentType = Objects.requireNonNull(componentType);
        this.fictitious = fictitious;
        // graph can be null here : for example, in a substation diagram, a fictitious node is created outside
        // any graph, in order to link the different windings together
        this.graph = graph;
        // for unicity purpose (in substation diagram), we prefix the id of the fictitious node with the voltageLevel id and "_"
        String tmpId = Objects.requireNonNull(id);
        if (type == NodeType.FICTITIOUS &&
                graph != null &&
                !StringUtils.startsWith(tmpId, "FICT_" + this.graph.getVoltageLevelInfos().getId() + "_")) {
            this.id = "FICT_" + graph.getVoltageLevelInfos().getId() + "_" + tmpId;
        } else {
            this.id = tmpId;
        }
    }

    public Cell getCell() {
        return cell;
    }

    public void setCell(Cell cell) {
        this.cell = cell;
    }

    public VoltageLevelGraph getGraph() {
        return graph;
    }

    @Override
    public String getComponentType() {
        return componentType;
    }

    @Override
    public Double getRotationAngle() {
        return rotationAngle;
    }

    public boolean isFictitious() {
        return fictitious;
    }

    public void setType(NodeType type) {
        this.type = type;
    }

    @Override
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEquipmentId() {
        return equipmentId;
    }

    public String getLabel() {
        if (label != null) {
            return label;
        } else {
            return graph.isUseName() ? name : equipmentId;
        }
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public List<Node> getAdjacentNodes() {
        return adjacentEdges.stream()
                .map(edge -> edge.getNode1() == Node.this ? edge.getNode2() : edge.getNode1())
                .collect(Collectors.toList());
    }

    public List<Edge> getAdjacentEdges() {
        return adjacentEdges;
    }

    public void addAdjacentEdge(Edge e) {
        adjacentEdges.add(e);
    }

    void removeAdjacentEdge(Edge e) {
        adjacentEdges.remove(e);
    }

    public Stream<Node> getListNodeAdjInCell(Cell cell) {
        return getAdjacentNodes().stream().filter(n -> cell.getNodes().contains(n));
    }

    @Override
    public double getX() {
        return x;
    }

    public void setX(double x) {
        setX(x, true);
    }

    public void setX(double x, boolean addXGraph) {
        this.x = x + (addXGraph ? graph.getX() : 0);
    }

    @Override
    public double getY() {
        return y;
    }

    public void setY(double y) {
        setY(y, true);
    }

    public void setY(double y, boolean addYGraph) {
        this.y = y + (addYGraph ? graph.getY() : 0);
    }

    public double getInitY() {
        return initY;
    }

    public void setInitY(double initY) {
        this.initY = initY;
    }

    public NodeType getType() {
        return this.type;
    }

    @Override
    public boolean isRotated() {
        return rotationAngle != null;
    }

    public void setRotationAngle(Double rotationAngle) {
        this.rotationAngle = rotationAngle;
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    /**
     * Check similarity with another node
     *
     * @param n the node to compare with
     * @return true IF the both are the same OR they are both Busbar OR they are both EQ(but not Busbar);
     * false otherwise
     **/
    public boolean checkNodeSimilarity(Node n) {
        return this.equals(n)
                || ((similarToAFeederNode(this)) && (similarToAFeederNode(n)))
                || ((this instanceof BusNode) && (n instanceof BusNode));
    }

    public boolean similarToAFeederNode(Node n) {
        return (n instanceof FeederNode)
                || (n.getType() == NodeType.FICTITIOUS && n.adjacentEdges.size() == 1);
    }

    protected void writeJsonContent(JsonGenerator generator) throws IOException {
        generator.writeStringField("type", type.name());
        generator.writeStringField("id", id);
        if (name != null) {
            generator.writeStringField("name", name);
        }
        generator.writeStringField("equipmentId", equipmentId);
        generator.writeStringField("componentType", componentType);
        generator.writeBooleanField("fictitious", fictitious);
        if (graph != null && graph.isGenerateCoordsInJson()) {
            generator.writeNumberField("x", x);
            generator.writeNumberField("y", y);
        }
        if (rotationAngle != null) {
            generator.writeNumberField("rotationAngle", rotationAngle);
        }
        generator.writeBooleanField("open", open);
        if (label != null) {
            generator.writeStringField("label", label);
        }
    }

    public void writeJson(JsonGenerator generator) throws IOException {
        generator.writeStartObject();
        writeJsonContent(generator);
        generator.writeEndObject();
    }

    @Override
    public String toString() {
        return type + " " + name + " " + id;
    }

    public void resetCoords() {
        x = -1;
        y = -1;
        initY = -1;
    }

    public void shiftY(double yShift) {
        y += yShift;
    }

    /**
     * Get voltage level infos for this node. By default it is the voltage level infos of the graph but it
     * could be override in case of node that represents an external voltage level.
     */
    public VoltageLevelInfos getVoltageLevelInfos() {
        if (graph != null) {
            return graph.getVoltageLevelInfos();
        }
        return null;
    }
}
