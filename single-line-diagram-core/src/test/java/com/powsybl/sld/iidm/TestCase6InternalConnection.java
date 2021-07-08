/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.iidm;

import com.powsybl.iidm.network.*;
import com.powsybl.sld.NetworkGraphBuilder;
import com.powsybl.sld.layout.BlockOrganizer;
import com.powsybl.sld.layout.ImplicitCellDetector;
import com.powsybl.sld.layout.LayoutParameters;
import com.powsybl.sld.layout.PositionVoltageLevelLayout;
import com.powsybl.sld.model.VoltageLevelGraph;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * <pre>
 *          --- b ---
 *          |       |
 * bbs1.1 -d1 ----- |-- bbs1.2
 * bbs2.1 ---- ds2 -d2- bbs2.2
 *
 * </pre>
 *
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public class TestCase6InternalConnection extends AbstractTestCaseIidm {

    @Override
    protected LayoutParameters getLayoutParameters() {
        return createDefaultLayoutParameters();
    }

    @Before
    public void setUp() {
        network = Network.create("testCase1", "test");
        graphBuilder = new NetworkGraphBuilder(network);
        substation = createSubstation(network, "s", "s", Country.FR);
        vl = createVoltageLevel(substation, "vl", "vl", TopologyKind.NODE_BREAKER, 380, 10);
        createBusBarSection(vl, "bbs1.1", "bbs1.1", 0, 1, 1);
        createBusBarSection(vl, "bbs1.2", "bbs1.2", 1, 1, 2);
        createBusBarSection(vl, "bbs2.1", "bbs2.1", 2, 2, 1);
        createBusBarSection(vl, "bbs2.2", "bbs2.2", 3, 2, 2);
        createSwitch(vl, "d1", "d1", SwitchKind.DISCONNECTOR, false, false, false, 0, 4);
        createSwitch(vl, "b", "b", SwitchKind.BREAKER, false, false, false, 4, 5);
        createSwitch(vl, "d2", "d2", SwitchKind.DISCONNECTOR, false, false, false, 5, 3);
        createInternalConnection(vl, 0, 1);
        createSwitch(vl, "ds2", "ds2", SwitchKind.DISCONNECTOR, false, false, false, 2, 3);
    }

    @Test
    public void test() {
        // build graph
        VoltageLevelGraph g = graphBuilder.buildVoltageLevelGraph(vl.getId(), false, true);

        // detect cells
        new ImplicitCellDetector().detectCells(g);

        // build blocks
        new BlockOrganizer().organize(g);

        // calculate coordinates
        new PositionVoltageLevelLayout(g).run(getLayoutParameters());

        // write Json and compare to reference
        assertEquals(toString("/TestCase6InternalConnection.json"), toJson(g, "/TestCase6InternalConnection.json"));
    }
}
