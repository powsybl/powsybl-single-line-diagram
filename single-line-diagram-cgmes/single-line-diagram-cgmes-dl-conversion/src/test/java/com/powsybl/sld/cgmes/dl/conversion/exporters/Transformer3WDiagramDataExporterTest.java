/**
 * Copyright (c) 2019-2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.cgmes.dl.conversion.exporters;

import java.util.Arrays;

import com.powsybl.sld.cgmes.dl.iidm.extensions.NetworkDiagramData;
import org.junit.Before;
import org.mockito.Mockito;

import com.powsybl.sld.cgmes.dl.conversion.AbstractCgmesDLExporterTest;
import com.powsybl.sld.cgmes.dl.iidm.extensions.Networks;
import com.powsybl.sld.cgmes.dl.iidm.extensions.DiagramPoint;
import com.powsybl.sld.cgmes.dl.iidm.extensions.DiagramTerminal;
import com.powsybl.sld.cgmes.dl.iidm.extensions.ThreeWindingsTransformerDiagramData;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.triplestore.api.PropertyBags;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public class Transformer3WDiagramDataExporterTest extends AbstractCgmesDLExporterTest {

    private final DiagramPoint point = new DiagramPoint(20, 13, 0);
    private final double rotation = 90;
    private final DiagramPoint terminal1Point1 = new DiagramPoint(5, 10, 1);
    private final DiagramPoint terminal1Point2 = new DiagramPoint(15, 10, 2);
    private final String terminal1Id = "terminal1Id";
    private final DiagramPoint terminal2Point1 = new DiagramPoint(25, 10, 1);
    private final DiagramPoint terminal2Point2 = new DiagramPoint(35, 10, 2);
    private final String terminal2Id = "terminal2Id";
    private final DiagramPoint terminal3Point1 = new DiagramPoint(20, 16, 1);
    private final DiagramPoint terminal3Point2 = new DiagramPoint(20, 30, 2);
    private final String terminal3Id = "terminal3Id";
    private ThreeWindingsTransformer twt;

    @Before
    public void setUp() {
        super.setUp();

        network = Networks.createNetworkWithThreeWindingsTransformer();
        twt = network.getThreeWindingsTransformer("Transformer3w");
        ThreeWindingsTransformerDiagramData twtDiagramData = new ThreeWindingsTransformerDiagramData(twt);
        ThreeWindingsTransformerDiagramData.ThreeWindingsTransformerDiagramDataDetails details = twtDiagramData.new ThreeWindingsTransformerDiagramDataDetails(point, rotation);
        details.addTerminalPoint(DiagramTerminal.TERMINAL1, terminal1Point1);
        details.addTerminalPoint(DiagramTerminal.TERMINAL1, terminal1Point2);
        details.addTerminalPoint(DiagramTerminal.TERMINAL2, terminal2Point1);
        details.addTerminalPoint(DiagramTerminal.TERMINAL2, terminal2Point2);
        details.addTerminalPoint(DiagramTerminal.TERMINAL3, terminal3Point1);
        details.addTerminalPoint(DiagramTerminal.TERMINAL3, terminal3Point2);
        twtDiagramData.addData(basename, details);
        twt.addExtension(ThreeWindingsTransformerDiagramData.class, twtDiagramData);
        NetworkDiagramData.addDiagramName(network, basename, "Substation");

        Mockito.when(cgmesDLModel.getTerminals()).thenReturn(getTerminals(twt.getId()));
        Mockito.when(cgmesDLModel.getBusbarNodes()).thenReturn(new PropertyBags());
    }

    protected PropertyBags getTerminals(String injectionId) {
        return new PropertyBags(Arrays.asList(getTerminal(injectionId, terminal1Id, 1),
                                              getTerminal(injectionId, terminal2Id, 2),
                                              getTerminal(injectionId, terminal3Id, 3)));
    }

    protected void checkStatements(String couplingDeviceId, String injectionName, String styleName) {
        Mockito.verify(tripleStore, Mockito.times(14)).add(contextCaptor.capture(), nsCaptor.capture(),
                                                          typeCaptor.capture(), propertiesCaptor.capture());
        checkDiagram(contextCaptor.getAllValues().get(1), nsCaptor.getAllValues().get(1), typeCaptor.getAllValues().get(1),
                     propertiesCaptor.getAllValues().get(1));
        checkDiagramObjectStyle(contextCaptor.getAllValues().get(2), nsCaptor.getAllValues().get(2), typeCaptor.getAllValues().get(2),
                                propertiesCaptor.getAllValues().get(2), styleName);
        checkDiagramObject(contextCaptor.getAllValues().get(3), nsCaptor.getAllValues().get(3), typeCaptor.getAllValues().get(3),
                           propertiesCaptor.getAllValues().get(3), injectionName, couplingDeviceId, rotation);
        checkDiagramObjectPoint(contextCaptor.getAllValues().get(4), nsCaptor.getAllValues().get(4), typeCaptor.getAllValues().get(4),
                                propertiesCaptor.getAllValues().get(4), point);
        checkDiagramObject(contextCaptor.getAllValues().get(5), nsCaptor.getAllValues().get(5), typeCaptor.getAllValues().get(5),
                           propertiesCaptor.getAllValues().get(5), injectionName + "_0", terminal1Id, 0);
        checkDiagramObjectPoint(contextCaptor.getAllValues().get(6), nsCaptor.getAllValues().get(6), typeCaptor.getAllValues().get(6),
                                propertiesCaptor.getAllValues().get(6), terminal1Point1);
        checkDiagramObjectPoint(contextCaptor.getAllValues().get(7), nsCaptor.getAllValues().get(7), typeCaptor.getAllValues().get(7),
                                propertiesCaptor.getAllValues().get(7), terminal1Point2);
        checkDiagramObject(contextCaptor.getAllValues().get(8), nsCaptor.getAllValues().get(8), typeCaptor.getAllValues().get(8),
                           propertiesCaptor.getAllValues().get(8), injectionName + "_1", terminal2Id, 0);
        checkDiagramObjectPoint(contextCaptor.getAllValues().get(9), nsCaptor.getAllValues().get(9), typeCaptor.getAllValues().get(9),
                                propertiesCaptor.getAllValues().get(9), terminal2Point1);
        checkDiagramObjectPoint(contextCaptor.getAllValues().get(10), nsCaptor.getAllValues().get(10), typeCaptor.getAllValues().get(10),
                                propertiesCaptor.getAllValues().get(10), terminal2Point2);
        checkDiagramObject(contextCaptor.getAllValues().get(11), nsCaptor.getAllValues().get(11), typeCaptor.getAllValues().get(11),
                           propertiesCaptor.getAllValues().get(11), injectionName + "_2", terminal3Id, 0);
        checkDiagramObjectPoint(contextCaptor.getAllValues().get(12), nsCaptor.getAllValues().get(12), typeCaptor.getAllValues().get(12),
                                propertiesCaptor.getAllValues().get(12), terminal3Point1);
        checkDiagramObjectPoint(contextCaptor.getAllValues().get(13), nsCaptor.getAllValues().get(13), typeCaptor.getAllValues().get(13),
                                propertiesCaptor.getAllValues().get(13), terminal3Point2);
    }

    @Override
    protected void checkStatements() {
        checkStatements(twt.getId(), twt.getName(), "bus-branch");
    }

}
