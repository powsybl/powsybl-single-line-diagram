/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.raw;

import com.powsybl.sld.RawGraphBuilder;
import com.powsybl.sld.layout.BlockOrganizer;
import com.powsybl.sld.layout.ImplicitCellDetector;
import com.powsybl.sld.layout.PositionVoltageLevelLayout;
import com.powsybl.sld.model.*;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * <PRE>
 *
 * fict2
 * |
 * d2
 * |
 * b
 * |
 * fict1
 * |
 * d1
 * |
 * ------ bbs
 * </PRE>
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class TestIncompleteFeederIssue extends AbstractTestCaseRaw {

    @Before
    public void setUp() {
    }

    @Test
    public void test() {
        RawGraphBuilder.VoltageLevelBuilder vlBuilder = rawGraphBuilder.createVoltageLevelBuilder("vl", 380);
        BusNode bbs = vlBuilder.createBusBarSection("bbs", 1, 1);
        SwitchNode d1 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "d1", false, false);
        FictitiousNode fict1 = vlBuilder.createFictitiousNode("fict1");
        SwitchNode b2 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "b", false, false);
        SwitchNode d2 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "d2", false, false);
        FictitiousNode fict2 = vlBuilder.createFictitiousNode("fict2");
        vlBuilder.connectNode(bbs, d1);
        vlBuilder.connectNode(d1, fict1);
        vlBuilder.connectNode(fict1, b2);
        vlBuilder.connectNode(b2, d2);
        vlBuilder.connectNode(d2, fict2);
        VoltageLevelGraph g = rawGraphBuilder.buildVoltageLevelGraph("vl", false, true);
        new ImplicitCellDetector().detectCells(g);
        new BlockOrganizer().organize(g);
        new PositionVoltageLevelLayout(g).run(getLayoutParameters());
        assertEquals(toString("/TestIncompleteFeederIssue.json"), toJson(g, "/TestIncompleteFeederIssue.json"));
    }
}
