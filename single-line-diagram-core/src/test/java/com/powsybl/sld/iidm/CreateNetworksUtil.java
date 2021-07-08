/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.iidm;

import com.powsybl.iidm.network.*;
import com.powsybl.sld.iidm.extensions.BranchStatus;
import com.powsybl.sld.iidm.extensions.BranchStatusAdder;
import com.powsybl.sld.iidm.extensions.ConnectablePosition;

import static com.powsybl.sld.iidm.AbstractTestCaseIidm.*;

/**
 * @author Slimane Amar <slimane.amar at rte-france.com>
 */
final class CreateNetworksUtil {

    private CreateNetworksUtil() {
    }

    static Network createNodeBreakerNetworkWithBranchStatus(String id, String sourceFormat) {
        Network network = createNodeBreakerNetworkWithInternalBranches(id, sourceFormat);

        createStatusExtensions(network);

        return network;
    }

    static Network createBusBreakerNetworkWithBranchStatus(String id, String sourceFormat) {
        Network network = createBusBreakerNetworkWithInternalBranches(id, sourceFormat);

        createStatusExtensions(network);

        return network;
    }

    private static void createStatusExtensions(Network network) {
        network.getLine("L11").newExtension(BranchStatusAdder.class).withStatus(BranchStatus.Status.PLANNED_OUTAGE).add();
        network.getLine("L12").newExtension(BranchStatusAdder.class).withStatus(BranchStatus.Status.FORCED_OUTAGE).add();

        network.getTwoWindingsTransformer("T11").newExtension(BranchStatusAdder.class).withStatus(BranchStatus.Status.PLANNED_OUTAGE).add();
        network.getTwoWindingsTransformer("T12").newExtension(BranchStatusAdder.class).withStatus(BranchStatus.Status.FORCED_OUTAGE).add();

        network.getThreeWindingsTransformer("T3_12").newExtension(BranchStatusAdder.class).withStatus(BranchStatus.Status.FORCED_OUTAGE).add();
    }

    static Network createNodeBreakerNetworkWithInternalBranches(String id, String sourceFormat) {
        Network network = Network.create(id, sourceFormat);
        Substation substation = createSubstation(network, "S1", "S1", Country.FR);

        VoltageLevel vl1 = createVoltageLevel(substation, "VL1", "VL1", TopologyKind.NODE_BREAKER, 400, 24);

        createBusBarSection(vl1, "BBS11", "BBS11", 10, 1, 1);
        createBusBarSection(vl1, "BBS12", "BBS12", 20, 2, 1);

        createSwitch(vl1, "D10", "D10", SwitchKind.DISCONNECTOR, false, false, false, 10, 120);
        createSwitch(vl1, "BR1", "BR1", SwitchKind.BREAKER, false, false, false, 120, 121);
        createSwitch(vl1, "D20", "D20", SwitchKind.DISCONNECTOR, false, false, false, 121, 20);

        createLoad(vl1, "L1", "L1", "L1", -1, ConnectablePosition.Direction.TOP, 12, 1, 1);

        createSwitch(vl1, "D11", "D11", SwitchKind.DISCONNECTOR, false, false, false, 10, 11);
        createSwitch(vl1, "BR12", "BR12", SwitchKind.BREAKER, false, false, false, 11, 12);

        createGenerator(vl1, "G", "G", "G", -1, ConnectablePosition.Direction.TOP, 22, 50, 100, false, 100, 400);

        createSwitch(vl1, "D21", "D21", SwitchKind.DISCONNECTOR, false, false, false, 20, 21);
        createSwitch(vl1, "BR22", "BR22", SwitchKind.BREAKER, false, false, false, 21, 22);

        createSwitch(vl1, "D13", "D13", SwitchKind.DISCONNECTOR, false, false, false, 10, 13);
        createSwitch(vl1, "BR14", "BR14", SwitchKind.BREAKER, false, false, false, 13, 14);

        createSwitch(vl1, "D23", "D23", SwitchKind.DISCONNECTOR, false, false, false, 20, 23);
        createSwitch(vl1, "BR24", "BR24", SwitchKind.BREAKER, false, false, false, 23, 24);

        createLine(network, "L11", "L11", 1.0, 1.0, 1.0, 0.0, 0.0, 0.0,
                14, 24, vl1.getId(), vl1.getId(),
                "L11", -1, ConnectablePosition.Direction.TOP,
                "L11", -1, ConnectablePosition.Direction.TOP);

        VoltageLevel vl2 = createVoltageLevel(substation, "VL2", "VL2", TopologyKind.NODE_BREAKER, 400, 9);

        createBusBarSection(vl2, "BBS2", "BBS2", 30, 1, 1);

        createLoad(vl2, "L2", "L2", "L2", -1, ConnectablePosition.Direction.TOP, 32, 1, 1);

        createSwitch(vl2, "D31", "D31", SwitchKind.DISCONNECTOR, false, false, false, 30, 31);
        createSwitch(vl2, "BR32", "BR32", SwitchKind.BREAKER, false, false, false, 31, 32);

        createSwitch(vl1, "D15", "D15", SwitchKind.DISCONNECTOR, false, false, false, 10, 15);
        createSwitch(vl1, "BR16", "BR16", SwitchKind.BREAKER, false, false, false, 15, 16);

        createSwitch(vl2, "D33", "D33", SwitchKind.DISCONNECTOR, false, false, false, 30, 33);
        createSwitch(vl2, "BR34", "BR34", SwitchKind.BREAKER, false, false, false, 33, 34);

        createLine(network, "L12", "L12", 1.0, 1.0, 1.0, 0.0, 0.0, 0.0,
                16, 34, vl1.getId(), vl2.getId(),
                "L12", -1, ConnectablePosition.Direction.TOP,
                "L12", -1, ConnectablePosition.Direction.TOP);

        createSwitch(vl1, "D17", "D17", SwitchKind.DISCONNECTOR, false, false, false, 10, 17);
        createSwitch(vl1, "BR18", "BR18", SwitchKind.BREAKER, false, false, false, 17, 18);

        createSwitch(vl1, "D25", "D25", SwitchKind.DISCONNECTOR, false, false, false, 20, 25);
        createSwitch(vl1, "BR26", "BR26", SwitchKind.BREAKER, false, false, false, 25, 26);

        createTwoWindingsTransformer(substation, "T11", "T11", 250, 100, 52, 12, 65, 90,
                18, 26, vl1.getId(), vl1.getId(),
                "T11", -1, ConnectablePosition.Direction.TOP,
                "T11", -1, ConnectablePosition.Direction.TOP);

        TwoWindingsTransformer twoWindingsTransformer = network.getTwoWindingsTransformer("T11");
        twoWindingsTransformer.getTerminal(TwoWindingsTransformer.Side.ONE).setP(375);
        twoWindingsTransformer.getTerminal(TwoWindingsTransformer.Side.TWO).setP(375);
        twoWindingsTransformer.getTerminal(TwoWindingsTransformer.Side.ONE).setQ(48);
        twoWindingsTransformer.getTerminal(TwoWindingsTransformer.Side.TWO).setQ(48);

        createSwitch(vl1, "D27", "D27", SwitchKind.DISCONNECTOR, false, false, false, 20, 27);
        createSwitch(vl1, "BR28", "BR28", SwitchKind.BREAKER, false, false, false, 27, 28);

        createSwitch(vl2, "D35", "D35", SwitchKind.DISCONNECTOR, false, false, false, 30, 35);
        createSwitch(vl2, "BR36", "BR36", SwitchKind.BREAKER, false, false, false, 35, 36);

        createTwoWindingsTransformer(substation, "T12", "T12", 250, 100, 52, 12, 65, 90,
                28, 36, vl1.getId(), vl2.getId(),
                "T12", -1, ConnectablePosition.Direction.TOP,
                "T12", -1, ConnectablePosition.Direction.TOP);

        twoWindingsTransformer = network.getTwoWindingsTransformer("T12");
        twoWindingsTransformer.getTerminal(TwoWindingsTransformer.Side.ONE).setP(375);
        twoWindingsTransformer.getTerminal(TwoWindingsTransformer.Side.TWO).setP(375);
        twoWindingsTransformer.getTerminal(TwoWindingsTransformer.Side.ONE).setQ(48);
        twoWindingsTransformer.getTerminal(TwoWindingsTransformer.Side.TWO).setQ(48);

        createSwitch(vl1, "D19", "D19", SwitchKind.DISCONNECTOR, false, false, false, 10, 19);
        createSwitch(vl1, "BR20", "BR20", SwitchKind.BREAKER, false, false, false, 19, 190);

        createSwitch(vl1, "D29", "D29", SwitchKind.DISCONNECTOR, false, false, false, 20, 29);
        createSwitch(vl1, "BR30", "BR30", SwitchKind.BREAKER, false, false, false, 29, 290);

        createSwitch(vl2, "D37", "D37", SwitchKind.DISCONNECTOR, false, false, false, 30, 37);
        createSwitch(vl2, "BR38", "BR38", SwitchKind.BREAKER, false, false, false, 37, 38);

        createThreeWindingsTransformer(substation, "T3_12", "T3_12", vl1.getId(), vl1.getId(), vl2.getId(),
                45, 47, 49, 35, 32, 39., 25, 15,
                5, 7, 9,
                190, 290, 38,
                "T3_12", -1, ConnectablePosition.Direction.TOP,
                "T3_12", -1, ConnectablePosition.Direction.TOP,
                "T3_12", -1, ConnectablePosition.Direction.TOP);

        return network;
    }

    static Network createBusBreakerNetworkWithInternalBranches(String id, String sourceFormat) {
        Network network = Network.create(id, sourceFormat);
        Substation substation = createSubstation(network, "S1", "S1", Country.FR);

        VoltageLevel vl1 = createVoltageLevel(substation, "VL1", "VL1", TopologyKind.BUS_BREAKER, 400, -1);

        vl1.getBusBreakerView().newBus()
                .setId("B11")
                .add();
        vl1.newLoad()
                .setId("LD1")
                .setConnectableBus("B11")
                .setBus("B11")
                .setP0(1.0)
                .setQ0(1.0)
                .add();

        network.getVoltageLevel("VL1").getBusBreakerView().newBus()
                .setId("B12")
                .add();
        vl1.getBusBreakerView().newSwitch()
                .setId("BR1")
                .setBus1("B12")
                .setBus2("B11")
                .setOpen(false)
                .add();

        vl1.newGenerator()
                .setId("G")
                .setBus("B12")
                .setConnectableBus("B12")
                .setMaxP(100.0)
                .setMinP(50.0)
                .setTargetP(100.0)
                .setTargetV(400.0)
                .setVoltageRegulatorOn(true)
                .add();

        network.newLine()
                .setId("L11")
                .setVoltageLevel1("VL1")
                .setConnectableBus1("B11")
                .setBus1("B11")
                .setVoltageLevel2("VL1")
                .setConnectableBus2("B12")
                .setBus2("B12")
                .setR(1.0)
                .setX(1.0)
                .setG1(0.0)
                .setB1(0.0)
                .setG2(0.0)
                .setB2(0.0)
                .add();

        substation.newTwoWindingsTransformer()
                .setId("T11")
                .setVoltageLevel1("VL1")
                .setBus1("B11")
                .setConnectableBus1("B11")
                .setVoltageLevel2("VL1")
                .setBus2("B12")
                .setConnectableBus2("B12")
                .setR(250)
                .setX(100)
                .setG(52)
                .setB(12)
                .setRatedU1(65)
                .setRatedU2(90)
                .add();

        VoltageLevel vl2 = substation.newVoltageLevel()
                .setId("VL2")
                .setNominalV(400.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        vl2.getBusBreakerView().newBus()
                .setId("B21")
                .add();
        vl2.newLoad()
                .setId("LD2")
                .setConnectableBus("B21")
                .setBus("B21")
                .setP0(1.0)
                .setQ0(1.0)
                .add();

        network.newLine()
                .setId("L12")
                .setVoltageLevel1("VL1")
                .setConnectableBus1("B11")
                .setBus1("B11")
                .setVoltageLevel2("VL2")
                .setConnectableBus2("B21")
                .setBus2("B21")
                .setR(1.0)
                .setX(1.0)
                .setG1(0.0)
                .setB1(0.0)
                .setG2(0.0)
                .setB2(0.0)
                .add();

        substation.newTwoWindingsTransformer()
                .setId("T12")
                .setVoltageLevel1("VL1")
                .setBus1("B12")
                .setConnectableBus1("B12")
                .setVoltageLevel2("VL2")
                .setBus2("B21")
                .setConnectableBus2("B21")
                .setR(250)
                .setX(100)
                .setG(52)
                .setB(12)
                .setRatedU1(65)
                .setRatedU2(90)
                .add();

        ThreeWindingsTransformer threeWindingsTransformer = substation.newThreeWindingsTransformer()
                .setId("T3_12")
                .setRatedU0(234)
                .newLeg1()
                .setVoltageLevel("VL1")
                .setBus("B11")
                .setConnectableBus("B11")
                .setR(45)
                .setX(35)
                .setG(25)
                .setB(15)
                .setRatedU(5)
                .add()
                .newLeg2()
                .setVoltageLevel("VL1")
                .setBus("B12")
                .setConnectableBus("B12")
                .setR(47)
                .setX(37)
                .setG(27)
                .setB(17)
                .setRatedU(7)
                .add()
                .newLeg3()
                .setVoltageLevel("VL2")
                .setBus("B21")
                .setConnectableBus("B21")
                .setR(49)
                .setX(39)
                .setG(29)
                .setB(19)
                .setRatedU(9)
                .add()
                .add();

        return network;
    }
}
