/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.iidm;

import com.powsybl.iidm.network.*;
import com.powsybl.sld.NetworkGraphBuilder;
import com.powsybl.sld.SubstationDiagram;
import com.powsybl.sld.iidm.extensions.ConnectablePosition;
import com.powsybl.sld.layout.*;
import com.powsybl.sld.model.SubstationGraph;
import com.powsybl.sld.svg.DefaultDiagramLabelProvider;
import com.powsybl.sld.svg.DefaultDiagramStyleProvider;
import com.powsybl.sld.util.NominalVoltageDiagramStyleProvider;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class TestCase11SubstationGraph extends AbstractTestCaseIidm {

    protected LayoutParameters layoutParameters;

    @Override
    public LayoutParameters getLayoutParameters() {
        return layoutParameters;
    }

    @Before
    public void setUp() {
        layoutParameters = createDefaultLayoutParameters();

        network = Network.create("testCase11", "test");
        graphBuilder = new NetworkGraphBuilder(network);

        substation = createSubstation(network, "subst", "subst", Country.FR);

        // first voltage level
        //
        VoltageLevel vl1 = createVoltageLevel(substation, "vl1", "vl1", TopologyKind.NODE_BREAKER, 380, 50);

        createBusBarSection(vl1, "bbs1", "bbs1", 0, 1, 1);
        createBusBarSection(vl1, "bbs2", "bbs2", 1, 1, 2);
        createBusBarSection(vl1, "bbs3", "bbs3", 2, 2, 1);
        createBusBarSection(vl1, "bbs4", "bbs4", 3, 2, 2);

        createSwitch(vl1, "dsect11", "dsect11", SwitchKind.DISCONNECTOR, false, false, true, 0, 14);
        createSwitch(vl1, "dtrct11", "dtrct11", SwitchKind.BREAKER, true, false, true, 14, 15);
        createSwitch(vl1, "dsect12", "dsect12", SwitchKind.DISCONNECTOR, false, false, true, 15, 1);

        createSwitch(vl1, "dsect21", "dsect21", SwitchKind.DISCONNECTOR, false, false, true, 2, 16);
        createSwitch(vl1, "dtrct21", "dtrct21", SwitchKind.BREAKER, true, false, true, 16, 17);
        createSwitch(vl1, "dsect22", "dsect22", SwitchKind.DISCONNECTOR, false, false, true, 17, 3);

        createLoad(vl1, "load1", "load1", "load1", 0, ConnectablePosition.Direction.TOP, 4, 10, 10);
        createSwitch(vl1, "dload1", "dload1", SwitchKind.DISCONNECTOR, false, false, true, 0, 5);
        createSwitch(vl1, "bload1", "bload1", SwitchKind.BREAKER, true, false, true, 4, 5);

        createGenerator(vl1, "gen1", "gen1", "gen1", 2, ConnectablePosition.Direction.BOTTOM, 6, 0, 20, false, 10, 10);
        createSwitch(vl1, "dgen1", "dgen1", SwitchKind.DISCONNECTOR, false, false, true, 2, 7);
        createSwitch(vl1, "bgen1", "bgen1", SwitchKind.BREAKER, true, false, true, 6, 7);

        createLoad(vl1, "load2", "load2", "load2", 8, ConnectablePosition.Direction.TOP, 8, 10, 10);
        createSwitch(vl1, "dload2", "dload2", SwitchKind.DISCONNECTOR, false, false, true, 1, 9);
        createSwitch(vl1, "bload2", "bload2", SwitchKind.BREAKER, true, false, true, 8, 9);

        createGenerator(vl1, "gen2", "gen2", "gen2", 12, ConnectablePosition.Direction.BOTTOM, 10, 0, 20, false, 10, 10);
        createSwitch(vl1, "dgen2", "dgen2", SwitchKind.DISCONNECTOR, false, false, true, 3, 11);
        createSwitch(vl1, "bgen2", "bgen2", SwitchKind.BREAKER, true, false, true, 10, 11);

        // second voltage level
        //
        VoltageLevel vl2 = createVoltageLevel(substation, "vl2", "vl2", TopologyKind.NODE_BREAKER, 225, 30);

        createBusBarSection(vl2, "bbs5", "bbs5", 0, 1, 1);
        createBusBarSection(vl2, "bbs6", "bbs6", 1, 2, 1);

        createSwitch(vl2, "dscpl1", "dscpl1", SwitchKind.DISCONNECTOR, false, false, true, 0, 6);
        createSwitch(vl2, "ddcpl1", "ddcpl1", SwitchKind.BREAKER, true, false, true, 6, 7);
        createSwitch(vl2, "dscpl2", "dscpl2", SwitchKind.DISCONNECTOR, false, false, true, 7, 1);

        createLoad(vl2, "load3", "load3", "load3", 0, ConnectablePosition.Direction.TOP, 2, 10, 10);
        createSwitch(vl2, "dload3", "dload3", SwitchKind.DISCONNECTOR, false, false, true, 0, 3);
        createSwitch(vl2, "bload3", "bload3", SwitchKind.BREAKER, true, false, true, 2, 3);

        createGenerator(vl2, "gen4", "gen4", "gen4", 2, ConnectablePosition.Direction.BOTTOM, 4, 0, 20, false, 10, 10);
        createSwitch(vl2, "dgen4", "dgen4", SwitchKind.DISCONNECTOR, false, false, true, 1, 5);
        createSwitch(vl2, "bgen4", "bgen4", SwitchKind.BREAKER, true, false, true, 4, 5);

        // third voltage level
        //
        VoltageLevel vl3 = createVoltageLevel(substation, "vl3", "vl3", TopologyKind.NODE_BREAKER, 225, 20);

        createBusBarSection(vl3, "bbs7", "bbs7", 0, 1, 1);

        createLoad(vl3, "load4", "load4", "load4", 0, ConnectablePosition.Direction.TOP, 1, 10, 10);
        createSwitch(vl3, "dload4", "dload4", SwitchKind.DISCONNECTOR, false, false, true, 0, 2);
        createSwitch(vl3, "bload4", "bload4", SwitchKind.BREAKER, true, false, true, 2, 1);

        // two windings transformers between voltage levels
        //
        createSwitch(vl1, "dtrf11", "dtrf11", SwitchKind.DISCONNECTOR, false, false, true, 0, 18);
        createSwitch(vl1, "btrf11", "btrf11", SwitchKind.BREAKER, true, false, true, 18, 19);
        createSwitch(vl2, "dtrf21", "dtrf21", SwitchKind.DISCONNECTOR, false, false, true, 0, 8);
        createSwitch(vl2, "btrf21", "btrf21", SwitchKind.BREAKER, true, false, true, 8, 9);
        createTwoWindingsTransformer(substation, "trf1", "trf1", 2.0, 14.745, 0.0, 3.2E-5, 400.0, 225.0,
                19, 9, vl1.getId(), vl2.getId(),
                "trf1", 1, ConnectablePosition.Direction.TOP,
                "trf1", 1, ConnectablePosition.Direction.TOP);

        createSwitch(vl1, "dtrf12", "dtrf12", SwitchKind.DISCONNECTOR, false, false, true, 1, 20);
        createSwitch(vl1, "btrf12", "btrf12", SwitchKind.BREAKER, true, false, true, 20, 21);
        createSwitch(vl2, "dtrf22", "dtrf22", SwitchKind.DISCONNECTOR, false, false, true, 1, 10);
        createSwitch(vl2, "btrf22", "btrf22", SwitchKind.BREAKER, true, false, true, 10, 11);
        createTwoWindingsTransformer(substation, "trf2", "trf2", 2.0, 14.745, 0.0, 3.2E-5, 400.0, 225.0,
                21, 11, vl1.getId(), vl2.getId(),
                "trf2", 11, ConnectablePosition.Direction.TOP,
                "trf2", 7, ConnectablePosition.Direction.BOTTOM);

        createSwitch(vl1, "dtrf13", "dtrf13", SwitchKind.DISCONNECTOR, false, false, true, 2, 22);
        createSwitch(vl1, "btrf13", "btrf13", SwitchKind.BREAKER, true, false, true, 22, 23);
        createSwitch(vl2, "dtrf23", "dtrf23", SwitchKind.DISCONNECTOR, false, false, true, 1, 12);
        createSwitch(vl2, "btrf23", "btrf23", SwitchKind.BREAKER, true, false, true, 12, 13);
        createTwoWindingsTransformer(substation, "trf3", "trf3", 2.0, 14.745, 0.0, 3.2E-5, 400.0, 225.0,
                23, 13, vl1.getId(), vl2.getId(),
                "trf3", 3, ConnectablePosition.Direction.BOTTOM,
                "trf3", 8, ConnectablePosition.Direction.BOTTOM);

        createSwitch(vl1, "dtrf14", "dtrf14", SwitchKind.DISCONNECTOR, false, false, true, 3, 24);
        createSwitch(vl1, "btrf14", "btrf14", SwitchKind.BREAKER, true, false, true, 24, 25);
        createSwitch(vl2, "dtrf24", "dtrf24", SwitchKind.DISCONNECTOR, false, false, true, 0, 14);
        createSwitch(vl2, "btrf24", "btrf24", SwitchKind.BREAKER, true, false, true, 14, 15);
        createTwoWindingsTransformer(substation, "trf4", "trf4", 2.0, 14.745, 0.0, 3.2E-5, 400.0, 225.0,
                25, 15, vl1.getId(), vl2.getId(),
                "trf4", 10, ConnectablePosition.Direction.BOTTOM,
                "trf4", 3, ConnectablePosition.Direction.TOP);

        createSwitch(vl1, "dtrf15", "dtrf15", SwitchKind.DISCONNECTOR, false, false, true, 0, 26);
        createSwitch(vl1, "btrf15", "btrf15", SwitchKind.BREAKER, true, false, true, 26, 27);
        createSwitch(vl3, "dtrf25", "dtrf25", SwitchKind.DISCONNECTOR, false, false, true, 0, 3);
        createSwitch(vl3, "btrf25", "btrf25", SwitchKind.BREAKER, true, false, true, 3, 4);
        createTwoWindingsTransformer(substation, "trf5", "trf5", 2.0, 14.745, 0.0, 3.2E-5, 400.0, 225.0,
                27, 4, vl1.getId(), vl3.getId(),
                "trf5", 4, ConnectablePosition.Direction.TOP,
                "trf5", 1, ConnectablePosition.Direction.BOTTOM);

        // three windings transformers between voltage levels
        //
        createSwitch(vl1, "dtrf16", "dtrf16", SwitchKind.DISCONNECTOR, false, false, true, 0, 28);
        createSwitch(vl1, "btrf16", "btrf16", SwitchKind.BREAKER, true, false, true, 28, 29);
        createSwitch(vl2, "dtrf26", "dtrf26", SwitchKind.DISCONNECTOR, false, false, true, 1, 16);
        createSwitch(vl2, "btrf26", "btrf26", SwitchKind.BREAKER, true, false, true, 16, 17);
        createSwitch(vl3, "dtrf36", "dtrf36", SwitchKind.DISCONNECTOR, false, false, true, 0, 5);
        createSwitch(vl3, "btrf36", "btrf36", SwitchKind.BREAKER, true, false, true, 5, 6);

        createThreeWindingsTransformer(substation, "trf6", "trf6", vl1.getId(), vl2.getId(), vl3.getId(),
                0.5, 0.5, 0.5, 1., 1., 1., 0.1, 0.1,
                400., 225., 225.,
                29, 17, 6,
                "trf61", 5, ConnectablePosition.Direction.TOP,
                "trf62", 5, ConnectablePosition.Direction.TOP,
                "trf63", 2, ConnectablePosition.Direction.TOP);

        createSwitch(vl1, "dtrf17", "dtrf17", SwitchKind.DISCONNECTOR, false, false, true, 2, 30);
        createSwitch(vl1, "btrf17", "btrf17", SwitchKind.BREAKER, true, false, true, 30, 31);
        createSwitch(vl2, "dtrf27", "dtrf27", SwitchKind.DISCONNECTOR, false, false, true, 0, 18);
        createSwitch(vl2, "btrf27", "btrf27", SwitchKind.BREAKER, true, false, true, 18, 19);
        createSwitch(vl3, "dtrf37", "dtrf37", SwitchKind.DISCONNECTOR, false, false, true, 0, 7);
        createSwitch(vl3, "btrf37", "btrf37", SwitchKind.BREAKER, true, false, true, 7, 8);

        createThreeWindingsTransformer(substation, "trf7", "trf7", vl1.getId(), vl2.getId(), vl3.getId(),
                0.5, 0.5, 0.5, 1., 1., 1., 0.1, 0.1,
                400., 225., 225.,
                31, 19, 8,
                "trf71", 6, ConnectablePosition.Direction.BOTTOM,
                "trf72", 4, ConnectablePosition.Direction.TOP,
                "trf73", 3, ConnectablePosition.Direction.BOTTOM);

        createSwitch(vl1, "dtrf18", "dtrf18", SwitchKind.DISCONNECTOR, false, false, true, 1, 32);
        createSwitch(vl1, "btrf18", "btrf18", SwitchKind.BREAKER, true, false, true, 32, 33);
        createSwitch(vl2, "dtrf28", "dtrf28", SwitchKind.DISCONNECTOR, false, false, true, 1, 20);
        createSwitch(vl2, "btrf28", "btrf28", SwitchKind.BREAKER, true, false, true, 20, 21);
        createSwitch(vl3, "dtrf38", "dtrf38", SwitchKind.DISCONNECTOR, false, false, true, 0, 9);
        createSwitch(vl3, "btrf38", "btrf38", SwitchKind.BREAKER, true, false, true, 9, 10);

        createThreeWindingsTransformer(substation, "trf8", "trf8", vl1.getId(), vl2.getId(), vl3.getId(),
                0.5, 0.5, 0.5, 1., 1., 1., 0.1, 0.1,
                400., 225., 225.,
                33, 21, 10,
                "trf81", 9, ConnectablePosition.Direction.TOP,
                "trf82", 6, ConnectablePosition.Direction.BOTTOM,
                "trf83", 4, ConnectablePosition.Direction.TOP);

        // Creation of another substation, another voltageLevel and a line between the two substations
        //
        Substation substation2 = createSubstation(network, "subst2", "subst2", Country.FR);
        VoltageLevel vlSubst2 = createVoltageLevel(substation2, "vlSubst2", "vlSubst2", TopologyKind.NODE_BREAKER, 380, 50);

        createBusBarSection(vlSubst2, "bbs1_2", "bbs1_2", 0, 1, 1);

        createSwitch(vl1, "dline11_2", "dline11_2", SwitchKind.DISCONNECTOR, false, false, true, 0, 34);
        createSwitch(vl1, "bline11_2", "bline11_2", SwitchKind.BREAKER, true, false, true, 34, 35);
        createSwitch(vlSubst2, "dline21_2", "dline21_2", SwitchKind.DISCONNECTOR, false, false, true, 0, 1);
        createSwitch(vlSubst2, "bline21_2", "bline21_2", SwitchKind.BREAKER, true, false, true, 1, 2);
        createLine(network, "line1", "line1", 2.0, 14.745, 1.0, 1.0, 1.0, 1.0,
                35, 2, vl1.getId(), vlSubst2.getId(),
                "line1", 7, ConnectablePosition.Direction.TOP,
                "line1", 1, ConnectablePosition.Direction.TOP);
    }

    @Test
    public void testHorizontal() {

        // build substation graph
        SubstationGraph g = graphBuilder.buildSubstationGraph(substation.getId(), false);

        // write Json and compare to reference (with horizontal substation layout)
        new HorizontalSubstationLayoutFactory().create(g, new PositionVoltageLevelLayoutFactory()).run(getLayoutParameters());
        assertEquals(toString("/TestCase11SubstationGraphH.json"), toJson(g, "/TestCase11SubstationGraphH.json"));
    }

    @Test
    public void testVertical() {
        // build substation graph
        SubstationGraph g = graphBuilder.buildSubstationGraph(substation.getId(), false);

        // write Json and compare to reference (with vertical substation layout)
        new VerticalSubstationLayoutFactory().create(g, new PositionVoltageLevelLayoutFactory()).run(getLayoutParameters());
        assertEquals(toString("/TestCase11SubstationGraphV.json"), toJson(g, "/TestCase11SubstationGraphV.json"));
    }

    @Test
    public void testHorizontalDefaultStyle() {
        // compare metadata of substation diagram with reference
        // (with horizontal substation layout)
        SubstationDiagram diagram = SubstationDiagram.build(graphBuilder, substation.getId());

        compareMetadata(diagram, getLayoutParameters(), "/substDiag_metadata.json",
                new DefaultDiagramLabelProvider(network, componentLibrary, getLayoutParameters()),
                new DefaultDiagramStyleProvider());
    }

    @Test
    public void testHorizontalNominalStyle() {
        // compare metadata of substation diagram with reference
        // (with horizontal substation layout)
        SubstationDiagram diagram = SubstationDiagram.build(graphBuilder, substation.getId());

        compareMetadata(diagram, getLayoutParameters(), "/substDiag_metadata.json",
                new DefaultDiagramLabelProvider(network, componentLibrary, getLayoutParameters()),
                new NominalVoltageDiagramStyleProvider(network));
    }

    @Test
    public void testSmart() {
        // write Json and compare to reference (with smart substation layout)
        SubstationGraph g = graphBuilder.buildSubstationGraph(substation.getId(), false);
        new ForceSubstationLayoutFactory(ForceSubstationLayoutFactory.CompactionType.NONE).create(g, new PositionVoltageLevelLayoutFactory()).run(
            getLayoutParameters());
        assertEquals(toString("/TestCase11SubstationGraphSmart.json"), toJson(g, "/TestCase11SubstationGraphSmart.json", false));
        assertEquals(substation.getId(), g.getSubstationId());
        assertEquals(substation.getVoltageLevelStream().count(), g.getNodes().size());
        assertEquals(19, g.getTwtEdges().size());
        assertEquals(8, g.getMultiTermNodes().size());
    }

    @Test
    public void testSmartHorizontalCompaction() {
        // write Json and compare to reference (with smart substation layout and horizontal compaction)
        SubstationGraph g = graphBuilder.buildSubstationGraph(substation.getId(), false);
        new ForceSubstationLayoutFactory(ForceSubstationLayoutFactory.CompactionType.HORIZONTAL).create(g, new PositionVoltageLevelLayoutFactory()).run(getLayoutParameters());
        assertEquals(toString("/TestCase11SubstationGraphSmartHorizontal.json"), toJson(g, "/TestCase11SubstationGraphSmartHorizontal.json", false));
        assertEquals(substation.getId(), g.getSubstationId());
        assertEquals(substation.getVoltageLevelStream().count(), g.getNodes().size());
        assertEquals(19, g.getTwtEdges().size());
        assertEquals(8, g.getMultiTermNodes().size());
    }

    @Test
    public void testSmartVerticalCompaction() {
        // write Json and compare to reference (with smart substation layout and vertical compaction)
        SubstationGraph g = graphBuilder.buildSubstationGraph(substation.getId(), false);
        new ForceSubstationLayoutFactory(ForceSubstationLayoutFactory.CompactionType.VERTICAL).create(g, new PositionVoltageLevelLayoutFactory()).run(getLayoutParameters());
        assertEquals(toString("/TestCase11SubstationGraphSmartVertical.json"), toJson(g, "/TestCase11SubstationGraphSmartVertical.json", false));
        assertEquals(substation.getId(), g.getSubstationId());
        assertEquals(substation.getVoltageLevelStream().count(), g.getNodes().size());
        assertEquals(19, g.getTwtEdges().size());
        assertEquals(8, g.getMultiTermNodes().size());
    }
}
