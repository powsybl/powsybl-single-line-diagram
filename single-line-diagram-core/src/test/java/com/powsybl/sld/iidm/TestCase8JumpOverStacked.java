/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.iidm;

import com.powsybl.iidm.network.*;
import com.powsybl.sld.layout.LayoutParameters;

/**
 * <pre>
 *
 *              *-----b-----*
 *             /\          /\
 *            |  |        |  |
 *  bbs11 ---d11-|---d1--d12-|--- bbs12
 *               |           |
 *  bbs21 ------d21--d2-----d22-- bbs12
 *
 * </pre>
 *
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class TestCase8JumpOverStacked extends AbstractTestCaseIidm {

    @Override
    protected LayoutParameters getLayoutParameters() {
        return null;
    }

    @Override
    public void setUp() {
        network = Network.create("testCase", "test");
        substation = createSubstation(network, "s", "s", Country.FR);
        vl = createVoltageLevel(substation, "vl", "vl", TopologyKind.NODE_BREAKER, 380, 15);
        createBusBarSection(vl, "bbs11", "bbs11", 0, 1, 1);
        createBusBarSection(vl, "bbs12", "bbs12", 1, 1, 2);
        createSwitch(vl, "d1", "d1", SwitchKind.DISCONNECTOR, false, false, false, 0, 1);
        createBusBarSection(vl, "bbs21", "bbs21", 2, 2, 1);
        createBusBarSection(vl, "bbs22", "bbs22", 3, 2, 2);
        createSwitch(vl, "d2", "d2", SwitchKind.DISCONNECTOR, false, false, false, 2, 3);
        createSwitch(vl, "d11", "d11", SwitchKind.DISCONNECTOR, false, false, false, 0, 4);
        createSwitch(vl, "d12", "d12", SwitchKind.DISCONNECTOR, false, false, false, 1, 5);
        createSwitch(vl, "d21", "d21", SwitchKind.DISCONNECTOR, false, false, false, 2, 4);
        createSwitch(vl, "d22", "d22", SwitchKind.DISCONNECTOR, false, false, false, 3, 5);
        createSwitch(vl, "b", "b", SwitchKind.BREAKER, false, false, false, 4, 5);
    }
}
