/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.svg;

import java.util.Optional;

import com.powsybl.sld.svg.DiagramLabelProvider.Direction;

/**
 * @author Giovanni Ferrari <giovanni.ferrari at techrain.eu>
 */
public class InitialValue {

    private final Direction direction1;
    private final Direction direction2;
    private final String label1;
    private final String label2;
    private final String label3;
    private final String label4;

    public InitialValue(Direction dir1, Direction dir2, String text1, String text2, String text3, String text4) {
        direction1 = dir1;
        direction2 = dir2;
        label1 = text1;
        label2 = text2;
        label3 = text3;
        label4 = text4;
    }

    public Optional<Direction> getArrowDirection1() {
        return Optional.ofNullable(direction1);
    }

    public Optional<Direction> getArrowDirection2() {
        return Optional.ofNullable(direction2);
    }

    public Optional<String> getLabel1() {
        return Optional.ofNullable(label1);
    }

    public Optional<String> getLabel2() {
        return Optional.ofNullable(label2);
    }

    public Optional<String> getLabel3() {
        return Optional.ofNullable(label3);
    }

    public Optional<String> getLabel4() {
        return Optional.ofNullable(label4);
    }
}
