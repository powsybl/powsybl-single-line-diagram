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

import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Network;
import com.powsybl.sld.cgmes.dl.iidm.extensions.DiagramPoint;
import com.powsybl.sld.cgmes.dl.iidm.extensions.InjectionDiagramData;
import com.powsybl.sld.cgmes.dl.iidm.extensions.NetworkDiagramData;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public class GeneratorDiagramDataImporter extends AbstractInjectionDiagramDataImporter {

    private static final Logger LOG = LoggerFactory.getLogger(GeneratorDiagramDataImporter.class);

    public GeneratorDiagramDataImporter(Network network, Map<String, PropertyBags> terminalsDiagramData) {
        super(network, terminalsDiagramData);
    }

    public void importDiagramData(PropertyBag generatorDiagramData) {
        Objects.requireNonNull(generatorDiagramData);
        String generatorId = generatorDiagramData.getId("identifiedObject");
        Generator generator = network.getGenerator(generatorId);
        if (generator != null) {
            InjectionDiagramData<Generator> generatorIidmDiagramData = generator.getExtension(InjectionDiagramData.class);
            if (generatorIidmDiagramData == null) {
                generatorIidmDiagramData = new InjectionDiagramData<>(generator);
            }
            InjectionDiagramData.InjectionDiagramDetails diagramDetails = generatorIidmDiagramData.new InjectionDiagramDetails(new DiagramPoint(generatorDiagramData.asDouble("x"), generatorDiagramData.asDouble("y"), generatorDiagramData.asInt("seq")),
                    generatorDiagramData.asDouble("rotation"));
            addTerminalPoints(generatorId, generator.getName(), generatorDiagramData.get("diagramName"), diagramDetails);
            generatorIidmDiagramData.addData(generatorDiagramData.get("diagramName"), diagramDetails);
            generator.addExtension(InjectionDiagramData.class, generatorIidmDiagramData);
            NetworkDiagramData.addDiagramName(network, generatorDiagramData.get("diagramName"), generator.getTerminal().getVoltageLevel().getSubstation().getId());
        } else {
            LOG.warn("Cannot find generator {}, name {} in network {}: skipping generator diagram data", generatorId, generatorDiagramData.get("name"), network.getId());
        }
    }

}
