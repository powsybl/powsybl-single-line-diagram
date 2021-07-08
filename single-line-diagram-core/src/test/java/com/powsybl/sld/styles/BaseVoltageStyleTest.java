/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.styles;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public class BaseVoltageStyleTest {

    @Test
    public void test() {
        BaseVoltageStyle baseVoltageStyle = BaseVoltageStyle.fromInputStream(getClass().getResourceAsStream("/base-voltages.yml"));
        // getProfiles
        assertEquals(Collections.singletonList("Default"), baseVoltageStyle.getProfiles());
        // getDefaultProfile
        assertEquals("Default", baseVoltageStyle.getDefaultProfile());
        // getBaseVoltageNames
        assertEquals(Arrays.asList("sld-vl300to500", "sld-vl180to300", "sld-vl120to180", "sld-vl70to120", "sld-vl50to70", "sld-vl30to50", "sld-vl0to30"), baseVoltageStyle.getBaseVoltageNames("Default"));
        // getBaseVoltageName
        assertFalse(baseVoltageStyle.getBaseVoltageName(500, "Default").isPresent());
        assertEquals("sld-vl300to500", baseVoltageStyle.getBaseVoltageName(450, "Default").orElseThrow(AssertionError::new));
        assertEquals("sld-vl300to500", baseVoltageStyle.getBaseVoltageName(400, "Default").orElseThrow(AssertionError::new));
        assertEquals("sld-vl300to500", baseVoltageStyle.getBaseVoltageName(300, "Default").orElseThrow(AssertionError::new));
        assertEquals("sld-vl180to300", baseVoltageStyle.getBaseVoltageName(250, "Default").orElseThrow(AssertionError::new));
        assertEquals("sld-vl180to300", baseVoltageStyle.getBaseVoltageName(180, "Default").orElseThrow(AssertionError::new));
        assertFalse(baseVoltageStyle.getBaseVoltageName(700, "Default").isPresent());
    }
}
