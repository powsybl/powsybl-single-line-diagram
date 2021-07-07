/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model;

import com.powsybl.iidm.network.*;
import com.powsybl.sld.NetworkGraphBuilder;
import org.joda.time.DateTime;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public class ZoneGraphTest {

    public static final String SUBSTATION_ID_1 = "Substation1";
    public static final String SUBSTATION_ID_2 = "Substation2";
    private static final String VOLTAGELEVEL_ID_1 = "VoltageLevel1";
    private static final String VOLTAGELEVEL_ID_2 = "VoltageLevel2";
    private static final String BUS_ID_1 = "Bus1";
    private static final String BUS_ID_2 = "Bus2";
    private static final String LINE_ID = "Line";

    public static Network createNetwork() {
        Network network = Network.create("Network", "test");
        network.setCaseDate(DateTime.parse("2018-01-01T00:30:00.000+01:00"));
        Substation substation1 = network.newSubstation()
                .setId(SUBSTATION_ID_1)
                .setCountry(Country.FR)
                .add();
        VoltageLevel voltageLevel1 = substation1.newVoltageLevel()
                .setId(VOLTAGELEVEL_ID_1)
                .setNominalV(380)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        voltageLevel1.getBusBreakerView().newBus()
                .setId(BUS_ID_1)
                .add();
        Substation substation2 = network.newSubstation()
                .setId(SUBSTATION_ID_2)
                .setCountry(Country.FR)
                .add();
        VoltageLevel voltageLevel2 = substation2.newVoltageLevel()
                .setId(VOLTAGELEVEL_ID_2)
                .setNominalV(380)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        voltageLevel2.getBusBreakerView().newBus()
                .setId(BUS_ID_2)
                .add();
        network.newLine()
                .setId(LINE_ID)
                .setVoltageLevel1(voltageLevel1.getId())
                .setBus1(BUS_ID_1)
                .setConnectableBus1(BUS_ID_1)
                .setVoltageLevel2(voltageLevel2.getId())
                .setBus2(BUS_ID_2)
                .setConnectableBus2(BUS_ID_2)
                .setR(3.0)
                .setX(33.0)
                .setG1(0.0)
                .setB1(386E-6 / 2)
                .setG2(0.0)
                .setB2(386E-6 / 2)
                .add();
        return network;
    }

    @Test
    public void test() {
        List<String> zone = Arrays.asList(SUBSTATION_ID_1, SUBSTATION_ID_2);
        ZoneGraph graph = new NetworkGraphBuilder(createNetwork()).buildZoneGraph(zone, false);
        assertEquals(2, graph.getNodes().size());
        assertEquals(SUBSTATION_ID_1, graph.getNodes().get(0).getSubstationId());
        assertEquals(SUBSTATION_ID_2, graph.getNodes().get(1).getSubstationId());
        assertEquals(1, graph.getLineEdges().size());
        BranchEdge edge = graph.getLineEdges().get(0);
        assertEquals(LINE_ID, edge.getId());
        String lineNodeId1 = getLineNodeId(graph, SUBSTATION_ID_1, VOLTAGELEVEL_ID_1, Branch.Side.ONE);
        String lineNodeId2 = getLineNodeId(graph, SUBSTATION_ID_2, VOLTAGELEVEL_ID_2, Branch.Side.TWO);
        assertEquals(lineNodeId1, edge.getNode1().getId());
        assertEquals(lineNodeId2, edge.getNode2().getId());
    }

    private String getLineNodeId(ZoneGraph graph, String substationId, String voltageLevelId, Branch.Side side) {
        SubstationGraph substationGraph1 = graph.getNode(substationId);
        assertNotNull(substationGraph1);
        VoltageLevelGraph voltageLevelGraph1 = substationGraph1.getNode(voltageLevelId);
        assertNotNull(voltageLevelGraph1);
        Node lineNode = voltageLevelGraph1.getNode(LINE_ID + "_" + side);
        assertNotNull(lineNode);
        return lineNode.getId();
    }

}
