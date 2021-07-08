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
import static org.junit.Assert.*;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class TestSerialParallelBlock extends AbstractTestCaseIidm {

    @Override
    protected LayoutParameters getLayoutParameters() {
        return createDefaultLayoutParameters();
    }

    @Before
    public void setUp() {
        Network network = Network.create("testCase1", "test");
        graphBuilder = new NetworkGraphBuilder(network);
        substation = createSubstation(network, "s", "s", Country.FR);
        vl = createVoltageLevel(substation, "vl", "vl", TopologyKind.NODE_BREAKER, 380, 10);
        createBusBarSection(vl, "bbs", "bbs", 0, 1, 1);
        createSwitch(vl, "da", "da", SwitchKind.DISCONNECTOR, false, false, false, 0, 1);
        createSwitch(vl, "ba", "ba", SwitchKind.BREAKER, false, false, false, 1, 2);
        createSwitch(vl, "ba1", "ba1", SwitchKind.BREAKER, false, false, false, 2, 4);
        createSwitch(vl, "ba2", "ba2", SwitchKind.BREAKER, false, false, false, 2, 5);
        createLoad(vl, "la1", "la1", "la1", 10, ConnectablePosition.Direction.TOP, 4, 10, 10);
        createLoad(vl, "la2", "la2", "la2", 10, ConnectablePosition.Direction.TOP, 5, 10, 10);
    }

    @Test
    public void test() {
        // build graph
        VoltageLevelGraph g = graphBuilder.buildVoltageLevelGraph(vl.getId(), false, true);

        // detect cells
        new ImplicitCellDetector().detectCells(g);
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
        assertTrue(sb.getSubBlocks().get(1).isEmbeddingNodeType(Node.NodeType.SWITCH));

        assertSame(Block.Type.LEGPRIMARY, sb.getLowerBlock().getType());
        LegPrimaryBlock subSB = (LegPrimaryBlock) sb.getLowerBlock();
        assertSame(Block.Type.BODYPARALLEL, sb.getUpperBlock().getType());
        BodyParallelBlock subPB = (BodyParallelBlock) sb.getUpperBlock();

        assertEquals("bbs", sb.getStartingNode().getId());
        assertEquals("bbs", subSB.getStartingNode().getId());
        assertEquals("FICT_vl_2", subPB.getSubBlocks().get(0).getStartingNode().getId());

        sb.sizing();
        assertEquals(0, sb.getPosition().get(H));
        assertEquals(0, sb.getPosition().get(V));
        assertEquals(4, sb.getPosition().getSpan(H));
        assertEquals(8, sb.getPosition().getSpan(V));

        assertEquals(0, subSB.getPosition().get(H));
        assertEquals(0, subSB.getPosition().get(V));
        assertEquals(2, subSB.getPosition().getSpan(H));
        assertEquals(0, subSB.getPosition().getSpan(V));

        assertEquals(0, subPB.getSubBlocks().get(0).getPosition().get(H));
        assertEquals(0, subPB.getSubBlocks().get(0).getPosition().get(V));
        assertEquals(2, subPB.getSubBlocks().get(0).getPosition().getSpan(H));
        assertEquals(4, subPB.getSubBlocks().get(0).getPosition().getSpan(V));

        assertEquals(2, subPB.getSubBlocks().get(1).getPosition().get(H));
        assertEquals(0, subPB.getSubBlocks().get(1).getPosition().get(V));
        assertEquals(2, subPB.getSubBlocks().get(1).getPosition().getSpan(H));
        assertEquals(4, subPB.getSubBlocks().get(1).getPosition().getSpan(V));

        sb.getCoord().set(X, 10);
        sb.getCoord().set(Y, 20);
        sb.getCoord().setSpan(X, 100);
        sb.getCoord().setSpan(Y, 200);
        sb.coordHorizontalCase(getLayoutParameters());

        assertEquals(10, sb.getCoord().get(X), 0);
        assertEquals(20, sb.getCoord().get(Y), 0);
        assertEquals(100, sb.getCoord().getSpan(X), 0);
        assertEquals(200, sb.getCoord().getSpan(Y), 0);

        assertEquals(35, subSB.getCoord().get(X), 0);
        assertEquals(20, subSB.getCoord().get(Y), 0);
        assertEquals(50, subSB.getCoord().getSpan(X), 0);
        assertEquals(200, subSB.getCoord().getSpan(Y), 0);

        assertEquals(-15, subPB.getSubBlocks().get(0).getCoord().get(X), 0);
        assertEquals(20, subPB.getSubBlocks().get(0).getCoord().get(Y), 0);
        assertEquals(50, subPB.getSubBlocks().get(0).getCoord().getSpan(X), 0);
        assertEquals(200, subPB.getSubBlocks().get(0).getCoord().getSpan(Y), 0);

        assertEquals(35, subPB.getSubBlocks().get(1).getCoord().get(X), 0);
        assertEquals(20, subPB.getSubBlocks().get(1).getCoord().get(Y), 0);
        assertEquals(50, subPB.getSubBlocks().get(1).getCoord().getSpan(X), 0);
        assertEquals(200, subPB.getSubBlocks().get(1).getCoord().getSpan(Y), 0);

        sb.reverseBlock();

        assertEquals("FICT_vl_daFictif", sb.getEndingNode().getId());
        assertEquals("FICT_vl_daFictif", subSB.getEndingNode().getId());
        assertEquals("FICT_vl_2", subPB.getSubBlocks().get(1).getEndingNode().getId());
    }
}
