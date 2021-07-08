/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.sld.postprocessor;

import com.google.auto.service.AutoService;
import com.powsybl.sld.model.VoltageLevelGraph;

import java.util.Objects;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@AutoService(GraphBuildPostProcessor.class)
public class GraphBuildPostProcessorMock implements GraphBuildPostProcessor {
    private static final String ID = "PostProcessorMock";

    public String getId() {
        return ID;
    }

    public void addNode(VoltageLevelGraph graph, Object network) {
        Objects.requireNonNull(graph);
        Objects.requireNonNull(network);

        // this mock does nothing
    }
}
