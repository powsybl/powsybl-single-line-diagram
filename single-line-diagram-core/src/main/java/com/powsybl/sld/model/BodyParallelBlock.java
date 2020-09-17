/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model;

import com.powsybl.sld.layout.LayoutParameters;

import java.util.Comparator;
import java.util.List;

import static com.powsybl.sld.model.Block.Type.BODYPARALLEL;
import static com.powsybl.sld.model.Position.Dimension.*;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class BodyParallelBlock extends AbstractParallelBlock {

    public BodyParallelBlock(List<Block> subBlocks, Cell cell, boolean allowMerge) {
        super(BODYPARALLEL, subBlocks, cell, allowMerge);
    }

    @Override
    public void sizing() {
        subBlocks.forEach(Block::sizing);
        if (getPosition().getOrientation().isVertical()) {
            getPosition().getVSeg().mergeEnvelop(getSegments(V));
            subBlocks.sort(Comparator.comparingInt(Block::getOrder));
            getPosition().getHSeg().glue(getSegments(H));
        } else {
            getPosition().getHSeg().mergeEnvelop(getSegments(H));
            getPosition().getVSeg().glue(getSegments(V));
        }
    }

    @Override
    public void coordHorizontalCase(LayoutParameters layoutParam) {
        subBlocks.forEach(sub -> {
            sub.setX(getCoord().getX());
            sub.setXSpan(getCoord().getXSpan());
            sub.setY(getCoord().getY());
            sub.setYSpan(getCoord().getYSpan());
            sub.calculateCoord(layoutParam);
        });
    }
}
