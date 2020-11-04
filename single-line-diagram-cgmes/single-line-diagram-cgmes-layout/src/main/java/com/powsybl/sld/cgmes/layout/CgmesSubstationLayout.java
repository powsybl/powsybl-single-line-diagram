/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.cgmes.layout;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.sld.layout.Layout;
import com.powsybl.sld.layout.LayoutParameters;
import com.powsybl.sld.model.Graph;
import com.powsybl.sld.model.Node;
import com.powsybl.sld.model.SubstationGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

/**
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class CgmesSubstationLayout extends AbstractCgmesLayout implements Layout {

    private static final Logger LOG = LoggerFactory.getLogger(CgmesSubstationLayout.class);

    private final SubstationGraph graph;

    public CgmesSubstationLayout(SubstationGraph graph, Network network) {
        this.network = Objects.requireNonNull(network);
        Objects.requireNonNull(graph);
        for (Graph vlGraph : graph.getNodes()) {
            removeFictitiousNodes(vlGraph, network.getVoltageLevel(vlGraph.getVoltageLevelInfos().getId()));
        }
        fixTransformersLabel = true;
        this.graph = graph;
    }

    @Override
    public void run(LayoutParameters layoutParam) {
        String diagramName = layoutParam.getDiagramName();
        if (!checkDiagram(diagramName, "substation " + graph.getSubstationId())) {
            return;
        }
        LOG.info("Applying CGMES-DL layout to network {}, substation {}, diagram name {}", network.getId(), graph.getSubstationId(), diagramName);
        for (Graph vlGraph : graph.getNodes()) {
            VoltageLevel vl = network.getVoltageLevel(vlGraph.getVoltageLevelInfos().getId());
            setNodeCoordinates(vl, vlGraph, diagramName);
        }
        for (Graph vlGraph : graph.getNodes()) {
            vlGraph.getNodes().forEach(node -> shiftNodeCoordinates(node, layoutParam.getScaleFactor()));
        }
        if (layoutParam.getScaleFactor() != 1) {
            for (Graph vlGraph : graph.getNodes()) {
                vlGraph.getNodes().forEach(node -> scaleNodeCoordinates(node, layoutParam.getScaleFactor()));
            }
        }
        for (Graph vlGraph : graph.getNodes()) {
            setVoltageLevelCoord(vlGraph);
        }

        setMultiNodesCoord();
    }

    private void setMultiNodesCoord() {
        for (Node multiNode : graph.getMultiTermNodes()) {
            List<Node> adjacentNodes = multiNode.getAdjacentNodes();
            multiNode.setX(adjacentNodes.get(0).getX(), false);
            multiNode.setY(adjacentNodes.get(0).getY(), false);
        }
    }
}
