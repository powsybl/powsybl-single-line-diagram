/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.cgmes.dl.conversion;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Switch;
import com.powsybl.iidm.network.TopologyKind;
import com.powsybl.sld.cgmes.dl.conversion.importers.*;
import com.powsybl.triplestore.api.PropertyBags;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public class CgmesDLImporter {

    private static final Logger LOG = LoggerFactory.getLogger(CgmesDLImporter.class);

    private Network network;
    private CgmesDLModel cgmesDLModel;
    private Map<String, PropertyBags> terminalsDiagramData = new HashMap<>();

    public CgmesDLImporter(Network network, CgmesDLModel cgmesDLModel) {
        this.network = Objects.requireNonNull(network);
        this.cgmesDLModel = Objects.requireNonNull(cgmesDLModel);
    }

    public void importDLData() {
        importTerminalsDLData();
        importBusesDLData();
        importBusbarsDLData();
        importLinesDLData();
        importGeneratorsDLData();
        importLoadsDLData();
        importShuntsDLData();
        importSwitchesDLData();
        importTransformersDLData();
        importHvdcLinesDLData();
        importSvcsDLData();
        importVoltageLevelDLData();
    }

    private void importTerminalsDLData() {
        LOG.info("Importing Terminals DL Data");
        cgmesDLModel.getTerminalsDiagramData().forEach(terminalDiagramData -> {
            String terminalKey = terminalDiagramData.get("diagramName") + "_" + terminalDiagramData.getId("terminalEquipment") + "_" + terminalDiagramData.get("terminalSide");
            PropertyBags equipmentTerminalsDiagramData = new PropertyBags();
            if (terminalsDiagramData.containsKey(terminalKey)) {
                equipmentTerminalsDiagramData = terminalsDiagramData.get(terminalKey);
            }
            equipmentTerminalsDiagramData.add(terminalDiagramData);
            terminalsDiagramData.put(terminalKey, equipmentTerminalsDiagramData);
        });
    }

    private void importBusesDLData() {
        LOG.info("Importing Buses DL Data");
        BusDiagramDataImporter diagramDataImporter = new BusDiagramDataImporter(network);
        cgmesDLModel.getBusesDiagramData().forEach(diagramDataImporter::importDiagramData);
    }

    private void importBusbarsDLData() {
        LOG.info("Importing Busbars DL Data");
        BusbarDiagramDataImporter diagramDataImporter = new BusbarDiagramDataImporter(network);
        cgmesDLModel.getBusbarsDiagramData().forEach(diagramDataImporter::importDiagramData);
    }

    private void importLinesDLData() {
        LOG.info("Importing Lines DL Data");
        LineDiagramDataImporter diagramDataImporter = new LineDiagramDataImporter(network);
        cgmesDLModel.getLinesDiagramData().forEach(diagramDataImporter::importDiagramData);
    }

    private void importGeneratorsDLData() {
        LOG.info("Importing Generators DL Data");
        GeneratorDiagramDataImporter diagramDataImporter = new GeneratorDiagramDataImporter(network, terminalsDiagramData);
        cgmesDLModel.getGeneratorsDiagramData().forEach(diagramDataImporter::importDiagramData);
    }

    private void importLoadsDLData() {
        LOG.info("Importing Loads DL Data");
        LoadDiagramDataImporter diagramDataImporter = new LoadDiagramDataImporter(network, terminalsDiagramData);
        cgmesDLModel.getLoadsDiagramData().forEach(diagramDataImporter::importDiagramData);
    }

    private void importShuntsDLData() {
        LOG.info("Importing Shunts DL Data");
        ShuntDiagramDataImporter diagramDataImporter = new ShuntDiagramDataImporter(network, terminalsDiagramData);
        cgmesDLModel.getShuntsDiagramData().forEach(diagramDataImporter::importDiagramData);
    }

    private void importSwitchesDLData() {
        LOG.info("Importing Switches DL Data");
        SwitchDiagramDataImporter diagramDataImporter = new SwitchDiagramDataImporter(network, terminalsDiagramData);
        cgmesDLModel.getSwitchesDiagramData().forEach(diagramDataImporter::importDiagramData);
    }

    private void importTransformersDLData() {
        LOG.info("Importing Transformers DL Data");
        TransformerDiagramDataImporter diagramDataImporter = new TransformerDiagramDataImporter(network, terminalsDiagramData);
        cgmesDLModel.getTransformersDiagramData().forEach(diagramDataImporter::importDiagramData);
    }

    private void importHvdcLinesDLData() {
        LOG.info("Importing HVDC Lines DL Data");
        HvdcLineDiagramDataImporter diagramDataImporter = new HvdcLineDiagramDataImporter(network);
        cgmesDLModel.getHvdcLinesDiagramData().forEach(diagramDataImporter::importDiagramData);
    }

    private void importSvcsDLData() {
        LOG.info("Importing Svcs DL Data");
        SvcDiagramDataImporter diagramDataImporter = new SvcDiagramDataImporter(network, terminalsDiagramData);
        cgmesDLModel.getSvcsDiagramData().forEach(diagramDataImporter::importDiagramData);
    }

    private void importVoltageLevelDLData() {
        LOG.info("Importing VoltageLevel DL Data");
        Map<String, Set<String>> nodeSwitches = cgmesDLModel.findCgmesConnectivityNodesSwitchesForks();
        Map<String, Map<String, Integer>> mapCnodeInode = new HashMap<>();
        network.getVoltageLevelStream().forEach(vl -> {
            if (vl.getTopologyKind().equals(TopologyKind.NODE_BREAKER)) {
                for (int n : vl.getNodeBreakerView().getNodes()) {
                    Set<Switch> switches = CgmesDLUtils.findSurroundingSwitches(vl.getNodeBreakerView(), n);
                    if (switches.size() > 1) {
                        mapCnodeInode.putIfAbsent(vl.getId(), new HashMap<>());
                        String matchedCnode = CgmesDLUtils.findMatchingConnectivityNodeId(nodeSwitches, switches);
                        if (matchedCnode != null) {
                            mapCnodeInode.get(vl.getId()).putIfAbsent(matchedCnode, n);
                        }
                    }
                }
            }
        });
        VoltageLevelDiagramDataImporter diagramDataImporter = new VoltageLevelDiagramDataImporter(network, mapCnodeInode);
        cgmesDLModel.getVoltageLevelDiagramData().forEach(diagramDataImporter::importDiagramData);
    }

    public Network getNetworkWithDLData() {
        return network;
    }

}
