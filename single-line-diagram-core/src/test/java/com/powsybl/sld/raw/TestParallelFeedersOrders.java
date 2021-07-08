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
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 */
public class TestParallelFeedersOrders extends AbstractTestCaseRaw {

    @Before
    public void setUp() {
        com.powsybl.sld.RawGraphBuilder.VoltageLevelBuilder vlBuilder = rawGraphBuilder.createVoltageLevelBuilder("vl", 380);
        BusNode bbs = vlBuilder.createBusBarSection("bbs", 1, 1);
        SwitchNode b = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "b", false, false);
        SwitchNode d = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "d", false, false);
        FictitiousNode f = vlBuilder.createFictitiousNode("f");
        FeederNode load1 = vlBuilder.createLoad("l1", 0, BusCell.Direction.TOP);
        SwitchNode b2 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "b2", false, false);
        FeederNode load2 = vlBuilder.createLoad("l2", 1, BusCell.Direction.TOP);
        vlBuilder.connectNode(bbs, d);
        vlBuilder.connectNode(d, b);
        vlBuilder.connectNode(b, f);
        vlBuilder.connectNode(f, load1);
        vlBuilder.connectNode(f, b2);
        vlBuilder.connectNode(b2, load2);
    }

    @Test
    public void test() {
        VoltageLevelGraph g = rawGraphBuilder.buildVoltageLevelGraph("vl", false, true);
        new ImplicitCellDetector().detectCells(g);
        new BlockOrganizer().organize(g);
        new PositionVoltageLevelLayout(g).run(getLayoutParameters());
        assertEquals(toString("/testParallelFeedersOrders.json"), toJson(g, "/testParallelFeedersOrders.json"));
    }
}
