/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model;

import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public class LineEdge extends AbstractBranchEdge {

    private final String lineId;
    private List<Point> points = new ArrayList<>();

    public LineEdge(String lineId, Node node1, Node node2) {
        super(node1, node2);
        this.lineId = Objects.requireNonNull(lineId);
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

    @Override
    public List<Double> getSnakeLine() {
        return getPoints()
                .stream()
                .flatMap(point -> Stream.of(point.getX(), point.getY()))
                .collect(Collectors.toList());
    }

    @Override
    public void setSnakeLine(List<Double> snakeLine) {
        Objects.requireNonNull(snakeLine);
        points = new ArrayList<>();
        for (int i = 0; i < snakeLine.size(); i += 2) {
            addPoint(snakeLine.get(i), snakeLine.get(i + 1));
        }
    }

    public class Point {

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

        void writeJson(JsonGenerator generator) throws IOException {
            generator.writeStartObject();
            generator.writeNumberField("x", x);
            generator.writeNumberField("y", y);
            generator.writeEndObject();
        }
    }

    @Override
    void writeJson(JsonGenerator generator) throws IOException {
        writeJson(generator, false);
    }

    void writeJson(JsonGenerator generator, boolean generateCoordsInJson) throws IOException {
        generator.writeStartObject();
        generator.writeStringField("lineId", lineId);
        generator.writeArrayFieldStart("nodes");
        super.writeJson(generator);
        generator.writeEndArray();
        if (generateCoordsInJson) {
            generator.writeArrayFieldStart("points");
            for (Point point : points) {
                point.writeJson(generator);
            }
            generator.writeEndArray();
        }
        generator.writeEndObject();
    }
}
