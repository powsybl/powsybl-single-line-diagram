/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public class Point {

    private double x;
    private double y;

    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Point(Point point) {
        this.x = point.x;
        this.y = point.y;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void setCoordinates(Point point) {
        setX(point.x);
        setY(point.y);
    }

    public static List<Point> createPointsList(double... coordinates) {
        if (coordinates.length % 2 == 1) {
            throw new AssertionError("The number of coordinates given must be even");
        }
        List<Point> res = new ArrayList<>();
        for (int i = 0; i < coordinates.length / 2; i++) {
            res.add(new Point(coordinates[2 * i], coordinates[2 * i + 1]));
        }
        return res;
    }

    public Point getMiddlePoint(Point other) {
        return new Point((x + other.x) / 2, (y + other.y) / 2);
    }

    public void shift(Point shiftPoint) {
        x += shiftPoint.x;
        y += shiftPoint.y;
    }

    public Point getShiftedPoint(Point shift) {
        Point res = new Point(this);
        res.shift(shift);
        return res;
    }

    public void shiftX(double transX) {
        x += transX;
    }

    public void shiftY(double transY) {
        y += transY;
    }

    public void scale(double scale) {
        x *= scale;
        y *= scale;
    }

    public double distanceSquare(Point other) {
        double dx = other.x - x;
        double dy = other.y - y;
        return dx * dx + dy * dy;
    }

    public double distance(Point other) {
        return Math.sqrt(distanceSquare(other));
    }

    @Override
    public String toString() {
        return "{" + x + ',' + y + '}';
    }

}

