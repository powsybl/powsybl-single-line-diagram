/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.cgmes.layout;

import com.powsybl.iidm.network.Network;
import com.powsybl.sld.layout.Layout;
import com.powsybl.sld.layout.SubstationLayoutFactory;
import com.powsybl.sld.layout.VoltageLevelLayoutFactory;
import com.powsybl.sld.model.SubstationGraph;

import java.util.Objects;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public class CgmesSubstationLayoutFactory implements SubstationLayoutFactory {

    private final Network network;

    public CgmesSubstationLayoutFactory(Network network) {
        this.network = Objects.requireNonNull(network);
    }

    @Override
    public Layout create(SubstationGraph graph, VoltageLevelLayoutFactory vLayoutFactory) {
        return new CgmesSubstationLayout(graph, network);
    }
}
