/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.cgmes.layout;

import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.TopologyKind;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.sld.cgmes.dl.iidm.extensions.DiagramPoint;
import com.powsybl.sld.cgmes.dl.iidm.extensions.LineDiagramData;
import com.powsybl.sld.layout.LayoutParameters;
import com.powsybl.sld.layout.ZoneLayout;
import com.powsybl.sld.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public class CgmesZoneLayout extends AbstractCgmesLayout implements ZoneLayout {

    private static final Logger LOG = LoggerFactory.getLogger(CgmesZoneLayout.class);

    private final ZoneGraph graph;
    private List<VoltageLevelGraph> vlGraphs;

    public CgmesZoneLayout(ZoneGraph graph, Network network) {
        this.network = Objects.requireNonNull(network);
        Objects.requireNonNull(graph);
        vlGraphs = graph.getNodes().stream().map(SubstationGraph::getNodes).flatMap(Collection::stream).collect(Collectors.toList());
        for (VoltageLevelGraph vlGraph : vlGraphs) {
            removeFictitiousNodes(vlGraph, network.getVoltageLevel(vlGraph.getVoltageLevelInfos().getId()));
        }
        fixTransformersLabel = true;
        this.graph = graph;
    }

    @Override
    public void run(LayoutParameters layoutParam) {
        if (graph.getZone().isEmpty()) {
            LOG.warn("No substations in the zone: skipping coordinates assignment");
            return;
        }
        String diagramName = layoutParam.getDiagramName();
        if (!checkDiagram(diagramName, "")) {
            return;
        }
        // assign coordinates
        for (VoltageLevelGraph vlGraph : vlGraphs) {
            VoltageLevel vl = network.getVoltageLevel(vlGraph.getVoltageLevelInfos().getId());
            setNodeCoordinates(vl, vlGraph, diagramName);
        }
        for (BranchEdge edge : graph.getLineEdges()) {
            VoltageLevel vl = network.getVoltageLevel(edge.getNode1().getGraph().getVoltageLevelInfos().getId());
            setLineCoordinates(vl, edge, diagramName);
        }
        // shift coordinates
        for (VoltageLevelGraph vlGraph : vlGraphs) {
            vlGraph.getNodes().forEach(node -> shiftNodeCoordinates(node, layoutParam.getScaleFactor()));
        }
        for (BranchEdge edge : graph.getLineEdges()) {
            shiftLineCoordinates(edge, layoutParam.getScaleFactor());
        }
        // scale coordinates
        if (layoutParam.getScaleFactor() != 1) {
            for (VoltageLevelGraph vlGraph : vlGraphs) {
                vlGraph.getNodes().forEach(node -> scaleNodeCoordinates(node, layoutParam.getScaleFactor()));
            }
            for (BranchEdge edge : graph.getLineEdges()) {
                scaleLineCoordinates(edge, layoutParam.getScaleFactor());
            }
        }
    }

    private void setLineCoordinates(VoltageLevel vl, BranchEdge edge, String diagramName) {
        Line line = vl.getConnectable(edge.getId(), Line.class);
        if (line == null) {
            LOG.warn("No line {} in voltage level {}, skipping line edge", edge.getId(), edge.getId());
            return;
        }
        LineDiagramData<Line> lineDiagramData = line.getExtension(LineDiagramData.class);
        if (lineDiagramData == null) {
            LOG.warn("No CGMES-DL data for line {} name {}, skipping line edge {}", line.getId(), line.getName(), edge.getId());
            return;
        }
        if (!lineDiagramData.getDiagramsNames().contains(diagramName)) {
            LOG.warn("No CGMES-DL data for line {} name {}, diagramName {}, skipping line edge {}", line.getId(), line.getName(), diagramName, edge.getId());
            return;
        }
        List<Point> snakeLine = edge.getSnakeLine();
        lineDiagramData.getPoints(diagramName).forEach(point -> {
            snakeLine.add(new Point(point.getX(), point.getY()));
            setMin(point.getX(), point.getY());
        });

        if (TopologyKind.BUS_BREAKER.equals(line.getTerminal1().getVoltageLevel().getTopologyKind())) {
            // if bus breaker topology first and last point of lines are shifted
            DiagramPoint firstPoint = lineDiagramData.getFirstPoint(diagramName, LINE_OFFSET);
            edge.getSnakeLine().get(0).setX(firstPoint.getX());
            edge.getSnakeLine().get(0).setY(firstPoint.getY());
            DiagramPoint lastPoint = lineDiagramData.getLastPoint(diagramName, LINE_OFFSET);
            edge.getSnakeLine().get(edge.getSnakeLine().size() - 1).setX(lastPoint.getX());
            edge.getSnakeLine().get(edge.getSnakeLine().size() - 1).setY(lastPoint.getY());
        }
    }

    private void shiftLineCoordinates(BranchEdge edge, double scaleFactor) {
        Point shift = new Point(-minX + (X_MARGIN / scaleFactor), -minY + (Y_MARGIN / scaleFactor));
        edge.getSnakeLine().forEach(p -> p.shift(shift));
    }

    private void scaleLineCoordinates(BranchEdge edge, double scaleFactor) {
        edge.getSnakeLine().forEach(p -> p.scale(scaleFactor));
    }

}
