/*
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model;

import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;
import java.util.Objects;

import static com.powsybl.sld.library.ComponentTypeName.TWO_WINDINGS_TRANSFORMER;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class Middle2WTNode extends FictitiousNode {

    private final VoltageLevelInfos voltageLevelInfosLeg1;

    private final VoltageLevelInfos voltageLevelInfosLeg2;

    public Middle2WTNode(VoltageLevelGraph graph, String id, VoltageLevelInfos voltageLevelInfosLeg1, VoltageLevelInfos voltageLevelInfosLeg2) {
        super(graph, id, TWO_WINDINGS_TRANSFORMER);
        this.voltageLevelInfosLeg1 = Objects.requireNonNull(voltageLevelInfosLeg1);
        this.voltageLevelInfosLeg2 = Objects.requireNonNull(voltageLevelInfosLeg2);
    }

    public static Middle2WTNode create(BaseGraph graph, Node node1, Node node2, VoltageLevelInfos vlInfos1, VoltageLevelInfos vlInfos2) {
        Middle2WTNode middleNode = new Middle2WTNode(null, node1.getId() + "_" + node2.getId(), vlInfos1, vlInfos2);

        TwtEdge edge1 = graph.addTwtEdge(node1, middleNode);
        TwtEdge edge2 = graph.addTwtEdge(middleNode, node2);

        middleNode.addAdjacentEdge(edge1);
        middleNode.addAdjacentEdge(edge2);
        return middleNode;
    }

    @Override
    public VoltageLevelInfos getVoltageLevelInfos() {
        return null; // there is not a unique voltage level infos for a middle point so we consider this is undefined
    }

    public VoltageLevelInfos getVoltageLevelInfosLeg1() {
        return voltageLevelInfosLeg1;
    }

    public VoltageLevelInfos getVoltageLevelInfosLeg2() {
        return voltageLevelInfosLeg2;
    }

    public VoltageLevelInfos getVoltageLevelInfos(FeederWithSideNode.Side side) {
        Objects.requireNonNull(side);
        switch (side) {
            case ONE:
                return voltageLevelInfosLeg1;
            case TWO:
                return voltageLevelInfosLeg2;
            default:
                throw new IllegalStateException();
        }
    }

    @Override
    protected void writeJsonContent(JsonGenerator generator) throws IOException {
        super.writeJsonContent(generator);

        generator.writeFieldName("voltageLevelInfosLeg1");
        voltageLevelInfosLeg1.writeJsonContent(generator);

        generator.writeFieldName("voltageLevelInfosLeg2");
        voltageLevelInfosLeg2.writeJsonContent(generator);
    }
}
