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

public class TestCaseComplexCoupling extends AbstractTestCaseRaw {

    @Before
    public void setUp() {
        com.powsybl.sld.RawGraphBuilder.VoltageLevelBuilder vlBuilder = rawGraphBuilder.createVoltageLevelBuilder("vl", 380);
        BusNode bbs1 = vlBuilder.createBusBarSection("bbs1", 1, 1);
        BusNode bbs2 = vlBuilder.createBusBarSection("bbs2", 2, 1);
        SwitchNode d1 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "d1", false, false);
        FictitiousNode f1 = vlBuilder.createFictitiousNode("f1");
        SwitchNode bA = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "bA", false, false);
        SwitchNode bB = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "bB", false, false);
        SwitchNode bC = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "bC", false, false);
        SwitchNode bD = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "bD", false, false);
        FictitiousNode f2 = vlBuilder.createFictitiousNode("f2");
        SwitchNode d2 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "d2", false, false);
        vlBuilder.connectNode(bbs1, d1);
        vlBuilder.connectNode(d1, f1);
        vlBuilder.connectNode(f1, bA);
        vlBuilder.connectNode(f2, bA);
        vlBuilder.connectNode(f1, bB);
        vlBuilder.connectNode(f2, bB);
        vlBuilder.connectNode(f1, bC);
        vlBuilder.connectNode(f2, bC);
        vlBuilder.connectNode(f2, bD);
        vlBuilder.connectNode(d2, bD);
        vlBuilder.connectNode(d2, bbs2);
    }

    @Test
    public void test() {
        VoltageLevelGraph g = rawGraphBuilder.buildVoltageLevelGraph("vl", false, true);
        new ImplicitCellDetector().detectCells(g);
        new BlockOrganizer().organize(g);
        new PositionVoltageLevelLayout(g).run(getLayoutParameters());
        assertEquals(toString("/TestCaseComplexCoupling.json"), toJson(g, "/TestCaseComplexCoupling.json"));
    }
}
