/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld;

import com.powsybl.sld.layout.VoltageLevelLayout;
import com.powsybl.sld.layout.VoltageLevelLayoutFactory;
import com.powsybl.sld.model.Graph;
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
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public final class VoltageLevelDiagram implements Diagram {

    private static final Logger LOGGER = LoggerFactory.getLogger(VoltageLevelDiagram.class);

    private final Graph graph;

    private final VoltageLevelLayout vlLayout;

    private VoltageLevelDiagram(Graph graph, VoltageLevelLayout layout) {
        this.graph = Objects.requireNonNull(graph);
        this.vlLayout = Objects.requireNonNull(layout);
    }

    public static VoltageLevelDiagram build(GraphBuilder graphBuilder, String voltageLevelId,
                                            VoltageLevelLayoutFactory layoutFactory,
                                            boolean useName, boolean showInductorFor3WT) {
        Objects.requireNonNull(graphBuilder);
        Objects.requireNonNull(voltageLevelId);
        Objects.requireNonNull(layoutFactory);

        Graph graph = graphBuilder.buildVoltageLevelGraph(voltageLevelId, useName, true, showInductorFor3WT);

        VoltageLevelLayout layout = layoutFactory.create(graph);

        return new VoltageLevelDiagram(graph, layout);
    }

    public Graph getGraph() {
        return graph;
    }

    public void writeSvg(String prefixId,
                         SVGWriter writer,
                         DiagramInitialValueProvider initProvider,
                         DiagramStyleProvider styleProvider,
                         NodeLabelConfiguration nodeLabelConfiguration,
                         Writer svgWriter,
                         Writer metadataWriter) {
        Objects.requireNonNull(writer);
        Objects.requireNonNull(writer.getLayoutParameters());
        Objects.requireNonNull(svgWriter);
        Objects.requireNonNull(metadataWriter);

        // calculate coordinate
        vlLayout.run(writer.getLayoutParameters());

        // write SVG file
        LOGGER.info("Writing SVG and JSON metadata files...");

        GraphMetadata metadata = writer.write(prefixId, graph, initProvider, styleProvider, nodeLabelConfiguration, svgWriter);

        // write metadata file
        metadata.writeJson(metadataWriter);
    }
}
