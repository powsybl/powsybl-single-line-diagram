/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout;

import com.powsybl.sld.model.VoltageLevelGraph;
import com.powsybl.sld.model.Node;
import com.powsybl.sld.model.SwitchNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Visit the graph to identifies the connected sets of nodes
 *
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 */
public final class TopologyCalculation {

    private TopologyCalculation() {
    }

    /**
     * Analyses the graph to partition it into TopologicallyConnectedNodeSets.
     * @param graph the graph to be analysed
     * @return a list of TopologicallyConnectedNodeSets
     */
    public static List<TopologicallyConnectedNodesSet> run(VoltageLevelGraph graph) {
        List<TopologicallyConnectedNodesSet> topologicallyConnectedNodesSets = new ArrayList<>();
        List<Node> nodesToVisit = graph.getNodes();
        List<Node> visitedNodes = new ArrayList<>();
        Node node = identifyNonOpenNode(nodesToVisit);
        while (node != null) {
            List<SwitchNode> openSwitches = new ArrayList<>();
            List<Node> connectedNodes = GraphTraversal
                    .run(node, n -> extremityCriteria(n, openSwitches), visitedNodes);
            Set<SwitchNode> borderSwitchNodes = openSwitches.stream()
                    .filter(n -> isBorderSwitchNode(n, connectedNodes))
                    .collect(Collectors.toSet());
            topologicallyConnectedNodesSets.add(new TopologicallyConnectedNodesSet(connectedNodes, borderSwitchNodes));
            connectedNodes.removeAll(borderSwitchNodes);
            visitedNodes.addAll(connectedNodes); //a border switch is part of 2 connectedNodesSets
            nodesToVisit.removeAll(connectedNodes);
            node = identifyNonOpenNode(nodesToVisit);
        }
        return topologicallyConnectedNodesSets;
    }

    private static boolean extremityCriteria(Node node, List<SwitchNode> openSwitches) {
        if (isOpenSwitchNode(node)) {
            openSwitches.add((SwitchNode) node);
            return true;
        } else {
            return false;
        }
    }

    private static Node identifyNonOpenNode(List<Node> remainingNodes) {
        return remainingNodes.stream().filter(n -> !isOpenSwitchNode(n)).findFirst().orElse(null);
    }

    private static boolean isBorderSwitchNode(SwitchNode switchNode, List<Node> connectedNodes) {
        return !connectedNodes.containsAll(switchNode.getAdjacentNodes());
    }

    private static boolean isOpenSwitchNode(Node node) {
        return node.getType() == Node.NodeType.SWITCH && node.isOpen();
    }
}
