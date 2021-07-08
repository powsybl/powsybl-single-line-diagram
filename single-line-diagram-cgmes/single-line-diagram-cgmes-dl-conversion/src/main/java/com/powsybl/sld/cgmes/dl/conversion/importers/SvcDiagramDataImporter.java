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
import com.powsybl.iidm.network.StaticVarCompensator;
import com.powsybl.sld.cgmes.dl.iidm.extensions.DiagramPoint;
import com.powsybl.sld.cgmes.dl.iidm.extensions.InjectionDiagramData;
import com.powsybl.sld.cgmes.dl.iidm.extensions.NetworkDiagramData;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public class SvcDiagramDataImporter extends AbstractInjectionDiagramDataImporter {

    private static final Logger LOG = LoggerFactory.getLogger(SvcDiagramDataImporter.class);

    public SvcDiagramDataImporter(Network network, Map<String, PropertyBags> terminalsDiagramData) {
        super(network, terminalsDiagramData);
    }

    public void importDiagramData(PropertyBag svcDiagramData) {
        Objects.requireNonNull(svcDiagramData);
        String svcId = svcDiagramData.getId("identifiedObject");
        StaticVarCompensator svc = network.getStaticVarCompensator(svcId);
        if (svc != null) {
            InjectionDiagramData<StaticVarCompensator> svcIidmDiagramData = svc.getExtension(InjectionDiagramData.class);
            if (svcIidmDiagramData == null) {
                svcIidmDiagramData = new InjectionDiagramData<>(svc);
            }
            InjectionDiagramData.InjectionDiagramDetails diagramDetails = svcIidmDiagramData.new InjectionDiagramDetails(new DiagramPoint(svcDiagramData.asDouble("x"), svcDiagramData.asDouble("y"), svcDiagramData.asInt("seq")),
                    svcDiagramData.asDouble("rotation"));
            addTerminalPoints(svcId, svc.getName(), svcDiagramData.get("diagramName"), diagramDetails);
            svcIidmDiagramData.addData(svcDiagramData.get("diagramName"), diagramDetails);
            svc.addExtension(InjectionDiagramData.class, svcIidmDiagramData);
            NetworkDiagramData.addDiagramName(network, svcDiagramData.get("diagramName"), svc.getTerminal().getVoltageLevel().getSubstation().getId());
        } else {
            LOG.warn("Cannot find svc {}, name {} in network {}: skipping svc diagram data", svcId, svcDiagramData.get("name"), network.getId());
        }
    }

}
