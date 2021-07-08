/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.cgmes.layout;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.sld.layout.LayoutParameters;
import com.powsybl.sld.layout.VoltageLevelLayout;
import com.powsybl.sld.model.VoltageLevelGraph;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public class CgmesVoltageLevelLayout extends AbstractCgmesLayout implements VoltageLevelLayout {

    private static final Logger LOG = LoggerFactory.getLogger(CgmesVoltageLevelLayout.class);

    private final VoltageLevelGraph graph;

    public CgmesVoltageLevelLayout(VoltageLevelGraph graph, Network network) {
        this.network = Objects.requireNonNull(network);
        Objects.requireNonNull(graph);
        this.graph = removeFictitiousNodes(graph, network.getVoltageLevel(graph.getVoltageLevelInfos().getId()));
    }

    @Override
    public void run(LayoutParameters layoutParam) {
        VoltageLevel vl = network.getVoltageLevel(graph.getVoltageLevelInfos().getId());
        String diagramName = layoutParam.getDiagramName();
        if (!checkDiagram(diagramName, "voltage level " + vl.getId())) {
            return;
        }
        LOG.info("Applying CGMES-DL layout to network {}, voltage level {}, diagram name {}", network.getId(), graph.getVoltageLevelInfos().getId(), diagramName);
        setNodeCoordinates(vl, graph, diagramName);
        graph.getNodes().forEach(node -> shiftNodeCoordinates(node, layoutParam.getScaleFactor()));
        if (layoutParam.getScaleFactor() != 1) {
            graph.getNodes().forEach(node -> scaleNodeCoordinates(node, layoutParam.getScaleFactor()));
        }
    }
}
