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
 *
 *       la     lb
 *       |      |
 *      nsa-bs-nsb
 *       |      |
 *       ba     bb
 *       |      |
 * bbs---da-----db---
 *
 * </pre>
 *
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 */
public class TestCase5H extends AbstractTestCaseRaw {

    @Before
    public void setUp() {
        com.powsybl.sld.RawGraphBuilder.VoltageLevelBuilder vlBuilder = rawGraphBuilder.createVoltageLevelBuilder("vl", 380);
        BusNode bbs = vlBuilder.createBusBarSection("bbs", 1, 1);
        FeederNode la = vlBuilder.createLoad("la", 10, BusCell.Direction.TOP);
        SwitchNode ba = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "ba", false, false);
        SwitchNode da = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "da", false, false);
        vlBuilder.connectNode(la, ba);
        vlBuilder.connectNode(ba, da);
        vlBuilder.connectNode(da, bbs);

        FeederNode lb = vlBuilder.createLoad("lb", 20, BusCell.Direction.BOTTOM);
        SwitchNode bb = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "bb", false, false);
        SwitchNode db = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "db", false, false);
        vlBuilder.connectNode(lb, bb);
        vlBuilder.connectNode(bb, db);
        vlBuilder.connectNode(db, bbs);

        SwitchNode bs = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "bs", false, false);
        vlBuilder.connectNode(la, bs);
        vlBuilder.connectNode(lb, bs);

    }

    @Test
    public void test() {
        VoltageLevelGraph g = rawGraphBuilder.buildVoltageLevelGraph("vl", false, true);
        new ImplicitCellDetector().detectCells(g);
        new BlockOrganizer().organize(g);
        new PositionVoltageLevelLayout(g).run(getLayoutParameters());
        assertEquals(toString("/TestCase5H.json"), toJson(g, "/TestCase5H.json"));
    }
}
