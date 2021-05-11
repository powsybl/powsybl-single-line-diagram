/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model;

import static com.powsybl.sld.library.ComponentTypeName.BUS_CONNECTION;

/**
 * @author Jacques Borsenberger <jacques.borsenberger at rte-france.com>
 */
public class BusConnection extends FictitiousNode {

    public BusConnection(VoltageLevelGraph graph, String id) {
        super(graph, id, BUS_CONNECTION);
    }
}
