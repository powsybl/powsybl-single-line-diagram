/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout;

import com.powsybl.sld.model.BusCell;
import com.powsybl.sld.model.Side;
import com.powsybl.sld.model.SubstationGraph;
import com.powsybl.sld.model.VoltageLevelGraph;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 * @author Slimane Amar <slimane.amar at rte-france.com>
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public class InfosNbSnakeLinesVertical {

    private final Map<Side, Integer> nbSnakeLinesLeftRight;
    private final List<String> vlYSorted;
    private final int[] nbSnakeLinesHorizontalBetween;

    static InfosNbSnakeLinesVertical create(SubstationGraph substationGraph) {
        Map<Side, Integer> nbSnakeLinesLeftRight = EnumSet.allOf(Side.class).stream().collect(Collectors.toMap(Function.identity(), v -> 0));
        List<String> vlYSorted = substationGraph.getNodeStream().map(VoltageLevelGraph::getId).collect(Collectors.toList());
        int[] nbSnakeLinesHorizontalBetween = new int[(int) substationGraph.getNodeStream().count() + 1];
        return new InfosNbSnakeLinesVertical(nbSnakeLinesLeftRight, vlYSorted, nbSnakeLinesHorizontalBetween);
    }

    protected InfosNbSnakeLinesVertical(Map<Side, Integer> nbSnakeLinesLeftRight,
                                      List<String> vlYSorted,
                                      int[] nbSnakeLinesHorizontalBetween) {
        this.nbSnakeLinesLeftRight = nbSnakeLinesLeftRight;
        this.vlYSorted = vlYSorted;
        this.nbSnakeLinesHorizontalBetween = nbSnakeLinesHorizontalBetween;
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

    public void setNbSnakeLinesTopBottom(String vlId, BusCell.Direction direction, int nbSnakeLines) {
        nbSnakeLinesHorizontalBetween[getSnakeLinesIndex(direction, vlId)] = nbSnakeLines;
    }

    public int incrementAndGetNbSnakeLinesTopBottom(String vlId, BusCell.Direction direction) {
        return ++nbSnakeLinesHorizontalBetween[getSnakeLinesIndex(direction, vlId)];
    }

    public void reset() {
        nbSnakeLinesLeftRight.keySet().forEach(side -> nbSnakeLinesLeftRight.compute(side, (k, v) -> 0));
        Arrays.fill(nbSnakeLinesHorizontalBetween, 0);
    }
}
