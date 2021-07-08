/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.iidm;

import com.powsybl.iidm.network.SwitchKind;
import com.powsybl.sld.layout.BlockOrganizer;
import com.powsybl.sld.layout.ImplicitCellDetector;
import com.powsybl.sld.layout.PositionVoltageLevelLayout;
import com.powsybl.sld.model.VoltageLevelGraph;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * <pre>
 *     b        b2       b3
 *    / \      / \      / \
 *   |   |    |   |    |   |
 * -d1---|---d3---|---d6---|---- bbs1
 *       |    |   |        |
 * -----d2---d4--d5-------d7---- bbs2
 *
 * </pre>
 *
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public class TestCase3TripleCoupling extends TestCase3Coupling {

    @Before
    public void setUp() {
        // we add two other coupling to TestCase3Coupling setUp
        super.setUp();

        // second coupling
        createSwitch(vl, "d3", "d3", SwitchKind.DISCONNECTOR, false, false, false, 0, 4);
        createSwitch(vl, "d4", "d4", SwitchKind.DISCONNECTOR, false, false, false, 3, 4);
        createSwitch(vl, "b2", "b2", SwitchKind.BREAKER, false, false, false, 4, 5);
        createSwitch(vl, "d5", "d5", SwitchKind.DISCONNECTOR, false, false, false, 5, 3);

        // third coupling
        createSwitch(vl, "d6", "d6", SwitchKind.DISCONNECTOR, false, false, false, 0, 6);
        createSwitch(vl, "b3", "b3", SwitchKind.BREAKER, false, false, false, 6, 7);
        createSwitch(vl, "d7", "d7", SwitchKind.DISCONNECTOR, false, false, false, 7, 3);
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
        assertEquals(toString("/TestCase3TripleCoupling.json"), toJson(g, "/TestCase3TripleCoupling.json"));
    }

    @Test
    public void testDisconnectorOpen() {
        // the displayed result should remain the same in terms of intern cells order after opening a switch
        network.getSwitch("d3").setOpen(true);

        // build graph
        VoltageLevelGraph g = graphBuilder.buildVoltageLevelGraph(vl.getId(), false, true);

        // detect cells
        new ImplicitCellDetector().detectCells(g);

        // build blocks
        new BlockOrganizer().organize(g);

        // calculate coordinates
        new PositionVoltageLevelLayout(g).run(getLayoutParameters());

        // write Json and compare to reference
        String reference = toString("/TestCase3TripleCoupling.json");
        int d3Index = reference.indexOf("d3");
        int d3StateIndex = d3Index + reference.substring(d3Index).indexOf("\"open\" : false");
        reference = reference.substring(0, d3StateIndex) + reference.substring(d3StateIndex).replaceFirst("\"open\" : false", "\"open\" : true");
        assertEquals(reference, toJson(g, "/TestCase3TripleCoupling_disconnectorOpen.json"));
    }
}
