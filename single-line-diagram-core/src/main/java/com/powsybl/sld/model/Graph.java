/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model;

import com.fasterxml.jackson.core.JsonGenerator;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.Pseudograph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class builds the connectivity among the elements of a voltageLevel
 * buildGraphAndDetectCell establishes the List of nodes, edges and nodeBuses
 * cells is built by the PatternCellDetector Class
 *
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public final class Graph extends AbstractGraph {

    private static final Logger LOGGER = LoggerFactory.getLogger(Graph.class);

    private String voltageLevelId;

    private String voltageLevelName;

    private double voltageLevelNominalV;

    private String substationId;

    private final boolean useName;

    private final List<Node> nodes = new ArrayList<>();

    private final List<Edge> edges = new ArrayList<>();

    private final SortedSet<Cell> cells = new TreeSet<>(
            Comparator.comparingInt(Cell::getNumber)); // cells sorted to avoid randomness

    private final Map<Node.NodeType, List<Node>> nodesByType = new EnumMap<>(Node.NodeType.class);

    private final Map<String, Node> nodesById = new HashMap<>();

    private Position maxBusStructuralPosition = new Position(0, 0);

    private Map<Integer, Map<Integer, BusNode>> vPosToHPosToNodeBus;

    private int cellCounter = 0;

    private final boolean forVoltageLevelDiagram;  // true if voltageLevel diagram
                                                   // false if substation diagram

    private final boolean showInductorFor3WT;

    Function<Node, BusCell.Direction> nodeDirection = node ->
            (node instanceof FeederNode && node.getCell() != null) ? ((ExternCell) node.getCell()).getDirection() : BusCell.Direction.UNDEFINED;

    protected static final int VALUE_SHIFT_FEEDER = 8;

    // by direction, max calculated height of the extern cells
    // (filled and used only when using the adapt cell height to content option)
    private Map<BusCell.Direction, Double> maxCalculatedCellHeight = new EnumMap<>(BusCell.Direction.class);

    private Graph(String id, String name, double nominalV, String substationId,
                  boolean useName, boolean forVoltageLevelDiagram, boolean showInductorFor3WT) {
        this.voltageLevelId = Objects.requireNonNull(id);
        this.voltageLevelName = name;
        this.voltageLevelNominalV = nominalV;
        this.substationId = substationId;
        this.useName = useName;
        this.forVoltageLevelDiagram = forVoltageLevelDiagram;
        this.showInductorFor3WT = showInductorFor3WT;
    }

    public static Graph create(String id, String name, double nominalV,
                               String substationId,
                               boolean useName, boolean forVoltageLevelDiagram,
                               boolean showInductorFor3WT) {
        Objects.requireNonNull(id);
        Objects.requireNonNull(substationId);
        return new Graph(id, name, nominalV, substationId, useName, forVoltageLevelDiagram, showInductorFor3WT);
    }

    public boolean isUseName() {
        return useName;
    }

    public boolean isShowInductorFor3WT() {
        return showInductorFor3WT;
    }

    public boolean isForVoltageLevelDiagram() {
        return forVoltageLevelDiagram;
    }

    public int getNextCellIndex() {
        return cellCounter++;
    }

    public void removeUnnecessaryFictitiousNodes() {
        List<Node> fictitiousNodesToRemove = nodes.stream()
                .filter(node -> node.getType() == Node.NodeType.FICTITIOUS)
                .collect(Collectors.toList());
        for (Node n : fictitiousNodesToRemove) {
            if (n.getAdjacentEdges().size() == 2) {
                List<Node> adjNodes = n.getAdjacentNodes();
                Node node1 = adjNodes.get(0);
                Node node2 = adjNodes.get(1);
                LOGGER.info("Remove fictitious node {} between {} and {}", n.getId(), node1.getId(), node2.getId());
                removeNode(n);
                addEdge(node1, node2);
            } else {
                LOGGER.info("Working on fictitious node {} with {} adjacent nodes", n.getId(), n.getAdjacentNodes().size());
                Node busNode = n.getAdjacentNodes().stream().filter(node -> node.getType() == Node.NodeType.BUS).findFirst().orElse(null);
                if (busNode != null) {
                    n.getAdjacentNodes().stream().filter(node -> !node.equals(busNode)).forEach(node -> {
                        LOGGER.info("Connecting {} to {}", node.getId(), busNode.getId());
                        addEdge(node, busNode);
                    });
                    LOGGER.info("Remove fictitious node {}", n.getId());
                    removeNode(n);
                } else {
                    LOGGER.warn("Cannot remove fictitious node {} because there are no adjacent BUS nodes", n.getId());
                }
            }
        }
    }

    public void logCellDetectionStatus() {
        Set<Cell> cellsLog = new HashSet<>();
        Map<Cell.CellType, Integer> cellCountByType = new EnumMap<>(Cell.CellType.class);
        for (Cell.CellType cellType : Cell.CellType.values()) {
            cellCountByType.put(cellType, 0);
        }
        int remainingNodeCount = 0;
        Map<Node.NodeType, Integer> remainingNodeCountByType = new EnumMap<>(Node.NodeType.class);
        for (Node.NodeType nodeType : Node.NodeType.values()) {
            remainingNodeCountByType.put(nodeType, 0);
        }
        for (Node node : nodes) {
            Cell cell = node.getCell();
            if (cell != null) {
                if (cellsLog.add(cell)) {
                    cellCountByType.put(cell.getType(), cellCountByType.get(cell.getType()) + 1);
                }
            } else {
                remainingNodeCount++;
                remainingNodeCountByType.put(node.getType(), remainingNodeCountByType.get(node.getType()) + 1);
            }
        }
        if (cellsLog.isEmpty()) {
            LOGGER.warn("No cell detected");
        } else {
            LOGGER.info("{} cells detected ({})", cellsLog.size(), cellCountByType);
        }
        if (remainingNodeCount > 0) {
            LOGGER.warn("{}/{} nodes not associated to a cell ({})",
                    remainingNodeCount, nodes.size(), remainingNodeCountByType);
        }
    }

    public UndirectedGraph<Node, Edge> toJgrapht() {
        UndirectedGraph<Node, Edge> graph = new Pseudograph<>(Edge.class);
        for (Node node : nodes) {
            graph.addVertex(node);
        }
        for (Edge edge : edges) {
            graph.addEdge(edge.getNode1(), edge.getNode2(), edge);
        }
        return graph;
    }

    public void addNode(Node node) {
        nodes.add(node);
        nodesByType.computeIfAbsent(node.getType(), nodeType -> new ArrayList<>()).add(node);
        nodesById.put(node.getId(), node);
    }

    public void removeNode(Node node) {
        nodes.remove(node);
        nodesByType.computeIfAbsent(node.getType(), nodeType -> new ArrayList<>()).remove(node);
        nodesById.remove(node.getId());
        for (Edge edge : new ArrayList<>(node.getAdjacentEdges())) {
            removeEdge(edge);
        }
    }

    public Node getNode(String id) {
        Objects.requireNonNull(id);
        return nodesById.get(id);
    }

    /**
     * Add an edge between the two nodes
     *
     * @param n1 first node
     * @param n2 second node
     */
    public void addEdge(Node n1, Node n2) {
        Edge edge = new Edge(n1, n2);
        edges.add(edge);
        n1.addAdjacentEdge(edge);
        n2.addAdjacentEdge(edge);
    }

    /**
     * Remove an edge between two nodes
     *
     * @param n1 first node
     * @param n2 second node
     */
    private void removeEdge(Node n1, Node n2) {
        for (Edge edge : edges) {
            if ((edge.getNode1().equals(n1) && edge.getNode2().equals(n2))
                    || (edge.getNode1().equals(n2) && edge.getNode2().equals(n1))) {
                removeEdge(edge);
                return;
            }
        }
    }

    private void removeEdge(Edge edge) {
        edge.getNode1().removeAdjacentEdge(edge);
        edge.getNode2().removeAdjacentEdge(edge);
        edges.remove(edge);
    }

    /**
     * Resolve when one EQ is connected with 2 switchs component
     */
    private void rIdentifyConnexComponent(Node node, List<Node> nodesIn, List<Node> connexComponent) {
        if (!connexComponent.contains(node)) {
            connexComponent.add(node);
            List<Node> nodesToVisit = node.getAdjacentNodes()
                    .stream()
                    .filter(nodesIn::contains)
                    .collect(Collectors.toList());
            for (Node n : nodesToVisit) {
                rIdentifyConnexComponent(n, nodesIn, connexComponent);
            }
        }
    }

    public List<List<Node>> getConnexComponents(List<Node> nodesIn) {
        List<Node> nodesToHandle = new ArrayList<>(nodesIn);
        List<List<Node>> result = new ArrayList<>();
        while (!nodesToHandle.isEmpty()) {
            Node n = nodesToHandle.get(0);
            List<Node> connexComponent = new ArrayList<>();
            rIdentifyConnexComponent(n, nodesIn, connexComponent);
            nodesToHandle.removeAll(connexComponent);
            result.add(connexComponent);
        }
        return result;
    }

    public Position getMaxBusStructuralPosition() {
        return maxBusStructuralPosition;
    }

    public void setMaxBusPosition() {
        List<Integer> h = new ArrayList<>();
        List<Integer> v = new ArrayList<>();
        getNodeBuses().forEach(nodeBus -> {
            v.add(nodeBus.getStructuralPosition().getV());
            h.add(nodeBus.getStructuralPosition().getH());
        });
        if (h.isEmpty() || v.isEmpty()) {
            return;
        }
        maxBusStructuralPosition.setH(Collections.max(h));
        maxBusStructuralPosition.setV(Collections.max(v));
    }

    public Stream<BusCell> getBusCells() {
        return cells.stream()
                .filter(cell -> cell instanceof BusCell && !((BusCell) cell).getPrimaryLegBlocks().isEmpty())
                .map(BusCell.class::cast);
    }

    private void buildVPosToHposToNodeBus() {
        vPosToHPosToNodeBus = new HashMap<>();
        getNodeBuses()
                .forEach(nodeBus -> {
                    int vPos = nodeBus.getStructuralPosition().getV();
                    int hPos = nodeBus.getStructuralPosition().getH();
                    vPosToHPosToNodeBus.putIfAbsent(vPos, new HashMap<>());
                    vPosToHPosToNodeBus.get(vPos).put(hPos, nodeBus);
                });
    }

    public void extendFeederWithMultipleSwitches() {
        List<Node> nodesToAdd = new ArrayList<>();
        for (Node n : nodes) {
            if (n instanceof FeederNode && n.getAdjacentNodes().size() > 1) {
                // Create a new fictitious node
                FictitiousNode nf = new FictitiousNode(Graph.this, n.getId() + "Fictif");
                nodesToAdd.add(nf);
                // Create all new edges and remove old ones
                List<Node> oldNeighboor = new ArrayList<>(n.getAdjacentNodes());
                for (Node neighboor : oldNeighboor) {
                    addEdge(nf, neighboor);
                    removeEdge(n, neighboor);
                }
                addEdge(n, nf);
            }
        }
        nodes.addAll(nodesToAdd);
    }

    //add a fictitious node between 2 switches or between a switch and a feeder
    //when one switch is connected to a bus
    public void extendFirstOutsideNode() {
        getNodeBuses().stream()
                .flatMap(node -> node.getAdjacentNodes().stream())
                .filter(node -> node.getType() == Node.NodeType.SWITCH)
                .forEach(nodeSwitch ->
                        nodeSwitch.getAdjacentNodes().stream()
                                .filter(node -> node.getType() == Node.NodeType.SWITCH ||
                                        node.getType() == Node.NodeType.FEEDER)
                                .forEach(node -> {
                                    removeEdge(node, nodeSwitch);
                                    FictitiousNode newNode = new FictitiousNode(Graph.this, nodeSwitch.getId() + "Fictif");
                                    addNode(newNode);
                                    addEdge(node, newNode);
                                    addEdge(nodeSwitch, newNode);
                                }));
    }

    //the first element shouldn't be a Breaker
    public void extendBreakerConnectedToBus() {
        getNodeBuses().forEach(nodeBus -> nodeBus.getAdjacentNodes().stream()
                .filter(node -> node.getType() == Node.NodeType.SWITCH
                        && ((SwitchNode) node).getKind() != SwitchNode.SwitchKind.DISCONNECTOR)
                .forEach(nodeSwitch -> addDoubleNode(nodeBus, nodeSwitch, "")));
    }

    //the first element shouldn't be a Feeder
    public void extendFeederConnectedToBus() {
        getNodeBuses().forEach(nodeBus -> nodeBus.getAdjacentNodes().stream()
                .filter(node -> node.getType() == Node.NodeType.FEEDER)
                .forEach(feeder -> addDoubleNode(nodeBus, feeder, "")));
    }

    public void extendSwitchBetweenBus(SwitchNode nodeSwitch) {
        List<Node> copyAdj = new ArrayList<>(nodeSwitch.getAdjacentNodes());
        addDoubleNode((BusNode) copyAdj.get(0), nodeSwitch, "0");
        addDoubleNode((BusNode) copyAdj.get(1), nodeSwitch, "1");
    }

    private void addDoubleNode(BusNode busNode, Node node, String suffix) {
        removeEdge(busNode, node);
        SwitchNode fNodeToBus = SwitchNode.createFictitious(Graph.this, node.getId() + "fSwitch" + suffix, node.isOpen());
        addNode(fNodeToBus);
        FictitiousNode fNodeToSw = new FictitiousNode(Graph.this, node.getId() + "fNode" + suffix);
        addNode(fNodeToSw);
        addEdge(busNode, fNodeToBus);
        addEdge(fNodeToBus, fNodeToSw);
        addEdge(fNodeToSw, node);
    }

    public void substitueNode(Node nodeOrigin, Node newNode) {
        while (!nodeOrigin.getAdjacentEdges().isEmpty()) {
            Edge edge = nodeOrigin.getAdjacentEdges().get(0);
            Node node1 = edge.getNode1() == nodeOrigin ? newNode : edge.getNode1();
            Node node2 = edge.getNode2() == nodeOrigin ? newNode : edge.getNode2();
            addEdge(node1, node2);
            removeEdge(edge);
        }
        removeNode(nodeOrigin);
    }

    public void substituteFictitiousNodesMirroringBusNodes() {
        getNodeBuses().forEach(busNode -> {
            List<Node> adjs = busNode.getAdjacentNodes();
            if (adjs.size() == 1 && adjs.get(0).getType() == Node.NodeType.FICTITIOUS) {
                Node adj = adjs.get(0);
                removeEdge(adj, busNode);
                substitueNode(adj, busNode);
            }
        });
    }

    public void substituteSingularFictitiousByFeederNode() {
        getNodes().stream()
                .filter(n -> n.getType() == Node.NodeType.FICTITIOUS && n.getAdjacentEdges().size() == 1)
                .forEach(n -> {
                    FeederNode feederNode = FeederNode.createFictitious(this, n.getId());
                    addNode(feederNode);
                    substitueNode(n, feederNode);
                });
    }

    public BusNode getVHNodeBus(int v, int h) {
        if (vPosToHPosToNodeBus == null) {
            buildVPosToHposToNodeBus();
        }
        if (!vPosToHPosToNodeBus.containsKey(v)) {
            return null;
        }
        if (!vPosToHPosToNodeBus.get(v).containsKey(h)) {
            return null;
        }
        return vPosToHPosToNodeBus.get(v).get(h);
    }

    public void addCell(Cell c) {
        cells.add(c);
    }

    public void removeCell(Cell c) {
        cells.remove(c);
    }

    public List<BusNode> getNodeBuses() {
        return nodesByType.computeIfAbsent(Node.NodeType.BUS, nodeType -> new ArrayList<>())
                .stream()
                .map(BusNode.class::cast)
                .collect(Collectors.toList());
    }

    public List<Node> getNodes() {
        return new ArrayList<>(nodes);
    }

    public List<Edge> getEdges() {
        return new ArrayList<>(edges);
    }

    public Set<Cell> getCells() {
        return new TreeSet<>(cells);
    }

    public String getVoltageLevelId() {
        return voltageLevelId;
    }

    public String getVoltageLevelName() {
        return voltageLevelName;
    }

    public double getVoltageLevelNominalV() {
        return voltageLevelNominalV;
    }

    public String getSubstationId() {
        return substationId;
    }

    public boolean isPositionNodeBusesCalculated() {
        return getNodeBuses().stream().allMatch(n -> n.getPosition().getH() != -1 && n.getPosition().getV() != -1);
    }

    /**
     * Adjust feeders height, positioning them on a descending/ascending ramp
     * (depending on their BusCell direction)
     */
    public void shiftFeedersPosition(double scaleShiftFeederNames) {
        Map<BusCell.Direction, List<Node>> orderedFeederNodesByDirection = getNodes().stream()
                .filter(node -> !node.isFictitious() && node instanceof FeederNode && node.getCell() != null)
                .sorted(Comparator.comparing(Node::getX))
                .collect(Collectors.groupingBy(node -> nodeDirection.apply(node)));

        Map<BusCell.Direction, Double> mapLev = Arrays.stream(BusCell.Direction.values()).collect(Collectors.toMap(d -> d, d -> 0.0));

        Stream.of(BusCell.Direction.values())
                .filter(direction -> orderedFeederNodesByDirection.get(direction) != null)
                .forEach(direction ->
                        orderedFeederNodesByDirection.get(direction).stream().skip(1).forEach(node -> {
                            double oldY = node.getY();
                            double newY = mapLev.get(direction) + scaleShiftFeederNames * VALUE_SHIFT_FEEDER;
                            node.setY(oldY - getY() + ((direction == BusCell.Direction.TOP) ? 1 : -1) * newY);
                            node.setInitY(oldY);
                            mapLev.put(direction, newY);
                        }));
    }

    @Override
    public void writeJson(JsonGenerator generator) throws IOException {
        generator.writeStartObject();

        generator.writeStringField("id", voltageLevelId);
        if (generateCoordsInJson) {
            generator.writeNumberField("x", x);
            generator.writeNumberField("y", y);
        }

        generator.writeArrayFieldStart("cells");
        for (Cell cell : cells) {
            cell.writeJson(generator);
        }
        generator.writeEndArray();

        generator.writeArrayFieldStart("edges");
        for (Edge edge : edges) {
            edge.writeJson(generator);
        }
        generator.writeEndArray();
        generator.writeEndObject();
    }

    public void resetCoords() {
        nodes.stream().forEach(Node::resetCoords);
    }

    public int getMaxH() {
        return getNodeBuses().stream()
                .mapToInt(nodeBus -> nodeBus.getPosition().getH() + nodeBus.getPosition().getHSpan())
                .max().orElse(0);
    }

    public int getMaxV() {
        return getNodeBuses().stream()
                .mapToInt(nodeBus -> nodeBus.getPosition().getV() + nodeBus.getPosition().getVSpan())
                .max().orElse(0);
    }

    public Double getMaxCalculatedCellHeight(BusCell.Direction direction) {
        return maxCalculatedCellHeight.get(direction);
    }

    public void setMaxCalculatedCellHeight(Map<BusCell.Direction, Double> maxCalculatedCellHeight) {
        this.maxCalculatedCellHeight = maxCalculatedCellHeight;
    }

    @Override
    public void setGenerateCoordsInJson(boolean generateCoordsInJson) {
        this.generateCoordsInJson = generateCoordsInJson;
    }

    @Override
    public boolean isGenerateCoordsInJson() {
        return generateCoordsInJson;
    }
}
