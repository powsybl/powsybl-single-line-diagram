/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.iidm.extensions;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.Connectable;

/**
 * @author Nicolas Noir <nicolas.noir at rte-france.com>
 */
public interface BranchStatus<C extends Connectable<C>> extends Extension<C> {

    static final String NAME = "branchStatus";

    public enum Status {
        IN_OPERATION,
        PLANNED_OUTAGE,
        FORCED_OUTAGE
    }

    @Override
    default String getName() {
        return NAME;
    }

    Status getStatus();

    BranchStatus setStatus(Status status);
}
