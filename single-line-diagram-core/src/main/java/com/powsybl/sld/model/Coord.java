/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model;

import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.powsybl.sld.model.Coord.Dimension.*;

/**
 * class use to store relatives coordinates of a nodeBus
 *
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class Coord {

    public class Segment {
        private double value;
        private double span;
        private double shift;

        Segment(double value, double span, double shift) {
            this.value = value;
            this.span = span;
            this.shift = shift;
        }

        public void copy(Segment segment) {
            this.value = segment.value;
            this.span = segment.span;
            this.shift = segment.shift;
        }

        public void replicateMe(Stream<Segment> segments) {
            segments.forEach(seg -> seg.copy(this));
        }

        public double getValue() {
            return value;
        }

        public void setValue(double value) {
            this.value = value;
        }

        public double getSpan() {
            return span;
        }

        public void setSpan(double span) {
            this.span = span;
        }

        public double getShift() {
            return shift;
        }

        public void setShift(double shift) {
            this.shift = shift;
        }

        public void mergeEnvelop(Stream<Segment> segStream) {
            List<Segment> segments = segStream.collect(Collectors.toList());
            setSpan(segments.stream().mapToDouble(Segment::getSpan).max().orElse(0));
            segments.forEach(seg -> seg.setValue(0));
        }

        void glue(Stream<Segment> segStream) {
            List<Segment> segments = segStream.collect(Collectors.toList());
            setSpan(segments.stream().mapToDouble(Segment::getSpan).sum());
            double cumulSpan = 0;
            for (Segment seg : segments) {
                seg.setValue(cumulSpan);
                cumulSpan += seg.getSpan();
            }
        }
    }

    public enum Dimension {
        X, Y
    }

    private Map<Dimension, Segment> dim2seg = new EnumMap<>(Dimension.class);

    public Coord(double x, double y) {
        dim2seg.put(X, new Segment(x, 0, 0));
        dim2seg.put(Y, new Segment(y, 0, 0));
    }

    public double get(Dimension dimension) {
        return dim2seg.get(dimension).getValue();
    }

    public double getSpan(Dimension dimension) {
        return dim2seg.get(dimension).getSpan();
    }

    public double getShift(Dimension dimension) {
        return dim2seg.get(dimension).getShift();
    }

    public Segment getSegment(Dimension dimension) {
        return dim2seg.get(dimension);
    }

    public void set(Dimension dimension, double value) {
        dim2seg.get(dimension).setValue(value);
    }

    public void setSpan(Dimension dimension, double span) {
        dim2seg.get(dimension).setSpan(span);
    }

    public void setShift(Dimension dimension, double shift) {
        dim2seg.get(dimension).setShift(shift);
    }

    @Override
    public String toString() {
        return "Coord(x=" + get(X) + ", y=" + get(Y) + ", xSpan=" + getSpan(X) + ", ySpan=" + getSpan(Y) + ")";
    }

    public void writeJsonContent(JsonGenerator generator) throws IOException {
        generator.writeStartObject();
        generator.writeNumberField("x", get(X));
        generator.writeNumberField("y", get(Y));
        generator.writeNumberField("xSpan", getSpan(X));
        generator.writeNumberField("ySpan", getSpan(Y));
        generator.writeEndObject();
    }
}

