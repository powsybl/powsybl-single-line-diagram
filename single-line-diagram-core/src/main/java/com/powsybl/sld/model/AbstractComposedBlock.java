/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model;

import com.fasterxml.jackson.core.JsonGenerator;
import com.powsybl.sld.layout.LayoutParameters;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public abstract class AbstractComposedBlock extends AbstractBlock implements ComposedBlock {

    List<Block> subBlocks;

    AbstractComposedBlock(Type type, List<Block> subBlocks) {
        super(type);
        if (subBlocks.isEmpty()) {
            throw new IllegalArgumentException("Empty block list");
        }
        subBlocks.forEach(b -> b.setParentBlock(this));
    }

    @Override
    public VoltageLevelGraph getGraph() {
        return subBlocks.get(0).getGraph();
    }

    public List<Block> getSubBlocks() {
        return subBlocks;
    }

    @Override
    public boolean isEmbeddingNodeType(Node.NodeType type) {
        return subBlocks.stream().anyMatch(b -> b.isEmbeddingNodeType(type));
    }

    @Override
    public List<Block> findBlockEmbeddingNode(Node node) {
        return subBlocks.stream().flatMap(b -> b.findBlockEmbeddingNode(node).stream()).collect(Collectors.toList());
    }

    @Override
    public Node getExtremityNode(Extremity extremity) {
        if (extremity == Extremity.START) {
            return subBlocks.get(0).getExtremityNode(Extremity.START);
        } else {
            return subBlocks.get(subBlocks.size() - 1).getExtremityNode(Extremity.END);
        }
    }

    @Override
    public void reverseBlock() {
        Collections.reverse(subBlocks);
        subBlocks.forEach(Block::reverseBlock);
    }

    @Override
    public void setOrientation(Orientation orientation) {
        super.setOrientation(orientation);
        setOrientation(orientation, true);
    }

    public void setOrientation(Orientation orientation, boolean recursively) {
        super.setOrientation(orientation);
        if (recursively) {
            subBlocks.forEach(sub -> sub.setOrientation(orientation));
        }
    }

    public Stream<Position.Segment> getSegments(Position.Dimension dimension) {
        return subBlocks.stream().map(b -> b.getPosition().getSegment(dimension));
    }

    void replicateCoordInSubblocks(Coord.Dimension dim) {
        getCoord().getSegment(dim).replicateMe(subBlocks.stream().map(b -> b.getCoord().getSegment(dim)));
    }

    void distributeCoordInSubblocs(Position.Dimension pDim, Coord.Dimension cDim, int sign) {
        double init = getCoord().get(cDim) - sign * getCoord().getSpan(cDim) / 2;

        // Computes the step, avoiding the division by 0 for 0-span composed block (e.g. LegPrimaryBlock + Feeder)
        int pSpan = getPosition().getSpan(pDim);
        double step = pSpan == 0 ? 0 : getCoord().getSpan(cDim) / pSpan;

        subBlocks.forEach(sub -> {
            sub.getCoord().set(cDim, init + sign * step * (sub.getPosition().get(pDim) + (double) sub.getPosition().getSpan(pDim) / 2));
            sub.getCoord().setSpan(cDim, sub.getPosition().getSpan(pDim) * step);
        });
    }

    void translatePosInCoord(LayoutParameters layoutParameters, Coord.Dimension cDimSteady,
                             Coord.Dimension cDimVariable, Position.Dimension pDim, int sign) {
        replicateCoordInSubblocks(cDimSteady);
        distributeCoordInSubblocs(pDim, cDimVariable, sign);
        subBlocks.forEach(sub -> sub.calculateCoord(layoutParameters));
    }

    @Override
    protected void writeJsonContent(JsonGenerator generator) throws IOException {
        generator.writeFieldName("subBlocks");
        generator.writeStartArray();
        for (Block subBlock : subBlocks) {
            subBlock.writeJson(generator);
        }
        generator.writeEndArray();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " " + subBlocks;
    }
}
