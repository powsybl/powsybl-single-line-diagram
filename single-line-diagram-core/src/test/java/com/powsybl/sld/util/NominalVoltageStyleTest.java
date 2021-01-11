/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.util;

import com.powsybl.iidm.network.*;
import com.powsybl.sld.NetworkGraphBuilder;
import com.powsybl.sld.iidm.AbstractTestCaseIidm;
import com.powsybl.sld.iidm.extensions.ConnectablePosition;
import com.powsybl.sld.layout.LayoutParameters;
import com.powsybl.sld.model.Edge;
import com.powsybl.sld.model.Graph;
import com.powsybl.sld.model.Node;
import com.powsybl.sld.svg.DefaultDiagramLabelProvider;
import com.powsybl.sld.svg.DiagramStyles;
import com.powsybl.sld.svg.InitialValue;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Giovanni Ferrari <giovanni.ferrari at techrain.eu>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class NominalVoltageStyleTest extends AbstractTestCaseIidm {

    VoltageLevel vl1;
    VoltageLevel vl2;
    VoltageLevel vl3;
    private LayoutParameters layoutParameters;
    private NominalVoltageDiagramStyleProvider styleProvider;

    @Override
    protected LayoutParameters getLayoutParameters() {
        return layoutParameters;
    }

    @Before
    public void setUp() {

        layoutParameters = createDefaultLayoutParameters()
            .setCellWidth(80)
            .setShowInternalNodes(false);

        network = Network.create("testCase1", "test");
        graphBuilder = new NetworkGraphBuilder(network);
        substation = createSubstation(network, "s", "s", Country.FR);

        styleProvider = new NominalVoltageDiagramStyleProvider(network);

        // first voltage level
        vl1 = createVoltageLevel(substation, "vl1", "vl1", TopologyKind.NODE_BREAKER, 400, 10);
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
        createSwitch(vl1, "b3WT_1", "b3WT_1", SwitchKind.BREAKER, true, true, true, 5, 6);
        createSwitch(vl2, "d3WT_2", "d3WT_2", SwitchKind.DISCONNECTOR, false, false, true, 0, 4);
        createSwitch(vl2, "b3WT_2", "b3WT_2", SwitchKind.BREAKER, true, false, true, 3, 4);
        createSwitch(vl3, "d3WT_3", "d3WT_3", SwitchKind.DISCONNECTOR, false, false, true, 0, 2);
        createSwitch(vl3, "b3WT_3", "b3WT_3", SwitchKind.BREAKER, true, true, true, 1, 2);
    }

    private class NoFeederValueProvider extends DefaultDiagramLabelProvider {
        public NoFeederValueProvider() {
            super(network, componentLibrary, getLayoutParameters());
        }

        @Override
        public InitialValue getInitialValue(Node node) {
            InitialValue initialValue;
            if (node.getType() == Node.NodeType.BUS) {
                initialValue = new InitialValue(null, null, null, null, null, null);
            } else {
                initialValue = new InitialValue(Direction.UP, Direction.DOWN, null, null, null, null);
            }
            return initialValue;
        }
    }

    @Test
    public void testAttributes() {
        // construction des graphes
        Graph graph1 = graphBuilder.buildVoltageLevelGraph(vl1.getId(), false, true);
        Graph graph2 = graphBuilder.buildVoltageLevelGraph(vl2.getId(), false, true);
        Graph graph3 = graphBuilder.buildVoltageLevelGraph(vl3.getId(), false, true);

        Node node1 = graph1.getNode("bbs1");
        List<String> nodeStyle1 = styleProvider.getSvgNodeStyles(node1, false);
        assertEquals(2, nodeStyle1.size());
        assertEquals(DiagramStyles.BUS_STYLE_CLASS, nodeStyle1.get(0));
        assertEquals("vl400", nodeStyle1.get(1));

        Node node2 = graph2.getNode("bbs2");
        List<String>  nodeStyle2 = styleProvider.getSvgNodeStyles(node2, false);
        assertEquals(2, nodeStyle2.size());
        assertEquals(DiagramStyles.BUS_STYLE_CLASS, nodeStyle2.get(0));
        assertEquals("vl225", nodeStyle2.get(1));

        Node node3 = graph3.getNode("bbs3");
        List<String>  nodeStyle3 = styleProvider.getSvgNodeStyles(node3, false);
        assertEquals(2, nodeStyle3.size());
        assertEquals(DiagramStyles.BUS_STYLE_CLASS, nodeStyle3.get(0));
        assertEquals("vl63", nodeStyle3.get(1));

        Edge edge = graph1.getEdges().get(12);
        List<String> wireStyles = styleProvider.getSvgWireStyles(edge, false);
        assertEquals(2, wireStyles.size());
        assertEquals(DiagramStyles.WIRE_STYLE_CLASS, wireStyles.get(0));
        assertEquals("vl400", wireStyles.get(1));

        Node fict3WTNode = graph1.getNode("FICT_vl1_3WT_fictif");
        List<String>  node3WTStyle = styleProvider.getSvgNodeStyles(fict3WTNode, false);
        assertEquals(1, node3WTStyle.size());
        assertEquals("three-windings-transformer", node3WTStyle.get(0));

        Node f2WTNode = graph1.getNode("2WT_ONE");
        List<String>  node2WTStyle = styleProvider.getSvgNodeStyles(f2WTNode, false);
        assertEquals(1, node2WTStyle.size());
        assertEquals("two-windings-transformer", node2WTStyle.get(0));

    }

    @Test
    public void testVl1() {
        Graph graph1 = graphBuilder.buildVoltageLevelGraph(vl1.getId(), false, true);
        assertEquals(toString("/vl1_nominal_voltage_style.svg"), toSVG(graph1, "/vl1_nominal_voltage_style.svg", getLayoutParameters(), new NoFeederValueProvider(), styleProvider));
    }

    @Test
    public void testVl2() {
        Graph graph2 = graphBuilder.buildVoltageLevelGraph(vl2.getId(), false, true);
        assertEquals(toString("/vl2_nominal_voltage_style.svg"), toSVG(graph2, "/vl2_nominal_voltage_style.svg", getLayoutParameters(), new NoFeederValueProvider(), styleProvider));
    }

    @Test
    public void testVl3() {
        Graph graph3 = graphBuilder.buildVoltageLevelGraph(vl3.getId(), false, true);
        assertEquals(toString("/vl3_nominal_voltage_style.svg"), toSVG(graph3, "/vl3_nominal_voltage_style.svg", getLayoutParameters(), new NoFeederValueProvider(), styleProvider));
    }

}
