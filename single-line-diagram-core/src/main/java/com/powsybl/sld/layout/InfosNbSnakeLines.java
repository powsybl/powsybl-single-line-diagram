/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout;

import com.powsybl.sld.model.BusCell;
import com.powsybl.sld.model.VoltageLevelGraph;
import com.powsybl.sld.model.Side;
import com.powsybl.sld.model.SubstationGraph;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 * @author Slimane Amar <slimane.amar at rte-france.com>
 */
public final class InfosNbSnakeLines {

    private Map<BusCell.Direction, Integer> nbSnakeLinesTopBottom;
    private Map<String, Integer> nbSnakeLinesBetween;
    private Map<Side, Integer> nbSnakeLinesLeftRight;
    private Map<String, Integer> nbSnakeLinesBottomVL;
    private Map<String, Integer> nbSnakeLinesTopVL;

    static InfosNbSnakeLines create(SubstationGraph substationGraph) {
        // used only for horizontal layout
        Map<BusCell.Direction, Integer> nbSnakeLinesTopBottom = EnumSet.allOf(BusCell.Direction.class).stream().collect(Collectors.toMap(Function.identity(), v -> 0));
        Map<String, Integer> nbSnakeLinesBetween = substationGraph.getNodes().stream().collect(Collectors.toMap(g -> g.getVoltageLevelInfos().getId(), v -> 0));

        // used only for vertical layout
        Map<Side, Integer> nbSnakeLinesLeftRight = EnumSet.allOf(Side.class).stream().collect(Collectors.toMap(Function.identity(), v -> 0));
        Map<String, Integer> nbSnakeLinesBottomVL = substationGraph.getNodes().stream().collect(Collectors.toMap(g -> g.getVoltageLevelInfos().getId(), v -> 0));
        Map<String, Integer> nbSnakeLinesTopVL = substationGraph.getNodes().stream().collect(Collectors.toMap(g -> g.getVoltageLevelInfos().getId(), v -> 0));

        return new InfosNbSnakeLines(nbSnakeLinesTopBottom, nbSnakeLinesBetween, nbSnakeLinesLeftRight, nbSnakeLinesBottomVL, nbSnakeLinesTopVL);
    }

    static InfosNbSnakeLines create(VoltageLevelGraph graph) {
        // used only for horizontal layout
        Map<BusCell.Direction, Integer> nbSnakeLinesTopBottom = EnumSet.allOf(BusCell.Direction.class).stream().collect(Collectors.toMap(Function.identity(), v -> 0));
        Map<String, Integer> nbSnakeLinesBetween = Stream.of(graph).collect(Collectors.toMap(g -> g.getVoltageLevelInfos().getId(), v -> 0));

        return new InfosNbSnakeLines(nbSnakeLinesTopBottom, nbSnakeLinesBetween);
    }

    private InfosNbSnakeLines(Map<BusCell.Direction, Integer> nbSnakeLinesTopBottom,
                              Map<String, Integer> nbSnakeLinesBetween,
                              Map<Side, Integer> nbSnakeLinesLeftRight,
                              Map<String, Integer> nbSnakeLinesBottomVL,
                              Map<String, Integer> nbSnakeLinesTopVL) {
        this.nbSnakeLinesTopBottom = nbSnakeLinesTopBottom;
        this.nbSnakeLinesBetween = nbSnakeLinesBetween;
        this.nbSnakeLinesLeftRight = nbSnakeLinesLeftRight;
        this.nbSnakeLinesBottomVL = nbSnakeLinesBottomVL;
        this.nbSnakeLinesTopVL = nbSnakeLinesTopVL;
    }

    private InfosNbSnakeLines(Map<BusCell.Direction, Integer> nbSnakeLinesTopBottom,
                              Map<String, Integer> nbSnakeLinesBetween) {
        this(nbSnakeLinesTopBottom, nbSnakeLinesBetween,
                EnumSet.allOf(Side.class).stream().collect(Collectors.toMap(Function.identity(), v -> 0)),
                Collections.emptyMap(), Collections.emptyMap());
    }

    public Map<BusCell.Direction, Integer> getNbSnakeLinesTopBottom() {
        return nbSnakeLinesTopBottom;
    }

    public Map<String, Integer> getNbSnakeLinesBetween() {
        return nbSnakeLinesBetween;
    }

    public Map<Side, Integer> getNbSnakeLinesLeftRight() {
        return nbSnakeLinesLeftRight;
    }

    public Map<String, Integer> getNbSnakeLinesBottomVL() {
        return nbSnakeLinesBottomVL;
    }

    public Map<String, Integer> getNbSnakeLinesTopVL() {
        return nbSnakeLinesTopVL;
    }
}
