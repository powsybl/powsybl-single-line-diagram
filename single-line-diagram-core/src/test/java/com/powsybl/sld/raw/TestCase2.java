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
import com.powsybl.sld.layout.positionfromextension.PositionFromExtension;
import com.powsybl.sld.model.*;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * <pre>
 *     l
 *     |
 *     b
 *    / \
 *   |   |
 * -d1---|---- bbs1
 * -----d2---- bbs2
 *
 * </pre>
 *
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 */
public class TestCase2 extends AbstractTestCaseRaw {

    @Before
    public void setUp() {
        buildVl("vl");
        buildVl("vlUnstack");
    }

    private void buildVl(String id) {
        com.powsybl.sld.RawGraphBuilder.VoltageLevelBuilder vlBuilder = rawGraphBuilder.createVoltageLevelBuilder(id, 380);
        BusNode bbs1 = vlBuilder.createBusBarSection("bbs1", 1, 1);
        BusNode bbs2 = vlBuilder.createBusBarSection("bbs2", 2, 1);
        SwitchNode d1 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "d1", false, false);
        SwitchNode d2 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "d2", false, false);
        FictitiousNode f = vlBuilder.createFictitiousNode("2");
        SwitchNode b = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "b", false, false);
        FeederNode load = vlBuilder.createLoad("l", 0, BusCell.Direction.TOP);
        vlBuilder.connectNode(bbs1, d1);
        vlBuilder.connectNode(d1, f);
        vlBuilder.connectNode(bbs2, d2);
        vlBuilder.connectNode(d2, f);
        vlBuilder.connectNode(f, b);
        vlBuilder.connectNode(b, load);
    }

    @Test
    public void testStacked() {
        VoltageLevelGraph g = rawGraphBuilder.buildVoltageLevelGraph("vl", false, true);
        new ImplicitCellDetector().detectCells(g);
        new BlockOrganizer().organize(g);
        new PositionVoltageLevelLayout(g).run(getLayoutParameters());
        assertEquals(toString("/TestCase2Stacked.json"), toJson(g, "/TestCase2Stacked.json"));
    }

    @Test
    public void testUnstacked() {
        VoltageLevelGraph g = rawGraphBuilder.buildVoltageLevelGraph("vlUnstack", false, true);
        new ImplicitCellDetector().detectCells(g);
        new BlockOrganizer(new PositionFromExtension(), false).organize(g);
        new PositionVoltageLevelLayout(g).run(getLayoutParameters());
        assertEquals(toString("/TestCase2UnStackedCell.json"), toJson(g, "/TestCase2UnStackedCell.json"));
    }
}
