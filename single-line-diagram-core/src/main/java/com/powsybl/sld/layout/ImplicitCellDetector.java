/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout;

import com.powsybl.sld.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ImplicitCellDetector implements CellDetector {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImplicitCellDetector.class);
    private boolean removeUnnecessaryFictitiousNodes;
    private boolean substituteSingularFictitiousByFeederNode;
    private boolean exceptionIfPatternNotHandled;

    public ImplicitCellDetector(boolean removeUnnecessaryFictitiousNodes, boolean substituteSingularFictitiousByFeederNode, boolean exceptionIfPatternNotHandled) {
        this.removeUnnecessaryFictitiousNodes = removeUnnecessaryFictitiousNodes;
        this.substituteSingularFictitiousByFeederNode = substituteSingularFictitiousByFeederNode;
        this.exceptionIfPatternNotHandled = exceptionIfPatternNotHandled;
    }

    public ImplicitCellDetector() {
        this(true, true, false);
    }


    /**
     * internCell detection : an internal cell is composed of nodes connecting BUSes without connecting Feeder.
     * detectCell is used to detect cells exploring the graph and scanning exclusionTypes and stopTypes
     * <p>
     * *************INTERN CELL*******************
     * exclusion types = {FEEDER} : if a FEEDER type is reached it is not an INTERN CELL
     * stop the visit if reach a Bus : stopTypes = {BUS}
     * ***************EXTERN AND SHUNT CELLS******
     * detection : nodes connecting buses and departures without being in an internCell (previously allocated nodes)
     * exclusion types = {}
     * stop the visit if reach a Bus : stopTypes = {BUS,FEEDER} * @param graph g
     */
    @Override
    public void detectCells(VoltageLevelGraph graph) {
        cleaning(graph);

        LOGGER.info("Detecting cells...");

        List<Node> allocatedNodes = new ArrayList<>();
        // **************INTERN CELL*******************
        List<Node.NodeType> exclusionTypes = new ArrayList<>();
        exclusionTypes.add(Node.NodeType.FEEDER);
        List<Node.NodeType> stopTypes = new ArrayList<>();
        stopTypes.add(Node.NodeType.BUS);
        detectCell(graph, stopTypes, exclusionTypes, true, allocatedNodes);

        // ****************EXTERN AND SHUNT CELLS******
        stopTypes.add(Node.NodeType.FEEDER);
        detectCell(graph, stopTypes, new ArrayList<>(), false, allocatedNodes);
        for (ExternCell cell : graph.getCells().stream()

                .filter(cell -> cell instanceof ExternCell)
                .map(ExternCell.class::cast)
                .collect(Collectors.toList())) {

            //*****************EXTERN CELL
            if (!isPureExternCell(graph, cell)) {
                //*****************SHUNT CELL
                //in that case the cell is splitted into 2 EXTERN Cells and 1 SHUNT CELL
                detectAndTypeShunt(graph, cell);
            }
        }
        graph.getCells().forEach(Cell::getFullId);

        graph.logCellDetectionStatus();
    }

    private void cleaning(VoltageLevelGraph graph) {
        graph.substituteFictitiousNodesMirroringBusNodes();
        if (removeUnnecessaryFictitiousNodes) {
            graph.removeUnnecessaryFictitiousNodes();
        }
        if (substituteSingularFictitiousByFeederNode) {
            graph.substituteSingularFictitiousByFeederNode();
        }
        graph.insertFictitiousNodesAtFeeders();
        graph.extendFirstOutsideNode();
        graph.conditionalExtensionOfNodeConnectedToBus(node ->
                node.getType() == Node.NodeType.SWITCH && ((SwitchNode) node).getKind() != SwitchNode.SwitchKind.DISCONNECTOR
                        || node instanceof Middle2WTNode || node instanceof Middle3WTNode
        );
        graph.extendBusConnectedToBus();
    }

    /**
     * @param typeStops      is the types of node that stops the exploration
     * @param exclusionTypes is the types when reached considers the exploration unsuccessful
     * @param isCellIntern   when the exploration is for the identification of internCell enables to instantiate InternCell class instead of Cell
     * @param allocatedNodes is the list of nodes already allocated to a cell.
     **/
    private void detectCell(VoltageLevelGraph graph,
                            List<Node.NodeType> typeStops,
                            List<Node.NodeType> exclusionTypes,
                            boolean isCellIntern,
                            List<Node> allocatedNodes) {
        graph.getNodeBuses().forEach(bus -> bus.getAdjacentNodes().forEach(adj -> {
            List<Node> cellNodes = new ArrayList<>();
            List<Node> outsideNodes = new ArrayList<>(allocatedNodes);
            outsideNodes.add(bus);
            if (GraphTraversal.run(
                    adj, node -> typeStops.contains(node.getType()), node -> exclusionTypes.contains(node.getType()),
                    cellNodes, outsideNodes)) {
                cellNodes.add(0, bus);
                Cell cell = isCellIntern ? new InternCell(graph, exceptionIfPatternNotHandled) : new ExternCell(graph);
                cell.setNodes(cellNodes);
                allocatedNodes.addAll(cellNodes.stream()
                        .filter(node -> node.getType() != Node.NodeType.BUS)
                        .collect(Collectors.toList()));
            }
        }));
    }

    /**
     * Check if the cell is a pure extern and return true in that case, else false (suspected shunt)
     *
     * @param cell : the cell to analyse
     **/
    private boolean isPureExternCell(VoltageLevelGraph graph, ExternCell cell) {
        /*Explore the graph of the candidate cell. Remove successively one node, assess if it splits the graph into n>1 branches
        if so, then check if each component is exclusively reaching FEEDER or exclusively reaching BUS
        And verify you have at least one of them
        Return true in that case else false meaning there is one shunt
        */
        for (Node n : cell.getNodes()) {
            List<Node> nodes = new ArrayList<>(cell.getNodes());
            nodes.remove(n);
            List<List<Node>> connexComponents = graph.getConnexComponents(nodes);
            if (checkExternComponents(connexComponents)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param connexComponents components partition to analyse
     * @return true if this partition reflects an extern cell
     */
    private boolean checkExternComponents(List<List<Node>> connexComponents) {
        if (connexComponents.size() > 1) {
            boolean hasDepartBranch = false;
            boolean hasBusBranch = false;
            boolean hasMixBranch = false;
            for (List<Node> nodesConnex : connexComponents) {
                List<Node.NodeType> types = nodesConnex.stream()
                        .map(Node::getType)
                        .distinct().filter(t -> t == Node.NodeType.FEEDER || t == Node.NodeType.BUS)
                        .collect(Collectors.toList());
                if (types.size() == 2) {
                    hasMixBranch = true;
                } else if (types.isEmpty()) {
                    return false;
                } else if (types.get(0).equals(Node.NodeType.FEEDER)) {
                    hasDepartBranch = true;
                } else {
                    hasBusBranch = true;
                }
            }
            return hasBusBranch && hasDepartBranch && !hasMixBranch;
        }
        return false;
    }

    /**
     * @param cell the nodes of a cell that is suppected to be a shunt
     **/
    private void detectAndTypeShunt(VoltageLevelGraph graph, Cell cell) {

        List<Node> externalNodes = graph.getNodes()
                .stream()
                .filter(node -> !cell.getNodes().contains(node))
                .collect(Collectors.toList());

        for (Node n : cell.getNodes().stream()
                .filter(n -> n.getAdjacentNodes().size() > 2).collect(Collectors.toList())) {
            // optimisation : a Shunt node has necessarily 3 ore more adjacent nodes

            List<Node> cellNodesExtern1 = checkCandidateShuntNode(n, externalNodes);
            if (cellNodesExtern1 != null) {
                // create the 1st new external cell
                cell.removeAllNodes(cellNodesExtern1.stream()
                        .filter(m -> !m.getType().equals(Node.NodeType.BUS))
                        .collect(Collectors.toList()));
                n.setType(Node.NodeType.SHUNT);
                ExternCell newExternCell = new ExternCell(graph);
                cellNodesExtern1.add(n);
                newExternCell.setNodes(cellNodesExtern1);

                //create the shunt cell

                List<Node> shuntNodes = createShuntCellNodes(n, newExternCell);

                // create the 2nd external cell
                List<Node> cellNodesExtern2 = cell.getNodes().stream()
                        .filter(node -> (!cellNodesExtern1.contains(node) || node.getType() == Node.NodeType.BUS)
                                && (!shuntNodes.contains(node) || node.getType() == Node.NodeType.SHUNT))
                        .collect(Collectors.toList());

                cellNodesExtern2.removeAll(cellNodesExtern2.stream()
                        .filter(node -> node.getType() == Node.NodeType.BUS
                                && node.getAdjacentNodes().stream().noneMatch(
                                cellNodesExtern2::contains))
                        .collect(Collectors.toList()));

                ExternCell newExternCell2 = new ExternCell(graph);
                newExternCell2.setNodes(cellNodesExtern2);

                ShuntCell.create(newExternCell, newExternCell2, shuntNodes);

                graph.removeCell(cell);
                break;
            }
        }
    }

    private List<Node> checkCandidateShuntNode(Node n, List<Node> externalNodes) {
        List<Node.NodeType> kindToFilter = Arrays.asList(Node.NodeType.BUS,
                Node.NodeType.FEEDER,
                Node.NodeType.SHUNT);
        /*
        the node n is candidate to be a SHUNT node if there is
        (i) at least one branch exclusively reaching BUSes
        (ii) at least one branch exclusively reaching DEPARTs
        (iii) at least one branch reaching BUSes and DEPARTs (this branch would be a Shunt)
        In that case, the BUSes branches and DEPARTs Branches constitute an EXTERN Cell,
        and returned in the cellNodesExtern
         */

        List<Node> visitedNodes = new ArrayList<>(externalNodes);
        visitedNodes.add(n); //removal of the node to explore branches from it

        List<Node> cellNodesExtern = new ArrayList<>();
        boolean hasFeederBranch = false;
        boolean hasBusBranch = false;
        boolean hasMixBranch = false;

        List<Node> adjList = new ArrayList<>(n.getAdjacentNodes());
        adjList.removeAll(visitedNodes);
        for (Node adj : adjList) {
            if (!visitedNodes.contains(adj)) {
                List<Node> resultNodes = GraphTraversal.run(adj, node -> kindToFilter.contains(node.getType()), visitedNodes);

                List<Node.NodeType> types = resultNodes.stream() // what are the types of terminal node of the branch
                        .map(Node::getType)
                        .distinct().filter(kindToFilter::contains)
                        .collect(Collectors.toList());

                if (types.size() > 1) {
                    hasMixBranch = true;
                } else if (types.size() == 1) {
                    hasBusBranch |= types.get(0).equals(Node.NodeType.BUS);
                    hasFeederBranch |= types.get(0).equals(Node.NodeType.FEEDER);

                    if (types.get(0).equals(Node.NodeType.BUS) || types.get(0).equals(Node.NodeType.FEEDER)) {
                        cellNodesExtern.addAll(resultNodes);
                    }
                }
                visitedNodes.addAll(resultNodes.stream()
                        .filter(m -> m.getType() != Node.NodeType.BUS)
                        .collect(Collectors.toList()));

            }
        }
        return (hasBusBranch && hasFeederBranch && hasMixBranch) ? cellNodesExtern : null;
    }

    private List<Node> createShuntCellNodes(Node n, ExternCell cellExtern1) {
        List<Node> shuntCellNodes = new ArrayList<>();
        shuntCellNodes.add(n);
        Node currentNode = n.getAdjacentNodes().stream()
                .filter(node -> !cellExtern1.getNodes().contains(node))
                .findAny().orElse(null);
        if (currentNode != null) {
            while (currentNode.getAdjacentNodes().size() == 2) {
                shuntCellNodes.add(currentNode);
                currentNode = shuntCellNodes.contains(currentNode.getAdjacentNodes().get(0))
                        ? currentNode.getAdjacentNodes().get(1) : currentNode.getAdjacentNodes().get(0);
            }
            shuntCellNodes.add(currentNode);
            currentNode.setType(Node.NodeType.SHUNT);
        }
        return shuntCellNodes;
    }
}

