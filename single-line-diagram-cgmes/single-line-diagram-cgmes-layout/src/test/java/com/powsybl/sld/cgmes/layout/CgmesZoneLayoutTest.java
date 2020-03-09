/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.cgmes.layout;

import static com.powsybl.sld.library.ComponentTypeName.BUSBAR_SECTION;
import static com.powsybl.sld.library.ComponentTypeName.GENERATOR;
import static com.powsybl.sld.library.ComponentTypeName.LINE;
import static com.powsybl.sld.library.ComponentTypeName.LOAD;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import com.powsybl.sld.ZoneId;
import org.joda.time.DateTime;
import org.junit.Test;

import com.powsybl.iidm.network.Branch.Side;
import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Country;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Substation;
import com.powsybl.iidm.network.TopologyKind;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.sld.NetworkGraphBuilder;
import com.powsybl.sld.cgmes.dl.iidm.extensions.CouplingDeviceDiagramData;
import com.powsybl.sld.cgmes.dl.iidm.extensions.DiagramPoint;
import com.powsybl.sld.cgmes.dl.iidm.extensions.DiagramTerminal;
import com.powsybl.sld.cgmes.dl.iidm.extensions.InjectionDiagramData;
import com.powsybl.sld.cgmes.dl.iidm.extensions.LineDiagramData;
import com.powsybl.sld.cgmes.dl.iidm.extensions.NetworkDiagramData;
import com.powsybl.sld.cgmes.dl.iidm.extensions.NodeDiagramData;
import com.powsybl.sld.layout.LayoutParameters;
import com.powsybl.sld.model.Graph;
import com.powsybl.sld.model.LineEdge;
import com.powsybl.sld.model.LineEdge.Point;
import com.powsybl.sld.model.Node;
import com.powsybl.sld.model.ZoneGraph;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class CgmesZoneLayoutTest {

    private static final String SUBSTATION_1_ID = "Substation1";
    private static final String SUBSTATION_2_ID = "Substation2";
    private static final String VOLTAGE_LEVEL_11_ID = "VoltageLevel11";
    private static final String VOLTAGE_LEVEL_12_ID = "VoltageLevel12";
    private static final String VOLTAGE_LEVEL_21_ID = "VoltageLevel21";
    private static final String BUS_11_ID = "Bus11";
    private static final String BUS_12_ID = "Bus12";
    private static final String BUS_21_ID = "Bus21";
    private static final String LOAD_ID = "Load";
    private static final String LINE_ID = "Line";
    private static final String GENERATOR_ID = "Generator";
    private static final String TRANSFORMER_ID = "Transformer";
    private static final String DIAGRAM_ID = "Diagram";

    private Network createNetwork() {
        Network network = Network.create("Network", "test");
        network.setCaseDate(DateTime.parse("2018-01-01T00:30:00.000+01:00"));
        Substation substation1 = network.newSubstation()
                .setId(SUBSTATION_1_ID)
                .setCountry(Country.FR)
                .add();
        VoltageLevel voltageLevel11 = substation1.newVoltageLevel()
                .setId(VOLTAGE_LEVEL_11_ID)
                .setNominalV(400)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        voltageLevel11.getBusBreakerView().newBus()
                .setId(BUS_11_ID)
                .add();
        voltageLevel11.newLoad()
                .setId(LOAD_ID)
                .setBus(BUS_11_ID)
                .setConnectableBus(BUS_11_ID)
                .setP0(100)
                .setQ0(50)
                .add();
        VoltageLevel voltageLevel12 = substation1.newVoltageLevel()
                .setId(VOLTAGE_LEVEL_12_ID)
                .setNominalV(280)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        voltageLevel12.getBusBreakerView().newBus()
                .setId(BUS_12_ID)
                .add();
        int zb380 = 380 * 380 / 100;
        substation1.newTwoWindingsTransformer()
                .setId(TRANSFORMER_ID)
                .setVoltageLevel1(voltageLevel11.getId())
                .setBus1(BUS_11_ID)
                .setConnectableBus1(BUS_11_ID)
                .setRatedU1(24.0)
                .setVoltageLevel2(voltageLevel12.getId())
                .setBus2(BUS_12_ID)
                .setConnectableBus2(BUS_12_ID)
                .setRatedU2(400.0)
                .setR(0.24 / 1300 * zb380)
                .setX(Math.sqrt(10 * 10 - 0.24 * 0.24) / 1300 * zb380)
                .setG(0.0)
                .setB(0.0)
                .add();
        Substation substation2 = network.newSubstation()
                .setId(SUBSTATION_2_ID)
                .setCountry(Country.FR)
                .add();
        VoltageLevel voltageLevel21 = substation2.newVoltageLevel()
                .setId(VOLTAGE_LEVEL_21_ID)
                .setNominalV(280)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        voltageLevel21.getBusBreakerView().newBus()
                .setId(BUS_21_ID)
                .add();
        voltageLevel21.newGenerator()
                .setId(GENERATOR_ID)
                .setBus(BUS_21_ID)
                .setConnectableBus(BUS_21_ID)
                .setTargetP(100)
                .setTargetV(380)
                .setVoltageRegulatorOn(true)
                .setMaxP(100)
                .setMinP(0)
                .add();
        network.newLine()
                .setId(LINE_ID)
                .setVoltageLevel1(voltageLevel12.getId())
                .setBus1(BUS_12_ID)
                .setConnectableBus1(BUS_12_ID)
                .setVoltageLevel2(voltageLevel21.getId())
                .setBus2(BUS_21_ID)
                .setConnectableBus2(BUS_21_ID)
                .setR(3.0)
                .setX(33.0)
                .setG1(0.0)
                .setB1(386E-6 / 2)
                .setG2(0.0)
                .setB2(386E-6 / 2)
                .add();
        return network;
    }

    private void addDiagramData(Network network) {
        Load load = network.getLoad(LOAD_ID);
        InjectionDiagramData<Load> loadDiagramData = new InjectionDiagramData<>(load);
        InjectionDiagramData.InjectionDiagramDetails loadsDiagramDetails = loadDiagramData.new InjectionDiagramDetails(new DiagramPoint(10, 20, 0), 90);
        loadsDiagramDetails.addTerminalPoint(new DiagramPoint(15, 20, 1));
        loadsDiagramDetails.addTerminalPoint(new DiagramPoint(30, 20, 2));
        loadDiagramData.addData(DIAGRAM_ID, loadsDiagramDetails);
        load.addExtension(InjectionDiagramData.class, loadDiagramData);

        Bus bus11 = network.getVoltageLevel(VOLTAGE_LEVEL_11_ID).getBusBreakerView().getBus(BUS_11_ID);
        NodeDiagramData<Bus> busDiagramData11 = new NodeDiagramData<>(bus11);
        NodeDiagramData.NodeDiagramDataDetails diagramDetails11 = busDiagramData11.new NodeDiagramDataDetails();
        diagramDetails11.setPoint1(new DiagramPoint(30, 10, 1));
        diagramDetails11.setPoint2(new DiagramPoint(30, 30, 2));
        busDiagramData11.addData(DIAGRAM_ID, diagramDetails11);
        bus11.addExtension(NodeDiagramData.class, busDiagramData11);

        TwoWindingsTransformer twt = network.getTwoWindingsTransformer(TRANSFORMER_ID);
        CouplingDeviceDiagramData<TwoWindingsTransformer> twtDiagramData = new CouplingDeviceDiagramData<>(twt);
        CouplingDeviceDiagramData.CouplingDeviceDiagramDetails twtDiagramDetails = twtDiagramData.new CouplingDeviceDiagramDetails(new DiagramPoint(50, 20, 0), 90);
        twtDiagramDetails.addTerminalPoint(DiagramTerminal.TERMINAL1, new DiagramPoint(45, 20, 1));
        twtDiagramDetails.addTerminalPoint(DiagramTerminal.TERMINAL1, new DiagramPoint(30, 20, 2));
        twtDiagramDetails.addTerminalPoint(DiagramTerminal.TERMINAL2, new DiagramPoint(55, 20, 1));
        twtDiagramDetails.addTerminalPoint(DiagramTerminal.TERMINAL2, new DiagramPoint(70, 20, 2));
        twtDiagramData.addData(DIAGRAM_ID, twtDiagramDetails);
        twt.addExtension(CouplingDeviceDiagramData.class, twtDiagramData);

        Bus bus12 = network.getVoltageLevel(VOLTAGE_LEVEL_12_ID).getBusBreakerView().getBus(BUS_12_ID);
        NodeDiagramData<Bus> busDiagramData12 = new NodeDiagramData<>(bus12);
        NodeDiagramData.NodeDiagramDataDetails diagramDetails12 = busDiagramData11.new NodeDiagramDataDetails();
        diagramDetails12.setPoint1(new DiagramPoint(70, 10, 1));
        diagramDetails12.setPoint2(new DiagramPoint(70, 30, 2));
        busDiagramData12.addData(DIAGRAM_ID, diagramDetails12);
        bus12.addExtension(NodeDiagramData.class, busDiagramData12);

        Line line = network.getLine(LINE_ID);
        LineDiagramData<Line> lineDiagramData = new LineDiagramData<>(line);
        lineDiagramData.addPoint(DIAGRAM_ID, new DiagramPoint(70, 20, 1));
        lineDiagramData.addPoint(DIAGRAM_ID, new DiagramPoint(100, 20, 2));
        lineDiagramData.addPoint(DIAGRAM_ID, new DiagramPoint(100, 60, 3));
        lineDiagramData.addPoint(DIAGRAM_ID, new DiagramPoint(130, 60, 4));
        line.addExtension(LineDiagramData.class, lineDiagramData);

        Bus bus21 = network.getVoltageLevel(VOLTAGE_LEVEL_21_ID).getBusBreakerView().getBus(BUS_21_ID);
        NodeDiagramData<Bus> busDiagramData21 = new NodeDiagramData<>(bus21);
        NodeDiagramData.NodeDiagramDataDetails diagramDetails21 = busDiagramData21.new NodeDiagramDataDetails();
        diagramDetails21.setPoint1(new DiagramPoint(130, 50, 1));
        diagramDetails21.setPoint2(new DiagramPoint(130, 70, 2));
        busDiagramData21.addData(DIAGRAM_ID, diagramDetails21);
        bus21.addExtension(NodeDiagramData.class, busDiagramData21);

        Generator generator = network.getGenerator(GENERATOR_ID);
        InjectionDiagramData<Generator> generatorDiagramData = new InjectionDiagramData<>(generator);
        InjectionDiagramData.InjectionDiagramDetails diagramDetails = generatorDiagramData.new InjectionDiagramDetails(new DiagramPoint(150, 60, 0), 0);
        diagramDetails.addTerminalPoint(new DiagramPoint(145, 60, 1));
        diagramDetails.addTerminalPoint(new DiagramPoint(130, 60, 2));
        generatorDiagramData.addData(DIAGRAM_ID, diagramDetails);
        generator.addExtension(InjectionDiagramData.class, generatorDiagramData);

        NetworkDiagramData.addDiagramName(network, DIAGRAM_ID);
    }

    @Test
    public void test() {
        Network network = createNetwork();
        addDiagramData(network);
        ZoneId zoneId = ZoneId.create(Arrays.asList(SUBSTATION_1_ID, SUBSTATION_2_ID));
        ZoneGraph graph = new NetworkGraphBuilder(network).buildZoneGraph(zoneId, false);
        LayoutParameters layoutParameters = new LayoutParameters();
        layoutParameters.setScaleFactor(2);
        layoutParameters.setDiagramName(DIAGRAM_ID);
        new CgmesZoneLayout(graph, network).run(layoutParameters);

        assertEquals(2, graph.getNodes().size());
        assertEquals(2, graph.getNode(SUBSTATION_1_ID).getNodes().size());
        assertEquals(1, graph.getNode(SUBSTATION_2_ID).getNodes().size());
        assertEquals(SUBSTATION_1_ID, graph.getNodes().get(0).getSubstationId());
        assertEquals(SUBSTATION_2_ID, graph.getNodes().get(1).getSubstationId());

        Graph vlGraph11 = graph.getNode(SUBSTATION_1_ID).getNode(VOLTAGE_LEVEL_11_ID);
        assertEquals(3, vlGraph11.getNodes().size());
        assertEquals(2, vlGraph11.getEdges().size());
        checkNode(vlGraph11.getNodes().get(0), Node.NodeType.BUS, BUS_11_ID, BUSBAR_SECTION, Arrays.asList(LOAD_ID, TRANSFORMER_ID + "_" + Side.ONE), 60, 10, true);
        checkNode(vlGraph11.getNodes().get(1), Node.NodeType.FEEDER, LOAD_ID, LOAD, Arrays.asList(BUS_11_ID), 20, 30, true);
        checkNode(vlGraph11.getNodes().get(2), Node.NodeType.FEEDER, TRANSFORMER_ID + "_" + Side.ONE, LINE, Arrays.asList(BUS_11_ID), 100, 30, false);

        Graph vlGraph12 = graph.getNode(SUBSTATION_1_ID).getNode(VOLTAGE_LEVEL_12_ID);
        assertEquals(3, vlGraph12.getNodes().size());
        assertEquals(2, vlGraph12.getEdges().size());
        checkNode(vlGraph12.getNodes().get(0), Node.NodeType.BUS, BUS_12_ID, BUSBAR_SECTION, Arrays.asList(LINE_ID + "_" + Side.ONE, TRANSFORMER_ID + "_" + Side.TWO), 140, 10, true);
        checkNode(vlGraph12.getNodes().get(1), Node.NodeType.FEEDER, TRANSFORMER_ID + "_" + Side.TWO, LINE, Arrays.asList(BUS_12_ID), 100, 30, false);
        checkNode(vlGraph12.getNodes().get(2), Node.NodeType.FEEDER, LINE_ID + "_" + Side.ONE, LINE, Arrays.asList(BUS_12_ID), 180, 30, true);

        Graph vlGraph21 = graph.getNode(SUBSTATION_2_ID).getNode(VOLTAGE_LEVEL_21_ID);
        assertEquals(3, vlGraph21.getNodes().size());
        assertEquals(2, vlGraph21.getEdges().size());
        checkNode(vlGraph21.getNodes().get(0), Node.NodeType.BUS, BUS_21_ID, BUSBAR_SECTION, Arrays.asList(LINE_ID + "_" + Side.TWO, GENERATOR_ID), 260, 90, true);
        checkNode(vlGraph21.getNodes().get(1), Node.NodeType.FEEDER, GENERATOR_ID, GENERATOR, Arrays.asList(BUS_21_ID), 300, 110, false);
        checkNode(vlGraph21.getNodes().get(2), Node.NodeType.FEEDER, LINE_ID + "_" + Side.TWO, LINE, Arrays.asList(BUS_21_ID), 220, 110, true);

        assertEquals(1, graph.getEdges().size());
        LineEdge linEdge = graph.getEdges().get(0);
        assertEquals(LINE_ID, linEdge.getLineId());
        assertEquals(LINE_ID + "_" + Side.ONE, linEdge.getNode1().getId());
        assertEquals(LINE_ID + "_" + Side.TWO, linEdge.getNode2().getId());
        List<Point> points = linEdge.getPoints();
        assertEquals(4, points.size());
        checkLinePointCoordinates(points.get(0), 180, 30);
        checkLinePointCoordinates(points.get(1), 200, 30);
        checkLinePointCoordinates(points.get(2), 200, 110);
        checkLinePointCoordinates(points.get(3), 220, 110);
    }

    private void checkNode(Node node, Node.NodeType type, String id, String componentType,
                           List<String> expectedAdjacentNodes, double x, double y, boolean rotated) {
        assertEquals(type, node.getType());
        assertEquals(id, node.getId());
        assertEquals(componentType, node.getComponentType());
        assertEquals(expectedAdjacentNodes.size(), node.getAdjacentNodes().size());
        checkAdjacentNodes(node, expectedAdjacentNodes);
        checkNodeCoordinates(node, x, y, rotated);
    }

    private void checkAdjacentNodes(Node node, List<String> expectedAdjacentNodes) {
        node.getAdjacentNodes().forEach(adjacentNode -> {
            assertTrue(expectedAdjacentNodes.contains(adjacentNode.getId()));
        });
    }

    private void checkNodeCoordinates(Node node, double x, double y, boolean rotated) {
        assertEquals(x, node.getX(), 0);
        assertEquals(y, node.getY(), 0);
        if (rotated) {
            assertTrue(node.isRotated());
        } else {
            assertFalse(node.isRotated());
        }
    }

    private void checkLinePointCoordinates(Point point, int x, int y) {
        assertEquals(x, point.getX(), 0);
        assertEquals(y, point.getY(), 0);
    }

}
