/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout;

import com.powsybl.sld.model.VoltageLevelGraph;

import java.util.Random;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class RandomVoltageLevelLayoutFactory implements VoltageLevelLayoutFactory {

    private final double width;

    private final double height;

    private final Random random = new Random();

    public RandomVoltageLevelLayoutFactory(double width, double height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public VoltageLevelLayout create(VoltageLevelGraph graph) {
        return new RandomVoltageLevelLayout(graph, width, height, random);
    }
}
