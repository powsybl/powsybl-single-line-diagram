/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.force.layout;

/**
 * @author Mathilde Grapin <mathilde.grapin at rte-france.com>
 */
public class Canvas {
    private final double width;
    private final double height;
    private final BoundingBox boundingBox;
    private final double margin;
    private final double scale;

    public Canvas(BoundingBox boundingBox, double height, double margin) {
        this.boundingBox = boundingBox;
        this.height = height;
        this.scale = (height - 2 * margin) / boundingBox.getHeight(); // scale has to be computed and used without margins
        this.width = boundingBox.getWidth() * scale + 2 * margin;
        this.margin = margin;
    }

    public Vector toScreen(Vector position) {
        double screenX = (position.getX() - boundingBox.getLeft()) * scale + margin;
        double screenY = (position.getY() - boundingBox.getTop()) * scale + margin;
        return new Vector(screenX, screenY);
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }
}
