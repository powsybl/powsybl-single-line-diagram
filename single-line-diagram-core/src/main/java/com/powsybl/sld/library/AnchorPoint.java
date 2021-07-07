/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.library;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.powsybl.sld.model.Point;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.Objects;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@XmlJavaTypeAdapter(AnchorPointAdapter.class)
public class AnchorPoint extends Point {

    private final AnchorOrientation orientation;

    /**
     * Constructor
     *
     * @param x    abscissa
     * @param y    ordinate
     * @param orientation connection orientation
     */
    @JsonCreator
    public AnchorPoint(@JsonProperty("x") double x, @JsonProperty("y") double y,
                       @JsonProperty("orientation") AnchorOrientation orientation) {
        super(x, y);
        this.orientation = Objects.requireNonNull(orientation);
    }

    public AnchorOrientation getOrientation() {
        return orientation;
    }

    /**
     * Rotate the anchorPoints
     */
    public AnchorPoint rotate(Double rotationAngle) {
        if (rotationAngle == 90.) {
            switch (orientation) {
                case VERTICAL:
                    return new AnchorPoint(getY(), getX(), AnchorOrientation.HORIZONTAL);
                case HORIZONTAL:
                    return new AnchorPoint(getY(), getX(), AnchorOrientation.VERTICAL);
                case NONE:
                    return this;
                default:
                    throw new AssertionError("Unknown anchor orientation " + orientation);
            }
        } else if (rotationAngle == 180.) {
            return new AnchorPoint(-getX(), -getY(), orientation);
        } else {
            return this;
        }
    }

    public AnchorPoint rotate() {
        return rotate(90.);
    }

    @Override
    public String toString() {
        return "AnchorPoint(x=" + getX() + ", y=" + getY() + ", orientation=" + orientation + ")";
    }
}
