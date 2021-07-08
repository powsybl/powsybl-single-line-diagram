/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.svg;

import com.powsybl.sld.model.Node;

import java.util.List;

/**
 * @author Giovanni Ferrari <giovanni.ferrari at techrain.eu>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public interface DiagramLabelProvider {

    class NodeLabel {
        private final String label;
        private final LabelPosition position;

        public NodeLabel(String label, LabelPosition labelPosition) {
            this.label = label;
            this.position = labelPosition;
        }

        public String getLabel() {
            return label;
        }

        public LabelPosition getPosition() {
            return position;
        }

    }

    class NodeDecorator {
        private final String type;
        private final LabelPosition position;

        /**
         * Creates a node decorator, with given type and position
         * @param type decorator type; corresponds to the type defined in components.xml file
         * @param labelPosition position of the decorator relatively to the center of the decorated node
         */
        public NodeDecorator(String type, LabelPosition labelPosition) {
            this.type = type;
            this.position = labelPosition;
        }

        public String getType() {
            return type;
        }

        public LabelPosition getPosition() {
            return position;
        }

    }

    public enum Direction {
        UP, DOWN;

    }

    InitialValue getInitialValue(Node node);

    List<NodeLabel> getNodeLabels(Node node);

    List<NodeDecorator> getNodeDecorators(Node node);
}
