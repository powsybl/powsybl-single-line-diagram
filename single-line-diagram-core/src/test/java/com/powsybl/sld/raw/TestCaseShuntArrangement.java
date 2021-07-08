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

import static com.powsybl.sld.model.BusCell.Direction.TOP;
import static org.junit.Assert.assertEquals;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 */
public class TestCaseShuntArrangement extends AbstractTestCaseRaw {

    @Before
    public void setUp() {
        int i = 0;
        com.powsybl.sld.RawGraphBuilder.VoltageLevelBuilder vlBuilder = rawGraphBuilder.createVoltageLevelBuilder("vl", 380);
        BusNode bbs1 = vlBuilder.createBusBarSection("bbs1", 1, 1);
        BusNode bbs21 = vlBuilder.createBusBarSection("bbs21", 2, 1);
        BusNode bbs22 = vlBuilder.createBusBarSection("bbs22", 2, 2);
        FeederNode loadA = vlBuilder.createLoad("loadA", i++, TOP);
        FeederNode load = vlBuilder.createLoad("l", i++, TOP);
        FeederNode loadB = vlBuilder.createLoad("loadB", i++, TOP);
        SwitchNode dA = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dA", false, false);
        SwitchNode dB1 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dB1", false, false);
        SwitchNode dB2 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dB2", false, false);
        SwitchNode d1 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "d1", false, false);
        SwitchNode d2 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "d2", false, false);
        SwitchNode bA = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "bA", false, false);
        SwitchNode bB = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "bB", false, false);
        SwitchNode b = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "b", false, false);
        FictitiousNode f = vlBuilder.createFictitiousNode("f");
        FictitiousNode fB = vlBuilder.createFictitiousNode("fB");
        FictitiousNode fShuntA = vlBuilder.createFictitiousNode("fShuntA");
        FictitiousNode fShuntB = vlBuilder.createFictitiousNode("fShuntB");
        SwitchNode bShunt = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "bShunt", false, false);

        vlBuilder.connectNode(bbs1, dA);
        vlBuilder.connectNode(bA, dA);
        vlBuilder.connectNode(bA, fShuntA);
        vlBuilder.connectNode(loadA, fShuntA);

        vlBuilder.connectNode(bbs1, d1);
        vlBuilder.connectNode(f, d1);
        vlBuilder.connectNode(bbs21, d2);
        vlBuilder.connectNode(f, d2);
        vlBuilder.connectNode(f, b);
        vlBuilder.connectNode(b, load);

        vlBuilder.connectNode(bbs1, dB1);
        vlBuilder.connectNode(bbs22, dB2);
        vlBuilder.connectNode(dB1, fB);
        vlBuilder.connectNode(dB2, fB);
        vlBuilder.connectNode(fB, bB);
        vlBuilder.connectNode(fShuntB, bB);
        vlBuilder.connectNode(fShuntB, loadB);

        vlBuilder.connectNode(fShuntA, bShunt);
        vlBuilder.connectNode(fShuntB, bShunt);

        FeederNode gen1 = vlBuilder.createGenerator("gen1", i++, TOP);
        SwitchNode bg1 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "bg1", false, false);
        vlBuilder.connectNode(gen1, bg1);
        FictitiousNode fg1 = vlBuilder.createFictitiousNode("fg1");
        vlBuilder.connectNode(fg1, bg1);
        SwitchNode dg11 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dg11", false, false);
        vlBuilder.connectNode(dg11, fg1);
        vlBuilder.connectNode(dg11, bbs1);
        SwitchNode dg12 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dg12", false, false);
        vlBuilder.connectNode(dg12, fg1);
        vlBuilder.connectNode(dg12, bbs22);

        FeederNode loadC = vlBuilder.createLoad("loadC", i++, TOP);
        SwitchNode bC = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "bC", false, false);
        vlBuilder.connectNode(loadC, bC);
        FictitiousNode fC = vlBuilder.createFictitiousNode("fC");
        vlBuilder.connectNode(fC, bC);
        SwitchNode dC1 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dC1", false, false);
        vlBuilder.connectNode(dC1, fC);
        vlBuilder.connectNode(dC1, bbs1);
        SwitchNode dC2 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dC2", false, false);
        vlBuilder.connectNode(dC2, fC);
        vlBuilder.connectNode(dC2, bbs22);

        FeederNode loadD = vlBuilder.createLoad("loadD", i++, TOP);
        SwitchNode bD = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "bD", false, false);
        vlBuilder.connectNode(loadD, bD);
        FictitiousNode fD = vlBuilder.createFictitiousNode("fD");
        vlBuilder.connectNode(fD, bD);
        SwitchNode dD1 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dD1", false, false);
        vlBuilder.connectNode(dD1, fD);
        vlBuilder.connectNode(dD1, bbs1);
        SwitchNode dD2 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dD2", false, false);
        vlBuilder.connectNode(dD2, fD);
        vlBuilder.connectNode(dD2, bbs22);

        FeederNode gen2 = vlBuilder.createGenerator("gen2", i++, TOP);
        SwitchNode bg2 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "bg2", false, false);
        vlBuilder.connectNode(gen2, bg2);
        FictitiousNode fg2 = vlBuilder.createFictitiousNode("fg2");
        vlBuilder.connectNode(fg2, bg2);
        SwitchNode dg21 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dg21", false, false);
        vlBuilder.connectNode(dg21, fg2);
        vlBuilder.connectNode(dg21, bbs1);
        SwitchNode dg22 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dg22", false, false);
        vlBuilder.connectNode(dg22, fg2);
        vlBuilder.connectNode(dg22, bbs22);

        SwitchNode shuntGen = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "shuntGen", false, false);
        vlBuilder.connectNode(fg1, shuntGen);
        vlBuilder.connectNode(fg2, shuntGen);

        BusNode bbs13 = vlBuilder.createBusBarSection("bbs13", 1, 3);
        BusNode bbs23 = vlBuilder.createBusBarSection("bbs23", 2, 3);

        FeederNode loadE = vlBuilder.createLoad("loadE", i++, TOP);
        SwitchNode bE = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "bE", false, false);
        vlBuilder.connectNode(loadE, bE);
        FictitiousNode fE = vlBuilder.createFictitiousNode("fE");
        vlBuilder.connectNode(fE, bE);
        SwitchNode dE1 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dE1", false, false);
        vlBuilder.connectNode(dE1, fE);
        vlBuilder.connectNode(dE1, bbs13);
        SwitchNode dE2 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dE2", false, false);
        vlBuilder.connectNode(dE2, fE);
        vlBuilder.connectNode(dE2, bbs23);

        FictitiousNode commonFG = vlBuilder.createFictitiousNode("commonFG");

        FeederNode loadF = vlBuilder.createLoad("loadF", i++, TOP);
        vlBuilder.connectNode(loadF, commonFG);
        SwitchNode bF = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "bF", false, false);
        vlBuilder.connectNode(commonFG, bF);
        FictitiousNode fF = vlBuilder.createFictitiousNode("fF");
        vlBuilder.connectNode(fF, bF);
        SwitchNode dF1 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dF1", false, false);
        vlBuilder.connectNode(dF1, fF);
        vlBuilder.connectNode(dF1, bbs13);
        SwitchNode dF2 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dF2", false, false);
        vlBuilder.connectNode(dF2, fF);
        vlBuilder.connectNode(dF2, bbs23);

        FeederNode loadG = vlBuilder.createLoad("loadG", i++, TOP);
        FictitiousNode fLoadG = vlBuilder.createFictitiousNode("fLoadG");
        vlBuilder.connectNode(loadG, fLoadG);
        SwitchNode bFeederG = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "bFeederG", false, false);
        vlBuilder.connectNode(fLoadG, bFeederG);
        vlBuilder.connectNode(bFeederG, commonFG);

        SwitchNode bSwitchCG = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "bSwitchCG", false, false);
        vlBuilder.connectNode(fC, bSwitchCG);
        vlBuilder.connectNode(fLoadG, bSwitchCG);

    }

    @Test
    public void test1() {
        VoltageLevelGraph g = rawGraphBuilder.buildVoltageLevelGraph("vl", false, true);
        new ImplicitCellDetector().detectCells(g);
        new BlockOrganizer(new PositionFromExtension(), true, true, false).organize(g);
        new PositionVoltageLevelLayout(g).run(getLayoutParameters());
        assertEquals(toString("/TestCaseShuntArrangementNo.json"), toJson(g, "/TestCaseShuntArrangementNo.json"));
    }

    @Test
    public void test2() {
        VoltageLevelGraph g = rawGraphBuilder.buildVoltageLevelGraph("vl", false, true);
        new ImplicitCellDetector().detectCells(g);
        new BlockOrganizer(new PositionFromExtension(), true, true, true).organize(g);
        new PositionVoltageLevelLayout(g).run(getLayoutParameters());
        assertEquals(toString("/TestCaseShuntArrangementYes.json"), toJson(g, "/TestCaseShuntArrangementYes.json"));
    }

}
