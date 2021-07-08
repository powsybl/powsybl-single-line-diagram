/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout;

import com.powsybl.sld.model.BusNode;
import com.powsybl.sld.model.VoltageLevelGraph;
import com.powsybl.sld.model.Node;

import java.util.Objects;
import java.util.Random;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class RandomVoltageLevelLayout extends AbstractVoltageLevelLayout {

    private final double width;

    private final double height;

    private final Random random;

    public RandomVoltageLevelLayout(VoltageLevelGraph graph, double width, double height, Random random) {
        super(graph);
        this.width = width;
        this.height = height;
        this.random = Objects.requireNonNull(random);
    }

    @Override
    public void run(LayoutParameters layoutParam) {
        for (Node node : getGraph().getNodes()) {
            node.setX(random.nextDouble() * width);
            node.setY(random.nextDouble() * height);
            if (node instanceof BusNode) {
                ((BusNode) node).setPxWidth(50);
            }
        }
    }
}
