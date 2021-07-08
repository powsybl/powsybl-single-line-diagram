/**
 * Copyright (c) 2019-2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.cgmes.dl.conversion.importers;

import java.util.Objects;

import com.powsybl.sld.cgmes.dl.iidm.extensions.NetworkDiagramData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.sld.cgmes.dl.iidm.extensions.DiagramPoint;
import com.powsybl.sld.cgmes.dl.iidm.extensions.NodeDiagramData;
import com.powsybl.iidm.network.BusbarSection;
import com.powsybl.iidm.network.Network;
import com.powsybl.triplestore.api.PropertyBag;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public class BusbarDiagramDataImporter {

    private static final Logger LOG = LoggerFactory.getLogger(BusbarDiagramDataImporter.class);

    private Network network;

    public BusbarDiagramDataImporter(Network network) {
        this.network = Objects.requireNonNull(network);
    }

    public void importDiagramData(PropertyBag busbarDiagramData) {
        Objects.requireNonNull(busbarDiagramData);
        String busbarId = busbarDiagramData.getId("busbarSection");
        BusbarSection busbar = network.getBusbarSection(busbarId);
        if (busbar != null) {
            NodeDiagramData<BusbarSection> busbarIidmDiagramData = busbar.getExtension(NodeDiagramData.class);
            if (busbarIidmDiagramData == null) {
                busbarIidmDiagramData = new NodeDiagramData<>(busbar);
            }
            String diagramName = busbarDiagramData.get("diagramName");
            NodeDiagramData.NodeDiagramDataDetails diagramDetails = busbarIidmDiagramData.getData(diagramName);
            if (diagramDetails == null) {
                diagramDetails = busbarIidmDiagramData.new NodeDiagramDataDetails();
            }
            if (busbarDiagramData.asInt("seq") == 1) {
                diagramDetails.setPoint1(new DiagramPoint(busbarDiagramData.asDouble("x"), busbarDiagramData.asDouble("y"), busbarDiagramData.asInt("seq")));
            } else {
                diagramDetails.setPoint2(new DiagramPoint(busbarDiagramData.asDouble("x"), busbarDiagramData.asDouble("y"), busbarDiagramData.asInt("seq")));
            }
            busbarIidmDiagramData.addData(diagramName, diagramDetails);
            busbar.addExtension(NodeDiagramData.class, busbarIidmDiagramData);
            NetworkDiagramData.addDiagramName(network, diagramName, busbar.getTerminal().getVoltageLevel().getSubstation().getId());
        } else {
            LOG.warn("Cannot find busbar {}, name {} in network {}: skipping busbar diagram data", busbarId, busbarDiagramData.get("name"), network.getId());
        }
    }

}
