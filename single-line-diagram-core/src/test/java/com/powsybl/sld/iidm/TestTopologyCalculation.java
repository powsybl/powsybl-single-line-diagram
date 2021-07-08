/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.iidm;

import com.powsybl.iidm.network.Country;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.SwitchKind;
import com.powsybl.iidm.network.TopologyKind;
import com.powsybl.sld.NetworkGraphBuilder;
import com.powsybl.sld.iidm.extensions.ConnectablePosition;
import com.powsybl.sld.layout.*;
import com.powsybl.sld.model.VoltageLevelGraph;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * <PRE>
 * lA                                             lB
 * |   \                                           |
 * bA1  bA2                                        bB
 * |   /                                          /  \
 * dA          d11 --------- b1 -------- d12     dB1  dB2
 * |            |                         |      |     |
 * bbs 1.1 -----x----x-------d1-------x-- | -----x---- | -- bbs 1.2
 * bbs 2.1 ----------x-d21-x-b2-x-d22-x---x------------x--- bbs 2.2
 * </PRE>
 *
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class TestTopologyCalculation extends AbstractTestCaseIidm {

    @Override
    protected LayoutParameters getLayoutParameters() {
        return null;
    }

    @Before
    public void setUp() {
        network = Network.create("testCase1", "test");
        graphBuilder = new NetworkGraphBuilder(network);
        substation = createSubstation(network, "s", "s", Country.FR);
        vl = createVoltageLevel(substation, "vl", "vl", TopologyKind.NODE_BREAKER, 380, 12);
        createBusBarSection(vl, "bbs1.1", "bbs1.1", 0, 1, 1);
        createBusBarSection(vl, "bbs1.2", "bbs1.2", 1, 1, 2);
        createBusBarSection(vl, "bbs2.1", "bbs2.1", 2, 2, 1);
        createBusBarSection(vl, "bbs2.2", "bbs2.2", 3, 2, 2);

        createLoad(vl, "lA", "lA", "lA", 0, ConnectablePosition.Direction.TOP, 4, 10, 10);
        createSwitch(vl, "bA1", "bA1", SwitchKind.BREAKER, false, false, false, 4, 5);
        createSwitch(vl, "bA2", "bA2", SwitchKind.BREAKER, false, false, false, 4, 5);
        createSwitch(vl, "dA", "dA", SwitchKind.DISCONNECTOR, false, false, false, 0, 5);

        createSwitch(vl, "d11", "d11", SwitchKind.DISCONNECTOR, false, false, false, 0, 6);
        createSwitch(vl, "b1", "b1", SwitchKind.BREAKER, false, false, false, 6, 7);
        createSwitch(vl, "d12", "d12", SwitchKind.DISCONNECTOR, false, false, false, 7, 3);

        createSwitch(vl, "d21", "d21", SwitchKind.DISCONNECTOR, false, false, false, 2, 8);
        createSwitch(vl, "b2", "b2", SwitchKind.BREAKER, false, false, false, 8, 9);
        createSwitch(vl, "d22", "d22", SwitchKind.DISCONNECTOR, false, false, false, 9, 3);

        createSwitch(vl, "d1", "d1", SwitchKind.DISCONNECTOR, false, false, false, 0, 1);

        createLoad(vl, "lB", "lB", "lB", 1, ConnectablePosition.Direction.TOP, 10, 10, 10);
        createSwitch(vl, "bB", "bB", SwitchKind.BREAKER, false, false, false, 10, 11);
        createSwitch(vl, "dB1", "dB1", SwitchKind.DISCONNECTOR, false, false, false, 11, 1);
        createSwitch(vl, "dB2", "dB2", SwitchKind.DISCONNECTOR, false, false, false, 11, 3);
    }

    public void assertTopo(List<TopologicallyConnectedNodesSet> tcnss,
                           int expectedNbTcns,
                           int exploredIndex,
                           int expectedNbNodes,
                           int expectedNbBorderSwitches) {
        assertEquals(expectedNbTcns, tcnss.size());
        TopologicallyConnectedNodesSet tcns = tcnss.get(exploredIndex);
        assertEquals(expectedNbNodes, tcns.getNodesSet().size());
        assertEquals(expectedNbBorderSwitches, tcns.getBorderSwitchNodesSet().size());
    }

    @Test
    public void test() {
        // build graph
        VoltageLevelGraph g = graphBuilder.buildVoltageLevelGraph(vl.getId(), false, true);
        List<TopologicallyConnectedNodesSet> tcnss = TopologyCalculation.run(g);
        assertTopo(tcnss, 1, 0, 25, 0);
        g.getNode("bA1").setOpen(true);
        tcnss = TopologyCalculation.run(g);
        assertTopo(tcnss, 1, 0, 25, 0);
        g.getNode("bA2").setOpen(true);
        tcnss = TopologyCalculation.run(g);
        assertTopo(tcnss, 2, 0, 24, 2);
        assertTopo(tcnss, 2, 1, 3, 2);
        g.getNode("d1").setOpen(true);
        tcnss = TopologyCalculation.run(g);
        assertTopo(tcnss, 2, 0, 24, 2);
        assertTopo(tcnss, 2, 1, 3, 2);
        g.getNode("b1").setOpen(true);
        tcnss = TopologyCalculation.run(g);
        assertTopo(tcnss, 3, 0, 9, 4);
        assertTopo(tcnss, 3, 1, 17, 2);
        assertTopo(tcnss, 3, 2, 3, 2);
        g.getNode("dB2").setOpen(true);
        tcnss = TopologyCalculation.run(g);
        assertTopo(tcnss, 4, 0, 9, 4);
        assertTopo(tcnss, 4, 1, 7, 2);
        assertTopo(tcnss, 4, 2, 11, 2);
        assertTopo(tcnss, 4, 3, 3, 2);
    }
}
