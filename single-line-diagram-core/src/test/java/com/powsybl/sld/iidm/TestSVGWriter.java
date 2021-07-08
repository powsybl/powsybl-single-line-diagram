/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.iidm;

import com.powsybl.iidm.network.Branch.Side;
import com.powsybl.iidm.network.Network;
import com.powsybl.sld.layout.LayoutParameters;
import com.powsybl.sld.library.ComponentTypeName;
import com.powsybl.sld.model.*;
import com.powsybl.sld.svg.*;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class TestSVGWriter extends AbstractTestCaseIidm {

    private static final String SUBSTATION_1_ID = "Substation1";
    private static final String SUBSTATION_2_ID = "Substation2";
    private static final String VOLTAGE_LEVEL_11_ID = "VoltageLevel11";
    private static final double VOLTAGE_LEVEL_11_V = 400;
    private static final String VOLTAGE_LEVEL_12_ID = "VoltageLevel12";
    private static final double VOLTAGE_LEVEL_12_V = 280;
    private static final String VOLTAGE_LEVEL_21_ID = "VoltageLevel21";
    private static final double VOLTAGE_LEVEL_21_V = 280;
    private static final String BUS_11_ID = "Bus11";
    private static final String BUS_12_ID = "Bus12";
    private static final String BUS_21_ID = "Bus21";
    private static final String LOAD_ID = "Load";
    private static final String LINE_ID = "Line";
    private static final String GENERATOR_ID = "Generator";
    private static final String TRANSFORMER_ID = "Transformer";

    private VoltageLevelGraph g1;
    private VoltageLevelGraph g2;
    private VoltageLevelGraph g3;
    private SubstationGraph substG;
    private DiagramLabelProvider initValueProvider;
    private DiagramLabelProvider noFeederValueProvider;
    private LayoutParameters layoutParameters;
    private ZoneGraph zGraph;

    @Override
    public LayoutParameters getLayoutParameters() {
        return layoutParameters;
    }

    private void createVoltageLevelGraphs() {
        // Creation "by hand" (without any network) of 3 voltage level graphs
        // and then generation of a SVG with DefaultDiagramStyleProvider (no network necessary)
        //
        g1 = createVoltageLevelGraph1();
        g2 = createVoltageLevelGraph2();
        g3 = createVoltageLevelGraph3();
    }

    protected static VoltageLevelGraph createVoltageLevelGraph1() {
        VoltageLevelGraph g1 = VoltageLevelGraph.create(new VoltageLevelInfos("vl1", "vl1", 400), false, true);
        g1.setX(0);
        g1.setY(20);

        VoltageLevelInfos voltageLevelInfosLeg1 = new VoltageLevelInfos("vl1", "vl1", 400.);
        VoltageLevelInfos voltageLevelInfosLeg2 = new VoltageLevelInfos("vl2", "vl2", 225);
        VoltageLevelInfos voltageLevelInfosLeg3 = new VoltageLevelInfos("vl3", "vl3", 63.);

        BusNode vl1Bbs1 = BusNode.create(g1, "vl1_bbs1", "vl1_bbs1");
        vl1Bbs1.setX(0);
        vl1Bbs1.setY(300);
        vl1Bbs1.setPxWidth(200);
        vl1Bbs1.setPosition(new Position(0, 1, 4, 0, null));
        g1.addNode(vl1Bbs1);
        BusNode vl1Bbs2 = BusNode.create(g1, "vl1_bbs2", "vl1_bbs2");
        vl1Bbs2.setX(280);
        vl1Bbs2.setY(300);
        vl1Bbs2.setPxWidth(200);
        vl1Bbs2.setPosition(new Position(6, 1, 6, 0, null));
        g1.addNode(vl1Bbs2);
        SwitchNode vl1D1 = new SwitchNode("vl1_d1", "vl1_d1", ComponentTypeName.DISCONNECTOR, false, g1, SwitchNode.SwitchKind.DISCONNECTOR, false);
        vl1D1.setX(220);
        vl1D1.setY(300);
        g1.addNode(vl1D1);
        SwitchNode vl1B1 = new SwitchNode("vl1_b1", "vl1_b1", ComponentTypeName.BREAKER, false, g1, SwitchNode.SwitchKind.BREAKER, false);
        vl1B1.setRotationAngle(90.);
        vl1B1.setX(245);
        vl1B1.setY(300);
        g1.addNode(vl1B1);
        SwitchNode vl1D2 = new SwitchNode("vl1_d2", "vl1_d2", ComponentTypeName.DISCONNECTOR, false, g1, SwitchNode.SwitchKind.DISCONNECTOR, false);
        vl1D2.setX(270);
        vl1D2.setY(300);
        g1.addNode(vl1D2);
        g1.addEdge(vl1Bbs1, vl1D1);
        g1.addEdge(vl1D1, vl1B1);
        g1.addEdge(vl1B1, vl1D2);
        g1.addEdge(vl1D2, vl1Bbs2);

        FeederNode vl1Load1 = FeederInjectionNode.createLoad(g1, "vl1_load1", "vl1_load1");
        vl1Load1.setOrder(0);
        vl1Load1.setDirection(BusCell.Direction.TOP);
        vl1Load1.setX(40);
        vl1Load1.setY(80);
        g1.addNode(vl1Load1);
        SwitchNode vl1Bload1 = new SwitchNode("vl1_bload1", "vl1_bload1", ComponentTypeName.BREAKER, false, g1, SwitchNode.SwitchKind.BREAKER, false);
        vl1Bload1.setX(40);
        vl1Bload1.setY(180);
        g1.addNode(vl1Bload1);
        SwitchNode vl1Dload1 = new SwitchNode("vl1_dload1", "vl1_dload1", ComponentTypeName.DISCONNECTOR, false, g1, SwitchNode.SwitchKind.DISCONNECTOR, false);
        vl1Dload1.setX(40);
        vl1Dload1.setY(300);
        g1.addNode(vl1Dload1);
        g1.addEdge(vl1Load1, vl1Bload1);
        g1.addEdge(vl1Bload1, vl1Dload1);
        g1.addEdge(vl1Dload1, vl1Bbs1);

        Feeder2WTNode vl1Trf1 = Feeder2WTNode.create(g1, "vl1_trf1", "vl1_trf1", "vl1_trf1", FeederBranchNode.Side.ONE, new VoltageLevelInfos("vl2", "vl2", 225));
        vl1Trf1.setOrder(1);
        vl1Trf1.setDirection(BusCell.Direction.BOTTOM);
        vl1Trf1.setX(80);
        vl1Trf1.setY(500);
        g1.addNode(vl1Trf1);
        SwitchNode vl1Btrf1 = new SwitchNode("vl1_btrf1", "vl1_btrf1", ComponentTypeName.BREAKER, false, g1, SwitchNode.SwitchKind.BREAKER, false);
        vl1Btrf1.setX(80);
        vl1Btrf1.setY(400);
        g1.addNode(vl1Btrf1);
        SwitchNode vl1Dtrf1 = new SwitchNode("vl1_dtrf1", "vl1_dtrf1", ComponentTypeName.DISCONNECTOR, false, g1, SwitchNode.SwitchKind.DISCONNECTOR, false);
        vl1Dtrf1.setX(80);
        vl1Dtrf1.setY(300);
        g1.addNode(vl1Dtrf1);
        g1.addEdge(vl1Trf1, vl1Btrf1);
        g1.addEdge(vl1Btrf1, vl1Dtrf1);
        g1.addEdge(vl1Dtrf1, vl1Bbs1);

        Feeder2WTLegNode vl1Trf2One = Feeder2WTLegNode.createForVoltageLevelDiagram(g1, "vl1_trf2_one", "vl1_trf2", "vl1_trf2", FeederBranchNode.Side.ONE, new VoltageLevelInfos("vl2", "vl2", 225));
        vl1Trf2One.setOrder(2);
        vl1Trf2One.setDirection(BusCell.Direction.TOP);
        vl1Trf2One.setX(360);
        vl1Trf2One.setY(80);
        g1.addNode(vl1Trf2One);
        Feeder2WTLegNode vl1Trf2Two = Feeder2WTLegNode.createForVoltageLevelDiagram(g1, "vl1_trf2_two", "vl1_trf2", "vl1_trf2", FeederBranchNode.Side.TWO, new VoltageLevelInfos("vl3", "vl3", 63));
        vl1Trf2Two.setOrder(3);
        vl1Trf2Two.setDirection(BusCell.Direction.TOP);
        vl1Trf2Two.setX(440);
        vl1Trf2Two.setY(80);
        g1.addNode(vl1Trf2Two);
        Middle3WTNode vl1Trf2Fict = new Middle3WTNode(g1, "vl1_trf2", voltageLevelInfosLeg1, voltageLevelInfosLeg2, voltageLevelInfosLeg3);
        vl1Trf2Fict.setX(400);
        vl1Trf2Fict.setY(140);
        g1.addNode(vl1Trf2Fict);
        g1.addEdge(vl1Trf2One, vl1Trf2Fict);
        g1.addEdge(vl1Trf2Two, vl1Trf2Fict);
        SwitchNode vl1Btrf2 = new SwitchNode("vl1_btrf2", "vl1_btrf2", ComponentTypeName.BREAKER, false, g1, SwitchNode.SwitchKind.BREAKER, false);
        vl1Btrf2.setX(400);
        vl1Btrf2.setY(180);
        g1.addNode(vl1Btrf2);
        SwitchNode vl1Dtrf2 = new SwitchNode("vl1_dtrf2", "vl1_dtrf2", ComponentTypeName.DISCONNECTOR, false, g1, SwitchNode.SwitchKind.DISCONNECTOR, false);
        vl1Dtrf2.setX(400);
        vl1Dtrf2.setY(300);
        g1.addNode(vl1Dtrf2);
        g1.addEdge(vl1Trf2Fict, vl1Btrf2);
        g1.addEdge(vl1Btrf2, vl1Dtrf2);
        g1.addEdge(vl1Dtrf2, vl1Bbs2);

        return g1;
    }

    private static VoltageLevelGraph createVoltageLevelGraph2() {
        VoltageLevelGraph g2 = VoltageLevelGraph.create(new VoltageLevelInfos("vl2", "vl2", 225), false, true);
        g2.setX(550);
        g2.setY(20);

        VoltageLevelInfos voltageLevelInfosLeg1 = new VoltageLevelInfos("vl1", "vl1", 400.);
        VoltageLevelInfos voltageLevelInfosLeg2 = new VoltageLevelInfos("vl2", "vl2", 225);
        VoltageLevelInfos voltageLevelInfosLeg3 = new VoltageLevelInfos("vl3", "vl3", 63.);

        BusNode vl2Bbs1 = BusNode.create(g2, "vl2_bbs1", "vl2_bbs1");
        vl2Bbs1.setX(0);
        vl2Bbs1.setY(300);
        vl2Bbs1.setPxWidth(200);
        vl2Bbs1.setPxWidth(200);
        vl2Bbs1.setPosition(new Position(0, 1, 6, 0, null));
        g2.addNode(vl2Bbs1);
        FeederNode vl2Gen1 = FeederInjectionNode.createGenerator(g2, "vl2_gen1", "vl2_gen1");
        vl2Gen1.setOrder(0);
        vl2Gen1.setDirection(BusCell.Direction.TOP);
        vl2Gen1.setX(50);
        vl2Gen1.setY(80);
        g2.addNode(vl2Gen1);
        SwitchNode vl2Bgen1 = new SwitchNode("vl2_bgen1", "vl2_bgen1", ComponentTypeName.BREAKER, false, g2, SwitchNode.SwitchKind.BREAKER, false);
        vl2Bgen1.setX(50);
        vl2Bgen1.setY(180);
        g2.addNode(vl2Bgen1);
        SwitchNode vl2Dgen1 = new SwitchNode("vl2_dgen1", "vl2_dgen1", ComponentTypeName.DISCONNECTOR, false, g2, SwitchNode.SwitchKind.DISCONNECTOR, false);
        vl2Dgen1.setX(50);
        vl2Dgen1.setY(300);
        g2.addNode(vl2Dgen1);
        g2.addEdge(vl2Gen1, vl2Bgen1);
        g2.addEdge(vl2Bgen1, vl2Dgen1);
        g2.addEdge(vl2Dgen1, vl2Bbs1);

        Feeder2WTNode vl2Trf1 = Feeder2WTNode.create(g2, "vl2_trf1", "vl2_trf1", "vl2_trf1", FeederBranchNode.Side.TWO, new VoltageLevelInfos("vl1", "vl1", 400));
        vl2Trf1.setOrder(1);
        vl2Trf1.setDirection(BusCell.Direction.BOTTOM);
        vl2Trf1.setX(100);
        vl2Trf1.setY(500);
        g2.addNode(vl2Trf1);
        SwitchNode vl2Btrf1 = new SwitchNode("vl2_btrf1", "vl2_btrf1", ComponentTypeName.BREAKER, false, g2, SwitchNode.SwitchKind.BREAKER, false);
        vl2Btrf1.setX(100);
        vl2Btrf1.setY(400);
        g2.addNode(vl2Btrf1);
        SwitchNode vl2Dtrf1 = new SwitchNode("vl2_dtrf1", "vl2_dtrf1", ComponentTypeName.DISCONNECTOR, false, g2, SwitchNode.SwitchKind.DISCONNECTOR, false);
        vl2Dtrf1.setX(100);
        vl2Dtrf1.setY(300);
        g2.addNode(vl2Dtrf1);
        g2.addEdge(vl2Trf1, vl2Btrf1);
        g2.addEdge(vl2Btrf1, vl2Dtrf1);
        g2.addEdge(vl2Dtrf1, vl2Bbs1);

        Feeder2WTLegNode vl2Trf2One = Feeder2WTLegNode.createForVoltageLevelDiagram(g2, "vl2_trf2_one", "vl2_trf2", "vl2_trf2", FeederBranchNode.Side.ONE, new VoltageLevelInfos("vl1", "vl1", 400));
        vl2Trf2One.setOrder(2);
        vl2Trf2One.setDirection(BusCell.Direction.TOP);
        vl2Trf2One.setX(130);
        vl2Trf2One.setY(80);
        g2.addNode(vl2Trf2One);
        Feeder2WTLegNode vl2Trf2Two = Feeder2WTLegNode.createForVoltageLevelDiagram(g2, "vl2_trf2_two", "vl2_trf2", "vl2_trf2", FeederBranchNode.Side.TWO, new VoltageLevelInfos("vl3", "vl3", 63));
        vl2Trf2Two.setOrder(3);
        vl2Trf2Two.setDirection(BusCell.Direction.TOP);
        vl2Trf2Two.setX(190);
        vl2Trf2Two.setY(80);
        g2.addNode(vl2Trf2Two);
        Middle3WTNode vl2Trf2Fict = new Middle3WTNode(g2, "vl2_trf2", voltageLevelInfosLeg1, voltageLevelInfosLeg2, voltageLevelInfosLeg3);
        vl2Trf2Fict.setX(160);
        vl2Trf2Fict.setY(140);
        g2.addNode(vl2Trf2Fict);
        g2.addEdge(vl2Trf2One, vl2Trf2Fict);
        g2.addEdge(vl2Trf2Two, vl2Trf2Fict);
        SwitchNode vl2Btrf2 = new SwitchNode("vl2_btrf2", "vl2_btrf2", ComponentTypeName.BREAKER, false, g2, SwitchNode.SwitchKind.BREAKER, false);
        vl2Btrf2.setX(160);
        vl2Btrf2.setY(180);
        g2.addNode(vl2Btrf2);
        SwitchNode vl2Dtrf2 = new SwitchNode("vl2_dtrf2", "vl2_dtrf2", ComponentTypeName.DISCONNECTOR, false, g2, SwitchNode.SwitchKind.DISCONNECTOR, false);
        vl2Dtrf2.setX(160);
        vl2Dtrf2.setY(300);
        g2.addNode(vl2Dtrf2);
        g2.addEdge(vl2Trf2Fict, vl2Btrf2);
        g2.addEdge(vl2Btrf2, vl2Dtrf2);
        g2.addEdge(vl2Dtrf2, vl2Bbs1);

        return g2;
    }

    private static VoltageLevelGraph createVoltageLevelGraph3() {
        VoltageLevelGraph g3 = VoltageLevelGraph.create(new VoltageLevelInfos("vl3", "vl3", 63), false, true);
        g3.setX(850);
        g3.setY(20);

        VoltageLevelInfos voltageLevelInfosLeg1 = new VoltageLevelInfos("vl1", "vl1", 400.);
        VoltageLevelInfos voltageLevelInfosLeg2 = new VoltageLevelInfos("vl2", "vl2", 225);
        VoltageLevelInfos voltageLevelInfosLeg3 = new VoltageLevelInfos("vl3", "vl3", 63.);

        BusNode vl3Bbs1 = BusNode.create(g3, "vl3_bbs1", "vl3_bbs1");
        vl3Bbs1.setX(0);
        vl3Bbs1.setY(300);
        vl3Bbs1.setPxWidth(200);
        vl3Bbs1.setPosition(new Position(0, 1, 6, 0, null));
        g3.addNode(vl3Bbs1);
        FeederNode vl3Capa1 = FeederInjectionNode.createCapacitor(g3, "vl3_capa1", "vl3_capa1");
        vl3Capa1.setOrder(0);
        vl3Capa1.setDirection(BusCell.Direction.TOP);
        vl3Capa1.setX(40);
        vl3Capa1.setY(80);
        g3.addNode(vl3Capa1);
        SwitchNode vl3Bcapa1 = new SwitchNode("vl3_bcapa1", "vl3_bcapa1", ComponentTypeName.BREAKER, false, g3, SwitchNode.SwitchKind.BREAKER, false);
        vl3Bcapa1.setX(40);
        vl3Bcapa1.setY(180);
        g3.addNode(vl3Bcapa1);
        SwitchNode vl3Dcapa1 = new SwitchNode("vl3_dcapa1", "vl3_dcapa1", ComponentTypeName.DISCONNECTOR, false, g3, SwitchNode.SwitchKind.DISCONNECTOR, false);
        vl3Dcapa1.setX(40);
        vl3Dcapa1.setY(300);
        g3.addNode(vl3Dcapa1);
        g3.addEdge(vl3Capa1, vl3Bcapa1);
        g3.addEdge(vl3Bcapa1, vl3Dcapa1);
        g3.addEdge(vl3Dcapa1, vl3Bbs1);

        Feeder2WTLegNode vl3Trf2One = Feeder2WTLegNode.createForVoltageLevelDiagram(g3, "vl3_trf2_one", "vl3_trf2", "vl3_trf2", FeederBranchNode.Side.ONE, new VoltageLevelInfos("vl1", "vl1", 400));
        vl3Trf2One.setOrder(1);
        vl3Trf2One.setDirection(BusCell.Direction.TOP);
        vl3Trf2One.setX(110);
        vl3Trf2One.setY(80);
        g3.addNode(vl3Trf2One);
        Feeder2WTLegNode vl3Trf2Two = Feeder2WTLegNode.createForVoltageLevelDiagram(g3, "vl3_trf2_two", "vl3_trf2", "vl3_trf2", FeederBranchNode.Side.TWO, new VoltageLevelInfos("vl2", "vl2", 225));
        vl3Trf2Two.setOrder(2);
        vl3Trf2Two.setDirection(BusCell.Direction.TOP);
        vl3Trf2Two.setX(190);
        vl3Trf2Two.setY(80);
        g3.addNode(vl3Trf2Two);
        Middle3WTNode vl3Trf2Fict = new Middle3WTNode(g3, "vl3_trf2", voltageLevelInfosLeg1, voltageLevelInfosLeg2, voltageLevelInfosLeg3);
        vl3Trf2Fict.setX(150);
        vl3Trf2Fict.setY(140);
        g3.addNode(vl3Trf2Fict);
        g3.addEdge(vl3Trf2One, vl3Trf2Fict);
        g3.addEdge(vl3Trf2Two, vl3Trf2Fict);
        SwitchNode vl3Btrf2 = new SwitchNode("vl3_btrf2", "vl3_btrf2", ComponentTypeName.BREAKER, false, g3, SwitchNode.SwitchKind.BREAKER, false);
        vl3Btrf2.setX(150);
        vl3Btrf2.setY(180);
        g3.addNode(vl3Btrf2);
        SwitchNode vl3Dtrf2 = new SwitchNode("vl3_dtrf2", "vl3_dtrf2", ComponentTypeName.DISCONNECTOR, false, g3, SwitchNode.SwitchKind.DISCONNECTOR, false);
        vl3Dtrf2.setX(150);
        vl3Dtrf2.setY(300);
        g3.addNode(vl3Dtrf2);
        g3.addEdge(vl3Trf2Fict, vl3Btrf2);
        g3.addEdge(vl3Btrf2, vl3Dtrf2);
        g3.addEdge(vl3Dtrf2, vl3Bbs1);

        return g3;
    }

    private void createSubstationGraph() {
        // Creation "by hand" (without any network) of 3 voltage level graphs and one substation graph
        // and then generation of a SVG with DefaultDiagramStyleProvider (no network necessary)
        //

        // First voltage level graph :
        //
        VoltageLevelInfos vl1Infos = new VoltageLevelInfos("vl1", "vl1", 400);
        VoltageLevelGraph g1Graph = VoltageLevelGraph.create(vl1Infos, false, true);
        g1Graph.setX(0);
        g1Graph.setY(20);

        BusNode vl1Bbs1 = BusNode.create(g1Graph, "vl1_bbs1", "vl1_bbs1");
        vl1Bbs1.setX(0);
        vl1Bbs1.setY(300);
        vl1Bbs1.setPxWidth(200);
        vl1Bbs1.setPosition(new Position(0, 1, 4, 0, null));
        g1Graph.addNode(vl1Bbs1);
        BusNode vl1Bbs2 = BusNode.create(g1Graph, "vl1_bbs2", "vl1_bbs2");
        vl1Bbs2.setX(280);
        vl1Bbs2.setY(300);
        vl1Bbs2.setPxWidth(200);
        vl1Bbs2.setPosition(new Position(6, 1, 6, 0, null));
        g1Graph.addNode(vl1Bbs2);
        SwitchNode vl1D1 = new SwitchNode("vl1_d1", "vl1_d1", ComponentTypeName.DISCONNECTOR, false, g1Graph, SwitchNode.SwitchKind.DISCONNECTOR, false);
        vl1D1.setX(220);
        vl1D1.setY(300);
        g1Graph.addNode(vl1D1);
        SwitchNode vl1B1 = new SwitchNode("vl1_b1", "vl1_b1", ComponentTypeName.BREAKER, false, g1Graph, SwitchNode.SwitchKind.BREAKER, false);
        vl1B1.setRotationAngle(90.);
        vl1B1.setX(245);
        vl1B1.setY(300);
        g1Graph.addNode(vl1B1);
        SwitchNode vl1D2 = new SwitchNode("vl1_d2", "vl1_d2", ComponentTypeName.DISCONNECTOR, false, g1Graph, SwitchNode.SwitchKind.DISCONNECTOR, false);
        vl1D2.setX(270);
        vl1D2.setY(300);
        g1Graph.addNode(vl1D2);
        g1Graph.addEdge(vl1Bbs1, vl1D1);
        g1Graph.addEdge(vl1D1, vl1B1);
        g1Graph.addEdge(vl1B1, vl1D2);
        g1Graph.addEdge(vl1D2, vl1Bbs2);

        FeederNode vl1Load1 = FeederInjectionNode.createLoad(g1Graph, "vl1_load1", "vl1_load1");
        vl1Load1.setOrder(0);
        vl1Load1.setDirection(BusCell.Direction.TOP);
        vl1Load1.setX(40);
        vl1Load1.setY(80);
        g1Graph.addNode(vl1Load1);
        SwitchNode vl1Bload1 = new SwitchNode("vl1_bload1", "vl1_bload1", ComponentTypeName.BREAKER, false, g1Graph, SwitchNode.SwitchKind.BREAKER, false);
        vl1Bload1.setX(40);
        vl1Bload1.setY(180);
        g1Graph.addNode(vl1Bload1);
        SwitchNode vl1Dload1 = new SwitchNode("vl1_dload1", "vl1_dload1", ComponentTypeName.DISCONNECTOR, false, g1Graph, SwitchNode.SwitchKind.DISCONNECTOR, false);
        vl1Dload1.setX(40);
        vl1Dload1.setY(300);
        g1Graph.addNode(vl1Dload1);
        g1Graph.addEdge(vl1Load1, vl1Bload1);
        g1Graph.addEdge(vl1Bload1, vl1Dload1);
        g1Graph.addEdge(vl1Dload1, vl1Bbs1);

        Feeder2WTLegNode vl1Trf1 = Feeder2WTLegNode.createForSubstationDiagram(g1Graph, "vl1_trf1", "vl1_trf1", "vl1_trf1", FeederBranchNode.Side.ONE);
        vl1Trf1.setOrder(1);
        vl1Trf1.setDirection(BusCell.Direction.BOTTOM);
        vl1Trf1.setX(80);
        vl1Trf1.setY(500);
        g1Graph.addNode(vl1Trf1);
        SwitchNode vl1Btrf1 = new SwitchNode("vl1_btrf1", "vl1_btrf1", ComponentTypeName.BREAKER, false, g1Graph, SwitchNode.SwitchKind.BREAKER, false);
        vl1Btrf1.setX(80);
        vl1Btrf1.setY(400);
        g1Graph.addNode(vl1Btrf1);
        SwitchNode vl1Dtrf1 = new SwitchNode("vl1_dtrf1", "vl1_dtrf1", ComponentTypeName.DISCONNECTOR, false, g1Graph, SwitchNode.SwitchKind.DISCONNECTOR, false);
        vl1Dtrf1.setX(80);
        vl1Dtrf1.setY(300);
        g1Graph.addNode(vl1Dtrf1);
        g1Graph.addEdge(vl1Trf1, vl1Btrf1);
        g1Graph.addEdge(vl1Btrf1, vl1Dtrf1);
        g1Graph.addEdge(vl1Dtrf1, vl1Bbs1);

        Feeder3WTLegNode vl1Trf2 = Feeder3WTLegNode.createForSubstationDiagram(g1Graph, "vl1_trf2_one", "vl1_trf2", "vl1_trf2", Feeder3WTLegNode.Side.ONE);
        vl1Trf2.setOrder(2);
        vl1Trf2.setDirection(BusCell.Direction.TOP);
        vl1Trf2.setX(400);
        vl1Trf2.setY(80);
        g1Graph.addNode(vl1Trf2);
        SwitchNode vl1Btrf2 = new SwitchNode("vl1_btrf2", "vl1_btrf2", ComponentTypeName.BREAKER, false, g1Graph, SwitchNode.SwitchKind.BREAKER, false);
        vl1Btrf2.setX(400);
        vl1Btrf2.setY(180);
        g1Graph.addNode(vl1Btrf2);
        SwitchNode vl1Dtrf2 = new SwitchNode("vl1_dtrf2", "vl1_dtrf2", ComponentTypeName.DISCONNECTOR, false, g1Graph, SwitchNode.SwitchKind.DISCONNECTOR, false);
        vl1Dtrf2.setX(400);
        vl1Dtrf2.setY(300);
        g1Graph.addNode(vl1Dtrf2);
        g1Graph.addEdge(vl1Trf2, vl1Btrf2);
        g1Graph.addEdge(vl1Btrf2, vl1Dtrf2);
        g1Graph.addEdge(vl1Dtrf2, vl1Bbs2);

        // Second voltage level graph :
        //
        VoltageLevelInfos vl2Infos = new VoltageLevelInfos("vl2", "vl2", 225);
        VoltageLevelGraph g2Graph = VoltageLevelGraph.create(vl2Infos, false, true);
        g2Graph.setX(550);
        g2Graph.setY(20);

        BusNode vl2Bbs1 = BusNode.create(g2Graph, "vl2_bbs1", "vl2_bbs1");
        vl2Bbs1.setX(0);
        vl2Bbs1.setY(300);
        vl2Bbs1.setPxWidth(200);
        vl2Bbs1.setPxWidth(200);
        vl2Bbs1.setPosition(new Position(0, 1, 6, 0, null));
        g2Graph.addNode(vl2Bbs1);
        FeederNode vl2Gen1 = FeederInjectionNode.createGenerator(g2Graph, "vl2_gen1", "vl2_gen1");
        vl2Gen1.setOrder(0);
        vl2Gen1.setDirection(BusCell.Direction.TOP);
        vl2Gen1.setX(50);
        vl2Gen1.setY(80);
        g2Graph.addNode(vl2Gen1);
        SwitchNode vl2Bgen1 = new SwitchNode("vl2_bgen1", "vl2_bgen1", ComponentTypeName.BREAKER, false, g2Graph, SwitchNode.SwitchKind.BREAKER, false);
        vl2Bgen1.setX(50);
        vl2Bgen1.setY(180);
        g2Graph.addNode(vl2Bgen1);
        SwitchNode vl2Dgen1 = new SwitchNode("vl2_dgen1", "vl2_dgen1", ComponentTypeName.DISCONNECTOR, false, g2Graph, SwitchNode.SwitchKind.DISCONNECTOR, false);
        vl2Dgen1.setX(50);
        vl2Dgen1.setY(300);
        g2Graph.addNode(vl2Dgen1);
        g2Graph.addEdge(vl2Gen1, vl2Bgen1);
        g2Graph.addEdge(vl2Bgen1, vl2Dgen1);
        g2Graph.addEdge(vl2Dgen1, vl2Bbs1);

        Feeder2WTLegNode vl2Trf1 = Feeder2WTLegNode.createForSubstationDiagram(g2Graph, "vl2_trf1", "vl2_trf1", "vl2_trf1", FeederBranchNode.Side.ONE);
        vl2Trf1.setOrder(1);
        vl2Trf1.setDirection(BusCell.Direction.BOTTOM);
        vl2Trf1.setX(100);
        vl2Trf1.setY(500);
        g2Graph.addNode(vl2Trf1);
        SwitchNode vl2Btrf1 = new SwitchNode("vl2_btrf1", "vl2_btrf1", ComponentTypeName.BREAKER, false, g2Graph, SwitchNode.SwitchKind.BREAKER, false);
        vl2Btrf1.setX(100);
        vl2Btrf1.setY(400);
        g2Graph.addNode(vl2Btrf1);
        SwitchNode vl2Dtrf1 = new SwitchNode("vl2_dtrf1", "vl2_dtrf1", ComponentTypeName.DISCONNECTOR, false, g2Graph, SwitchNode.SwitchKind.DISCONNECTOR, false);
        vl2Dtrf1.setX(100);
        vl2Dtrf1.setY(300);
        g2Graph.addNode(vl2Dtrf1);
        g2Graph.addEdge(vl2Trf1, vl2Btrf1);
        g2Graph.addEdge(vl2Btrf1, vl2Dtrf1);
        g2Graph.addEdge(vl2Dtrf1, vl2Bbs1);

        Feeder3WTLegNode vl2Trf2 = Feeder3WTLegNode.createForSubstationDiagram(g2Graph, "vl2_trf2_one", "vl2_trf2", "vl2_trf2", Feeder3WTLegNode.Side.TWO);
        vl2Trf2.setOrder(2);
        vl2Trf2.setDirection(BusCell.Direction.TOP);
        vl2Trf2.setX(160);
        vl2Trf2.setY(80);
        g2Graph.addNode(vl2Trf2);
        SwitchNode vl2Btrf2 = new SwitchNode("vl2_btrf2", "vl2_btrf2", ComponentTypeName.BREAKER, false, g2Graph, SwitchNode.SwitchKind.BREAKER, false);
        vl2Btrf2.setX(160);
        vl2Btrf2.setY(180);
        g2Graph.addNode(vl2Btrf2);
        SwitchNode vl2Dtrf2 = new SwitchNode("vl2_dtrf2", "vl2_dtrf2", ComponentTypeName.DISCONNECTOR, false, g2Graph, SwitchNode.SwitchKind.DISCONNECTOR, false);
        vl2Dtrf2.setX(160);
        vl2Dtrf2.setY(300);
        g2Graph.addNode(vl2Dtrf2);
        g2Graph.addEdge(vl2Trf2, vl2Btrf2);
        g2Graph.addEdge(vl2Btrf2, vl2Dtrf2);
        g2Graph.addEdge(vl2Dtrf2, vl2Bbs1);

        // Third voltage level graph :
        //
        VoltageLevelInfos vl3Infos = new VoltageLevelInfos("vl3", "vl3", 63);
        VoltageLevelGraph g3Graph = VoltageLevelGraph.create(vl3Infos, false, true);
        g3Graph.setX(850);
        g3Graph.setY(20);

        BusNode vl3Bbs1 = BusNode.create(g3Graph, "vl3_bbs1", "vl3_bbs1");
        vl3Bbs1.setX(0);
        vl3Bbs1.setY(300);
        vl3Bbs1.setPxWidth(200);
        vl3Bbs1.setPosition(new Position(0, 1, 6, 0, null));
        g3Graph.addNode(vl3Bbs1);
        FeederNode vl3Capa1 = FeederInjectionNode.createCapacitor(g3Graph, "vl3_capa1", "vl3_capa1");
        vl3Capa1.setOrder(0);
        vl3Capa1.setDirection(BusCell.Direction.TOP);
        vl3Capa1.setX(40);
        vl3Capa1.setY(80);
        g3Graph.addNode(vl3Capa1);
        SwitchNode vl3Bcapa1 = new SwitchNode("vl3_bcapa1", "vl3_bcapa1", ComponentTypeName.BREAKER, false, g3Graph, SwitchNode.SwitchKind.BREAKER, false);
        vl3Bcapa1.setX(40);
        vl3Bcapa1.setY(180);
        g3Graph.addNode(vl3Bcapa1);
        SwitchNode vl3Dcapa1 = new SwitchNode("vl3_dcapa1", "vl3_dcapa1", ComponentTypeName.DISCONNECTOR, false, g3Graph, SwitchNode.SwitchKind.DISCONNECTOR, false);
        vl3Dcapa1.setX(40);
        vl3Dcapa1.setY(300);
        g3Graph.addNode(vl3Dcapa1);
        g3Graph.addEdge(vl3Capa1, vl3Bcapa1);
        g3Graph.addEdge(vl3Bcapa1, vl3Dcapa1);
        g3Graph.addEdge(vl3Dcapa1, vl3Bbs1);

        Feeder3WTLegNode vl3Trf2 = Feeder3WTLegNode.createForSubstationDiagram(g3Graph, "vl3_trf2_one", "vl3_trf2", "vl3_trf2", Feeder3WTLegNode.Side.THREE);
        vl3Trf2.setOrder(1);
        vl3Trf2.setDirection(BusCell.Direction.TOP);
        vl3Trf2.setX(150);
        vl3Trf2.setY(80);
        g3Graph.addNode(vl3Trf2);
        SwitchNode vl3Btrf2 = new SwitchNode("vl3_btrf2", "vl3_btrf2", ComponentTypeName.BREAKER, false, g3Graph, SwitchNode.SwitchKind.BREAKER, false);
        vl3Btrf2.setX(150);
        vl3Btrf2.setY(180);
        g3Graph.addNode(vl3Btrf2);
        SwitchNode vl3Dtrf2 = new SwitchNode("vl3_dtrf2", "vl3_dtrf2", ComponentTypeName.DISCONNECTOR, false, g3Graph, SwitchNode.SwitchKind.DISCONNECTOR, false);
        vl3Dtrf2.setX(150);
        vl3Dtrf2.setY(300);
        g3Graph.addNode(vl3Dtrf2);
        g3Graph.addEdge(vl3Trf2, vl3Btrf2);
        g3Graph.addEdge(vl3Btrf2, vl3Dtrf2);
        g3Graph.addEdge(vl3Dtrf2, vl3Bbs1);

        // Substation graph :
        //
        substG = SubstationGraph.create("subst");
        substG.addNode(g1Graph);
        substG.addNode(g2Graph);
        substG.addNode(g3Graph);
        Node nMulti1 = new Middle2WTNode(null, vl1Trf1.getId() + "_" + vl2Trf1.getId(), vl1Infos, vl2Infos);
        nMulti1.setX(365., false);
        nMulti1.setY(550., false);
        TwtEdge edge1 = substG.addEdge(vl1Trf1, nMulti1);
        edge1.setSnakeLine(Arrays.asList(80., 500., 80., 550., 365., 550.));
        TwtEdge edge2 = substG.addEdge(nMulti1, vl2Trf1);
        edge2.setSnakeLine(Arrays.asList(365., 550., 650., 550., 650., 500.));
        nMulti1.addAdjacentEdge(edge1);
        nMulti1.addAdjacentEdge(edge2);
        substG.addMultiTermNode(nMulti1);

        Node nMulti3 = new Middle3WTNode(null, vl1Trf2.getId() + "_" + vl2Trf2.getId() + "_" + vl3Trf2.getId(), vl1Infos, vl2Infos, vl3Infos);
        nMulti3.setX(710., false);
        nMulti3.setY(50., false);
        TwtEdge edge21 = substG.addEdge(vl1Trf2, nMulti3);
        edge21.setSnakeLine(Arrays.asList(400., 80., 400., 50., 710., 50.));
        TwtEdge edge22 = substG.addEdge(nMulti3, vl2Trf2);
        edge22.setSnakeLine(Arrays.asList(710., 50., 710., 80.));
        TwtEdge edge23 = substG.addEdge(nMulti3, vl3Trf2);
        edge23.setSnakeLine(Arrays.asList(710., 50., 1000., 50., 1000., 80.));

        nMulti3.addAdjacentEdge(edge21);
        nMulti3.addAdjacentEdge(edge22);
        nMulti3.addAdjacentEdge(edge23);
        substG.addMultiTermNode(nMulti3);
    }

    private void createZoneGraph() {
        VoltageLevelInfos vl11Infos = new VoltageLevelInfos(VOLTAGE_LEVEL_11_ID, VOLTAGE_LEVEL_11_ID, VOLTAGE_LEVEL_11_V);
        VoltageLevelInfos vl12Infos = new VoltageLevelInfos(VOLTAGE_LEVEL_12_ID, VOLTAGE_LEVEL_12_ID, VOLTAGE_LEVEL_12_V);
        VoltageLevelInfos vl21Infos = new VoltageLevelInfos(VOLTAGE_LEVEL_21_ID, VOLTAGE_LEVEL_21_ID, VOLTAGE_LEVEL_21_V);

        // create first voltage level graph
        VoltageLevelGraph vl11Graph = VoltageLevelGraph.create(vl11Infos, false, false);
        BusNode bus11Node = BusNode.create(vl11Graph, BUS_11_ID, BUS_11_ID);
        bus11Node.setX(30);
        bus11Node.setY(200);
        bus11Node.setPxWidth(40);
        vl11Graph.addNode(bus11Node);
        FeederNode loadNode = FeederInjectionNode.createLoad(vl11Graph, LOAD_ID, LOAD_ID);
        loadNode.setX(50);
        loadNode.setY(50);
        vl11Graph.addNode(loadNode);
        Feeder2WTLegNode twtSide1Node = Feeder2WTLegNode.createForVoltageLevelDiagram(vl11Graph, TRANSFORMER_ID + "_" + Side.ONE, TRANSFORMER_ID, TRANSFORMER_ID, FeederBranchNode.Side.ONE, vl12Infos);
        twtSide1Node.setX(50);
        twtSide1Node.setY(300);
        vl11Graph.addNode(twtSide1Node);
        vl11Graph.addEdge(bus11Node, loadNode);
        vl11Graph.addEdge(bus11Node, twtSide1Node);
        // create second voltage level graph
        VoltageLevelGraph vl12Graph = VoltageLevelGraph.create(vl12Infos, false, false);
        BusNode bus12Node = BusNode.create(vl12Graph, BUS_12_ID, BUS_12_ID);
        bus12Node.setX(30);
        bus12Node.setY(500);
        bus12Node.setPxWidth(40);
        vl12Graph.addNode(bus12Node);
        Feeder2WTLegNode twtSide2Node = Feeder2WTLegNode.createForVoltageLevelDiagram(vl12Graph, TRANSFORMER_ID + "_" + Side.TWO, TRANSFORMER_ID, TRANSFORMER_ID, FeederBranchNode.Side.TWO, vl11Infos);
        twtSide2Node.setX(50);
        twtSide2Node.setY(400);
        vl12Graph.addNode(twtSide2Node);
        FeederLineNode lineSide1Node = FeederLineNode.create(vl12Graph, LINE_ID + "_" + Side.ONE, LINE_ID, LINE_ID, FeederBranchNode.Side.ONE, vl21Infos);
        lineSide1Node.setX(50);
        lineSide1Node.setY(650);
        vl12Graph.addNode(lineSide1Node);
        vl12Graph.addEdge(bus12Node, twtSide2Node);
        vl12Graph.addEdge(bus12Node, lineSide1Node);
        // create third voltage level graph
        VoltageLevelGraph vl21Graph = VoltageLevelGraph.create(vl21Infos, false, false);
        BusNode bus21Node = BusNode.create(vl21Graph, BUS_21_ID, BUS_21_ID);
        bus21Node.setX(130);
        bus21Node.setY(1100);
        bus21Node.setPxWidth(40);
        vl21Graph.addNode(bus21Node);
        FeederNode genNode = FeederInjectionNode.createGenerator(vl21Graph, GENERATOR_ID, GENERATOR_ID);
        genNode.setX(150);
        genNode.setY(1250);
        vl21Graph.addNode(genNode);
        FeederLineNode lineSide2Node = FeederLineNode.create(vl21Graph, LINE_ID + "_" + Side.TWO, LINE_ID, LINE_ID, FeederBranchNode.Side.TWO, vl12Infos);
        lineSide2Node.setX(150);
        lineSide2Node.setY(950);
        vl21Graph.addNode(lineSide2Node);
        vl21Graph.addEdge(bus21Node, genNode);
        vl21Graph.addEdge(bus21Node, lineSide2Node);
        // create first substation graph
        SubstationGraph s1Graph = SubstationGraph.create(SUBSTATION_1_ID);
        s1Graph.addNode(vl11Graph);
        s1Graph.addNode(vl12Graph);
        twtSide1Node.setLabel(TRANSFORMER_ID);
        twtSide2Node.setLabel(TRANSFORMER_ID);
        Node nMulti1 = new Middle2WTNode(null, twtSide1Node.getId() + "_" + twtSide2Node.getId(), vl12Infos, vl11Infos);
        nMulti1.setX(50, false);
        nMulti1.setY(350, false);
        TwtEdge edge1 = s1Graph.addEdge(twtSide1Node, nMulti1);
        edge1.setSnakeLine(Arrays.asList(50., 300., 50., 320., 50., 350.));
        TwtEdge edge2 = s1Graph.addEdge(nMulti1, twtSide2Node);
        edge2.setSnakeLine(Arrays.asList(50., 350., 50., 380., 50., 400.));
        nMulti1.addAdjacentEdge(edge1);
        nMulti1.addAdjacentEdge(edge2);
        s1Graph.addMultiTermNode(nMulti1);

        // create second substation graph
        SubstationGraph s2Graph = SubstationGraph.create(SUBSTATION_2_ID);
        s2Graph.addNode(vl21Graph);
        // create zone graph
        zGraph = ZoneGraph.create(Arrays.asList(SUBSTATION_1_ID, SUBSTATION_2_ID));
        zGraph.addNode(s1Graph);
        zGraph.addNode(s2Graph);
        zGraph.addLineEdge(LINE_ID, lineSide1Node, lineSide2Node);
        zGraph.getLineEdge(LINE_ID).addPoint(50, 650);
        zGraph.getLineEdge(LINE_ID).addPoint(50, 800);
        zGraph.getLineEdge(LINE_ID).addPoint(150, 800);
        zGraph.getLineEdge(LINE_ID).addPoint(150, 950);
    }

    @Before
    public void setUp() {
        createVoltageLevelGraphs();
        createSubstationGraph();
        createZoneGraph();

        // Layout parameters :
        layoutParameters = createDefaultLayoutParameters()
            .setCellWidth(80);

        // initValueProvider example for the test :
        //
        initValueProvider = new DefaultDiagramLabelProvider(Network.create("empty", ""), componentLibrary, layoutParameters) {
            @Override
            public InitialValue getInitialValue(Node node) {
                InitialValue initialValue;
                if (node.getType() == Node.NodeType.BUS) {
                    initialValue = new InitialValue(null, null, node.getLabel(), null, null, null);
                } else {
                    initialValue = new InitialValue(Direction.UP, Direction.DOWN, "10", "20", null, null);
                }
                return initialValue;
            }

            @Override
            public List<DiagramLabelProvider.NodeDecorator> getNodeDecorators(Node node) {
                return new ArrayList<>();
            }
        };

        // no feeder value provider example for the test :
        //
        noFeederValueProvider = new DefaultDiagramLabelProvider(Network.create("empty", ""), componentLibrary, layoutParameters) {
            @Override
            public InitialValue getInitialValue(Node node) {
                InitialValue initialValue;
                if (node.getType() == Node.NodeType.BUS) {
                    initialValue = new InitialValue(null, null, null, null, null, null);
                } else {
                    initialValue = new InitialValue(Direction.UP, Direction.DOWN, null, null, null, null);
                }
                return initialValue;
            }

            @Override
            public List<DiagramLabelProvider.NodeDecorator> getNodeDecorators(Node node) {
                return new ArrayList<>();
            }
        };
    }

    @Test
    public void testVl1() {
        assertEquals(toString("/vl1.svg"),
            toSVG(g1, "/vl1.svg", getLayoutParameters(), initValueProvider, new DefaultDiagramStyleProvider()));
    }

    @Test
    public void testVl1CssExternal() {
        assertEquals(toString("/vl1_external_css.svg"),
            toSVG(g1, "/vl1_external_css.svg", getLayoutParameters().setCssInternal(false), initValueProvider, new DefaultDiagramStyleProvider()));
    }

    @Test
    public void testVl2() {
        assertEquals(toString("/vl2.svg"),
            toSVG(g2, "/vl2.svg", getLayoutParameters(), initValueProvider, new DefaultDiagramStyleProvider()));
    }

    @Test
    public void testVl3() {
        assertEquals(toString("/vl3.svg"),
            toSVG(g3, "/vl3.svg", getLayoutParameters(), initValueProvider, new DefaultDiagramStyleProvider()));
    }

    @Test
    public void testSubstation() {
        // SVG file generation for substation and comparison to reference
        assertEquals(toString("/substation.svg"),
            toSVG(substG, "/substation.svg", getLayoutParameters(), initValueProvider, new DefaultDiagramStyleProvider()));
    }

    @Test
    public void testSubstationArrowSymmetry() {
        // SVG file generation for substation with symmetric feeder arrow and comparison to reference
        getLayoutParameters().setFeederArrowSymmetry(true);
        assertEquals(toString("/substation_feeder_arrow_symmetry.svg"),
            toSVG(substG, "/substation_feeder_arrow_symmetry.svg", getLayoutParameters(), initValueProvider, new DefaultDiagramStyleProvider()));
    }

    @Test
    public void testSubstationNoFeederValues() {
        // SVG file generation for substation and comparison to reference but with no feeder values
        assertEquals(toString("/substation_no_feeder_values.svg"),
            toSVG(substG, "/substation_no_feeder_values.svg", getLayoutParameters(), noFeederValueProvider, new DefaultDiagramStyleProvider()));
    }

    @Test
    public void testVl1Optimized() {
        // Same tests than before, with optimized svg
        getLayoutParameters().setAvoidSVGComponentsDuplication(true);
        assertEquals(toString("/vl1_optimized.svg"),
            toSVG(g1, "/vl1_optimized.svg", getLayoutParameters(), initValueProvider, new DefaultDiagramStyleProvider()));
    }

    @Test
    public void testVl2Optimized() {
        // Same tests than before, with optimized svg
        getLayoutParameters().setAvoidSVGComponentsDuplication(true);
        assertEquals(toString("/vl2_optimized.svg"),
            toSVG(g2, "/vl2_optimized.svg", getLayoutParameters(), initValueProvider, new DefaultDiagramStyleProvider()));
    }

    @Test
    public void testVl3Optimized() {
        // Same tests than before, with optimized svg
        getLayoutParameters().setAvoidSVGComponentsDuplication(true);
        assertEquals(toString("/vl3_optimized.svg"),
            toSVG(g3, "/vl3_optimized.svg", getLayoutParameters(), initValueProvider, new DefaultDiagramStyleProvider()));
    }

    @Test
    public void testSubstationOptimized() {
        // Same tests than before, with optimized svg
        getLayoutParameters().setAvoidSVGComponentsDuplication(true);
        assertEquals(toString("/substation_optimized.svg"),
            toSVG(substG, "/substation_optimized.svg", getLayoutParameters(), initValueProvider, new DefaultDiagramStyleProvider()));
    }

    @Test
    public void testWriteZone() {
        getLayoutParameters().setShowGrid(false);
        assertEquals(toString("/zone.svg"),
            toSVG(zGraph, "/zone.svg", getLayoutParameters(), initValueProvider, new DefaultDiagramStyleProvider()));
    }

    @Test
    public void testStraightWires() {
        DiagramStyleProvider styleProvider = new DefaultDiagramStyleProvider();
        getLayoutParameters().setDrawStraightWires(true);
        assertEquals(toString("/vl1_straightWires.svg"),
            toSVG(g1, "/vl1_straightWires.svg", getLayoutParameters(), initValueProvider, styleProvider));
    }

    @Test
    public void testTooltip() {
        DiagramStyleProvider styleProvider = new DefaultDiagramStyleProvider();
        getLayoutParameters()
            .setTooltipEnabled(true)
            .setAvoidSVGComponentsDuplication(true);
        assertEquals(toString("/vl1_tooltip_opt.svg"),
            toSVG(g1, "/vl1_tooltip_opt.svg", getLayoutParameters(), initValueProvider, styleProvider));
    }

}
