/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.cgmes.layout;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.powsybl.sld.cgmes.dl.iidm.extensions.DiagramPoint;
import com.powsybl.iidm.network.Network;
import com.powsybl.sld.model.BusNode;
import com.powsybl.sld.model.VoltageLevelGraph;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public class NodeTopologyVerticalBusbarTest extends AbstractNodeTopologyTest {

    @Override
    protected void addDiagramData(Network network) {
        addBusbarSectionDiagramData(network.getBusbarSection("BusbarSection"), new DiagramPoint(140, 60, 1), new DiagramPoint(140, 170, 2));
        addGeneratorDiagramData(network.getGenerator("Generator"), new DiagramPoint(45, 85, 0),
                                new DiagramPoint(50, 85, 1), new DiagramPoint(140, 85, 2));
        addSwitchDiagramData(network.getSwitch("Disconnector1"), new DiagramPoint(155, 150, 0), 90, new DiagramPoint(150, 150, 1),
                             new DiagramPoint(145, 150, 2), new DiagramPoint(130, 160, 1), new DiagramPoint(165, 150, 2));
        addSwitchDiagramData(network.getSwitch("Breaker1"), new DiagramPoint(175, 150, 0), 90, new DiagramPoint(170, 150, 1),
                             new DiagramPoint(165, 150, 2), new DiagramPoint(180, 150, 1), new DiagramPoint(185, 150, 2));
        addSwitchDiagramData(network.getSwitch("Disconnector2"), new DiagramPoint(195, 150, 0), 90, new DiagramPoint(190, 150, 1),
                             new DiagramPoint(185, 150, 2), new DiagramPoint(200, 150, 1), new DiagramPoint(205, 150, 1));
        addLineDiagramData(network.getLine("Line"), new DiagramPoint(205, 150, 1), new DiagramPoint(260, 150, 2));
    }

    @Override
    protected void checkCoordinates(VoltageLevelGraph graph) {
        assertEquals(210, graph.getNodes().get(0).getX(), 0);
        assertEquals(10, graph.getNodes().get(0).getY(), 0);
        assertEquals(220, ((BusNode) graph.getNodes().get(0)).getPxWidth(), 0);
        assertTrue(graph.getNodes().get(0).isRotated());
        assertEquals(340, graph.getNodes().get(1).getX(), 0);
        assertEquals(190, graph.getNodes().get(1).getY(), 0);
        assertEquals(20, graph.getNodes().get(2).getX(), 0);
        assertEquals(60, graph.getNodes().get(2).getY(), 0);
        assertEquals(240, graph.getNodes().get(3).getX(), 0);
        assertEquals(190, graph.getNodes().get(3).getY(), 0);
        assertTrue(graph.getNodes().get(3).isRotated());
        assertEquals(280, graph.getNodes().get(4).getX(), 0);
        assertEquals(190, graph.getNodes().get(4).getY(), 0);
        assertTrue(graph.getNodes().get(4).isRotated());
        assertEquals(320, graph.getNodes().get(5).getX(), 0);
        assertEquals(190, graph.getNodes().get(5).getY(), 0);
        assertTrue(graph.getNodes().get(5).isRotated());
    }

}
