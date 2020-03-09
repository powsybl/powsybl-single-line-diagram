/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.svg;

import static com.powsybl.sld.library.ComponentTypeName.ARROW;
import static com.powsybl.sld.library.ComponentTypeName.BREAKER;
import static com.powsybl.sld.library.ComponentTypeName.BUSBAR_SECTION;
import static com.powsybl.sld.library.ComponentTypeName.DISCONNECTOR;
import static com.powsybl.sld.library.ComponentTypeName.NODE;
import static com.powsybl.sld.library.ComponentTypeName.THREE_WINDINGS_TRANSFORMER;
import static com.powsybl.sld.library.ComponentTypeName.TWO_WINDINGS_TRANSFORMER;
import static com.powsybl.sld.svg.DiagramStyles.WIRE_STYLE_CLASS;
import static com.powsybl.sld.svg.DiagramStyles.escapeClassName;
import static com.powsybl.sld.svg.DiagramStyles.escapeId;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.xml.XMLConstants;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import com.powsybl.sld.model.Fictitious3WTNode;
import org.apache.batik.anim.dom.SVGOMDocument;
import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Precision;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.CDATASection;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import org.w3c.dom.svg.SVGElement;

import com.powsybl.commons.exceptions.UncheckedTransformerException;
import com.powsybl.sld.layout.LayoutParameters;
import com.powsybl.sld.library.AnchorOrientation;
import com.powsybl.sld.library.AnchorPoint;
import com.powsybl.sld.library.AnchorPointProvider;
import com.powsybl.sld.library.ComponentLibrary;
import com.powsybl.sld.library.ComponentMetadata;
import com.powsybl.sld.library.ComponentSize;
import com.powsybl.sld.model.BusCell;
import com.powsybl.sld.model.BusNode;
import com.powsybl.sld.model.Edge;
import com.powsybl.sld.model.ExternCell;
import com.powsybl.sld.model.FeederBranchNode;
import com.powsybl.sld.model.FeederNode;
import com.powsybl.sld.model.Graph;
import com.powsybl.sld.model.LineEdge;
import com.powsybl.sld.model.Node;
import com.powsybl.sld.model.SubstationGraph;
import com.powsybl.sld.model.SwitchNode;
import com.powsybl.sld.model.TwtEdge;
import com.powsybl.sld.model.ZoneGraph;
import com.powsybl.sld.svg.DiagramInitialValueProvider.Direction;
import com.powsybl.sld.svg.GraphMetadata.ArrowMetadata;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class DefaultSVGWriter implements SVGWriter {

    private static final String SVG_NAMESPACE = "http://www.w3.org/2000/svg";
    private static final String SVG_QUALIFIED_NAME = "svg";

    protected static final Logger LOGGER = LoggerFactory.getLogger(DefaultSVGWriter.class);

    protected static final String CLASS = "class";
    protected static final String TRANSFORM = "transform";
    protected static final String TRANSLATE = "translate";
    protected static final String ROTATE = "rotate";
    protected static final int FONT_SIZE = 8;
    protected static final String FONT_FAMILY = "Verdana";
    protected static final double LABEL_OFFSET = 5d;
    protected static final int FONT_VOLTAGE_LEVEL_LABEL_SIZE = 12;
    protected static final String POLYLINE = "polyline";
    protected static final String POINTS = "points";

    protected final ComponentLibrary componentLibrary;

    protected final LayoutParameters layoutParameters;

    public DefaultSVGWriter(ComponentLibrary componentLibrary, LayoutParameters layoutParameters) {
        this.componentLibrary = Objects.requireNonNull(componentLibrary);
        this.layoutParameters = Objects.requireNonNull(layoutParameters);
    }

    /**
     * Create the SVGDocument corresponding to the graph
     *
     * @param graph   graph
     * @param svgFile file
     */
    @Override
    public GraphMetadata write(String prefixId,
                               Graph graph,
                               DiagramInitialValueProvider initProvider,
                               DiagramStyleProvider styleProvider,
                               NodeLabelConfiguration nodeLabelConfiguration,
                               Path svgFile) {
        try (Writer writer = Files.newBufferedWriter(svgFile)) {
            return write(prefixId, graph, initProvider, styleProvider, nodeLabelConfiguration, writer);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Create the SVGDocument corresponding to the graph
     *
     * @param graph  graph
     * @param writer writer
     */
    @Override
    public GraphMetadata write(String prefixId,
                               Graph graph,
                               DiagramInitialValueProvider initProvider,
                               DiagramStyleProvider styleProvider,
                               NodeLabelConfiguration nodeLabelConfiguration,
                               Writer writer) {
        DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();

        Document document = domImpl.createDocument(SVG_NAMESPACE, SVG_QUALIFIED_NAME, null);

        Set<String> listUsedComponentSVG = new HashSet<>();
        addStyle(document, styleProvider, Collections.singletonList(graph), listUsedComponentSVG, null, null);

        createDefsSVGComponents(document, listUsedComponentSVG);

        GraphMetadata metadata = writegraph(prefixId, graph, document, initProvider, styleProvider, nodeLabelConfiguration);

        transformDocument(document, writer);

        return metadata;
    }

    protected void addStyle(Document document, DiagramStyleProvider styleProvider, List<Graph> graphs,
                            Set<String> listUsedComponentSVG, List<TwtEdge> snakeLines,
                            List<LineEdge> lines) {
        Element style = document.createElement("style");

        StringBuilder graphStyle = new StringBuilder();
        graphStyle.append(componentLibrary.getStyleSheet());

        for (Graph graph : graphs) {
            graph.getNodes().forEach(n -> {
                Optional<String> nodeStyle = styleProvider.getNodeStyle(n, layoutParameters.isAvoidSVGComponentsDuplication(), layoutParameters.isShowInternalNodes());
                if (nodeStyle.isPresent()) {
                    graphStyle.append(nodeStyle.get()).append("\n");
                }
                listUsedComponentSVG.add(n.getComponentType());
            });
            graph.getEdges().forEach(e -> {
                Optional<String> wireStyle = styleProvider.getWireStyle(e, graph.getVoltageLevelId(), graph.getEdges().indexOf(e));
                if (wireStyle.isPresent()) {
                    graphStyle.append(wireStyle.get()).append("\n");
                }
            });
        }

        if (snakeLines != null) {
            snakeLines.forEach(e -> {
                String idVLS = e.getNode1().getGraph() != null ? e.getNode1().getGraph().getVoltageLevelId() : "_";
                idVLS += e.getNode2().getGraph() != null ? e.getNode2().getGraph().getVoltageLevelId() : "_";

                Optional<String> wireStyle = styleProvider.getWireStyle(e, idVLS, snakeLines.indexOf(e));
                if (wireStyle.isPresent()) {
                    graphStyle.append(wireStyle.get()).append("\n");
                }
            });
        }

        if (lines != null) {
            lines.forEach(e -> {
                String idVLS = e.getNode1().getGraph().getVoltageLevelId() + "_" + e.getNode2().getGraph().getVoltageLevelId();

                Optional<String> wireStyle = styleProvider.getWireStyle(e, idVLS, lines.indexOf(e));
                if (wireStyle.isPresent()) {
                    graphStyle.append(wireStyle.get()).append("\n");
                }
            });
        }

        String cssStr = graphStyle.toString()
                .replace("\r\n", "\n") // workaround for https://bugs.openjdk.java.net/browse/JDK-8133452
                .replace("\r", "\n");
        CDATASection cd = document.createCDATASection(cssStr);
        style.appendChild(cd);

        document.adoptNode(style);
        document.getDocumentElement().appendChild(style);
    }

    protected void transformDocument(Document document, Writer writer) {
        try {
            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(writer);
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            transformer.transform(source, result);
        } catch (TransformerException e) {
            throw new UncheckedTransformerException(e);
        }
    }

    /**
     * Create the SVGDocument corresponding to the graph
     */
    protected GraphMetadata writegraph(String prefixId,
                                       Graph graph,
                                       Document document,
                                       DiagramInitialValueProvider initProvider,
                                       DiagramStyleProvider styleProvider,
                                       NodeLabelConfiguration nodeLabelConfiguration) {
        GraphMetadata metadata = new GraphMetadata();

        Element root = document.createElement("g");

        if (layoutParameters.isShowGrid() && graph.isPositionNodeBusesCalculated()) {
            root.appendChild(drawGrid(prefixId, graph, document, metadata));
        }

        drawVoltageLevel(prefixId, graph, root, metadata, initProvider, styleProvider, nodeLabelConfiguration);

        // the drawing of the voltageLevel graph label is done at the end in order to
        // facilitate the move of a voltageLevel in the diagram
        drawGraphLabel(prefixId, root, graph, metadata);

        document.adoptNode(root);
        document.getDocumentElement().appendChild(root);

        return metadata;
    }

    protected void drawVoltageLevel(String prefixId,
                                    Graph graph,
                                    Element root,
                                    GraphMetadata metadata,
                                    DiagramInitialValueProvider initProvider,
                                    DiagramStyleProvider styleProvider,
                                    NodeLabelConfiguration nodeLabelConfiguration) {
        AnchorPointProvider anchorPointProvider = (type, id) -> {
            if (type.equals(BUSBAR_SECTION)) {
                BusNode busbarSectionNode = (BusNode) graph.getNode(id);
                List<AnchorPoint> result = new ArrayList<>();
                result.add(new AnchorPoint(0, 0, AnchorOrientation.HORIZONTAL));
                for (int i = 1; i < 2 * busbarSectionNode.getPosition().getHSpan(); i++) {
                    result.add(new AnchorPoint(
                            ((double) i / 2) * layoutParameters.getCellWidth() - layoutParameters.getHorizontalBusPadding() / 2,
                            0, AnchorOrientation.VERTICAL));
                }
                result.add(new AnchorPoint(busbarSectionNode.getPxWidth(), 0, AnchorOrientation.HORIZONTAL));
                return result;
            }
            return componentLibrary.getAnchorPoints(type);
        };

        // To avoid overlapping lines over the switches, first, we draw all nodes except the switch nodes,
        // then we draw all the edges, and finally we draw the switch nodes
        drawNodes(prefixId, root, graph, metadata, anchorPointProvider, initProvider, styleProvider, nodeLabelConfiguration, n -> !(n instanceof SwitchNode));
        drawEdges(prefixId, root, graph, metadata, anchorPointProvider, initProvider, styleProvider);
        drawNodes(prefixId, root, graph, metadata, anchorPointProvider, initProvider, styleProvider, nodeLabelConfiguration, n -> n instanceof SwitchNode);
    }

    /**
     * Create the SVGDocument corresponding to the substation graph
     *
     * @param graph   substation graph
     * @param svgFile file
     */
    @Override
    public GraphMetadata write(String prefixId,
                               SubstationGraph graph,
                               DiagramInitialValueProvider initProvider,
                               DiagramStyleProvider styleProvider,
                               NodeLabelConfiguration nodeLabelConfiguration,
                               Path svgFile) {
        try (Writer writer = Files.newBufferedWriter(svgFile)) {
            return write(prefixId, graph, initProvider, styleProvider, nodeLabelConfiguration, writer);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Create the SVGDocument corresponding to the substation graph
     *
     * @param graph  substation graph
     * @param writer writer
     */
    @Override
    public GraphMetadata write(String prefixId,
                               SubstationGraph graph,
                               DiagramInitialValueProvider initProvider,
                               DiagramStyleProvider styleProvider,
                               NodeLabelConfiguration nodeLabelConfiguration,
                               Writer writer) {
        DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();

        Document document = domImpl.createDocument(SVG_NAMESPACE, SVG_QUALIFIED_NAME, null);

        Set<String> listUsedComponentSVG = new HashSet<>();
        addStyle(document, styleProvider, graph.getNodes(), listUsedComponentSVG, graph.getEdges(), null);
        graph.getMultiTermNodes().stream().forEach(n -> listUsedComponentSVG.add(n.getComponentType()));

        createDefsSVGComponents(document, listUsedComponentSVG);

        GraphMetadata metadata = writegraph(prefixId, graph, document, initProvider, styleProvider, nodeLabelConfiguration);

        transformDocument(document, writer);

        return metadata;
    }

    @Override
    public LayoutParameters getLayoutParameters() {
        return layoutParameters;
    }

    @Override
    public ComponentLibrary getComponentLibrary() {
        return componentLibrary;
    }

    /**
     * Create the SVGDocument corresponding to the substation graph
     */
    protected GraphMetadata writegraph(String prefixId,
                                       SubstationGraph graph,
                                       Document document,
                                       DiagramInitialValueProvider initProvider,
                                       DiagramStyleProvider styleProvider,
                                       NodeLabelConfiguration nodeLabelConfiguration) {
        GraphMetadata metadata = new GraphMetadata();

        Element root = document.createElement("g");

        // Drawing grid lines
        if (layoutParameters.isShowGrid()) {
            for (Graph vlGraph : graph.getNodes()) {
                if (vlGraph.isPositionNodeBusesCalculated()) {
                    root.appendChild(drawGrid(prefixId, vlGraph, document, metadata));
                }
            }
        }

        drawSubstation(prefixId, graph, root, metadata, initProvider, styleProvider, nodeLabelConfiguration);

        // the drawing of the voltageLevel graph labels is done at the end in order to
        // facilitate the move of a voltageLevel in the diagram
        for (Graph vlGraph : graph.getNodes()) {
            drawGraphLabel(prefixId, root, vlGraph, metadata);
        }

        document.adoptNode(root);
        document.getDocumentElement().appendChild(root);

        return metadata;
    }

    protected void drawSubstation(String prefixId,
                                  SubstationGraph graph,
                                  Element root,
                                  GraphMetadata metadata,
                                  DiagramInitialValueProvider initProvider,
                                  DiagramStyleProvider styleProvider,
                                  NodeLabelConfiguration nodeLabelConfiguration) {
        // Drawing the voltageLevel graphs
        for (Graph vlGraph : graph.getNodes()) {
            drawVoltageLevel(prefixId, vlGraph, root, metadata, initProvider, styleProvider, nodeLabelConfiguration);
        }

        AnchorPointProvider anchorPointProvider = (type, id) -> componentLibrary.getAnchorPoints(type);

        // Drawing the nodes outside the voltageLevel graphs (multi-terminal nodes)
        drawMultiTerminalNodes(prefixId, root, graph, metadata, styleProvider, anchorPointProvider);

        // Drawing the snake lines
        drawSnakeLines(prefixId, root, graph, metadata, anchorPointProvider);
    }

    /*
     * Drawing the grid lines (if required)
     */
    protected Element drawGrid(String prefixId, Graph graph, Document document, GraphMetadata metadata) {
        int maxH = graph.getNodeBuses().stream()
                .mapToInt(nodeBus -> nodeBus.getPosition().getH() + nodeBus.getPosition().getHSpan())
                .max().orElse(0);
        int maxV = graph.getNodeBuses().stream()
                .mapToInt(nodeBus -> nodeBus.getPosition().getV())
                .max().orElse(1) - 1;

        Element gridRoot = document.createElement("g");

        String gridId = prefixId + "GRID_" + graph.getVoltageLevelId();
        gridRoot.setAttribute("id", gridId);
        gridRoot.setAttribute(CLASS, DiagramStyles.GRID_STYLE_CLASS);
        gridRoot.setAttribute(TRANSFORM,
                TRANSLATE + "(" + layoutParameters.getTranslateX() + "," + layoutParameters.getTranslateY() + ")");
        // vertical lines
        for (int i = 0; i < maxH + 1; i++) {
            gridRoot.appendChild(drawGridVerticalLine(document, graph, maxV,
                    graph.getX() + layoutParameters.getInitialXBus() + i * layoutParameters.getCellWidth()));
        }

        // StackHeight Horizontal lines
        gridRoot.appendChild(drawGridHorizontalLine(document, graph, maxH,
                graph.getY() + layoutParameters.getInitialYBus() - layoutParameters.getStackHeight()));
        gridRoot.appendChild(drawGridHorizontalLine(document, graph, maxH,
                graph.getY() + layoutParameters.getInitialYBus() + layoutParameters.getStackHeight()
                        + layoutParameters.getVerticalSpaceBus() * maxV));

        // internCellHeight Horizontal lines
        gridRoot.appendChild(drawGridHorizontalLine(document, graph, maxH,
                graph.getY() + layoutParameters.getInitialYBus() - layoutParameters.getInternCellHeight()));
        gridRoot.appendChild(drawGridHorizontalLine(document, graph, maxH,
                graph.getY() + layoutParameters.getInitialYBus() + layoutParameters.getInternCellHeight()
                        + layoutParameters.getVerticalSpaceBus() * maxV));

        metadata.addNodeMetadata(new GraphMetadata.NodeMetadata(gridId,
                graph.getVoltageLevelId(),
                null,
                null,
                null,
                false,
                BusCell.Direction.UNDEFINED,
                false));

        return gridRoot;
    }

    protected Element drawGridHorizontalLine(Document document, Graph graph, int maxH, double y) {
        return drawGridLine(document,
                layoutParameters.getInitialXBus() + graph.getX(), y,
                layoutParameters.getInitialXBus() + maxH * layoutParameters.getCellWidth() + graph.getX(), y);
    }

    protected Element drawGridVerticalLine(Document document, Graph graph, int maxV, double x) {
        return drawGridLine(document,
                x, layoutParameters.getInitialYBus()
                        - layoutParameters.getStackHeight() - layoutParameters.getExternCellHeight() + graph.getY(),
                x, layoutParameters.getInitialYBus()
                        + layoutParameters.getStackHeight() + layoutParameters.getExternCellHeight()
                        + layoutParameters.getVerticalSpaceBus() * maxV + graph.getY());
    }

    protected Element drawGridLine(Document document, double x1, double y1, double x2, double y2) {
        Element line = document.createElement("line");
        line.setAttribute("x1", Double.toString(x1));
        line.setAttribute("x2", Double.toString(x2));
        line.setAttribute("y1", Double.toString(y1));
        line.setAttribute("y2", Double.toString(y2));
        return line;
    }

    /*
     * Drawing the voltageLevel graph nodes
     */
    protected void drawNodes(String prefixId,
                             Element root,
                             Graph graph,
                             GraphMetadata metadata,
                             AnchorPointProvider anchorPointProvider,
                             DiagramInitialValueProvider initProvider,
                             DiagramStyleProvider styleProvider,
                             NodeLabelConfiguration nodeLabelConfiguration,
                             Predicate<Node> predicate) {
        graph.getNodes().stream().filter(predicate).forEach(node -> {

            String nodeId = DiagramStyles.escapeId(prefixId + node.getId());
            Element g = root.getOwnerDocument().createElement("g");
            g.setAttribute("id", nodeId);

            g.setAttribute(CLASS, node.getComponentType() + " " + nodeId);

            if (node.getType() == Node.NodeType.BUS) {
                drawBus((BusNode) node, g);
            } else {
                incorporateComponents(prefixId, node, g, styleProvider);
            }

            BusCell.Direction direction = (node instanceof FeederNode && node.getCell() != null) ? ((ExternCell) node.getCell()).getDirection() : BusCell.Direction.UNDEFINED;

            if (!node.isFictitious()) {
                drawNodeLabel(prefixId, g, node, initProvider, nodeLabelConfiguration);
            }
            root.appendChild(g);

            setMetadata(metadata, node, nodeId, graph, direction, anchorPointProvider);
        });
    }

    protected void setMetadata(GraphMetadata metadata, Node node, String nodeId, Graph graph, BusCell.Direction direction, AnchorPointProvider anchorPointProvider) {
        String nextVId = null;
        if (node instanceof FeederBranchNode) {
            nextVId = ((FeederBranchNode) node).getVIdOtherSide();
        }

        metadata.addNodeMetadata(
                new GraphMetadata.NodeMetadata(nodeId, graph != null ? graph.getVoltageLevelId() : "", nextVId,
                        node.getComponentType(), node.getRotationAngle(),
                        node.isOpen(), direction, false));
        if (node.getType() == Node.NodeType.BUS) {
            metadata.addComponentMetadata(new ComponentMetadata(BUSBAR_SECTION,
                    nodeId,
                    anchorPointProvider.getAnchorPoints(BUSBAR_SECTION, node.getId()),
                    new ComponentSize(0, 0), true, null));
        } else {
            if (metadata.getComponentMetadata(node.getComponentType()) == null) {
                metadata.addComponentMetadata(new ComponentMetadata(node.getComponentType(),
                        null,
                        componentLibrary.getAnchorPoints(node.getComponentType()),
                        componentLibrary.getSize(node.getComponentType()), true, null));
            }
        }
    }

    protected void drawNodeLabel(String prefixId, Element g, Node node,
                                 DiagramInitialValueProvider initProvider,
                                 NodeLabelConfiguration nodeLabelConfiguration) {
        List<String> labelsNode = initProvider.getNodeLabelValue(node);
        List<LabelPosition> labelsPosition = nodeLabelConfiguration.getLabelsPosition(node);

        if (labelsPosition.size() != labelsNode.size()) {
            throw new AssertionError("Number of node labels <> Number of labels positions");
        }

        for (int i = 0; i < labelsNode.size(); ++i) {
            drawLabel(prefixId + labelsPosition.get(i).getPositionName(), labelsNode.get(i), node.isRotated(),
                    labelsPosition.get(i).getdX(), labelsPosition.get(i).getdY(),
                    g, FONT_SIZE);
        }
    }

    /*
     * Drawing the graph label
     */
    protected void drawGraphLabel(String prefixId, Element root, Graph graph, GraphMetadata metadata) {
        // drawing the label of the voltageLevel
        String idLabelVoltageLevel = prefixId + "LABEL_VL_" + graph.getVoltageLevelId();
        Element gLabel = root.getOwnerDocument().createElement("g");
        gLabel.setAttribute("id", idLabelVoltageLevel);

        double yPos = graph.getY() + layoutParameters.getInitialYBus()
                - (!layoutParameters.isAdaptCellHeightToContent()
                ? layoutParameters.getExternCellHeight()
                : graph.getMaxCalculatedCellHeight(BusCell.Direction.TOP))
                - 20.;
        drawLabel(null, graph.isUseName()
                     ? graph.getVoltageLevelName()
                     : graph.getVoltageLevelId(),
                  false, graph.getX(), yPos, gLabel, FONT_VOLTAGE_LEVEL_LABEL_SIZE);
        root.appendChild(gLabel);

        metadata.addNodeMetadata(new GraphMetadata.NodeMetadata(idLabelVoltageLevel,
                graph.getVoltageLevelId(),
                null,
                null,
                null,
                false,
                BusCell.Direction.UNDEFINED,
                true));
    }

    /*
     * Drawing the voltageLevel graph busbar sections
     */
    protected void drawBus(BusNode node, Element g) {
        Element line = g.getOwnerDocument().createElement("line");
        line.setAttribute("x1", "0");
        line.setAttribute("y1", "0");
        if (node.isRotated()) {
            line.setAttribute("x2", "0");
            line.setAttribute("y2", String.valueOf(node.getPxWidth()));
        } else {
            line.setAttribute("x2", String.valueOf(node.getPxWidth()));
            line.setAttribute("y2", "0");
        }

        g.appendChild(line);

        g.setAttribute(TRANSFORM, TRANSLATE + "(" + (layoutParameters.getTranslateX() + node.getX()) + ","
                + (layoutParameters.getTranslateY() + node.getY()) + ")");
    }

    /*
     * Drawing the voltageLevel graph busbar section names and feeder names
     */
    protected void drawLabel(String idLabel, String str, boolean rotated, double xShift, double yShift, Element g,
                             int fontSize) {
        Element label = g.getOwnerDocument().createElement("text");
        if (!StringUtils.isEmpty(idLabel)) {
            label.setAttribute("id", idLabel);
        }
        label.setAttribute("x", String.valueOf(xShift));
        label.setAttribute("y", String.valueOf(yShift));
        label.setAttribute("font-family", FONT_FAMILY);
        label.setAttribute("font-size", Integer.toString(fontSize));
        label.setAttribute(CLASS, DiagramStyles.LABEL_STYLE_CLASS);
        Text text = g.getOwnerDocument().createTextNode(str);
        label.setAttribute(TRANSFORM, ROTATE + "(" + (rotated ? -90 : 0) + "," + 0 + "," + 0 + ")");
        label.appendChild(text);
        g.appendChild(label);
    }

    protected boolean canInsertComponentSVG(Node node) {
        return (!node.isFictitious() && node.getType() != Node.NodeType.SHUNT)
                || (node.isFictitious()
                && node.getComponentType().equals(THREE_WINDINGS_TRANSFORMER)
                || node.getComponentType().equals(TWO_WINDINGS_TRANSFORMER)
                || node.getComponentType().equals(NODE));
    }

    protected void incorporateComponents(String prefixId, Node node, Element g, DiagramStyleProvider styleProvider) {
        Map<String, SVGOMDocument> subComponents = componentLibrary.getSvgDocument(node.getComponentType());
        transformComponent(node, g);
        if (subComponents != null && canInsertComponentSVG(node)) {
            String componentDefsId = node.getComponentType();
            if (node.getComponentType().equals(BREAKER)
                    || node.getComponentType().equals(DISCONNECTOR)) {
                componentDefsId += node.isOpen() ? "-open" : "-closed";
            }
            insertComponentSVGIntoDocumentSVG(prefixId, subComponents, g, node, styleProvider, componentDefsId, false);
        }
    }

    private void handleNodeRotation(Node node) {
        if (node.getGraph() != null) { // node in voltage level graph
            if (node instanceof Fictitious3WTNode && node.getCell() != null && ((ExternCell) node.getCell()).getDirection() == BusCell.Direction.BOTTOM) {
                node.setRotationAngle(180.);  // rotation if 3WT cell direction is BOTTOM
            }
        } else {  // node outside any graph
            List<Node> adjacentNodes = node.getAdjacentNodes();
            adjacentNodes.sort(Comparator.comparingDouble(Node::getX));
            if (adjacentNodes.size() == 2) {  // 2 windings transformer
                List<Edge> edges = node.getAdjacentEdges();
                List<Double> pol1 = ((TwtEdge) edges.get(0)).getSnakeLine();
                List<Double> pol2 = ((TwtEdge) edges.get(1)).getSnakeLine();
                double x1 = pol1.get(pol1.size() - 4); // absciss of the first polyline second last point
                double x2 = pol2.get(2);  // absciss of the second polyline third point
                if (x1 == x2) {
                    node.setRotationAngle(90.);  // rotation if points abscisses are the same
                }
            } else {  // 3 windings transformer
                Node n2 = adjacentNodes.get(1);
                if (n2.getCell() != null && ((ExternCell) n2.getCell()).getDirection() == BusCell.Direction.BOTTOM) {
                    node.setRotationAngle(180.);  // rotation if middle node cell orientation is BOTTOM
                }
            }
        }
    }

    protected void insertComponentSVGIntoDocumentSVG(String prefixId,
                                                     Map<String, SVGOMDocument> subComponents,
                                                     Element g, Node node,
                                                     DiagramStyleProvider styleProvider,
                                                     String componentDefsId,
                                                     boolean forArrow) {
        ComponentSize size = componentLibrary.getSize(node.getComponentType());

        handleNodeRotation(node);

        if (!layoutParameters.isAvoidSVGComponentsDuplication()) {
            // The following code work correctly considering SVG part describing the component is the first child of the SVGDocument.
            // If SVG are written otherwise, it will not work correctly.
            for (Map.Entry<String, SVGOMDocument> subComponent : subComponents.entrySet()) {
                String nameSubComponent = subComponent.getKey();
                SVGOMDocument svgSubComponent = subComponent.getValue();

                for (int i = 0; i < svgSubComponent.getChildNodes().item(0).getChildNodes().getLength(); i++) {
                    org.w3c.dom.Node n = svgSubComponent.getChildNodes().item(0).getChildNodes().item(i).cloneNode(true);

                    if (n instanceof SVGElement) {
                        if (!(node instanceof SwitchNode) && node.isRotated()) {
                            ((Element) n).setAttribute(TRANSFORM, ROTATE + "(" + node.getRotationAngle() + "," + size.getWidth() / 2 + "," + size.getHeight() / 2 + ")");
                        }

                        Map<String, String> svgStyle = styleProvider.getNodeSVGStyle(node, size, nameSubComponent, layoutParameters.isShowInternalNodes());
                        svgStyle.forEach(((Element) n)::setAttribute);
                    }

                    // Adding prefixId and node id before id of n : to ensure unicity of ids
                    if (n.getAttributes() != null) {
                        org.w3c.dom.Node nodeId = n.getAttributes().getNamedItem("id");
                        if (nodeId != null) {
                            String nodeIdValue = nodeId.getTextContent();
                            String gIdValue = StringUtils.removeStart(g.getAttribute("id"), prefixId);
                            nodeId.setTextContent(prefixId + gIdValue + "_" + nodeIdValue);
                        }
                    }

                    g.getOwnerDocument().adoptNode(n);
                    g.appendChild(n);
                }
            }
        } else {
            // Adding <use> markup to reuse the svg defined in the <defs> part
            String prefixHref = "#" + componentDefsId;
            String componentType = !forArrow ? node.getComponentType() : ARROW;

            Map<String, SVGOMDocument> subCmps = componentLibrary.getSvgDocument(componentType);
            if (subCmps != null) {
                Set<String> subCmpsName = subCmps.keySet();
                subCmpsName.forEach(s -> {
                    Element eltUse = g.getOwnerDocument().createElement("use");
                    eltUse.setAttribute("href", subCmpsName.size() > 1 ? prefixHref + "-" + s : prefixHref);

                    if (!(node instanceof SwitchNode) && node.isRotated()) {
                        eltUse.setAttribute(TRANSFORM, ROTATE + "(" + node.getRotationAngle() + "," + size.getWidth() / 2 + "," + size.getHeight() / 2 + ")");
                    }

                    Map<String, String> svgStyle = styleProvider.getNodeSVGStyle(node, size, s, layoutParameters.isShowInternalNodes());
                    svgStyle.forEach(eltUse::setAttribute);

                    g.getOwnerDocument().adoptNode(eltUse);
                    g.appendChild(eltUse);
                });
            }
        }
    }

    protected void insertRotatedComponentSVGIntoDocumentSVG(String prefixId,
                                                            Map<String, SVGOMDocument> subComponents,
                                                            Element g, double angle,
                                                            double cx, double cy,
                                                            String componentDefsId) {
        if (!layoutParameters.isAvoidSVGComponentsDuplication()) {
            // The following code work correctly considering SVG part describing the component is the first child of the SVGDocument.
            // If SVG are written otherwise, it will not work correctly.
            if (subComponents != null) {
                for (Map.Entry<String, SVGOMDocument> subComponent : subComponents.entrySet()) {
                    SVGOMDocument svgSubComponent = subComponent.getValue();

                    for (int i = 0; i < svgSubComponent.getChildNodes().item(0).getChildNodes().getLength(); i++) {
                        org.w3c.dom.Node n = svgSubComponent.getChildNodes().item(0).getChildNodes().item(i).cloneNode(true);
                        if (n.getNodeName().equals("g") && n.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                            Element e = (Element) n;
                            e.setAttribute(TRANSFORM, ROTATE + "(" + angle + "," + cx + "," + cy + ")");
                        }

                        // Adding prefixId and g id before id of n : to ensure unicity of ids
                        if (n.getAttributes() != null) {
                            org.w3c.dom.Node nodeId = n.getAttributes().getNamedItem("id");
                            if (nodeId != null) {
                                String nodeIdValue = nodeId.getTextContent();
                                String gIdValue = StringUtils.removeStart(g.getAttribute("id"), prefixId);
                                if (StringUtils.isEmpty(prefixId)) {
                                    nodeId.setTextContent(gIdValue + "_" + nodeIdValue);
                                } else {
                                    nodeId.setTextContent(prefixId + gIdValue + "_" + nodeIdValue);
                                }
                            }
                        }

                        g.getOwnerDocument().adoptNode(n);
                        g.appendChild(n);
                    }
                }
            }
        } else {
            // Adding <use> markup to reuse the svg defined in the <defs> part
            Element eltUse = g.getOwnerDocument().createElement("use");
            eltUse.setAttribute("href", "#" + componentDefsId);
            eltUse.setAttribute(TRANSFORM, ROTATE + "(" + angle + "," + cx + "," + cy + ")");

            g.getOwnerDocument().adoptNode(eltUse);
            g.appendChild(eltUse);
        }
    }

    protected void transformComponent(Node node, Element g) {
        ComponentSize componentSize = componentLibrary.getSize(node.getComponentType());

        // For a node marked for rotation during the graph building, but with an svg component not allowed
        // to rotate (ex : disconnector in SVG component library), we cancel the rotation
        if (node.isRotated() && !componentLibrary.isAllowRotation(node.getComponentType())) {
            node.setRotationAngle(null);
        }

        if (!node.isRotated()) {
            g.setAttribute(TRANSFORM,
                    TRANSLATE + "(" + (layoutParameters.getTranslateX() + node.getX() - componentSize.getWidth() / 2) + ","
                            + (layoutParameters.getTranslateY() + node.getY() - componentSize.getHeight() / 2) + ")");
            return;
        }

/*
        afester javafx library does not handle more than one transformation, yet, so
        combine the couple of transformations, translation+rotation, in a single matrix transformation
*/
        int precision = 4;

        double angle = Math.toRadians(90);
        double cosRo = Math.cos(angle);
        double sinRo = Math.sin(angle);
        double cdx = componentSize.getWidth() / 2;
        double cdy = componentSize.getHeight() / 2;

        double e1 = layoutParameters.getTranslateX() - cdx * cosRo + cdy * sinRo + node.getX();
        double f1 = layoutParameters.getTranslateY() - cdx * sinRo - cdy * cosRo + node.getY();

        g.setAttribute(TRANSFORM,
                "matrix(" + Precision.round(cosRo, precision) + "," + Precision.round(sinRo, precision)
                        + "," + Precision.round(-sinRo, precision) + "," + Precision.round(cosRo,
                        precision) + ","
                        + Precision.round(e1, precision) + "," + Precision.round(f1, precision) + ")");
    }

    protected void transformArrow(List<Double> points, ComponentSize componentSize, double shift, Element g) {

        double x1 = points.get(0);
        double y1 = points.get(1);
        double x2 = points.get(2);
        double y2 = points.get(3);

        if (points.size() > 4 && Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1)) < 3 * componentSize.getHeight()) {
            double x3 = points.get(4);
            double y3 = points.get(5);
            if (Math.sqrt((x3 - x2) * (x3 - x2) + (y3 - y2) * (y3 - y2)) > 3 * componentSize.getHeight()) {
                x1 = x2;
                y1 = y2;
                x2 = x3;
                y2 = y3;
            }
        }
        double dx = x2 - x1;
        double dy = y2 - y1;

        double angle = Math.atan(dx / dy);
        if (!Double.isNaN(angle)) {
            double cosRo = Math.cos(angle);
            double sinRo = Math.sin(angle);
            double cdx = componentSize.getWidth() / 2;
            double cdy = componentSize.getHeight() / 2;

            double dist = this.layoutParameters.getArrowDistance();

            double x = x1 + sinRo * (dist + shift);
            double y = y1 + cosRo * (y1 > y2 ? -(dist + shift) : (dist + shift));

            double e1 = layoutParameters.getTranslateX() - cdx * cosRo + cdy * sinRo + x;
            double f1 = layoutParameters.getTranslateY() - cdx * sinRo - cdy * cosRo + y;

            int precision = 4;
            g.setAttribute(TRANSFORM,
                    "matrix(" + Precision.round(cosRo, precision) + "," + Precision.round(sinRo, precision)
                            + "," + Precision.round(-sinRo, precision) + "," + Precision.round(cosRo,
                            precision) + ","
                            + Precision.round(e1, precision) + "," + Precision.round(f1, precision) + ")");
        }
    }

    protected void insertArrowsAndLabels(String prefixId,
                                         String wireId,
                                         List<Double> points,
                                         Element root,
                                         Node n,
                                         GraphMetadata metadata,
                                         DiagramInitialValueProvider initProvider,
                                         DiagramStyleProvider styleProvider) {
        InitialValue init = initProvider.getInitialValue(n);
        ComponentMetadata cd = metadata.getComponentMetadata(ARROW);
        Map<String, SVGOMDocument> arr = componentLibrary.getSvgDocument(ARROW);

        double shX = cd.getSize().getWidth() + LABEL_OFFSET;
        double shY = cd.getSize().getHeight() - LABEL_OFFSET + (double) FONT_SIZE / 2;

        String defsId = ARROW;
        double y1 = points.get(1);
        double y2 = points.get(3);

        Optional<String> label1 = init.getLabel1();

        if (label1.isPresent()) {  // we draw the arrow only if value 1 is present
            Element g1 = root.getOwnerDocument().createElement("g");
            String arrow1WireId = wireId + "_ARROW1";
            g1.setAttribute("id", arrow1WireId);
            transformArrow(points, cd.getSize(), 0, g1);

            Optional<Direction> dir1 = init.getArrowDirection1();
            if (dir1.isPresent()) {
                defsId += dir1.get() == Direction.UP ? "-arrow-up" : "-arrow-down";
            }

            if (y1 > y2) {
                insertRotatedComponentSVGIntoDocumentSVG(prefixId, arr, g1, 180, cd.getSize().getWidth() / 2, cd.getSize().getHeight() / 2, defsId);
            } else {
                insertComponentSVGIntoDocumentSVG(prefixId, arr, g1, n, styleProvider, defsId, true);
            }
            drawLabel(null, label1.get(), false, shX, shY, g1, FONT_SIZE);

            if (dir1.isPresent()) {
                g1.setAttribute(CLASS, "ARROW1_" + escapeClassName(n.getId()) + "_" + dir1.get());
                if (layoutParameters.isAvoidSVGComponentsDuplication()) {
                    styleProvider.getAttributesArrow(1).forEach(((Element) g1.getFirstChild())::setAttribute);
                }
            }

            Optional<String> label3 = init.getLabel3();
            label3.ifPresent(s -> drawLabel(null, s, false, -(s.length() * (double) FONT_SIZE / 2 + LABEL_OFFSET), shY, g1, FONT_SIZE));

            root.appendChild(g1);
            metadata.addArrowMetadata(new ArrowMetadata(arrow1WireId, wireId, layoutParameters.getArrowDistance()));
        }

        Optional<String> label2 = init.getLabel2();
        if (label2.isPresent()) {  // we draw the arrow only if value 2 is present
            Element g2 = root.getOwnerDocument().createElement("g");
            String arrow2WireId = wireId + "_ARROW2";
            g2.setAttribute("id", arrow2WireId);
            transformArrow(points, cd.getSize(), 2 * cd.getSize().getHeight(), g2);

            defsId = ARROW;
            Optional<Direction> dir2 = init.getArrowDirection2();
            if (dir2.isPresent()) {
                defsId += dir2.get() == Direction.UP ? "-arrow-up" : "-arrow-down";
            }

            if (y1 > y2) {
                insertRotatedComponentSVGIntoDocumentSVG(prefixId, arr, g2, 180, 5, 5, defsId);
            } else {
                insertComponentSVGIntoDocumentSVG(prefixId, arr, g2, n, styleProvider, defsId, true);
            }
            drawLabel(null, label2.get(), false, shX, shY, g2, FONT_SIZE);

            if (dir2.isPresent()) {
                g2.setAttribute(CLASS, "ARROW2_" + escapeClassName(n.getId()) + "_" + dir2.get());
                if (layoutParameters.isAvoidSVGComponentsDuplication()) {
                    styleProvider.getAttributesArrow(2).forEach(((Element) g2.getFirstChild())::setAttribute);
                }
            }

            Optional<String> label4 = init.getLabel4();
            label4.ifPresent(s -> drawLabel(null, s, false, -(s.length() * (double) FONT_SIZE / 2 + LABEL_OFFSET), shY, g2, FONT_SIZE));

            root.appendChild(g2);
            metadata.addArrowMetadata(new ArrowMetadata(arrow2WireId, wireId, layoutParameters.getArrowDistance()));
        }
    }

    /*
     * Drawing the voltageLevel graph edges
     */
    protected void drawEdges(String prefixId, Element root, Graph graph, GraphMetadata metadata, AnchorPointProvider anchorPointProvider, DiagramInitialValueProvider initProvider, DiagramStyleProvider styleProvider) {
        String vId = graph.getVoltageLevelId();

        for (Edge edge : graph.getEdges()) {
            // for unicity purpose (in substation diagram), we prefix the id of the WireMetadata with the voltageLevel id and "_"
            String wireId = escapeId(prefixId + vId + "_Wire" + graph.getEdges().indexOf(edge));

            Element g = root.getOwnerDocument().createElement(POLYLINE);
            g.setAttribute("id", wireId);

            WireConnection anchorPoints = WireConnection.searchBetterAnchorPoints(anchorPointProvider, edge.getNode1(), edge.getNode2());

            // Determine points of the polyline
            List<Double> pol = anchorPoints.calculatePolylinePoints(edge.getNode1(), edge.getNode2(),
                    layoutParameters.isDrawStraightWires());

            g.setAttribute(POINTS, pointsListToString(pol));
            g.setAttribute(CLASS, WIRE_STYLE_CLASS + " " + styleProvider.getIdWireStyle(edge));
            root.appendChild(g);

            metadata.addWireMetadata(new GraphMetadata.WireMetadata(wireId,
                    escapeId(edge.getNode1().getId()),
                    escapeId(edge.getNode2().getId()),
                    layoutParameters.isDrawStraightWires(),
                    false));

            if (metadata.getComponentMetadata(ARROW) == null) {
                metadata.addComponentMetadata(new ComponentMetadata(ARROW,
                        null,
                        componentLibrary.getAnchorPoints(ARROW),
                        componentLibrary.getSize(ARROW), true, null));
            }

            if (edge.getNode1() instanceof FeederNode) {
                if (!(edge.getNode2() instanceof FeederNode)) {
                    insertArrowsAndLabels(prefixId, wireId, pol, root, edge.getNode1(), metadata, initProvider, styleProvider);
                }
            } else if (edge.getNode2() instanceof FeederNode) {
                List<Double> reversePoints = new ArrayList();

                for (int i = pol.size() - 1; i >= 0; i--) {
                    if (i % 2 == 0) {
                        reversePoints.add(pol.get(i));
                        reversePoints.add(pol.get(i + 1));
                    }
                }

                insertArrowsAndLabels(prefixId, wireId, reversePoints, root, edge.getNode2(), metadata, initProvider, styleProvider);
            }
        }
    }

    /*
     * Drawing the substation graph edges (snakelines between voltageLevel diagram)
     */
    protected void drawSnakeLines(String prefixId, Element root, SubstationGraph graph,
                                  GraphMetadata metadata, AnchorPointProvider anchorPointProvider) {
        for (TwtEdge edge : graph.getEdges()) {
            Graph g1 = edge.getNode1().getGraph();
            Graph g2 = edge.getNode2().getGraph();

            if (g1 == null && g2 == null) {
                throw new AssertionError("Edge between two nodes outside any graph");
            }
            if (g1 != null && g2 != null) {
                throw new AssertionError("One node must be outside any graph");
            }

            String tmp = g1 != null ? g1.getVoltageLevelId() : "_";
            tmp += g2 != null ? g2.getVoltageLevelId() : "_";

            String wireId = escapeId(prefixId + tmp + "_" + "Wire" + graph.getEdges().indexOf(edge));
            Element g = root.getOwnerDocument().createElement(POLYLINE);
            g.setAttribute("id", wireId);

            // Get the points of the snakeLine, already calculated during the layout application
            List<Double> pol = edge.getSnakeLine();
            if (!pol.isEmpty()) {
                adaptCoordSnakeLine(anchorPointProvider, edge, pol);
            }

            g.setAttribute(POINTS, pointsListToString(pol));

            String vId = g1 != null ? g1.getVoltageLevelId() : g2.getVoltageLevelId();

            g.setAttribute(CLASS, DiagramStyles.WIRE_STYLE_CLASS + " " + DiagramStyles.WIRE_STYLE_CLASS + "_" + escapeClassName(vId));
            root.appendChild(g);

            metadata.addWireMetadata(new GraphMetadata.WireMetadata(wireId,
                    escapeId(edge.getNode1().getId()),
                    escapeId(edge.getNode2().getId()),
                    layoutParameters.isDrawStraightWires(),
                    true));

            if (metadata.getComponentMetadata(ARROW) == null) {
                metadata.addComponentMetadata(new ComponentMetadata(ARROW,
                        null,
                        componentLibrary.getAnchorPoints(ARROW),
                        componentLibrary.getSize(ARROW), true, null));
            }
        }
    }

    /*
     * Adaptation of the previously calculated snakeLine points, in order to use the anchor points
     * of the node outside any graph
     */
    private void adaptCoordSnakeLine(AnchorPointProvider anchorPointProvider, TwtEdge edge, List<Double> pol) {
        Node n1 = edge.getNode1();
        Node n2 = edge.getNode2();

        Graph g1 = n1.getGraph();
        Graph g2 = n2.getGraph();

        // Getting the right polyline point from where we need to compute the best anchor point
        double x;
        double y;
        if (g2 == null) {
            if (pol.size() <= 4) {
                x = pol.get(0);
                y = pol.get(1);
            } else if (pol.size() <= 6) {
                x = pol.get(2);
                y = pol.get(3);
            } else {
                x = pol.get(pol.size() - 4);
                y = pol.get(pol.size() - 3);
            }
        } else {
            x = pol.get(2);
            y = pol.get(3);
        }

        WireConnection wireC = WireConnection.searchBetterAnchorPoints(anchorPointProvider, g1 == null ? n1 : n2, x, y);
        AnchorPoint anc1 = wireC.getAnchorPoint1();

        int n = pol.size();

        // Replacing the right points coordinates in the original polyline
        if (g2 == null) {
            double xOld = pol.get(n - 2);
            double yOld = pol.get(n - 1);
            pol.set(n - 2, xOld + anc1.getX());
            pol.set(n - 1, yOld + anc1.getY());
            if (xOld == x) {
                pol.set(n - 4, xOld + anc1.getX());
            } else {
                pol.set(n - 3, yOld + anc1.getY());
            }
        } else {
            double xOld = pol.get(0);
            double yOld = pol.get(1);
            pol.set(0, xOld + anc1.getX());
            pol.set(1, yOld + anc1.getY());
            if (xOld == x) {
                pol.set(2, xOld + anc1.getX());
            } else {
                pol.set(3, yOld + anc1.getY());
            }
        }
    }

    protected String pointsListToString(List<Double> pol) {
        return IntStream.range(0, pol.size())
                .mapToObj(n -> n % 2 == 0 ? pol.get(n) + layoutParameters.getTranslateX() : pol.get(n) + layoutParameters.getTranslateY())
                .map(Object::toString)
                .collect(Collectors.joining(","));
    }


    /**
     * Creation of the defs area for the SVG components
     */
    protected void createDefsSVGComponents(Document document, Set<String> listUsedComponentSVG) {
        if (layoutParameters.isAvoidSVGComponentsDuplication()) {
            listUsedComponentSVG.add(ARROW);  // adding also arrows

            Element defs = document.createElement("defs");

            listUsedComponentSVG.forEach(c -> {
                Map<String, SVGOMDocument> subComponents = componentLibrary.getSvgDocument(c);
                if (subComponents != null) {
                    Element group = document.createElement("g");
                    group.setAttribute("id", c);

                    insertSVGComponentIntoDefsArea(group, subComponents);

                    defs.getOwnerDocument().adoptNode(group);
                    defs.appendChild(group);
                }
            });

            document.adoptNode(defs);
            document.getDocumentElement().appendChild(defs);
        }
    }

    protected void insertSVGComponentIntoDefsArea(Element group, Map<String, SVGOMDocument> subComponents) {
        for (SVGOMDocument subComponent : subComponents.values()) {
            for (int i = 0; i < subComponent.getChildNodes().item(0).getChildNodes().getLength(); i++) {
                org.w3c.dom.Node n = subComponent.getChildNodes().item(0).getChildNodes().item(i).cloneNode(true);
                group.getOwnerDocument().adoptNode(n);
                group.appendChild(n);
            }
        }
    }

    @Override
    public GraphMetadata write(String prefixId,
                               ZoneGraph graph,
                               DiagramInitialValueProvider initProvider,
                               DiagramStyleProvider styleProvider,
                               NodeLabelConfiguration nodeLabelConfiguration,
                               Path svgFile) {
        try (Writer writer = Files.newBufferedWriter(svgFile)) {
            return write(prefixId, graph, initProvider, styleProvider, nodeLabelConfiguration, writer);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public GraphMetadata write(String prefixId,
                               ZoneGraph graph,
                               DiagramInitialValueProvider initProvider,
                               DiagramStyleProvider styleProvider,
                               NodeLabelConfiguration nodeLabelConfiguration,
                               Writer writer) {
        DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();

        Document document = domImpl.createDocument(SVG_NAMESPACE, SVG_QUALIFIED_NAME, null);

        List<Graph> vlGraphs = graph.getNodes().stream().map(SubstationGraph::getNodes).flatMap(Collection::stream).collect(Collectors.toList());

        Set<String> listUsedComponentSVG = new HashSet<>();

        addStyle(document, styleProvider, vlGraphs, listUsedComponentSVG,
                graph.getNodes().stream().map(SubstationGraph::getEdges).flatMap(Collection::stream).collect(Collectors.toList()),
                graph.getEdges());

        createDefsSVGComponents(document, listUsedComponentSVG);

        GraphMetadata metadata = writegraph(prefixId, graph, vlGraphs, document, initProvider, styleProvider, nodeLabelConfiguration);

        transformDocument(document, writer);

        return metadata;
    }

    private GraphMetadata writegraph(String prefixId,
                                     ZoneGraph graph,
                                     List<Graph> vlGraphs,
                                     Document document,
                                     DiagramInitialValueProvider initProvider,
                                     DiagramStyleProvider styleProvider,
                                     NodeLabelConfiguration nodeLabelConfiguration) {
        GraphMetadata metadata = new GraphMetadata();

        Element root = document.createElement("g");

        // Drawing grid lines
        if (layoutParameters.isShowGrid()) {
            for (Graph vlGraph : vlGraphs) {
                if (vlGraph.isPositionNodeBusesCalculated()) {
                    root.appendChild(drawGrid(prefixId, vlGraph, document, metadata));
                }
            }
        }

        drawZone(prefixId, graph, root, metadata, initProvider, styleProvider, nodeLabelConfiguration);

        // the drawing of the voltageLevel graph labels is done at the end in order to
        // facilitate the move of a voltageLevel in the diagram
        for (Graph vlGraph : vlGraphs) {
            drawGraphLabel(prefixId, root, vlGraph, metadata);
        }

        document.adoptNode(root);
        document.getDocumentElement().appendChild(root);

        return metadata;
    }

    private void drawZone(String prefixId,
                          ZoneGraph graph,
                          Element root,
                          GraphMetadata metadata,
                          DiagramInitialValueProvider initProvider,
                          DiagramStyleProvider styleProvider,
                          NodeLabelConfiguration nodeLabelConfiguration) {
        for (SubstationGraph sGraph : graph.getNodes()) {
            drawSubstation(prefixId, sGraph, root, metadata, initProvider, styleProvider, nodeLabelConfiguration);
        }

        drawLines(prefixId, root, graph, metadata);
    }

    private void drawLines(String prefixId, Element root, ZoneGraph graph, GraphMetadata metadata) {
        for (LineEdge edge : graph.getEdges()) {

            String tmp = edge.getNode1().getGraph().getVoltageLevelId() + "_" + edge.getNode2().getGraph().getVoltageLevelId();
            String lineId = escapeId(prefixId + tmp + "_" + "Wire" + graph.getEdges().indexOf(edge));

            Element g = root.getOwnerDocument().createElement(POLYLINE);
            g.setAttribute("id", lineId);
            String polyline = edge.getPoints()
                                  .stream()
                                  .map(point -> (point.getX() + layoutParameters.getTranslateX()) + "," + (point.getY() + layoutParameters.getTranslateY()))
                                  .collect(Collectors.joining(","));
            g.setAttribute(POINTS, polyline);
            g.setAttribute(CLASS, DiagramStyles.LINE_STYLE_CLASS + " " + DiagramStyles.LINE_STYLE_CLASS + "_" + escapeClassName(edge.getNode1().getGraph().getVoltageLevelId()));

            root.appendChild(g);

            metadata.addLineMetadata(new GraphMetadata.LineMetadata(lineId,
                    escapeId(edge.getNode1().getId()),
                    escapeId(edge.getNode2().getId())));
        }
    }

    /*
     * Drawing the multi-terminal nodes
     */
    protected void drawMultiTerminalNodes(String prefixId,
                                          Element root,
                                          SubstationGraph graph,
                                          GraphMetadata metadata,
                                          DiagramStyleProvider styleProvider,
                                          AnchorPointProvider anchorPointProvider) {
        graph.getMultiTermNodes().stream().forEach(node -> {

            String nodeId = DiagramStyles.escapeId(prefixId + node.getId());
            Element g = root.getOwnerDocument().createElement("g");
            g.setAttribute("id", nodeId);

            g.setAttribute(CLASS, node.getComponentType() + " " + nodeId);

            incorporateComponents(prefixId, node, g, styleProvider);

            root.appendChild(g);

            setMetadata(metadata, node, nodeId, null, BusCell.Direction.UNDEFINED, anchorPointProvider);
        });
    }
}
