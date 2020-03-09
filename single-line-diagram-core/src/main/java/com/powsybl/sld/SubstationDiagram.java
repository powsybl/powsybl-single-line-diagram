/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld;

import com.powsybl.sld.layout.HorizontalSubstationLayoutFactory;
import com.powsybl.sld.layout.PositionVoltageLevelLayoutFactory;
import com.powsybl.sld.layout.SubstationLayout;
import com.powsybl.sld.layout.SubstationLayoutFactory;
import com.powsybl.sld.layout.VoltageLevelLayoutFactory;
import com.powsybl.sld.model.SubstationGraph;
import com.powsybl.sld.svg.GraphMetadata;
import com.powsybl.sld.svg.NodeLabelConfiguration;
import com.powsybl.sld.svg.SVGWriter;
import com.powsybl.sld.svg.DiagramInitialValueProvider;
import com.powsybl.sld.svg.DiagramStyleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Writer;
import java.util.Objects;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public final class SubstationDiagram implements Diagram {

    private static final Logger LOGGER = LoggerFactory.getLogger(SubstationDiagram.class);

    private final SubstationGraph subGraph;

    private final SubstationLayout subLayout;

    private SubstationDiagram(SubstationGraph graph, SubstationLayout layout) {
        this.subGraph = Objects.requireNonNull(graph);
        this.subLayout = Objects.requireNonNull(layout);
    }

    public static SubstationDiagram build(GraphBuilder graphBuilder, String substationId) {
        return build(graphBuilder, substationId,
                     new HorizontalSubstationLayoutFactory(),
                     new PositionVoltageLevelLayoutFactory(), false);
    }

    public SubstationGraph getSubGraph() {
        return subGraph;
    }

    public static SubstationDiagram build(GraphBuilder graphBuilder, String substationId,
                                          SubstationLayoutFactory sLayoutFactory,
                                          VoltageLevelLayoutFactory vLayoutFactory,
                                          boolean useName) {
        Objects.requireNonNull(graphBuilder);
        Objects.requireNonNull(substationId);
        Objects.requireNonNull(sLayoutFactory);
        Objects.requireNonNull(vLayoutFactory);

        SubstationGraph graph = graphBuilder.buildSubstationGraph(substationId, useName);

        SubstationLayout layout = sLayoutFactory.create(graph, vLayoutFactory);

        return new SubstationDiagram(graph, layout);
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

        subLayout.run(writer.getLayoutParameters());

        // write SVG file
        LOGGER.info("Writing SVG and JSON metadata files...");

        GraphMetadata metadata = writer.write(prefixId, subGraph, initProvider, styleProvider, nodeLabelConfiguration, svgWriter);

        // write metadata file
        metadata.writeJson(metadataWriter);
    }
}
