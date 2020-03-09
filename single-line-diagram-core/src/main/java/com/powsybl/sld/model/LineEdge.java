/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public class LineEdge extends Edge {

    private final String lineId;
    private List<Point> points = new ArrayList<>();

    public LineEdge(String lineId, Node node1, Node node2) {
        super(node1, node2);
        this.lineId = Objects.requireNonNull(lineId);
    }

    public LineEdge(LineEdge other) {
        super(other);
        lineId = other.lineId;
        points = new ArrayList<>(other.points);
    }

    public String getLineId() {
        return lineId;
    }

    public void addPoint(double x, double y) {
        points.add(new Point(x, y));
    }

    public void setPoints(List<Point> points) {
        this.points = Objects.requireNonNull(points);
    }

    public List<Point> getPoints() {
        return points;
    }

    public static class Point {

        private double x;
        private double y;

        public Point(double x, double y) {
            this.x = x;
            this.y = y;
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }

        public void setX(double x) {
            this.x = x;
        }

        public void setY(double y) {
            this.y = y;
        }
    }
}
