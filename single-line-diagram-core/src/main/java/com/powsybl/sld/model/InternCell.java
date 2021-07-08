/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model;

import com.powsybl.commons.PowsyblException;
import com.powsybl.sld.layout.LayoutParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static com.powsybl.sld.model.Position.Dimension.*;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class InternCell extends AbstractBusCell {

    public enum Shape {
        UNDEFINED, UNILEG, FLAT, MAYBEFLAT, VERTICAL, CROSSOVER, UNHANDLEDPATTERN;

        public boolean checkIsShape(Shape... shapes) {
            for (Shape s : shapes) {
                if (this == s) {
                    return true;
                }
            }
            return false;
        }

        public boolean checkIsNotShape(Shape... shapes) {
            for (Shape s : shapes) {
                if (this == s) {
                    return false;
                }
            }
            return true;
        }
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(InternCell.class);

    private static final Side BODY_SIDE = Side.LEFT;

    private Shape shape;
    private Map<Side, LegBlock> legs;
    private Block body;
    private boolean exceptionIfPatternNotHandled;

    public InternCell(VoltageLevelGraph graph, boolean exceptionIfPatternNotHandled) {
        super(graph, CellType.INTERN);
        legs = new EnumMap<>(Side.class);
        setDirection(Direction.TOP);
        shape = Shape.UNDEFINED;
        this.exceptionIfPatternNotHandled = exceptionIfPatternNotHandled;
    }

    public void organizeBlocks() {
        List<LegBlock> candidateLegs = searchLegs();
        if (getRootBlock().getType() == Block.Type.SERIAL && candidateLegs.size() == 2) {
            SerialBlock serialRootBlock = (SerialBlock) getRootBlock();
            assignLeg(serialRootBlock, candidateLegs.get(0));
            assignLeg(serialRootBlock, candidateLegs.get(1));
            body = serialRootBlock.extractBody(new ArrayList<>(legs.values()));
            body.setOrientation(Orientation.RIGHT);
        } else {
            if (candidateLegs.size() != 1) {
                if (exceptionIfPatternNotHandled) {
                    throw new PowsyblException("InternCell pattern not recognized");
                } else {
                    shape = Shape.UNHANDLEDPATTERN;
                    LOGGER.error("InternCell pattern not handled");
                    LegBlock leg = candidateLegs.get(0);
                    legs.put(Side.UNDEFINED, candidateLegs.get(0));
                    leg.setOrientation(Orientation.UP);
                }
            }
        }
        if (candidateLegs.size() == 1) {
            shape = Shape.UNILEG;
            LegBlock leg = candidateLegs.get(0);
            legs.put(Side.UNDEFINED, leg);
            leg.setOrientation(Orientation.UP);
        } else if (candidateLegs.size() == 2 && getBusNodes().size() == 2) {
            shape = Shape.MAYBEFLAT;
        }
    }

    public void setFlat() {
        shape = Shape.FLAT;
        setDirection(Direction.MIDDLE);
        legs.values().forEach(l -> l.setOrientation(Orientation.RIGHT));
    }

    private void assignLeg(SerialBlock sb, LegBlock candidateLeg) {
        Optional<Block.Extremity> extremity = sb.whichExtremity(candidateLeg);
        if (extremity.isPresent()) {
            legs.put(extremityToSide(extremity.get()), candidateLeg);
            candidateLeg.setOrientation(Orientation.UP);
        } else {
            throw new PowsyblException("Unable to identify legs of internCell");
        }
    }

    private Side extremityToSide(Block.Extremity extremity) {
        if (extremity == Block.Extremity.START) {
            return Side.LEFT;
        }
        if (extremity == Block.Extremity.END) {
            return Side.RIGHT;
        }
        return Side.UNDEFINED;
    }

    private List<LegBlock> searchLegs() {
        List<LegBlock> candidateLegs = new ArrayList<>();
        List<LegPrimaryBlock> plbCopy = new ArrayList<>(getLegPrimaryBlocks());
        while (!plbCopy.isEmpty()) {
            LegPrimaryBlock lpb = plbCopy.get(0);
            Block parentBlock = lpb.getParentBlock();
            if (parentBlock instanceof LegParralelBlock) {
                candidateLegs.add((LegBlock) parentBlock);
                plbCopy.removeAll(((LegParralelBlock) parentBlock).subBlocks);
            } else {
                candidateLegs.add(lpb);
                plbCopy.remove(lpb);
            }
        }
        return candidateLegs;
    }

    public void identifyIfFlat() {
        List<BusNode> buses = getBusNodes();
        if (shape != Shape.MAYBEFLAT) {
            return;
        }
        if (Math.abs(buses.get(1).getSectionIndex() - buses.get(0).getSectionIndex()) == 1 && buses.get(1).getBusbarIndex() == buses.get(0).getBusbarIndex()) {
            setFlat();
            getRootBlock().setOrientation(Orientation.RIGHT);
        } else {
            shape = Shape.CROSSOVER;
        }
    }

    public boolean checkisShape(Shape... shape) {
        return this.shape.checkIsShape(shape);
    }

    public boolean checkIsNotShape(Shape... shapes) {
        return shape.checkIsNotShape(shapes);
    }

    public void reverseCell() {
        body.reverseBlock();
        if (legs.get(Side.LEFT) != null) {
            LegBlock tmp = legs.get(Side.LEFT);
            legs.put(Side.LEFT, legs.get(Side.RIGHT));
            legs.put(Side.RIGHT, tmp);
        }
    }

    public int getSideHPos(Side side) {
        return getSideToLeg(side).getPosition().get(H);
    }

    @Override
    public void blockSizing() {
        legs.values().forEach(Block::sizing);
        if (shape.checkIsNotShape(Shape.UNILEG, Shape.UNDEFINED, Shape.UNHANDLEDPATTERN)) {
            body.sizing();
        }
    }

    @Override
    public int newHPosition(int hPosition) {
        int h = hPosition;
        if (shape == Shape.UNILEG) {
            legs.get(Side.UNDEFINED).getPosition().set(H, h);
            h += legs.get(Side.UNDEFINED).getPosition().getSpan(H);
        } else {
            legs.get(Side.LEFT).getPosition().set(H, h);
            h += legs.get(Side.LEFT).getPosition().getSpan(H);
            Position pos = body.getPosition();
            if (shape == Shape.FLAT) {
                pos.set(H, h);
                pos.set(V, legs.get(Side.LEFT).getBusNodes().get(0).getBusbarIndex());
            } else {
                h -= 2;
                pos.set(H, h);
                pos.set(V, 0);
            }
            h += pos.getSpan(H);
            legs.get(Side.RIGHT).getPosition().set(H, h);
            h += legs.get(Side.RIGHT).getPosition().getSpan(H);
        }
        return h;
    }

    public int newHPosition(int hPosition, Side side) {
        int h = hPosition;
        if (side == Side.LEFT) {
            legs.get(Side.LEFT).getPosition().set(H, h);
            h += legs.get(Side.LEFT).getPosition().getSpan(H);
        }
        if (side == BODY_SIDE) {
            h -= 2;
            Position pos = body.getPosition();
            pos.set(H, h);
            pos.set(V, 1);
            h += body.getPosition().getSpan(H);
        }
        if (side == Side.RIGHT) {
            legs.get(Side.RIGHT).getPosition().set(H, h);
            h += legs.get(Side.RIGHT).getPosition().getSpan(H);
        }
        return h;
    }

    @Override
    public void setDirection(Direction direction) {
        super.setDirection(direction);
        for (LegBlock leg : legs.values()) {
            leg.setOrientation(direction.toOrientation());
        }
    }

    @Override
    public void calculateCoord(LayoutParameters layoutParam) {
        if (shape.checkIsNotShape(Shape.UNILEG, Shape.UNDEFINED, Shape.UNHANDLEDPATTERN)) {
            body.calculateRootCoord(layoutParam);
        }
        legs.values().forEach(lb -> lb.calculateRootCoord(layoutParam));
    }

    public LegBlock getSideToLeg(Side side) {
        return legs.get(side);
    }

    public List<BusNode> getSideBusNodes(Side side) {
        return legs.get(side).getBusNodes();
    }

    public Block getBodyBlock() {
        return body;
    }

    public Shape getShape() {
        return shape;
    }

    public void setShape(Shape shape) {
        this.shape = shape;
    }
}
