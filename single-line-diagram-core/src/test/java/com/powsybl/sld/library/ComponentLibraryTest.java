/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.library;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ComponentLibraryTest {

    @Test
    public void test() {
        List<ComponentLibrary> libraries = ComponentLibrary.findAll();
        assertEquals(1, libraries.size());
        ComponentLibrary cvg = ComponentLibrary.find("Convergence").orElse(null);
        assertNotNull(cvg);
        assertEquals("Convergence", cvg.getName());
    }
}
