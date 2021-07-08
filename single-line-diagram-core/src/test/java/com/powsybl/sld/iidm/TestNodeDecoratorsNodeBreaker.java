/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.iidm;

import com.powsybl.iidm.network.Network;
import com.powsybl.sld.NetworkGraphBuilder;
import com.powsybl.sld.layout.*;
import com.powsybl.sld.library.ComponentSize;
import com.powsybl.sld.model.Node;
import com.powsybl.sld.model.SubstationGraph;
import com.powsybl.sld.model.SwitchNode;
import com.powsybl.sld.model.VoltageLevelGraph;
import com.powsybl.sld.svg.DefaultDiagramLabelProvider;
import com.powsybl.sld.svg.LabelPosition;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.junit.Assert.assertEquals;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 * @author Slimane Amar <slimane.amar at rte-france.com>
 */
public class TestNodeDecoratorsNodeBreaker extends AbstractTestCaseIidm {

    @Override
    public LayoutParameters getLayoutParameters() {
        return createDefaultLayoutParameters().setShowInternalNodes(false);
    }

    @Before
    public void setUp() {
        network = CreateNetworksUtil.createNodeBreakerNetworkWithBranchStatus("TestNodeDecorators", "test");
        graphBuilder = new NetworkGraphBuilder(network);
    }

    @Test
    public void testBranchStatusDecorators() {

        // build substation graph
        SubstationGraph g = graphBuilder.buildSubstationGraph("S1", true);

        new HorizontalSubstationLayoutFactory().create(g, new PositionVoltageLevelLayoutFactory()).run(getLayoutParameters());
        assertEquals(toString("/NodeDecoratorsBranchStatusNodeBreaker.svg"),
            toSVG(g, "/NodeDecoratorsBranchStatusNodeBreaker.svg", getLayoutParameters(), getDefaultDiagramLabelProvider(), getDefaultDiagramStyleProvider()));
    }

    @Test
    public void testSwitchDecorators() {
        // build graph
        VoltageLevelGraph g = graphBuilder.buildVoltageLevelGraph(network.getVoltageLevel("VL1").getId(), true, true);

        // detect cells
        new ImplicitCellDetector().detectCells(g);

        // build blocks
        new BlockOrganizer().organize(g);

        // calculate coordinates
        new PositionVoltageLevelLayout(g).run(getLayoutParameters());

        // write SVG and compare to reference
        assertEquals(toString("/NodeDecoratorsSwitches.svg"),
            toSVG(g, "/NodeDecoratorsSwitches.svg", getLayoutParameters(), new TestDiagramLabelProvider(network), getDefaultDiagramStyleProvider()));
    }

    private class TestDiagramLabelProvider extends DefaultDiagramLabelProvider {

        private static final double SWITCH_DECORATOR_OFFSET = 1d;

        public TestDiagramLabelProvider(Network network) {
            super(network, componentLibrary, getLayoutParameters());
        }

        @Override
        public List<NodeDecorator> getNodeDecorators(Node node) {
            Objects.requireNonNull(node);
            if (node instanceof SwitchNode) {
                return Collections.singletonList(new NodeDecorator("LOCK", getSwitchDecoratorPosition((SwitchNode) node)));
            }
            return Collections.emptyList();
        }

        private LabelPosition getSwitchDecoratorPosition(SwitchNode node) {
            ComponentSize size = componentLibrary.getSize(node.getComponentType());
            double yShift = -size.getHeight() / 2;
            double xShift = size.getWidth() / 2 + SWITCH_DECORATOR_OFFSET;
            return new LabelPosition(node.getId() + "_DECORATOR", xShift, yShift, false, 0);
        }
    }
}
