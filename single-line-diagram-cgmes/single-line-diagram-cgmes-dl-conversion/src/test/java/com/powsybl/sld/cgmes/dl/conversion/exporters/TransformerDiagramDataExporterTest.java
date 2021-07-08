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
import com.powsybl.sld.cgmes.dl.iidm.extensions.CouplingDeviceDiagramData;
import com.powsybl.sld.cgmes.dl.iidm.extensions.DiagramTerminal;
import com.powsybl.iidm.network.TwoWindingsTransformer;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public class TransformerDiagramDataExporterTest extends AbstractCouplingDeviceDiagramDataExporterTest {
    private TwoWindingsTransformer twt;

    @Before
    public void setUp() {
        super.setUp();

        network = Networks.createNetworkWithTwoWindingsTransformer();
        twt = network.getTwoWindingsTransformer("Transformer");
        CouplingDeviceDiagramData<TwoWindingsTransformer> twtDiagramData = new CouplingDeviceDiagramData<>(twt);
        CouplingDeviceDiagramData.CouplingDeviceDiagramDetails details = twtDiagramData.new CouplingDeviceDiagramDetails(point, rotation);
        details.addTerminalPoint(DiagramTerminal.TERMINAL1, terminal1Point1);
        details.addTerminalPoint(DiagramTerminal.TERMINAL1, terminal1Point2);
        details.addTerminalPoint(DiagramTerminal.TERMINAL2, terminal2Point1);
        details.addTerminalPoint(DiagramTerminal.TERMINAL2, terminal2Point2);
        twtDiagramData.addData(basename, details);
        twt.addExtension(CouplingDeviceDiagramData.class, twtDiagramData);
        NetworkDiagramData.addDiagramName(network, basename, "Substation");

        Mockito.when(cgmesDLModel.getTerminals()).thenReturn(getTerminals(twt.getId()));
    }

    @Override
    protected void checkStatements() {
        checkStatements(twt.getId(), twt.getName(), "bus-branch");
    }

}
