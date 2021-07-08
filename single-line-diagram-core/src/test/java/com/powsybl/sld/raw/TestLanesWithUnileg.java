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
import com.powsybl.sld.model.BusNode;
import com.powsybl.sld.model.FictitiousNode;
import com.powsybl.sld.model.VoltageLevelGraph;
import com.powsybl.sld.model.SwitchNode;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 */

public class TestLanesWithUnileg extends AbstractTestCaseRaw {
    @Before
    public void setUp() {
        com.powsybl.sld.RawGraphBuilder.VoltageLevelBuilder vlBuilder = rawGraphBuilder.createVoltageLevelBuilder("vl", 380);
        BusNode bbs11 = vlBuilder.createBusBarSection("bbs11", 1, 1);
        BusNode bbs12 = vlBuilder.createBusBarSection("bbs12", 1, 2);
        BusNode bbs13 = vlBuilder.createBusBarSection("bbs13", 1, 3);
        BusNode bbs22 = vlBuilder.createBusBarSection("bbs22", 2, 2);

        FictitiousNode fNode = vlBuilder.createFictitiousNode("Fictitious_unileg");
        SwitchNode unilegDc1 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dF1", false, false);
        SwitchNode unilegDc2 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dF2", false, false);
        vlBuilder.connectNode(bbs12, unilegDc1);
        vlBuilder.connectNode(bbs22, unilegDc2);
        vlBuilder.connectNode(unilegDc1, fNode);
        vlBuilder.connectNode(unilegDc2, fNode);

        SwitchNode db11 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "db11", false, false);
        SwitchNode b1 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "b1", false, false);
        SwitchNode db31 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "db31", false, false);
        vlBuilder.connectNode(bbs11, db11);
        vlBuilder.connectNode(db11, b1);
        vlBuilder.connectNode(b1, db31);
        vlBuilder.connectNode(db31, bbs13);

        SwitchNode db12 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "db12", false, false);
        SwitchNode b2 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "b2", false, false);
        SwitchNode db32 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "db32", false, false);
        vlBuilder.connectNode(bbs11, db12);
        vlBuilder.connectNode(db12, b2);
        vlBuilder.connectNode(b2, db32);
        vlBuilder.connectNode(db32, bbs13);
    }

    @Test
    public void test() {
        VoltageLevelGraph g = rawGraphBuilder.buildVoltageLevelGraph("vl", false, true);
        new ImplicitCellDetector().detectCells(g);
        new BlockOrganizer().organize(g);
        new PositionVoltageLevelLayout(g).run(getLayoutParameters());
        assertEquals(toString("/testLanesWithUnileg.json"), toJson(g, "/testLanesWithUnileg.json"));
    }
}
