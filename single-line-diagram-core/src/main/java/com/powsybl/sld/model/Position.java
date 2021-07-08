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

import static com.powsybl.sld.model.Position.Dimension.*;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class Position {

    public class Segment {
        private int value;
        private int span;
        private int shift;

        Segment(int value, int span, int shift) {
            this.value = value;
            this.span = span;
            this.shift = shift;
        }

        public void copy(Segment segment) {
            this.value = segment.value;
            this.span = segment.span;
            this.shift = segment.shift;
        }

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }

        public int getSpan() {
            return span;
        }

        public void setSpan(int span) {
            this.span = span;
        }

        public int getShift() {
            return shift;
        }

        public void setShift(int shift) {
            this.shift = shift;
        }

        public void mergeEnvelop(Stream<Segment> segStream) {
            List<Segment> segments = segStream.collect(Collectors.toList());
            setSpan(segments.stream().mapToInt(Segment::getSpan).max().orElse(0));
            segments.forEach(seg -> seg.setValue(0));
        }

        void glue(Stream<Segment> segStream) {
            List<Segment> segments = segStream.collect(Collectors.toList());
            setSpan(segments.stream().mapToInt(Segment::getSpan).sum());
            int cumulSpan = 0;
            for (Segment seg : segments) {
                seg.setValue(cumulSpan);
                cumulSpan += seg.getSpan();
            }
        }
    }

    public enum Dimension {
        H, V
    }

    private Map<Dimension, Segment> dim2Seg = new EnumMap<>(Dimension.class);

    private Orientation orientation;

    public Position(int h, int v, int hSpan, int vSpan, Orientation orientation) {
        dim2Seg.put(H, new Segment(h, hSpan, 0));
        dim2Seg.put(V, new Segment(v, vSpan, 0));
        this.orientation = orientation;
    }

    public Position(int h, int v) {
        this(h, v, 0, 0, null);
    }

    public Orientation getOrientation() {
        return orientation;
    }

    public void setOrientation(Orientation orientation) {
        this.orientation = orientation;
    }

    public int get(Dimension dimension) {
        return dim2Seg.get(dimension).getValue();
    }

    public int getSpan(Dimension dimension) {
        return dim2Seg.get(dimension).getSpan();
    }

    public Position set(Dimension dimension, int h) {
        dim2Seg.get(dimension).setValue(h);
        return this;
    }

    public Position setSpan(Dimension dimension, int h) {
        dim2Seg.get(dimension).setSpan(h);
        return this;
    }

    public Segment getSegment(Dimension dimension) {
        return dim2Seg.get(dimension);
    }

    @Override
    public String toString() {
        return "h=" + get(H) + " v=" + get(V) + " hSpan=" + getSpan(H) + " vSpan=" + getSpan(V) + ", " + orientation;
    }

    public void writeJsonContent(JsonGenerator generator, boolean writeOrientation) throws IOException {
        generator.writeStartObject();
        generator.writeNumberField("h", get(H));
        generator.writeNumberField("v", get(V));
        generator.writeNumberField("hSpan", getSpan(H));
        generator.writeNumberField("vSpan", getSpan(V));
        if (orientation != null && writeOrientation) {
            generator.writeStringField("orientation", orientation.name());
        }
        generator.writeEndObject();
    }
}
