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
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.sld.cgmes.dl.conversion.CgmesDLModel;
import com.powsybl.sld.cgmes.dl.iidm.extensions.CouplingDeviceDiagramData;
import com.powsybl.sld.cgmes.dl.iidm.extensions.DiagramPoint;
import com.powsybl.sld.cgmes.dl.iidm.extensions.DiagramTerminal;
import com.powsybl.sld.cgmes.dl.iidm.extensions.NetworkDiagramData;
import com.powsybl.sld.cgmes.dl.iidm.extensions.ThreeWindingsTransformerDiagramData;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public class TransformerDiagramDataImporter extends AbstractCouplingDeviceDiagramDataImporter {

    private static final Logger LOG = LoggerFactory.getLogger(TransformerDiagramDataImporter.class);

    public TransformerDiagramDataImporter(Network network, Map<String, PropertyBags> terminalsDiagramData) {
        super(network, terminalsDiagramData);
    }

    public void importDiagramData(PropertyBag transformersDiagramData) {
        Objects.requireNonNull(transformersDiagramData);
        String transformerId = transformersDiagramData.getId("identifiedObject");
        TwoWindingsTransformer transformer = network.getTwoWindingsTransformer(transformerId);
        if (transformer != null) {
            CouplingDeviceDiagramData<TwoWindingsTransformer> transformerIidmDiagramData = transformer.getExtension(CouplingDeviceDiagramData.class);
            if (transformerIidmDiagramData == null) {
                transformerIidmDiagramData = new CouplingDeviceDiagramData<>(transformer);
            }
            CouplingDeviceDiagramData.CouplingDeviceDiagramDetails diagramDetails = transformerIidmDiagramData.new CouplingDeviceDiagramDetails(new DiagramPoint(transformersDiagramData.asDouble("x"), transformersDiagramData.asDouble("y"), transformersDiagramData.asInt("seq")),
                    transformersDiagramData.asDouble("rotation"));
            addTerminalPoints(transformerId, transformer.getName(), transformersDiagramData.get(CgmesDLModel.DIAGRAM_NAME), DiagramTerminal.TERMINAL1, "1", diagramDetails);
            addTerminalPoints(transformerId, transformer.getName(), transformersDiagramData.get(CgmesDLModel.DIAGRAM_NAME), DiagramTerminal.TERMINAL2, "2", diagramDetails);
            transformerIidmDiagramData.addData(transformersDiagramData.get(CgmesDLModel.DIAGRAM_NAME), diagramDetails);
            transformer.addExtension(CouplingDeviceDiagramData.class, transformerIidmDiagramData);
            NetworkDiagramData.addDiagramName(network, transformersDiagramData.get(CgmesDLModel.DIAGRAM_NAME), transformer.getSubstation().getId());
        } else {
            ThreeWindingsTransformer transformer3w = network.getThreeWindingsTransformer(transformerId);
            if (transformer3w != null) {
                ThreeWindingsTransformerDiagramData transformerIidmDiagramData = transformer3w.getExtension(ThreeWindingsTransformerDiagramData.class);
                if (transformerIidmDiagramData == null) {
                    transformerIidmDiagramData = new ThreeWindingsTransformerDiagramData(transformer3w);
                }
                ThreeWindingsTransformerDiagramData.ThreeWindingsTransformerDiagramDataDetails diagramDetails = transformerIidmDiagramData.new ThreeWindingsTransformerDiagramDataDetails(new DiagramPoint(transformersDiagramData.asDouble("x"), transformersDiagramData.asDouble("y"), transformersDiagramData.asInt("seq")),
                        transformersDiagramData.asDouble("rotation"));
                addTerminalPoints(transformerId, transformer3w.getName(), transformersDiagramData.get(CgmesDLModel.DIAGRAM_NAME), DiagramTerminal.TERMINAL1, "1", diagramDetails);
                addTerminalPoints(transformerId, transformer3w.getName(), transformersDiagramData.get(CgmesDLModel.DIAGRAM_NAME), DiagramTerminal.TERMINAL2, "2", diagramDetails);
                addTerminalPoints(transformerId, transformer3w.getName(), transformersDiagramData.get(CgmesDLModel.DIAGRAM_NAME), DiagramTerminal.TERMINAL3, "3", diagramDetails);
                transformerIidmDiagramData.addData(transformersDiagramData.get(CgmesDLModel.DIAGRAM_NAME), diagramDetails);
                transformer3w.addExtension(ThreeWindingsTransformerDiagramData.class, transformerIidmDiagramData);
                NetworkDiagramData.addDiagramName(network, transformersDiagramData.get(CgmesDLModel.DIAGRAM_NAME), transformer3w.getSubstation().getId());
            } else {
                LOG.warn("Cannot find transformer {}, name {} in network {}: skipping transformer diagram data", transformerId, transformersDiagramData.get("name"), network.getId());
            }
        }
    }

    private void addTerminalPoints(String transformerId, String transformerName, String diagram, DiagramTerminal terminal, String terminalSide, ThreeWindingsTransformerDiagramData.ThreeWindingsTransformerDiagramDataDetails diagramDetails) {
        String terminalKey = diagram + "_" + transformerId + "_" + terminalSide;
        if (terminalsDiagramData.containsKey(terminalKey)) {
            PropertyBags equipmentTerminalsDiagramData = terminalsDiagramData.get(terminalKey);
            equipmentTerminalsDiagramData.forEach(terminalDiagramData ->
                diagramDetails.addTerminalPoint(terminal, new DiagramPoint(terminalDiagramData.asDouble("x"), terminalDiagramData.asDouble("y"), terminalDiagramData.asInt("seq")))
            );
        } else {
            LOG.warn("Cannot find terminal diagram data of transformer {}, name {}, terminal {}", transformerId, transformerName, terminal);
        }
    }

}
