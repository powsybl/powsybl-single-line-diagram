/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model;

import com.fasterxml.jackson.core.JsonGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.stream.Collectors;

import static com.powsybl.sld.model.Cell.CellType.*;
import static com.powsybl.sld.model.Node.NodeType.*;
import static com.powsybl.sld.model.Position.Dimension.*;
import static com.powsybl.sld.model.Side.*;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ExternCell extends AbstractBusCell {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExternCell.class);

    private int order = -1;
    private ShuntCell shuntCell = null;

    public ExternCell(VoltageLevelGraph graph) {
        super(graph, EXTERN);
    }

    public void orderFromFeederOrders() {
        int sumOrder = 0;
        int nbFeeder = 0;
        for (FeederNode node : getNodes().stream()
                .filter(node -> node.getType() == FEEDER)
                .map(node -> (FeederNode) node).collect(Collectors.toList())) {
            sumOrder += node.getOrder();
            nbFeeder++;
        }
        if (nbFeeder != 0) {
            setOrder(sumOrder / nbFeeder);
        }
    }

    public void organizeBlockDirections() {
        getRootBlock().setOrientation(getDirection().toOrientation());
    }

    @Override
    public int newHPosition(int hPosition) {
        int minHv;
        if (isShunted() && shuntCell.getSideCell(RIGHT) == this) {
            Position leftPos = shuntCell.getSidePosition(LEFT);
            minHv = Math.max(hPosition, leftPos.get(H) + leftPos.getSpan(H) + shuntCell.getLength());
        } else {
            minHv = hPosition;
        }
        Position pos = getRootBlock().getPosition();
        pos.set(H, minHv);
        pos.set(V, 0);
        return minHv + pos.getSpan(H);
    }

    public boolean isShunted() {
        return shuntCell != null;
    }

    public ShuntCell getShuntCell() {
        return shuntCell;
    }

    public void setShuntCell(ShuntCell shuntCell) {
        this.shuntCell = shuntCell;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public String toString() {
        return getType() + " " + order + " " + getDirection() + " " + nodes;
    }

    @Override
    public void setDirection(Direction direction) {
        super.setDirection(direction);
        getNodes().stream().filter(f -> f.getType() == FEEDER)
                .map(FeederNode.class::cast)
                .forEach(fn -> {
                    if (fn.getOrientation() == null || !fn.getOrientation().isHorizontal()) {
                        fn.setOrientation(direction.toOrientation());
                        fn.setDirection(direction);
                    }
                });
    }

    @Override
    protected void writeJsonContent(JsonGenerator generator) throws IOException {
        super.writeJsonContent(generator);
        generator.writeNumberField("order", order);
    }
}
