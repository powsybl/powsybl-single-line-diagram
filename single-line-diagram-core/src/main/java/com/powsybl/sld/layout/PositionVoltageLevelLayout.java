/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout;

import com.powsybl.sld.model.*;
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
        calculateExternCellHeight(layoutParam);
        calculateBusNodeCoord(getGraph(), layoutParam);
        calculateCellCoord(getGraph(), layoutParam);

        // Calculate all the coordinates for the middle nodes and the snake lines in the voltageLevel graph
        manageSnakeLines(layoutParam);

        Point size = calculateSize(getGraph(), layoutParam);
        getGraph().setSize(size.getX(), size.getY());
    }

    private Point calculateSize(VoltageLevelGraph graph, LayoutParameters layoutParam) {
        double elementaryWidth = layoutParam.getCellWidth() / 2; // the elementary step within a voltageLevel Graph is half a cell width
        double width = layoutParam.getPaddingLeft()
            + graph.getMaxH() * elementaryWidth
            + layoutParam.getPaddingRight();
        double height = layoutParam.getPaddingTop()
            + graph.getExternCellHeight(BusCell.Direction.TOP)
            + 2 * layoutParam.getStackHeight() + layoutParam.getVerticalSpaceBus() * graph.getMaxV()
            + graph.getExternCellHeight(BusCell.Direction.BOTTOM)
            + layoutParam.getPaddingBottom();
        return new Point(width, height);
    }

    private void calculateBusNodeCoord(VoltageLevelGraph graph, LayoutParameters layoutParam) {
        graph.getNodeBuses().forEach(nb -> nb.calculateCoord(layoutParam));
    }

    private void calculateCellCoord(VoltageLevelGraph graph, LayoutParameters layoutParam) {
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
    private void calculateExternCellHeight(LayoutParameters layoutParam) {
        Map<BusCell.Direction, Double> maxCellHeight = EnumSet.allOf(BusCell.Direction.class).stream().collect(Collectors.toMap(Function.identity(), v -> 0.));
        if (layoutParam.isAdaptCellHeightToContent()) {
            // when using the adapt cell height to content option, we have to calculate the
            // maximum height of all the extern cells in each direction (top and bottom)
            getGraph().getCells().stream()
                .filter(cell -> cell.getType() == Cell.CellType.EXTERN)
                .forEach(cell -> maxCellHeight.compute(((BusCell) cell).getDirection(), (k, v) -> Math.max(v, cell.calculateHeight(layoutParam))));

            // if needed, adjusting the maximum calculated cell height to the minimum extern cell height parameter
            maxCellHeight.compute(BusCell.Direction.TOP, (k, v) -> Math.max(v, layoutParam.getMinExternCellHeight()) + getFeederSpan(layoutParam));
            maxCellHeight.compute(BusCell.Direction.BOTTOM, (k, v) -> Math.max(v, layoutParam.getMinExternCellHeight()) + getFeederSpan(layoutParam));
        } else {
            maxCellHeight.put(BusCell.Direction.TOP, layoutParam.getExternCellHeight());
            maxCellHeight.put(BusCell.Direction.BOTTOM, layoutParam.getExternCellHeight());
        }

        getGraph().setExternCellHeight(maxCellHeight);
    }

    public static double getFeederSpan(LayoutParameters layoutParam) {
        // The space needed between the feeder and the node connected to it corresponds to the space for feeder arrows
        // + half the height of the feeder component + half the height of that node component
        return layoutParam.getMinSpaceForFeederArrows() + layoutParam.getMaxComponentHeight();
    }
}
