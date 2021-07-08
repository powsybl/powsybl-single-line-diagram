/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout;

import com.google.auto.service.AutoService;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.sld.layout.positionbyclustering.PositionByClustering;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(VoltageLevelLayoutFactorySmartSelector.class)
public class PositionByClusterVoltageLevelLayoutFactorySmartSelector implements VoltageLevelLayoutFactorySmartSelector {

    private static final int PRIORITY = 0;

    @Override
    public int getPriority(VoltageLevel vl) {
        return PRIORITY;
    }

    @Override
    public boolean isSelectable(VoltageLevel vl) {
        return true;
    }

    @Override
    public VoltageLevelLayoutFactory createFactory(Network network) {
        return new PositionVoltageLevelLayoutFactory(new PositionByClustering());
    }
}
