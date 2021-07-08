/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model;

import com.fasterxml.jackson.core.JsonGenerator;
import com.powsybl.commons.PowsyblException;

import java.io.IOException;
import java.util.Objects;

import static com.powsybl.sld.library.ComponentTypeName.NODE;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer@rte-france.com>
 */
public class SwitchNode extends Node {

    public enum SwitchKind {
        BREAKER,
        DISCONNECTOR,
        LOAD_BREAK_SWITCH;
    }

    private final SwitchKind kind;

    public SwitchNode(String id, String name, String componentType, boolean fictitious, VoltageLevelGraph graph, SwitchKind kind, boolean open) {
        super(NodeType.SWITCH, id, name, id, componentType, fictitious, graph);
        this.kind = Objects.requireNonNull(kind);
        setOpen(open);
    }

    public static SwitchNode createFictitious(VoltageLevelGraph graph, String id, boolean open) {
        return new SwitchNode(id, id, NODE, true, graph, SwitchKind.DISCONNECTOR, open);
    }

    public SwitchKind getKind() {
        return kind;
    }

    public Node getOtherAdjNode(Node adj) {
        // a switch node has 2 and only 2 adjacent nodes.
        if (getAdjacentNodes().size() != 2) {
            throw new PowsyblException("Error switch node not having exactly 2 adjacent nodes " + getId());
        }
        return getAdjacentNodes().get(getAdjacentNodes().get(0).equals(adj) ? 1 : 0);
    }

    @Override
    protected void writeJsonContent(JsonGenerator generator) throws IOException {
        super.writeJsonContent(generator);
        generator.writeStringField("kind", kind.name());
    }
}
