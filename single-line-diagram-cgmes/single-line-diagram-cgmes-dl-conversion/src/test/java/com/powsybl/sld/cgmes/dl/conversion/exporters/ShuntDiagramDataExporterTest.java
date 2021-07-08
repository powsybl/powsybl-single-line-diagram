/**
 * Copyright (c) 2019-2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.cgmes.dl.conversion.exporters;

import com.powsybl.sld.cgmes.dl.iidm.extensions.NetworkDiagramData;
import org.junit.Before;
import org.mockito.Mockito;

import com.powsybl.sld.cgmes.dl.iidm.extensions.Networks;
import com.powsybl.sld.cgmes.dl.iidm.extensions.InjectionDiagramData;
import com.powsybl.iidm.network.ShuntCompensator;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public class ShuntDiagramDataExporterTest extends AbstractInjectionDiagramDataExporterTest {

    private ShuntCompensator shunt;

    @Before
    public void setUp() {
        super.setUp();

        network = Networks.createNetworkWithShuntCompensator();
        shunt = network.getShuntCompensator("Shunt");
        InjectionDiagramData<ShuntCompensator> shuntDiagramData = new InjectionDiagramData<>(shunt);
        InjectionDiagramData.InjectionDiagramDetails details = shuntDiagramData.new InjectionDiagramDetails(point, rotation);
        details.addTerminalPoint(terminalPoint1);
        details.addTerminalPoint(terminalPoint2);
        shuntDiagramData.addData(basename, details);
        shunt.addExtension(InjectionDiagramData.class, shuntDiagramData);
        NetworkDiagramData.addDiagramName(network, basename, "Substation");

        Mockito.when(cgmesDLModel.getTerminals()).thenReturn(getTerminals(shunt.getId()));
    }

    @Override
    protected void checkStatements() {
        checkStatements(shunt.getId(), shunt.getName(), "bus-branch");
    }

}
