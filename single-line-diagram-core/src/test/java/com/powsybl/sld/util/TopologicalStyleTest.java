/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.util;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.iidm.network.*;
import com.powsybl.sld.NetworkGraphBuilder;
import com.powsybl.sld.iidm.AbstractTestCaseIidm;
import com.powsybl.sld.iidm.extensions.ConnectablePosition;
import com.powsybl.sld.layout.LayoutParameters;
import com.powsybl.sld.model.Edge;
import com.powsybl.sld.model.VoltageLevelGraph;
import com.powsybl.sld.model.Node;
import com.powsybl.sld.svg.DiagramStyles;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Giovanni Ferrari <giovanni.ferrari at techrain.eu>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class TopologicalStyleTest extends AbstractTestCaseIidm {

    VoltageLevel vl1;
    VoltageLevel vl2;
    VoltageLevel vl3;
    private FileSystem fileSystem;
    private Path tmpDir;

    @Override
    protected LayoutParameters getLayoutParameters() {
        return null;
    }

    @Before
    public void setUp() throws IOException {
        network = Network.create("testCase1", "test");
        graphBuilder = new NetworkGraphBuilder(network);
        substation = network.newSubstation().setId("s").setCountry(Country.FR).add();

        // first voltage level
        vl1 = createVoltageLevel(substation, "vl1", "vl1", TopologyKind.NODE_BREAKER, 380, 10);
        createBusBarSection(vl1, "bbs1", "bbs1", 0, 1, 1);
        createLoad(vl1, "l", "l", "l", 0, ConnectablePosition.Direction.TOP, 2, 10, 10);
        createSwitch(vl1, "d", "d", SwitchKind.DISCONNECTOR, false, false, false, 0, 1);
        createSwitch(vl1, "b", "b", SwitchKind.BREAKER, false, false, false, 1, 2);

        // second voltage level
        vl2 = createVoltageLevel(substation, "vl2", "vl2", TopologyKind.NODE_BREAKER, 225, 10);
        createBusBarSection(vl2, "bbs2", "bbs2", 0, 1, 1);

        // third voltage level
        vl3 = createVoltageLevel(substation, "vl3", "vl3", TopologyKind.NODE_BREAKER, 63, 10);
        createBusBarSection(vl3, "bbs3", "bbs3", 0, 1, 1);

        // 2WT between first and second voltage level
        createTwoWindingsTransformer(substation, "2WT", "2WT", 1, 1, 1, 1, 1, 1,
                3, 1, vl1.getId(), vl2.getId(),
                "2WT_1", 1, ConnectablePosition.Direction.TOP,
                "2WT_2", 0, ConnectablePosition.Direction.TOP);
        createSwitch(vl1, "d2WT_1", "d2WT_1", SwitchKind.DISCONNECTOR, false, false, true, 0, 4);
        createSwitch(vl1, "b2WT_1", "b2WT_1", SwitchKind.BREAKER, true, false, true, 3, 4);
        createSwitch(vl2, "d2WT_2", "d2WT_2", SwitchKind.DISCONNECTOR, false, false, true, 0, 2);
        createSwitch(vl2, "b2WT_2", "b2WT_2", SwitchKind.BREAKER, true, true, true, 1, 2);

        // 3WT between the 3 voltage levels
        createThreeWindingsTransformer(substation, "3WT", "3WT", vl1.getId(), vl2.getId(), vl3.getId(),
                1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                5, 3, 1,
                "3WT_1", 2, ConnectablePosition.Direction.TOP,
                "3WT_2", 1, ConnectablePosition.Direction.TOP,
                "3WT_3", 0, ConnectablePosition.Direction.TOP);
        createSwitch(vl1, "d3WT_1", "d3WT_1", SwitchKind.DISCONNECTOR, false, false, true, 0, 6);
        createSwitch(vl1, "b3WT_1", "b3WT_1", SwitchKind.BREAKER, true, false, true, 5, 6);
        createSwitch(vl2, "d3WT_2", "d3WT_2", SwitchKind.DISCONNECTOR, false, false, true, 0, 4);
        createSwitch(vl2, "b3WT_2", "b3WT_2", SwitchKind.BREAKER, true, true, true, 3, 4);
        createSwitch(vl3, "d3WT_3", "d3WT_3", SwitchKind.DISCONNECTOR, false, false, true, 0, 2);
        createSwitch(vl3, "b3WT_3", "b3WT_3", SwitchKind.BREAKER, true, false, true, 1, 2);

        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        tmpDir = Files.createDirectory(fileSystem.getPath("/tmp"));
    }

    @Test
    public void test() throws IOException {
        // construction des graphes
        VoltageLevelGraph graph1 = graphBuilder.buildVoltageLevelGraph(vl1.getId(), false, true);
        VoltageLevelGraph graph2 = graphBuilder.buildVoltageLevelGraph(vl2.getId(), false, true);
        VoltageLevelGraph graph3 = graphBuilder.buildVoltageLevelGraph(vl3.getId(), false, true);

        TopologicalStyleProvider styleProvider = new TopologicalStyleProvider(network);

        Node node1 = graph1.getNode("bbs1");
        List<String> nodeStyle1 = styleProvider.getSvgNodeStyles(node1, componentLibrary, true);
        assertEquals(3, nodeStyle1.size());
        assertTrue(nodeStyle1.contains("sld-busbar-section"));
        assertTrue(nodeStyle1.contains("sld-constant-color"));
        assertTrue(nodeStyle1.contains("sld-vl300to500-0"));

        Node node2 = graph2.getNode("bbs2");
        List<String> nodeStyle2 = styleProvider.getSvgNodeStyles(node2, componentLibrary, true);
        assertEquals(3, nodeStyle2.size());
        assertTrue(nodeStyle2.contains("sld-busbar-section"));
        assertTrue(nodeStyle2.contains("sld-constant-color"));
        assertTrue(nodeStyle2.contains(DiagramStyles.DISCONNECTED_STYLE_CLASS));

        Node node3 = graph3.getNode("bbs3");
        List<String> nodeStyle3 = styleProvider.getSvgNodeStyles(node3, componentLibrary, true);
        assertEquals(3, nodeStyle3.size());
        assertTrue(nodeStyle3.contains("sld-busbar-section"));
        assertTrue(nodeStyle3.contains("sld-constant-color"));
        assertTrue(nodeStyle3.contains("sld-vl50to70-0"));

        Edge edge = graph1.getEdges().get(12);

        List<String> wireStyles = styleProvider.getSvgWireStyles(edge, false);
        assertEquals(3, wireStyles.size());
        assertTrue(wireStyles.contains(DiagramStyles.WIRE_STYLE_CLASS));
        assertTrue(wireStyles.contains("sld-constant-color"));
        assertTrue(wireStyles.contains("sld-vl300to500-0"));

        Node fict3WTNode = graph1.getNode("FICT_vl1_3WT_fictif");
        List<String> node3WTStyle = styleProvider.getSvgNodeStyles(fict3WTNode, componentLibrary, true);
        assertEquals(2, node3WTStyle.size());
        assertTrue(node3WTStyle.contains("sld-constant-color"));
        assertTrue(node3WTStyle.contains("sld-three-wt"));

        Node f2WTNode = graph1.getNode("2WT_ONE");
        List<String> node2WTStyle = styleProvider.getSvgNodeStyles(f2WTNode, componentLibrary, true);
        assertEquals(2, node2WTStyle.size());
        assertTrue(node2WTStyle.contains("sld-constant-color"));
        assertTrue(node2WTStyle.contains("sld-two-wt"));

    }
}
