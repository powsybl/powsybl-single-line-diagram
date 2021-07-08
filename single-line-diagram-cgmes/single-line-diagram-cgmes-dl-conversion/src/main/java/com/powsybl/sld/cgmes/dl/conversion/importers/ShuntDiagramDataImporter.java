/**
 * Copyright (c) 2019-2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.cgmes.dl.conversion.importers;

import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.ShuntCompensator;
import com.powsybl.sld.cgmes.dl.iidm.extensions.DiagramPoint;
import com.powsybl.sld.cgmes.dl.iidm.extensions.InjectionDiagramData;
import com.powsybl.sld.cgmes.dl.iidm.extensions.NetworkDiagramData;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public class ShuntDiagramDataImporter extends AbstractInjectionDiagramDataImporter {

    private static final Logger LOG = LoggerFactory.getLogger(ShuntDiagramDataImporter.class);

    public ShuntDiagramDataImporter(Network network, Map<String, PropertyBags> terminalsDiagramData) {
        super(network, terminalsDiagramData);
    }

    public void importDiagramData(PropertyBag shuntDiagramData) {
        Objects.requireNonNull(shuntDiagramData);
        String shuntId = shuntDiagramData.getId("identifiedObject");
        ShuntCompensator shunt = network.getShuntCompensator(shuntId);
        if (shunt != null) {
            InjectionDiagramData<ShuntCompensator> shuntIidmDiagramData = shunt.getExtension(InjectionDiagramData.class);
            if (shuntIidmDiagramData == null) {
                shuntIidmDiagramData = new InjectionDiagramData<>(shunt);
            }
            InjectionDiagramData.InjectionDiagramDetails diagramDetails = shuntIidmDiagramData.new InjectionDiagramDetails(new DiagramPoint(shuntDiagramData.asDouble("x"), shuntDiagramData.asDouble("y"), shuntDiagramData.asInt("seq")),
                    shuntDiagramData.asDouble("rotation"));
            addTerminalPoints(shuntId, shunt.getName(), shuntDiagramData.get("diagramName"), diagramDetails);
            shuntIidmDiagramData.addData(shuntDiagramData.get("diagramName"), diagramDetails);
            shunt.addExtension(InjectionDiagramData.class, shuntIidmDiagramData);
            NetworkDiagramData.addDiagramName(network, shuntDiagramData.get("diagramName"), shunt.getTerminal().getVoltageLevel().getSubstation().getId());
        } else {
            LOG.warn("Cannot find shunt {}, name {} in network {}: skipping shunt diagram data", shuntId, shuntDiagramData.get("name"), network.getId());
        }
    }

}
