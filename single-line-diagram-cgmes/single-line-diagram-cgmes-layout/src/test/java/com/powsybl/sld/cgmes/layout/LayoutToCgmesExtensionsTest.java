/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.cgmes.layout;

import com.powsybl.iidm.network.*;
import com.powsybl.sld.cgmes.dl.iidm.extensions.*;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Christian Biasuzzi <christian.biasuzzi@techrain.eu>
 */
public class LayoutToCgmesExtensionsTest {

    private List<Network> networks = new ArrayList<>();

    @Before
    public void setUp() {
        createNetworks();
    }

    private void createNetworks() {
        networks.add(Networks.createNetworkWithGenerator());
        networks.add(Networks.createNetworkWithLine());
        networks.add(Networks.createNetworkWithDanglingLine());
        networks.add(Networks.createNetworkWithBusbar());
        networks.add(Networks.createNetworkWithBus());
        networks.add(Networks.createNetworkWithLoad());
        networks.add(Networks.createNetworkWithShuntCompensator());
        networks.add(Networks.createNetworkWithStaticVarCompensator());
        networks.add(Networks.createNetworkWithSwitch());
        networks.add(Networks.createNetworkWithTwoWindingsTransformer());
        networks.add(Networks.createNetworkWithThreeWindingsTransformer());
        networks.add(Networks.createNetworkWithBusbarAndSwitch());
        networks.add(Networks.createNetworkWithPhaseShiftTransformer());
    }

    private void checkExtensionsSet(Network network) {
        network.getVoltageLevelStream().forEach(vl -> {
            vl.visitEquipments(new DefaultTopologyVisitor() {
                @Override
                public void visitDanglingLine(DanglingLine danglingLine) {
                    assertNotNull(danglingLine.getExtension(LineDiagramData.class));
                }

                @Override
                public void visitGenerator(Generator generator) {
                    assertNotNull(generator.getExtension(InjectionDiagramData.class));
                }

                @Override
                public void visitShuntCompensator(ShuntCompensator sc) {
                    assertNotNull(sc.getExtension(InjectionDiagramData.class));
                }

                @Override
                public void visitLoad(Load load) {
                    assertNotNull(load.getExtension(InjectionDiagramData.class));
                }

                @Override
                public void visitStaticVarCompensator(StaticVarCompensator staticVarCompensator) {
                    assertNotNull(staticVarCompensator.getExtension(InjectionDiagramData.class));
                }

                @Override
                public void visitLine(Line line, Branch.Side side) {
                    assertNotNull(line.getExtension(LineDiagramData.class));
                }

                @Override
                public void visitBusbarSection(BusbarSection busBarSection) {
                    assertNotNull(busBarSection.getExtension(NodeDiagramData.class));
                }

                @Override
                public void visitTwoWindingsTransformer(TwoWindingsTransformer transformer, Branch.Side side) {
                    assertNotNull(transformer.getExtension(CouplingDeviceDiagramData.class));
                }

                @Override
                public void visitThreeWindingsTransformer(ThreeWindingsTransformer transformer, ThreeWindingsTransformer.Side side) {
                    assertNotNull(transformer.getExtension(ThreeWindingsTransformerDiagramData.class));
                }

                @Override
                public void visitHvdcConverterStation(HvdcConverterStation<?> converterStation) {
                    assertNotNull(converterStation.getExtension(LineDiagramData.class));
                }
            });
        });
    }

    private void checkExtensionsUnset(Network network) {
        network.getVoltageLevelStream().forEach(vl -> {
            vl.visitEquipments(new DefaultTopologyVisitor() {
                @Override
                public void visitDanglingLine(DanglingLine danglingLine) {
                    assertNull(danglingLine.getExtension(LineDiagramData.class));
                }

                @Override
                public void visitGenerator(Generator generator) {
                    assertNull(generator.getExtension(InjectionDiagramData.class));
                }

                @Override
                public void visitShuntCompensator(ShuntCompensator sc) {
                    assertNull(sc.getExtension(InjectionDiagramData.class));
                }

                @Override
                public void visitLoad(Load load) {
                    assertNull(load.getExtension(InjectionDiagramData.class));
                }

                @Override
                public void visitStaticVarCompensator(StaticVarCompensator staticVarCompensator) {
                    assertNull(staticVarCompensator.getExtension(InjectionDiagramData.class));
                }

                @Override
                public void visitLine(Line line, Branch.Side side) {
                    assertNull(line.getExtension(LineDiagramData.class));
                }

                @Override
                public void visitBusbarSection(BusbarSection busBarSection) {
                    assertNull(busBarSection.getExtension(NodeDiagramData.class));
                }

                @Override
                public void visitTwoWindingsTransformer(TwoWindingsTransformer transformer, Branch.Side side) {
                    assertNull(transformer.getExtension(CouplingDeviceDiagramData.class));
                }

                @Override
                public void visitThreeWindingsTransformer(ThreeWindingsTransformer transformer, ThreeWindingsTransformer.Side side) {
                    assertNull(transformer.getExtension(ThreeWindingsTransformerDiagramData.class));
                }

                @Override
                public void visitHvdcConverterStation(HvdcConverterStation<?> converterStation) {
                    assertNull(converterStation.getExtension(LineDiagramData.class));
                }
            });
        });
    }

    @Test
    public void testCgmesDlExtensionsEmpty() {
        networks.stream().forEach(this::checkExtensionsUnset);
    }

    @Test
    public void testCgmesDlExtensionsSet() {
        networks.stream().forEach(network -> {
            LayoutToCgmesExtensionsConverter lconv = new LayoutToCgmesExtensionsConverter();
            lconv.convertLayout(network, "new-diagram");
            checkExtensionsSet(network);
        });
    }

    @Test
    public void testCgmesDlExtensionsSetNoname() {
        networks.stream().forEach(network -> {
            LayoutToCgmesExtensionsConverter lconv = new LayoutToCgmesExtensionsConverter();
            lconv.convertLayout(network);
            checkExtensionsSet(network);
        });
    }

    @Test
    public void testCgmesDlExtensionsEmptyNetwork() {
        Network network = NetworkFactory.findDefault().createNetwork("testEmpty", "testEmpty");
        LayoutToCgmesExtensionsConverter lconv = new LayoutToCgmesExtensionsConverter();
        lconv.convertLayout(network, null);

        checkExtensionsUnset(network);
    }

    @Test
    public void testCgmesDlExtensionsBridgePatternNetwork() {
        Network network = Networks.createNetworkWithBridge();

        LayoutToCgmesExtensionsConverter lconv = new LayoutToCgmesExtensionsConverter();
        lconv.convertLayout(network);

        checkExtensionsSet(network);

        VoltageLevel vl1 = network.getVoltageLevel("V1");
        assertTrue(VoltageLevelDiagramData.checkDiagramData(vl1));

        VoltageLevel vl2 = network.getVoltageLevel("V2");
        assertFalse(VoltageLevelDiagramData.checkDiagramData(vl2));

        VoltageLevel vl3 = network.getVoltageLevel("V3");
        assertFalse(VoltageLevelDiagramData.checkDiagramData(vl3));

        assertEquals(2, VoltageLevelDiagramData.getInternalNodeDiagramPoints(vl1, vl1.getSubstation().getId()).length);
    }
}
