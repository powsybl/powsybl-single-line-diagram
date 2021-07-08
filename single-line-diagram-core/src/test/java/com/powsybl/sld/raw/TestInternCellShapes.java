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

public class TestInternCellShapes extends AbstractTestCaseRaw {
    @Before
    public void setUp() {
        com.powsybl.sld.RawGraphBuilder.VoltageLevelBuilder vlBuilder = rawGraphBuilder.createVoltageLevelBuilder("vl", 380);
        BusNode bbs11 = vlBuilder.createBusBarSection("bbs11", 1, 1);
        BusNode bbs21 = vlBuilder.createBusBarSection("bbs21", 2, 1);
        BusNode bbs12 = vlBuilder.createBusBarSection("bbs12", 1, 2);
        BusNode bbs22 = vlBuilder.createBusBarSection("bbs22", 2, 2);

        // InternCell.Shape.FLAT with one disconnector only on busbar 1
        SwitchNode flatDisconector = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "flatDisconector", false, false);
        vlBuilder.connectNode(bbs11, flatDisconector);
        vlBuilder.connectNode(flatDisconector, bbs12);

        // InternCell.Shape.FLAT with a breaker and disconnectors only on busbar 2
        SwitchNode flatBreaker = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "flatBreaker", false, false);
        SwitchNode dFlatBk1 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dFlatBk1", false, false);
        SwitchNode dFlatBk2 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dFlatBk2", false, false);
        vlBuilder.connectNode(dFlatBk1, bbs21);
        vlBuilder.connectNode(dFlatBk1, flatBreaker);
        vlBuilder.connectNode(dFlatBk2, flatBreaker);
        vlBuilder.connectNode(dFlatBk2, bbs22);

        // InternCell.Shape.VERTICAL on section 1
        SwitchNode verticalBreaker = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "verticalBreaker", false, false);
        SwitchNode dVerticalBk1 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dVerticalBk1", false, false);
        SwitchNode dVerticalBk2 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dVerticalBk2", false, false);
        vlBuilder.connectNode(dVerticalBk1, bbs11);
        vlBuilder.connectNode(dVerticalBk1, verticalBreaker);
        vlBuilder.connectNode(dVerticalBk2, verticalBreaker);
        vlBuilder.connectNode(dVerticalBk2, bbs21);

        // InternCell.Shape.CROSSOVER
        SwitchNode crossOverBreaker = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "crossOverBreaker", false, false);
        SwitchNode dCrossOverBk1 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dCrossOverBk1", false, false);
        SwitchNode dCrossOverBk2 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dCrossOverBk2", false, false);
        vlBuilder.connectNode(dCrossOverBk1, bbs11);
        vlBuilder.connectNode(dCrossOverBk1, crossOverBreaker);
        vlBuilder.connectNode(dCrossOverBk2, crossOverBreaker);
        vlBuilder.connectNode(dCrossOverBk2, bbs22);

        //Shape.UNILEG on section 2
        FictitiousNode fNode = vlBuilder.createFictitiousNode("Fictitious_unileg");
        SwitchNode unilegDc1 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dF1", false, false);
        SwitchNode unilegDc2 = vlBuilder.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dF2", false, false);
        vlBuilder.connectNode(bbs12, unilegDc1);
        vlBuilder.connectNode(bbs22, unilegDc2);
        vlBuilder.connectNode(unilegDc1, fNode);
        vlBuilder.connectNode(unilegDc2, fNode);
    }

    @Test
    public void test() {
        VoltageLevelGraph g = rawGraphBuilder.buildVoltageLevelGraph("vl", false, true);
        new ImplicitCellDetector().detectCells(g);
        new BlockOrganizer().organize(g);
        new PositionVoltageLevelLayout(g).run(getLayoutParameters());
        assertEquals(toString("/TestInternCellShapes.json"), toJson(g, "/TestInternCellShapes.json"));
    }
}
