/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout;

import com.powsybl.sld.iidm.TestCase11SubstationGraph;
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
        layoutParameters.setShowInternalNodes(false);
    }

    @Test
    public void test() {

        // write Json and compare to reference (with smart substation layout)
        SubstationGraph g = graphBuilder.buildSubstationGraph(substation.getId(), false);
        new ForceSubstationLayoutFactory(ForceSubstationLayoutFactory.CompactionType.NONE).create(g, new PositionVoltageLevelLayoutFactory()).run(getLayoutParameters());
        assertEquals(toString("/TestCase11SubstationGraphSmart.json"), toJson(g, "/TestCase11SubstationGraphSmart.json", false));
        assertEquals(substation.getId(), g.getSubstationId());
        assertEquals(substation.getVoltageLevelStream().count(), g.getNodes().size());
        assertEquals(19, g.getTwtEdges().size());
        assertEquals(8, g.getMultiTermNodes().size());

        // write Json and compare to reference (with smart substation layout and horizontal compaction)
        g = graphBuilder.buildSubstationGraph(substation.getId(), false);
        new ForceSubstationLayoutFactory(ForceSubstationLayoutFactory.CompactionType.HORIZONTAL).create(g, new PositionVoltageLevelLayoutFactory()).run(getLayoutParameters());
        assertEquals(toString("/TestCase11SubstationGraphSmartHorizontal.json"), toJson(g, "/TestCase11SubstationGraphSmartHorizontal.json", false));
        assertEquals(substation.getId(), g.getSubstationId());
        assertEquals(substation.getVoltageLevelStream().count(), g.getNodes().size());
        assertEquals(19, g.getTwtEdges().size());
        assertEquals(8, g.getMultiTermNodes().size());

        // write Json and compare to reference (with smart substation layout and vertical compaction)
        g = graphBuilder.buildSubstationGraph(substation.getId(), false);
        new ForceSubstationLayoutFactory(ForceSubstationLayoutFactory.CompactionType.VERTICAL).create(g, new PositionVoltageLevelLayoutFactory()).run(getLayoutParameters());
        assertEquals(toString("/TestCase11SubstationGraphSmartVertical.json"), toJson(g, "/TestCase11SubstationGraphSmartVertical.json", false));
        assertEquals(substation.getId(), g.getSubstationId());
        assertEquals(substation.getVoltageLevelStream().count(), g.getNodes().size());
        assertEquals(19, g.getTwtEdges().size());
        assertEquals(8, g.getMultiTermNodes().size());
    }
}
