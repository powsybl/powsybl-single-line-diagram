/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld;

import com.google.auto.service.AutoService;
import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.import_.Importers;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Substation;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.sld.layout.*;
import com.powsybl.sld.library.ComponentLibrary;
import com.powsybl.sld.library.ResourcesComponentLibrary;
import com.powsybl.sld.svg.DefaultDiagramInitialValueProvider;
import com.powsybl.sld.svg.DefaultDiagramStyleProvider;
import com.powsybl.sld.svg.DefaultNodeLabelConfiguration;
import com.powsybl.sld.svg.DefaultSVGWriter;
import com.powsybl.sld.svg.SVGWriter;
import com.powsybl.tools.Command;
import com.powsybl.tools.Tool;
import com.powsybl.tools.ToolOptions;
import com.powsybl.tools.ToolRunningContext;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import static com.powsybl.sld.AbstractSingleLineDiagramCommand.INPUT_FILE;
import static com.powsybl.sld.AbstractSingleLineDiagramCommand.OUTPUT_DIR;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(Tool.class)
public class SingleLineDiagramTool implements Tool {

    private static final String IDS = "ids";
    private static final String ALL_VOLTAGE_LEVELS = "all-voltage-levels";
    private static final String ALL_SUBSTATIONS = "all-substations";

    @Override
    public Command getCommand() {
        return new AbstractSingleLineDiagramCommand() {

            @Override
            public String getName() {
                return "generate-substation-diagram";
            }

            @Override
            public String getDescription() {
                return "generate substation diagram";
            }

            @Override
            public Options getOptions() {
                Options options = new Options();
                addInputFileOption(options);
                addOutputDirectoryOption(options);
                options.addOption(Option.builder().longOpt(IDS)
                        .desc("voltage level/substation id list")
                        .hasArg()
                        .argName("ID_LIST")
                        .build());
                options.addOption(Option.builder().longOpt(ALL_VOLTAGE_LEVELS)
                        .desc("all voltage levels")
                        .build());
                options.addOption(Option.builder().longOpt(ALL_SUBSTATIONS)
                        .desc("all substations")
                        .build());

                return options;
            }

            @Override
            public String getUsageFooter() {
                return null;
            }
        };
    }

    private Path getSvgFile(Path outputDir, Identifiable identifiable) {
        try {
            return outputDir.resolve(URLEncoder.encode(identifiable.getId(), StandardCharsets.UTF_8.name()) + ".svg");
        } catch (UnsupportedEncodingException e) {
            throw new PowsyblException(e);
        }
    }

    static class SvgGenerationConfig {

        ComponentLibrary componentLibrary = new ResourcesComponentLibrary("/ConvergenceLibrary");

        LayoutParameters parameters = new LayoutParameters();

        VoltageLevelLayoutFactory voltageLevelLayoutFactory;

        SubstationLayoutFactory substationLayoutFactory = new HorizontalSubstationLayoutFactory();

        SvgGenerationConfig(Network network) {
            voltageLevelLayoutFactory = new SmartVoltageLevelLayoutFactory(network);
        }
    }

    private void generateSvg(Diagram diag, SVGWriter writer, Network network,
                             String svgFileName, Path dir, Path svgFile) {
        try (Writer svgWriter = Files.newBufferedWriter(svgFile, StandardCharsets.UTF_8);
             Writer metadataWriter = Files.newBufferedWriter(dir.resolve(svgFileName.replace(".svg", "_metadata.json")), StandardCharsets.UTF_8)) {

            diag.writeSvg("",
                    writer,
                    new DefaultDiagramInitialValueProvider(network),
                    new DefaultDiagramStyleProvider(),
                    new DefaultNodeLabelConfiguration(writer.getComponentLibrary()),
                    svgWriter,
                    metadataWriter);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void generateVoltageLevelSvg(ToolRunningContext context, Path outputDir, SvgGenerationConfig generationConfig,
                                         VoltageLevel vl, GraphBuilder graphBuilder, Network network) {
        Path svgFile = getSvgFile(outputDir, vl);
        context.getOutputStream().println("Generating '" + svgFile + "' (" + vl.getNominalV() + ")");
        try {
            Diagram vlDiag = VoltageLevelDiagram.build(graphBuilder, vl.getId(), generationConfig.voltageLevelLayoutFactory, true, generationConfig.parameters.isShowInductorFor3WT());

            SVGWriter writer = new DefaultSVGWriter(generationConfig.componentLibrary, generationConfig.parameters);
            Path dir = svgFile.toAbsolutePath().getParent();
            String svgFileName = svgFile.getFileName().toString();
            if (!svgFileName.endsWith(".svg")) {
                svgFileName = svgFileName + ".svg";
            }

            generateSvg(vlDiag, writer, network, svgFileName, dir, svgFile);
        } catch (Exception e) {
            e.printStackTrace(context.getErrorStream());
        }
    }

    private void generateSubstationSvg(ToolRunningContext context, Path outputDir, SvgGenerationConfig generationConfig,
                                       Substation s, GraphBuilder graphBuilder, Network network) {
        Path svgFile = getSvgFile(outputDir, s);
        context.getOutputStream().println("Generating '" + svgFile + "'");
        try {
            Diagram substDiag = SubstationDiagram.build(graphBuilder, s.getId(), generationConfig.substationLayoutFactory, generationConfig.voltageLevelLayoutFactory, true);

            SVGWriter writer = new DefaultSVGWriter(generationConfig.componentLibrary, generationConfig.parameters);
            Path dir = svgFile.toAbsolutePath().getParent();
            String svgFileName = svgFile.getFileName().toString();
            if (!svgFileName.endsWith(".svg")) {
                svgFileName = svgFileName + ".svg";
            }

            generateSvg(substDiag, writer, network, svgFileName, dir, svgFile);
        } catch (Exception e) {
            e.printStackTrace(context.getErrorStream());
        }
    }

    private void generateSome(ToolRunningContext context, Path outputDir, List<String> ids, GraphBuilder graphBuilder, Network network, SvgGenerationConfig generationConfig) {
        for (String id : ids) {
            VoltageLevel vl = network.getVoltageLevel(id);
            if (vl == null) {
                Substation s = network.getSubstation(id);
                if (s == null) {
                    throw new PowsyblException("No voltage level or substation with id : '" + id + "'");
                } else {  // id is a substation id
                    generateSubstationSvg(context, outputDir, generationConfig, s, graphBuilder, network);
                }
            } else {  // id is a voltage level id
                generateVoltageLevelSvg(context, outputDir, generationConfig, vl, graphBuilder, network);
            }
        }
    }

    private void generateAll(ToolRunningContext context, boolean allVoltageLevels, boolean allSubstations,
                             Path outputDir, GraphBuilder graphBuilder, Network network, SvgGenerationConfig generationConfig) {
        // by default, export all voltage levels if no id given and no
        // additional option (all-voltage-levels or all-substations) given
        if (allVoltageLevels || !allSubstations) {
            // export all voltage levels
            for (VoltageLevel vl : network.getVoltageLevels()) {
                generateVoltageLevelSvg(context, outputDir, generationConfig, vl, graphBuilder, network);
            }
        }
        if (allSubstations) {
            // export all substations
            for (Substation s : network.getSubstations()) {
                generateSubstationSvg(context, outputDir, generationConfig, s, graphBuilder, network);
            }
        }
    }

    @Override
    public void run(CommandLine line, ToolRunningContext context) {
        ToolOptions toolOptions = new ToolOptions(line, context);
        Path inputFile = toolOptions.getPath(INPUT_FILE).orElseThrow(() -> new PowsyblException(INPUT_FILE  + " option is missing"));
        Path outputDir = toolOptions.getPath(OUTPUT_DIR).orElseThrow(() -> new PowsyblException(OUTPUT_DIR  + " option is missing"));
        List<String> ids = toolOptions.getValues(IDS).orElse(Collections.emptyList());
        context.getOutputStream().println("Loading network '" + inputFile + "'...");
        Network network = Importers.loadNetwork(inputFile);
        if (network == null) {
            throw new PowsyblException("File '" + inputFile + "' is not importable");
        }
        GraphBuilder graphBuilder = new NetworkGraphBuilder(network);
        SvgGenerationConfig generationConfig = new SvgGenerationConfig(network);
        if (ids.isEmpty()) {
            boolean allVoltageLevels = toolOptions.hasOption(ALL_VOLTAGE_LEVELS);
            boolean allSubstations = toolOptions.hasOption(ALL_SUBSTATIONS);
            generateAll(context, allVoltageLevels, allSubstations, outputDir, graphBuilder, network, generationConfig);
        } else {
            generateSome(context, outputDir, ids, graphBuilder, network, generationConfig);
        }
    }
}
