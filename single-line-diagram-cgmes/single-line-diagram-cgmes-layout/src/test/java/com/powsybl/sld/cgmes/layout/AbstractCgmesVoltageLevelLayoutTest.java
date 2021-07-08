/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.cgmes.layout;

import static org.junit.Assert.assertTrue;

import java.util.List;

import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.sld.NetworkGraphBuilder;
import com.powsybl.sld.layout.LayoutParameters;
import com.powsybl.sld.model.VoltageLevelGraph;
import com.powsybl.sld.model.Node;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public abstract class AbstractCgmesVoltageLevelLayoutTest {

    protected static final String DIAGRAM_NAME = "default";

    protected void test(VoltageLevel vl) {
        VoltageLevelGraph graph = new NetworkGraphBuilder(vl.getNetwork()).buildVoltageLevelGraph(vl.getId(), false, true);
        LayoutParameters layoutParameters = new LayoutParameters();
        layoutParameters.setScaleFactor(2);
        layoutParameters.setDiagramName(DIAGRAM_NAME);
        new CgmesVoltageLevelLayout(graph, vl.getNetwork()).run(layoutParameters);
        checkGraph(graph);
        checkCoordinates(graph);
    }

    protected abstract void checkGraph(VoltageLevelGraph graph);

    protected void checkAdjacentNodes(Node node, List<String> expectedAdjacentNodes) {
        node.getAdjacentNodes().forEach(adjacentNode -> {
            assertTrue(expectedAdjacentNodes.contains(adjacentNode.getId()));
        });
    }

    protected abstract void checkCoordinates(VoltageLevelGraph graph);

}
