/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.powsybl.sld.model.Feeder3WTNode;
import com.powsybl.sld.model.FictitiousNode;
import com.powsybl.sld.model.TwtEdge;
import org.junit.Before;
import org.junit.Test;

import com.powsybl.iidm.network.Branch.Side;
import com.powsybl.sld.layout.LayoutParameters;
import com.powsybl.sld.library.ComponentTypeName;
import com.powsybl.sld.model.BusCell;
import com.powsybl.sld.model.BusNode;
import com.powsybl.sld.model.Feeder2WTNode;
import com.powsybl.sld.model.FeederLineNode;
import com.powsybl.sld.model.FeederNode;
import com.powsybl.sld.model.Fictitious3WTNode;
import com.powsybl.sld.model.Graph;
import com.powsybl.sld.model.Node;
import com.powsybl.sld.model.Position;
import com.powsybl.sld.model.SubstationGraph;
import com.powsybl.sld.model.SwitchNode;
import com.powsybl.sld.model.ZoneGraph;
import com.powsybl.sld.svg.DefaultDiagramStyleProvider;
import com.powsybl.sld.svg.DiagramInitialValueProvider;
import com.powsybl.sld.svg.DiagramStyleProvider;
import com.powsybl.sld.svg.InitialValue;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class TestSVGWriter extends AbstractTestCase {

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

    private Graph g1;
    private Graph g2;
    private Graph g3;
    private SubstationGraph substG;
    private DiagramInitialValueProvider initValueProvider;
    private DiagramInitialValueProvider noFeederValueProvider;
    private ZoneGraph zGraph;

    private void createVoltageLevelGraphs() {
        // Creation "by hand" (without any network) of 3 voltage level graphs
        // and then generation of a SVG with DefaultDiagramStyleProvider (no network necessary)
        //

        // First voltage level graph :
        //
        g1 = Graph.create("vl1", "vl1", 400, "subst", false, true, false);
        g1.setX(0);
        g1.setY(20);

        Map<Feeder3WTNode.Side, String> idsLegs = new EnumMap<>(Feeder3WTNode.Side.class);
        Map<Feeder3WTNode.Side, Double> vNomsLegs = new EnumMap<>(Feeder3WTNode.Side.class);

        idsLegs.put(Feeder3WTNode.Side.ONE, "vl1");
        idsLegs.put(Feeder3WTNode.Side.TWO, "vl2");
        idsLegs.put(Feeder3WTNode.Side.THREE, "vl3");

        vNomsLegs.put(Feeder3WTNode.Side.ONE, 400.);
        vNomsLegs.put(Feeder3WTNode.Side.TWO, 225.);
        vNomsLegs.put(Feeder3WTNode.Side.THREE, 63.);

        BusNode vl1Bbs1 = BusNode.create(g1, "vl1_bbs1", "vl1_bbs1");
        vl1Bbs1.setX(0);
        vl1Bbs1.setY(300);
        vl1Bbs1.setPxWidth(200);
        vl1Bbs1.setPosition(new Position(0, 1, 2, 0, false, null));
        g1.addNode(vl1Bbs1);
        BusNode vl1Bbs2 = BusNode.create(g1, "vl1_bbs2", "vl1_bbs2");
        vl1Bbs2.setX(280);
        vl1Bbs2.setY(300);
        vl1Bbs2.setPxWidth(200);
        vl1Bbs2.setPosition(new Position(3, 1, 3, 0, false, null));
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

        FeederNode vl1Load1 = new FeederNode("vl1_load1", "vl1_load1", ComponentTypeName.LOAD, false, g1);
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

        Feeder2WTNode vl1Trf1 = new Feeder2WTNode("vl1_trf1", "vl1_trf1", ComponentTypeName.TWO_WINDINGS_TRANSFORMER, false, g1, "vl2", 225);
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

        Feeder2WTNode vl1Trf2One = new Feeder2WTNode("vl1_trf2_one", "vl1_trf2_one", ComponentTypeName.LINE, false, g1, "vl2", 225);
        vl1Trf2One.setOrder(2);
        vl1Trf2One.setDirection(BusCell.Direction.TOP);
        vl1Trf2One.setX(360);
        vl1Trf2One.setY(80);
        g1.addNode(vl1Trf2One);
        Feeder2WTNode vl1Trf2Two = new Feeder2WTNode("vl1_trf2_two", "vl1_trf2_two", ComponentTypeName.LINE, false, g1, "vl3", 63);
        vl1Trf2Two.setOrder(3);
        vl1Trf2Two.setDirection(BusCell.Direction.TOP);
        vl1Trf2Two.setX(440);
        vl1Trf2Two.setY(80);
        g1.addNode(vl1Trf2Two);
        Fictitious3WTNode vl1Trf2Fict = new Fictitious3WTNode(g1, "vl1_trf2", idsLegs, vNomsLegs);
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

        // Second voltage level graph :
        //
        g2 = Graph.create("vl2", "vl2", 225, "subst", false, true, false);
        g2.setX(550);
        g2.setY(20);

        BusNode vl2Bbs1 = BusNode.create(g2, "vl2_bbs1", "vl2_bbs1");
        vl2Bbs1.setX(0);
        vl2Bbs1.setY(300);
        vl2Bbs1.setPxWidth(200);
        vl2Bbs1.setPxWidth(200);
        vl2Bbs1.setPosition(new Position(0, 1, 3, 0, false, null));
        g2.addNode(vl2Bbs1);
        FeederNode vl2Gen1 = new FeederNode("vl2_gen1", "vl2_gen1", ComponentTypeName.GENERATOR, false, g2);
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

        Feeder2WTNode vl2Trf1 = new Feeder2WTNode("vl2_trf1", "vl2_trf1", ComponentTypeName.TWO_WINDINGS_TRANSFORMER, false, g2, "vl1", 400);
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

        Feeder2WTNode vl2Trf2One = new Feeder2WTNode("vl2_trf2_one", "vl2_trf2_one", ComponentTypeName.LINE, false, g2, "vl1", 400);
        vl2Trf2One.setOrder(2);
        vl2Trf2One.setDirection(BusCell.Direction.TOP);
        vl2Trf2One.setX(130);
        vl2Trf2One.setY(80);
        g2.addNode(vl2Trf2One);
        Feeder2WTNode vl2Trf2Two = new Feeder2WTNode("vl2_trf2_two", "vl2_trf2_two", ComponentTypeName.LINE, false, g2, "vl3", 63);
        vl2Trf2Two.setOrder(3);
        vl2Trf2Two.setDirection(BusCell.Direction.TOP);
        vl2Trf2Two.setX(190);
        vl2Trf2Two.setY(80);
        g2.addNode(vl2Trf2Two);
        Fictitious3WTNode vl2Trf2Fict = new Fictitious3WTNode(g2, "vl2_trf2", idsLegs, vNomsLegs);
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

        // Third voltage level graph :
        //
        g3 = Graph.create("vl3", "vl3", 63, "subst", false, true, false);
        g3.setX(850);
        g3.setY(20);

        BusNode vl3Bbs1 = BusNode.create(g3, "vl3_bbs1", "vl3_bbs1");
        vl3Bbs1.setX(0);
        vl3Bbs1.setY(300);
        vl3Bbs1.setPxWidth(200);
        vl3Bbs1.setPosition(new Position(0, 1, 3, 0, false, null));
        g3.addNode(vl3Bbs1);
        FeederNode vl3Capa1 = new FeederNode("vl3_capa1", "vl3_capa1", ComponentTypeName.CAPACITOR, false, g3);
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

        Feeder2WTNode vl3Trf2One = new Feeder2WTNode("vl3_trf2_one", "vl3_trf2_one", ComponentTypeName.LINE, false, g3, "vl1", 400);
        vl3Trf2One.setOrder(1);
        vl3Trf2One.setDirection(BusCell.Direction.TOP);
        vl3Trf2One.setX(110);
        vl3Trf2One.setY(80);
        g3.addNode(vl3Trf2One);
        Feeder2WTNode vl3Trf2Two = new Feeder2WTNode("vl3_trf2_two", "vl3_trf2_two", ComponentTypeName.LINE, false, g3, "vl2", 225);
        vl3Trf2Two.setOrder(2);
        vl3Trf2Two.setDirection(BusCell.Direction.TOP);
        vl3Trf2Two.setX(190);
        vl3Trf2Two.setY(80);
        g3.addNode(vl3Trf2Two);
        Fictitious3WTNode vl3Trf2Fict = new Fictitious3WTNode(g3, "vl3_trf2", idsLegs, vNomsLegs);
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
    }

    private void createSubstationGraph() {
        // Creation "by hand" (without any network) of 3 voltage level graphs and one substation graph
        // and then generation of a SVG with DefaultDiagramStyleProvider (no network necessary)
        //

        // First voltage level graph :
        //
        Graph g1Graph = Graph.create("vl1", "vl1", 400, "subst", false, true, false);
        g1Graph.setX(0);
        g1Graph.setY(20);

        BusNode vl1Bbs1 = BusNode.create(g1Graph, "vl1_bbs1", "vl1_bbs1");
        vl1Bbs1.setX(0);
        vl1Bbs1.setY(300);
        vl1Bbs1.setPxWidth(200);
        vl1Bbs1.setPosition(new Position(0, 1, 2, 0, false, null));
        g1Graph.addNode(vl1Bbs1);
        BusNode vl1Bbs2 = BusNode.create(g1Graph, "vl1_bbs2", "vl1_bbs2");
        vl1Bbs2.setX(280);
        vl1Bbs2.setY(300);
        vl1Bbs2.setPxWidth(200);
        vl1Bbs2.setPosition(new Position(3, 1, 3, 0, false, null));
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

        FeederNode vl1Load1 = new FeederNode("vl1_load1", "vl1_load1", ComponentTypeName.LOAD, false, g1Graph);
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

        Feeder2WTNode vl1Trf1 = new Feeder2WTNode("vl1_trf1", "vl1_trf1", ComponentTypeName.LINE, false, g1Graph, "vl2", 225);
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

        Feeder3WTNode vl1Trf2 = new Feeder3WTNode("vl1_trf2_one", "vl1_trf2_one", ComponentTypeName.LINE, false, g1Graph, "trf2", Feeder3WTNode.Side.ONE);
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
        Graph g2Graph = Graph.create("vl2", "vl2", 225, "subst", false, true, false);
        g2Graph.setX(550);
        g2Graph.setY(20);

        BusNode vl2Bbs1 = BusNode.create(g2Graph, "vl2_bbs1", "vl2_bbs1");
        vl2Bbs1.setX(0);
        vl2Bbs1.setY(300);
        vl2Bbs1.setPxWidth(200);
        vl2Bbs1.setPxWidth(200);
        vl2Bbs1.setPosition(new Position(0, 1, 3, 0, false, null));
        g2Graph.addNode(vl2Bbs1);
        FeederNode vl2Gen1 = new FeederNode("vl2_gen1", "vl2_gen1", ComponentTypeName.GENERATOR, false, g2Graph);
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

        Feeder2WTNode vl2Trf1 = new Feeder2WTNode("vl2_trf1", "vl2_trf1", ComponentTypeName.LINE, false, g2Graph, "vl1", 400);
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

        Feeder3WTNode vl2Trf2 = new Feeder3WTNode("vl2_trf2_one", "vl2_trf2_one", ComponentTypeName.LINE, false, g2Graph, "vl1", Feeder3WTNode.Side.TWO);
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
        Graph g3Graph = Graph.create("vl3", "vl3", 63, "subst", false, true, false);
        g3Graph.setX(850);
        g3Graph.setY(20);

        BusNode vl3Bbs1 = BusNode.create(g3Graph, "vl3_bbs1", "vl3_bbs1");
        vl3Bbs1.setX(0);
        vl3Bbs1.setY(300);
        vl3Bbs1.setPxWidth(200);
        vl3Bbs1.setPosition(new Position(0, 1, 3, 0, false, null));
        g3Graph.addNode(vl3Bbs1);
        FeederNode vl3Capa1 = new FeederNode("vl3_capa1", "vl3_capa1", ComponentTypeName.CAPACITOR, false, g3Graph);
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

        Feeder3WTNode vl3Trf2 = new Feeder3WTNode("vl3_trf2_one", "vl3_trf2_one", ComponentTypeName.LINE, false, g3Graph, "vl1", Feeder3WTNode.Side.THREE);
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
        Node nMulti1 = new FictitiousNode(null, vl1Trf1.getId() + "_" + vl2Trf1.getId(), ComponentTypeName.TWO_WINDINGS_TRANSFORMER);
        nMulti1.setX(365., false, false);
        nMulti1.setY(550., false, false);
        TwtEdge edge1 = substG.addEdge(ComponentTypeName.TWO_WINDINGS_TRANSFORMER, vl1Trf1, nMulti1);
        edge1.setSnakeLine(Arrays.asList(80., 500., 80., 550., 365., 550.));
        TwtEdge edge2 = substG.addEdge(ComponentTypeName.TWO_WINDINGS_TRANSFORMER, nMulti1, vl2Trf1);
        edge2.setSnakeLine(Arrays.asList(365., 550., 650., 550., 650., 500.));
        nMulti1.addAdjacentEdge(edge1);
        nMulti1.addAdjacentEdge(edge2);
        substG.addMultiTermNode(nMulti1);

        Node nMulti3 = new FictitiousNode(null, vl1Trf2.getId() + "_" + vl2Trf2.getId() + "_" + vl3Trf2.getId(), ComponentTypeName.THREE_WINDINGS_TRANSFORMER);
        nMulti3.setX(710., false, false);
        nMulti3.setY(50., false, false);
        TwtEdge edge21 = substG.addEdge(ComponentTypeName.THREE_WINDINGS_TRANSFORMER, vl1Trf2, nMulti3);
        edge21.setSnakeLine(Arrays.asList(400., 80., 400., 50., 710., 50.));
        TwtEdge edge22 = substG.addEdge(ComponentTypeName.THREE_WINDINGS_TRANSFORMER, nMulti3, vl2Trf2);
        edge22.setSnakeLine(Arrays.asList(710., 50., 710., 80.));
        TwtEdge edge23 = substG.addEdge(ComponentTypeName.THREE_WINDINGS_TRANSFORMER, nMulti3, vl3Trf2);
        edge23.setSnakeLine(Arrays.asList(710., 50., 1000., 50., 1000., 80.));

        nMulti3.addAdjacentEdge(edge21);
        nMulti3.addAdjacentEdge(edge22);
        nMulti3.addAdjacentEdge(edge23);
        substG.addMultiTermNode(nMulti3);
    }

    private void createZoneGraph() {
      //create first voltage level graph
        Graph vl11Graph = Graph.create(VOLTAGE_LEVEL_11_ID, VOLTAGE_LEVEL_11_ID, VOLTAGE_LEVEL_11_V, SUBSTATION_1_ID, false, false, false);
        BusNode bus11Node = BusNode.create(vl11Graph, BUS_11_ID, BUS_11_ID);
        bus11Node.setX(30);
        bus11Node.setY(200);
        bus11Node.setPxWidth(40);
        vl11Graph.addNode(bus11Node);
        FeederNode loadNode = new FeederNode(LOAD_ID, LOAD_ID, ComponentTypeName.LOAD, false, vl11Graph);
        loadNode.setX(50);
        loadNode.setY(50);
        vl11Graph.addNode(loadNode);
        Feeder2WTNode twtSide1Node = new Feeder2WTNode(TRANSFORMER_ID + "_" + Side.ONE, TRANSFORMER_ID + "_" + Side.ONE, ComponentTypeName.LINE, false, vl11Graph, VOLTAGE_LEVEL_12_ID, VOLTAGE_LEVEL_12_V);
        twtSide1Node.setX(50);
        twtSide1Node.setY(300);
        vl11Graph.addNode(twtSide1Node);
        vl11Graph.addEdge(bus11Node, loadNode);
        vl11Graph.addEdge(bus11Node, twtSide1Node);
        // create second voltage level graph
        Graph vl12Graph = Graph.create(VOLTAGE_LEVEL_12_ID, VOLTAGE_LEVEL_12_ID, VOLTAGE_LEVEL_12_V, SUBSTATION_1_ID, false, false, false);
        BusNode bus12Node = BusNode.create(vl12Graph, BUS_12_ID, BUS_12_ID);
        bus12Node.setX(30);
        bus12Node.setY(500);
        bus12Node.setPxWidth(40);
        vl12Graph.addNode(bus12Node);
        Feeder2WTNode twtSide2Node = new Feeder2WTNode(TRANSFORMER_ID + "_" + Side.TWO, TRANSFORMER_ID + "_" + Side.TWO, ComponentTypeName.LINE, false, vl12Graph, VOLTAGE_LEVEL_11_ID, VOLTAGE_LEVEL_11_V);
        twtSide2Node.setX(50);
        twtSide2Node.setY(400);
        vl12Graph.addNode(twtSide2Node);
        FeederLineNode lineSide1Node = new FeederLineNode(LINE_ID + "_" + Side.ONE, LINE_ID + "_" + Side.ONE, ComponentTypeName.LINE, false, vl12Graph, VOLTAGE_LEVEL_21_ID, VOLTAGE_LEVEL_21_V);
        lineSide1Node.setX(50);
        lineSide1Node.setY(650);
        vl12Graph.addNode(lineSide1Node);
        vl12Graph.addEdge(bus12Node, twtSide2Node);
        vl12Graph.addEdge(bus12Node, lineSide1Node);
        // create third voltage level graph
        Graph vl21Graph = Graph.create(VOLTAGE_LEVEL_21_ID, VOLTAGE_LEVEL_21_ID, VOLTAGE_LEVEL_21_V, SUBSTATION_2_ID, false, false, false);
        BusNode bus21Node = BusNode.create(vl21Graph, BUS_21_ID, BUS_21_ID);
        bus21Node.setX(130);
        bus21Node.setY(1100);
        bus21Node.setPxWidth(40);
        vl21Graph.addNode(bus21Node);
        FeederNode genNode = new FeederNode(GENERATOR_ID, GENERATOR_ID, ComponentTypeName.GENERATOR, false, vl21Graph);
        genNode.setX(150);
        genNode.setY(1250);
        vl21Graph.addNode(genNode);
        FeederLineNode lineSide2Node = new FeederLineNode(LINE_ID + "_" + Side.TWO, LINE_ID + "_" + Side.TWO, ComponentTypeName.LINE, false, vl21Graph, VOLTAGE_LEVEL_12_ID, VOLTAGE_LEVEL_12_V);
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
        Node nMulti1 = new FictitiousNode(null, twtSide1Node.getId() + "_" + twtSide2Node.getId(), ComponentTypeName.TWO_WINDINGS_TRANSFORMER);
        nMulti1.setX(50, false, false);
        nMulti1.setY(350, false, false);
        TwtEdge edge1 = s1Graph.addEdge(ComponentTypeName.TWO_WINDINGS_TRANSFORMER, twtSide1Node, nMulti1);
        edge1.setSnakeLine(Arrays.asList(50., 300., 50., 320., 50., 350.));
        TwtEdge edge2 = s1Graph.addEdge(ComponentTypeName.TWO_WINDINGS_TRANSFORMER, nMulti1, twtSide2Node);
        edge2.setSnakeLine(Arrays.asList(50., 350., 50., 380., 50., 400.));
        nMulti1.addAdjacentEdge(edge1);
        nMulti1.addAdjacentEdge(edge2);
        s1Graph.addMultiTermNode(nMulti1);

        // create second substation graph
        SubstationGraph s2Graph = SubstationGraph.create(SUBSTATION_2_ID);
        s2Graph.addNode(vl21Graph);
        // create zone graph
        zGraph = ZoneGraph.create(ZoneId.create(Arrays.asList(SUBSTATION_1_ID, SUBSTATION_2_ID)));
        zGraph.addNode(s1Graph);
        zGraph.addNode(s2Graph);
        zGraph.addEdge(LINE_ID, lineSide1Node, lineSide2Node);
        zGraph.getEdge(LINE_ID).addPoint(50, 650);
        zGraph.getEdge(LINE_ID).addPoint(50, 800);
        zGraph.getEdge(LINE_ID).addPoint(150, 800);
        zGraph.getEdge(LINE_ID).addPoint(150, 950);
    }

    @Before
    public void setUp() {
        createVoltageLevelGraphs();
        createSubstationGraph();
        createZoneGraph();

        // initValueProvider example for the test :
        //
        initValueProvider = new DiagramInitialValueProvider() {
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
            public List<String> getNodeLabelValue(Node node) {
                List<String> res = new ArrayList<>();
                if (node instanceof FeederNode || node instanceof BusNode) {
                    res.add(node.getLabel());
                }
                return res;
            }
        };

        // no feeder value provider example for the test :
        //
        noFeederValueProvider = new DiagramInitialValueProvider() {
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
            public List<String> getNodeLabelValue(Node node) {
                List<String> res = new ArrayList<>();
                if (node instanceof FeederNode || node instanceof BusNode) {
                    res.add(node.getLabel());
                }
                return res;
            }
        };
    }

    @Test
    public void test() {

        DiagramStyleProvider styleProvider = new DefaultDiagramStyleProvider();

        // Layout parameters :
        //
        LayoutParameters layoutParameters = new LayoutParameters()
                .setTranslateX(20)
                .setTranslateY(50)
                .setInitialXBus(0)
                .setInitialYBus(260)
                .setVerticalSpaceBus(25)
                .setHorizontalBusPadding(20)
                .setCellWidth(80)
                .setExternCellHeight(250)
                .setInternCellHeight(40)
                .setStackHeight(30)
                .setShowGrid(true)
                .setShowInternalNodes(false)
                .setScaleFactor(1)
                .setHorizontalSubstationPadding(50)
                .setVerticalSubstationPadding(50)
                .setDrawStraightWires(false)
                .setHorizontalSnakeLinePadding(30)
                .setVerticalSnakeLinePadding(30)
                .setShowInductorFor3WT(true);

        Map<String, Graph> mapGr = new HashMap<>();
        mapGr.put("/vl1.svg", g1);
        mapGr.put("/vl2.svg", g2);
        mapGr.put("/vl3.svg", g3);

        for (String filename : mapGr.keySet()) {
            // SVG file generation first voltage level and comparison to reference :
            assertEquals(toSVG(mapGr.get(filename), filename, layoutParameters, initValueProvider, styleProvider), toString(filename));
        }

        // SVG file generation for substation and comparison to reference
        assertEquals(toSVG(substG, "/substation.svg", layoutParameters, initValueProvider, styleProvider), toString("/substation.svg"));

        // SVG file generation for substation and comparison to reference but with no feeder values
        assertEquals(toSVG(substG, "/substation_no_feeder_values.svg", layoutParameters, noFeederValueProvider, styleProvider), toString("/substation_no_feeder_values.svg"));

        // Same tests than before, with optimized svg :
        //
        LayoutParameters layoutParameters2 = new LayoutParameters(layoutParameters);
        layoutParameters2.setAvoidSVGComponentsDuplication(true);

        mapGr.clear();
        mapGr.put("/vl1_optimized.svg", g1);
        mapGr.put("/vl2_optimized.svg", g2);
        mapGr.put("/vl3_optimized.svg", g3);

        for (String filename : mapGr.keySet()) {
            // SVG file generation first voltage level and comparison to reference :
            assertEquals(toSVG(mapGr.get(filename), filename, layoutParameters2, initValueProvider, styleProvider), toString(filename));
        }

        // SVG file generation for substation and comparison to reference
        assertEquals(toSVG(substG, "/substation_optimized.svg", layoutParameters2, initValueProvider, styleProvider), toString("/substation_optimized.svg"));
    }

    @Test
    public void testWriteZone() {
        DiagramStyleProvider styleProvider = new DefaultDiagramStyleProvider();

        LayoutParameters layoutParameters = new LayoutParameters()
                .setTranslateX(20)
                .setTranslateY(50)
                .setInitialXBus(0)
                .setInitialYBus(260)
                .setVerticalSpaceBus(25)
                .setHorizontalBusPadding(20)
                .setCellWidth(80)
                .setExternCellHeight(250)
                .setInternCellHeight(40)
                .setStackHeight(30)
                .setShowGrid(false)
                .setShowInternalNodes(false)
                .setScaleFactor(1)
                .setHorizontalSubstationPadding(50)
                .setVerticalSubstationPadding(50)
                .setDrawStraightWires(false)
                .setHorizontalSnakeLinePadding(30)
                .setVerticalSnakeLinePadding(30)
                .setShowInductorFor3WT(true);

        assertEquals(toString("/zone.svg"), toSVG(zGraph, "/zone.svg", layoutParameters, initValueProvider, styleProvider));
    }

}
