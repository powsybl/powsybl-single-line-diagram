/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.force.layout;

import java.io.PrintWriter;
import java.util.Locale;
import java.util.function.Function;

/**
 * @author Mathilde Grapin <mathilde.grapin at rte-france.com>
 */
public class Point {
    private static final double DEFAULT_MASS = 1.0;

    private Vector position;
    private Vector velocity;
    private Vector acceleration;
    private final double mass;

    public Point(double x, double y) {
        this(x, y, DEFAULT_MASS);
    }

    public Point(double x, double y, double mass) {
        this.position = new Vector(x, y);
        this.velocity = new Vector(0, 0);
        this.acceleration = new Vector(0, 0);
        this.mass = mass;
    }

    public void applyForce(Vector force) {
        acceleration = acceleration.add(force.divide(mass));
    }

    public Vector getPosition() {
        return position;
    }

    public Vector getVelocity() {
        return velocity;
    }

    public Vector getAcceleration() {
        return acceleration;
    }

    public void setPosition(Vector position) {
        this.position = position;
    }

    public void setVelocity(Vector velocity) {
        this.velocity = velocity;
    }

    public void setAcceleration(Vector acceleration) {
        this.acceleration = acceleration;
    }

    public double getEnergy() {
        double speed = velocity.magnitude();
        return 0.5 * mass * speed * speed;
    }

    public <V> void toSVG(PrintWriter printWriter, Canvas canvas, Function<V, String> tooltip, V vertex) {
        printWriter.println("<g>");

        printWriter.printf("<title>%s</title>%n", tooltip.apply(vertex));

        Vector screenPosition = canvas.toScreen(getPosition());
        printWriter.printf(Locale.US, "<circle cx=\"%.2f\" cy=\"%.2f\" r=\"10\"/>%n",
            screenPosition.getX(), screenPosition.getY()
        );

        printWriter.println("</g>");
    }
}
