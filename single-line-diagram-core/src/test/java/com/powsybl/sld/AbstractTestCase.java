/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld;

import com.google.common.io.ByteStreams;
import com.powsybl.iidm.network.Network;
import com.powsybl.sld.layout.LayoutParameters;
import com.powsybl.sld.library.ResourcesComponentLibrary;
import com.powsybl.sld.model.Graph;
import com.powsybl.sld.model.SubstationGraph;
import com.powsybl.sld.model.ZoneGraph;
import com.powsybl.sld.svg.*;
import com.powsybl.sld.util.NominalVoltageDiagramStyleProvider;

import java.io.*;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 */
public abstract class AbstractTestCase {

    protected boolean writeFile = false;

    protected final ResourcesComponentLibrary componentLibrary = getResourcesComponentLibrary();
    protected LayoutParameters layoutParameters;

    protected ResourcesComponentLibrary getResourcesComponentLibrary() {
        return new ResourcesComponentLibrary("/ConvergenceLibrary");
    }

    protected static String normalizeLineSeparator(String str) {
        return str.replace("\r\n", "\n")
                .replace("\r", "\n");
    }

    public abstract void setUp() throws IOException;

    String getName() {
        return getClass().getSimpleName();
    }

    private void writeToFileInHomeDir(String filename, StringWriter content) {
        if (writeFile) {
            File homeFolder = new File(System.getProperty("user.home"));
            File file = new File(homeFolder, filename);
            try (OutputStreamWriter fw = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)){
                fw.write(content.toString());
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    public abstract void toSVG(Graph g, String filename);
    public abstract void toSVG(SubstationGraph g, String filename);
    public abstract void toSVG(ZoneGraph g, String filename);

    public String toSVG(Graph graph,
                        String filename,
                        LayoutParameters layoutParameters,
                        DiagramLabelProvider initValueProvider,
                        DiagramStyleProvider styleProvider) {
        try (StringWriter writer = new StringWriter()) {
            new DefaultSVGWriter(componentLibrary, layoutParameters)
                    .write("", graph,
                            initValueProvider,
                            styleProvider,
                            writer);

            writeToFileInHomeDir(filename, writer);

            return normalizeLineSeparator(writer.toString());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void compareMetadata(VoltageLevelDiagram diagram, LayoutParameters layoutParameters,
                                String refMetdataName,
                                DiagramLabelProvider initValueProvider,
                                DiagramStyleProvider styleProvider) {
        try (StringWriter writer = new StringWriter();
             StringWriter metadataWriter = new StringWriter()) {
            diagram.writeSvg("",
                    new DefaultSVGWriter(componentLibrary, layoutParameters),
                    initValueProvider, styleProvider,
                    writer, metadataWriter);

            writeToFileInHomeDir(refMetdataName, metadataWriter);

            String refMetadata = normalizeLineSeparator(new String(ByteStreams.toByteArray(getClass().getResourceAsStream(refMetdataName)), StandardCharsets.UTF_8));
            String metadata = normalizeLineSeparator(metadataWriter.toString());
            assertEquals(refMetadata, metadata);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public String toSVG(SubstationGraph graph,
                        String filename,
                        LayoutParameters layoutParameters,
                        DiagramLabelProvider initValueProvider,
                        DiagramStyleProvider styleProvider) {
        try (StringWriter writer = new StringWriter()) {
            new DefaultSVGWriter(componentLibrary, layoutParameters)
                    .write("", graph,
                            initValueProvider,
                            styleProvider,
                            writer);

            writeToFileInHomeDir(filename, writer);

            return normalizeLineSeparator(writer.toString());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void compareMetadata(SubstationDiagram diagram, LayoutParameters layoutParameters,
                                String refMetdataName,
                                DiagramLabelProvider initValueProvider,
                                DiagramStyleProvider styleProvider) {
        try (StringWriter writer = new StringWriter();
             StringWriter metadataWriter = new StringWriter()) {
            diagram.writeSvg("",
                    new DefaultSVGWriter(componentLibrary, layoutParameters),
                    initValueProvider,
                    styleProvider,
                    writer, metadataWriter);

            writeToFileInHomeDir(refMetdataName, metadataWriter);

            String refMetadata = normalizeLineSeparator(new String(ByteStreams.toByteArray(getClass().getResourceAsStream(refMetdataName)), StandardCharsets.UTF_8));
            String metadata = normalizeLineSeparator(metadataWriter.toString());
            assertEquals(refMetadata, metadata);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public String toJson(Graph graph, String filename) {
        try (StringWriter writer = new StringWriter()) {
            graph.writeJson(writer);

            writeToFileInHomeDir(filename, writer);

            if (writeFile) {
                toSVG(graph, filename.replace(".json", ".svg"));
            }

            return normalizeLineSeparator(writer.toString());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public String toJson(SubstationGraph graph, String filename) {
        try (StringWriter writer = new StringWriter()) {
            graph.writeJson(writer);

            writeToFileInHomeDir(filename, writer);

            if (writeFile) {
                toSVG(graph, filename.replace(".json", ".svg"));
            }

            return normalizeLineSeparator(writer.toString());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public String toJson(SubstationGraph graph, String filename, boolean genCoords) {
        graph.setGenerateCoordsInJson(genCoords);
        return toJson(graph, filename);
    }

    public String toJson(ZoneGraph graph, String filename, boolean generateCoordsInJson) {
        graph.setGenerateCoordsInJson(generateCoordsInJson);
        return toJson(graph, filename);
    }

    public String toJson(ZoneGraph graph, String filename) {
        try (StringWriter writer = new StringWriter()) {
            graph.writeJson(writer);
            writeToFileInHomeDir(filename, writer);
            if (writeFile) {
                toSVG(graph, filename.replace(".json", ".svg"));
            }
            return normalizeLineSeparator(writer.toString());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public String toString(String resourceName) {
        try {
            return normalizeLineSeparator(new String(ByteStreams.toByteArray(getClass().getResourceAsStream(resourceName)), StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public String toSVG(ZoneGraph graph, String filename, LayoutParameters layoutParameters, DiagramLabelProvider initValueProvider, DiagramStyleProvider styleProvider) {
        try (StringWriter writer = new StringWriter()) {
            new DefaultSVGWriter(componentLibrary, layoutParameters)
                    .write("", graph,
                            initValueProvider,
                            styleProvider,
                            writer);

            writeToFileInHomeDir(filename, writer);

            return normalizeLineSeparator(writer.toString());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}
