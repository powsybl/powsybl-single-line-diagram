/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout;

import com.powsybl.sld.model.BusCell;

import java.util.Map;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class HorizontalInfoCalcPoints extends InfoCalcPoints {
    private Map<BusCell.Direction, Integer> nbSnakeLinesTopBottom;
    private Map<String, Integer> nbSnakeLinesBetween;

    public Map<BusCell.Direction, Integer> getNbSnakeLinesTopBottom() {
        return nbSnakeLinesTopBottom;
    }

    public void setNbSnakeLinesTopBottom(Map<BusCell.Direction, Integer> nbSnakeLinesTopBottom) {
        this.nbSnakeLinesTopBottom = nbSnakeLinesTopBottom;
    }

    public Map<String, Integer> getNbSnakeLinesBetween() {
        return nbSnakeLinesBetween;
    }

    public void setNbSnakeLinesBetween(Map<String, Integer> nbSnakeLinesBetween) {
        this.nbSnakeLinesBetween = nbSnakeLinesBetween;
    }
}
