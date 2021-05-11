/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.sld.model;

import com.powsybl.commons.PowsyblException;
import com.powsybl.sld.layout.LayoutParameters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.powsybl.sld.model.Block.Extremity.*;
import static com.powsybl.sld.model.Block.Type.*;
import static com.powsybl.sld.model.Cell.CellType.*;
import static com.powsybl.sld.model.Coord.Dimension.*;
import static com.powsybl.sld.model.InternCell.Shape.*;
import static com.powsybl.sld.model.Node.NodeType.*;
import static com.powsybl.sld.model.Node.NodeType.SHUNT;
import static com.powsybl.sld.model.Orientation.*;
import static com.powsybl.sld.model.Position.Dimension.*;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class LegPrimaryBlock extends AbstractPrimaryBlock implements LegBlock {

    private final List<LegPrimaryBlock> stackableBlocks = new ArrayList<>();

    public LegPrimaryBlock(List<Node> nodes, Cell cell) {
        super(LEGPRIMARY, nodes, cell);
        if (getExtremityNode(END).getType() == BUS) {
            super.reverseBlock();
        }
        if (!checkConsistency()) {
            throw new PowsyblException("LegPrimaryBlock not consistent");
        }
    }

    private boolean checkConsistency() {
        return nodes.size() == 3
                && nodes.get(0).getType() == BUS
                && nodes.get(1).getType() == SWITCH
                && (nodes.get(2).getType() == FICTITIOUS
                || nodes.get(2).getType() == SHUNT);
    }

    public BusNode getBusNode() {
        return (BusNode) getExtremityNode(START);
    }

    @Override
    public List<BusNode> getBusNodes() {
        return Collections.singletonList(getBusNode());
    }

    private SwitchNode getSwNode() {
        return (SwitchNode) nodes.get(1);
    }

    public void addStackableBlock(LegPrimaryBlock block) {
        stackableBlocks.add(block);
    }

    public List<LegPrimaryBlock> getStackableBlocks() {
        return new ArrayList<>(stackableBlocks);
    }

    @Override
    public void reverseBlock() {
        // Will never happen.
    }

    @Override
    public void sizing() {
        if (getOrientation().isHorizontal()) {
            getPosition().setSpan(H, 0);
            getPosition().setSpan(V, 0);
        } else {
            getPosition().setSpan(H, 2);
            getPosition().setSpan(V, 0);
        }
    }

    @Override
    public double calculateHeight(Set<Node> encounteredNodes, LayoutParameters layoutParameters) {
        // LegPrimaryBlock has a 0 vertical span (see sizing above) and its height should not be included in external
        // cell height. Indeed, its height is fixed and corresponds to the layoutParameters.getStackSize().
        return 0.;
    }

    @Override
    public void coordHorizontalCase(LayoutParameters layoutParam) {
        getSwNode().setX(getCoord().get(X) + getCoord().getSpan(X) / 2);
        getSwNode().setY(getBusNode().getY(), false);
        getLegNode().setY(getBusNode().getY(), false);
    }

    @Override
    public void coordVerticalCase(LayoutParameters layoutParam) {
        getSwNode().setX(getCoord().get(X));
        getSwNode().setY(getBusNode().getY(), false);

        getLegNode().setX(getCoord().get(X));
        if (getCell().getType() == INTERN && ((InternCell) getCell()).checkisShape(UNILEG)) {
            InternCell cell = (InternCell) getCell();
            if (getOrientation() == UP) {
                getLegNode().setY(layoutParam.getInitialYBus() - layoutParam.getInternCellHeight());
            } else {
                getLegNode().setY(layoutParam.getInitialYBus() + layoutParam.getInternCellHeight()
                        + (cell.getGraph().getMaxVerticalBusPosition() - 1) * layoutParam.getVerticalSpaceBus());
            }
        }
    }

}
