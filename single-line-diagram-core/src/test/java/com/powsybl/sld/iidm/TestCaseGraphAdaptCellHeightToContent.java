/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.iidm;

import com.powsybl.iidm.network.Country;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.SwitchKind;
import com.powsybl.iidm.network.TopologyKind;
import com.powsybl.sld.NetworkGraphBuilder;
import com.powsybl.sld.iidm.extensions.ConnectablePosition;
import com.powsybl.sld.layout.BlockOrganizer;
import com.powsybl.sld.layout.ImplicitCellDetector;
import com.powsybl.sld.layout.LayoutParameters;
import com.powsybl.sld.layout.positionfromextension.PositionFromExtension;
import com.powsybl.sld.layout.PositionVoltageLevelLayout;
import com.powsybl.sld.model.VoltageLevelGraph;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class TestCaseGraphAdaptCellHeightToContent extends AbstractTestCaseIidm {

    private LayoutParameters layoutParameters;

    @Override
    protected LayoutParameters getLayoutParameters() {
        return layoutParameters;
    }

    @Before
    public void setUp() {
        layoutParameters = createDefaultLayoutParameters()
            .setExternCellHeight(200);

        network = Network.create("testCaseGraphAdaptCellHeightToContent", "test");
        graphBuilder = new NetworkGraphBuilder(network);

        substation = createSubstation(network, "subst", "subst", Country.FR);

        vl = createVoltageLevel(substation, "vl", "vl", TopologyKind.NODE_BREAKER, 380, 50);

        createBusBarSection(vl, "bbs1", "bbs1", 0, 1, 1);
        createBusBarSection(vl, "bbs2", "bbs2", 1, 2, 1);

        // coupling (intern cell)
        createSwitch(vl, "d1", "d1", SwitchKind.DISCONNECTOR, false, false, false, 0, 2);
        createSwitch(vl, "b1", "b1", SwitchKind.BREAKER, false, false, false, 2, 3);
        createSwitch(vl, "d2", "d2", SwitchKind.DISCONNECTOR, false, false, false, 3, 1);

        // load (huge feeder cell with serial blocks)
        createSwitch(vl, "d3", "d3", SwitchKind.DISCONNECTOR, false, false, false, 0, 4);
        createSwitch(vl, "b2", "b2", SwitchKind.BREAKER, false, true, false, 4, 5);
        createSwitch(vl, "b3", "b3", SwitchKind.BREAKER, false, false, false, 5, 6);
        createSwitch(vl, "b4", "b4", SwitchKind.BREAKER, false, true, false, 6, 7);
        createSwitch(vl, "b5", "b5", SwitchKind.BREAKER, false, false, false, 7, 8);
        createSwitch(vl, "b6", "b6", SwitchKind.BREAKER, false, false, false, 8, 9);
        createSwitch(vl, "b7", "b7", SwitchKind.BREAKER, false, true, false, 9, 10);
        createSwitch(vl, "b8", "b8", SwitchKind.BREAKER, false, true, false, 10, 11);
        createSwitch(vl, "b9", "b9", SwitchKind.BREAKER, false, false, false, 11, 12);
        createSwitch(vl, "b10", "b10", SwitchKind.BREAKER, false, false, false, 12, 13);
        createSwitch(vl, "b11", "b11", SwitchKind.BREAKER, false, false, false, 13, 14);
        createSwitch(vl, "b12", "b12", SwitchKind.BREAKER, false, false, false, 14, 15);
        createLoad(vl, "load1", "load1", "load1", 0, ConnectablePosition.Direction.TOP, 15, 10, 10);

        // generator (small feeder cell with serial blocks)
        createSwitch(vl, "d4", "d4", SwitchKind.DISCONNECTOR, false, true, false, 1, 16);
        createSwitch(vl, "b13", "b13", SwitchKind.BREAKER, true, false, false, 16, 17);
        createGenerator(vl, "gen1", "gen1", "gen1", 3, ConnectablePosition.Direction.BOTTOM, 17, 0, 20, false, 10, 10);

        // load (small feeder cell with parallel blocks)
        createSwitch(vl, "d5", "d5", SwitchKind.DISCONNECTOR, false, true, false, 0, 18);
        createSwitch(vl, "d6", "d6", SwitchKind.DISCONNECTOR, false, true, false, 1, 19);
        createSwitch(vl, "b14", "b14", SwitchKind.BREAKER, true, false, false, 18, 20);
        createSwitch(vl, "b15", "b15", SwitchKind.BREAKER, true, false, false, 19, 20);
        createSwitch(vl, "b16", "b16", SwitchKind.BREAKER, true, false, false, 20, 21);
        createLoad(vl, "load2", "load2", "load2", 1, ConnectablePosition.Direction.TOP, 21, 10, 10);

        // undefined block
        createSwitch(vl, "d7", "d7", SwitchKind.DISCONNECTOR, false, true, false, 0, 22);
        createSwitch(vl, "b17", "b17", SwitchKind.BREAKER, true, false, false, 22, 23);
        createSwitch(vl, "b18", "b18", SwitchKind.BREAKER, true, false, false, 23, 24);
        createSwitch(vl, "b19", "b19", SwitchKind.BREAKER, true, false, false, 23, 24);
        createSwitch(vl, "b20", "b20", SwitchKind.BREAKER, true, false, false, 24, 25);
        createGenerator(vl, "gen2", "gen2", "gen2", 2, ConnectablePosition.Direction.BOTTOM, 25, 0, 20, false, 10, 10);
    }

    @Test
    public void testHeightFixed() {
        // layout parameters with extern cell height fixed
        getLayoutParameters()
            .setAdaptCellHeightToContent(false);

        VoltageLevelGraph g = graphBuilder.buildVoltageLevelGraph(vl.getId(), false, true);
        new ImplicitCellDetector(false, true, false).detectCells(g);
        new BlockOrganizer(new PositionFromExtension(), false).organize(g);
        new PositionVoltageLevelLayout(g).run(getLayoutParameters());

        assertEquals(toString("/TestCaseGraphExternCellHeightFixed.json"), toJson(g, "/TestCaseGraphExternCellHeightFixed.json"));
    }

    @Test
    public void testAdaptHeight() {
        // layout parameters with adapt cell height to content
        getLayoutParameters()
            .setAdaptCellHeightToContent(true);

        VoltageLevelGraph g = graphBuilder.buildVoltageLevelGraph(vl.getId(), false, true);
        new ImplicitCellDetector(false, true, false).detectCells(g);
        new BlockOrganizer(new PositionFromExtension(), true).organize(g);
        new PositionVoltageLevelLayout(g).run(getLayoutParameters());

        assertEquals(toString("/TestCaseGraphAdaptCellHeightToContent.json"), toJson(g, "/TestCaseGraphAdaptCellHeightToContent.json"));
    }
}
