/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld;

import com.powsybl.iidm.network.Network;
import com.powsybl.sld.svg.DiagramLabelProvider;
import com.powsybl.sld.svg.DiagramStyleProvider;
import com.powsybl.sld.svg.SVGWriter;

import java.io.Writer;
import java.nio.file.Path;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public interface Diagram {
    void writeSvg(Network network, Path svgFile);

    void writeSvg(String prefixId,
                  SVGWriter writer,
                  DiagramLabelProvider initProvider,
                  DiagramStyleProvider styleProvider,
                  Path svgFile);

    void writeSvg(String prefixId,
                  SVGWriter writer,
                  DiagramLabelProvider initProvider,
                  DiagramStyleProvider styleProvider,
                  Writer svgWriter,
                  Writer metadataWriter);
}
