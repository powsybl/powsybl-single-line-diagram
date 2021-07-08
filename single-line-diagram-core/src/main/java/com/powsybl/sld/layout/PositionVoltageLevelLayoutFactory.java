/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout;

import com.powsybl.sld.layout.positionfromextension.PositionFromExtension;
import com.powsybl.sld.model.VoltageLevelGraph;

import java.util.Objects;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class PositionVoltageLevelLayoutFactory implements VoltageLevelLayoutFactory {

    private final PositionFinder positionFinder;

    private boolean feederStacked = true;

    private boolean removeUnnecessaryFictitiousNodes = true;

    private boolean substituteSingularFictitiousByFeederNode = true;

    private boolean exceptionIfPatternNotHandled = false;

    private boolean handleShunts = false;

    public PositionVoltageLevelLayoutFactory() {
        this(new PositionFromExtension());
    }

    public PositionVoltageLevelLayoutFactory(PositionFinder positionFinder) {
        this.positionFinder = Objects.requireNonNull(positionFinder);
    }

    public boolean isFeederStacked() {
        return feederStacked;
    }

    public PositionVoltageLevelLayoutFactory setFeederStacked(boolean feederStacked) {
        this.feederStacked = feederStacked;
        return this;
    }

    public boolean isExceptionIfPatternNotHandled() {
        return exceptionIfPatternNotHandled;
    }

    public PositionVoltageLevelLayoutFactory setExceptionIfPatternNotHandled(boolean exceptionIfPatternNotHandled) {
        this.exceptionIfPatternNotHandled = exceptionIfPatternNotHandled;
        return this;
    }

    public boolean isRemoveUnnecessaryFictitiousNodes() {
        return removeUnnecessaryFictitiousNodes;
    }

    public PositionVoltageLevelLayoutFactory setRemoveUnnecessaryFictitiousNodes(boolean removeUnnecessaryFictitiousNodes) {
        this.removeUnnecessaryFictitiousNodes = removeUnnecessaryFictitiousNodes;
        return this;
    }

    public boolean isSubstituteSingularFictitiousByFeederNode() {
        return substituteSingularFictitiousByFeederNode;
    }

    public PositionVoltageLevelLayoutFactory setSubstituteSingularFictitiousByFeederNode(boolean substituteSingularFictitiousByFeederNode) {
        this.substituteSingularFictitiousByFeederNode = substituteSingularFictitiousByFeederNode;
        return this;
    }

    public boolean isHandleShunts() {
        return handleShunts;
    }

    public PositionVoltageLevelLayoutFactory setHandleShunts(boolean handleShunts) {
        this.handleShunts = handleShunts;
        return this;
    }

    @Override
    public VoltageLevelLayout create(VoltageLevelGraph graph) {
        // detect cells
        new ImplicitCellDetector(removeUnnecessaryFictitiousNodes, substituteSingularFictitiousByFeederNode, exceptionIfPatternNotHandled)
                .detectCells(graph);

        // build blocks from cells
        new BlockOrganizer(positionFinder, feederStacked, exceptionIfPatternNotHandled, handleShunts).organize(graph);

        return new PositionVoltageLevelLayout(graph);
    }
}
