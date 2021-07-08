/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld;

import com.powsybl.sld.model.*;

import java.util.*;

import static com.powsybl.sld.model.FeederWithSideNode.Side.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */

public class RawGraphBuilder implements GraphBuilder {

    private final Map<String, VoltageLevelBuilder> vlBuilders = new TreeMap<>();
    private final Map<String, SubstationBuilder> ssBuilders = new TreeMap<>();

    public VoltageLevelBuilder createVoltageLevelBuilder(VoltageLevelInfos voltageLevelInfos, boolean forVoltageLevelDiagram) {
        VoltageLevelBuilder vlBuilder = new VoltageLevelBuilder(voltageLevelInfos, forVoltageLevelDiagram);
        vlBuilders.put(voltageLevelInfos.getId(), vlBuilder);
        return vlBuilder;
    }

    public VoltageLevelBuilder createVoltageLevelBuilder(String vlId, double vlNominalV, boolean forVoltageLevelDiagram) {
        return createVoltageLevelBuilder(new VoltageLevelInfos(vlId, vlId, vlNominalV), forVoltageLevelDiagram);
    }

    public VoltageLevelBuilder createVoltageLevelBuilder(VoltageLevelInfos voltageLevelInfos) {
        return createVoltageLevelBuilder(voltageLevelInfos, true);
    }

    public VoltageLevelBuilder createVoltageLevelBuilder(String vlId, double vlNominalV) {
        return createVoltageLevelBuilder(new VoltageLevelInfos(vlId, vlId, vlNominalV));
    }

    public VoltageLevelInfos getVoltageLevelInfosFromId(String id) {
        if (vlBuilders.containsKey(id)) {
            return vlBuilders.get(id).voltageLevelInfos;
        }
        return new VoltageLevelInfos("OTHER", "OTHER", 0);
    }

    public class VoltageLevelBuilder {

        private VoltageLevelInfos voltageLevelInfos;
        private final VoltageLevelGraph graph;

        private SubstationBuilder substationBuilder;

        public VoltageLevelBuilder(VoltageLevelInfos voltageLevelInfos, boolean forVoltageLevelDiagram) {
            this.voltageLevelInfos = voltageLevelInfos;
            graph = VoltageLevelGraph.create(voltageLevelInfos, false, forVoltageLevelDiagram);
        }

        public VoltageLevelGraph getGraph() {
            return graph;
        }

        public SubstationBuilder getSubstationBuilder() {
            return substationBuilder;
        }

        public void setSubstationBuilder(SubstationBuilder substationBuilder) {
            this.substationBuilder = substationBuilder;
        }

        public BusNode createBusBarSection(String id, int busbarIndex, int sectionIndex) {
            BusNode busNode = BusNode.create(graph, id, id);
            graph.addNode(busNode);
            busNode.setBusBarIndexSectionIndex(busbarIndex, sectionIndex);
            return busNode;
        }

        public BusNode createBusBarSection(String id) {
            BusNode busNode = BusNode.create(graph, id, id);
            graph.addNode(busNode);
            return busNode;
        }

        public SwitchNode createSwitchNode(SwitchNode.SwitchKind sk, String id, boolean fictitious, boolean open) {
            SwitchNode sw = new SwitchNode(id, id, sk.name(), fictitious, graph, sk, open);
            graph.addNode(sw);
            return sw;
        }

        public void connectNode(Node node1, Node node2) {
            graph.addEdge(node1, node2);
        }

        public FictitiousNode createFictitiousNode(String id) {
            InternalNode fictitiousNode = new InternalNode(graph, id);
            graph.addNode(fictitiousNode);
            return fictitiousNode;
        }

        public void addExtension(FeederNode fn, int order, BusCell.Direction direction) {
            fn.setOrder(order);
            fn.setDirection(direction);
        }

        private void commonFeederSetting(FeederNode node, String id, int order, BusCell.Direction direction) {
            node.setLabel(id);
            graph.addNode(node);
            if (direction != null) {
                addExtension(node, order, direction);
            }
        }

        public FeederNode createLoad(String id) {
            return createLoad(id, 0, null);
        }

        public FeederNode createLoad(String id, int order, BusCell.Direction direction) {
            FeederInjectionNode fn = FeederInjectionNode.createLoad(graph, id, id);
            commonFeederSetting(fn, id, order, direction);
            return fn;
        }

        public FeederNode createGenerator(String id) {
            return createGenerator(id, 0, null);
        }

        public FeederNode createGenerator(String id, int order, BusCell.Direction direction) {
            FeederInjectionNode fn = FeederInjectionNode.createGenerator(graph, id, id);
            commonFeederSetting(fn, id, order, direction);
            return fn;
        }

        public FeederLineNode createFeederLineNode(String id, String otherVlId, FeederWithSideNode.Side side, int order, BusCell.Direction direction) {
            FeederLineNode fln = FeederLineNode.create(graph, id + "_" + side, id, id, side, getVoltageLevelInfosFromId(otherVlId));
            commonFeederSetting(fln, id, order, direction);
            return fln;
        }

        public Feeder2WTNode createFeeder2WTNode(String id, String otherVlId, FeederWithSideNode.Side side,
                                                 int order, BusCell.Direction direction) {
            Feeder2WTNode f2WTe = Feeder2WTNode.create(graph, id + "_" + side, id, id, side, getVoltageLevelInfosFromId(otherVlId));
            commonFeederSetting(f2WTe, id, order, direction);
            return f2WTe;
        }

        public Feeder2WTLegNode createFeeder2wtLegNode(String id, FeederWithSideNode.Side side,
                                                       int order, BusCell.Direction direction) {
            Feeder2WTLegNode f2WTe = Feeder2WTLegNode.createForSubstationDiagram(graph, id + "_" + side, id, id, side);
            commonFeederSetting(f2WTe, id, order, direction);
            return f2WTe;
        }

        public Feeder3WTLegNode createFeeder3wtLegNode(String id, FeederWithSideNode.Side side, int order, BusCell.Direction direction) {
            Feeder3WTLegNode f3WTe = Feeder3WTLegNode.createForSubstationDiagram(graph, id + "_" + side, id, id, side);
            commonFeederSetting(f3WTe, id + side.getIntValue(), order, direction);
            return f3WTe;
        }
    }

    public SubstationBuilder createSubstationBuilder(String id) {
        SubstationBuilder ssb = new SubstationBuilder(id);
        ssBuilders.put(id, ssb);
        return ssb;
    }

    public class SubstationBuilder {

        SubstationGraph ssGraph;

        public SubstationBuilder(String id) {
            ssGraph = SubstationGraph.create(id);
        }

        public SubstationGraph getSsGraph() {
            return ssGraph;
        }

        public void addVlBuilder(VoltageLevelBuilder vlBuilder) {
            ssGraph.addNode(vlBuilder.getGraph());
            vlBuilder.setSubstationBuilder(this);
        }

        public Map<VoltageLevelBuilder, FeederLineNode> createLine(String id, VoltageLevelBuilder vl1, VoltageLevelBuilder vl2, int order1, int order2,
                                                                   BusCell.Direction direction1, BusCell.Direction direction2) {
            Map<VoltageLevelBuilder, FeederLineNode> feederLineNodes = new HashMap<>();
            FeederLineNode feederLineNode1 = vl1.createFeederLineNode(id, vl2.voltageLevelInfos.getId(), ONE, order1, direction1);
            FeederLineNode feederLineNode2 = vl2.createFeederLineNode(id, vl1.voltageLevelInfos.getId(), TWO, order2, direction2);
            feederLineNodes.put(vl1, feederLineNode1);
            feederLineNodes.put(vl2, feederLineNode2);
            ssGraph.addTwtEdge(feederLineNode1, feederLineNode2);
            return feederLineNodes;
        }

        public Map<VoltageLevelBuilder, FeederLineNode> createLine(String id, VoltageLevelBuilder vl1, VoltageLevelBuilder vl2) {
            return createLine(id, vl1, vl2, 0, 0, null, null);
        }

        public Map<VoltageLevelBuilder, Feeder2WTLegNode> createFeeder2WT(String id, VoltageLevelBuilder vl1, VoltageLevelBuilder vl2, int order1, int order2,
                                                                          BusCell.Direction direction1, BusCell.Direction direction2) {
            Map<VoltageLevelBuilder, Feeder2WTLegNode> f2WTNodes = new HashMap<>();
            Feeder2WTLegNode feeder2WtNode1 = vl1.createFeeder2wtLegNode(id, ONE, order1, direction1);
            Feeder2WTLegNode feeder2WTNode2 = vl2.createFeeder2wtLegNode(id, TWO, order2, direction2);
            f2WTNodes.put(vl1, feeder2WtNode1);
            f2WTNodes.put(vl2, feeder2WTNode2);
            ssGraph.addMultiTermNode(Middle2WTNode.create(ssGraph, feeder2WtNode1, feeder2WTNode2, vl1.voltageLevelInfos, vl2.voltageLevelInfos));
            return f2WTNodes;
        }

        public Map<VoltageLevelBuilder, Feeder2WTLegNode> createFeeder2WT(String id, VoltageLevelBuilder vl1, VoltageLevelBuilder vl2) {
            return createFeeder2WT(id, vl1, vl2, 0, 0, null, null);
        }

        public Map<VoltageLevelBuilder, Feeder3WTLegNode> createFeeder3WT(String id, VoltageLevelBuilder vl1, VoltageLevelBuilder vl2, VoltageLevelBuilder vl3,
                                                                          int order1, int order2, int order3,
                                                                          BusCell.Direction direction1, BusCell.Direction direction2, BusCell.Direction direction3) {
            Map<VoltageLevelBuilder, Feeder3WTLegNode> f3WTNodes = new HashMap<>();
            Feeder3WTLegNode feeder3WTNode1 = vl1.createFeeder3wtLegNode(id, ONE, order1, direction1);
            Feeder3WTLegNode feeder3WTNode2 = vl2.createFeeder3wtLegNode(id, TWO, order2, direction2);
            Feeder3WTLegNode feeder3WTNode3 = vl3.createFeeder3wtLegNode(id, THREE, order3, direction3);
            f3WTNodes.put(vl1, feeder3WTNode1);
            f3WTNodes.put(vl2, feeder3WTNode2);
            f3WTNodes.put(vl3, feeder3WTNode3);

            // creation of the middle node and the edges linking the transformer leg nodes to this middle node
            ssGraph.addMultiTermNode(Middle3WTNode.create(ssGraph, feeder3WTNode1, feeder3WTNode2, feeder3WTNode3,
                    vl1.voltageLevelInfos, vl2.voltageLevelInfos, vl3.voltageLevelInfos));

            return f3WTNodes;
        }

        public Map<VoltageLevelBuilder, Feeder3WTLegNode> createFeeder3WT(String id, VoltageLevelBuilder vl1, VoltageLevelBuilder vl2, VoltageLevelBuilder vl3) {
            return createFeeder3WT(id, vl1, vl2, vl3, 0, 0, 0, null, null, null);
        }
    }

    public VoltageLevelGraph buildVoltageLevelGraph(String id,
                                                    boolean useName,
                                                    boolean forVoltageLevelDiagram) {
        return vlBuilders.get(id).getGraph();
    }

    public SubstationGraph buildSubstationGraph(String id,
                                                boolean useName) {
        SubstationGraph ssGraph = ssBuilders.get(id).getSsGraph();
        ssGraph.getNodes().sort(Comparator.comparingDouble(g -> -g.getVoltageLevelInfos().getNominalVoltage()));
        return ssGraph;
    }

    //TODO: buildZoneGraph
    public ZoneGraph buildZoneGraph(List<String> substationIds, boolean useName) {
        return null;
    }
}
