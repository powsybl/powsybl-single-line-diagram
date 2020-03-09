/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.force.layout;

import com.powsybl.iidm.network.Country;
import com.powsybl.iidm.network.Substation;
import com.powsybl.iidm.network.SwitchKind;
import com.powsybl.iidm.network.TopologyKind;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.sld.TestCase11SubstationGraph;
import com.powsybl.sld.ZoneDiagram;
import com.powsybl.sld.ZoneId;
import com.powsybl.sld.iidm.extensions.ConnectablePosition;
import com.powsybl.sld.layout.CompactionType;
import com.powsybl.sld.layout.HorizontalSubstationLayoutFactory;
import com.powsybl.sld.layout.LayoutParameters;
import com.powsybl.sld.layout.PositionVoltageLevelLayoutFactory;
import com.powsybl.sld.model.SubstationGraph;
import com.powsybl.sld.model.ZoneGraph;
import com.powsybl.sld.svg.DefaultDiagramInitialValueProvider;
import com.powsybl.sld.svg.DefaultDiagramStyleProvider;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class TestCaseSmartSubstationGraph extends TestCase11SubstationGraph {

    Substation substation2;

    @Before
    public void setUp() {
        super.setUp();

        // Creation of another substation, another voltageLevel and lines between the two substations
        //
        substation2 = createSubstation(network, "subst2", "subst2", Country.FR);
        VoltageLevel vlSubst2 = createVoltageLevel(substation2, "vlSubst2", "vlSubst2", TopologyKind.NODE_BREAKER, 400, 50);

        createBusBarSection(vlSubst2, "bbs1_2", "bbs1_2", 0, 1, 1);

        // line 1
        createSwitch(vl1, "dline11_3", "dline11_3", SwitchKind.DISCONNECTOR, false, false, true, 0, 34);
        createSwitch(vl1, "bline11_3", "bline11_3", SwitchKind.BREAKER, true, false, true, 34, 35);
        createSwitch(vlSubst2, "dline21_3", "dline21_3", SwitchKind.DISCONNECTOR, false, false, true, 0, 1);
        createSwitch(vlSubst2, "bline21_3", "bline21_3", SwitchKind.BREAKER, true, false, true, 1, 2);
        createLine(network, "line1", "line1", 2.0, 14.745, 1.0, 1.0, 1.0, 1.0,
                35, 2, vl1.getId(), vlSubst2.getId(),
                "line1", 3, ConnectablePosition.Direction.TOP,
                "line1", 1, ConnectablePosition.Direction.TOP);

        // line 2
        createSwitch(vl1, "dline11_4", "dline11_4", SwitchKind.DISCONNECTOR, false, false, true, 0, 36);
        createSwitch(vl1, "bline11_4", "bline11_4", SwitchKind.BREAKER, true, false, true, 36, 37);
        createSwitch(vlSubst2, "dline21_4", "dline21_4", SwitchKind.DISCONNECTOR, false, false, true, 0, 3);
        createSwitch(vlSubst2, "bline21_4", "bline21_4", SwitchKind.BREAKER, true, false, true, 3, 4);
        createLine(network, "line2", "line2", 2.0, 14.745, 1.0, 1.0, 1.0, 1.0,
                37, 4, vl1.getId(), vlSubst2.getId(),
                "line2", 4, ConnectablePosition.Direction.TOP,
                "line2", 2, ConnectablePosition.Direction.BOTTOM);

        // line 3
        createSwitch(vl1, "dline11_5", "dline11_5", SwitchKind.DISCONNECTOR, false, false, true, 0, 38);
        createSwitch(vl1, "bline11_5", "bline11_5", SwitchKind.BREAKER, true, false, true, 38, 39);
        createSwitch(vlSubst2, "dline21_5", "dline21_5", SwitchKind.DISCONNECTOR, false, false, true, 0, 5);
        createSwitch(vlSubst2, "bline21_5", "bline21_5", SwitchKind.BREAKER, true, false, true, 5, 6);
        createLine(network, "line3", "line3", 2.0, 14.745, 1.0, 1.0, 1.0, 1.0,
                39, 6, vl1.getId(), vlSubst2.getId(),
                "line3", 5, ConnectablePosition.Direction.BOTTOM,
                "line3", 3, ConnectablePosition.Direction.TOP);

        // line 4
        createSwitch(vl1, "dline11_6", "dline11_6", SwitchKind.DISCONNECTOR, false, false, true, 0, 40);
        createSwitch(vl1, "bline11_6", "bline11_6", SwitchKind.BREAKER, true, false, true, 40, 41);
        createSwitch(vlSubst2, "dline21_6", "dline21_6", SwitchKind.DISCONNECTOR, false, false, true, 0, 7);
        createSwitch(vlSubst2, "bline21_6", "bline21_6", SwitchKind.BREAKER, true, false, true, 7, 8);
        createLine(network, "line4", "line4", 2.0, 14.745, 1.0, 1.0, 1.0, 1.0,
                41, 8, vl1.getId(), vlSubst2.getId(),
                "line4", 6, ConnectablePosition.Direction.BOTTOM,
                "line4", 4, ConnectablePosition.Direction.BOTTOM);
    }

    @Test
    public void test() {
        LayoutParameters layoutParameters = new LayoutParameters()
                .setTranslateX(20)
                .setTranslateY(50)
                .setInitialXBus(0)
                .setInitialYBus(260)
                .setVerticalSpaceBus(25)
                .setHorizontalBusPadding(20)
                .setCellWidth(50)
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
                .setShowInductorFor3WT(false);

        // write Json and compare to reference (with smart substation layout)
        SubstationGraph g = graphBuilder.buildSubstationGraph(substation.getId(), false);
        new ForceSubstationLayoutFactory(CompactionType.NONE).create(g, new PositionVoltageLevelLayoutFactory()).run(layoutParameters);
        assertEquals(toJson(g, "/TestCase11SubstationGraphSmart.json", false), toString("/TestCase11SubstationGraphSmart.json"));

        // write Json and compare to reference (with smart substation layout and horizontal compaction)
        g = graphBuilder.buildSubstationGraph(substation.getId(), false);
        new ForceSubstationLayoutFactory(CompactionType.HORIZONTAL).create(g, new PositionVoltageLevelLayoutFactory()).run(layoutParameters);
        assertEquals(toJson(g, "/TestCase11SubstationGraphSmartHorizontal.json", false), toString("/TestCase11SubstationGraphSmartHorizontal.json"));

        // write Json and compare to reference (with smart substation layout and vertical compaction)
        g = graphBuilder.buildSubstationGraph(substation.getId(), false);
        new ForceSubstationLayoutFactory(CompactionType.VERTICAL).create(g, new PositionVoltageLevelLayoutFactory()).run(layoutParameters);
        assertEquals(toJson(g, "/TestCase11SubstationGraphSmartVertical.json", false), toString("/TestCase11SubstationGraphSmartVertical.json"));

        // write Json and compare to reference (with smart zone layout and smart substation layout)
        ZoneGraph zoneG = graphBuilder.buildZoneGraph(ZoneId.create(Arrays.asList(substation.getId(), substation2.getId())), false);
        new ForceZoneLayoutFactory(CompactionType.NONE).create(zoneG,
                new ForceSubstationLayoutFactory(CompactionType.NONE),
                new PositionVoltageLevelLayoutFactory()).run(layoutParameters);
        assertEquals(toJson(zoneG, "/TestCase11ZoneGraphSmart.json", false), toString("/TestCase11ZoneGraphSmart.json"));

        // write Json and compare to reference (with smart zone layout and smart substation layout with vertical compaction)
        zoneG = graphBuilder.buildZoneGraph(ZoneId.create(Arrays.asList(substation.getId(), substation2.getId())), false);
        new ForceZoneLayoutFactory(CompactionType.VERTICAL).create(zoneG,
                new ForceSubstationLayoutFactory(CompactionType.VERTICAL),
                new PositionVoltageLevelLayoutFactory()).run(layoutParameters);
        assertEquals(toJson(zoneG, "/TestCase11ZoneGraphSmartVertical.json", false), toString("/TestCase11ZoneGraphSmartVertical.json"));

        zoneG = graphBuilder.buildZoneGraph(ZoneId.create(Arrays.asList(substation.getId(), substation2.getId())), false);
        new ForceZoneLayoutFactory(CompactionType.HORIZONTAL).create(zoneG,
                new ForceSubstationLayoutFactory(CompactionType.HORIZONTAL),
                new PositionVoltageLevelLayoutFactory()).run(layoutParameters);
        assertEquals(toJson(zoneG, "/TestCase11ZoneGraphSmartHorizontal.json", false), toString("/TestCase11ZoneGraphSmartHorizontal.json"));

        // compare metadata of zone diagram with reference
        // (with smart zone layout and horizontal substation layout)
        ZoneDiagram zoneDiag = ZoneDiagram.build(graphBuilder,
                ZoneId.create(Arrays.asList(substation.getId(), substation2.getId())),
                new ForceZoneLayoutFactory(CompactionType.NONE),
                new HorizontalSubstationLayoutFactory(),
                new PositionVoltageLevelLayoutFactory(),
                false);

        compareMetadata(zoneDiag, layoutParameters, "/zoneDiag_metadata.json",
                new DefaultDiagramInitialValueProvider(network),
                new DefaultDiagramStyleProvider());
    }
}
