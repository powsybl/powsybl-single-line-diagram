/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout;

import com.powsybl.sld.model.BusCell;
import com.powsybl.sld.model.Cell;
import com.powsybl.sld.model.VoltageLevelGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumSet;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class PositionVoltageLevelLayout extends AbstractVoltageLevelLayout {

    private static final Logger LOGGER = LoggerFactory.getLogger(PositionVoltageLevelLayout.class);

    public PositionVoltageLevelLayout(VoltageLevelGraph graph) {
        super(graph);
    }

    /**
     * Calculate real coordinate of busNode and blocks connected to busbar
     */
    @Override
    public void run(LayoutParameters layoutParam) {
        LOGGER.info("Running voltage level layout");
        calculateBusNodeCoord(getGraph(), layoutParam);
        calculateCellCoord(getGraph(), layoutParam);

        // Calculate all the coordinates for the middle nodes and the snake lines in the voltageLevel graph
        manageSnakeLines(layoutParam);
    }

    private void calculateBusNodeCoord(VoltageLevelGraph graph, LayoutParameters layoutParam) {
        graph.getNodeBuses().forEach(nb -> nb.calculateCoord(layoutParam));
    }

    private void calculateCellCoord(VoltageLevelGraph graph, LayoutParameters layoutParam) {
        if (layoutParam.isAdaptCellHeightToContent()) {
            // when using the adapt cell height to content option, we have to calculate the
            // maximum height of all the extern cells in each direction (top and bottom)
            calculateMaxCellHeight(layoutParam);
        }
        graph.getCells().stream()
                .filter(cell -> cell.getType() == Cell.CellType.EXTERN
                        || cell.getType() == Cell.CellType.INTERN)
                .forEach(cell -> cell.calculateCoord(layoutParam));
        graph.getCells().stream()
                .filter(cell -> cell.getType() == Cell.CellType.SHUNT)
                .forEach(cell -> cell.calculateCoord(layoutParam));
    }

    /**
     * Calculating the maximum height of all the extern cells in each direction (top and bottom). This height does not
     * include the constant stack height.
     * @param layoutParam the layout parameters
     */
    private void calculateMaxCellHeight(LayoutParameters layoutParam) {
        Map<BusCell.Direction, Double> maxCalculatedCellHeight = EnumSet.allOf(BusCell.Direction.class).stream().collect(Collectors.toMap(Function.identity(), v -> 0.));

        getGraph().getCells().stream()
                .filter(cell -> cell.getType() == Cell.CellType.EXTERN)
                .forEach(cell -> maxCalculatedCellHeight.compute(((BusCell) cell).getDirection(), (k, v) -> Math.max(v, cell.calculateHeight(layoutParam))));

        // if needed, adjusting the maximum calculated cell height to the minimum extern cell height parameter
        maxCalculatedCellHeight.compute(BusCell.Direction.TOP, (k, v) -> Math.max(v, layoutParam.getMinExternCellHeight()));
        maxCalculatedCellHeight.compute(BusCell.Direction.BOTTOM, (k, v) -> Math.max(v, layoutParam.getMinExternCellHeight()));

        getGraph().setMaxCalculatedCellHeight(maxCalculatedCellHeight);
    }
}
