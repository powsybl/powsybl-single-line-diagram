/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld;

import com.powsybl.iidm.network.Network;
import com.powsybl.sld.layout.LayoutParameters;
import com.powsybl.sld.library.ComponentLibrary;
import com.powsybl.sld.library.ResourcesComponentLibrary;
import com.powsybl.sld.svg.DefaultDiagramLabelProvider;
import com.powsybl.sld.svg.DefaultDiagramStyleProvider;
import com.powsybl.sld.svg.DefaultSVGWriter;

import java.nio.file.Path;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public abstract class AbstractDiagram implements Diagram {

    @Override
    public void writeSvg(Network network, Path svgFile) {
        ComponentLibrary componentLibrary = ResourcesComponentLibrary.getDefault();
        LayoutParameters layoutParameters = new LayoutParameters();
        writeSvg("",
            new DefaultSVGWriter(componentLibrary, layoutParameters),
            new DefaultDiagramLabelProvider(network, componentLibrary, layoutParameters),
            new DefaultDiagramStyleProvider(),
            svgFile);
    }

}
