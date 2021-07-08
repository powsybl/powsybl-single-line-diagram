/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.raw;

import com.powsybl.sld.RawGraphBuilder;
import com.powsybl.sld.layout.HorizontalSubstationLayoutFactory;
import com.powsybl.sld.layout.PositionVoltageLevelLayoutFactory;
import com.powsybl.sld.layout.VerticalSubstationLayoutFactory;
import com.powsybl.sld.model.*;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;

/*
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 */
public class TestCase11SubstationGraph extends AbstractTestCaseRaw {

    @Before
    public void setUp() {
        RawGraphBuilder.SubstationBuilder ssb1 = rawGraphBuilder.createSubstationBuilder("subst");
        RawGraphBuilder.VoltageLevelBuilder vlb1 = rawGraphBuilder.createVoltageLevelBuilder("vl1", 380, false);
        ssb1.addVlBuilder(vlb1);

        BusNode bbs1 = vlb1.createBusBarSection("bbs1", 1, 1);
        BusNode bbs2 = vlb1.createBusBarSection("bbs2", 1, 2);
        BusNode bbs3 = vlb1.createBusBarSection("bbs3", 2, 1);
        BusNode bbs4 = vlb1.createBusBarSection("bbs4", 2, 2);

        SwitchNode dsect11 = vlb1.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dsect11", false, false);
        SwitchNode dtrct11 = vlb1.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "dtrct11", false, false);
        SwitchNode dsect12 = vlb1.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dsect12", false, false);
        vlb1.connectNode(bbs1, dsect11);
        vlb1.connectNode(dsect11, dtrct11);
        vlb1.connectNode(dtrct11, dsect12);
        vlb1.connectNode(dsect12, bbs2);

        SwitchNode dsect21 = vlb1.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dsect21", false, false);
        SwitchNode dtrct21 = vlb1.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "dtrct21", false, false);
        SwitchNode dsect22 = vlb1.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dsect22", false, false);
        vlb1.connectNode(bbs3, dsect21);
        vlb1.connectNode(dsect21, dtrct21);
        vlb1.connectNode(dtrct21, dsect22);
        vlb1.connectNode(dsect22, bbs4);

        FeederNode load1 = vlb1.createLoad("load1", 0, BusCell.Direction.TOP);
        SwitchNode dload1 = vlb1.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dload1", false, false);
        SwitchNode bload1 = vlb1.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "bload1", false, false);
        vlb1.connectNode(bbs1, dload1);
        vlb1.connectNode(dload1, bload1);
        vlb1.connectNode(load1, bload1);

        FeederNode gen1 = vlb1.createGenerator("gen1", 2, BusCell.Direction.BOTTOM);
        SwitchNode dgen1 = vlb1.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dgen1", false, false);
        SwitchNode bgen1 = vlb1.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "bgen1", false, false);
        vlb1.connectNode(bbs3, dgen1);
        vlb1.connectNode(dgen1, bgen1);
        vlb1.connectNode(gen1, bgen1);

        FeederNode load2 = vlb1.createLoad("load2", 8, BusCell.Direction.TOP);
        SwitchNode dload2 = vlb1.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dload2", false, false);
        SwitchNode bload2 = vlb1.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "bload2", false, false);
        vlb1.connectNode(bbs2, dload2);
        vlb1.connectNode(dload2, bload2);
        vlb1.connectNode(load2, bload2);

        FeederNode gen2 = vlb1.createGenerator("gen2", 12, BusCell.Direction.BOTTOM);
        SwitchNode dgen2 = vlb1.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dgen2", false, false);
        SwitchNode bgen2 = vlb1.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "bgen2", false, false);
        vlb1.connectNode(bbs4, dgen2);
        vlb1.connectNode(dgen2, bgen2);
        vlb1.connectNode(gen2, bgen2);

        RawGraphBuilder.VoltageLevelBuilder vlb2 = rawGraphBuilder.createVoltageLevelBuilder("vl2", 225, false);
        ssb1.addVlBuilder(vlb2);

        BusNode bbs5 = vlb2.createBusBarSection("bbs5", 1, 1);
        BusNode bbs6 = vlb2.createBusBarSection("bbs6", 2, 1);

        SwitchNode dscpl1 = vlb2.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dscpl1", false, false);
        SwitchNode ddcpl1 = vlb2.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "ddcpl1", false, false);
        SwitchNode dscpl2 = vlb2.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dscpl2", false, false);
        vlb2.connectNode(bbs5, dscpl1);
        vlb2.connectNode(dscpl1, ddcpl1);
        vlb2.connectNode(ddcpl1, dscpl2);
        vlb2.connectNode(dscpl2, bbs6);

        FeederNode load3 = vlb2.createLoad("load3", 0, BusCell.Direction.TOP);
        SwitchNode dload3 = vlb2.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dload3", false, false);
        SwitchNode bload3 = vlb2.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "bload3", false, false);
        vlb2.connectNode(bbs5, dload3);
        vlb2.connectNode(load3, bload3);
        vlb2.connectNode(bload3, dload3);

        FeederNode gen4 = vlb2.createGenerator("gen4", 2, BusCell.Direction.BOTTOM);
        SwitchNode dgen4 = vlb2.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dgen4", false, false);
        SwitchNode bgen4 = vlb2.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "bgen4", false, false);
        vlb2.connectNode(bbs6, dgen4);
        vlb2.connectNode(gen4, bgen4);
        vlb2.connectNode(bgen4, dgen4);

        // third voltage level
        RawGraphBuilder.VoltageLevelBuilder vlb3 = rawGraphBuilder.createVoltageLevelBuilder("vl3", 225, false);
        ssb1.addVlBuilder(vlb3);

        BusNode bbs7 = vlb3.createBusBarSection("bbs7", 1, 1);

        FeederNode load4 = vlb3.createLoad("load4", 0, BusCell.Direction.TOP);
        SwitchNode dload4 = vlb3.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dload4", false, false);
        SwitchNode bload4 = vlb3.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "bload4", false, false);
        vlb3.connectNode(bbs7, dload4);
        vlb3.connectNode(bload4, load4);
        vlb3.connectNode(bload4, dload4);

        /*
        // two windings transformers between voltage levels
        //
        */

        Map<RawGraphBuilder.VoltageLevelBuilder, Feeder2WTLegNode> feeder2WTs1 = ssb1.createFeeder2WT("trf1", vlb1, vlb2,
                1, 1, BusCell.Direction.TOP, BusCell.Direction.TOP);
        SwitchNode dtrf11 = vlb1.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dtrf11", false, false);
        SwitchNode btrf11 = vlb1.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "btrf11", false, false);
        vlb1.connectNode(bbs1, dtrf11);
        vlb1.connectNode(dtrf11, btrf11);
        vlb1.connectNode(btrf11, feeder2WTs1.get(vlb1));

        SwitchNode dtrf21 = vlb2.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dtrf21", false, false);
        SwitchNode btrf21 = vlb2.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "btrf21", false, false);
        vlb2.connectNode(bbs5, dtrf21);
        vlb2.connectNode(dtrf21, btrf21);
        vlb2.connectNode(btrf21, feeder2WTs1.get(vlb2));

        Map<RawGraphBuilder.VoltageLevelBuilder, Feeder2WTLegNode> feeder2WTs2 = ssb1.createFeeder2WT("trf2", vlb1, vlb2,
                11, 7, BusCell.Direction.TOP, BusCell.Direction.BOTTOM);
        SwitchNode dtrf12 = vlb1.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dtrf12", false, false);
        SwitchNode btrf12 = vlb1.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "btrf12", false, false);
        vlb1.connectNode(bbs2, dtrf12);
        vlb1.connectNode(dtrf12, btrf12);
        vlb1.connectNode(btrf12, feeder2WTs2.get(vlb1));

        SwitchNode dtrf22 = vlb2.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dtrf22", false, false);
        SwitchNode btrf22 = vlb2.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "btrf22", false, false);
        vlb2.connectNode(bbs6, dtrf22);
        vlb2.connectNode(dtrf22, btrf22);
        vlb2.connectNode(btrf22, feeder2WTs2.get(vlb2));

        Map<RawGraphBuilder.VoltageLevelBuilder, Feeder2WTLegNode> feeder2WTs3 = ssb1.createFeeder2WT("trf3", vlb1, vlb2,
                3, 8, BusCell.Direction.BOTTOM, BusCell.Direction.BOTTOM);

        SwitchNode dtrf13 = vlb1.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dtrf13", false, false);
        SwitchNode btrf13 = vlb1.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "btrf13", false, false);
        vlb1.connectNode(bbs3, dtrf13);
        vlb1.connectNode(dtrf13, btrf13);
        vlb1.connectNode(btrf13, feeder2WTs3.get(vlb1));

        SwitchNode dtrf23 = vlb2.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dtrf23", false, false);
        SwitchNode btrf23 = vlb2.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "btrf23", false, false);
        vlb2.connectNode(bbs6, dtrf23);
        vlb2.connectNode(dtrf23, btrf23);
        vlb2.connectNode(btrf23, feeder2WTs3.get(vlb2));

        Map<RawGraphBuilder.VoltageLevelBuilder, Feeder2WTLegNode> feeder2WTs4 = ssb1.createFeeder2WT("trf4", vlb1, vlb2,
                10, 3, BusCell.Direction.BOTTOM, BusCell.Direction.TOP);

        SwitchNode dtrf14 = vlb1.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dtrf14", false, false);
        SwitchNode btrf14 = vlb1.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "btrf14", false, false);
        vlb1.connectNode(bbs4, dtrf14);
        vlb1.connectNode(dtrf14, btrf14);
        vlb1.connectNode(btrf14, feeder2WTs4.get(vlb1));

        SwitchNode dtrf24 = vlb2.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dtrf24", false, false);
        SwitchNode btrf24 = vlb2.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "btrf24", false, false);
        vlb2.connectNode(bbs5, dtrf24);
        vlb2.connectNode(dtrf24, btrf24);
        vlb2.connectNode(btrf24, feeder2WTs4.get(vlb2));

        Map<RawGraphBuilder.VoltageLevelBuilder, Feeder2WTLegNode> feeder2WTs5 = ssb1.createFeeder2WT("trf5", vlb1, vlb3,
                4, 1, BusCell.Direction.TOP, BusCell.Direction.BOTTOM);

        SwitchNode dtrf15 = vlb1.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dtrf15", false, false);
        SwitchNode btrf15 = vlb1.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "btrf15", false, false);
        vlb1.connectNode(bbs1, dtrf15);
        vlb1.connectNode(dtrf15, btrf15);
        vlb1.connectNode(btrf15, feeder2WTs5.get(vlb1));

        SwitchNode dtrf25 = vlb3.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dtrf25", false, false);
        SwitchNode btrf25 = vlb3.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "btrf25", false, false);
        vlb3.connectNode(bbs7, dtrf25);
        vlb3.connectNode(dtrf25, btrf25);
        vlb3.connectNode(btrf25, feeder2WTs5.get(vlb3));
        /*
        // three windings transformers between voltage levels
        //
       */

        Map<RawGraphBuilder.VoltageLevelBuilder, Feeder3WTLegNode> feeder3WTs6 = ssb1.createFeeder3WT("trf6", vlb1, vlb2, vlb3,
                5, 5, 2, BusCell.Direction.TOP, BusCell.Direction.TOP, BusCell.Direction.TOP);

        SwitchNode dtrf16 = vlb1.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dtrf16", false, false);
        SwitchNode btrf16 = vlb1.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "btrf16", false, false);
        vlb1.connectNode(bbs1, dtrf16);
        vlb1.connectNode(dtrf16, btrf16);
        vlb1.connectNode(btrf16, feeder3WTs6.get(vlb1));

        SwitchNode dtrf26 = vlb2.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dtrf26", false, false);
        SwitchNode btrf26 = vlb2.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "btrf26", false, false);
        vlb2.connectNode(bbs6, dtrf26);
        vlb2.connectNode(dtrf26, btrf26);
        vlb2.connectNode(btrf26, feeder3WTs6.get(vlb2));

        SwitchNode dtrf36 = vlb3.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dtrf36", false, false);
        SwitchNode btrf36 = vlb3.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "btrf36", false, false);
        vlb3.connectNode(bbs7, dtrf36);
        vlb3.connectNode(dtrf36, btrf36);
        vlb3.connectNode(btrf36, feeder3WTs6.get(vlb3));

        Map<RawGraphBuilder.VoltageLevelBuilder, Feeder3WTLegNode> feeder3WTs7 = ssb1.createFeeder3WT("trf7", vlb1, vlb2, vlb3,
                6, 4, 3, BusCell.Direction.BOTTOM, BusCell.Direction.TOP, BusCell.Direction.BOTTOM);

        SwitchNode dtrf17 = vlb1.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dtrf17", false, false);
        SwitchNode btrf17 = vlb1.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "btrf17", false, false);
        vlb1.connectNode(bbs3, dtrf17);
        vlb1.connectNode(dtrf17, btrf17);
        vlb1.connectNode(btrf17, feeder3WTs7.get(vlb1));

        SwitchNode dtrf27 = vlb2.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dtrf27", false, false);
        SwitchNode btrf27 = vlb2.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "btrf27", false, false);
        vlb2.connectNode(bbs5, dtrf27);
        vlb2.connectNode(dtrf27, btrf27);
        vlb2.connectNode(btrf27, feeder3WTs7.get(vlb2));

        SwitchNode dtrf37 = vlb3.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dtrf37", false, false);
        SwitchNode btrf37 = vlb3.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "btrf37", false, false);
        vlb3.connectNode(bbs7, dtrf37);
        vlb3.connectNode(dtrf37, btrf37);
        vlb3.connectNode(btrf37, feeder3WTs7.get(vlb3));

        Map<RawGraphBuilder.VoltageLevelBuilder, Feeder3WTLegNode> feeder3WTs8 = ssb1.createFeeder3WT("trf8", vlb1, vlb2, vlb3,
                9, 6, 4, BusCell.Direction.TOP, BusCell.Direction.BOTTOM, BusCell.Direction.TOP);

        SwitchNode dtrf18 = vlb1.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dtrf18", false, false);
        SwitchNode btrf18 = vlb1.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "btrf18", false, false);
        vlb1.connectNode(bbs2, dtrf18);
        vlb1.connectNode(dtrf18, btrf18);
        vlb1.connectNode(btrf18, feeder3WTs8.get(vlb1));

        SwitchNode dtrf28 = vlb2.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dtrf28", false, false);
        SwitchNode btrf28 = vlb2.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "btrf28", false, false);
        vlb2.connectNode(bbs6, dtrf28);
        vlb2.connectNode(dtrf28, btrf28);
        vlb2.connectNode(btrf28, feeder3WTs8.get(vlb2));

        SwitchNode dtrf38 = vlb3.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dtrf38", false, false);
        SwitchNode btrf38 = vlb3.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "btrf38", false, false);
        vlb3.connectNode(bbs7, dtrf38);
        vlb3.connectNode(dtrf38, btrf38);
        vlb3.connectNode(btrf38, feeder3WTs8.get(vlb3));
        /*
        // Creation of another substation, another voltageLevel and a line between the two substations
        //
       */

        RawGraphBuilder.SubstationBuilder ssb2 = rawGraphBuilder.createSubstationBuilder("subst2");
        RawGraphBuilder.VoltageLevelBuilder vlsubst2 = rawGraphBuilder.createVoltageLevelBuilder("vlSubst2", 380, false);
        ssb2.addVlBuilder(vlsubst2);

        BusNode bbs12 = vlsubst2.createBusBarSection("bbs1_2", 1, 1);

        SwitchNode dline112 = vlb1.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dline11_2", false, false);
        SwitchNode bline112 = vlb1.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "bline11_2", false, false);

        SwitchNode dline212 = vlsubst2.createSwitchNode(SwitchNode.SwitchKind.DISCONNECTOR, "dline21_2", false, false);
        SwitchNode bline212 = vlsubst2.createSwitchNode(SwitchNode.SwitchKind.BREAKER, "bline21_2", false, false);
        Map<RawGraphBuilder.VoltageLevelBuilder, FeederLineNode> line1 =
                ssb2.createLine("line1", vlb1, vlsubst2, 7, 1, BusCell.Direction.TOP, BusCell.Direction.TOP);
        vlb1.connectNode(bbs1, dline112);
        vlb1.connectNode(dline112, bline112);
        vlb1.connectNode(bline112, line1.get(vlb1));
        vlsubst2.connectNode(bbs12, dline212);
        vlsubst2.connectNode(dline212, bline212);
        vlsubst2.connectNode(bline212, line1.get(vlsubst2));
    }

    @Test
    public void testH() {
        SubstationGraph g = rawGraphBuilder.buildSubstationGraph("subst", false);
        new HorizontalSubstationLayoutFactory().create(g, new PositionVoltageLevelLayoutFactory()).run(getLayoutParameters());
        assertEquals(toString("/TestCase11SubstationGraphH.json"), toJson(g, "/TestCase11SubstationGraphH.json"));
    }

    @Test
    public void testV() {
        SubstationGraph g = rawGraphBuilder.buildSubstationGraph("subst", false);
        new VerticalSubstationLayoutFactory().create(g, new PositionVoltageLevelLayoutFactory()).run(getLayoutParameters());
        assertEquals(toString("/TestCase11SubstationGraphV.json"), toJson(g, "/TestCase11SubstationGraphV.json"));
    }
}
