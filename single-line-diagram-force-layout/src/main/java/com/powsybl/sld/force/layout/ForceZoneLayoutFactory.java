/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.force.layout;

import com.powsybl.sld.layout.CompactionType;
import com.powsybl.sld.layout.SubstationLayoutFactory;
import com.powsybl.sld.layout.VoltageLevelLayoutFactory;
import com.powsybl.sld.layout.ZoneLayout;
import com.powsybl.sld.layout.ZoneLayoutFactory;
import com.powsybl.sld.model.ZoneGraph;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class ForceZoneLayoutFactory implements ZoneLayoutFactory {

    private CompactionType compactionType;

    public ForceZoneLayoutFactory(CompactionType compactionType) {
        this.compactionType = compactionType;
    }

    @Override
    public ZoneLayout create(ZoneGraph graph, SubstationLayoutFactory sLayoutFactory, VoltageLevelLayoutFactory vLayoutFactory) {
        return new ForceZoneLayout(graph, sLayoutFactory, vLayoutFactory, compactionType);
    }
}
