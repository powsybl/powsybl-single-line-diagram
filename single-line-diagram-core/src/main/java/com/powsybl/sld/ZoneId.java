/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld;

import java.util.List;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public final class ZoneId {
    private List<String> substationsIds;

    private ZoneId(List<String> substationsIds) {
        this.substationsIds = substationsIds;
    }

    public static ZoneId create(List<String> substationsIds) {
        return new ZoneId(substationsIds);
    }

    public List<String> getSubstationsIds() {
        return substationsIds;
    }

    public boolean isEmpty() {
        return substationsIds.isEmpty();
    }
}
