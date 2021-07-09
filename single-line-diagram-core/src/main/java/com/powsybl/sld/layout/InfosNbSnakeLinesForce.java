package com.powsybl.sld.layout;

import com.powsybl.sld.model.BusCell;
import com.powsybl.sld.model.SubstationGraph;
import com.powsybl.sld.model.VoltageLevelGraph;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public final class InfosNbSnakeLinesForce {

    private final Map<String, Integer> nbSnakeLinesLeft;
    private final List<String> vlYSorted;
    private final int[] nbSnakeLinesHorizontalBetween;
    private final Map<String, Map<BusCell.Direction, Integer>> nbSnakeLinesTopBottom;
    private final ForceSubstationLayoutFactory.CompactionType compactionType;

    private InfosNbSnakeLinesForce(Map<String, Integer> nbSnakeLinesLeft, List<String> vlYSorted,
                                   int[] nbSnakeLinesHorizontalBetween, Map<String, Map<BusCell.Direction, Integer>> nbSnakeLinesTopBottom,
                                   ForceSubstationLayoutFactory.CompactionType compactionType) {
        this.nbSnakeLinesLeft = nbSnakeLinesLeft;
        this.vlYSorted = vlYSorted;
        this.nbSnakeLinesTopBottom = nbSnakeLinesTopBottom;
        this.nbSnakeLinesHorizontalBetween = nbSnakeLinesHorizontalBetween;
        this.compactionType = compactionType;
    }

    static InfosNbSnakeLinesForce create(SubstationGraph substationGraph, ForceSubstationLayoutFactory.CompactionType compactionType) {
        List<String> vlYSorted = substationGraph.getNodeStream().sorted(Comparator.comparingDouble(VoltageLevelGraph::getY)).map(VoltageLevelGraph::getId).collect(Collectors.toList());
        Stream<String> vlXSorted = substationGraph.getNodeStream().sorted(Comparator.comparingDouble(VoltageLevelGraph::getX)).map(VoltageLevelGraph::getId);
        Map<String, Integer> nbSnakeLinesLeft = vlXSorted.collect(Collectors.toMap(Function.identity(), v -> 0));
        int[] nbSnakeLinesHorizontalBetween = new int[(int) substationGraph.getNodeStream().count() + 1];
        Map<String, Map<BusCell.Direction, Integer>> nbSnakeLinesTopBottom = compactionType != ForceSubstationLayoutFactory.CompactionType.VERTICAL
            ? new HashMap<>()
            : substationGraph.getNodeStream().map(VoltageLevelGraph::getId).collect(
                Collectors.toMap(Function.identity(), v0 -> EnumSet.allOf(BusCell.Direction.class).stream().collect(
                    Collectors.toMap(Function.identity(), v1 -> 0))));
        return new InfosNbSnakeLinesForce(nbSnakeLinesLeft, vlYSorted, nbSnakeLinesHorizontalBetween, nbSnakeLinesTopBottom, compactionType);
    }

    public int getNbSnakeLinesLeft(String vlId) {
        return nbSnakeLinesLeft.get(vlId);
    }

    public int incrementAndGetNbSnakeLinesLeft(String vlId) {
        return nbSnakeLinesLeft.compute(vlId, (k, v) -> v == null ? 0 : v + 1);
    }

    private int getSnakeLinesIndex(BusCell.Direction direction, String vlId) {
        int vlIndex = vlYSorted.indexOf(vlId);
        return direction == BusCell.Direction.BOTTOM ? vlIndex + 1 : vlIndex;
    }

    public int getNbSnakeLinesTopBottom(String vlId, BusCell.Direction direction) {
        if (compactionType != ForceSubstationLayoutFactory.CompactionType.VERTICAL) {
            return nbSnakeLinesHorizontalBetween[getSnakeLinesIndex(direction, vlId)];
        } else {
            return nbSnakeLinesTopBottom.get(vlId).get(direction);
        }
    }

    public int incrementAndGetNbSnakeLinesTopBottom(String vlId, BusCell.Direction direction) {
        if (compactionType != ForceSubstationLayoutFactory.CompactionType.VERTICAL) {
            return ++nbSnakeLinesHorizontalBetween[getSnakeLinesIndex(direction, vlId)];
        } else {
            return nbSnakeLinesTopBottom.get(vlId).compute(direction, (k, v) -> v + 1);
        }
    }

    public void reset() {
        nbSnakeLinesLeft.keySet().forEach(vl -> nbSnakeLinesLeft.compute(vl, (k, v) -> 0));
        nbSnakeLinesTopBottom.keySet().forEach(vl -> nbSnakeLinesTopBottom.put(vl,
            EnumSet.allOf(BusCell.Direction.class).stream().collect(Collectors.toMap(Function.identity(), v1 -> 0))));
        Arrays.fill(nbSnakeLinesHorizontalBetween, 0);
    }

    public List<String> getYSortedVls() {
        return vlYSorted;
    }
}
