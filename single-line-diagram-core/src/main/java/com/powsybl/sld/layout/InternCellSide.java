/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout;

import com.powsybl.sld.model.InternCell;
import com.powsybl.sld.model.Side;

import java.util.*;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 */
public class InternCellSide {

    private InternCell cell;
    private Side side;

    InternCellSide(InternCell cell, Side side) {
        this.cell = cell;
        this.side = side;
    }

    public InternCell getCell() {
        return cell;
    }

    public void setCell(InternCell cell) {
        this.cell = cell;
    }

    public Side getSide() {
        return side;
    }

    public void setSide(Side side) {
        this.side = side;
    }

    public void flipSide() {
        side = side.getFlip();
    }
}
