/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model;

import static com.powsybl.sld.library.ComponentTypeName.BUSBREAKER_CONNECTION;

/**
 * @author Jacques Borsenberger <jacques.borsenberger at rte-france.com>
 */
public class BusBreakerConnection extends FictitiousNode {

    public BusBreakerConnection(VoltageLevelGraph graph, String id) {
        super(graph, id, BUSBREAKER_CONNECTION);
    }
}
