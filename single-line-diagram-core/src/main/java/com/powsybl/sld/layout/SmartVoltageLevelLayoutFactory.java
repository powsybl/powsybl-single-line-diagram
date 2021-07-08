/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Network;
import com.powsybl.sld.model.VoltageLevelGraph;

import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class SmartVoltageLevelLayoutFactory implements VoltageLevelLayoutFactory {

    private final Network network;

    public SmartVoltageLevelLayoutFactory(Network network) {
        this.network = Objects.requireNonNull(network);
    }

    @Override
    public VoltageLevelLayout create(VoltageLevelGraph graph) {
        return VoltageLevelLayoutFactorySmartSelector.findBest(network.getVoltageLevel(graph.getVoltageLevelInfos().getId()))
                .orElseThrow(() -> new PowsyblException("Voltage level layout factory not found"))
                .createFactory(network)
                .create(graph);
    }
}
