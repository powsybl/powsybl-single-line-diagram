/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.force.layout;

import java.util.Collection;

/**
 * @author Mathilde Grapin <mathilde.grapin at rte-france.com>
 */
public final class BoundingBox {

    private final double left;
    private final double bottom;
    private final double right;
    private final double top;

    private BoundingBox(double left, double top, double right, double bottom) {
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
        if (left > right || bottom < top) {
            throw new IllegalStateException("Bounding box with negative width or height");
        }
    }

    public static BoundingBox computeBoundingBox(Collection<Point> points) {
        double left = points.stream().mapToDouble(p -> p.getPosition().getX()).min().orElse(-2);
        double top = points.stream().mapToDouble(p -> p.getPosition().getY()).min().orElse(-2);
        double right = points.stream().mapToDouble(p -> p.getPosition().getX()).max().orElse(2);
        double bottom = points.stream().mapToDouble(p -> p.getPosition().getY()).max().orElse(2);
        return new BoundingBox(left, top, right, bottom);
    }

    public double getHeight() {
        return bottom - top;
    }

    public double getWidth() {
        return right - left;
    }

    public double getLeft() {
        return left;
    }

    public double getTop() {
        return top;
    }
}
