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
import com.powsybl.sld.layout.PositionVoltageLevelLayout;
import com.powsybl.sld.model.*;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * <pre>
 *            la                        gc
 *             |                        |
 *            ba                        bc
 *           /  \                       |
 *          |    |                      |
 * bbs1.1 -da1---|--- ss1 --db1--------dc- bbs1.2
 * bbs2.1 ------da2----------|---db2------
 *                           |    |
 *                            \  /
 *                             bb
 *                              |
 *                             lb
 *
 * </pre>
 * <p>
 * the branch c is to cover the merging part of SubSections class (and use of generator)
 *
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class TestCase4NotParallelel extends AbstractTestCaseIidm {

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
        createLoad(vl, "la", "la", "la", 10, ConnectablePosition.Direction.TOP, 3, 10, 10);
        createSwitch(vl, "ba", "ba", SwitchKind.BREAKER, false, false, false, 3, 4);
        createSwitch(vl, "da1", "da1", SwitchKind.DISCONNECTOR, false, false, false, 4, 0);
        createSwitch(vl, "da2", "da2", SwitchKind.DISCONNECTOR, false, false, false, 4, 2);
        createLoad(vl, "lb", "lb", "lb", 20, ConnectablePosition.Direction.BOTTOM, 5, 10, 10);
        createSwitch(vl, "bb", "bb", SwitchKind.BREAKER, false, false, false, 5, 6);
        createSwitch(vl, "db1", "db1", SwitchKind.DISCONNECTOR, false, false, false, 6, 1);
        createSwitch(vl, "db2", "db2", SwitchKind.DISCONNECTOR, false, false, false, 6, 2);
        createSwitch(vl, "ss1", "ss1", SwitchKind.DISCONNECTOR, false, false, false, 1, 0);
        createGenerator(vl, "gc", "gc", "gc", 30, ConnectablePosition.Direction.TOP, 7, 0, 20, false, 10, 10);
        createSwitch(vl, "bc", "bc", SwitchKind.BREAKER, false, false, false, 7, 8);
        createSwitch(vl, "dc1", "dc1", SwitchKind.DISCONNECTOR, false, false, false, 8, 1);
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
        assertEquals(toString("/TestCase4NotParallelel.json"), toJson(g, "/TestCase4NotParallelel.json"));
    }
}
