/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model;

import com.fasterxml.jackson.core.JsonGenerator;
import com.powsybl.sld.library.ComponentTypeName;
import org.jgrapht.graph.Pseudograph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.powsybl.sld.model.Position.Dimension.H;
import static com.powsybl.sld.model.Position.Dimension.V;

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
public class VoltageLevelGraph extends AbstractBaseGraph {

    private static final Logger LOGGER = LoggerFactory.getLogger(VoltageLevelGraph.class);

    private final VoltageLevelInfos voltageLevelInfos;

    private final boolean useName;

    private final List<Node> nodes = new ArrayList<>();

    private final List<Edge> edges = new ArrayList<>();

    private final SortedSet<Cell> cells = new TreeSet<>(
            Comparator.comparingInt(Cell::getNumber)); // cells sorted to avoid randomness

    private final Map<Node.NodeType, List<Node>> nodesByType = new EnumMap<>(Node.NodeType.class);

    private final Map<String, Node> nodesById = new HashMap<>();

    private int maxHorizontalBusPosition = 0;
    private int maxVerticalBusPosition = 0;

    private Map<Integer, Map<Integer, BusNode>> vPosToHPosToNodeBus;

    private int cellCounter = 0;

    private double x = 0;
    private double y = 0;

    private final boolean forVoltageLevelDiagram;  // true if voltageLevel diagram
    // false if substation diagram

    Function<Node, BusCell.Direction> nodeDirection = node ->
            (node instanceof FeederNode && node.getCell() != null) ? ((ExternCell) node.getCell()).getDirection() : BusCell.Direction.UNDEFINED;

    protected static final int VALUE_SHIFT_FEEDER = 8;

    // by direction, max calculated height of the extern cells
    // (filled and used only when using the adapt cell height to content option)
    private Map<BusCell.Direction, Double> maxCalculatedCellHeight = new EnumMap<>(BusCell.Direction.class);

    protected VoltageLevelGraph(VoltageLevelInfos voltageLevelInfos, boolean useName, boolean forVoltageLevelDiagram) {
        this.voltageLevelInfos = Objects.requireNonNull(voltageLevelInfos);
        this.useName = useName;
        this.forVoltageLevelDiagram = forVoltageLevelDiagram;
    }

    public static VoltageLevelGraph create(VoltageLevelInfos voltageLevelInfos,
                                           boolean useName, boolean forVoltageLevelDiagram) {
        return new VoltageLevelGraph(voltageLevelInfos, useName, forVoltageLevelDiagram);
    }

    @Override
    public String getId() {
        return voltageLevelInfos.getId();
    }

    public boolean isUseName() {
        return useName;
    }

    public boolean isForVoltageLevelDiagram() {
        return forVoltageLevelDiagram;
    }

    public int getNextCellIndex() {
        return cellCounter++;
    }

    public void removeUnnecessaryFictitiousNodes() {
        List<Node> fictitiousNodesToRemove = nodes.stream()
                .filter(node -> node.getType() == Node.NodeType.FICTITIOUS && !(node instanceof BusBreakerConnection))
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

    public org.jgrapht.Graph<Node, Edge> toJgrapht() {
        org.jgrapht.Graph<Node, Edge> graph = new Pseudograph<>(Edge.class);
        for (Node node : nodes) {
            graph.addVertex(node);
        }
        for (Edge edge : edges) {
            graph.addEdge(edge.getNode1(), edge.getNode2(), edge);
        }
        return graph;
    }

    public void addNode(Node node) {
        if (nodes.contains(node)) {
            throw new AssertionError("The node cannot be added, it is already in the graph");
        }
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

    @Override
    public VoltageLevelGraph getVLGraph(String voltageLevelId) {
        Objects.requireNonNull(voltageLevelId);
        return voltageLevelId.equals(voltageLevelInfos.getId()) ? this : null;
    }

    /**
     * Add an edge between the two nodes
     *
     * @param n1 first node
     * @param n2 second node
     */
    public Edge addEdge(Node n1, Node n2) {
        Edge edge = new Edge(n1, n2);
        edges.add(edge);
        n1.addAdjacentEdge(edge);
        n2.addAdjacentEdge(edge);
        return edge;
    }

    /**
     * Remove an edge between two nodes
     *
     * @param n1 first node
     * @param n2 second node
     */
    private void removeEdge(Node n1, Node n2) {
        for (Edge edge : n1.getAdjacentEdges()) {
            if (edge.getNode1().equals(n2) || edge.getNode2().equals(n2)) {
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
            node.getAdjacentNodes()
                    .stream()
                    .filter(nodesIn::contains)
                    .forEach(n -> rIdentifyConnexComponent(n, nodesIn, connexComponent));
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

    public void setMaxBusPosition() {
        List<Integer> h = new ArrayList<>();
        List<Integer> v = new ArrayList<>();
        getNodeBuses().forEach(nodeBus -> {
            v.add(nodeBus.getBusbarIndex());
            h.add(nodeBus.getSectionIndex());
        });
        if (h.isEmpty() || v.isEmpty()) {
            return;
        }
        setMaxHorizontalBusPosition(Collections.max(h));
        setMaxVerticalBusPosition(Collections.max(v));
    }

    public Stream<BusCell> getBusCells() {
        return cells.stream()
                .filter(cell -> cell instanceof BusCell && !((BusCell) cell).getLegPrimaryBlocks().isEmpty())
                .map(BusCell.class::cast);
    }

    private void buildVPosToHposToNodeBus() {
        vPosToHPosToNodeBus = new HashMap<>();
        getNodeBuses()
                .forEach(nodeBus -> {
                    int vPos = nodeBus.getBusbarIndex();
                    int hPos = nodeBus.getSectionIndex();
                    vPosToHPosToNodeBus.putIfAbsent(vPos, new HashMap<>());
                    vPosToHPosToNodeBus.get(vPos).put(hPos, nodeBus);
                });
    }

    /**
     * Insert fictitious node(s) before feeders in order for the feeder to be properly displayed:
     * feeders need at least one inserted fictitious node to have enough space to display the feeder arrows.
     * Some special cases:
     *  - feeders connected directly to a bus need 3 additional nodes (1 fictitious disconnector, 2 internal nodes) to obey the Leg/Body/Feeder structure
     *  - feeders connected to a bus through a disconnector need 2 additional internal nodes to obey the Leg/Body/Feeder structure
     *  - 3WT do not need any fictitious node inserted here as they already have the fictitious Middle3WTNode
     */
    public void insertFictitiousNodesAtFeeders() {
        List<Node> nodesToAdd = new ArrayList<>();
        List<Node> feederNodes = nodesByType.computeIfAbsent(Node.NodeType.FEEDER, nodeType -> new ArrayList<>());
        for (Node feederNode : feederNodes) {
            List<Node> adjacentNodes = feederNode.getAdjacentNodes();
            if (isFeederConnectedToBus(feederNode)) {
                // Feeders linked directly to a bus need 3 fictitious nodes to be properly displayed:
                //  - 1 fictitious disconnector on the bus
                //  - 2 internal nodes to have LegPrimaryBlock + BodyPrimaryBlock + FeederPrimaryBlock
                addTripleNode(adjacentNodes.get(0), feederNode, nodesToAdd);
            } else if (isFeederConnectedToBusDisconnector(feederNode)) {
                // Feeders linked directly to a bus disconnector need 2 internal nodes to be properly displayed, in order
                // to have LegPrimaryBlock + BodyPrimaryBlock + FeederPrimaryBlock
                insertTwoInternalNodes(adjacentNodes.get(0), feederNode, nodesToAdd);
            } else if (!isFeeder3WT(feederNode)) {
                // Three-winding transformers do not need to be extended as the Middle3WTNode is already itself an internal node
                // Create a new fictitious node
                InternalNode nf = new InternalNode(feederNode.graph, feederNode.getId() + "Fictif");
                nodesToAdd.add(nf);
                // Create all new edges and remove old ones
                for (Node neighbor : adjacentNodes) {
                    addEdge(neighbor, nf);
                    removeEdge(neighbor, feederNode);
                }
                addEdge(nf, feederNode);
            }
        }

        nodesToAdd.forEach(this::addNode);
    }

    private boolean isFeederConnectedToBus(Node feederNode) {
        List<Node> adjacentNodes = feederNode.getAdjacentNodes();
        return adjacentNodes.size() == 1 && adjacentNodes.get(0).getType() == Node.NodeType.BUS;
    }

    private boolean isFeederConnectedToBusDisconnector(Node feederNode) {
        List<Node> adjacentNodes = feederNode.getAdjacentNodes();
        return adjacentNodes.size() == 1
            && adjacentNodes.get(0).getType() == Node.NodeType.SWITCH
            && ((SwitchNode) adjacentNodes.get(0)).getKind() == SwitchNode.SwitchKind.DISCONNECTOR
            && adjacentNodes.get(0).getAdjacentNodes().stream().anyMatch(n -> n.getType() == Node.NodeType.BUS);
    }

    private boolean isFeeder3WT(Node feederNode) {
        List<Node> adjacentNodes = feederNode.getAdjacentNodes();
        return adjacentNodes.size() == 1 && adjacentNodes.get(0).getComponentType().equals(ComponentTypeName.THREE_WINDINGS_TRANSFORMER);
    }

    private void addTripleNode(Node busNode, Node feederNode, List<Node> nodesToAdd) {
        removeEdge(busNode, feederNode);

        // Create nodes
        SwitchNode fNodeToBus = SwitchNode.createFictitious(VoltageLevelGraph.this, feederNode.getId() + "fSwitch", false);
        InternalNode fNodeToSw1 = new InternalNode(VoltageLevelGraph.this, feederNode.getId() + "fNode1");
        InternalNode fNodeToSw2 = new InternalNode(VoltageLevelGraph.this, feederNode.getId() + "fNode2");

        // Nodes will be added afterwards
        nodesToAdd.add(fNodeToBus);
        nodesToAdd.add(fNodeToSw1);
        nodesToAdd.add(fNodeToSw2);

        // Add edges right away
        addEdge(busNode, fNodeToBus);
        addEdge(fNodeToBus, fNodeToSw1);
        addEdge(fNodeToSw1, fNodeToSw2);
        addEdge(fNodeToSw2, feederNode);
    }

    private void insertTwoInternalNodes(Node busDisconnectorNode, Node feederNode, List<Node> nodesToAdd) {
        removeEdge(busDisconnectorNode, feederNode);

        // Create nodes
        InternalNode fNodeToSw1 = new InternalNode(VoltageLevelGraph.this, feederNode.getId() + "fNode1");
        InternalNode fNodeToSw2 = new InternalNode(VoltageLevelGraph.this, feederNode.getId() + "fNode2");

        // Nodes will be added afterwards
        nodesToAdd.add(fNodeToSw1);
        nodesToAdd.add(fNodeToSw2);

        // Add edges right away
        addEdge(busDisconnectorNode, fNodeToSw1);
        addEdge(fNodeToSw1, fNodeToSw2);
        addEdge(fNodeToSw2, feederNode);
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
                                    InternalNode newNode = new InternalNode(VoltageLevelGraph.this, nodeSwitch.getId() + "Fictif");
                                    addNode(newNode);
                                    addEdge(node, newNode);
                                    addEdge(nodeSwitch, newNode);
                                }));
    }

    public void conditionalExtensionOfNodeConnectedToBus(Predicate<Node> condition) {
        getNodeBuses().forEach(nodeBus -> nodeBus.getAdjacentNodes().stream()
                .filter(condition)
                .forEach(nodeSwitch -> addDoubleNode(nodeBus, nodeSwitch, "")));
    }

    public void extendBusConnectedToBus() {
        for (BusNode n1 : getNodeBuses()) {
            n1.getAdjacentNodes().stream()
                    .filter(n2 -> n2.getType() == Node.NodeType.BUS)
                    .forEach(n2 -> {
                        removeEdge(n1, n2);
                        SwitchNode fSwToBus1 = SwitchNode.createFictitious(this, n1.getId() + "fSwitch1", false);
                        InternalNode internalNode = new InternalNode(this, "internal" + n1.getId() + n2.getId());
                        SwitchNode fSwToBus2 = SwitchNode.createFictitious(this, n2.getId() + "fSwitch2", false);
                        addEdge(n1, fSwToBus1);
                        addEdge(internalNode, fSwToBus1);
                        addEdge(internalNode, fSwToBus2);
                        addEdge(n2, fSwToBus2);
                    });
        }
    }

    public void extendSwitchBetweenBus(SwitchNode nodeSwitch) {
        List<Node> copyAdj = new ArrayList<>(nodeSwitch.getAdjacentNodes());
        addDoubleNode((BusNode) copyAdj.get(0), nodeSwitch, "0");
        addDoubleNode((BusNode) copyAdj.get(1), nodeSwitch, "1");
    }

    public InternalNode insertInternalNode(Node node1, Node node2, String id) {
        removeEdge(node1, node2);
        InternalNode iNode = new InternalNode(this, id);
        addNode(iNode);
        addEdge(node1, iNode);
        addEdge(node2, iNode);
        return iNode;
    }

    private void addDoubleNode(BusNode busNode, Node node, String suffix) {
        removeEdge(busNode, node);
        SwitchNode fNodeToBus = SwitchNode.createFictitious(VoltageLevelGraph.this, node.getId() + "fSwitch" + suffix, node.isOpen());
        addNode(fNodeToBus);
        InternalNode fNodeToSw = new InternalNode(VoltageLevelGraph.this, node.getId() + "fNode" + suffix);
        addNode(fNodeToSw);
        addEdge(busNode, fNodeToBus);
        addEdge(fNodeToBus, fNodeToSw);
        addEdge(fNodeToSw, node);
    }

    /**
     * Substitute a node with another node already in the graph.
     * Use {@link #replaceNode} instead if the node newNode is not already in the graph.
     *
     * @param nodeOrigin: node which will be substituted
     * @param newNode:    node which will substitute the first one
     */
    public void substituteNode(Node nodeOrigin, Node newNode) {
        while (!nodeOrigin.getAdjacentEdges().isEmpty()) {
            Edge edge = nodeOrigin.getAdjacentEdges().get(0);
            Node node1 = edge.getNode1() == nodeOrigin ? newNode : edge.getNode1();
            Node node2 = edge.getNode2() == nodeOrigin ? newNode : edge.getNode2();
            addEdge(node1, node2);
            removeEdge(edge);
        }
        removeNode(nodeOrigin);
    }

    /**
     * Replace a node with another node which is not yet in the graph.
     * Use {@link #substituteNode} instead if the node newNode is already in the graph.
     *
     * @param nodeOrigin: node which will be replaced
     * @param newNode:    node which will replace the first one
     */
    public void replaceNode(Node nodeOrigin, Node newNode) {
        substituteNode(nodeOrigin, newNode);
        if (!nodes.contains(newNode)) {
            addNode(newNode);
        }
    }

    public void substituteFictitiousNodesMirroringBusNodes() {
        getNodeBuses().forEach(busNode -> {
            List<Node> adjs = busNode.getAdjacentNodes();
            if (adjs.size() == 1 && adjs.get(0).getType() == Node.NodeType.FICTITIOUS) {
                Node adj = adjs.get(0);
                removeEdge(adj, busNode);
                substituteNode(adj, busNode);
            }
        });
    }

    public void substituteSingularFictitiousByFeederNode() {
        getNodes().stream()
                .filter(n -> n.getType() == Node.NodeType.FICTITIOUS && n.getAdjacentEdges().size() == 1)
                .forEach(n -> replaceNode(n, FeederNode.createFictitious(this, n.getId(), Orientation.UP)));
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

    public VoltageLevelInfos getVoltageLevelInfos() {
        return voltageLevelInfos;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public boolean isPositionNodeBusesCalculated() {
        return getNodeBuses().stream().allMatch(n -> n.getPosition().get(H) != -1 && n.getPosition().get(V) != -1);
    }

    @Override
    public void writeJson(JsonGenerator generator) throws IOException {
        generator.writeStartObject();

        generator.writeFieldName("voltageLevelInfos");
        voltageLevelInfos.writeJsonContent(generator);

        if (isGenerateCoordsInJson()) {
            generator.writeNumberField("x", x);
            generator.writeNumberField("y", y);
        }

        generator.writeArrayFieldStart("nodes");
        for (Node node : nodes.stream().sorted(Comparator.comparing(Node::getId)).collect(Collectors.toList())) {
            node.writeJson(generator);
        }
        generator.writeEndArray();

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

        writeBranchFields(generator);

        generator.writeEndObject();
    }

    public void resetCoords() {
        nodes.stream().forEach(Node::resetCoords);
    }

    public int getMaxH() {
        return getNodeBuses().stream()
                .mapToInt(nodeBus -> nodeBus.getPosition().get(H) + nodeBus.getPosition().getSpan(H))
                .max().orElse(0);
    }

    public int getMaxV() {
        return getNodeBuses().stream()
                .mapToInt(nodeBus -> nodeBus.getPosition().get(V) + nodeBus.getPosition().getSpan(V))
                .max().orElse(0);
    }

    public Double getMaxCalculatedCellHeight(BusCell.Direction direction) {
        return !maxCalculatedCellHeight.isEmpty() ? maxCalculatedCellHeight.get(direction) : -1.;
    }

    public void setMaxCalculatedCellHeight(Map<BusCell.Direction, Double> maxCalculatedCellHeight) {
        this.maxCalculatedCellHeight = maxCalculatedCellHeight;
    }

    public int getMaxHorizontalBusPosition() {
        return maxHorizontalBusPosition;
    }

    public void setMaxHorizontalBusPosition(int maxHorizontalBusPosition) {
        this.maxHorizontalBusPosition = maxHorizontalBusPosition;
    }

    public int getMaxVerticalBusPosition() {
        return maxVerticalBusPosition;
    }

    public void setMaxVerticalBusPosition(int maxVerticalBusPosition) {
        this.maxVerticalBusPosition = maxVerticalBusPosition;
    }
}
