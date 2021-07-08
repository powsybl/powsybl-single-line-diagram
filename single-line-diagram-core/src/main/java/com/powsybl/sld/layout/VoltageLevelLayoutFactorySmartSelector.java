/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VoltageLevel;

import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.stream.StreamSupport;

/**
 * {@link VoltageLevelLayoutFactory} smart selector.
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface VoltageLevelLayoutFactorySmartSelector {

    /**
     * Find best {@link VoltageLevelLayoutFactory}, i.e the one with higher priority among selectable ones.
     * @param vl the voltage level
     *
     * @return best {@link VoltageLevelLayoutFactory}
     */
    static Optional<VoltageLevelLayoutFactorySmartSelector> findBest(VoltageLevel vl) {
        Objects.requireNonNull(vl);
        return StreamSupport.stream(ServiceLoader.load(VoltageLevelLayoutFactorySmartSelector.class, VoltageLevelLayoutFactorySmartSelector.class.getClassLoader()).spliterator(), false)
                .filter(selector -> selector.isSelectable(vl))
                .max(Comparator.comparingInt(selector -> selector.getPriority(vl)));
    }

    /**
     * Get a selection priority number. A high number means a high priority.
     *
     * @param vl the voltage level
     * @return priority number
     */
    int getPriority(VoltageLevel vl);

    /**
     * Verify that the {@link VoltageLevelLayoutFactory} is selectable for this voltage level.
     *
     * @param vl the voltage level
     * @return true if the {@link VoltageLevelLayoutFactory} is selectable for this voltage level, false otherwise.
     */
    boolean isSelectable(VoltageLevel vl);

    /**
     * Create a {@link VoltageLevelLayoutFactory} instance.
     *
     * @return a {@link VoltageLevelLayoutFactory} instance
     */
    VoltageLevelLayoutFactory createFactory(Network network);
}
