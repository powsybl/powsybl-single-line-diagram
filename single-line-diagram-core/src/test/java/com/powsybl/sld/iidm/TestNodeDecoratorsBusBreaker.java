/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.iidm;

import com.powsybl.sld.NetworkGraphBuilder;
import com.powsybl.sld.layout.BlockOrganizer;
import com.powsybl.sld.layout.ImplicitCellDetector;
import com.powsybl.sld.layout.LayoutParameters;
import com.powsybl.sld.layout.PositionVoltageLevelLayout;
import com.powsybl.sld.model.VoltageLevelGraph;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 * @author Slimane Amar <slimane.amar at rte-france.com>
 */
public class TestNodeDecoratorsBusBreaker extends AbstractTestCaseIidm {

    @Override
    protected LayoutParameters getLayoutParameters() {
        return createDefaultLayoutParameters().setShowInternalNodes(false);
    }

    @Before
    public void setUp() {
        network = CreateNetworksUtil.createBusBreakerNetworkWithBranchStatus("TestNodeDecorators", "test");
        graphBuilder = new NetworkGraphBuilder(network);
    }

    @Test
    public void testBranchStatusDecorators() {
        // build graph
        VoltageLevelGraph g = graphBuilder.buildVoltageLevelGraph(network.getVoltageLevel("VL1").getId(), true, true);

        // detect cells
        new ImplicitCellDetector().detectCells(g);

        // build blocks
        new BlockOrganizer().organize(g);

        // calculate coordinates
        new PositionVoltageLevelLayout(g).run(getLayoutParameters());

        // write SVG and compare to reference
        assertEquals(toString("/NodeDecoratorsBranchStatusBusBreaker.svg"),
            toSVG(g, "/NodeDecoratorsBranchStatusBusBreaker.svg", getLayoutParameters(), getDefaultDiagramLabelProvider(), getDefaultDiagramStyleProvider()));
    }
}
