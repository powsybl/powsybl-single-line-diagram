/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.cgmes.dl.conversion;

import com.powsybl.iidm.network.*;
import com.powsybl.sld.cgmes.dl.iidm.extensions.*;
import com.powsybl.triplestore.api.PropertyBags;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public class CgmesDLImporterTest extends AbstractCgmesDLTest {

    protected static String OTHER_DIAGRAM_NAME = "diagram-1";

    private CgmesDLModel cgmesDLModel;

    @Before
    public void setUp() {
        super.setUp();
        addOtherDiagram();
        cgmesDLModel = Mockito.mock(CgmesDLModel.class);
        Mockito.when(cgmesDLModel.getTerminalsDiagramData()).thenReturn(terminalsPropertyBags);
        Mockito.when(cgmesDLModel.getBusesDiagramData()).thenReturn(busesPropertyBags);
        Mockito.when(cgmesDLModel.getBusbarsDiagramData()).thenReturn(busbarsPropertyBags);
        Mockito.when(cgmesDLModel.getLinesDiagramData()).thenReturn(linesPropertyBags);
        Mockito.when(cgmesDLModel.getGeneratorsDiagramData()).thenReturn(generatorsPropertyBags);
        Mockito.when(cgmesDLModel.getLoadsDiagramData()).thenReturn(loadsPropertyBags);
        Mockito.when(cgmesDLModel.getShuntsDiagramData()).thenReturn(shuntsPropertyBags);
        Mockito.when(cgmesDLModel.getSwitchesDiagramData()).thenReturn(switchesPropertyBags);
        Mockito.when(cgmesDLModel.getTransformersDiagramData()).thenReturn(tranformersPropertyBags);
        Mockito.when(cgmesDLModel.getHvdcLinesDiagramData()).thenReturn(hvdcLinesPropertyBags);
        Mockito.when(cgmesDLModel.getSvcsDiagramData()).thenReturn(svcsPropertyBags);
        Mockito.when(cgmesDLModel.getVoltageLevelDiagramData()).thenReturn(voltageLevels);
    }

    private void addOtherDiagram() {
        terminalsPropertyBags.addAll(Arrays.asList(createTerminalPropertyBag(NAMESPACE + "Generator", "1", 4, 20, 1, OTHER_DIAGRAM_NAME),
                                                   createTerminalPropertyBag(NAMESPACE + "Generator", "1", 12, 20, 2, OTHER_DIAGRAM_NAME),
                                                   createTerminalPropertyBag(NAMESPACE + "Load", "1", 4, 20, 1, OTHER_DIAGRAM_NAME),
                                                   createTerminalPropertyBag(NAMESPACE + "Load", "1", 12, 20, 2, OTHER_DIAGRAM_NAME),
                                                   createTerminalPropertyBag(NAMESPACE + "Shunt", "1", 4, 20, 1, OTHER_DIAGRAM_NAME),
                                                   createTerminalPropertyBag(NAMESPACE + "Shunt", "1", 12, 20, 2, OTHER_DIAGRAM_NAME),
                                                   createTerminalPropertyBag(NAMESPACE + "Switch", "1", 4, 20, 1, OTHER_DIAGRAM_NAME),
                                                   createTerminalPropertyBag(NAMESPACE + "Switch", "1", 12, 20, 2, OTHER_DIAGRAM_NAME),
                                                   createTerminalPropertyBag(NAMESPACE + "Switch", "2", 28, 20, 1, OTHER_DIAGRAM_NAME),
                                                   createTerminalPropertyBag(NAMESPACE + "Switch", "2", 36, 20, 2, OTHER_DIAGRAM_NAME),
                                                   createTerminalPropertyBag(NAMESPACE + "Transformer", "1", 4, 20, 1, OTHER_DIAGRAM_NAME),
                                                   createTerminalPropertyBag(NAMESPACE + "Transformer", "1", 12, 20, 2, OTHER_DIAGRAM_NAME),
                                                   createTerminalPropertyBag(NAMESPACE + "Transformer", "2", 28, 20, 1, OTHER_DIAGRAM_NAME),
                                                   createTerminalPropertyBag(NAMESPACE + "Transformer", "2", 36, 20, 2, OTHER_DIAGRAM_NAME),
                                                   createTerminalPropertyBag(NAMESPACE + "Transformer3w", "1", 4, 20, 1, OTHER_DIAGRAM_NAME),
                                                   createTerminalPropertyBag(NAMESPACE + "Transformer3w", "1", 12, 20, 2, OTHER_DIAGRAM_NAME),
                                                   createTerminalPropertyBag(NAMESPACE + "Transformer3w", "2", 28, 20, 1, OTHER_DIAGRAM_NAME),
                                                   createTerminalPropertyBag(NAMESPACE + "Transformer3w", "2", 36, 20, 2, OTHER_DIAGRAM_NAME),
                                                   createTerminalPropertyBag(NAMESPACE + "Transformer3w", "3", 20, 32, 1, OTHER_DIAGRAM_NAME),
                                                   createTerminalPropertyBag(NAMESPACE + "Transformer3w", "3", 20, 40, 2, OTHER_DIAGRAM_NAME),
                                                   createTerminalPropertyBag(NAMESPACE + "Svc", "1", 4, 20, 1, OTHER_DIAGRAM_NAME),
                                                   createTerminalPropertyBag(NAMESPACE + "Svc", "1", 12, 20, 2, OTHER_DIAGRAM_NAME)));
        busesPropertyBags.addAll(Arrays.asList(createBusPropertyBag(NAMESPACE + "Bus", "Bus", NAMESPACE + "VoltageLevel", "VoltageLevel", 40, 10, 1, OTHER_DIAGRAM_NAME),
                                               createBusPropertyBag(NAMESPACE + "Bus", "Bus", NAMESPACE + "VoltageLevel", "VoltageLevel", 40, 80, 2, OTHER_DIAGRAM_NAME)));
        busbarsPropertyBags.addAll(Arrays.asList(createBusbarPropertyBag(NAMESPACE + "Busbar", "Busbar", 40, 10, 1, OTHER_DIAGRAM_NAME),
                                                 createBusbarPropertyBag(NAMESPACE + "Busbar", "Busbar", 40, 80, 2, OTHER_DIAGRAM_NAME)));
        linesPropertyBags.addAll(Arrays.asList(createPropertyBag(NAMESPACE + "Line", "Line", 40, 10, 1, OTHER_DIAGRAM_NAME),
                                               createPropertyBag(NAMESPACE + "Line", "Line", 40, 80, 2, OTHER_DIAGRAM_NAME)));
        danglingLinesPropertyBags.addAll(Arrays.asList(createPropertyBag(NAMESPACE + "DanglingLine", "DanglingLine", 40, 10, 1, OTHER_DIAGRAM_NAME),
                                                       createPropertyBag(NAMESPACE + "DanglingLine", "DanglingLine", 40, 80, 2, OTHER_DIAGRAM_NAME)));
        generatorsPropertyBags.addAll(Arrays.asList(createPropertyBag(NAMESPACE + "Generator", "Generator", 20, 20, 0, 90, OTHER_DIAGRAM_NAME)));
        loadsPropertyBags.addAll(Arrays.asList(createPropertyBag(NAMESPACE + "Load", "Load", 20, 20, 0, 90, OTHER_DIAGRAM_NAME)));
        shuntsPropertyBags.addAll(Arrays.asList(createPropertyBag(NAMESPACE + "Shunt", "Shunt", 20, 20, 0, 90, OTHER_DIAGRAM_NAME)));
        switchesPropertyBags.addAll(Arrays.asList(createSwitchPropertyBag(NAMESPACE + "Switch", "Switch", 20, 20, 90, OTHER_DIAGRAM_NAME)));
        tranformersPropertyBags.addAll(Arrays.asList(createPropertyBag(NAMESPACE + "Transformer", "Transformer", 20, 20, 0, 90, OTHER_DIAGRAM_NAME)));
        tranformers3wPropertyBags.addAll(Arrays.asList(createPropertyBag(NAMESPACE + "Transformer3w", "Transformer3w", 20, 26, 0, 90, OTHER_DIAGRAM_NAME)));
        hvdcLinesPropertyBags.addAll(Arrays.asList(createPropertyBag(NAMESPACE + "HvdcLine", "HvdcLine", 40, 10, 1, OTHER_DIAGRAM_NAME),
                                                   createPropertyBag(NAMESPACE + "HvdcLine", "HvdcLine", 40, 80, 2, OTHER_DIAGRAM_NAME)));
        svcsPropertyBags.addAll(Arrays.asList(createPropertyBag(NAMESPACE + "Svc", "Svc", 20, 20, 0, 90, OTHER_DIAGRAM_NAME)));
    }

    private <T> void checkDiagramData(NodeDiagramData.NodeDiagramDataDetails diagramDetails, double x1, double y1,
                                      double x2, double y2) {
        assertNotNull(diagramDetails);
        assertEquals(1, diagramDetails.getPoint1().getSeq(), 0);
        assertEquals(x1, diagramDetails.getPoint1().getX(), 0);
        assertEquals(y1, diagramDetails.getPoint1().getY(), 0);
        assertEquals(2, diagramDetails.getPoint2().getSeq(), 0);
        assertEquals(x2, diagramDetails.getPoint2().getX(), 0);
        assertEquals(y2, diagramDetails.getPoint2().getY(), 0);
    }

    @Test
    public void testBuses() {
        CgmesDLImporter cgmesDLImporter = new CgmesDLImporter(Networks.createNetworkWithBus(), cgmesDLModel);
        cgmesDLImporter.importDLData();
        Network network = cgmesDLImporter.getNetworkWithDLData();
        Bus bus = network.getVoltageLevel("VoltageLevel").getBusBreakerView().getBus("Bus");
        NodeDiagramData<Bus> busDiagramData = bus.getExtension(NodeDiagramData.class);

        checkDiagramData(busDiagramData.getData(DEFAULT_DIAGRAM_NAME), 20, 5, 20, 40);
        checkDiagramData(busDiagramData.getData(OTHER_DIAGRAM_NAME), 40, 10, 40, 80);
    }

    @Test
    public void testBusbars() {
        CgmesDLImporter cgmesDLImporter = new CgmesDLImporter(Networks.createNetworkWithBusbar(), cgmesDLModel);
        cgmesDLImporter.importDLData();
        Network network = cgmesDLImporter.getNetworkWithDLData();
        BusbarSection busbar = network.getBusbarSection("Busbar");
        NodeDiagramData<BusbarSection> busbarDiagramData = busbar.getExtension(NodeDiagramData.class);

        checkDiagramData(busbarDiagramData.getData(DEFAULT_DIAGRAM_NAME), 20, 5, 20, 40);
        checkDiagramData(busbarDiagramData.getData(OTHER_DIAGRAM_NAME), 40, 10, 40, 80);
    }

    private <T> void checkDiagramData(LineDiagramData<?> diagramData, String diagram, double x1, double y1, double x2, double y2) {
        assertNotNull(diagramData);
        assertEquals(1, diagramData.getPoints(diagram).get(0).getSeq(), 0);
        assertEquals(x1, diagramData.getPoints(diagram).get(0).getX(), 0);
        assertEquals(y1, diagramData.getPoints(diagram).get(0).getY(), 0);
        assertEquals(2, diagramData.getPoints(diagram).get(1).getSeq(), 0);
        assertEquals(x2, diagramData.getPoints(diagram).get(1).getX(), 0);
        assertEquals(y2, diagramData.getPoints(diagram).get(1).getY(), 0);
        assertEquals(1, diagramData.getFirstPoint(diagram).getSeq(), 0);
        assertEquals(x1, diagramData.getFirstPoint(diagram).getX(), 0);
        assertEquals(y1, diagramData.getFirstPoint(diagram).getY(), 0);
        assertEquals(2, diagramData.getLastPoint(diagram).getSeq(), 0);
        assertEquals(x2, diagramData.getLastPoint(diagram).getX(), 0);
        assertEquals(y2, diagramData.getLastPoint(diagram).getY(), 0);
    }

    @Test
    public void testLines() {
        CgmesDLImporter cgmesDLImporter = new CgmesDLImporter(Networks.createNetworkWithLine(), cgmesDLModel);
        cgmesDLImporter.importDLData();
        Network network = cgmesDLImporter.getNetworkWithDLData();
        Line line = network.getLine("Line");
        LineDiagramData<Line> lineDiagramData = line.getExtension(LineDiagramData.class);

        checkDiagramData(lineDiagramData, DEFAULT_DIAGRAM_NAME, 20, 5, 20, 40);
        checkDiagramData(lineDiagramData, OTHER_DIAGRAM_NAME, 40, 10, 40, 80);
    }

    @Test
    public void testDanglingLines() {
        Mockito.when(cgmesDLModel.getLinesDiagramData()).thenReturn(danglingLinesPropertyBags);
        CgmesDLImporter cgmesDLImporter = new CgmesDLImporter(Networks.createNetworkWithDanglingLine(), cgmesDLModel);
        cgmesDLImporter.importDLData();
        Network network = cgmesDLImporter.getNetworkWithDLData();
        DanglingLine danglingLine = network.getDanglingLine("DanglingLine");
        LineDiagramData<DanglingLine> danglingLineDiagramData = danglingLine.getExtension(LineDiagramData.class);

        checkDiagramData(danglingLineDiagramData, DEFAULT_DIAGRAM_NAME, 20, 5, 20, 40);
        checkDiagramData(danglingLineDiagramData, OTHER_DIAGRAM_NAME, 40, 10, 40, 80);
    }

    @Test
    public void testHvdcLines() {
        CgmesDLImporter cgmesDLImporter = new CgmesDLImporter(Networks.createNetworkWithHvdcLine(), cgmesDLModel);
        cgmesDLImporter.importDLData();
        Network network = cgmesDLImporter.getNetworkWithDLData();
        HvdcLine hdcvLine = network.getHvdcLine("HvdcLine");
        LineDiagramData<HvdcLine> hvdcLineDiagramData = hdcvLine.getExtension(LineDiagramData.class);

        checkDiagramData(hvdcLineDiagramData, DEFAULT_DIAGRAM_NAME, 20, 5, 20, 40);
        checkDiagramData(hvdcLineDiagramData, OTHER_DIAGRAM_NAME, 40, 10, 40, 80);
    }

    private <T> void checkDiagramData(InjectionDiagramData.InjectionDiagramDetails diagramDetails, double x, double y,
                                      double tx1, double ty1, double tx2, double ty2) {
        assertNotNull(diagramDetails);
        assertEquals(0, diagramDetails.getPoint().getSeq(), 0);
        assertEquals(x, diagramDetails.getPoint().getX(), 0);
        assertEquals(y, diagramDetails.getPoint().getY(), 0);
        assertEquals(90, diagramDetails.getRotation(), 0);

        List<DiagramPoint> points = diagramDetails.getTerminalPoints();
        assertEquals(1, points.get(0).getSeq(), 0);
        assertEquals(tx1, points.get(0).getX(), 0);
        assertEquals(ty1, points.get(0).getY(), 0);
        assertEquals(2, points.get(1).getSeq(), 0);
        assertEquals(tx2, points.get(1).getX(), 0);
        assertEquals(ty2, points.get(1).getY(), 0);
    }

    @Test
    public void testGenerators() {
        CgmesDLImporter cgmesDLImporter = new CgmesDLImporter(Networks.createNetworkWithGenerator(), cgmesDLModel);
        cgmesDLImporter.importDLData();
        Network network = cgmesDLImporter.getNetworkWithDLData();
        Generator generator = network.getGenerator("Generator");
        InjectionDiagramData<Generator> generatorDiagramData = generator.getExtension(InjectionDiagramData.class);

        checkDiagramData(generatorDiagramData.getData(DEFAULT_DIAGRAM_NAME), 10, 10, 2, 10, 6, 10);
        checkDiagramData(generatorDiagramData.getData(OTHER_DIAGRAM_NAME), 20, 20, 4, 20, 12, 20);
    }

    @Test
    public void testLoads() {
        CgmesDLImporter cgmesDLImporter = new CgmesDLImporter(Networks.createNetworkWithLoad(), cgmesDLModel);
        cgmesDLImporter.importDLData();
        Network network = cgmesDLImporter.getNetworkWithDLData();
        Load load = network.getLoad("Load");
        InjectionDiagramData<Load> loadDiagramData = load.getExtension(InjectionDiagramData.class);

        checkDiagramData(loadDiagramData.getData(DEFAULT_DIAGRAM_NAME), 10, 10, 2, 10, 6, 10);
        checkDiagramData(loadDiagramData.getData(OTHER_DIAGRAM_NAME), 20, 20, 4, 20, 12, 20);
    }

    @Test
    public void testShunts() {
        CgmesDLImporter cgmesDLImporter = new CgmesDLImporter(Networks.createNetworkWithShuntCompensator(), cgmesDLModel);
        cgmesDLImporter.importDLData();
        Network network = cgmesDLImporter.getNetworkWithDLData();
        ShuntCompensator shunt = network.getShuntCompensator("Shunt");
        InjectionDiagramData<ShuntCompensator> shuntDiagramData = shunt.getExtension(InjectionDiagramData.class);

        checkDiagramData(shuntDiagramData.getData(DEFAULT_DIAGRAM_NAME), 10, 10, 2, 10, 6, 10);
        checkDiagramData(shuntDiagramData.getData(OTHER_DIAGRAM_NAME), 20, 20, 4, 20, 12, 20);
    }

    @Test
    public void testSvcs() {
        CgmesDLImporter cgmesDLImporter = new CgmesDLImporter(Networks.createNetworkWithStaticVarCompensator(), cgmesDLModel);
        cgmesDLImporter.importDLData();
        Network network = cgmesDLImporter.getNetworkWithDLData();
        StaticVarCompensator svc = network.getStaticVarCompensator("Svc");
        InjectionDiagramData<StaticVarCompensator> svcDiagramData = svc.getExtension(InjectionDiagramData.class);

        checkDiagramData(svcDiagramData.getData(DEFAULT_DIAGRAM_NAME), 10, 10, 2, 10, 6, 10);
        checkDiagramData(svcDiagramData.getData(OTHER_DIAGRAM_NAME), 20, 20, 4, 20, 12, 20);
    }

    private <T> void checkDiagramData(CouplingDeviceDiagramData.CouplingDeviceDiagramDetails diagramDataDetails, double x, double y,
                                      double tx1, double ty1, double tx2, double ty2, double tx3, double ty3, double tx4, double ty4) {
        assertNotNull(diagramDataDetails);
        assertEquals(0, diagramDataDetails.getPoint().getSeq(), 0);
        assertEquals(x, diagramDataDetails.getPoint().getX(), 0);
        assertEquals(y, diagramDataDetails.getPoint().getY(), 0);
        assertEquals(90, diagramDataDetails.getRotation(), 0);

        List<DiagramPoint> pointsT1 = diagramDataDetails.getTerminalPoints(DiagramTerminal.TERMINAL1);
        List<DiagramPoint> pointsT2 = diagramDataDetails.getTerminalPoints(DiagramTerminal.TERMINAL2);
        assertEquals(1, pointsT1.get(0).getSeq(), 0);
        assertEquals(tx1, pointsT1.get(0).getX(), 0);
        assertEquals(ty1, pointsT1.get(0).getY(), 0);
        assertEquals(2, pointsT1.get(1).getSeq(), 0);
        assertEquals(tx2, pointsT1.get(1).getX(), 0);
        assertEquals(ty2, pointsT1.get(1).getY(), 0);
        assertEquals(1, pointsT2.get(0).getSeq(), 0);
        assertEquals(tx3, pointsT2.get(0).getX(), 0);
        assertEquals(ty3, pointsT2.get(0).getY(), 0);
        assertEquals(2, pointsT2.get(1).getSeq(), 0);
        assertEquals(tx4, pointsT2.get(1).getX(), 0);
        assertEquals(ty4, pointsT2.get(1).getY(), 0);
    }

    @Test
    public void testSwitches() {
        CgmesDLImporter cgmesDLImporter = new CgmesDLImporter(Networks.createNetworkWithSwitch(), cgmesDLModel);
        cgmesDLImporter.importDLData();
        Network network = cgmesDLImporter.getNetworkWithDLData();
        Switch sw = network.getSwitch("Switch");
        CouplingDeviceDiagramData<Switch> shuntDiagramData = sw.getExtension(CouplingDeviceDiagramData.class);

        checkDiagramData(shuntDiagramData.getData(DEFAULT_DIAGRAM_NAME), 10, 10, 2, 10, 6, 10, 14, 10, 18, 10);
        checkDiagramData(shuntDiagramData.getData(OTHER_DIAGRAM_NAME), 20, 20, 4, 20, 12, 20, 28, 20, 36, 20);
    }

    @Test
    public void testTransformers() {
        CgmesDLImporter cgmesDLImporter = new CgmesDLImporter(Networks.createNetworkWithTwoWindingsTransformer(), cgmesDLModel);
        cgmesDLImporter.importDLData();
        Network network = cgmesDLImporter.getNetworkWithDLData();
        TwoWindingsTransformer transformer = network.getTwoWindingsTransformer("Transformer");
        CouplingDeviceDiagramData<TwoWindingsTransformer> transformerDiagramData = transformer.getExtension(CouplingDeviceDiagramData.class);

        checkDiagramData(transformerDiagramData.getData(DEFAULT_DIAGRAM_NAME), 10, 10, 2, 10, 6, 10, 14, 10, 18, 10);
        checkDiagramData(transformerDiagramData.getData(OTHER_DIAGRAM_NAME), 20, 20, 4, 20, 12, 20, 28, 20, 36, 20);
    }

    private <T> void checkDiagramData(ThreeWindingsTransformerDiagramData.ThreeWindingsTransformerDiagramDataDetails diagramDataDetails, double x, double y,
                                      double tx1, double ty1, double tx2, double ty2, double tx3, double ty3, double tx4, double ty4, double tx5,
                                      double ty5, double tx6, double ty6) {
        assertNotNull(diagramDataDetails);
        assertEquals(0, diagramDataDetails.getPoint().getSeq(), 0);
        assertEquals(x, diagramDataDetails.getPoint().getX(), 0);
        assertEquals(y, diagramDataDetails.getPoint().getY(), 0);
        assertEquals(90, diagramDataDetails.getRotation(), 0);
        assertEquals(1, diagramDataDetails.getTerminalPoints(DiagramTerminal.TERMINAL1).get(0).getSeq(), 0);
        assertEquals(tx1, diagramDataDetails.getTerminalPoints(DiagramTerminal.TERMINAL1).get(0).getX(), 0);
        assertEquals(ty1, diagramDataDetails.getTerminalPoints(DiagramTerminal.TERMINAL1).get(0).getY(), 0);
        assertEquals(2, diagramDataDetails.getTerminalPoints(DiagramTerminal.TERMINAL1).get(1).getSeq(), 0);
        assertEquals(tx2, diagramDataDetails.getTerminalPoints(DiagramTerminal.TERMINAL1).get(1).getX(), 0);
        assertEquals(ty2, diagramDataDetails.getTerminalPoints(DiagramTerminal.TERMINAL1).get(1).getY(), 0);
        assertEquals(1, diagramDataDetails.getTerminalPoints(DiagramTerminal.TERMINAL2).get(0).getSeq(), 0);
        assertEquals(tx3, diagramDataDetails.getTerminalPoints(DiagramTerminal.TERMINAL2).get(0).getX(), 0);
        assertEquals(ty3, diagramDataDetails.getTerminalPoints(DiagramTerminal.TERMINAL2).get(0).getY(), 0);
        assertEquals(2, diagramDataDetails.getTerminalPoints(DiagramTerminal.TERMINAL2).get(1).getSeq(), 0);
        assertEquals(tx4, diagramDataDetails.getTerminalPoints(DiagramTerminal.TERMINAL2).get(1).getX(), 0);
        assertEquals(ty4, diagramDataDetails.getTerminalPoints(DiagramTerminal.TERMINAL2).get(1).getY(), 0);
        assertEquals(1, diagramDataDetails.getTerminalPoints(DiagramTerminal.TERMINAL3).get(0).getSeq(), 0);
        assertEquals(tx5, diagramDataDetails.getTerminalPoints(DiagramTerminal.TERMINAL3).get(0).getX(), 0);
        assertEquals(ty5, diagramDataDetails.getTerminalPoints(DiagramTerminal.TERMINAL3).get(0).getY(), 0);
        assertEquals(2, diagramDataDetails.getTerminalPoints(DiagramTerminal.TERMINAL3).get(1).getSeq(), 0);
        assertEquals(tx6, diagramDataDetails.getTerminalPoints(DiagramTerminal.TERMINAL3).get(1).getX(), 0);
        assertEquals(ty6, diagramDataDetails.getTerminalPoints(DiagramTerminal.TERMINAL3).get(1).getY(), 0);
    }

    @Test
    public void testTransformers3w() {
        Mockito.when(cgmesDLModel.getTransformersDiagramData()).thenReturn(tranformers3wPropertyBags);
        CgmesDLImporter cgmesDLImporter = new CgmesDLImporter(Networks.createNetworkWithThreeWindingsTransformer(), cgmesDLModel);
        cgmesDLImporter.importDLData();
        Network network = cgmesDLImporter.getNetworkWithDLData();
        ThreeWindingsTransformer transformer = network.getThreeWindingsTransformer("Transformer3w");
        ThreeWindingsTransformerDiagramData transformerDiagramData = transformer.getExtension(ThreeWindingsTransformerDiagramData.class);

        checkDiagramData(transformerDiagramData.getData(DEFAULT_DIAGRAM_NAME), 10, 13, 2, 10, 6, 10, 14, 10, 18, 10, 10, 16, 10, 20);
        checkDiagramData(transformerDiagramData.getData(OTHER_DIAGRAM_NAME), 20, 26, 4, 20, 12, 20, 28, 20, 36, 20, 20, 32, 20, 40);
    }

    @Test
    public void testRemoveExtensions() {
        Mockito.when(cgmesDLModel.getTransformersDiagramData()).thenReturn(tranformers3wPropertyBags);
        CgmesDLImporter cgmesDLImporter = new CgmesDLImporter(Networks.createNetworkWithLoad(), cgmesDLModel);
        cgmesDLImporter.importDLData();
        Network network = cgmesDLImporter.getNetworkWithDLData();
        Load load = network.getLoad("Load");

        assertNotNull(load.getExtension(InjectionDiagramData.class));
        CgmesDLUtils.removeIidmCgmesExtensions(network);
        CgmesDLUtils.clearCgmesDl(network);
        assertNull(load.getExtension(InjectionDiagramData.class));
    }

    @Test
    public void testVoltageLevels() {
        voltageLevels = new PropertyBags(Arrays.asList(createVoltageLevelPropertyBag(NAMESPACE + "2", "2", "Breaker1", 0, 0, 0)));
        Mockito.when(cgmesDLModel.getVoltageLevelDiagramData()).thenReturn(voltageLevels);
        Network network = Networks.createNetworkWithDoubleBusbarSections();
        Map<String, Set<String>> nodesSwitchesForks = new HashMap<>();
        nodesSwitchesForks.put("2", Arrays.asList("Breaker1", "Disconnector1", "Disconnector2").stream().collect(Collectors.toSet()));
        Mockito.when(cgmesDLModel.findCgmesConnectivityNodesSwitchesForks()).thenReturn(nodesSwitchesForks);
        CgmesDLImporter cgmesDLImporter = new CgmesDLImporter(network, cgmesDLModel);
        cgmesDLImporter.importDLData();
        Network network1 = cgmesDLImporter.getNetworkWithDLData();
        VoltageLevel voltageLevel = network1.getVoltageLevel("VoltageLevel1");
        assertTrue(VoltageLevelDiagramData.checkDiagramData(voltageLevel));
    }

}
