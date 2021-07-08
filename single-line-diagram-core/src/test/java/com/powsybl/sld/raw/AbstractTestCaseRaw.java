/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.raw;

import com.powsybl.sld.AbstractTestCase;
import com.powsybl.sld.RawGraphBuilder;
import com.powsybl.sld.layout.LayoutParameters;
import com.powsybl.sld.model.VoltageLevelGraph;
import com.powsybl.sld.model.Node;
import com.powsybl.sld.model.SubstationGraph;
import com.powsybl.sld.model.ZoneGraph;
import com.powsybl.sld.svg.DefaultDiagramStyleProvider;
import com.powsybl.sld.svg.DiagramLabelProvider;
import com.powsybl.sld.svg.InitialValue;
import com.powsybl.sld.svg.LabelPosition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 */
public abstract class AbstractTestCaseRaw extends AbstractTestCase {
    protected RawGraphBuilder rawGraphBuilder = new RawGraphBuilder();
    private LayoutParameters layoutParameters;

    protected AbstractTestCaseRaw() {
        layoutParameters = createDefaultLayoutParameters();
    }

    @Override
    protected LayoutParameters getLayoutParameters() {
        return layoutParameters;
    }

    @Override
    public void toSVG(VoltageLevelGraph graph, String filename) {
        Stream<Node> nodeStream = graph.getNodes().stream();
        toSVG(graph, filename, getLayoutParameters(), new RawDiagramLabelProvider(nodeStream), new DefaultDiagramStyleProvider());
    }

    @Override
    public void toSVG(SubstationGraph graph, String filename) {
        Stream<Node> nodeStream = graph.getNodes().stream().flatMap(g -> g.getNodes().stream());
        toSVG(graph, filename, getLayoutParameters(), new RawDiagramLabelProvider(nodeStream), new DefaultDiagramStyleProvider());
    }

    @Override
    public void toSVG(ZoneGraph graph, String filename) {
        Stream<Node> nodeStream = graph.getNodes().stream().flatMap(g -> g.getNodes().stream()).flatMap(g -> g.getNodes().stream());
        toSVG(graph, filename, getLayoutParameters(), new RawDiagramLabelProvider(nodeStream), new DefaultDiagramStyleProvider());
    }

    private static class RawDiagramLabelProvider implements DiagramLabelProvider {
        private final Map<Node, List<NodeLabel>> busLabels;

        public RawDiagramLabelProvider(Stream<Node> nodeStream) {
            this.busLabels = new HashMap<>();
            LabelPosition labelPosition = new LabelPosition("default", 0, -5, true, 0);
            nodeStream.forEach(n -> {
                List<DiagramLabelProvider.NodeLabel> labels = new ArrayList<>();
                labels.add(new DiagramLabelProvider.NodeLabel(n.getLabel(), labelPosition));
                busLabels.put(n, labels);
            });
        }

        @Override
        public InitialValue getInitialValue(Node node) {
            return new InitialValue(Direction.UP, Direction.DOWN, "tata", "tutu", "", "");
        }

        @Override
        public List<NodeLabel> getNodeLabels(Node node) {
            return busLabels.get(node);
        }

        @Override
        public List<NodeDecorator> getNodeDecorators(Node node) {
            return new ArrayList<>();
        }
    }
}
