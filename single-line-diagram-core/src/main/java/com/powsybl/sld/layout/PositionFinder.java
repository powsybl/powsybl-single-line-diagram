/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout;

import com.powsybl.sld.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * a PositionFinder determines:
 * <ul>
 * <li>the positions of nodeBuses</li>
 * <li>cell order and direction of each cell connected to Bus (ie all cells except Shunt ones)</li>
 * </ul>
 *
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface PositionFinder {

    Map<BusNode, Integer> indexBusPosition(List<BusNode> busNodes);

    LBSCluster organizeLegBusSets(VoltageLevelGraph graph, List<LegBusSet> legBusSets);

    default List<Subsection> buildLayout(VoltageLevelGraph graph, boolean handleShunt) {
        if (graph.getNodes().isEmpty()) {
            return new ArrayList<>();
        }
        Map<BusNode, Integer> busToNb = indexBusPosition(graph.getNodeBuses());
        List<LegBusSet> legBusSets = LegBusSet.createLegBusSets(graph, busToNb, handleShunt);
        LBSCluster lbsCluster = organizeLegBusSets(graph, legBusSets);
        graph.setMaxBusPosition();
        List<Subsection> subsections = Subsection.createSubsections(graph, lbsCluster, handleShunt);
        organizeDirections(graph, subsections);
        return subsections;
    }

    default void forceSameOrientationForShuntedCell(VoltageLevelGraph graph) {
        graph.getCells().stream()
                .filter(c -> c.getType() == Cell.CellType.SHUNT).map(ShuntCell.class::cast)
                .forEach(sc -> sc.alignDirections(Side.LEFT));
    }

    default void organizeDirections(VoltageLevelGraph graph, List<Subsection> subsections) {
        forceSameOrientationForShuntedCell(graph);
    }
}
