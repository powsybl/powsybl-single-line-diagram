/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.sld.model;

import com.powsybl.commons.PowsyblException;
import com.powsybl.sld.layout.LayoutParameters;
import com.powsybl.sld.layout.PositionVoltageLevelLayout;

import java.util.List;
import java.util.Set;

import static com.powsybl.sld.model.Block.Extremity.START;
import static com.powsybl.sld.model.Block.Type.FEEDERPRIMARY;
import static com.powsybl.sld.model.Coord.Dimension.X;
import static com.powsybl.sld.model.Node.NodeType.*;
import static com.powsybl.sld.model.Position.Dimension.H;
import static com.powsybl.sld.model.Position.Dimension.V;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public class FeederPrimaryBlock extends AbstractPrimaryBlock {

    public FeederPrimaryBlock(List<Node> nodes, Cell cell) {
        super(FEEDERPRIMARY, nodes, cell);
        if (getExtremityNode(START).getType() == FEEDER) {
            super.reverseBlock();
        }
        if (!checkConsistency()) {
            throw new PowsyblException("FeederPrimaryBlock not consistent");
        }
    }

    private boolean checkConsistency() {
        return nodes.size() == 2 && nodes.get(1).getType() == FEEDER
            && (nodes.get(0).getType() == FICTITIOUS || nodes.get(0).getType() == SHUNT);
    }

    private FeederNode getFeederNode() {
        return (FeederNode) nodes.get(1);
    }

    private Node getConnectedNode() {
        return nodes.get(0);
    }

    @Override
    public void reverseBlock() {
        // Will never happen.
    }

    @Override
    public void sizing() {
        // Orientation is always vertical (no horizontal feeder so far)
        // The span is equal to 0 as the height is constant
        getPosition().setSpan(H, 2);
        getPosition().setSpan(V, 0);
    }

    @Override
    public int getOrder() {
        return getFeederNode().getOrder();
    }

    @Override
    public double calculateHeight(Set<Node> encounteredNodes, LayoutParameters layoutParameters) {
        // FeederPrimaryBlock has a 0 vertical span (see sizing above) and its height should not be included in external
        // cell height. Indeed, its height is fixed and corresponds to the layoutParameters.getMinSpaceForFeederArrows().
        return 0.;
    }

    @Override
    public void coordHorizontalCase(LayoutParameters layoutParam) {
        // Will never happen
    }

    @Override
    public void coordVerticalCase(LayoutParameters layoutParam) {
        double yFeeder = getConnectedNode().getY() + getOrientation().progressionSign() * PositionVoltageLevelLayout.getFeederSpan(layoutParam);
        getFeederNode().setX(getCoord().get(X));
        getFeederNode().setY(yFeeder, false);
    }

}
