/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.cgmes.layout;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import com.powsybl.sld.cgmes.dl.iidm.extensions.DiagramPoint;
import com.powsybl.iidm.network.Network;
import com.powsybl.sld.model.BusNode;
import com.powsybl.sld.model.VoltageLevelGraph;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public class NodeTopologyHorizontalBusbarTest extends AbstractNodeTopologyTest {

    @Override
    protected void addDiagramData(Network network) {
        addBusbarSectionDiagramData(network.getBusbarSection("BusbarSection"), new DiagramPoint(20, 115, 1), new DiagramPoint(180, 115, 2));
        addGeneratorDiagramData(network.getGenerator("Generator"), new DiagramPoint(105, 230, 0),
                                new DiagramPoint(105, 225, 1), new DiagramPoint(105, 115, 2));
        addSwitchDiagramData(network.getSwitch("Disconnector1"), new DiagramPoint(105, 100, 0), 0, new DiagramPoint(105, 95, 1),
                             new DiagramPoint(105, 90, 2), new DiagramPoint(105, 105, 1), new DiagramPoint(105, 115, 2));
        addSwitchDiagramData(network.getSwitch("Breaker1"), new DiagramPoint(105, 80, 0), 0, new DiagramPoint(105, 85, 1),
                             new DiagramPoint(105, 90, 2), new DiagramPoint(105, 75, 1), new DiagramPoint(105, 70, 2));
        addSwitchDiagramData(network.getSwitch("Disconnector2"), new DiagramPoint(105, 60, 0), 0, new DiagramPoint(105, 65, 1),
                             new DiagramPoint(105, 70, 2), new DiagramPoint(105, 55, 1), new DiagramPoint(105, 50, 2));
        addLineDiagramData(network.getLine("Line"), new DiagramPoint(105, 50, 1), new DiagramPoint(105, 10, 2));
    }

    @Override
    protected void checkCoordinates(VoltageLevelGraph graph) {
        assertEquals(20, graph.getNodes().get(0).getX(), 0);
        assertEquals(140, graph.getNodes().get(0).getY(), 0);
        assertEquals(320, ((BusNode) graph.getNodes().get(0)).getPxWidth(), 0);
        assertFalse(graph.getNodes().get(0).isRotated());
        assertEquals(190, graph.getNodes().get(1).getX(), 0);
        assertEquals(10, graph.getNodes().get(1).getY(), 0);
        assertEquals(190, graph.getNodes().get(2).getX(), 0);
        assertEquals(370, graph.getNodes().get(2).getY(), 0);
        assertEquals(190, graph.getNodes().get(3).getX(), 0);
        assertEquals(110, graph.getNodes().get(3).getY(), 0);
        assertFalse(graph.getNodes().get(3).isRotated());
        assertEquals(190, graph.getNodes().get(4).getX(), 0);
        assertEquals(70, graph.getNodes().get(4).getY(), 0);
        assertFalse(graph.getNodes().get(4).isRotated());
        assertEquals(190, graph.getNodes().get(5).getX(), 0);
        assertEquals(30, graph.getNodes().get(5).getY(), 0);
        assertFalse(graph.getNodes().get(5).isRotated());
    }

}
