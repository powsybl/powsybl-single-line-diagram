/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.view;

import com.powsybl.sld.force.layout.ForceSubstationLayout;
import com.powsybl.sld.layout.InfoCalcPoints;
import com.powsybl.sld.layout.LayoutParameters;
import com.powsybl.sld.library.ComponentSize;
import com.powsybl.sld.model.BaseNode;
import com.powsybl.sld.model.BusCell;
import com.powsybl.sld.model.Coord;
import com.powsybl.sld.svg.GraphMetadata;
import javafx.scene.Node;
import javafx.scene.shape.Polyline;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

import static com.powsybl.sld.library.ComponentTypeName.BUSBAR_SECTION;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class VoltageLevelHandler implements BaseNode {

    private final Node node;   // node for voltageLevel label

    private final List<NodeHandler> nodeHandlers = new ArrayList<>();

    private final String vId;

    private double mouseX;
    private double mouseY;

    private final GraphMetadata metadata;

    public VoltageLevelHandler(Node node, GraphMetadata metadata, String vId) {
        this.node = Objects.requireNonNull(node);
        this.metadata = Objects.requireNonNull(metadata);
        this.vId = Objects.requireNonNull(vId);

        setDragAndDrop();
    }

    public Node getNode() {
        return node;
    }

    @Override
    public String getId() {
        return node.getId();
    }

    public String getVId() {
        return vId;
    }

    @Override
    public String getComponentType() {
        return null;
    }

    @Override
    public Double getRotationAngle() {
        return null;
    }

    @Override
    public boolean isRotated() {
        return false;
    }

    @Override
    public double getX() {
        ComponentSize size = new ComponentSize(0, 0);
        return node.localToParent(node.getLayoutX() + size.getWidth() / 2,
                                  node.getLayoutY() + size.getHeight() / 2).getX();
    }

    @Override
    public double getY() {
        ComponentSize size = new ComponentSize(0, 0);
        return node.localToParent(node.getLayoutX() + size.getWidth() / 2,
                                  node.getLayoutY() + size.getHeight() / 2).getY();
    }

    public void addNodeHandlers(List<NodeHandler> nodeHandlers) {
        this.nodeHandlers.addAll(nodeHandlers);
    }

    public void setDragAndDrop() {
        node.setOnMousePressed(event -> {
            mouseX = event.getSceneX() - node.getTranslateX();
            mouseY = event.getSceneY() - node.getTranslateY();
            event.consume();
        });

        node.setOnMouseDragged(event -> {
            // apply transformation for label node
            node.setTranslateX(event.getSceneX() - mouseX);
            node.setTranslateY(event.getSceneY() - mouseY);

            // apply transformation to all nodes of the voltageLevel in nodeHandlers list
            nodeHandlers.stream().filter(n -> n.getVId().equals(vId)).forEach(v -> v.translate(event.getSceneX() - mouseX,
                                                           event.getSceneY() - mouseY));

            // redraw the snakeLines between the voltage levels
            redrawSnakeLines();

            event.consume();
        });
    }

    private void redrawSnakeLines() {
        // trying to redraw the snakeLines between the voltage levels
        Map<String, Coord> posVL = new HashMap<>();

        List<WireHandler> whSnakeLines = new ArrayList<>();

        for (NodeHandler nh : nodeHandlers) {
            if (nh.getComponentType() != null && nh.getComponentType().equals(BUSBAR_SECTION)) {
                if (!posVL.containsKey(nh.getVId())) {
                    posVL.put(nh.getVId(), new Coord(Double.MAX_VALUE, 0));
                }
                double x = Math.min(posVL.get(nh.getVId()).getX(), nh.getX());
                posVL.put(nh.getVId(), new Coord(x, 0));
            }

            for (WireHandler wh : nh.getWireHandlers()) {
                if (wh.isSnakeLine()) {
                    whSnakeLines.add(wh);
                }
            }
        }

        for (WireHandler wh : whSnakeLines) {
            List<Double> pol = calculatePolylineSnakeLine(metadata.getLayoutParameters(), wh, posVL);
            if (!pol.isEmpty()) {
                ((Polyline) wh.getNode()).getPoints().setAll(pol);
            }
        }
    }

    private List<Double> calculatePolylineSnakeLine(LayoutParameters layoutParam, WireHandler wh, Map<String, Coord> posVL) {
        NodeHandler nh1 = wh.getNodeHandler1();
        NodeHandler nh2 = wh.getNodeHandler2();

        BusCell.Direction dNode1 = nh1.getDirection();
        BusCell.Direction dNode2 = nh2.getDirection();

        double xMaxGraph;
        String idMaxGraph;

        if (StringUtils.isEmpty(nh1.getVId()) || StringUtils.isEmpty(nh2.getVId()) || posVL.get(nh1.getVId()) == null || posVL.get(nh2.getVId()) == null) {
            return Collections.emptyList();
        }

        if (posVL.get(nh1.getVId()).getX() > posVL.get(nh2.getVId()).getX()) {
            xMaxGraph = posVL.get(nh1.getVId()).getX();
            idMaxGraph = nh1.getVId();
        } else {
            xMaxGraph = posVL.get(nh2.getVId()).getX();
            idMaxGraph = nh2.getVId();
        }

        double x1 = nh1.getX();
        double y1 = nh1.getY();
        double x2 = nh2.getX();
        double y2 = nh2.getY();

        InfoCalcPoints info = new InfoCalcPoints();
        info.setLayoutParam(layoutParam);
        info.setGraphId1(nh1.getVId());
        info.setGraphId2(nh2.getVId());
        info.setdNode1(dNode1);
        info.setdNode2(dNode2);
        info.setX1(x1);
        info.setX2(x2);
        info.setY1(y1);
        info.setInitY1(y1);
        info.setY2(y2);
        info.setInitY2(y2);
        info.setxMaxGraph(xMaxGraph);
        info.setIdMaxGraph(idMaxGraph);

        return new ForceSubstationLayout(null, null, null).calculatePolylinePoints(info);
    }
}
