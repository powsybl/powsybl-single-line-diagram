/**
 * Copyright (c) 2019-2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.cgmes.layout;

import static com.powsybl.sld.library.ComponentTypeName.BUSBAR_SECTION;
import static com.powsybl.sld.library.ComponentTypeName.CAPACITOR;
import static com.powsybl.sld.library.ComponentTypeName.DANGLING_LINE;
import static com.powsybl.sld.library.ComponentTypeName.GENERATOR;
import static com.powsybl.sld.library.ComponentTypeName.LOAD;
import static com.powsybl.sld.library.ComponentTypeName.STATIC_VAR_COMPENSATOR;
import static com.powsybl.sld.library.ComponentTypeName.THREE_WINDINGS_TRANSFORMER_LEG;
import static com.powsybl.sld.library.ComponentTypeName.TWO_WINDINGS_TRANSFORMER;
import static com.powsybl.sld.library.ComponentTypeName.TWO_WINDINGS_TRANSFORMER_LEG;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Country;
import com.powsybl.iidm.network.DanglingLine;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.ShuntCompensator;
import com.powsybl.iidm.network.StaticVarCompensator;
import com.powsybl.iidm.network.Substation;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
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
import com.powsybl.sld.cgmes.dl.iidm.extensions.ThreeWindingsTransformerDiagramData;
import com.powsybl.sld.layout.LayoutParameters;
import com.powsybl.sld.model.BusNode;
import com.powsybl.sld.model.VoltageLevelGraph;
import com.powsybl.sld.model.Node;
import com.powsybl.sld.model.SubstationGraph;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public class BusTopologyTest extends AbstractCgmesVoltageLevelLayoutTest {

    private VoltageLevel voltageLevel;
    private Substation substation;
    private VoltageLevel voltageLevel2;
    Network network;
    private VoltageLevel voltageLevel21;
    private VoltageLevel voltageLevel22;
    private VoltageLevel voltageLevel23;
    private Substation substation2;
    Network networkWith3WT;

    @Before
    public void setUp() {
        createNetwork();
        createNetworkWith3WT();
    }

    private void createNetwork() {
        network = Network.create("test", "test");
        substation = network.newSubstation()
                .setId("Substation")
                .setCountry(Country.FR)
                .add();
        voltageLevel = createFirstVoltageLevel(substation);
        voltageLevel2 = createSecondVoltageLevel(substation);
        createTransformer(substation);
        addFirstVoltageLevelDiagramData(network, voltageLevel);
        addSecondVoltageLevelDiagramData(network, voltageLevel2);
        addTransformerDiagramData(network);
        NetworkDiagramData.addDiagramName(network, DIAGRAM_NAME, "Substation");
    }

    private void createNetworkWith3WT() {
        networkWith3WT = Network.create("test", "test");
        substation2 = networkWith3WT.newSubstation()
                .setId("Substation")
                .setCountry(Country.FR)
                .add();
        voltageLevel21 = createFirstVoltageLevel(substation2);
        voltageLevel22 = createSecondVoltageLevel(substation2);
        voltageLevel23 = createThirdVoltageLevel(substation2);
        create3WTransformer(substation2);
        addFirstVoltageLevelDiagramData(networkWith3WT, voltageLevel21);
        addSecondVoltageLevelDiagramData(networkWith3WT, voltageLevel22);
        addThirdVoltageLevelDiagramData(networkWith3WT, voltageLevel23);
        add3WTransformerDiagramData(networkWith3WT);
        NetworkDiagramData.addDiagramName(networkWith3WT, DIAGRAM_NAME, "Substation");
    }

    private VoltageLevel createFirstVoltageLevel(Substation substation) {
        VoltageLevel voltageLevel1 = substation.newVoltageLevel()
                .setId("VoltageLevel1")
                .setNominalV(400)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        voltageLevel1.getBusBreakerView().newBus()
                .setId("Bus1")
                .add();
        voltageLevel1.newLoad()
                .setId("Load")
                .setBus("Bus1")
                .setConnectableBus("Bus1")
                .setP0(100)
                .setQ0(50)
                .add();
        voltageLevel1.newShuntCompensator()
                .setId("Shunt")
                .setBus("Bus1")
                .setConnectableBus("Bus1")
                .setSectionCount(1)
                .newLinearModel()
                    .setBPerSection(1e-5)
                    .setMaximumSectionCount(1)
                .add()
                .add();
        voltageLevel1.newDanglingLine()
                .setId("DanglingLine")
                .setBus("Bus1")
                .setR(10.0)
                .setX(1.0)
                .setB(10e-6)
                .setG(10e-5)
                .setP0(50.0)
                .setQ0(30.0)
                .add();
        return voltageLevel1;
    }

    private VoltageLevel createSecondVoltageLevel(Substation substation) {
        VoltageLevel voltageLevel2 = substation.newVoltageLevel()
                .setId("VoltageLevel2")
                .setNominalV(400)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        voltageLevel2.getBusBreakerView().newBus()
                .setId("Bus2")
                .add();
        voltageLevel2.newStaticVarCompensator()
                .setId("Svc")
                .setConnectableBus("Bus2")
                .setBus("Bus2")
                .setBmin(0.0002)
                .setBmax(0.0008)
                .setRegulationMode(StaticVarCompensator.RegulationMode.VOLTAGE)
                .setVoltageSetPoint(390.0)
                .setReactivePowerSetPoint(1.0)
                .add();
        return voltageLevel2;
    }

    private void createTransformer(Substation substation) {
        int zb380 = 380 * 380 / 100;
        substation.newTwoWindingsTransformer()
                .setId("Transformer")
                .setVoltageLevel1("VoltageLevel1")
                .setBus1("Bus1")
                .setConnectableBus1("Bus1")
                .setRatedU1(24.0)
                .setVoltageLevel2("VoltageLevel2")
                .setBus2("Bus2")
                .setConnectableBus2("Bus2")
                .setRatedU2(400.0)
                .setR(0.24 / 1300 * zb380)
                .setX(Math.sqrt(10 * 10 - 0.24 * 0.24) / 1300 * zb380)
                .setG(0.0)
                .setB(0.0)
                .add();
    }

    private VoltageLevel createThirdVoltageLevel(Substation substation) {
        VoltageLevel voltageLevel3 = substation.newVoltageLevel()
                .setId("VoltageLevel3")
                .setNominalV(400)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        voltageLevel3.getBusBreakerView().newBus()
                .setId("Bus3")
                .add();
        voltageLevel3.newGenerator()
                .setId("Generator")
                .setBus("Bus3")
                .setConnectableBus("Bus3")
                .setTargetP(100)
                .setTargetV(380)
                .setVoltageRegulatorOn(true)
                .setMaxP(100)
                .setMinP(0)
                .add();
        return voltageLevel3;
    }

    private void create3WTransformer(Substation substation) {
        substation.newThreeWindingsTransformer()
                .setId("Transformer")
                .newLeg1()
                    .setR(17.424)
                    .setX(1.7424)
                    .setB(0.000573921028466483)
                    .setG(0.00573921028466483)
                    .setRatedU(132.0)
                    .setVoltageLevel("VoltageLevel1")
                    .setBus("Bus1")
                    .add()
                .newLeg2()
                    .setR(1.089)
                    .setX(0.1089)
                    .setRatedU(33.0)
                    .setVoltageLevel("VoltageLevel2")
                    .setBus("Bus2")
                    .add()
                .newLeg3()
                    .setR(0.121)
                    .setX(0.0121)
                    .setRatedU(11.0)
                    .setVoltageLevel("VoltageLevel3")
                    .setBus("Bus3")
                    .add()
                .add();
    }

    private void addFirstVoltageLevelDiagramData(Network network, VoltageLevel voltageLevel) {
        Bus bus = voltageLevel.getBusBreakerView().getBus("Bus1");
        NodeDiagramData<Bus> busDiagramData = new NodeDiagramData<>(bus);
        NodeDiagramData.NodeDiagramDataDetails diagramDetails = busDiagramData.new NodeDiagramDataDetails();
        diagramDetails.setPoint1(new DiagramPoint(60, 10, 1));
        diagramDetails.setPoint2(new DiagramPoint(60, 70, 2));
        busDiagramData.addData(DIAGRAM_NAME, diagramDetails);
        bus.addExtension(NodeDiagramData.class, busDiagramData);

        Load load = network.getLoad("Load");
        InjectionDiagramData<Load> loadDiagramData = new InjectionDiagramData<>(load);
        InjectionDiagramData.InjectionDiagramDetails loadsDiagramDetails = loadDiagramData.new InjectionDiagramDetails(new DiagramPoint(10, 20, 0), 90);
        loadsDiagramDetails.addTerminalPoint(new DiagramPoint(15, 20, 2));
        loadsDiagramDetails.addTerminalPoint(new DiagramPoint(60, 20, 1));
        loadDiagramData.addData(DIAGRAM_NAME, loadsDiagramDetails);
        load.addExtension(InjectionDiagramData.class, loadDiagramData);

        ShuntCompensator shunt = network.getShuntCompensator("Shunt");
        InjectionDiagramData<ShuntCompensator> shuntDiagramData = new InjectionDiagramData<>(shunt);
        InjectionDiagramData.InjectionDiagramDetails shuntDiagramDetails = shuntDiagramData.new InjectionDiagramDetails(new DiagramPoint(15, 55, 0), 90);
        shuntDiagramDetails.addTerminalPoint(new DiagramPoint(20, 55, 1));
        shuntDiagramDetails.addTerminalPoint(new DiagramPoint(60, 55, 2));
        shuntDiagramData.addData(DIAGRAM_NAME, shuntDiagramDetails);
        shunt.addExtension(InjectionDiagramData.class, shuntDiagramData);

        DanglingLine danglingLine = network.getDanglingLine("DanglingLine");
        LineDiagramData<DanglingLine> danglingLineDiagramData = new LineDiagramData<>(danglingLine);
        danglingLineDiagramData.addPoint(DIAGRAM_NAME, new DiagramPoint(60, 60, 1));
        danglingLineDiagramData.addPoint(DIAGRAM_NAME, new DiagramPoint(120, 60, 2));
        danglingLine.addExtension(LineDiagramData.class, danglingLineDiagramData);
    }

    private void addSecondVoltageLevelDiagramData(Network network, VoltageLevel voltageLevel) {
        Bus bus2 = voltageLevel.getBusBreakerView().getBus("Bus2");
        NodeDiagramData<Bus> bus2DiagramData = new NodeDiagramData<>(bus2);
        NodeDiagramData.NodeDiagramDataDetails diagramDetails2 = bus2DiagramData.new NodeDiagramDataDetails();
        diagramDetails2.setPoint1(new DiagramPoint(120, 10, 1));
        diagramDetails2.setPoint2(new DiagramPoint(120, 25, 2));
        bus2DiagramData.addData(DIAGRAM_NAME, diagramDetails2);
        bus2.addExtension(NodeDiagramData.class, bus2DiagramData);

        StaticVarCompensator svc = network.getStaticVarCompensator("Svc");
        InjectionDiagramData<StaticVarCompensator> svcDiagramData = new InjectionDiagramData<>(svc);
        InjectionDiagramData.InjectionDiagramDetails svcDiagramDataDetails = svcDiagramData.new InjectionDiagramDetails(new DiagramPoint(140, 15, 0), 270);
        svcDiagramDataDetails.addTerminalPoint(new DiagramPoint(135, 15, 1));
        svcDiagramDataDetails.addTerminalPoint(new DiagramPoint(120, 15, 2));
        svcDiagramData.addData(DIAGRAM_NAME, svcDiagramDataDetails);
        svc.addExtension(InjectionDiagramData.class, svcDiagramData);
    }

    private void addTransformerDiagramData(Network network) {
        TwoWindingsTransformer twt = network.getTwoWindingsTransformer("Transformer");
        CouplingDeviceDiagramData<TwoWindingsTransformer> twtDiagramData = new CouplingDeviceDiagramData<>(twt);
        CouplingDeviceDiagramData.CouplingDeviceDiagramDetails twtDiagramDetails = twtDiagramData.new CouplingDeviceDiagramDetails(new DiagramPoint(100, 15, 0), 90);
        twtDiagramDetails.addTerminalPoint(DiagramTerminal.TERMINAL1, new DiagramPoint(95, 15, 1));
        twtDiagramDetails.addTerminalPoint(DiagramTerminal.TERMINAL1, new DiagramPoint(60, 15, 2));
        twtDiagramDetails.addTerminalPoint(DiagramTerminal.TERMINAL2, new DiagramPoint(105, 15, 1));
        twtDiagramDetails.addTerminalPoint(DiagramTerminal.TERMINAL2, new DiagramPoint(120, 15, 2));
        twtDiagramData.addData(DIAGRAM_NAME, twtDiagramDetails);
        twt.addExtension(CouplingDeviceDiagramData.class, twtDiagramData);
    }

    private void addThirdVoltageLevelDiagramData(Network network, VoltageLevel voltageLevel) {
        Bus bus3 = voltageLevel.getBusBreakerView().getBus("Bus3");
        NodeDiagramData<Bus> bus3DiagramData = new NodeDiagramData<>(bus3);
        NodeDiagramData.NodeDiagramDataDetails diagramDetails2 = bus3DiagramData.new NodeDiagramDataDetails();
        diagramDetails2.setPoint1(new DiagramPoint(80, 40, 1));
        diagramDetails2.setPoint2(new DiagramPoint(120, 40, 2));
        bus3DiagramData.addData(DIAGRAM_NAME, diagramDetails2);
        bus3.addExtension(NodeDiagramData.class, bus3DiagramData);

        Generator generator = network.getGenerator("Generator");
        InjectionDiagramData<Generator> generatorDiagramData = new InjectionDiagramData<>(generator);
        InjectionDiagramData.InjectionDiagramDetails genDiagramDataDetails = generatorDiagramData.new InjectionDiagramDetails(new DiagramPoint(100, 60, 0), 90);
        genDiagramDataDetails.addTerminalPoint(new DiagramPoint(100, 55, 1));
        genDiagramDataDetails.addTerminalPoint(new DiagramPoint(100, 40, 2));
        generatorDiagramData.addData(DIAGRAM_NAME, genDiagramDataDetails);
        generator.addExtension(InjectionDiagramData.class, generatorDiagramData);
    }

    private void add3WTransformerDiagramData(Network network) {
        ThreeWindingsTransformer twt = network.getThreeWindingsTransformer("Transformer");
        ThreeWindingsTransformerDiagramData twtDiagramData = new ThreeWindingsTransformerDiagramData(twt);
        ThreeWindingsTransformerDiagramData.ThreeWindingsTransformerDiagramDataDetails twtDiagramDetails = twtDiagramData.new ThreeWindingsTransformerDiagramDataDetails(new DiagramPoint(100, 15, 0), 90);
        twtDiagramDetails.addTerminalPoint(DiagramTerminal.TERMINAL1, new DiagramPoint(95, 15, 1));
        twtDiagramDetails.addTerminalPoint(DiagramTerminal.TERMINAL1, new DiagramPoint(60, 15, 2));
        twtDiagramDetails.addTerminalPoint(DiagramTerminal.TERMINAL2, new DiagramPoint(105, 15, 1));
        twtDiagramDetails.addTerminalPoint(DiagramTerminal.TERMINAL2, new DiagramPoint(120, 15, 2));
        twtDiagramDetails.addTerminalPoint(DiagramTerminal.TERMINAL3, new DiagramPoint(100, 20, 1));
        twtDiagramDetails.addTerminalPoint(DiagramTerminal.TERMINAL3, new DiagramPoint(10, 40, 2));
        twtDiagramData.addData(DIAGRAM_NAME, twtDiagramDetails);
        twt.addExtension(ThreeWindingsTransformerDiagramData.class, twtDiagramData);
    }

    @Test
    public void testVoltageLevelLayout() {
        test(voltageLevel);
    }

    @Test
    public void testSubstationLayout() {
        SubstationGraph graph = new NetworkGraphBuilder(network).buildSubstationGraph(substation.getId(), false);
        LayoutParameters layoutParameters = new LayoutParameters();
        layoutParameters.setScaleFactor(2);
        layoutParameters.setDiagramName(DIAGRAM_NAME);
        new CgmesSubstationLayout(graph, network).run(layoutParameters);
        checkGraph(graph.getNode(voltageLevel.getId()));
        checkCoordinates(graph.getNode(voltageLevel.getId()));
        checkGraphVl2(graph.getNode(voltageLevel2.getId()));
        checkCoordinatesVl2(graph.getNode(voltageLevel2.getId()));
        checkSubstationTwt(graph, 2);
    }

    @Override
    protected void checkGraph(VoltageLevelGraph graph) {
        assertEquals(9, graph.getNodes().size());

        assertEquals(Node.NodeType.BUS, graph.getNodes().get(0).getType());
        assertEquals(Node.NodeType.FEEDER, graph.getNodes().get(1).getType());
        assertEquals(Node.NodeType.FICTITIOUS, graph.getNodes().get(2).getType());
        assertEquals(Node.NodeType.FEEDER, graph.getNodes().get(3).getType());
        assertEquals(Node.NodeType.FICTITIOUS, graph.getNodes().get(4).getType());
        assertEquals(Node.NodeType.FEEDER, graph.getNodes().get(5).getType());
        assertEquals(Node.NodeType.FICTITIOUS, graph.getNodes().get(6).getType());
        assertEquals(Node.NodeType.FEEDER, graph.getNodes().get(7).getType());
        assertEquals(Node.NodeType.FICTITIOUS, graph.getNodes().get(8).getType());

        assertEquals("Bus1", graph.getNodes().get(0).getId());
        assertEquals("Load", graph.getNodes().get(1).getId());
        assertEquals("Shunt", graph.getNodes().get(3).getId());
        assertEquals("DanglingLine", graph.getNodes().get(5).getId());
        assertEquals("Transformer_ONE", graph.getNodes().get(7).getId());

        assertEquals(BUSBAR_SECTION, graph.getNodes().get(0).getComponentType());
        assertEquals(LOAD, graph.getNodes().get(1).getComponentType());
        assertEquals(CAPACITOR, graph.getNodes().get(3).getComponentType());
        assertEquals(DANGLING_LINE, graph.getNodes().get(5).getComponentType());
        assertTrue(graph.getNodes().get(7).getComponentType().equals(TWO_WINDINGS_TRANSFORMER) ||
                graph.getNodes().get(7).getComponentType().equals(TWO_WINDINGS_TRANSFORMER_LEG) ||
            graph.getNodes().get(7).getComponentType().equals(THREE_WINDINGS_TRANSFORMER_LEG));

        assertEquals(4, graph.getNodes().get(0).getAdjacentNodes().size());
        checkAdjacentNodes(graph.getNodes().get(0), Arrays.asList("FICT_VoltageLevel1_Bus1_Load", "FICT_VoltageLevel1_Bus1_Shunt",
            "FICT_VoltageLevel1_Bus1_DanglingLine", "FICT_VoltageLevel1_Bus1_Transformer"));
        checkFictConnectorConnectionToBusBar(graph.getNodes().get(2));
        checkFictConnectorConnectionToBusBar(graph.getNodes().get(4));
        checkFictConnectorConnectionToBusBar(graph.getNodes().get(6));
        checkFictConnectorConnectionToBusBar(graph.getNodes().get(8));

        checkFictConnectorConnection(graph.getNodes().get(1));
        checkFictConnectorConnection(graph.getNodes().get(3));
        checkFictConnectorConnection(graph.getNodes().get(5));
        checkFictConnectorConnection(graph.getNodes().get(7));

        assertEquals(8, graph.getEdges().size());
    }

    private void checkFictConnectorConnectionToBusBar(Node node) {
        assertEquals(2, node.getAdjacentNodes().size());
        assertEquals("Bus1", node.getAdjacentNodes().get(0).getId());
    }

    private void checkFictConnectorConnection(Node node) {
        assertEquals(1, node.getAdjacentNodes().size());
        assertEquals("FICT_VoltageLevel1_Bus1_" + node.getName(), node.getAdjacentNodes().get(0).getId());
    }

    @Override
    protected void checkCoordinates(VoltageLevelGraph graph) {
        assertEquals(120, graph.getNodes().get(0).getX(), 0);
        assertEquals(10, graph.getNodes().get(0).getY(), 0);
        assertEquals(120, ((BusNode) graph.getNodes().get(0)).getPxWidth(), 0);
        assertTrue(graph.getNodes().get(0).isRotated());
        assertEquals(20, graph.getNodes().get(1).getX(), 0);
        assertEquals(30, graph.getNodes().get(1).getY(), 0);
        assertTrue(graph.getNodes().get(1).isRotated());
        assertEquals(30, graph.getNodes().get(3).getX(), 0);
        assertEquals(100, graph.getNodes().get(3).getY(), 0);
        assertTrue(graph.getNodes().get(3).isRotated());
        assertEquals(160, graph.getNodes().get(5).getX(), 0);
        assertEquals(110, graph.getNodes().get(5).getY(), 0);
        assertTrue(graph.getNodes().get(5).isRotated());
        assertEquals(200, graph.getNodes().get(7).getX(), 0);
        assertEquals(20, graph.getNodes().get(7).getY(), 0);
        assertFalse(graph.getNodes().get(7).isRotated());
    }

    private void checkGraphVl2(VoltageLevelGraph graph) {
        assertEquals(5, graph.getNodes().size());

        assertEquals(Node.NodeType.BUS, graph.getNodes().get(0).getType());
        assertEquals(Node.NodeType.FEEDER, graph.getNodes().get(1).getType());
        assertEquals(Node.NodeType.FICTITIOUS, graph.getNodes().get(2).getType());
        assertEquals(Node.NodeType.FEEDER, graph.getNodes().get(3).getType());
        assertEquals(Node.NodeType.FICTITIOUS, graph.getNodes().get(4).getType());

        assertEquals("Bus2", graph.getNodes().get(0).getId());
        assertEquals("Svc", graph.getNodes().get(1).getId());
        assertEquals("Transformer_TWO", graph.getNodes().get(3).getId());

        assertEquals(BUSBAR_SECTION, graph.getNodes().get(0).getComponentType());
        assertEquals(STATIC_VAR_COMPENSATOR, graph.getNodes().get(1).getComponentType());
        assertTrue(graph.getNodes().get(3).getComponentType().equals(TWO_WINDINGS_TRANSFORMER_LEG) ||
                   graph.getNodes().get(3).getComponentType().equals(THREE_WINDINGS_TRANSFORMER_LEG));

        assertEquals(2, graph.getNodes().get(0).getAdjacentNodes().size());
        assertEquals("FICT_VoltageLevel2_Bus2_Svc", graph.getNodes().get(0).getAdjacentNodes().get(0).getId());
        assertEquals("FICT_VoltageLevel2_Bus2_Transformer", graph.getNodes().get(0).getAdjacentNodes().get(1).getId());
        assertEquals(1, graph.getNodes().get(1).getAdjacentNodes().size());
        assertEquals("FICT_VoltageLevel2_Bus2_Svc", graph.getNodes().get(1).getAdjacentNodes().get(0).getId());
        assertEquals(2, graph.getNodes().get(2).getAdjacentNodes().size());
        assertEquals("FICT_VoltageLevel2_Bus2_Transformer", graph.getNodes().get(3).getAdjacentNodes().get(0).getId());
    }

    private void checkCoordinatesVl2(VoltageLevelGraph graph) {
        assertEquals(240, graph.getNodes().get(0).getX(), 0);
        assertEquals(10, graph.getNodes().get(0).getY(), 0);
        assertEquals(30, ((BusNode) graph.getNodes().get(0)).getPxWidth(), 0);
        assertTrue(graph.getNodes().get(0).isRotated());
        assertEquals(280, graph.getNodes().get(1).getX(), 0);
        assertEquals(20, graph.getNodes().get(1).getY(), 0);
        assertTrue(graph.getNodes().get(1).isRotated());
        assertEquals(200, graph.getNodes().get(3).getX(), 0);
        assertEquals(20, graph.getNodes().get(3).getY(), 0);
        assertFalse(graph.getNodes().get(2).isRotated());
    }

    private void checkGraphVl3(VoltageLevelGraph graph) {
        assertEquals(5, graph.getNodes().size());

        assertEquals(Node.NodeType.BUS, graph.getNodes().get(0).getType());
        assertEquals(Node.NodeType.FEEDER, graph.getNodes().get(1).getType());
        assertEquals(Node.NodeType.FICTITIOUS, graph.getNodes().get(2).getType());
        assertEquals(Node.NodeType.FEEDER, graph.getNodes().get(3).getType());
        assertEquals(Node.NodeType.FICTITIOUS, graph.getNodes().get(4).getType());

        assertEquals("Bus3", graph.getNodes().get(0).getId());
        assertEquals("Generator", graph.getNodes().get(1).getId());
        assertEquals("Transformer_THREE", graph.getNodes().get(3).getId());

        assertEquals(BUSBAR_SECTION, graph.getNodes().get(0).getComponentType());
        assertEquals(GENERATOR, graph.getNodes().get(1).getComponentType());
        assertEquals(THREE_WINDINGS_TRANSFORMER_LEG, graph.getNodes().get(3).getComponentType());

        assertEquals(2, graph.getNodes().get(0).getAdjacentNodes().size());
        assertEquals("FICT_VoltageLevel3_Bus3_Generator", graph.getNodes().get(0).getAdjacentNodes().get(0).getId());
        assertEquals("FICT_VoltageLevel3_Bus3_Transformer", graph.getNodes().get(0).getAdjacentNodes().get(1).getId());
        assertEquals(1, graph.getNodes().get(1).getAdjacentNodes().size());
        assertEquals("FICT_VoltageLevel3_Bus3_Generator", graph.getNodes().get(1).getAdjacentNodes().get(0).getId());
        assertEquals(1, graph.getNodes().get(3).getAdjacentNodes().size());
        assertEquals("Bus3", graph.getNodes().get(2).getAdjacentNodes().get(0).getId());
    }

    private void checkCoordinatesVl3(VoltageLevelGraph graph) {
        assertEquals(160, graph.getNodes().get(0).getX(), 0);
        assertEquals(70, graph.getNodes().get(0).getY(), 0);
        assertEquals(80, ((BusNode) graph.getNodes().get(0)).getPxWidth(), 0);
        assertFalse(graph.getNodes().get(0).isRotated());
        assertEquals(200, graph.getNodes().get(1).getX(), 0);
        assertEquals(110, graph.getNodes().get(1).getY(), 0);
        assertEquals(200, graph.getNodes().get(3).getX(), 0);
        assertEquals(20, graph.getNodes().get(3).getY(), 0);
        assertFalse(graph.getNodes().get(2).isRotated());
    }

    private void checkSubstationTwt(SubstationGraph graph, int edgesNumber) {
        assertEquals(edgesNumber, graph.getTwtEdges().size());
        assertEquals(1, graph.getMultiTermNodes().size());
        assertEquals(200, graph.getMultiTermNodes().get(0).getX(), 0);
        assertEquals(20, graph.getMultiTermNodes().get(0).getY(), 0);
    }

    @Test
    public void testSubstationLayout3WT() {
        SubstationGraph graph = new NetworkGraphBuilder(networkWith3WT).buildSubstationGraph(substation2.getId(), false);
        LayoutParameters layoutParameters = new LayoutParameters();
        layoutParameters.setScaleFactor(2);
        layoutParameters.setDiagramName(DIAGRAM_NAME);
        new CgmesSubstationLayout(graph, networkWith3WT).run(layoutParameters);
        checkGraph(graph.getNode(voltageLevel21.getId()));
        checkCoordinates(graph.getNode(voltageLevel21.getId()));
        checkGraphVl2(graph.getNode(voltageLevel22.getId()));
        checkCoordinatesVl2(graph.getNode(voltageLevel22.getId()));
        checkGraphVl3(graph.getNode(voltageLevel23.getId()));
        checkCoordinatesVl3(graph.getNode(voltageLevel23.getId()));
        checkSubstationTwt(graph, 3);
    }

}
