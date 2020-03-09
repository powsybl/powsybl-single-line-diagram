/*
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.sld;

import com.powsybl.sld.svg.DiagramInitialValueProvider;
import com.powsybl.sld.svg.DiagramStyleProvider;
import com.powsybl.sld.svg.NodeLabelConfiguration;
import com.powsybl.sld.svg.SVGWriter;

import java.io.Writer;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public interface Diagram {
    void writeSvg(String prefixId,
                  SVGWriter writer,
                  DiagramInitialValueProvider initProvider,
                  DiagramStyleProvider styleProvider,
                  NodeLabelConfiguration nodeLabelConfiguration,
                  Writer svgWriter,
                  Writer metadataWriter);
}
