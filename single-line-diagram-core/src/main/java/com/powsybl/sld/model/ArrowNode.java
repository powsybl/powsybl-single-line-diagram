/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.model;

import static com.powsybl.sld.library.ComponentTypeName.ARROW;

/**
 * @author Giovanni Ferrari <giovanni.ferrari at techrain.eu>
 * @author Franck Lecuyer <franck.lecuyer@rte-france.com>
 */
public class ArrowNode extends Node {

    public ArrowNode(VoltageLevelGraph graph) {
        super(NodeType.OTHER, "", "", "", ARROW, false, graph);
    }

}
