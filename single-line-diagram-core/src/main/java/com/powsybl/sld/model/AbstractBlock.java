/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model;

import com.fasterxml.jackson.core.JsonGenerator;
import com.powsybl.sld.layout.LayoutParameters;

import java.io.IOException;
import java.util.*;

import static com.powsybl.sld.model.Block.Extremity.END;
import static com.powsybl.sld.model.Block.Extremity.START;
import static com.powsybl.sld.model.Cell.CellType.INTERN;
import static com.powsybl.sld.model.Coord.Dimension.X;
import static com.powsybl.sld.model.Coord.Dimension.Y;
import static com.powsybl.sld.model.InternCell.Shape.FLAT;
import static com.powsybl.sld.model.Position.Dimension.H;
import static com.powsybl.sld.model.Position.Dimension.V;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public abstract class AbstractBlock implements Block {

    protected final Type type;

    private Map<Extremity, Integer> cardinality;

    private Block parentBlock;

    private Cell cell;

    private Position position;

    private Coord coord;

    /**
     * Constructor for primary layout.block with the list of nodes corresponding to the
     * layout.block
     */
    AbstractBlock(Type type) {
        cardinality = new EnumMap<>(Extremity.class);
        cardinality.put(START, 0);
        cardinality.put(END, 0);
        this.type = Objects.requireNonNull(type);
        position = new Position(-1, -1);
        coord = new Coord(-1, -1);
    }

    @Override
    public Node getStartingNode() {
        return getExtremityNode(START);
    }

    @Override
    public Node getEndingNode() {
        return getExtremityNode(END);
    }

    @Override
    public Optional<Extremity> getExtremity(Node node) {
        if (node.equals(getExtremityNode(START))) {
            return Optional.of(START);
        }
        if (node.equals(getExtremityNode(END))) {
            return Optional.of(END);
        }
        return Optional.empty();
    }

    @Override
    public int getCardinality(Node node) {
        Optional<Extremity> extremity = getExtremity(node);
        return extremity.map(this::getCardinality).orElse(0);
    }

    @Override
    public int getCardinality(Extremity extremity) {
        return cardinality.get(extremity);
    }

    @Override
    public void setCardinality(Extremity extremity, int i) {
        cardinality.put(extremity, i);
    }

    public Block getParentBlock() {
        return parentBlock;
    }

    @Override
    public void setParentBlock(Block parentBlock) {
        this.parentBlock = parentBlock;
    }

    @Override
    public Cell getCell() {
        return cell;
    }

    @Override
    public void setCell(Cell cell) {
        this.cell = cell;
        if (cell == null) {
            return;
        }
        if (cell.getType() == Cell.CellType.SHUNT) {
            setOrientation(Orientation.RIGHT);
        }
    }

    @Override
    public Position getPosition() {
        return position;
    }

    @Override
    public void setOrientation(Orientation orientation) {
        getPosition().setOrientation(orientation);
    }

    @Override
    public void setOrientation(Orientation orientation, boolean recursively) {
        setOrientation(orientation);
    }

    @Override
    public Orientation getOrientation() {
        return getPosition().getOrientation();
    }

    @Override
    public Coord getCoord() {
        return coord;
    }

    @Override
    public void calculateCoord(LayoutParameters layoutParam) {
        if (getPosition().getOrientation().isVertical()) {
            coordVerticalCase(layoutParam);
        } else {
            coordHorizontalCase(layoutParam);
        }
    }

    double hToX(LayoutParameters layoutParameters, int h) {
        return layoutParameters.getInitialXBus() + layoutParameters.getCellWidth() * h / 2;
    }

    @Override
    public void calculateRootCoord(LayoutParameters layoutParam) {

        double spanX = position.getSpan(H) / 2. * layoutParam.getCellWidth();
        coord.setSpan(X, spanX);
        coord.set(X, hToX(layoutParam, position.get(H)) + spanX / 2);

        double spanY = getRootSpanYCoord(layoutParam);
        coord.setSpan(Y, spanY);
        coord.set(Y, getRootYCoord(spanY, layoutParam));

        calculateCoord(layoutParam);
    }

    private double getRootSpanYCoord(LayoutParameters layoutParam) {
        double ySpan;
        if (cell.getType() == INTERN) {
            ySpan = position.getSpan(V) / 2. * layoutParam.getInternCellHeight();
        } else {
            // The Y span of root block does not consider the space needed for the FeederPrimaryBlock
            // nor the one needed for the LegPrimaryBlock.
            if (layoutParam.isAdaptCellHeightToContent()) {
                // In this case, corresponds to the previously calculated maximum height of all extern cells having the same direction
                ySpan = getGraph().getMaxCalculatedCellHeight(((BusCell) cell).getDirection());
            } else {
                ySpan = layoutParam.getExternCellHeight() - getFeederSpan(layoutParam);
            }
        }
        return ySpan;
    }

    public static double getFeederSpan(LayoutParameters layoutParam) {
        // The space needed between the feeder and the node connected to it corresponds to the space for feeder arrows
        // + half the height of the feeder component + half the height of that node component
        return layoutParam.getMinSpaceForFeederArrows() + layoutParam.getMaxComponentHeight();
    }

    private double getRootYCoord(double spanY, LayoutParameters layoutParam) {
        double dyToBus = 0;
        if (cell.getType() == INTERN) {
            if (((InternCell) cell).getShape().checkIsNotShape(FLAT)) {
                dyToBus = spanY / 2 + layoutParam.getInternCellHeight() * (1 + position.get(V)) / 2.;
            }
        } else {
            dyToBus = spanY / 2 + layoutParam.getStackHeight();
        }
        switch (((BusCell) cell).getDirection()) {
            case BOTTOM:
                return layoutParam.getInitialYBus()
                    + (cell.getGraph().getMaxVerticalBusPosition() - 1) * layoutParam.getVerticalSpaceBus()
                    + dyToBus;
            case TOP:
                return layoutParam.getInitialYBus() - dyToBus;
            case MIDDLE:
                return layoutParam.getInitialYBus()
                    + (getPosition().get(V) - 1) * layoutParam.getVerticalSpaceBus();
            default:
                return 0;
        }
    }

    @Override
    public double calculateRootHeight(LayoutParameters layoutParam) {
        Set<Node> encounteredNodes = new HashSet<>();
        return calculateHeight(encounteredNodes, layoutParam);
    }

    @Override
    public Block.Type getType() {
        return this.type;
    }

    protected abstract void writeJsonContent(JsonGenerator generator) throws IOException;

    @Override
    public void writeJson(JsonGenerator generator) throws IOException {
        generator.writeStartObject();
        generator.writeStringField("type", type.name());
        generator.writeArrayFieldStart("cardinalities");
        for (Map.Entry<Extremity, Integer> ex : cardinality.entrySet()) {
            generator.writeStartObject();
            generator.writeObjectField(ex.getKey().name(), ex.getValue());
            generator.writeEndObject();
        }
        generator.writeEndArray();
        generator.writeFieldName("position");
        position.writeJsonContent(generator, getGraph().isGenerateCoordsInJson());

        if (getGraph().isGenerateCoordsInJson()) {
            generator.writeFieldName("coord");
            coord.writeJsonContent(generator);
        }
        writeJsonContent(generator);
        generator.writeEndObject();
    }

}
