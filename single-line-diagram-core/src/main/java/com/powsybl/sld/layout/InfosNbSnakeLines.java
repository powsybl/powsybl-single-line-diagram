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

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 * @author Slimane Amar <slimane.amar at rte-france.com>
 */
public final class InfosNbSnakeLines {

    private Map<BusCell.Direction, Integer> nbSnakeLinesTopBottom;
    private Map<String, Integer> nbSnakeLinesVerticalBetween;
    private Map<Side, Integer> nbSnakeLinesLeftRight;
    private final List<String> vlYSorted;
    private final int[] nbSnakeLinesHorizontalBetween;

    static InfosNbSnakeLines create(SubstationGraph substationGraph) {
        // used only for horizontal layout
        Map<BusCell.Direction, Integer> nbSnakeLinesTopBottom = EnumSet.allOf(BusCell.Direction.class).stream().collect(Collectors.toMap(Function.identity(), v -> 0));
        Map<String, Integer> nbSnakeLinesVerticalBetween = substationGraph.getNodeStream().collect(Collectors.toMap(g -> g.getVoltageLevelInfos().getId(), v -> 0));

        // used only for vertical layout
        Map<Side, Integer> nbSnakeLinesLeftRight = EnumSet.allOf(Side.class).stream().collect(Collectors.toMap(Function.identity(), v -> 0));
        List<String> vlYSorted = substationGraph.getNodeStream().sorted(Comparator.comparingDouble(VoltageLevelGraph::getY)).map(VoltageLevelGraph::getId).collect(Collectors.toList());
        int[] nbSnakeLinesHorizontalBetween = new int[(int) substationGraph.getNodeStream().count() + 1];

        return new InfosNbSnakeLines(nbSnakeLinesTopBottom, nbSnakeLinesVerticalBetween, nbSnakeLinesLeftRight, vlYSorted, nbSnakeLinesHorizontalBetween);
    }

    static InfosNbSnakeLines create(VoltageLevelGraph graph) {
        // used only for horizontal layout
        Map<BusCell.Direction, Integer> nbSnakeLinesTopBottom = EnumSet.allOf(BusCell.Direction.class).stream().collect(Collectors.toMap(Function.identity(), v -> 0));
        Map<String, Integer> nbSnakeLinesBetween = Stream.of(graph).collect(Collectors.toMap(g -> g.getVoltageLevelInfos().getId(), v -> 0));

        return new InfosNbSnakeLines(nbSnakeLinesTopBottom, nbSnakeLinesBetween);
    }

    private InfosNbSnakeLines(Map<BusCell.Direction, Integer> nbSnakeLinesTopBottom,
                              Map<String, Integer> nbSnakeLinesVerticalBetween,
                              Map<Side, Integer> nbSnakeLinesLeftRight,
                              List<String> vlYSorted,
                              int[] nbSnakeLinesHorizontalBetween) {
        this.nbSnakeLinesTopBottom = nbSnakeLinesTopBottom;
        this.nbSnakeLinesVerticalBetween = nbSnakeLinesVerticalBetween;
        this.nbSnakeLinesLeftRight = nbSnakeLinesLeftRight;
        this.vlYSorted = vlYSorted;
        this.nbSnakeLinesHorizontalBetween = nbSnakeLinesHorizontalBetween;
    }

    private InfosNbSnakeLines(Map<BusCell.Direction, Integer> nbSnakeLinesTopBottom,
                              Map<String, Integer> nbSnakeLinesVerticalBetween) {
        this(nbSnakeLinesTopBottom, nbSnakeLinesVerticalBetween,
                EnumSet.allOf(Side.class).stream().collect(Collectors.toMap(Function.identity(), v -> 0)),
                Collections.emptyList(), new int[0]);
    }

    public Map<BusCell.Direction, Integer> getNbSnakeLinesTopBottom() {
        return nbSnakeLinesTopBottom;
    }

    public Map<String, Integer> getNbSnakeLinesVerticalBetween() {
        return nbSnakeLinesVerticalBetween;
    }

    public Map<Side, Integer> getNbSnakeLinesLeftRight() {
        return nbSnakeLinesLeftRight;
    }

    private int getSnakeLinesIndex(BusCell.Direction direction, String vlId) {
        int vlIndex = vlYSorted.indexOf(vlId);
        return direction == BusCell.Direction.BOTTOM ? vlIndex + 1 : vlIndex;
    }

    public int getNbSnakeLinesHorizontalBetween(String vlId, BusCell.Direction direction) {
        return nbSnakeLinesHorizontalBetween[getSnakeLinesIndex(direction, vlId)];
    }

    public void setNbSnakeLinesHorizontalBetween(String vlId, BusCell.Direction direction, int nbSnakeLines) {
        nbSnakeLinesHorizontalBetween[getSnakeLinesIndex(direction, vlId)] = nbSnakeLines;
    }

    public int incrementAndGetNbSnakeLinesHorizontalBetween(String vlId, BusCell.Direction direction) {
        return ++nbSnakeLinesHorizontalBetween[getSnakeLinesIndex(direction, vlId)];
    }
}
