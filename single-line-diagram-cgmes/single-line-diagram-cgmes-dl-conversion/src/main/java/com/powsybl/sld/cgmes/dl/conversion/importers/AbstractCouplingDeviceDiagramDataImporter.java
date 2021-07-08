/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.cgmes.dl.conversion.importers;

import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.sld.cgmes.dl.iidm.extensions.CouplingDeviceDiagramData;
import com.powsybl.sld.cgmes.dl.iidm.extensions.DiagramPoint;
import com.powsybl.sld.cgmes.dl.iidm.extensions.DiagramTerminal;
import com.powsybl.iidm.network.Network;
import com.powsybl.triplestore.api.PropertyBags;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public abstract class AbstractCouplingDeviceDiagramDataImporter {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractCouplingDeviceDiagramDataImporter.class);

    protected Network network;
    protected Map<String, PropertyBags> terminalsDiagramData;

    public AbstractCouplingDeviceDiagramDataImporter(Network network, Map<String, PropertyBags> terminalsDiagramData) {
        this.network = Objects.requireNonNull(network);
        this.terminalsDiagramData = Objects.requireNonNull(terminalsDiagramData);
    }

    protected void addTerminalPoints(String equipmentId, String equipmentName, String diagram, DiagramTerminal terminal, String terminalSide, CouplingDeviceDiagramData.CouplingDeviceDiagramDetails diagramDetails) {
        String terminalKey = diagram + "_" + equipmentId + "_" + terminalSide;
        if (terminalsDiagramData.containsKey(terminalKey)) {
            PropertyBags equipmentTerminalsDiagramData = terminalsDiagramData.get(terminalKey);
            equipmentTerminalsDiagramData.forEach(terminalDiagramData ->
                    diagramDetails.addTerminalPoint(terminal, new DiagramPoint(terminalDiagramData.asDouble("x"), terminalDiagramData.asDouble("y"), terminalDiagramData.asInt("seq")))
            );
        } else {
            LOG.warn("Cannot find terminal diagram data of equipment {}, name {}, terminal {}", equipmentId, equipmentName, terminal);
        }
    }
}
