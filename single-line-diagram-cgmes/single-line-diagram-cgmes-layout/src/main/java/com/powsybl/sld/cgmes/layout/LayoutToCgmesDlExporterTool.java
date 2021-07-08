/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.cgmes.layout;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableMap;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.datasource.FileDataSource;
import com.powsybl.iidm.import_.Importers;
import com.powsybl.iidm.network.Network;
import com.powsybl.sld.AbstractSingleLineDiagramCommand;
import com.powsybl.sld.cgmes.dl.conversion.CgmesDLExporter;
import com.powsybl.sld.cgmes.dl.conversion.CgmesDLUtils;
import com.powsybl.sld.layout.*;
import com.powsybl.sld.layout.positionbyclustering.PositionByClustering;
import com.powsybl.sld.layout.positionfromextension.PositionFromExtension;
import com.powsybl.tools.Command;
import com.powsybl.tools.Tool;
import com.powsybl.tools.ToolOptions;
import com.powsybl.tools.ToolRunningContext;
import com.powsybl.triplestore.api.TripleStore;
import com.powsybl.triplestore.api.TripleStoreFactory;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import java.nio.file.Path;
import java.util.Map;

import static com.powsybl.sld.AbstractSingleLineDiagramCommand.INPUT_FILE;
import static com.powsybl.sld.AbstractSingleLineDiagramCommand.OUTPUT_DIR;

/**
 * @author Christian Biasuzzi <christian.biasuzzi@techrain.eu>
 */
@AutoService(Tool.class)
public class LayoutToCgmesDlExporterTool implements Tool {

    private static final String VOLTAGE_LEVEL_LAYOUT = "voltage-level-layout";
    private static final String SUBSTATION_LAYOUT = "substation-layout";
    private static final String DEFAULT_VOLTAGE_LAYOUT = "auto-without-extensions";
    private static final String DEFAULT_SUBSTATION_LAYOUT = "horizontal";
    private static final String DIAGRAM_NAME = "diagram-name";

    private final Map<String, VoltageLevelLayoutFactory> voltageLevelsLayouts
            = ImmutableMap.of("auto-extensions", new PositionVoltageLevelLayoutFactory(new PositionFromExtension()),
            DEFAULT_VOLTAGE_LAYOUT, new PositionVoltageLevelLayoutFactory(new PositionByClustering()));

    private final Map<String, SubstationLayoutFactory> substationsLayouts
            = ImmutableMap.of(DEFAULT_SUBSTATION_LAYOUT, new HorizontalSubstationLayoutFactory(),
            "vertical", new VerticalSubstationLayoutFactory());

    @Override
    public Command getCommand() {
        return new AbstractSingleLineDiagramCommand() {

            @Override
            public String getName() {
                return "generate-cgmes-dl";
            }

            @Override
            public String getDescription() {
                return "apply a layout to a network, generate and export a new CGMES-DL profile";
            }

            @Override
            public Options getOptions() {
                Options options = new Options();
                addInputFileOption(options);
                addOutputDirectoryOption(options);
                options.addOption(Option.builder().longOpt(VOLTAGE_LEVEL_LAYOUT)
                        .desc("voltage level layout")
                        .hasArg()
                        .argName("VOLTAGE LEVEL LAYOUT")
                        .build());
                options.addOption(Option.builder().longOpt(SUBSTATION_LAYOUT)
                        .desc("substation layout")
                        .hasArg()
                        .argName("SUBSTATION LAYOUT")
                        .build());
                options.addOption(Option.builder().longOpt(DIAGRAM_NAME)
                        .desc("diagram name")
                        .hasArg()
                        .argName("DIAGRAM NAME")
                        .build());
                return options;
            }

            @Override
            public String getUsageFooter() {
                return "Where SUBSTATION LAYOUT is one of: " + String.join(", ", substationsLayouts.keySet()) + " (default is: " + DEFAULT_SUBSTATION_LAYOUT + ")"
                        + " and VOLTAGE LEVEL LAYOUT is one of: " + String.join(", ", voltageLevelsLayouts.keySet()) + " (default is: " + DEFAULT_VOLTAGE_LAYOUT + ")";
            }
        };
    }

    @Override
    public void run(CommandLine line, ToolRunningContext context) {
        ToolOptions toolOptions = new ToolOptions(line, context);
        Path inputFile = toolOptions.getPath(INPUT_FILE).orElseThrow(() -> new PowsyblException(INPUT_FILE + " parameter is missing"));
        Path outputDir = toolOptions.getPath(OUTPUT_DIR).orElseThrow(() -> new PowsyblException(OUTPUT_DIR + " parameter is missing"));

        String substationLayout = toolOptions.getValue(SUBSTATION_LAYOUT).orElse(DEFAULT_SUBSTATION_LAYOUT);
        String voltageLayout = toolOptions.getValue(VOLTAGE_LEVEL_LAYOUT).orElse(DEFAULT_VOLTAGE_LAYOUT);
        SubstationLayoutFactory sFactory = substationsLayouts.get(substationLayout);
        if (sFactory == null) {
            throw new PowsyblException("invalid " + SUBSTATION_LAYOUT + ": " + substationLayout);
        }
        VoltageLevelLayoutFactory vFactory = voltageLevelsLayouts.get(voltageLayout);
        if (vFactory == null) {
            throw new PowsyblException("invalid " + VOLTAGE_LEVEL_LAYOUT + ": " + voltageLayout);
        }

        context.getOutputStream().println("Loading network '" + inputFile + "'...");
        Network network = Importers.loadNetwork(inputFile);

        context.getOutputStream().println("Generating layout for the network ...");
        LayoutToCgmesExtensionsConverter lTranslator = new LayoutToCgmesExtensionsConverter(sFactory, vFactory, new LayoutParameters(), true);

        String diagramName = toolOptions.getValue(DIAGRAM_NAME).orElse(null);
        lTranslator.convertLayout(network, diagramName);

        context.getOutputStream().println("Exporting network data (including the DL file) to " + outputDir);
        TripleStore tStore = CgmesDLUtils.getTripleStore(network);
        if (tStore == null) {
            tStore = TripleStoreFactory.create();
        }
        CgmesDLExporter dlExporter = new CgmesDLExporter(network, tStore);
        dlExporter.exportDLData(new FileDataSource(outputDir, network.getName()));
    }
}
