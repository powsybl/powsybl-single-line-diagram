/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.iidm;

import com.powsybl.iidm.network.*;
import com.powsybl.sld.AbstractTestCase;
import com.powsybl.sld.GraphBuilder;
import com.powsybl.sld.iidm.extensions.BusbarSectionPositionAdder;
import com.powsybl.sld.iidm.extensions.ConnectablePosition;
import com.powsybl.sld.iidm.extensions.ConnectablePositionAdder;
import com.powsybl.sld.model.VoltageLevelGraph;
import com.powsybl.sld.model.SubstationGraph;
import com.powsybl.sld.model.ZoneGraph;
import com.powsybl.sld.svg.DefaultDiagramLabelProvider;
import com.powsybl.sld.svg.DefaultDiagramStyleProvider;
import com.powsybl.sld.svg.DiagramLabelProvider;
import com.powsybl.sld.svg.DiagramStyleProvider;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public abstract class AbstractTestCaseIidm extends AbstractTestCase {

    protected Network network;
    protected VoltageLevel vl;
    protected Substation substation;
    protected GraphBuilder graphBuilder;

    protected static String normalizeLineSeparator(String str) {
        return str.replace("\r\n", "\n")
                .replace("\r", "\n");
    }

    VoltageLevel getVl() {
        return vl;
    }

    Substation getSubstation() {
        return substation;
    }

    @Override
    public void toSVG(VoltageLevelGraph g, String filename) {
        toSVG(g, filename, getLayoutParameters(), getDefaultDiagramLabelProvider(), getDefaultDiagramStyleProvider());
    }

    @Override
    public void toSVG(SubstationGraph g, String filename) {
        toSVG(g, filename, getLayoutParameters(), getDefaultDiagramLabelProvider(), getDefaultDiagramStyleProvider());
    }

    @Override
    public void toSVG(ZoneGraph g, String filename) {
        toSVG(g, filename, getLayoutParameters(), getDefaultDiagramLabelProvider(), getDefaultDiagramStyleProvider());
    }

    protected DiagramLabelProvider getDefaultDiagramLabelProvider() {
        return new DefaultDiagramLabelProvider(network, componentLibrary, getLayoutParameters());
    }

    protected DiagramStyleProvider getDefaultDiagramStyleProvider() {
        return new DefaultDiagramStyleProvider();
    }

    protected static Substation createSubstation(Network n, String id, String name, Country country) {
        return n.newSubstation()
                .setId(id)
                .setName(name)
                .setCountry(country)
                .add();
    }

    protected static VoltageLevel createVoltageLevel(Substation s, String id, String name,
                                                     TopologyKind topology, double vNom, int nodeCount) {
        VoltageLevel vl = s.newVoltageLevel()
                .setId(id)
                .setName(name)
                .setTopologyKind(topology)
                .setNominalV(vNom)
                .add();
        return vl;
    }

    protected static void createSwitch(VoltageLevel vl, String id, String name, SwitchKind kind, boolean retained, boolean open, boolean fictitious, int node1, int node2) {
        vl.getNodeBreakerView().newSwitch()
                .setId(id)
                .setName(name)
                .setKind(kind)
                .setRetained(retained)
                .setOpen(open)
                .setFictitious(fictitious)
                .setNode1(node1)
                .setNode2(node2)
                .add();
    }

    protected static void createInternalConnection(VoltageLevel vl, int node1, int node2) {
        vl.getNodeBreakerView().newInternalConnection()
                .setNode1(node1)
                .setNode2(node2)
                .add();
    }

    protected static void createBusBarSection(VoltageLevel vl, String id, String name, int node, int busbarIndex, int sectionIndex) {
        BusbarSection bbs = vl.getNodeBreakerView().newBusbarSection()
                .setId(id)
                .setName(name)
                .setNode(node)
                .add();
        bbs.newExtension(BusbarSectionPositionAdder.class)
                .withBusbarIndex(busbarIndex)
                .withSectionIndex(sectionIndex)
                .add();
    }

    protected static void createLoad(VoltageLevel vl, String id, String name, String feederName, int feederOrder,
                                     ConnectablePosition.Direction direction, int node, double p0, double q0) {
        Load load = vl.newLoad()
                .setId(id)
                .setName(name)
                .setNode(node)
                .setP0(p0)
                .setQ0(q0)
                .add();
        load.newExtension(ConnectablePositionAdder.class)
                .newFeeder()
                        .withDirection(direction)
                        .withName(feederName)
                        .withOrder(feederOrder)
                        .add()
                .add();
    }

    protected static void createGenerator(VoltageLevel vl, String id, String name, String feederName, int feederOrder,
                                          ConnectablePosition.Direction direction, int node,
                                          double minP, double maxP, boolean voltageRegulator,
                                          double targetP, double targetQ) {
        Generator gen = vl.newGenerator()
                .setId(id)
                .setName(name)
                .setNode(node)
                .setMinP(minP)
                .setMaxP(maxP)
                .setVoltageRegulatorOn(voltageRegulator)
                .setTargetP(targetP)
                .setTargetQ(targetQ)
                .add();
        gen.newExtension(ConnectablePositionAdder.class)
                .newFeeder()
                        .withName(feederName)
                        .withOrder(feederOrder)
                        .withDirection(direction)
                        .add()
                .add();
    }

    protected static void createShunt(VoltageLevel vl, String id, String name, String feederName, int feederOrder,
                                      ConnectablePosition.Direction direction, int node,
                                      double bPerSection, int maximumSectionCount, int currentSectionCount) {
        ShuntCompensator shunt = vl.newShuntCompensator()
                .setId(id)
                .setName(name)
                .setNode(node)
                .setSectionCount(currentSectionCount)
                .newLinearModel()
                    .setBPerSection(bPerSection)
                    .setMaximumSectionCount(maximumSectionCount)
                .add()
                .add();
        shunt.newExtension(ConnectablePositionAdder.class)
                .newFeeder()
                        .withName(feederName)
                        .withOrder(feederOrder)
                        .withDirection(direction)
                        .add()
                .add();
    }

    protected static void createTwoWindingsTransformer(Substation s, String id, String name,
                                                       double r, double x, double g, double b,
                                                       double ratedU1, double ratedU2,
                                                       int node1, int node2,
                                                       String idVoltageLevel1, String idVoltageLevel2,
                                                       String feederName1, int feederOrder1, ConnectablePosition.Direction direction1,
                                                       String feederName2, int feederOrder2, ConnectablePosition.Direction direction2) {
        TwoWindingsTransformer t = s.newTwoWindingsTransformer()
                .setId(id)
                .setName(name)
                .setR(r)
                .setX(x)
                .setG(g)
                .setB(b)
                .setRatedU1(ratedU1)
                .setRatedU2(ratedU2)
                .setNode1(node1)
                .setVoltageLevel1(idVoltageLevel1)
                .setNode2(node2)
                .setVoltageLevel2(idVoltageLevel2)
                .add();
        t.newExtension(ConnectablePositionAdder.class)
                .newFeeder1()
                        .withName(feederName1)
                        .withOrder(feederOrder1)
                        .withDirection(direction1).add()
                .newFeeder2()
                        .withName(feederName2)
                        .withOrder(feederOrder2)
                        .withDirection(direction2).add()
                .add();
    }

    protected static void createThreeWindingsTransformer(Substation s, String id, String name,
                                                         String vl1, String vl2, String vl3,
                                                         double r1, double r2, double r3,
                                                         double x1, double x2, double x3,
                                                         double g1, double b1,
                                                         double ratedU1, double ratedU2, double ratedU3,
                                                         int node1, int node2, int node3,
                                                         String feederName1, int feederOrder1, ConnectablePosition.Direction direction1,
                                                         String feederName2, int feederOrder2, ConnectablePosition.Direction direction2,
                                                         String feederName3, int feederOrder3, ConnectablePosition.Direction direction3) {
        ThreeWindingsTransformer t = s.newThreeWindingsTransformer()
                .setId(id)
                .setName(name)
                .newLeg1()
                .setR(r1)
                .setX(x1)
                .setG(g1)
                .setB(b1)
                .setRatedU(ratedU1)
                .setVoltageLevel(vl1)
                .setNode(node1)
                .add()
                .newLeg2()
                .setR(r2)
                .setX(x2)
                .setRatedU(ratedU2)
                .setVoltageLevel(vl2)
                .setNode(node2)
                .add()
                .newLeg3()
                .setR(r3)
                .setX(x3)
                .setRatedU(ratedU3)
                .setVoltageLevel(vl3)
                .setNode(node3)
                .add()
                .add();

        t.newExtension(ConnectablePositionAdder.class)
                .newFeeder1()
                        .withName(feederName1)
                        .withOrder(feederOrder1)
                        .withDirection(direction1).add()
                .newFeeder2()
                        .withName(feederName2)
                        .withOrder(feederOrder2)
                        .withDirection(direction2).add()
                .newFeeder3()
                        .withName(feederName3)
                        .withOrder(feederOrder3)
                        .withDirection(direction3).add()
                .add();
    }

    protected static void createLine(Network network,
                                     String id, String name,
                                     double r, double x,
                                     double g1, double b1,
                                     double g2, double b2,
                                     int node1, int node2,
                                     String idVoltageLevel1, String idVoltageLevel2,
                                     String feederName1, int feederOrder1, ConnectablePosition.Direction direction1,
                                     String feederName2, int feederOrder2, ConnectablePosition.Direction direction2) {
        Line line = network.newLine()
                .setId(id)
                .setName(name)
                .setR(r)
                .setX(x)
                .setG1(g1)
                .setB1(b1)
                .setG2(g2)
                .setB2(b2)
                .setNode1(node1)
                .setVoltageLevel1(idVoltageLevel1)
                .setNode2(node2)
                .setVoltageLevel2(idVoltageLevel2)
                .add();
        line.newExtension(ConnectablePositionAdder.class)
                .newFeeder1()
                        .withName(feederName1)
                        .withOrder(feederOrder1)
                        .withDirection(direction1)
                        .add()
                .newFeeder2()
                        .withName(feederName2)
                        .withOrder(feederOrder2)
                        .withDirection(direction2)
                        .add()
                .add();
    }

}
