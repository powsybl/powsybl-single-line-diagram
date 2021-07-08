/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.svg;

import java.io.Writer;
import java.nio.file.Path;

import com.powsybl.sld.layout.LayoutParameters;
import com.powsybl.sld.library.ComponentLibrary;
import com.powsybl.sld.model.VoltageLevelGraph;
import com.powsybl.sld.model.SubstationGraph;
import com.powsybl.sld.model.ZoneGraph;

/**
 * @author Gilles Brada <gilles.brada at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public interface SVGWriter {

    GraphMetadata write(String prefixId,
                        VoltageLevelGraph graph,
                        DiagramLabelProvider initProvider,
                        DiagramStyleProvider styleProvider,
                        Path svgFile);

    GraphMetadata write(String prefixId,
                        VoltageLevelGraph graph,
                        DiagramLabelProvider initProvider,
                        DiagramStyleProvider styleProvider,
                        Writer writer);

    GraphMetadata write(String prefixId,
                        SubstationGraph graph,
                        DiagramLabelProvider initProvider,
                        DiagramStyleProvider styleProvider,
                        Path svgFile);

    GraphMetadata write(String prefixId,
                        SubstationGraph graph,
                        DiagramLabelProvider initProvider,
                        DiagramStyleProvider styleProvider,
                        Writer writer);

    GraphMetadata write(String prefixId,
                        ZoneGraph graph,
                        DiagramLabelProvider initProvider,
                        DiagramStyleProvider styleProvider,
                        Path svgFile);

    GraphMetadata write(String prefixId,
                        ZoneGraph graph,
                        DiagramLabelProvider initProvider,
                        DiagramStyleProvider styleProvider,
                        Writer writer);

    LayoutParameters getLayoutParameters();

    ComponentLibrary getComponentLibrary();
}
