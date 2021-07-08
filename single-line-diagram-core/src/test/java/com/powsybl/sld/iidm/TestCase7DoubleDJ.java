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
 *  bbs1---d1-b1-b2-d2--- bbs2
 *
 * </pre>
 *
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class TestCase7DoubleDJ extends AbstractTestCaseIidm {

    @Override
    protected LayoutParameters getLayoutParameters() {
        return null;
    }

    @Override
    public void setUp() {
        network = Network.create("testCase", "test");
        substation = createSubstation(network, "s", "s", Country.FR);
        vl = createVoltageLevel(substation, "vl", "vl", TopologyKind.NODE_BREAKER, 380, 10);
        createBusBarSection(vl, "bbs1", "bbs1", 0, 1, 1);
        createSwitch(vl, "d1", "d1", SwitchKind.DISCONNECTOR, false, false, false, 0, 1);
        createSwitch(vl, "b1", "b1", SwitchKind.BREAKER, false, false, false, 1, 2);
        createSwitch(vl, "b2", "b2", SwitchKind.BREAKER, false, false, false, 2, 3);
        createSwitch(vl, "d2", "d2", SwitchKind.DISCONNECTOR, false, false, false, 3, 4);
        createBusBarSection(vl, "bbs2", "bbs2", 4, 1, 2);
    }
}
