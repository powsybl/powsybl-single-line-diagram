/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld;

import com.powsybl.commons.PowsyblException;
import com.powsybl.sld.layout.SubstationLayoutFactory;
import com.powsybl.sld.layout.VoltageLevelLayoutFactory;
import com.powsybl.sld.layout.ZoneLayout;
import com.powsybl.sld.layout.ZoneLayoutFactory;
import com.powsybl.sld.model.ZoneGraph;
import com.powsybl.sld.svg.DiagramInitialValueProvider;
import com.powsybl.sld.svg.DiagramStyleProvider;
import com.powsybl.sld.svg.GraphMetadata;
import com.powsybl.sld.svg.NodeLabelConfiguration;
import com.powsybl.sld.svg.SVGWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Writer;
import java.util.Objects;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public final class ZoneDiagram implements Diagram {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZoneDiagram.class);

    private final ZoneGraph zoneGraph;

    private final ZoneLayout zoneLayout;

    private ZoneDiagram(ZoneGraph graph, ZoneLayout layout) {
        this.zoneGraph = Objects.requireNonNull(graph);
        this.zoneLayout = Objects.requireNonNull(layout);
    }

    public static ZoneDiagram build(GraphBuilder graphBuilder,
                                    ZoneId zoneId,
                                    ZoneLayoutFactory zLayoutFactory,
                                    SubstationLayoutFactory sLayoutFactory,
                                    VoltageLevelLayoutFactory vLayoutFactory,
                                    boolean useName) {
        Objects.requireNonNull(graphBuilder);
        Objects.requireNonNull(zLayoutFactory);
        Objects.requireNonNull(vLayoutFactory);

        if (zoneId.isEmpty()) {
            throw new PowsyblException("Zone without any substation");
        }

        ZoneGraph graph = graphBuilder.buildZoneGraph(zoneId, useName);

        ZoneLayout layout = zLayoutFactory.create(graph, sLayoutFactory, vLayoutFactory);

        return new ZoneDiagram(graph, layout);
    }

    public ZoneGraph getZoneGraph() {
        return zoneGraph;
    }

    public void writeSvg(String prefixId,
                         SVGWriter writer,
                         DiagramInitialValueProvider initProvider,
                         DiagramStyleProvider styleProvider,
                         NodeLabelConfiguration nodeLabelConfiguration,
                         Writer svgWriter, Writer metadataWriter) {
        Objects.requireNonNull(writer);
        Objects.requireNonNull(writer.getLayoutParameters());
        Objects.requireNonNull(svgWriter);
        Objects.requireNonNull(metadataWriter);

        zoneLayout.run(writer.getLayoutParameters());

        // write SVG file
        LOGGER.info("Writing SVG and JSON metadata files...");

        GraphMetadata metadata = writer.write(prefixId, zoneGraph, initProvider, styleProvider, nodeLabelConfiguration, svgWriter);

        // write metadata file
        metadata.writeJson(metadataWriter);
    }
}
