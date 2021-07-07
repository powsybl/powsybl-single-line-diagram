/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public abstract class AbstractLineGraph extends AbstractGraph implements LineGraph {

    private final List<BranchEdge> lineEdges = new ArrayList<>();

    @Override
    public BranchEdge addLineEdge(String lineId, Node node1, Node node2) {
        BranchEdge edge = new BranchEdge(lineId, node1, node2);
        lineEdges.add(edge);
        return edge;
    }

    @Override
    public List<BranchEdge> getLineEdges() {
        return new ArrayList<>(lineEdges);
    }
}
