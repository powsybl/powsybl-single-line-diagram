/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.iidm;

import com.powsybl.iidm.network.*;
import com.powsybl.sld.NetworkGraphBuilder;
import com.powsybl.sld.iidm.extensions.ConnectablePosition;
import com.powsybl.sld.layout.BlockOrganizer;
import com.powsybl.sld.layout.ImplicitCellDetector;
import com.powsybl.sld.layout.LayoutParameters;
import com.powsybl.sld.layout.PositionVoltageLevelLayout;
import com.powsybl.sld.model.*;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * <PRE>
 * l
 * |
 * b
 * |
 * d
 * |
 * ------ bbs
 * </PRE>
 *
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class TestCase1inverted extends AbstractTestCaseIidm {

    @Override
    protected LayoutParameters getLayoutParameters() {
        return createDefaultLayoutParameters();
    }

    @Before
    public void setUp() {
        network = Network.create("testCase1", "test");
        graphBuilder = new NetworkGraphBuilder(network);
        substation = createSubstation(network, "s", "s", Country.FR);
        vl = createVoltageLevel(substation, "vl", "vl", TopologyKind.NODE_BREAKER, 380, 10);
        createLoad(vl, "l", "l", "l", 0, ConnectablePosition.Direction.TOP, 0, 10, 10);
        createSwitch(vl, "d", "d", SwitchKind.DISCONNECTOR, false, false, false, 2, 1);
        createSwitch(vl, "b", "b", SwitchKind.BREAKER, false, false, false, 1, 0);
        createBusBarSection(vl, "bbs", "bbs", 2, 1, 1);
    }

    @Test
    public void test() {
        // build graph
        VoltageLevelGraph g = graphBuilder.buildVoltageLevelGraph(vl.getId(), false, true);

        // detect cells
        new ImplicitCellDetector().detectCells(g);

        // build blocks
        new BlockOrganizer().organize(g);

        // calculate coordinates
        new PositionVoltageLevelLayout(g).run(getLayoutParameters());

        // write Json and compare to reference
        assertEquals(toString("/TestCase1inverted.json"), toJson(g, "/TestCase1inverted.json"));
    }
}
