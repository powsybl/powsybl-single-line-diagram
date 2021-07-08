/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout;

import com.powsybl.sld.model.SubstationGraph;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class ForceSubstationLayoutFactory implements SubstationLayoutFactory {

    public enum CompactionType {
        NONE,
        HORIZONTAL,
        VERTICAL
    }

    private CompactionType compactionType;

    public ForceSubstationLayoutFactory(CompactionType compactionType) {
        this.compactionType = compactionType;
    }

    @Override
    public SubstationLayout create(SubstationGraph substationGraph, VoltageLevelLayoutFactory vLayoutFactory) {
        return new ForceSubstationLayout(substationGraph, vLayoutFactory, compactionType);
    }
}
