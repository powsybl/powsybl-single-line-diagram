/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout;

import com.powsybl.sld.model.BusCell;
import com.powsybl.sld.model.Point;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class InfoCalcPoints {
    private LayoutParameters layoutParam;
    private BusCell.Direction dNode1;
    private BusCell.Direction dNode2;
    private Point coord1;
    private Point coord2;
    private double xMaxGraph;
    private String idMaxGraph;
    private boolean increment;

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

    public Point getCoord1() {
        return coord1;
    }

    public void setCoord1(Point coord1) {
        this.coord1 = coord1;
    }

    public Point getCoord2() {
        return coord2;
    }

    public void setCoord2(Point coord2) {
        this.coord2 = coord2;
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

    public boolean isIncrement() {
        return increment;
    }

    public void setIncrement(boolean increment) {
        this.increment = increment;
    }
}
