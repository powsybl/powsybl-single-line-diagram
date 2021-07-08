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
import com.powsybl.iidm.network.Generator;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public class GeneratorDiagramDataExporterTest extends AbstractInjectionDiagramDataExporterTest {

    private Generator generator;

    @Before
    public void setUp() {
        super.setUp();

        network = Networks.createNetworkWithGenerator();
        generator = network.getGenerator("Generator");
        InjectionDiagramData<Generator> generatorDiagramData = new InjectionDiagramData<>(generator);
        InjectionDiagramData.InjectionDiagramDetails details = generatorDiagramData.new InjectionDiagramDetails(point, rotation);
        details.addTerminalPoint(terminalPoint1);
        details.addTerminalPoint(terminalPoint2);
        generatorDiagramData.addData(basename, details);
        generator.addExtension(InjectionDiagramData.class, generatorDiagramData);
        NetworkDiagramData.addDiagramName(network, basename, "Substation");

        Mockito.when(cgmesDLModel.getTerminals()).thenReturn(getTerminals(generator.getId()));
    }

    @Override
    protected void checkStatements() {
        checkStatements(generator.getId(), generator.getName(), "bus-branch");
    }

}
