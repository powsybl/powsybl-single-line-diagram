/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.cgmes.layout;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.TopologyKind;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.sld.cgmes.dl.iidm.extensions.DiagramPoint;
import com.powsybl.sld.cgmes.dl.iidm.extensions.LineDiagramData;
import com.powsybl.sld.layout.LayoutParameters;
import com.powsybl.sld.layout.ZoneLayout;
import com.powsybl.sld.model.Graph;
import com.powsybl.sld.model.LineEdge;
import com.powsybl.sld.model.SubstationGraph;
import com.powsybl.sld.model.ZoneGraph;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class CgmesZoneLayout extends AbstractCgmesLayout implements ZoneLayout {

    private static final Logger LOG = LoggerFactory.getLogger(CgmesZoneLayout.class);

    private final ZoneGraph graph;
    private List<Graph> vlGraphs;

    public CgmesZoneLayout(ZoneGraph graph, Network network) {
        this.network = Objects.requireNonNull(network);
        Objects.requireNonNull(graph);
        vlGraphs = graph.getNodes().stream().map(SubstationGraph::getNodes).flatMap(Collection::stream).collect(Collectors.toList());
        for (Graph vlGraph : vlGraphs) {
            removeFictitiousNodes(vlGraph, network.getVoltageLevel(vlGraph.getVoltageLevelId()));
        }
        fixTransformersLabel = true;
        this.graph = graph;
    }

    @Override
    public void run(LayoutParameters layoutParam) {
        if (graph.getZoneId().isEmpty()) {
            LOG.warn("No substations in the zone: skipping coordinates assignment");
            return;
        }
        String diagramName = layoutParam.getDiagramName();
        if (!checkDiagram(diagramName, "")) {
            return;
        }
        // assign coordinates
        for (Graph vlGraph : vlGraphs) {
            VoltageLevel vl = network.getVoltageLevel(vlGraph.getVoltageLevelId());
            setNodeCoordinates(vl, vlGraph, diagramName);
        }
        for (LineEdge edge : graph.getEdges()) {
            VoltageLevel vl = network.getVoltageLevel(edge.getNode1().getGraph().getVoltageLevelId());
            setLineCoordinates(vl, edge, diagramName);
        }
        // shift coordinates
        for (Graph vlGraph : vlGraphs) {
            vlGraph.getNodes().forEach(node -> shiftNodeCoordinates(node, layoutParam.getScaleFactor()));
        }
        for (LineEdge edge : graph.getEdges()) {
            shiftLineCoordinates(edge, layoutParam.getScaleFactor());
        }
        // scale coordinates
        if (layoutParam.getScaleFactor() != 1) {
            for (Graph vlGraph : vlGraphs) {
                vlGraph.getNodes().forEach(node -> scaleNodeCoordinates(node, layoutParam.getScaleFactor()));
            }
            for (LineEdge edge : graph.getEdges()) {
                scaleLineCoordinates(edge, layoutParam.getScaleFactor());
            }
        }
    }

    private void setLineCoordinates(VoltageLevel vl, LineEdge edge, String diagramName) {
        Line line = vl.getConnectable(edge.getLineId(), Line.class);
        if (line == null) {
            LOG.warn("No line {} in voltage level {}, skipping line edge", edge.getLineId(), edge.getLineId());
            return;
        }
        LineDiagramData<Line> lineDiagramData = line.getExtension(LineDiagramData.class);
        if (lineDiagramData == null) {
            LOG.warn("No CGMES-DL data for line {} name {}, skipping line edge {}", line.getId(), line.getName(), edge.getLineId());
            return;
        }
        if (!lineDiagramData.getDiagramsNames().contains(diagramName)) {
            LOG.warn("No CGMES-DL data for line {} name {}, diagramName {}, skipping line edge {}", line.getId(), line.getName(), diagramName, edge.getLineId());
            return;
        }
        lineDiagramData.getPoints(diagramName).forEach(point -> {
            edge.addPoint(point.getX(), point.getY());
            setMin(point.getX(), point.getY());
        });
        if (TopologyKind.BUS_BREAKER.equals(line.getTerminal1().getVoltageLevel().getTopologyKind())) {
            // if bus breaker topology first and last point of lines are shifted
            DiagramPoint firstPoint = lineDiagramData.getFirstPoint(diagramName, LINE_OFFSET);
            edge.getPoints().get(0).setX(firstPoint.getX());
            edge.getPoints().get(0).setY(firstPoint.getY());
            DiagramPoint lastPoint = lineDiagramData.getLastPoint(diagramName, LINE_OFFSET);
            edge.getPoints().get(edge.getPoints().size() - 1).setX(lastPoint.getX());
            edge.getPoints().get(edge.getPoints().size() - 1).setY(lastPoint.getY());
        }
    }

    private void shiftLineCoordinates(LineEdge edge, double scaleFactor) {
        for (LineEdge.Point point : edge.getPoints()) {
            point.setX(point.getX() - minX + (X_MARGIN / scaleFactor));
            point.setY(point.getY() - minY + (Y_MARGIN / scaleFactor));
        }
    }

    private void scaleLineCoordinates(LineEdge edge, double scaleFactor) {
        for (LineEdge.Point point : edge.getPoints()) {
            point.setX(point.getX() * scaleFactor);
            point.setY(point.getY() * scaleFactor);
        }
    }

}
