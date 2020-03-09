/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld;

import com.powsybl.iidm.network.*;
import com.powsybl.sld.iidm.extensions.ConnectablePosition;
import com.powsybl.sld.layout.BlockOrganizer;
import com.powsybl.sld.layout.ImplicitCellDetector;
import com.powsybl.sld.layout.LayoutParameters;
import com.powsybl.sld.layout.PositionVoltageLevelLayout;
import com.powsybl.sld.model.Graph;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class TestUnicityNodeIdWithMutipleNetwork extends AbstractTestCase {

    private Network network2;
    private GraphBuilder graphBuilder2;
    private Substation substation2;
    private VoltageLevel vl2;

    @Before
    public void setUp() {
        // Create first network with a substation and a voltageLevel
        network = Network.create("n1", "test");
        graphBuilder = new NetworkGraphBuilder(network);

        substation = createSubstation(network, "s", "s", Country.FR);
        vl = createVoltageLevel(substation, "vl", "vl", TopologyKind.NODE_BREAKER, 400, 10);
        createBusBarSection(vl, "bbs", "bbs", 0, 1, 1);
        createLoad(vl, "l", "l", "l", 0, ConnectablePosition.Direction.TOP, 2, 10, 10);
        createSwitch(vl, "d", "d", SwitchKind.DISCONNECTOR, false, false, false, 0, 1);
        createSwitch(vl, "b", "b", SwitchKind.BREAKER, false, false, false, 1, 2);

        // Create second network with a substation and a voltageLevel
        network2 = Network.create("n2", "test");
        substation2 = createSubstation(network2, "s", "s", Country.FR);
        vl2 = createVoltageLevel(substation2, "vl", "vl", TopologyKind.NODE_BREAKER, 400, 10);
        createBusBarSection(vl2, "bbs", "bbs", 0, 1, 1);
        createLoad(vl2, "l", "l", "l", 0, ConnectablePosition.Direction.TOP, 2, 10, 10);
        createSwitch(vl2, "d", "d", SwitchKind.DISCONNECTOR, false, false, false, 0, 1);
        createSwitch(vl2, "b", "b", SwitchKind.BREAKER, false, false, false, 1, 2);
    }

    @Test
    public void test() {
        LayoutParameters layoutParameters = new LayoutParameters();

        // Generating json for voltage level in first network
        Graph graph1 = Graph.create(vl.getId(), vl.getName(), vl.getNominalV(), vl.getSubstation().getId(), false, true, false);
        new ImplicitCellDetector().detectCells(graph1);
        new BlockOrganizer().organize(graph1);
        new PositionVoltageLevelLayout(graph1).run(layoutParameters);

        String refJson1 = toString("/TestUnicityNodeIdNetWork1.json");
        assertEquals(toJson(graph1, "/TestUnicityNodeIdNetWork1.json"), refJson1);

        // Generating json for voltage level in second network
        Graph graph2 = Graph.create(vl2.getId(), vl2.getName(), vl2.getNominalV(), vl2.getSubstation().getId(), false, true, false);
        new ImplicitCellDetector().detectCells(graph2);
        new BlockOrganizer().organize(graph2);
        new PositionVoltageLevelLayout(graph2).run(layoutParameters);

        String refJson2 = toString("/TestUnicityNodeIdNetWork2.json");
        assertEquals(toJson(graph2, "/TestUnicityNodeIdNetWork2.json"), refJson2);

        assertEquals(refJson1, refJson2);
    }
}
