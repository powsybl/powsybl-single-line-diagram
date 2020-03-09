/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout;

import com.powsybl.commons.PowsyblException;
import com.powsybl.sld.model.BusCell;
import com.powsybl.sld.model.ExternCell;
import com.powsybl.sld.model.Node;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class InfoCalcPoints {
    private LayoutParameters layoutParam;
    private BusCell.Direction dNode1;
    private BusCell.Direction dNode2;
    private double x1;
    private double x2;
    private double y1;
    private double initY1;
    private double y2;
    private double initY2;
    private double xMaxGraph;
    private String idMaxGraph;
    private String idMaxSubstation;
    private boolean increment;
    private String graphId1;
    private String graphId2;
    private String substationId1;
    private String substationId2;
    private double maxH;
    private double xMinGraph;
    private boolean adjacentGraphs;

    public InfoCalcPoints() {
    }

    public InfoCalcPoints(LayoutParameters layoutParameters, Node node1, Node node2, boolean increment) {
        this.layoutParam = layoutParameters;
        this.graphId1 = node1.getGraph().getVoltageLevelId();
        this.graphId2 = node2.getGraph().getVoltageLevelId();
        this.x1 = node1.getX();
        this.x2 = node2.getX();
        this.y1 = node1.getY();
        this.initY1 = node1.getInitY() != -1 ? node1.getInitY() : node1.getY();
        this.y2 = node2.getY();
        this.initY2 = node2.getInitY() != -1 ? node2.getInitY() : node2.getY();
        this.xMaxGraph = Math.max(node1.getGraph().getX(), node2.getGraph().getX());
        this.idMaxGraph = node1.getGraph().getX() > node2.getGraph().getX()
                ? node1.getGraph().getVoltageLevelId()
                : node2.getGraph().getVoltageLevelId();
        this.xMinGraph = Math.min(node1.getGraph().getX(), node2.getGraph().getX());
        this.dNode1 = getNodeDirection(node1, 1);
        this.dNode2 = getNodeDirection(node2, 2);
        this.increment = increment;
    }

    public LayoutParameters getLayoutParam() {
        return layoutParam;
    }

    public void setLayoutParam(LayoutParameters layoutParam) {
        this.layoutParam = layoutParam;
    }

    public BusCell.Direction getdNode1() {
        return dNode1;
    }

    public void setdNode1(BusCell.Direction dNode1) {
        this.dNode1 = dNode1;
    }

    public BusCell.Direction getdNode2() {
        return dNode2;
    }

    public void setdNode2(BusCell.Direction dNode2) {
        this.dNode2 = dNode2;
    }

    public double getX1() {
        return x1;
    }

    public void setX1(double x1) {
        this.x1 = x1;
    }

    public double getX2() {
        return x2;
    }

    public void setX2(double x2) {
        this.x2 = x2;
    }

    public double getY1() {
        return y1;
    }

    public void setY1(double y1) {
        this.y1 = y1;
    }

    public double getY2() {
        return y2;
    }

    public void setY2(double y2) {
        this.y2 = y2;
    }

    public double getInitY1() {
        return initY1;
    }

    public void setInitY1(double initY1) {
        this.initY1 = initY1;
    }

    public double getInitY2() {
        return initY2;
    }

    public void setInitY2(double initY2) {
        this.initY2 = initY2;
    }

    public double getxMaxGraph() {
        return xMaxGraph;
    }

    public void setxMaxGraph(double xMaxGraph) {
        this.xMaxGraph = xMaxGraph;
    }

    public String getIdMaxGraph() {
        return idMaxGraph;
    }

    public void setIdMaxGraph(String idMaxGraph) {
        this.idMaxGraph = idMaxGraph;
    }

    public String getIdMaxSubstation() {
        return idMaxSubstation;
    }

    public void setIdMaxSubstation(String idMaxSubstation) {
        this.idMaxSubstation = idMaxSubstation;
    }

    public boolean isIncrement() {
        return increment;
    }

    public String getGraphId1() {
        return graphId1;
    }

    public void setGraphId1(String graphId1) {
        this.graphId1 = graphId1;
    }

    public String getGraphId2() {
        return graphId2;
    }

    public void setGraphId2(String graphId2) {
        this.graphId2 = graphId2;
    }

    public String getSubstationId1() {
        return substationId1;
    }

    public void setSubstationId1(String substationId1) {
        this.substationId1 = substationId1;
    }

    public String getSubstationId2() {
        return substationId2;
    }

    public void setSubstationId2(String substationId2) {
        this.substationId2 = substationId2;
    }

    public double getMaxH() {
        return maxH;
    }

    public void setMaxH(double maxH) {
        this.maxH = maxH;
    }

    public double getxMinGraph() {
        return xMinGraph;
    }

    public boolean isAdjacentGraphs() {
        return adjacentGraphs;
    }

    public void setAdjacentGraphs(boolean adjacentGraphs) {
        this.adjacentGraphs = adjacentGraphs;
    }

    private BusCell.Direction getNodeDirection(Node node, int nb) {
        if (node.getType() != Node.NodeType.FEEDER) {
            throw new PowsyblException("Node " + nb + " is not a feeder node");
        }
        BusCell.Direction dNode = node.getCell() != null ? ((ExternCell) node.getCell()).getDirection() : BusCell.Direction.TOP;
        if (dNode != BusCell.Direction.TOP && dNode != BusCell.Direction.BOTTOM) {
            throw new PowsyblException("Node " + nb + " cell direction not TOP or BOTTOM");
        }
        return dNode;
    }
}
