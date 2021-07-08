/**
 * Copyright (c) 2019-2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.cgmes.dl.conversion.exporters;

import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.sld.cgmes.dl.iidm.extensions.NetworkDiagramData;
import com.powsybl.sld.cgmes.dl.iidm.extensions.Networks;
import com.powsybl.sld.cgmes.dl.iidm.extensions.VoltageLevelDiagramData;
import com.powsybl.triplestore.api.PropertyBags;
import org.junit.Before;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author Christian Biasuzzi <christian.biasuzzi@techrain.eu>
 */
public class VoltageLevelDiagramDataExporterTest extends AbstractVoltageLevelDiagramDataExporterTest {

    private VoltageLevel voltageLevel;
    private final String voltageLevelId = "VoltageLevel1";
    private final String nId = "2";

    @Before
    public void setUp() {
        super.setUp();

        network = Networks.createNetworkWithDoubleBusbarSections();
        voltageLevel = network.getVoltageLevel(voltageLevelId);
        VoltageLevelDiagramData.addInternalNodeDiagramPoint(network.getVoltageLevel(voltageLevelId), basename, Integer.parseInt(nId), point1);
        NetworkDiagramData.addDiagramName(network, basename, "Substation1");

        Mockito.when(cgmesDLModel.getBusbarNodes()).thenReturn(new PropertyBags());
        Mockito.when(cgmesDLModel.getVoltageLevelDiagramData()).thenReturn(new PropertyBags());
        Map<String, Set<String>> switchesMap = new HashMap<>();
        switchesMap.put(nId, Stream.of("Disconnector1", "Disconnector2", "Breaker1").collect(Collectors.toSet()));
        Mockito.when(cgmesDLModel.findCgmesConnectivityNodesSwitchesForks()).thenReturn(switchesMap);
    }

    @Override
    protected void checkStatements() {
        checkStatements(nId, nId, "node-breaker");
    }

}
