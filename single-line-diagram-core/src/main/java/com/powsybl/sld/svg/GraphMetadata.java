/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.svg;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.sld.layout.LayoutParameters;
import com.powsybl.sld.library.*;
import com.powsybl.sld.model.BusCell;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class GraphMetadata implements AnchorPointProvider {

    public static class NodeMetadata {

        private final String id;

        private String componentType;

        private Double rotationAngle;

        private final boolean open;

        private final String vId;

        private final String nextVId;

        private final BusCell.Direction direction;

        private final boolean vLabel;

        private final String equipmentId;

        @JsonCreator
        public NodeMetadata(@JsonProperty("id") String id,
                            @JsonProperty("vid") String vId,
                            @JsonProperty("nextVId") String nextVId,
                            @JsonProperty("componentType") String componentType,
                            @JsonProperty("rotationAngle") Double rotationAngle,
                            @JsonProperty("open") boolean open,
                            @JsonProperty("direction") BusCell.Direction direction,
                            @JsonProperty("vlabel") boolean vLabel,
                            @JsonProperty("equipmentId") String equipmentId) {
            this.id = Objects.requireNonNull(id);
            this.vId = Objects.requireNonNull(vId);
            this.nextVId = nextVId;
            this.componentType = componentType;
            this.rotationAngle = rotationAngle;
            this.open = Objects.requireNonNull(open);
            this.direction = direction;
            this.vLabel = vLabel;
            this.equipmentId = equipmentId;
        }

        public String getId() {
            return id;
        }

        public String getVId() {
            return vId;
        }

        public String getNextVId() {
            return nextVId;
        }

        public String getComponentType() {
            return componentType;
        }

        public Double getRotationAngle() {
            return rotationAngle;
        }

        public boolean isOpen() {
            return open;
        }

        public BusCell.Direction getDirection() {
            return direction;
        }

        public boolean isVLabel() {
            return vLabel;
        }

        public String getEquipmentId() {
            return equipmentId;
        }
    }

    public static class WireMetadata {

        private final String id;

        private final String nodeId1;

        private final String nodeId2;

        private final boolean straight;

        private final boolean snakeLine;

        @JsonCreator
        public WireMetadata(@JsonProperty("id") String id,
                            @JsonProperty("nodeId1") String nodeId1,
                            @JsonProperty("nodeId2") String nodeId2,
                            @JsonProperty("straight") boolean straight,
                            @JsonProperty("snakeline") boolean snakeline) {
            this.id = Objects.requireNonNull(id);
            this.nodeId1 = Objects.requireNonNull(nodeId1);
            this.nodeId2 = Objects.requireNonNull(nodeId2);
            this.straight = straight;
            this.snakeLine = snakeline;
        }

        public String getId() {
            return id;
        }

        public String getNodeId1() {
            return nodeId1;
        }

        public String getNodeId2() {
            return nodeId2;
        }

        public boolean isStraight() {
            return straight;
        }

        public boolean isSnakeLine() {
            return snakeLine;
        }
    }

    public static class LineMetadata {

        private final String id;

        private final String nodeId1;

        private final String nodeId2;

        @JsonCreator
        public LineMetadata(@JsonProperty("id") String id,
                            @JsonProperty("nodeId1") String nodeId1,
                            @JsonProperty("nodeId2") String nodeId2) {
            this.id = Objects.requireNonNull(id);
            this.nodeId1 = Objects.requireNonNull(nodeId1);
            this.nodeId2 = Objects.requireNonNull(nodeId2);
        }

        public String getId() {
            return id;
        }

        public String getNodeId1() {
            return nodeId1;
        }

        public String getNodeId2() {
            return nodeId2;
        }
    }

    public static class ArrowMetadata {

        private final String id;

        private final String wireId;

        private final double distance;

        @JsonCreator
        public ArrowMetadata(@JsonProperty("id") String id, @JsonProperty("wireId") String wireId1, @JsonProperty("distance") double distance) {
            this.id = Objects.requireNonNull(id);
            this.wireId = Objects.requireNonNull(wireId1);
            this.distance = distance;
        }

        public String getId() {
            return id;
        }

        public String getWireId() {
            return wireId;
        }

        public double getDistance() {
            return distance;
        }
    }

    private final Map<String, ComponentMetadata> componentMetadataByType = new HashMap<>();

    private final Map<String, ComponentMetadata> componentMetadataById = new HashMap<>();

    private final Map<String, NodeMetadata> nodeMetadataMap = new HashMap<>();

    private final Map<String, WireMetadata> wireMetadataMap = new HashMap<>();

    private final Map<String, LineMetadata> lineMetadataMap = new HashMap<>();

    private final LayoutParameters layoutParameters;

    private final Map<String, ArrowMetadata> arrowMetadataMap = new HashMap<>();

    public GraphMetadata() {
        this(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), new LayoutParameters());
    }

    @JsonCreator
    public GraphMetadata(@JsonProperty("components") List<ComponentMetadata> componentMetadataList,
                         @JsonProperty("nodes") List<NodeMetadata> nodeMetadataList,
                         @JsonProperty("wires") List<WireMetadata> wireMetadataList,
                         @JsonProperty("lines") List<LineMetadata> lineMetadataList,
                         @JsonProperty("arrows") List<ArrowMetadata> arrowMetadataList,
                         @JsonProperty("layoutParams") LayoutParameters layoutParams) {
        for (ComponentMetadata componentMetadata : componentMetadataList) {
            addComponentMetadata(componentMetadata);
        }
        for (NodeMetadata nodeMetadata : nodeMetadataList) {
            addNodeMetadata(nodeMetadata);
        }
        for (WireMetadata wireMetadata : wireMetadataList) {
            addWireMetadata(wireMetadata);
        }
        for (LineMetadata lineMetadata : lineMetadataList) {
            addLineMetadata(lineMetadata);
        }
        for (ArrowMetadata arrowMetadata : arrowMetadataList) {
            addArrowMetadata(arrowMetadata);
        }
        layoutParameters = layoutParams;
    }

    public static GraphMetadata parseJson(Path file) {
        try (Reader reader = Files.newBufferedReader(file)) {
            return parseJson(reader);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static GraphMetadata parseJson(InputStream inputStream) {
        Objects.requireNonNull(inputStream);
        ObjectMapper objectMapper = JsonUtil.createObjectMapper();
        try {
            return objectMapper.readValue(inputStream, GraphMetadata.class);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static GraphMetadata parseJson(Reader reader) {
        Objects.requireNonNull(reader);
        ObjectMapper objectMapper = JsonUtil.createObjectMapper();
        try {
            return objectMapper.readValue(reader, GraphMetadata.class);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void writeJson(Path file) {
        Objects.requireNonNull(file);
        try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            writeJson(writer);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void writeJson(Writer writer) {
        Objects.requireNonNull(writer);
        ObjectMapper objectMapper = JsonUtil.createObjectMapper();
        try {
            objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValue(writer, this);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void addComponentMetadata(ComponentMetadata metadata) {
        Objects.requireNonNull(metadata);
        componentMetadataByType.put(metadata.getType(), metadata);
        if (metadata.getId() != null) {
            componentMetadataById.put(metadata.getId(), metadata);
        }
    }

    public ComponentMetadata getComponentMetadata(String componentType) {
        return componentType != null ? componentMetadataByType.get(componentType) : null;
    }

    @Override
    public List<AnchorPoint> getAnchorPoints(String type, String id) {
        ComponentMetadata componentMetadata = null;
        if (id != null) {
            componentMetadata = componentMetadataById.get(id);
        }
        if (componentMetadata == null) {
            componentMetadata = getComponentMetadata(type);
        }
        return componentMetadata != null ? componentMetadata.getAnchorPoints()
                                         : Collections.singletonList(new AnchorPoint(0, 0, AnchorOrientation.NONE));
    }

    @JsonProperty("components")
    public List<ComponentMetadata> getComponentMetadata() {
        return ImmutableList.copyOf(componentMetadataByType.values());
    }

    public void addNodeMetadata(NodeMetadata metadata) {
        Objects.requireNonNull(metadata);
        nodeMetadataMap.put(metadata.getId(), metadata);
    }

    public NodeMetadata getNodeMetadata(String id) {
        Objects.requireNonNull(id);
        return nodeMetadataMap.get(id);
    }

    @JsonProperty("nodes")
    public List<NodeMetadata> getNodeMetadata() {
        return ImmutableList.copyOf(nodeMetadataMap.values());
    }

    public void addWireMetadata(WireMetadata metadata) {
        Objects.requireNonNull(metadata);
        wireMetadataMap.put(metadata.getId(), metadata);
    }

    public WireMetadata getWireMetadata(String id) {
        Objects.requireNonNull(id);
        return wireMetadataMap.get(id);
    }

    @JsonProperty("wires")
    public List<WireMetadata> getWireMetadata() {
        return ImmutableList.copyOf(wireMetadataMap.values());
    }

    public void addLineMetadata(LineMetadata metadata) {
        Objects.requireNonNull(metadata);
        lineMetadataMap.put(metadata.getId(), metadata);
    }

    public LineMetadata getLineMetadata(String id) {
        Objects.requireNonNull(id);
        return lineMetadataMap.get(id);
    }

    @JsonProperty("lines")
    public List<LineMetadata> getLineMetadata() {
        return ImmutableList.copyOf(lineMetadataMap.values());
    }

    public void addArrowMetadata(ArrowMetadata metadata) {
        Objects.requireNonNull(metadata);
        arrowMetadataMap.put(metadata.getId(), metadata);
    }

    public ArrowMetadata getArrowMetadata(String id) {
        Objects.requireNonNull(id);
        return arrowMetadataMap.get(id);
    }

    @JsonProperty("arrows")
    public List<ArrowMetadata> getArrowMetadata() {
        return ImmutableList.copyOf(arrowMetadataMap.values());
    }

    @JsonProperty("layoutParams")
    public LayoutParameters getLayoutParameters() {
        return layoutParameters;

    }
}
