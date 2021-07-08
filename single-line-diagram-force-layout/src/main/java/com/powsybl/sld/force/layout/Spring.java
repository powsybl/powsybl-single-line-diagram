/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.force.layout;

import java.io.PrintWriter;
import java.util.Locale;

/**
 * @author Mathilde Grapin <mathilde.grapin at rte-france.com>
 */
public class Spring {
    private static final double DEFAULT_LENGTH = 1.0;
    private static final double DEFAULT_STIFFNESS = 400.0;

    private final double length;
    private final double stiffness;
    private final Point source;
    private final Point target;

    public Spring(Point source, Point target, double length, double stiffness) {
        this.length = length;
        this.stiffness = stiffness;
        this.source = source;
        this.target = target;
    }

    public Spring(Point source, Point target, double length) {
        this(source, target, length, DEFAULT_STIFFNESS);
    }

    public Spring(Point source, Point target) {
        this(source, target, DEFAULT_LENGTH);
    }

    public Point getNode1() {
        return source;
    }

    public Point getNode2() {
        return target;
    }

    public double getLength() {
        return length;
    }

    public double getStiffness() {
        return stiffness;
    }

    public void toSVG(PrintWriter printWriter, Canvas canvas) {
        Vector screenPosition1 = canvas.toScreen(target.getPosition());
        Vector screenPosition2 = canvas.toScreen(source.getPosition());
        printWriter.printf(Locale.US, "<line x1=\"%.2f\" y1=\"%.2f\" x2=\"%.2f\" y2=\"%.2f\"/>%n",
            screenPosition1.getX(), screenPosition1.getY(), screenPosition2.getX(), screenPosition2.getY());
    }
}
