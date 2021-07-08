/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout;

import com.powsybl.commons.PowsyblException;
import com.powsybl.sld.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Contain function to dispose components of cells based on Hierarchical Layout
 *
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
final class CellBlockDecomposer {

    private static final Logger LOGGER = LoggerFactory.getLogger(CellBlockDecomposer.class);

    private CellBlockDecomposer() {
    }

    /**
     * Search BlockPrimary and build Block hierarchy by merging blocks together; also
     * list blocks connected to busbar
     *
     * @param cell Cell we are working on
     */

    static void determineBlocks(Cell cell, boolean exceptionIfPatternNotHandled) {
        if (cell.getType() == Cell.CellType.SHUNT) {
            determineShuntCellBlocks((ShuntCell) cell);
        } else {
            determineBusCellBlocks((BusCell) cell, exceptionIfPatternNotHandled);
        }
    }

    private static void determineBusCellBlocks(BusCell busCell, boolean exceptionIfPatternNotHandled) {
        if (busCell.getType() == Cell.CellType.INTERN && busCell.getNodes().size() == 3) {
            SwitchNode switchNode = (SwitchNode) busCell.getNodes().get(1);
            busCell.getGraph().extendSwitchBetweenBus(switchNode);
            List<Node> adj = switchNode.getAdjacentNodes();
            busCell.addNodes(adj);
            busCell.addNodes(adj.stream()
                    .flatMap(node -> node.getAdjacentNodes().stream())
                    .filter(node -> node != switchNode)
                    .collect(Collectors.toList()));
        }
        determineComplexCell(busCell, exceptionIfPatternNotHandled);
    }

    private static void determineShuntCellBlocks(ShuntCell shuntCell) {
        BodyPrimaryBlock bpy = new BodyPrimaryBlock(shuntCell.getNodes(), shuntCell);
        shuntCell.setRootBlock(bpy);
    }

    private static void determineComplexCell(BusCell busCell, boolean exceptionIfPatternNotHandled) {
        List<Block> blocks = createPrimaryBlock(busCell);
        mergeBlocks(busCell, blocks, exceptionIfPatternNotHandled);
    }

    private static List<Block> createPrimaryBlock(BusCell busCell) {
        Set<Node> alreadyTreated = new HashSet<>();
        List<Block> blocks = new ArrayList<>();
        Node currentNode = busCell.getBusNodes().get(0);

        // Search all primary blocks
        rElaboratePrimaryBlocks(busCell, currentNode, alreadyTreated, blocks);

        return blocks;
    }

    private static void mergeBlocks(BusCell busCell, List<Block> blocks, boolean exceptionIfPatternNotHandled) {
        // Search all blocks connected to a busbar inside the primary blocks list
        List<LegPrimaryBlock> primaryLegBlocks = blocks.stream()
                .filter(b -> b instanceof LegPrimaryBlock)
                .map(LegPrimaryBlock.class::cast)
                .collect(Collectors.toList());

        // Merge blocks to obtain a hierarchy of blocks
        while (blocks.size() != 1) {
            boolean merged = searchParallelMerge(blocks, busCell);
            merged |= searchSerialMerge(blocks, busCell);
            if (!merged) {
                if (exceptionIfPatternNotHandled) {
                    throw new PowsyblException("Blocks detection impossible for cell " + busCell);
                } else {
                    LOGGER.error("{} busCell, cannot merge any additional blocks, {} blocks remains", busCell.getType(), blocks.size());
                    Block undefinedBlock = new UndefinedBlock(new ArrayList<>(blocks), busCell);
                    blocks.clear();
                    blocks.add(undefinedBlock);
                    break;
                }
            }
        }
        busCell.blocksSetting(blocks.get(0), primaryLegBlocks);
    }

    /**
     * Search possibility to merge two blocks into a chain layout.block and do the merging
     *
     * @param blocks list of blocks we can merge
     * @param cell   current cell
     */
    private static boolean searchSerialMerge(List<Block> blocks, Cell cell) {
        int i = 0;
        boolean identifiedMerge = false;

        while (i < blocks.size() - 1) {
            List<Block> blockToRemove = new ArrayList<>();
            boolean chainIdentified = false;
            Block b1 = blocks.get(i);
            SerialBlock serialBlock = new SerialBlock(b1, cell);
            for (int j = i + 1; j < blocks.size(); j++) {
                Block b2 = blocks.get(j);
                if (serialBlock.addSubBlock(b2)) {
                    chainIdentified = true;
                    blockToRemove.add(b2);
                }
            }
            if (chainIdentified) {
                blockToRemove.add(b1);
                identifiedMerge = true;
                blocks.removeAll(blockToRemove);
                blocks.add(i, serialBlock);
                i = -1; // start again from the beginning
            }
            i++;
        }
        return identifiedMerge;
    }

    /**
     * Search possibility to merge some blocks into a parallel layout.block and do the merging
     *
     * @param blocks list of blocks we can merge
     * @param cell   current cell
     */
    private static boolean searchParallelMerge(List<Block> blocks, Cell cell) {
        List<List<Block>> blocksBundlesToMerge = new ArrayList<>();
        Node commonNode;
        int i = 0;
        while (i < blocks.size()) {
            List<Block> blocksBundle = new ArrayList<>();
            for (int j = i + 1; j < blocks.size(); j++) {
                commonNode = checkParallelCriteria(blocks.get(i), blocks.get(j));
                if (commonNode != null) {
                    blocksBundle.add(blocks.get(j));
                }
            }
            if (blocksBundle.isEmpty()) {
                i++;
            } else {
                blocksBundle.add(blocks.get(i));
                blocks.removeAll(blocksBundle);
                blocksBundlesToMerge.add(blocksBundle);
            }
        }
        for (List<Block> blocksBundle : blocksBundlesToMerge) {
            Block parallelBlock;
            if (blocksBundle.stream().anyMatch(b -> !(b instanceof LegPrimaryBlock))) {
                parallelBlock = new BodyParallelBlock(blocksBundle, cell, true);
            } else {
                parallelBlock = new LegParralelBlock(blocksBundle, cell, true);
            }
            blocks.add(parallelBlock);
        }
        return !blocksBundlesToMerge.isEmpty();
    }

    /**
     * Compare two blocks to see if they are parallel
     *
     * @param block1 layout.block
     * @param block2 layout.block
     * @return true if the two blocks are similar : same start and end
     */
    private static Node checkParallelCriteria(Block block1, Block block2) {
        Node s1 = block1.getExtremityNode(Block.Extremity.START);
        Node e1 = block1.getExtremityNode(Block.Extremity.END);
        Node s2 = block2.getExtremityNode(Block.Extremity.START);
        Node e2 = block2.getExtremityNode(Block.Extremity.END);

        if ((s1.checkNodeSimilarity(s2) && e1.checkNodeSimilarity(e2))
                || (s1.checkNodeSimilarity(e2) && e1.checkNodeSimilarity(s2))) {
            if (s1.equals(s2) || s1.equals(e2)) {
                return s1;
            } else {
                return e1;
            }
        } else {
            return null;
        }
    }

    /**
     * Search for primary blocks. A {@link PrimaryBlock} is identified when the the following pattern is detected:
     * BUS|FICTITIOUS|FEEDER|SHUNT - n * SWITCH - BUS|FICTITIOUS|FEEDER|SHUNT (with n >= 0).
     * When there is one BUS, it is instantiated as LegPrimaryBlock with this pattern (only allowed pattern with a bus):
     * BUS - SWITCH - FICTITIOUS.
     * When there is one FEEDER, it is instantiated as FeederPrimaryBlock with this pattern (only allowed pattern with a
     * feeder): FICTITIOUS - FEEDER.
     * Otherwise it is instantiated as BodyPrimaryBlock.
     * @param busCell         the busCell on which we elaborate primary blocks
     * @param firstNode       first node for the primary block (non-switch node)
     * @param alreadyTreated  set of already treated nodes (we always check the second element of the primary pattern)
     * @param blocks          the list of elaborated primary blocks
     */
    private static void rElaboratePrimaryBlocks(BusCell busCell, Node firstNode,
                                                Set<Node> alreadyTreated, List<Block> blocks) {

        firstNode.getListNodeAdjInCell(busCell).forEach(node2 -> {

            if (!alreadyTreated.contains(node2)) {

                // Piling up switches starting from node2
                List<SwitchNode> switches = new ArrayList<>();
                Node currentNode = node2;
                Node parentCurrentNode = firstNode;
                while (currentNode instanceof SwitchNode) {
                    switches.add((SwitchNode) currentNode);
                    List<Node> adjacentNodes = currentNode.getAdjacentNodes();
                    Node nextNode = adjacentNodes.get(adjacentNodes.get(0).equals(parentCurrentNode) ? 1 : 0);
                    parentCurrentNode = currentNode;
                    currentNode = nextNode;
                }
                Node lastNode = currentNode; // first non-switch node (corresponds to node2 if node2 is not a switch)

                // The nodes pattern for a PrimaryBlock
                List<Node> primaryPattern = new ArrayList<>();
                primaryPattern.add(firstNode);   // BUS|FICTITIOUS|FEEDER|SHUNT
                primaryPattern.addAll(switches); // n * SWITCH (with n >= 0)
                primaryPattern.add(lastNode);    // BUS|FICTITIOUS|FEEDER|SHUNT

                // Create a PrimaryBlock from that pattern
                PrimaryBlock primaryBlock = AbstractPrimaryBlock.createPrimaryBlock(primaryPattern, busCell);
                blocks.add(primaryBlock);

                // Update already treated nodes
                // We also consider firstNode as alreadyTreated: we do not want to come back to it as the search started from it
                alreadyTreated.addAll(primaryPattern);

                // Continue to search for other blocks
                if (lastNode.getType() != Node.NodeType.BUS) {
                    // If we reach a busbar, we know we do not need to go further:
                    // we're either in another BusCell or back to the busbar we started the search with
                    rElaboratePrimaryBlocks(busCell, lastNode, alreadyTreated, blocks);
                }
            }
        });
    }
}
