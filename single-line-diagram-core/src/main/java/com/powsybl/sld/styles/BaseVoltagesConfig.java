/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.styles;

import java.util.List;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public class BaseVoltagesConfig {

    private List<BaseVoltageConfig> baseVoltages;
    private String defaultProfile;

    public List<BaseVoltageConfig> getBaseVoltages() {
        return baseVoltages;
    }

    public void setBaseVoltages(List<BaseVoltageConfig> baseVoltages) {
        this.baseVoltages = baseVoltages;
    }

    public String getDefaultProfile() {
        return defaultProfile;
    }

    public void setDefaultProfile(String defaultProfile) {
        this.defaultProfile = defaultProfile;
    }

}
