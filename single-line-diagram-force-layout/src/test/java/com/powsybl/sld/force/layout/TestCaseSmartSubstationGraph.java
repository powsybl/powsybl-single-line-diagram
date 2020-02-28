/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.force.layout;

import com.powsybl.sld.TestCase11SubstationGraph;
import com.powsybl.sld.layout.LayoutParameters;
import com.powsybl.sld.layout.PositionVoltageLevelLayoutFactory;
import com.powsybl.sld.model.SubstationGraph;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class TestCaseSmartSubstationGraph extends TestCase11SubstationGraph {

    @Before
    public void setUp() {
        super.setUp();
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
        new ForceSubstationLayoutFactory(ForceSubstationLayoutFactory.CompactionType.NONE).create(g, new PositionVoltageLevelLayoutFactory()).run(layoutParameters);
        assertEquals(toJson(g, "/TestCase11SubstationGraphSmart.json", false), toString("/TestCase11SubstationGraphSmart.json"));

        // write Json and compare to reference (with smart substation layout and horizontal compaction)
        g = graphBuilder.buildSubstationGraph(substation.getId(), false);
        new ForceSubstationLayoutFactory(ForceSubstationLayoutFactory.CompactionType.HORIZONTAL).create(g, new PositionVoltageLevelLayoutFactory()).run(layoutParameters);
        assertEquals(toJson(g, "/TestCase11SubstationGraphSmartHorizontal.json", false), toString("/TestCase11SubstationGraphSmartHorizontal.json"));

        // write Json and compare to reference (with smart substation layout and vertical compaction)
        g = graphBuilder.buildSubstationGraph(substation.getId(), false);
        new ForceSubstationLayoutFactory(ForceSubstationLayoutFactory.CompactionType.VERTICAL).create(g, new PositionVoltageLevelLayoutFactory()).run(layoutParameters);
        assertEquals(toJson(g, "/TestCase11SubstationGraphSmartVertical.json", false), toString("/TestCase11SubstationGraphSmartVertical.json"));
    }
}