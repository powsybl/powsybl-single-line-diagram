/**
 * Copyright (c) 2019-2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.cgmes.dl.conversion.exporters;

import java.util.Arrays;
import java.util.Collections;

import com.powsybl.sld.cgmes.dl.iidm.extensions.NetworkDiagramData;
import org.junit.Before;
import org.mockito.Mockito;

import com.powsybl.sld.cgmes.dl.iidm.extensions.Networks;
import com.powsybl.sld.cgmes.dl.iidm.extensions.NodeDiagramData;
import com.powsybl.iidm.network.BusbarSection;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public class BusbarDiagramDataExporterTest extends AbstractNodeLineDiagramDataExporterTest {

    private BusbarSection busbar;
    private final String busbarNodeId = "busbarNodeId";

    @Before
    public void setUp() {
        super.setUp();

        network = Networks.createNetworkWithBusbar();
        busbar = network.getVoltageLevel("VoltageLevel").getNodeBreakerView().getBusbarSection("Busbar");
        NodeDiagramData<BusbarSection> busbarDiagramData = new NodeDiagramData<>(busbar);
        NodeDiagramData.NodeDiagramDataDetails details = busbarDiagramData.new NodeDiagramDataDetails();
        details.setPoint1(point1);
        details.setPoint2(point2);
        busbarDiagramData.addData(basename, details);
        busbar.addExtension(NodeDiagramData.class, busbarDiagramData);
        NetworkDiagramData.addDiagramName(network, basename, "Substation");

        PropertyBag busbarNode = new PropertyBag(Arrays.asList("busbarSection", "busbarNode"));
        busbarNode.put("busbarSection", dataNs + busbar.getId());
        busbarNode.put("busbarNode", dataNs + busbarNodeId);
        PropertyBags busbarNodes = new PropertyBags(Collections.singleton(busbarNode));
        Mockito.when(cgmesDLModel.getBusbarNodes()).thenReturn(busbarNodes);
    }

    @Override
    protected void checkStatements() {
        checkStatements(busbarNodeId, busbar.getName(), "node-breaker");
    }

}
