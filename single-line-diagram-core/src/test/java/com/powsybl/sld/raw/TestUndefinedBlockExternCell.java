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
 * <PRE>
 * l
 * |
 * b
 * |
 * d
 * |
 * ------ bbs
 * </PRE>
 *
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 */
public class TestUndefinedBlockExternCell extends AbstractTestCaseRaw {

    @Before
    public void setUp() {
        com.powsybl.sld.RawGraphBuilder.VoltageLevelBuilder vlBuilder = rawGraphBuilder.createVoltageLevelBuilder("vl", 380);
        BusNode bbs = vlBuilder.createBusBarSection("bbs", 1, 1);
        SwitchNode d = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "d", false, false);
        FictitiousNode f0 = vlBuilder.createFictitiousNode("f0");
        vlBuilder.connectNode(bbs, d);
        vlBuilder.connectNode(d, f0);

        FictitiousNode f1 = vlBuilder.createFictitiousNode("f1");
        FeederNode l1 = vlBuilder.createLoad("l1", 0, BusCell.Direction.TOP);
        vlBuilder.connectNode(f0, f1);
        vlBuilder.connectNode(f1, l1);

        FictitiousNode f2 = vlBuilder.createFictitiousNode("f2");
        FeederNode l2 = vlBuilder.createLoad("l2", 1, BusCell.Direction.TOP);
        vlBuilder.connectNode(f1, f2);
        vlBuilder.connectNode(f2, l2);

        vlBuilder.connectNode(f2, f0);
    }

    @Test
    public void test() {
        VoltageLevelGraph g = rawGraphBuilder.buildVoltageLevelGraph("vl", false, true);
        new ImplicitCellDetector().detectCells(g);
        new BlockOrganizer().organize(g);
        new PositionVoltageLevelLayout(g).run(getLayoutParameters());
        assertEquals(toString("/TestUndefinedBlockExternCell.json"), toJson(g, "/TestUndefinedBlockExternCell.json"));
    }
}
