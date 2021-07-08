/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.cgmes.dl.conversion;

import com.powsybl.iidm.network.BusbarSection;
import com.powsybl.iidm.network.Network;
import com.powsybl.sld.cgmes.dl.iidm.extensions.Networks;
import com.powsybl.sld.cgmes.dl.iidm.extensions.NodeDiagramData;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.HashSet;

import static org.junit.Assert.*;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public class CgmesDLImportPostProcessorTest extends CgmesDLModelTest  {

    @Test
    public void process() throws Exception {
        Network network = Networks.createNetworkWithBusbar();
        new CgmesDLImportPostProcessor(queryCatalog).process(network, tripleStore);

        BusbarSection busbar = network.getBusbarSection("Busbar");
        NodeDiagramData<BusbarSection> busDiagramData = busbar.getExtension(NodeDiagramData.class);
        assertNotNull(busDiagramData);
        assertNotNull(busDiagramData.getData(DEFAULT_DIAGRAM_NAME));
        NodeDiagramData.NodeDiagramDataDetails nodeDiagramDataDetails = busDiagramData.getData(DEFAULT_DIAGRAM_NAME);
        assertEquals(1, nodeDiagramDataDetails.getPoint1().getSeq(), 0);
        assertEquals(20, nodeDiagramDataDetails.getPoint1().getX(), 0);
        assertEquals(5, nodeDiagramDataDetails.getPoint1().getY(), 0);
        assertEquals(2, nodeDiagramDataDetails.getPoint2().getSeq(), 0);
        assertEquals(20, nodeDiagramDataDetails.getPoint2().getX(), 0);
        assertEquals(40, nodeDiagramDataDetails.getPoint2().getY(), 0);
    }

    @Test
    public void processNoDlContext() {
        Network network = Networks.createNetworkWithBusbar();
        Mockito.when(tripleStore.contextNames()).thenReturn(new HashSet<>(Arrays.asList("Network_EQ.xml", "Network_SV.xml", "Network_TP.xml")));
        new CgmesDLImportPostProcessor(queryCatalog).process(network, tripleStore);
        assertNull(network.getBusbarSection("Busbar").getExtension(NodeDiagramData.class));
    }
}
