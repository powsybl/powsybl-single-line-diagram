/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout;

import com.powsybl.sld.model.BusCell;
import com.powsybl.sld.model.SubstationGraph;
import com.powsybl.sld.model.VoltageLevelGraph;

import java.util.EnumSet;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 * @author Slimane Amar <slimane.amar at rte-france.com>
 */
public final class InfosNbSnakeLinesHorizontal {

    private final Map<BusCell.Direction, Integer> nbSnakeLinesTopBottom;
    private final Map<String, Integer> nbSnakeLinesVerticalBetween;

    static InfosNbSnakeLinesHorizontal create(SubstationGraph substationGraph) {
        Map<BusCell.Direction, Integer> nbSnakeLinesTopBottom = EnumSet.allOf(BusCell.Direction.class).stream().collect(Collectors.toMap(Function.identity(), v -> 0));
        Map<String, Integer> nbSnakeLinesVerticalBetween = substationGraph.getNodeStream().collect(Collectors.toMap(g -> g.getVoltageLevelInfos().getId(), v -> 0));
        return new InfosNbSnakeLinesHorizontal(nbSnakeLinesTopBottom, nbSnakeLinesVerticalBetween);
    }

    static InfosNbSnakeLinesHorizontal create(VoltageLevelGraph graph) {
        // used only for horizontal layout
        Map<BusCell.Direction, Integer> nbSnakeLinesTopBottom = EnumSet.allOf(BusCell.Direction.class).stream().collect(Collectors.toMap(Function.identity(), v -> 0));
        Map<String, Integer> nbSnakeLinesBetween = Stream.of(graph).collect(Collectors.toMap(g -> g.getVoltageLevelInfos().getId(), v -> 0));

        return new InfosNbSnakeLinesHorizontal(nbSnakeLinesTopBottom, nbSnakeLinesBetween);
    }

    private InfosNbSnakeLinesHorizontal(Map<BusCell.Direction, Integer> nbSnakeLinesTopBottom,
                                        Map<String, Integer> nbSnakeLinesVerticalBetween) {
        this.nbSnakeLinesTopBottom = nbSnakeLinesTopBottom;
        this.nbSnakeLinesVerticalBetween = nbSnakeLinesVerticalBetween;
    }

    public Map<BusCell.Direction, Integer> getNbSnakeLinesTopBottom() {
        return nbSnakeLinesTopBottom;
    }

    public Map<String, Integer> getNbSnakeLinesVerticalBetween() {
        return nbSnakeLinesVerticalBetween;
    }

    public void reset() {
        nbSnakeLinesTopBottom.keySet().forEach(side -> nbSnakeLinesTopBottom.compute(side, (k, v) -> 0));
        nbSnakeLinesVerticalBetween.keySet().forEach(side -> nbSnakeLinesVerticalBetween.compute(side, (k, v) -> 0));
    }
}
