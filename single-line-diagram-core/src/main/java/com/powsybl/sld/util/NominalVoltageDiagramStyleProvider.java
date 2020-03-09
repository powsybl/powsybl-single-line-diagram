/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.util;

import static com.powsybl.sld.svg.DiagramStyles.LINE_STYLE_CLASS;
import static com.powsybl.sld.svg.DiagramStyles.WIRE_STYLE_CLASS;
import static com.powsybl.sld.svg.DiagramStyles.escapeClassName;
import static com.powsybl.sld.svg.DiagramStyles.escapeId;

import java.util.Optional;

import com.powsybl.sld.model.Edge;
import com.powsybl.sld.model.Feeder2WTNode;
import com.powsybl.sld.model.Fictitious3WTNode;
import com.powsybl.sld.model.LineEdge;
import com.powsybl.sld.model.Node;
import com.powsybl.sld.svg.DefaultDiagramStyleProvider;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class NominalVoltageDiagramStyleProvider extends DefaultDiagramStyleProvider {
    private static final String DEFAULT_COLOR = "rgb(171, 175, 40)";

    @Override
    public Optional<String> getColor(double nominalV, Node node) {
        String color;
        if (nominalV >= 300) {
            color = "rgb(255, 0, 0)";
        } else if (nominalV >= 170 && nominalV < 300) {
            color = "rgb(34, 139, 34)";
        } else if (nominalV >= 120 && nominalV < 170) {
            color = "rgb(1, 175, 175)";
        } else if (nominalV >= 70 && nominalV < 120) {
            color = "rgb(204, 85, 0)";
        } else if (nominalV >= 50 && nominalV < 70) {
            color = "rgb(160, 32, 240)";
        } else if (nominalV >= 30 && nominalV < 50) {
            color = "rgb(255, 130, 144)";
        } else {
            color = DEFAULT_COLOR;
        }
        return Optional.of(color);
    }

    @Override
    public Optional<String> getNodeStyle(Node node, boolean avoidSVGComponentsDuplication, boolean isShowInternalNodes) {
        Optional<String> defaultStyle = super.getNodeStyle(node, avoidSVGComponentsDuplication, isShowInternalNodes);

        String color = getColor(node.getGraph().getVoltageLevelNominalV(), null).orElse(DEFAULT_COLOR);
        if (node.getType() == Node.NodeType.SWITCH) {
            return defaultStyle;
        } else {
            return Optional.of(defaultStyle.orElse("") + " ." + escapeId(node.getId()) + " {stroke:" + color + ";}");
        }
    }

    @Override
    public String getIdWireStyle(Edge edge) {
        Node node1 = edge.getNode1();
        Node node2 = edge.getNode2();
        if ((node1 instanceof Fictitious3WTNode && node2 instanceof Feeder2WTNode) ||
                (node1 instanceof Feeder2WTNode && node2 instanceof Fictitious3WTNode)) {
            String vlId = node1 instanceof Feeder2WTNode
                    ? ((Feeder2WTNode) node1).getVIdOtherSide()
                    : ((Feeder2WTNode) node2).getVIdOtherSide();
            return WIRE_STYLE_CLASS + "_" + escapeClassName(vlId);
        } else {
            return WIRE_STYLE_CLASS + "_" + escapeClassName(edge.getNode1().getGraph().getVoltageLevelId());
        }
    }

    @Override
    public Optional<String> getWireStyle(Edge edge, String id, int index) {
        Node node1 = edge.getNode1();
        Node node2 = edge.getNode2();
        String vlId;
        double nominalV;

        if ((node1 instanceof Fictitious3WTNode && node2 instanceof Feeder2WTNode) ||
                (node1 instanceof Feeder2WTNode && node2 instanceof Fictitious3WTNode)) {
            vlId = node1 instanceof Feeder2WTNode
                    ? ((Feeder2WTNode) node1).getVIdOtherSide()
                    : ((Feeder2WTNode) node2).getVIdOtherSide();
            nominalV = node1 instanceof Feeder2WTNode
                    ? ((Feeder2WTNode) node1).getNominalVOtherSide()
                    : ((Feeder2WTNode) node2).getNominalVOtherSide();
        } else {
            vlId = node1.getGraph() != null ? node1.getGraph().getVoltageLevelId() : node2.getGraph().getVoltageLevelId();
            nominalV = node1.getGraph() != null ? node1.getGraph().getVoltageLevelNominalV() : node2.getGraph().getVoltageLevelNominalV();
        }

        String color = getColor(nominalV, null).orElse(DEFAULT_COLOR);
        StringBuilder style = new StringBuilder();
        style.append(".").append(!(edge instanceof LineEdge) ? WIRE_STYLE_CLASS : LINE_STYLE_CLASS).append("_").append(escapeClassName(vlId)).append(" {stroke:").append(color).append(";stroke-width:1;}");
        return Optional.of(style.toString());
    }
}
