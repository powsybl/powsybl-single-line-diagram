/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model;

import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;
import java.util.Objects;

import static com.powsybl.sld.library.ComponentTypeName.THREE_WINDINGS_TRANSFORMER;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class Middle3WTNode extends FictitiousNode {

    private static final String EDGE_SUFFIX = "_EDGE";

    private final VoltageLevelInfos voltageLevelInfosLeg1;

    private final VoltageLevelInfos voltageLevelInfosLeg2;

    private final VoltageLevelInfos voltageLevelInfosLeg3;

    public Middle3WTNode(VoltageLevelGraph graph, String id, VoltageLevelInfos voltageLevelInfosLeg1, VoltageLevelInfos voltageLevelInfosLeg2,
                         VoltageLevelInfos voltageLevelInfosLeg3) {
        super(graph, id, THREE_WINDINGS_TRANSFORMER);
        this.voltageLevelInfosLeg1 = Objects.requireNonNull(voltageLevelInfosLeg1);
        this.voltageLevelInfosLeg2 = Objects.requireNonNull(voltageLevelInfosLeg2);
        this.voltageLevelInfosLeg3 = Objects.requireNonNull(voltageLevelInfosLeg3);
    }

    public static Middle3WTNode create(BaseGraph ssGraph, Node node1, Node node2, Node node3,
                                       VoltageLevelInfos vlInfos1, VoltageLevelInfos vlInfos2, VoltageLevelInfos vlInfos3) {
        Middle3WTNode middleNode = new Middle3WTNode(null, node1.getId() + "_" + node2.getId() + "_" + node3.getId(),
                vlInfos1, vlInfos2, vlInfos3);

        BranchEdge edge1 = ssGraph.addTwtEdge(node1.getId() + EDGE_SUFFIX, node1, middleNode);
        BranchEdge edge2 = ssGraph.addTwtEdge(node2.getId() + EDGE_SUFFIX, middleNode, node2);
        BranchEdge edge3 = ssGraph.addTwtEdge(node3.getId() + EDGE_SUFFIX, middleNode, node3);

        middleNode.addAdjacentEdge(edge1);
        middleNode.addAdjacentEdge(edge2);
        middleNode.addAdjacentEdge(edge3);
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

    public VoltageLevelInfos getVoltageLevelInfosLeg3() {
        return voltageLevelInfosLeg3;
    }

    public VoltageLevelInfos getVoltageLevelInfos(FeederWithSideNode.Side side) {
        Objects.requireNonNull(side);
        switch (side) {
            case ONE:
                return voltageLevelInfosLeg1;
            case TWO:
                return voltageLevelInfosLeg2;
            case THREE:
                return voltageLevelInfosLeg3;
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

        generator.writeFieldName("voltageLevelInfosLeg3");
        voltageLevelInfosLeg3.writeJsonContent(generator);
    }
}
