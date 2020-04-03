/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.sld.model;

import com.powsybl.sld.layout.LayoutParameters;

import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * A block group that cannot be correctly decomposed anymore.
 * All subBlocks are superposed.
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class UndefinedBlock extends AbstractComposedBlock {

    public UndefinedBlock(List<Block> subBlocks) {
        super(Type.UNDEFINED, subBlocks);
        this.subBlocks = Objects.requireNonNull(subBlocks);
    }

    public UndefinedBlock(List<Block> subBlocks, Cell cell) {
        this(subBlocks);
        setCell(cell);
    }

    @Override
    public void sizing() {
        for (Block block : subBlocks) {
            block.sizing();
        }
        if (getPosition().getOrientation() == Orientation.VERTICAL) {
            // TODO
        } else {
            throw new UnsupportedOperationException("Horizontal layout of undefined  block not supported");
        }
    }

    @Override
    public void coordVerticalCase(LayoutParameters layoutParam) {
        for (Block block : subBlocks) {
            block.setX(getCoord().getX());
            block.setY(getCoord().getY());
            block.setXSpan(getCoord().getXSpan());
            block.setYSpan(getCoord().getYSpan());
            block.coordVerticalCase(layoutParam);
        }
    }

    @Override
    public void coordHorizontalCase(LayoutParameters layoutParam) {
        throw new UnsupportedOperationException("Horizontal layout of undefined  block not supported");
    }

    @Override
    public double calculateHeight(Set<Node> encounteredNodes, LayoutParameters layoutParameters) {
        double blockHeight = 0.;
        for (int i = 0; i < subBlocks.size(); i++) {
            Block sub = subBlocks.get(i);
            // Here, the subBlocks are superposed, so we calculate the max height of all these subBlocks
            blockHeight = Math.max(blockHeight, sub.calculateHeight(encounteredNodes, layoutParameters));
        }
        return blockHeight;
    }

    @Override
    public String toString() {
        return "UndefinedBlock(subBlocks=" + subBlocks + ")";
    }
}
