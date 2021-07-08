/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public enum Orientation {
    UP,
    DOWN,
    LEFT,
    RIGHT,
    MIDDLE,
    UNDEFINED;

    public boolean isVertical() {
        return this == UP || this == DOWN;
    }

    public boolean isHorizontal() {
        return this == LEFT || this == RIGHT;
    }

    public int progressionSign() {
        switch (this) {
            case UP:
            case LEFT:
                return -1;
            case DOWN:
            case RIGHT:
                return 1;
            default:
                return 0;
        }
    }

}
