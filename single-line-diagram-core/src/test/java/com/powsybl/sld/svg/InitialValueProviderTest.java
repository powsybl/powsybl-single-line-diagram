/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.svg;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.StaticVarCompensator.RegulationMode;
import com.powsybl.sld.GraphBuilder;
import com.powsybl.sld.NetworkGraphBuilder;
import com.powsybl.sld.iidm.extensions.BusbarSectionPositionAdder;
import com.powsybl.sld.layout.LayoutParameters;
import com.powsybl.sld.library.ComponentLibrary;
import com.powsybl.sld.library.ConvergenceComponentLibrary;
import com.powsybl.sld.model.VoltageLevelGraph;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Giovanni Ferrari <giovanni.ferrari at techrain.eu>
 */
public class InitialValueProviderTest {

    private Network network;
    private Substation substation;
    private VoltageLevel vl;
    private GraphBuilder graphBuilder;

    @Before
    public void setUp() {
        network = Network.create("testCase1", "test");
        graphBuilder = new NetworkGraphBuilder(network);
        substation = network.newSubstation().setId("s").setCountry(Country.FR).add();
        vl = substation.newVoltageLevel().setId("vl").setTopologyKind(TopologyKind.NODE_BREAKER).setNominalV(380).add();
        VoltageLevel.NodeBreakerView view = vl.getNodeBreakerView();
        BusbarSection bbs = view.newBusbarSection().setId("bbs").setNode(0).add();
        bbs.newExtension(BusbarSectionPositionAdder.class).withBusbarIndex(1).withSectionIndex(1);
        BusbarSection bbs2 = view.newBusbarSection().setId("bbs2").setNode(3).add();
        bbs2.newExtension(BusbarSectionPositionAdder.class).withBusbarIndex(2).withSectionIndex(2);
        vl.newStaticVarCompensator()
            .setId("svc")
            .setName("svc")
            .setNode(2)
            .setBmin(0.0002)
            .setBmax(0.0008)
            .setRegulationMode(RegulationMode.VOLTAGE)
            .setVoltageSetPoint(390)
            .add();
        vl.newVscConverterStation()
            .setId("vsc")
            .setName("Converter1")
            .setNode(1)
            .setLossFactor(0.011f)
            .setVoltageSetpoint(405.0)
            .setVoltageRegulatorOn(true)
            .add();
        vl.newShuntCompensator()
            .setId("C1")
            .setName("Filter 1")
            .setNode(4)
            .setSectionCount(1)
            .newLinearModel()
                .setBPerSection(1e-5)
                .setMaximumSectionCount(1)
            .add()
            .add();
        vl.newDanglingLine()
                .setId("dl1")
                .setName("Dangling line 1")
                .setNode(5)
                .setP0(1)
                .setQ0(1)
                .setR(0)
                .setX(0)
                .setB(0)
                .setG(0)
                .add();
        view.newDisconnector().setId("d").setNode1(0).setNode2(1).add();
        view.newBreaker().setId("b").setNode1(1).setNode2(2).add();
        view.newBreaker().setId("b2").setNode1(3).setNode2(4).add();
        view.newBreaker().setId("b3").setNode1(3).setNode2(5).add();
    }

    @Test
    public void test() {
        Network network2 = Network.create("testCase2", "test2");
        ComponentLibrary componentLibrary = new ConvergenceComponentLibrary();
        LayoutParameters layoutParameters = new LayoutParameters();
        DefaultDiagramLabelProvider initProvider = new DefaultDiagramLabelProvider(network2, componentLibrary, layoutParameters);
        VoltageLevelGraph g = graphBuilder.buildVoltageLevelGraph(vl.getId(), false, false);
        InitialValue init = initProvider.getInitialValue(g.getNode("svc"));
        assertFalse(init.getLabel1().isPresent());
        assertFalse(init.getLabel2().isPresent());
        assertFalse(init.getLabel3().isPresent());
        assertFalse(init.getLabel4().isPresent());
        assertFalse(init.getArrowDirection1().isPresent());
        assertFalse(init.getArrowDirection2().isPresent());
        DefaultDiagramLabelProvider initProvider1 = new DefaultDiagramLabelProvider(network, componentLibrary, layoutParameters);
        InitialValue init1 = initProvider1.getInitialValue(g.getNode("svc"));
        assertTrue(init1.getLabel1().isPresent());
        assertTrue(init1.getLabel2().isPresent());
        assertFalse(init1.getLabel3().isPresent());
        assertFalse(init1.getLabel4().isPresent());
        assertTrue(init1.getArrowDirection1().isPresent());
        assertTrue(init1.getArrowDirection2().isPresent());
        InitialValue init2 = initProvider1.getInitialValue(g.getNode("vsc"));
        assertTrue(init2.getLabel1().isPresent());
        assertTrue(init2.getLabel2().isPresent());
        assertFalse(init2.getLabel3().isPresent());
        assertFalse(init2.getLabel4().isPresent());
        assertTrue(init2.getArrowDirection1().isPresent());
        assertTrue(init2.getArrowDirection2().isPresent());
        InitialValue init3 = initProvider1.getInitialValue(g.getNode("C1"));
        assertTrue(init3.getLabel1().isPresent());
        assertTrue(init3.getLabel2().isPresent());
        assertFalse(init3.getLabel3().isPresent());
        assertFalse(init3.getLabel4().isPresent());
        assertTrue(init3.getArrowDirection1().isPresent());
        assertTrue(init3.getArrowDirection2().isPresent());
        InitialValue init4 = initProvider1.getInitialValue(g.getNode("b"));
        assertFalse(init4.getLabel1().isPresent());
        assertFalse(init4.getLabel2().isPresent());
        assertFalse(init4.getLabel3().isPresent());
        assertFalse(init4.getLabel4().isPresent());
        assertFalse(init4.getArrowDirection1().isPresent());
        assertFalse(init4.getArrowDirection2().isPresent());
        InitialValue init5 = initProvider1.getInitialValue(g.getNode("dl1"));
        assertTrue(init5.getLabel1().isPresent());
        assertTrue(init5.getLabel2().isPresent());
        assertFalse(init5.getLabel3().isPresent());
        assertFalse(init5.getLabel4().isPresent());
        assertTrue(init5.getArrowDirection1().isPresent());
        assertTrue(init5.getArrowDirection2().isPresent());
    }
}
