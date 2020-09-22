/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.iidm;

import com.powsybl.iidm.network.*;
import com.powsybl.sld.NetworkGraphBuilder;
import com.powsybl.sld.iidm.extensions.ConnectablePosition;
import com.powsybl.sld.layout.BlockOrganizer;
import com.powsybl.sld.layout.ImplicitCellDetector;
import com.powsybl.sld.layout.LayoutParameters;
import com.powsybl.sld.model.*;
import org.junit.Before;
import org.junit.Test;

import static com.powsybl.sld.model.Coord.Dimension.*;
import static com.powsybl.sld.model.Position.Dimension.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class TestSerialBlock extends AbstractTestCaseIidm {

    @Before
    public void setUp() {
        Network network = Network.create("testCase1", "test");
        graphBuilder = new NetworkGraphBuilder(network);
        substation = createSubstation(network, "s", "s", Country.FR);
        vl = createVoltageLevel(substation, "vl", "vl", TopologyKind.NODE_BREAKER, 400, 10);
        createBusBarSection(vl, "bbs", "bbs", 0, 1, 1);
        createLoad(vl, "la", "la", "la", 10, ConnectablePosition.Direction.TOP, 2, 10, 10);
        createSwitch(vl, "ba", "ba", SwitchKind.BREAKER, false, false, false, 2, 1);
        createSwitch(vl, "da", "da", SwitchKind.DISCONNECTOR, false, false, false, 1, 0);
    }

    @Test
    public void test() {
        // build graph
        Graph g = graphBuilder.buildVoltageLevelGraph(vl.getId(), false, true);

        // detect cells
        new ImplicitCellDetector().detectCells(g);

        // build blocks
        new BlockOrganizer().organize(g);

        assertEquals(1, g.getCells().size());
        Cell cell = g.getCells().iterator().next();
        assertEquals(Block.Type.SERIAL, cell.getRootBlock().getType());
        SerialBlock sb = (SerialBlock) cell.getRootBlock();
        assertTrue(sb.isEmbeddingNodeType(Node.NodeType.BUS));
        assertTrue(sb.isEmbeddingNodeType(Node.NodeType.FEEDER));
        assertTrue(sb.getLowerBlock().isEmbeddingNodeType(Node.NodeType.BUS));
        assertTrue(sb.getUpperBlock().isEmbeddingNodeType(Node.NodeType.FEEDER));
        assertTrue(sb.getSubBlocks().get(0).isEmbeddingNodeType(Node.NodeType.BUS));
        assertTrue(sb.getSubBlocks().get(1).isEmbeddingNodeType(Node.NodeType.FEEDER));

        assertEquals("bbs", sb.getSubBlocks().get(0).getStartingNode().getId());
        assertEquals("FICT_vl_daFictif", sb.getSubBlocks().get(0).getEndingNode().getId());
        assertEquals("FICT_vl_daFictif", sb.getSubBlocks().get(1).getStartingNode().getId());
        assertEquals("la", sb.getSubBlocks().get(1).getEndingNode().getId());

        sb.sizing();
        assertEquals(0, sb.getPosition().get(H));
        assertEquals(0, sb.getPosition().get(V));
        assertEquals(2, sb.getPosition().getSpan(H));
        assertEquals(4, sb.getPosition().getSpan(V));

        assertEquals(0, sb.getLowerBlock().getPosition().get(H));
        assertEquals(0, sb.getLowerBlock().getPosition().get(V));
        assertEquals(2, sb.getLowerBlock().getPosition().getSpan(H));
        assertEquals(0, sb.getLowerBlock().getPosition().getSpan(V));

        assertEquals(0, sb.getUpperBlock().getPosition().get(H));
        assertEquals(0, sb.getUpperBlock().getPosition().get(V));
        assertEquals(2, sb.getUpperBlock().getPosition().getSpan(H));
        assertEquals(4, sb.getUpperBlock().getPosition().getSpan(V));

        LayoutParameters layoutParameters = new LayoutParameters()
                .setTranslateX(20)
                .setTranslateY(50)
                .setInitialXBus(0)
                .setInitialYBus(260)
                .setVerticalSpaceBus(25)
                .setHorizontalBusPadding(20)
                .setCellWidth(50)
                .setExternCellHeight(250)
                .setInternCellHeight(40)
                .setStackHeight(30)
                .setShowGrid(true)
                .setShowInternalNodes(true)
                .setScaleFactor(1)
                .setHorizontalSubstationPadding(50)
                .setVerticalSubstationPadding(50);

        sb.setX(10);
        sb.setY(20);
        sb.setXSpan(100);
        sb.setYSpan(200);
        sb.coordHorizontalCase(layoutParameters);

        assertEquals(10, sb.getLowerBlock().getCoord().get(X), 0);
        assertEquals(20, sb.getLowerBlock().getCoord().get(Y), 0);
        assertEquals(100, sb.getLowerBlock().getCoord().getSpan(X), 0);
        assertEquals(200, sb.getLowerBlock().getCoord().getSpan(Y), 0);

        assertEquals(10, sb.getUpperBlock().getCoord().get(X), 0);
        assertEquals(20, sb.getUpperBlock().getCoord().get(Y), 0);
        assertEquals(100, sb.getUpperBlock().getCoord().getSpan(X), 0);
        assertEquals(200, sb.getUpperBlock().getCoord().getSpan(Y), 0);

        sb.reverseBlock();

        assertEquals("FICT_vl_daFictif", sb.getSubBlocks().get(1).getEndingNode().getId());
        assertEquals("bbs", sb.getSubBlocks().get(1).getStartingNode().getId());
        assertEquals("FICT_vl_daFictif", sb.getSubBlocks().get(0).getEndingNode().getId());
        assertEquals("la", sb.getSubBlocks().get(0).getStartingNode().getId());
    }
}
