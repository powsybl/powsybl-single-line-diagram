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
 */

public class TestCase4 extends AbstractTestCaseRaw {

    @Before
    public void setUp() {
        com.powsybl.sld.RawGraphBuilder.VoltageLevelBuilder vlBuilder = rawGraphBuilder.createVoltageLevelBuilder("vl", 380);
        BusNode bbs11 = vlBuilder.createBusBarSection("bbs1.1", 1, 1);
        BusNode bbs12 = vlBuilder.createBusBarSection("bbs1.2", 1, 2);
        SwitchNode ss1 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "ss1", false, false);
        vlBuilder.connectNode(bbs12, ss1);
        vlBuilder.connectNode(bbs11, ss1);
        BusNode bbs21 = vlBuilder.createBusBarSection("bbs2.1", 2, 1);

        FeederNode la = vlBuilder.createLoad("la", 10, BusCell.Direction.TOP);
        SwitchNode ba = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "ba", false, false);
        FictitiousNode fa = vlBuilder.createFictitiousNode("4");
        SwitchNode da1 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "da1", false, false);
        SwitchNode da2 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "da2", false, false);
        vlBuilder.connectNode(la, ba);
        vlBuilder.connectNode(ba, fa);
        vlBuilder.connectNode(fa, da1);
        vlBuilder.connectNode(da1, bbs11);
        vlBuilder.connectNode(fa, da2);
        vlBuilder.connectNode(da2, bbs21);

        SwitchNode db1 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "db1", false, false);
        SwitchNode db2 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "db2", false, false);
        FictitiousNode fb = vlBuilder.createFictitiousNode("6");
        SwitchNode bb = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "bb", false, false);
        FeederNode lb = vlBuilder.createLoad("lb", 20, BusCell.Direction.BOTTOM);
        vlBuilder.connectNode(lb, bb);
        vlBuilder.connectNode(bb, fb);
        vlBuilder.connectNode(fb, db1);
        vlBuilder.connectNode(db1, bbs12);
        vlBuilder.connectNode(fb, db2);
        vlBuilder.connectNode(db2, bbs21);

        SwitchNode dc1 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dc1", false, false);
        SwitchNode bc = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "bc", false, false);
        FeederNode gc = vlBuilder.createGenerator("gc", 30, BusCell.Direction.TOP);
        vlBuilder.connectNode(gc, bc);
        vlBuilder.connectNode(dc1, bbs12);
        vlBuilder.connectNode(bc, dc1);
    }

    @Test
    public void test() {
        VoltageLevelGraph g = rawGraphBuilder.buildVoltageLevelGraph("vl", false, true);
        new ImplicitCellDetector().detectCells(g);
        new BlockOrganizer().organize(g);
        new PositionVoltageLevelLayout(g).run(getLayoutParameters());
        assertEquals(toString("/TestCase4NotParallelel.json"), toJson(g, "/TestCase4NotParallelel.json"));
    }
}
