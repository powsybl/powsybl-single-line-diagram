/**
 * Copyright (c) 2019-2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.cgmes.layout;

import com.powsybl.sld.cgmes.dl.iidm.extensions.*;
import com.powsybl.iidm.network.*;
import com.powsybl.sld.model.VoltageLevelGraph;
import com.powsybl.sld.model.Node;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static com.powsybl.sld.library.ComponentTypeName.BREAKER;
import static com.powsybl.sld.library.ComponentTypeName.BUSBAR_SECTION;
import static com.powsybl.sld.library.ComponentTypeName.DISCONNECTOR;
import static com.powsybl.sld.library.ComponentTypeName.GENERATOR;
import static com.powsybl.sld.library.ComponentTypeName.LINE;
import static org.junit.Assert.assertEquals;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public abstract class AbstractNodeTopologyTest extends AbstractCgmesVoltageLevelLayoutTest {

    protected VoltageLevel voltageLevel;
    protected VoltageLevel voltageLevelWithInternalConnections;

    @Before
    public void setUp() {
        createNetwork();
        createNetworkWithInternalConnections();
    }

    protected void createNetwork() {
        Network network = Network.create("testCase1", "test");
        voltageLevel = createFirstVoltageLevel(network, 0, 4, 0, 1, 1, 2, 2, 3);
        voltageLevel.getNodeBreakerView().newInternalConnection()
                .setNode1(4)
                .setNode2(0)
                .add();
        createSecondVoltageLevel(network);
        createLine(network, 3);
        addDiagramData(network);
        NetworkDiagramData.addDiagramName(network, DIAGRAM_NAME, "Substation");
        NetworkDiagramData.addDiagramName(network, DIAGRAM_NAME, "Substation2");
    }

    protected void createNetworkWithInternalConnections() {
        Network network = Network.create("testCase1", "test");
        voltageLevelWithInternalConnections = createFirstVoltageLevel(network, 4, 12, 7, 8, 5, 6, 9, 10);
        voltageLevelWithInternalConnections.getNodeBreakerView().newInternalConnection()
                .setNode1(4)
                .setNode2(0)
                .add();
        voltageLevelWithInternalConnections.getNodeBreakerView().newInternalConnection()
                .setNode1(5)
                .setNode2(2)
                .add();
        voltageLevelWithInternalConnections.getNodeBreakerView().newInternalConnection()
                .setNode1(6)
                .setNode2(1)
                .add();
        voltageLevelWithInternalConnections.getNodeBreakerView().newInternalConnection()
                .setNode1(7)
                .setNode2(1)
                .add();
        voltageLevelWithInternalConnections.getNodeBreakerView().newInternalConnection()
                .setNode1(8)
                .setNode2(0)
                .add();
        voltageLevelWithInternalConnections.getNodeBreakerView().newInternalConnection()
                .setNode1(9)
                .setNode2(3)
                .add();
        voltageLevelWithInternalConnections.getNodeBreakerView().newInternalConnection()
                .setNode1(10)
                .setNode2(2)
                .add();
        voltageLevelWithInternalConnections.getNodeBreakerView().newInternalConnection()
                .setNode1(11)
                .setNode2(3)
                .add();
        voltageLevelWithInternalConnections.getNodeBreakerView().newInternalConnection()
                .setNode1(12)
                .setNode2(0)
                .add();
        createSecondVoltageLevel(network);
        createLine(network, 11);
        addDiagramData(network);
        NetworkDiagramData.addDiagramName(network, DIAGRAM_NAME, "Substation");
        NetworkDiagramData.addDiagramName(network, DIAGRAM_NAME, "Substation2");
    }

    protected VoltageLevel createFirstVoltageLevel(Network network, int busbarNode, int generatorNode, int disconnector1Node1, int disconnector1Node2,
                                            int breaker1Node1, int breaker1Node2, int disconnector2Node1, int disconnector2Node2) {
        Substation substation1 = network.newSubstation()
                .setId("Substation")
                .setCountry(Country.FR)
                .add();
        VoltageLevel voltageLevel1 = substation1.newVoltageLevel()
                .setId("VoltageLevel1")
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .setNominalV(400)
                .add();
        voltageLevel1.getNodeBreakerView().newBusbarSection()
                .setId("BusbarSection")
                .setNode(busbarNode)
                .add();
        voltageLevel1.newGenerator()
                .setId("Generator")
                .setNode(generatorNode)
                .setTargetP(100)
                .setTargetV(380)
                .setVoltageRegulatorOn(true)
                .setMaxP(100)
                .setMinP(0)
                .add();
        voltageLevel1.getNodeBreakerView().newDisconnector()
                .setId("Disconnector1")
                .setNode1(disconnector1Node1)
                .setNode2(disconnector1Node2)
                .add();
        voltageLevel1.getNodeBreakerView().newBreaker()
                .setId("Breaker1")
                .setNode1(breaker1Node1)
                .setNode2(breaker1Node2)
                .add();
        voltageLevel1.getNodeBreakerView().newDisconnector()
                .setId("Disconnector2")
                .setNode1(disconnector2Node1)
                .setNode2(disconnector2Node2)
                .add();
        return voltageLevel1;
    }

    protected void createSecondVoltageLevel(Network network) {
        Substation substation2 = network.newSubstation()
                .setId("Substation2")
                .setCountry(Country.FR)
                .add();
        VoltageLevel voltageLevel2 = substation2.newVoltageLevel()
                .setId("VoltageLevel2")
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .setNominalV(400)
                .add();
    }

    protected void createLine(Network network, int lineNode) {
        network.newLine()
                .setId("Line")
                .setVoltageLevel1("VoltageLevel1")
                .setNode1(lineNode)
                .setVoltageLevel2("VoltageLevel2")
                .setNode2(0)
                .setR(3.0)
                .setX(33.0)
                .setG1(0.0)
                .setB1(386E-6 / 2)
                .setG2(0.0)
                .setB2(386E-6 / 2)
                .add();
    }

    protected abstract void addDiagramData(Network network);

    protected void addBusbarSectionDiagramData(BusbarSection busbarSection, DiagramPoint point1, DiagramPoint point2) {
        NodeDiagramData<BusbarSection> busbarDiagramData = new NodeDiagramData<>(busbarSection);
        NodeDiagramData.NodeDiagramDataDetails diagramDetails = busbarDiagramData.new NodeDiagramDataDetails();
        diagramDetails.setPoint1(point1);
        diagramDetails.setPoint2(point2);
        busbarDiagramData.addData(DIAGRAM_NAME, diagramDetails);
        busbarSection.addExtension(NodeDiagramData.class, busbarDiagramData);
    }

    protected void addGeneratorDiagramData(Generator generator, DiagramPoint generatorPoint, DiagramPoint terminalPoint1, DiagramPoint terminalPoint2) {
        InjectionDiagramData<Generator> generatorDiagramData = new InjectionDiagramData<>(generator);
        InjectionDiagramData.InjectionDiagramDetails diagramDetails = generatorDiagramData.new InjectionDiagramDetails(generatorPoint, 0);
        diagramDetails.addTerminalPoint(terminalPoint1);
        diagramDetails.addTerminalPoint(terminalPoint2);
        generatorDiagramData.addData(DIAGRAM_NAME, diagramDetails);
        generator.addExtension(InjectionDiagramData.class, generatorDiagramData);
    }

    protected void addSwitchDiagramData(Switch sw, DiagramPoint switchPoint, int rotation, DiagramPoint terminal1Point1, DiagramPoint terminal1Point2,
                                        DiagramPoint terminal2Point1, DiagramPoint terminal2Point2) {
        CouplingDeviceDiagramData<Switch> switchDiagramData = new CouplingDeviceDiagramData<>(sw);
        CouplingDeviceDiagramData.CouplingDeviceDiagramDetails diagramDetails = switchDiagramData.new CouplingDeviceDiagramDetails(switchPoint, rotation);
        diagramDetails.addTerminalPoint(DiagramTerminal.TERMINAL1, terminal1Point1);
        diagramDetails.addTerminalPoint(DiagramTerminal.TERMINAL1, terminal1Point2);
        diagramDetails.addTerminalPoint(DiagramTerminal.TERMINAL2, terminal2Point1);
        diagramDetails.addTerminalPoint(DiagramTerminal.TERMINAL2, terminal2Point2);
        switchDiagramData.addData(DIAGRAM_NAME, diagramDetails);
        sw.addExtension(CouplingDeviceDiagramData.class, switchDiagramData);
    }

    protected void addLineDiagramData(Line line, DiagramPoint point1, DiagramPoint point2) {
        LineDiagramData<Line> lineDiagramData = new LineDiagramData<>(line);
        lineDiagramData.addPoint(DIAGRAM_NAME, point1);
        lineDiagramData.addPoint(DIAGRAM_NAME, point2);
        line.addExtension(LineDiagramData.class, lineDiagramData);
    }

    @Override
    protected void checkGraph(VoltageLevelGraph graph) {
        assertEquals(6, graph.getNodes().size());

        assertEquals(Node.NodeType.BUS, graph.getNodes().get(0).getType());
        assertEquals(Node.NodeType.FEEDER, graph.getNodes().get(1).getType());
        assertEquals(Node.NodeType.FEEDER, graph.getNodes().get(2).getType());
        assertEquals(Node.NodeType.SWITCH, graph.getNodes().get(3).getType());
        assertEquals(Node.NodeType.SWITCH, graph.getNodes().get(4).getType());
        assertEquals(Node.NodeType.SWITCH, graph.getNodes().get(5).getType());

        assertEquals("BusbarSection", graph.getNodes().get(0).getId());
        assertEquals("Line_ONE", graph.getNodes().get(1).getId());
        assertEquals("Generator", graph.getNodes().get(2).getId());
        assertEquals("Disconnector1", graph.getNodes().get(3).getId());
        assertEquals("Breaker1", graph.getNodes().get(4).getId());
        assertEquals("Disconnector2", graph.getNodes().get(5).getId());

        assertEquals(BUSBAR_SECTION, graph.getNodes().get(0).getComponentType());
        assertEquals(LINE, graph.getNodes().get(1).getComponentType());
        assertEquals(GENERATOR, graph.getNodes().get(2).getComponentType());
        assertEquals(DISCONNECTOR, graph.getNodes().get(3).getComponentType());
        assertEquals(BREAKER, graph.getNodes().get(4).getComponentType());
        assertEquals(DISCONNECTOR, graph.getNodes().get(5).getComponentType());

        assertEquals(2, graph.getNodes().get(0).getAdjacentNodes().size());
        checkAdjacentNodes(graph.getNodes().get(0), Arrays.asList("Disconnector1", "Generator"));
        assertEquals(1, graph.getNodes().get(1).getAdjacentNodes().size());
        assertEquals("Disconnector2", graph.getNodes().get(1).getAdjacentNodes().get(0).getId());
        assertEquals(1, graph.getNodes().get(2).getAdjacentNodes().size());
        assertEquals("BusbarSection", graph.getNodes().get(2).getAdjacentNodes().get(0).getId());
        assertEquals(2, graph.getNodes().get(3).getAdjacentNodes().size());
        checkAdjacentNodes(graph.getNodes().get(3), Arrays.asList("BusbarSection", "Breaker1"));
        assertEquals(2, graph.getNodes().get(4).getAdjacentNodes().size());
        checkAdjacentNodes(graph.getNodes().get(4), Arrays.asList("Disconnector1", "Disconnector2"));
        assertEquals(2, graph.getNodes().get(5).getAdjacentNodes().size());
        checkAdjacentNodes(graph.getNodes().get(5), Arrays.asList("Breaker1", "Line_ONE"));

        assertEquals(5, graph.getEdges().size());
    }

    @Test
    public void test() {
        test(voltageLevel);
    }

    @Test
    public void testWithInternalConnections() {
        test(voltageLevelWithInternalConnections);
    }

}
