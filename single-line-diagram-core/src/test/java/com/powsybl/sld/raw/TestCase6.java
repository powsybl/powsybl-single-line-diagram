/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.raw;

import com.powsybl.sld.layout.BlockOrganizer;
import com.powsybl.sld.layout.ImplicitCellDetector;
import com.powsybl.sld.layout.PositionVoltageLevelLayout;
import com.powsybl.sld.model.*;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * <pre>
 *              b
 *           /     \
 *          |       |
 * bbs1.1 -d1- ds1 -|-- bbs1.2
 * bbs2.1 ---- ds2 -d2- bbs2.2
 *
 * </pre>
 *
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 */

public class TestCase6 extends AbstractTestCaseRaw {

    @Before
    public void setUp() {
        com.powsybl.sld.RawGraphBuilder.VoltageLevelBuilder vlBuilder = rawGraphBuilder.createVoltageLevelBuilder("vl", 380);

        BusNode bbs11 = vlBuilder.createBusBarSection("bbs1.1", 1, 1);
        BusNode bbs12 = vlBuilder.createBusBarSection("bbs1.2", 1, 2);
        BusNode bbs21 = vlBuilder.createBusBarSection("bbs2.1", 2, 1);
        BusNode bbs22 = vlBuilder.createBusBarSection("bbs2.2", 2, 2);

        SwitchNode d1 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "d1", false, false);
        SwitchNode d2 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "d2", false, false);
        SwitchNode b = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "b", false, false);
        vlBuilder.connectNode(bbs11, d1);
        vlBuilder.connectNode(d1, b);
        vlBuilder.connectNode(d2, b);
        vlBuilder.connectNode(d2, bbs22);

        SwitchNode ds1 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "ds1", false, false);
        vlBuilder.connectNode(bbs11, ds1);
        vlBuilder.connectNode(bbs12, ds1);
        SwitchNode ds2 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "ds2", false, false);
        vlBuilder.connectNode(bbs21, ds2);
        vlBuilder.connectNode(bbs22, ds2);

    }

    @Test
    public void test() {
        VoltageLevelGraph g = rawGraphBuilder.buildVoltageLevelGraph("vl", false, true);
        new ImplicitCellDetector().detectCells(g);
        new BlockOrganizer().organize(g);
        new PositionVoltageLevelLayout(g).run(getLayoutParameters());
        assertEquals(toString("/TestCase6CouplingNonFlatHorizontal.json"), toJson(g, "/TestCase6CouplingNonFlatHorizontal.json"));
    }
}
