/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.util;

import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.sld.model.*;
import com.powsybl.sld.model.Node.NodeType;
import com.powsybl.sld.styles.BaseVoltageStyle;
import com.powsybl.sld.svg.DiagramStyles;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author Giovanni Ferrari <giovanni.ferrari at techrain.eu>
 * @author Franck Lecuyer <franck.lecuyer@rte-france.com>
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public class TopologicalStyleProvider extends AbstractBaseVoltageDiagramStyleProvider {

    private final Map<String, Map<String, String>> voltageLevelStyleMap = new HashMap<>();

    public TopologicalStyleProvider(Network network) {
        this(BaseVoltageStyle.fromPlatformConfig(), network);
    }

    public TopologicalStyleProvider(BaseVoltageStyle baseVoltageStyle, Network network) {
        super(baseVoltageStyle, network);
    }

    @Override
    protected Optional<String> getEdgeStyle(Edge edge) {
        Node node1 = edge.getNode1();
        Node node2 = edge.getNode2();
        if (node1.getType() == NodeType.SWITCH && node1.isOpen()) {
            return node2.getVoltageLevelInfos() != null ? getVoltageLevelNodeStyle(node2.getVoltageLevelInfos(), node2) : Optional.empty();
        }
        if (node2.getType() == NodeType.SWITCH && node2.isOpen()) {
            return node1.getVoltageLevelInfos() != null ? getVoltageLevelNodeStyle(node1.getVoltageLevelInfos(), node1) : Optional.empty();
        }
        return super.getEdgeStyle(edge);
    }

    @Override
    public void reset() {
        voltageLevelStyleMap.clear();
    }

    private Map<String, String> createStyleMap(String baseVoltageLevelStyle, VoltageLevelInfos voltageLevelInfos) {
        VoltageLevel vl = network.getVoltageLevel(voltageLevelInfos.getId());
        List<Bus> buses = vl.getBusView().getBusStream().collect(Collectors.toList());

        Map<String, String> styleMap = new HashMap<>();
        AtomicInteger idxStyle = new AtomicInteger(0);
        buses.forEach(b -> {
            String style = baseVoltageLevelStyle + '-' + idxStyle.getAndIncrement();
            b.visitConnectedEquipments(new TopologyVisitorId(equipmentId -> styleMap.put(equipmentId, style)));
        });

        return styleMap;
    }

    private String getStyleMapKey(Node node) {
        return node instanceof FeederWithSideNode ? node.getEquipmentId() + "_" + ((FeederWithSideNode) node).getSide().name() : node.getEquipmentId();
    }

    private Optional<String> getNodeTopologicalStyle(String baseVoltageLevelStyle, VoltageLevelInfos voltageLevelInfos, Node node) {
        Map<String, String> styleMap = getVoltageLevelStyleMap(baseVoltageLevelStyle, voltageLevelInfos);
        String nodeTopologicalStyle = styleMap.computeIfAbsent(
                getStyleMapKey(node), id -> findConnectedStyle(baseVoltageLevelStyle, voltageLevelInfos, node));
        return Optional.ofNullable(nodeTopologicalStyle);
    }

    private String findConnectedStyle(String baseVoltageLevelStyle, VoltageLevelInfos voltageLevelInfos, Node node) {
        Set<Node> connectedNodes = findConnectedNodes(node);
        for (Node connectedNode : connectedNodes) {
            String nodeTopologicalStyle = getVoltageLevelStyleMap(baseVoltageLevelStyle, voltageLevelInfos).get(connectedNode.getEquipmentId());
            if (nodeTopologicalStyle != null) {
                return nodeTopologicalStyle;
            }
        }
        return null;
    }

    private Map<String, String> getVoltageLevelStyleMap(String baseVoltageLevelStyle, VoltageLevelInfos voltageLevelInfos) {
        return voltageLevelStyleMap.computeIfAbsent(
            voltageLevelInfos.getId(), k -> createStyleMap(baseVoltageLevelStyle, voltageLevelInfos));
    }

    private Set<Node> findConnectedNodes(Node node) {
        Set<Node> visitedNodes = new HashSet<>();
        findConnectedNodes(node, visitedNodes);
        return visitedNodes;
    }

    private void findConnectedNodes(Node node, Set<Node> visitedNodes) {
        if (visitedNodes.contains(node)) {
            return;
        }
        if (node.getType() == NodeType.SWITCH && node.isOpen()) {
            return;
        }
        visitedNodes.add(node);
        for (Node adjNode : node.getAdjacentNodes()) {
            findConnectedNodes(adjNode, visitedNodes);
        }
    }

    @Override
    public Optional<String> getVoltageLevelNodeStyle(VoltageLevelInfos voltageLevelInfos, Node node) {
        if (node.getType() == NodeType.SWITCH && node.isOpen()) {
            return Optional.of(DiagramStyles.DISCONNECTED_STYLE_CLASS);
        }
        Optional<String> baseVoltageLevelStyle = super.getVoltageLevelNodeStyle(voltageLevelInfos, node);
        if (baseVoltageLevelStyle.isPresent()) {
            Optional<String> nodeTopologicalStyle = getNodeTopologicalStyle(baseVoltageLevelStyle.get(), voltageLevelInfos, node);
            if (nodeTopologicalStyle.isPresent()) {
                return nodeTopologicalStyle;
            }
        }
        return Optional.of(DiagramStyles.DISCONNECTED_STYLE_CLASS);
    }

    @Override
    public List<String> getCssFilenames() {
        return Arrays.asList("tautologies.css", "topologicalBaseVoltages.css", "highlightLineStates.css", "baseVoltageConstantColors.css");
    }

}
