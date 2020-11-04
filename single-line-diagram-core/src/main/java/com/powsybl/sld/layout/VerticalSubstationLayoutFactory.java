/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout;

import com.powsybl.sld.model.SubstationGraph;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class VerticalSubstationLayoutFactory implements SubstationLayoutFactory {

    @Override
    public Layout create(SubstationGraph graph, VoltageLevelLayoutFactory vLayoutFactory) {
        return new VerticalSubstationLayout(graph, vLayoutFactory);
    }
}
