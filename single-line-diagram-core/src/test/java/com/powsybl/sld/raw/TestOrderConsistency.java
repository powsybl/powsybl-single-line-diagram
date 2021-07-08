/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.raw;

import com.powsybl.sld.RawGraphBuilder;
import com.powsybl.sld.layout.BlockOrganizer;
import com.powsybl.sld.layout.ImplicitCellDetector;
import com.powsybl.sld.layout.PositionVoltageLevelLayout;
import com.powsybl.sld.layout.positionbyclustering.PositionByClustering;
import com.powsybl.sld.layout.positionfromextension.PositionFromExtension;
import com.powsybl.sld.model.*;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 */

public class TestOrderConsistency extends AbstractTestCaseRaw {

    @Before
    public void setUp() {
        createCommons("vl1", true);
        createCommons("vl2", false);
    }

    private void createCommons(String vlId, boolean middleLeft) {
        RawGraphBuilder.VoltageLevelBuilder vlBuilder = rawGraphBuilder.createVoltageLevelBuilder(vlId, 380);
        BusNode bbs11 = vlBuilder.createBusBarSection("bbs11", 1, 1);
        BusNode bbs12 = vlBuilder.createBusBarSection("bbs12", 1, 2);
        BusNode bbs21 = vlBuilder.createBusBarSection("bbs21", 2, 1);
        BusNode bbs22 = vlBuilder.createBusBarSection("bbs22", 2, 2);
        SwitchNode ss1 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "ss1", false, false);
        SwitchNode ss2 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "ss2", false, false);
        vlBuilder.connectNode(bbs11, ss1);
        vlBuilder.connectNode(bbs12, ss1);
        vlBuilder.connectNode(bbs21, ss2);
        vlBuilder.connectNode(bbs22, ss2);

        FeederNode load1 = vlBuilder.createLoad("l1", 0, BusCell.Direction.TOP);
        SwitchNode d11 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "d11", false, false);
        SwitchNode d12 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "d12", false, false);
        FictitiousNode f1 = vlBuilder.createFictitiousNode("f1");
        SwitchNode b1 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "b1", false, false);
        vlBuilder.connectNode(bbs11, d11);
        vlBuilder.connectNode(d11, f1);
        vlBuilder.connectNode(bbs21, d12);
        vlBuilder.connectNode(d12, f1);
        vlBuilder.connectNode(f1, b1);
        vlBuilder.connectNode(b1, load1);

        FeederNode loadMiddle = vlBuilder.createLoad("l", 1, BusCell.Direction.TOP);
        SwitchNode dMiddle1 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "d1", false, false);
        SwitchNode dMiddle2 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "d2", false, false);
        FictitiousNode fMiddle = vlBuilder.createFictitiousNode("f");
        SwitchNode bMiddle = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "b", false, false);
        if (middleLeft) {
            vlBuilder.connectNode(bbs11, dMiddle1);
            vlBuilder.connectNode(bbs22, dMiddle2);
        } else {
            vlBuilder.connectNode(bbs12, dMiddle1);
            vlBuilder.connectNode(bbs21, dMiddle2);
        }
        vlBuilder.connectNode(dMiddle1, fMiddle);
        vlBuilder.connectNode(dMiddle2, fMiddle);
        vlBuilder.connectNode(fMiddle, bMiddle);
        vlBuilder.connectNode(bMiddle, loadMiddle);

        FeederNode load2 = vlBuilder.createLoad("l2", 2, BusCell.Direction.TOP);
        SwitchNode d21 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "d21", false, false);
        SwitchNode d22 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "d22", false, false);
        FictitiousNode f2 = vlBuilder.createFictitiousNode("f2");
        SwitchNode b2 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "b2", false, false);
        vlBuilder.connectNode(bbs12, d21);
        vlBuilder.connectNode(d21, f2);
        vlBuilder.connectNode(bbs22, d22);
        vlBuilder.connectNode(d22, f2);
        vlBuilder.connectNode(f2, b2);
        vlBuilder.connectNode(b2, load2);
    }

    @Test
    public void testClustMiddleLeft() {
        VoltageLevelGraph g = rawGraphBuilder.buildVoltageLevelGraph("vl1", false, true);
        new ImplicitCellDetector().detectCells(g);
        new BlockOrganizer(new PositionByClustering()).organize(g);
        new PositionVoltageLevelLayout(g).run(getLayoutParameters());
        assertEquals(toString("/orderConsistencyClust1.json"), toJson(g, "/orderConsistencyClust1.json"));
    }

    @Test
    public void testClustNoMiddleLeft() {
        VoltageLevelGraph g = rawGraphBuilder.buildVoltageLevelGraph("vl2", false, true);
        new ImplicitCellDetector().detectCells(g);
        new BlockOrganizer(new PositionByClustering()).organize(g);
        new PositionVoltageLevelLayout(g).run(getLayoutParameters());
        assertEquals(toString("/orderConsistencyClust2.json"), toJson(g, "/orderConsistencyClust2.json"));
    }

    @Test
    public void testExtMiddleLeft() {
        VoltageLevelGraph g = rawGraphBuilder.buildVoltageLevelGraph("vl1", false, true);
        new ImplicitCellDetector().detectCells(g);
        new BlockOrganizer(new PositionFromExtension()).organize(g);
        new PositionVoltageLevelLayout(g).run(getLayoutParameters());
        assertEquals(toString("/orderConsistencyExt1.json"), toJson(g, "/orderConsistencyExt1.json"));
    }

    @Test
    public void testExtNoMiddleLeft() {
        VoltageLevelGraph g = rawGraphBuilder.buildVoltageLevelGraph("vl2", false, true);
        new ImplicitCellDetector().detectCells(g);
        new BlockOrganizer(new PositionFromExtension()).organize(g);
        new PositionVoltageLevelLayout(g).run(getLayoutParameters());
        assertEquals(toString("/orderConsistencyExt2.json"), toJson(g, "/orderConsistencyExt2.json"));
    }
}
