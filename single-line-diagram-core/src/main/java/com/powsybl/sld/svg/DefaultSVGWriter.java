/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.svg;

import com.powsybl.sld.layout.LayoutParameters;
import com.powsybl.sld.library.*;
import com.powsybl.sld.model.Node;
import com.powsybl.sld.model.*;
import com.powsybl.sld.svg.DiagramLabelProvider.Direction;
import com.powsybl.sld.svg.GraphMetadata.ArrowMetadata;
import com.powsybl.sld.util.DomUtil;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Precision;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.powsybl.sld.library.ComponentTypeName.*;
import static com.powsybl.sld.model.Position.Dimension.H;
import static com.powsybl.sld.model.Position.Dimension.V;
import static com.powsybl.sld.svg.DiagramStyles.*;

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

    protected static final String GROUP = "g";
    protected static final String CLASS = "class";
    protected static final String STYLE = "style";
    protected static final String TRANSFORM = "transform";
    protected static final String TRANSLATE = "translate";
    protected static final String ROTATE = "rotate";
    protected static final double LABEL_OFFSET = 5d;
    protected static final String POLYLINE = "polyline";
    protected static final String POINTS = "points";
    protected static final String TEXT_ANCHOR = "text-anchor";
    protected static final String MIDDLE = "middle";
    protected static final int CIRCLE_RADIUS_NODE_INFOS_SIZE = 10;

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
                               VoltageLevelGraph graph,
                               DiagramLabelProvider initProvider,
                               DiagramStyleProvider styleProvider,
                               Path svgFile) {
        try (Writer writer = Files.newBufferedWriter(svgFile)) {
            return write(prefixId, graph, initProvider, styleProvider, writer);
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
                               VoltageLevelGraph graph,
                               DiagramLabelProvider labelProvider,
                               DiagramStyleProvider styleProvider,
                               Writer writer) {
        DOMImplementation domImpl = DomUtil.getDocumentBuilder().getDOMImplementation();

        Document document = domImpl.createDocument(SVG_NAMESPACE, SVG_QUALIFIED_NAME, null);

        Set<String> listUsedComponentSVG = new HashSet<>();
        addStyle(document, styleProvider, labelProvider, Collections.singletonList(graph), listUsedComponentSVG);

        createDefsSVGComponents(document, listUsedComponentSVG);

        GraphMetadata metadata = writeGraph(prefixId, graph, document, labelProvider, styleProvider);

        DomUtil.transformDocument(document, writer);

        return metadata;
    }

    protected void addStyle(Document document, DiagramStyleProvider styleProvider, DiagramLabelProvider labelProvider,
                            List<VoltageLevelGraph> graphs, Set<String> listUsedComponentSVG) {
        Element style = document.createElement(STYLE);

        graphs.stream().flatMap(g -> g.getNodes().stream()).forEach(n -> {
            listUsedComponentSVG.add(n.getComponentType());
            List<DiagramLabelProvider.NodeDecorator> nodeDecorators = labelProvider.getNodeDecorators(n);
            if (nodeDecorators != null) {
                nodeDecorators.forEach(nodeDecorator -> listUsedComponentSVG.add(nodeDecorator.getType()));
            }
        });

        if (layoutParameters.isCssInternal()) {
            List<URL> urls = styleProvider.getCssUrls();
            urls.addAll(componentLibrary.getCssUrls());
            style.appendChild(getCdataSection(document, urls));
        } else {
            styleProvider.getCssFilenames().forEach(name -> addStyleImportTextNode(document, style, name));
            componentLibrary.getCssFilenames().forEach(name -> addStyleImportTextNode(document, style, name));
        }

        document.adoptNode(style);
        document.getDocumentElement().appendChild(style);
    }

    private org.w3c.dom.Node addStyleImportTextNode(Document document, Element style, String name) {
        return style.appendChild(document.createTextNode("@import url(" + name + ");"));
    }

    private CDATASection getCdataSection(Document document, List<URL> cssUrls) {
        StringBuilder styleSheetBuilder = new StringBuilder();
        for (URL cssUrl : cssUrls) {
            try {
                styleSheetBuilder.append(new String(IOUtils.toByteArray(cssUrl), StandardCharsets.UTF_8));
            } catch (IOException e) {
                throw new UncheckedIOException("Can't read css file " + cssUrl.getPath(), e);
            }
        }
        String graphStyle = "\n" + styleSheetBuilder + "\n";
        String cssStr = graphStyle
                .replace("\r\n", "\n") // workaround for https://bugs.openjdk.java.net/browse/JDK-8133452
                .replace("\r", "\n");
        return document.createCDATASection(cssStr);
    }

    /**
     * Create the SVGDocument corresponding to the graph
     */
    protected GraphMetadata writeGraph(String prefixId,
                                       VoltageLevelGraph graph,
                                       Document document,
                                       DiagramLabelProvider initProvider,
                                       DiagramStyleProvider styleProvider) {
        GraphMetadata metadata = new GraphMetadata();

        Element root = document.createElement(GROUP);

        if (layoutParameters.isShowGrid() && graph.isPositionNodeBusesCalculated()) {
            root.appendChild(drawGrid(prefixId, graph, document, metadata));
        }

        drawVoltageLevel(prefixId, graph, root, metadata, initProvider, styleProvider, true);

        document.adoptNode(root);
        document.getDocumentElement().appendChild(root);

        return metadata;
    }

    protected void drawVoltageLevel(String prefixId,
                                    VoltageLevelGraph graph,
                                    Element root,
                                    GraphMetadata metadata,
                                    DiagramLabelProvider initProvider,
                                    DiagramStyleProvider styleProvider,
                                    boolean useNodesInfosParam) {
        AnchorPointProvider anchorPointProvider = (type, id) -> {
            if (type.equals(BUSBAR_SECTION)) {
                BusNode busbarSectionNode = (BusNode) graph.getNode(id);
                List<AnchorPoint> result = new ArrayList<>();
                result.add(new AnchorPoint(0, 0, AnchorOrientation.HORIZONTAL));
                for (int i = 1; i < busbarSectionNode.getPosition().getSpan(H); i++) {
                    result.add(new AnchorPoint(
                            ((double) i / 2) * layoutParameters.getCellWidth() - layoutParameters.getHorizontalBusPadding() / 2,
                            0, AnchorOrientation.VERTICAL));
                }
                result.add(new AnchorPoint(busbarSectionNode.getPxWidth(), 0, AnchorOrientation.HORIZONTAL));
                return result;
            }
            return componentLibrary.getAnchorPoints(type);
        };

        List<Node> remainingNodes = graph.getNodes();

        List<Node> nodesToDraw = graph.getNodes().stream().filter(n -> n instanceof BusNode).collect(Collectors.toList());
        drawNodes(prefixId, root, graph, metadata, anchorPointProvider, initProvider, styleProvider, nodesToDraw);

        remainingNodes.removeAll(nodesToDraw);

        List<Edge> remainingEdges = graph.getEdges();

        for (Cell cell : graph.getCells()) {
            remainingEdges.removeAll(drawCell(prefixId, root, graph, cell, metadata, anchorPointProvider, initProvider,
                    styleProvider));
            remainingNodes.removeAll(cell.getNodes());
        }
        drawEdges(prefixId, root, graph, remainingEdges, metadata, anchorPointProvider, initProvider, styleProvider);
        drawNodes(prefixId, root, graph, metadata, anchorPointProvider, initProvider, styleProvider, remainingNodes);

        // Drawing the nodes outside the voltageLevel graphs (multi-terminal nodes)
        drawMultiTerminalNodes(prefixId, root, graph, metadata, styleProvider, anchorPointProvider);

        // Drawing the snake lines
        drawSnakeLines(prefixId, root, graph, metadata, styleProvider, anchorPointProvider);

        if (useNodesInfosParam && layoutParameters.isAddNodesInfos()) {
            drawNodesInfos(prefixId, root, graph, styleProvider);
        }
    }

    private List<Edge> drawCell(String prefixId, Element root, VoltageLevelGraph graph, Cell cell,
                                GraphMetadata metadata, AnchorPointProvider anchorPointProvider, DiagramLabelProvider initProvider,
                                DiagramStyleProvider styleProvider) {

        // To avoid overlapping lines over the switches, first, we draw all nodes except the switch nodes,
        // then we draw all the edges, and finally we draw the switch nodes

        String cellId = DiagramStyles.escapeId(prefixId + cell.getId());
        Element g = root.getOwnerDocument().createElement(GROUP);
        g.setAttribute("id", cellId);
        g.setAttribute(CLASS, "cell " + cellId);

        drawNodes(prefixId, g, graph, metadata, anchorPointProvider, initProvider, styleProvider,
                cell.getNodes().stream()
                        .filter(n -> !(n instanceof BusNode || n instanceof SwitchNode)).collect(Collectors.toList()));

        List<Edge> edgesToDraw = cell.getNodes().stream().filter(n -> !(n instanceof BusNode))
                .flatMap(n -> n.getAdjacentEdges().stream())
                .distinct().collect(Collectors.toList());
        drawEdges(prefixId, g, graph, edgesToDraw, metadata, anchorPointProvider, initProvider, styleProvider);

        drawNodes(prefixId, g, graph, metadata, anchorPointProvider, initProvider, styleProvider,
                cell.getNodes().stream().filter(n -> n instanceof SwitchNode).collect(Collectors.toList()));
        root.appendChild(g);
        return edgesToDraw;
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
                               DiagramLabelProvider initProvider,
                               DiagramStyleProvider styleProvider,
                               Path svgFile) {
        try (Writer writer = Files.newBufferedWriter(svgFile)) {
            return write(prefixId, graph, initProvider, styleProvider, writer);
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
                               DiagramLabelProvider labelProvider,
                               DiagramStyleProvider styleProvider,
                               Writer writer) {
        DOMImplementation domImpl = DomUtil.getDocumentBuilder().getDOMImplementation();

        Document document = domImpl.createDocument(SVG_NAMESPACE, SVG_QUALIFIED_NAME, null);

        Set<String> listUsedComponentSVG = new HashSet<>();
        addStyle(document, styleProvider, labelProvider, graph.getNodes(), listUsedComponentSVG);
        graph.getMultiTermNodes().forEach(n -> listUsedComponentSVG.add(n.getComponentType()));

        createDefsSVGComponents(document, listUsedComponentSVG);

        GraphMetadata metadata = writeGraph(prefixId, graph, document, labelProvider, styleProvider);

        DomUtil.transformDocument(document, writer);

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
    protected GraphMetadata writeGraph(String prefixId,
                                       SubstationGraph graph,
                                       Document document,
                                       DiagramLabelProvider initProvider,
                                       DiagramStyleProvider styleProvider) {
        GraphMetadata metadata = new GraphMetadata();

        Element root = document.createElement(GROUP);

        // Drawing grid lines
        if (layoutParameters.isShowGrid()) {
            for (VoltageLevelGraph vlGraph : graph.getNodes()) {
                if (vlGraph.isPositionNodeBusesCalculated()) {
                    root.appendChild(drawGrid(prefixId, vlGraph, document, metadata));
                }
            }
        }

        drawSubstation(prefixId, graph, root, metadata, initProvider, styleProvider);

        // the drawing of the voltageLevel graph labels is done at the end in order to
        // facilitate the move of a voltageLevel in the diagram
        for (VoltageLevelGraph vlGraph : graph.getNodes()) {
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
                                  DiagramLabelProvider initProvider,
                                  DiagramStyleProvider styleProvider) {
        // Drawing the voltageLevel graphs
        for (VoltageLevelGraph vlGraph : graph.getNodes()) {
            drawVoltageLevel(prefixId, vlGraph, root, metadata, initProvider, styleProvider, false);
        }

        AnchorPointProvider anchorPointProvider = (type, id) -> componentLibrary.getAnchorPoints(type);

        // Drawing the nodes outside the voltageLevel graphs (multi-terminal nodes)
        drawMultiTerminalNodes(prefixId, root, graph, metadata, styleProvider, anchorPointProvider);

        // Drawing the snake lines
        drawSnakeLines(prefixId, root, graph, metadata, styleProvider, anchorPointProvider);
    }

    /*
     * Drawing the grid lines (if required)
     */
    protected Element drawGrid(String prefixId, VoltageLevelGraph graph, Document document, GraphMetadata metadata) {
        int maxH = graph.getNodeBuses().stream()
                .mapToInt(nodeBus -> (nodeBus.getPosition().get(H) + nodeBus.getPosition().getSpan(H)) / 2)
                .max().orElse(0);
        int maxV = graph.getNodeBuses().stream()
                .mapToInt(nodeBus -> nodeBus.getPosition().get(V))
                .max().orElse(1) - 1;

        Element gridRoot = document.createElement(GROUP);

        String gridId = prefixId + "GRID_" + graph.getVoltageLevelInfos().getId();
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
                graph.getVoltageLevelInfos().getId(),
                null,
                null,
                null,
                false,
                BusCell.Direction.UNDEFINED,
                false,
                null));

        return gridRoot;
    }

    protected Element drawGridHorizontalLine(Document document, VoltageLevelGraph graph, int maxH, double y) {
        return drawGridLine(document,
                layoutParameters.getInitialXBus() + graph.getX(), y,
                layoutParameters.getInitialXBus() + maxH * layoutParameters.getCellWidth() + graph.getX(), y);
    }

    protected Element drawGridVerticalLine(Document document, VoltageLevelGraph graph, int maxV, double x) {
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
                             VoltageLevelGraph graph,
                             GraphMetadata metadata,
                             AnchorPointProvider anchorPointProvider,
                             DiagramLabelProvider initProvider,
                             DiagramStyleProvider styleProvider,
                             List<Node> nodes) {
        nodes.forEach(node -> {

            String nodeId = DiagramStyles.escapeId(prefixId + node.getId());
            Element g = root.getOwnerDocument().createElement(GROUP);
            g.setAttribute("id", nodeId);
            g.setAttribute(CLASS, String.join(" ", styleProvider.getSvgNodeStyles(node, componentLibrary, layoutParameters.isShowInternalNodes())));

            if (node.getType() == Node.NodeType.BUS) {
                drawBus((BusNode) node, g);
            } else {
                incorporateComponents(prefixId, node, g, styleProvider);
            }

            if (!node.isFictitious() || node instanceof Middle3WTNode) {
                drawNodeLabel(prefixId, g, node, initProvider);
                drawNodeDecorators(prefixId, g, node, initProvider, styleProvider);
            }

            root.appendChild(g);

            BusCell.Direction direction = (node instanceof FeederNode && node.getCell() != null) ? ((ExternCell) node.getCell()).getDirection() : BusCell.Direction.UNDEFINED;
            setMetadata(metadata, node, nodeId, graph, direction, anchorPointProvider);
        });
    }

    protected void setMetadata(GraphMetadata metadata, Node node, String nodeId, VoltageLevelGraph graph, BusCell.Direction direction, AnchorPointProvider anchorPointProvider) {
        String nextVId = null;
        if (node instanceof FeederWithSideNode) {
            VoltageLevelInfos otherSideVoltageLevelInfos = ((FeederWithSideNode) node).getOtherSideVoltageLevelInfos();
            if (otherSideVoltageLevelInfos != null) {
                nextVId = otherSideVoltageLevelInfos.getId();
            }
        }

        metadata.addNodeMetadata(
                new GraphMetadata.NodeMetadata(nodeId, graph != null ? graph.getVoltageLevelInfos().getId() : "", nextVId,
                        node.getComponentType(), node.getRotationAngle(),
                        node.isOpen(), direction, false, node.getEquipmentId()));
        if (node.getType() == Node.NodeType.BUS) {
            metadata.addComponentMetadata(new ComponentMetadata(BUSBAR_SECTION,
                    nodeId,
                    anchorPointProvider.getAnchorPoints(BUSBAR_SECTION, node.getId()),
                    new ComponentSize(0, 0),
                    componentLibrary.getComponentStyleClass(node.getComponentType()).orElse(null),
                    true, null));
        } else {
            if (metadata.getComponentMetadata(node.getComponentType()) == null) {
                metadata.addComponentMetadata(new ComponentMetadata(node.getComponentType(),
                        null,
                        componentLibrary.getAnchorPoints(node.getComponentType()),
                        componentLibrary.getSize(node.getComponentType()),
                        componentLibrary.getComponentStyleClass(node.getComponentType()).orElse(null),
                        true, null));
            }
        }
    }

    protected void drawNodeLabel(String prefixId, Element g, Node node, DiagramLabelProvider labelProvider) {
        for (DiagramLabelProvider.NodeLabel nodeLabel : labelProvider.getNodeLabels(node)) {
            LabelPosition labelPosition = nodeLabel.getPosition();
            Element label = createLabelElement(nodeLabel.getLabel(), labelPosition.getdX(), labelPosition.getdY(), labelPosition.getShiftAngle(), g);
            label.setAttribute("id", prefixId + labelPosition.getPositionName());
            if (labelPosition.isCentered()) {
                label.setAttribute(TEXT_ANCHOR, MIDDLE);
            }
            g.appendChild(label);
        }
    }

    protected void drawNodeDecorators(String prefixId, Element root, Node node, DiagramLabelProvider labelProvider,
                                      DiagramStyleProvider styleProvider) {
        for (DiagramLabelProvider.NodeDecorator nodeDecorator : labelProvider.getNodeDecorators(node)) {
            Element g = root.getOwnerDocument().createElement(GROUP);
            g.setAttribute(CLASS, String.join(" ", styleProvider.getSvgNodeDecoratorStyles(nodeDecorator, node, componentLibrary)));
            insertDecoratorSVGIntoDocumentSVG(prefixId, nodeDecorator, g, node, styleProvider);
            root.appendChild(g);
        }
    }

    /*
     * Drawing the graph label
     */
    protected void drawGraphLabel(String prefixId, Element root, VoltageLevelGraph graph, GraphMetadata metadata) {
        // drawing the label of the voltageLevel
        String idLabelVoltageLevel = prefixId + "LABEL_VL_" + graph.getVoltageLevelInfos().getId();
        Element gLabel = root.getOwnerDocument().createElement(GROUP);
        gLabel.setAttribute("id", idLabelVoltageLevel);

        double decalYLabel = !layoutParameters.isAdaptCellHeightToContent()
                ? layoutParameters.getExternCellHeight()
                : graph.getMaxCalculatedCellHeight(BusCell.Direction.TOP);
        if (decalYLabel < 0) {
            decalYLabel = layoutParameters.getExternCellHeight();
        }

        double yPos = graph.getY() + layoutParameters.getInitialYBus() - decalYLabel - 20.;

        String graphName = graph.isUseName() ? graph.getVoltageLevelInfos().getName() : graph.getVoltageLevelInfos().getId();
        Element label = createLabelElement(graphName, graph.getX(), yPos, 0, gLabel);
        label.setAttribute(CLASS, DiagramStyles.GRAPH_LABEL_STYLE_CLASS);
        gLabel.appendChild(label);
        root.appendChild(gLabel);

        metadata.addNodeMetadata(new GraphMetadata.NodeMetadata(idLabelVoltageLevel,
                graph.getVoltageLevelInfos().getId(),
                null,
                null,
                null,
                false,
                BusCell.Direction.UNDEFINED,
                true,
                null));
    }

    /*
     * Drawing the voltageLevel graph busbar sections
     */
    protected Element drawBus(BusNode node, Element g) {
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

        return line;
    }

    /*
     * Create a label text element at the given position
     */
    protected Element createLabelElement(String str, double xShift, double yShift, int shiftAngle, Element g) {
        Element label = g.getOwnerDocument().createElement("text");
        label.setAttribute("x", String.valueOf(xShift));
        label.setAttribute("y", String.valueOf(yShift));
        label.setAttribute(TRANSFORM, ROTATE + "(" + shiftAngle + "," + 0 + "," + 0 + ")");
        label.setAttribute(CLASS, LABEL_STYLE_CLASS);
        Text text = g.getOwnerDocument().createTextNode(str);
        label.appendChild(text);
        return label;
    }

    protected boolean canInsertComponentSVG(Node node) {
        return (!node.isFictitious() && node.getType() != Node.NodeType.SHUNT)
                || (node.isFictitious()
                && node.getComponentType().equals(THREE_WINDINGS_TRANSFORMER)
                || node.getComponentType().equals(TWO_WINDINGS_TRANSFORMER)
                || node.getComponentType().equals(PHASE_SHIFT_TRANSFORMER)
                || node.getComponentType().equals(NODE)
                || node.getComponentType().equals(BUSBREAKER_CONNECTION));
    }

    protected void incorporateComponents(String prefixId, Node node, Element g, DiagramStyleProvider styleProvider) {
        String componentType = node.getComponentType();
        transformComponent(node, g);
        if (componentLibrary.getSvgElements(componentType) != null && canInsertComponentSVG(node)) {
            insertComponentSVGIntoDocumentSVG(prefixId, componentType, g, node, styleProvider);
        }
    }

    private void handleNodeRotation(Node node) {
        if (node.getGraph() != null) { // node in voltage level graph
            if ((node.getComponentType().equals(TWO_WINDINGS_TRANSFORMER)
                    || node.getComponentType().equals(PHASE_SHIFT_TRANSFORMER)
                    || node.getComponentType().equals(THREE_WINDINGS_TRANSFORMER))
                    && node.getCell() != null
                    && ((ExternCell) node.getCell()).getDirection() == BusCell.Direction.BOTTOM) {
                // permutation if cell direction is BOTTOM,
                // because in the svg component library, circle for winding1 is below circle for winding2
                node.setRotationAngle(180.);
            }
        } else {  // node outside any graph
            List<Node> adjacentNodes = node.getAdjacentNodes();
            adjacentNodes.sort(Comparator.comparingDouble(Node::getX));
            if (adjacentNodes.size() == 2) {  // 2 windings transformer
                FeederWithSideNode node1 = (FeederWithSideNode) adjacentNodes.get(0);
                FeederWithSideNode node2 = (FeederWithSideNode) adjacentNodes.get(1);
                List<Edge> edges = node.getAdjacentEdges();
                List<Point> pol1 = ((BranchEdge) edges.get(0)).getSnakeLine();
                List<Point> pol2 = ((BranchEdge) edges.get(1)).getSnakeLine();
                if (!(pol1.isEmpty() || pol2.isEmpty())) {
                    // get points for the line supporting the svg component
                    double x1 = pol1.get(pol1.size() - 2).getX(); // absciss of the first polyline second last point
                    double x2 = pol2.get(1).getX();  // absciss of the second polyline third point

                    if (x1 == x2) {
                        // vertical line supporting the svg component
                        FeederWithSideNode nodeWinding1 = node1.getSide() == FeederWithSideNode.Side.ONE ? node1 : node2;
                        FeederWithSideNode nodeWinding2 = node1.getSide() == FeederWithSideNode.Side.TWO ? node1 : node2;
                        if (nodeWinding2.getY() > nodeWinding1.getY()) {
                            // permutation here, because in the svg component library, circle for winding1 is below circle for winding2
                            node.setRotationAngle(180.);
                        }
                    } else {
                        // horizontal line supporting the svg component,
                        // so we rotate the component by 90 or 270 (the component is vertical in the library)
                        if (node1.getSide() == FeederWithSideNode.Side.ONE) {
                            // rotation by 90 to get circle for winding1 at the left side
                            node.setRotationAngle(90.);
                        } else {
                            // rotation by 90 to get circle for winding1 at the right side
                            node.setRotationAngle(270.);
                        }
                    }
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
                                                     String componentType,
                                                     Element g, Node node,
                                                     DiagramStyleProvider styleProvider) {
        handleNodeRotation(node);
        BiConsumer<Element, String> elementAttributesSetter
                = (elt, subComponent) -> setComponentAttributes(prefixId, g, node, styleProvider, elt, componentType, subComponent);
        insertSVGIntoDocumentSVG(node.getName(), componentType, g, elementAttributesSetter);
    }

    protected void insertArrowSVGIntoDocumentSVG(String prefixId, Element g, double angle) {
        BiConsumer<Element, String> elementAttributesSetter
                = (e, subComponent) -> setArrowAttributes(prefixId, g, e, subComponent, angle);
        insertSVGIntoDocumentSVG("", ARROW, g, elementAttributesSetter);
    }

    private void setArrowAttributes(String prefixId, Element g, Element e, String subComponent, double angle) {
        replaceId(g, e, prefixId);
        componentLibrary.getSubComponentStyleClass(ARROW, subComponent).ifPresent(style -> e.setAttribute(CLASS, style));
        if (Math.abs(angle) > 0) {
            ComponentSize componentSize = componentLibrary.getSize(ARROW);
            double cx = componentSize.getWidth() / 2;
            double cy = componentSize.getHeight() / 2;
            e.setAttribute(TRANSFORM, ROTATE + "(" + angle + "," + cx + "," + cy + ")");
        }
    }

    protected void insertDecoratorSVGIntoDocumentSVG(String prefixId,
                                                     DiagramLabelProvider.NodeDecorator nodeDecorator,
                                                     Element g, Node node,
                                                     DiagramStyleProvider styleProvider) {
        BiConsumer<Element, String> elementAttributesSetter
                = (elt, subComponent) -> setDecoratorAttributes(prefixId, g, node, nodeDecorator, styleProvider, elt, subComponent);
        String nodeDecoratorType = nodeDecorator.getType();
        insertSVGIntoDocumentSVG(nodeDecoratorType, nodeDecoratorType, g, elementAttributesSetter);
    }

    protected void insertSVGIntoDocumentSVG(String name, String componentType, Element g,
                                            BiConsumer<Element, String> elementAttributesSetter) {
        addToolTip(name, g);
        Map<String, List<Element>> subComponents = componentLibrary.getSvgElements(componentType);
        subComponents.forEach(layoutParameters.isAvoidSVGComponentsDuplication() ?
            (subComponentName, svgSubComponent) -> insertSubcomponentReference(g, elementAttributesSetter, componentType, subComponentName, subComponents.size()) :
            (subComponentName, svgSubComponent) -> insertDuplicatedSubcomponent(g, elementAttributesSetter, subComponentName, svgSubComponent)
        );
    }

    private void insertDuplicatedSubcomponent(Element g, BiConsumer<Element, String> elementAttributesSetter, String subComponentName, List<Element> svgSubComponent) {
        svgSubComponent.forEach(e -> {
            Element clonedElement = (Element) e.cloneNode(true);
            setAttributesAndInsertElement(g, elementAttributesSetter, subComponentName, clonedElement);
        });
    }

    private void insertSubcomponentReference(Element g, BiConsumer<Element, String> elementAttributesSetter, String componentType, String subComponentName, int nbSubComponents) {
        // Adding <use> markup to reuse the svg defined in the <defs> part
        Element eltUse = g.getOwnerDocument().createElement("use");
        eltUse.setAttribute("href", "#" + getHRefValue(nbSubComponents, componentType, subComponentName));
        setAttributesAndInsertElement(g, elementAttributesSetter, subComponentName, eltUse);
    }

    private static String getHRefValue(int nbSubComponents, String componentType, String subComponentName) {
        return nbSubComponents > 1 ? componentType + "-" + subComponentName : componentType;
    }

    private void setAttributesAndInsertElement(Element g, BiConsumer<Element, String> elementAttributesSetter, String subComponentName, Element element) {
        elementAttributesSetter.accept(element, subComponentName);
        g.getOwnerDocument().adoptNode(element);
        g.appendChild(element);
    }

    private void addToolTip(String tooltip, Element g) {
        if (layoutParameters.isTooltipEnabled() && !tooltip.isEmpty()) {
            Document doc = g.getOwnerDocument();
            Element title = doc.createElement("title");
            title.appendChild(doc.createTextNode(tooltip));
            doc.adoptNode(title);
            g.appendChild(title);
        }
    }

    private void setComponentAttributes(String prefixId, Element g, Node node, DiagramStyleProvider styleProvider,
                                        Element elt, String componentType, String subComponent) {
        replaceId(g, elt, prefixId);
        ComponentSize size = componentLibrary.getSize(componentType);
        if (node.getType() != Node.NodeType.SWITCH && node.isRotated()) {
            elt.setAttribute(TRANSFORM, ROTATE + "(" + node.getRotationAngle() + "," + size.getWidth() / 2 + "," + size.getHeight() / 2 + ")");
        }
        List<String> subComponentStyles = styleProvider.getSvgNodeSubcomponentStyles(node, subComponent);
        componentLibrary.getSubComponentStyleClass(componentType, subComponent).ifPresent(subComponentStyles::add);
        if (!subComponentStyles.isEmpty()) {
            elt.setAttribute(CLASS, String.join(" ", subComponentStyles));
        }
    }

    private void setDecoratorAttributes(String prefixId, Element g, Node node, DiagramLabelProvider.NodeDecorator nodeDecorator,
                                        DiagramStyleProvider styleProvider, Element elt, String subComponentName) {
        replaceId(g, elt, prefixId);
        ComponentSize decoratorSize = componentLibrary.getSize(nodeDecorator.getType());
        LabelPosition decoratorPosition = nodeDecorator.getPosition();
        elt.setAttribute(TRANSFORM, getTransformStringDecorator(node, decoratorPosition, decoratorSize));
        List<String> svgNodeSubcomponentStyles = styleProvider.getSvgNodeSubcomponentStyles(node, subComponentName);
        componentLibrary.getSubComponentStyleClass(nodeDecorator.getType(), subComponentName).ifPresent(svgNodeSubcomponentStyles::add);
        if (!svgNodeSubcomponentStyles.isEmpty()) {
            elt.setAttribute(CLASS, String.join(" ", svgNodeSubcomponentStyles));
        }
    }

    /**
     * Ensures uniqueness of ids by adding prefixId and node id before id of elt (if existing)
     *
     * @param g        XML element for the node
     * @param elt      XML element being duplicated
     * @param prefixId prefix string
     */
    private void replaceId(Element g, Element elt, String prefixId) {
        org.w3c.dom.Node nodeId = elt.getAttributes().getNamedItem("id");
        // the id is set only if elt had already an id
        if (nodeId != null) {
            String nodeIdValue = nodeId.getTextContent();
            String gIdValue = StringUtils.removeStart(g.getAttribute("id"), prefixId);
            nodeId.setTextContent(prefixId + gIdValue + "_" + nodeIdValue);
        }
    }

    private String getTransformStringDecorator(Node node, LabelPosition decoratorPosition, ComponentSize decoratorSize) {
        String transform;
        if (node.isRotated() && node.getType() == Node.NodeType.SWITCH) {
            double[] matrix = getDecoratorTransformMatrix(node, decoratorPosition, decoratorSize);
            transform = transformMatrixToString(matrix, 4);
        } else {
            ComponentSize componentSize = componentLibrary.getSize(node.getComponentType());
            double dX = componentSize.getWidth() / 2 + decoratorPosition.getdX();
            double dY = componentSize.getHeight() / 2 + decoratorPosition.getdY();
            if (decoratorPosition.isCentered()) {
                dX -= decoratorSize.getWidth() / 2;
                dY -= decoratorSize.getHeight() / 2;
            }
            transform = TRANSLATE + "(" + dX + "," + dY + ")";
        }
        return transform;
    }

    protected void transformComponent(Node node, Element g) {
        ComponentSize componentSize = componentLibrary.getSize(node.getComponentType());

        // For a node marked for rotation during the graph building, but with an svg component not allowed
        // to rotate (ex : disconnector in SVG component library), we cancel the rotation
        if (node.isRotated() && !componentLibrary.isAllowRotation(node.getComponentType())) {
            node.setRotationAngle(null);
        }

        String trans;
        if (!node.isRotated()) {
            double[] translate = getNodeTranslate(node);
            trans = TRANSLATE + "(" + translate[0] + "," + translate[1] + ")";
        } else {
            // afester javafx library does not handle more than one transformation, yet, so
            // combine the couple of transformations, translation+rotation, in a single matrix transformation
            trans = getTransformMatrixString(node.getX(), node.getY(), Math.toRadians(node.getRotationAngle()), componentSize);
        }
        g.setAttribute(TRANSFORM, trans);
    }

    private double[] getNodeTranslate(Node node) {
        ComponentSize componentSize = componentLibrary.getSize(node.getComponentType());
        double translateX = layoutParameters.getTranslateX() + node.getX() - componentSize.getWidth() / 2;
        double translateY = layoutParameters.getTranslateY() + node.getY() - componentSize.getHeight() / 2;
        return new double[]{translateX, translateY};
    }

    private double[] getDecoratorTransformMatrix(Node node, LabelPosition decoratorPosition, ComponentSize decoratorSize) {
        ComponentSize componentSize = componentLibrary.getSize(node.getComponentType());
        double[] translateNode = getNodeTranslate(node);
        double[] matrixNode = getTransformMatrix(componentSize.getWidth(), componentSize.getHeight(), node.getRotationAngle() * Math.PI / 180,
                layoutParameters.getTranslateX() + node.getX(), layoutParameters.getTranslateY() + node.getY());
        double translateDecoratorX = translateNode[0] + componentSize.getWidth() / 2 + decoratorPosition.getdX();
        double translateDecoratorY = translateNode[1] + componentSize.getHeight() / 2 + decoratorPosition.getdY();
        if (decoratorPosition.isCentered()) {
            translateDecoratorX -= decoratorSize.getWidth() / 2;
            translateDecoratorY -= decoratorSize.getHeight() / 2;
        }
        double t1 = +matrixNode[3] * (translateDecoratorX - matrixNode[4]) - matrixNode[2] * (translateDecoratorY - matrixNode[5]);
        double t2 = -matrixNode[1] * (translateDecoratorX - matrixNode[4]) + matrixNode[0] * (translateDecoratorY - matrixNode[5]);
        return new double[]{matrixNode[3], -matrixNode[1], -matrixNode[2], matrixNode[0], t1, t2};
    }

    protected void transformArrow(List<Point> points, ComponentSize componentSize, double shift, Element g) {

        Point pointA = points.get(0);
        Point pointB = points.get(1);
        double distancePoints = pointA.distance(pointB);

        // Case of wires with non-direct straight lines: if wire distance between first 2 points is too small to display
        // the arrow, checks if the distance between the 2nd and the 3rd points is big enough
        if (points.size() > 2 && distancePoints < 3 * componentSize.getHeight()) {
            double distancePoints23 = points.get(1).distance(points.get(2));
            if (distancePoints23 > 3 * componentSize.getHeight()) {
                distancePoints = distancePoints23;
                pointA = points.get(1);
                pointB = points.get(2);
            }
        }

        if (distancePoints > 0) {
            double dx = pointB.getX() - pointA.getX();
            double dy = pointB.getY() - pointA.getY();

            // Calculate cos and sin of the angle between the wire line and the abscisse
            double cosAngle = dx / distancePoints;
            double sinAngle = dy / distancePoints;

            // If not enough space to have layoutParameters.getArrowDistance() at both sides of the 2 arrows,
            // we compute the distance between feeder anchor and first arrow so that the two arrows are centered.
            double distFeederAnchorToFirstArrowCenter =
                distancePoints >= 2 * layoutParameters.getArrowDistance() + 2 * componentSize.getHeight()
                    ? layoutParameters.getArrowDistance()
                    : (distancePoints - 2 * componentSize.getHeight()) / 2;
            double x = pointA.getX() + cosAngle * (distFeederAnchorToFirstArrowCenter + shift);
            double y = pointA.getY() + sinAngle * (distFeederAnchorToFirstArrowCenter + shift);

            double arrowRotationAngle = Math.atan(dy / dx) - Math.PI / 2;
            if (arrowRotationAngle < -Math.PI / 2) {
                arrowRotationAngle += Math.PI;
            }
            g.setAttribute(TRANSFORM, getTransformMatrixString(x, y, arrowRotationAngle, componentSize));
        }

    }

    private String getTransformMatrixString(double centerPosX, double centerPosY, double angle, ComponentSize componentSize) {
        double centerPosTransX = layoutParameters.getTranslateX() + centerPosX;
        double centerPosTransY = layoutParameters.getTranslateY() + centerPosY;
        double[] matrix = getTransformMatrix(componentSize.getWidth(), componentSize.getHeight(), angle,
                centerPosTransX, centerPosTransY);
        return transformMatrixToString(matrix, 4);
    }

    private double[] getTransformMatrix(double width, double height, double angle,
                                        double centerPosX, double centerPosY) {

        double cosRo = Math.cos(angle);
        double sinRo = Math.sin(angle);
        double cdx = width / 2;
        double cdy = height / 2;

        double e1 = centerPosX - cdx * cosRo + cdy * sinRo;
        double f1 = centerPosY - cdx * sinRo - cdy * cosRo;

        return new double[]{+cosRo, sinRo, -sinRo, cosRo, e1, f1};
    }

    private static String transformMatrixToString(double[] matrix, int precision) {
        double[] matrix2 = new double[matrix.length];
        for (int i = 0; i < matrix.length; i++) {
            matrix2[i] = Precision.round(matrix[i], precision);
        }
        return "matrix("
                + matrix2[0] + "," + matrix2[1] + ","
                + matrix2[2] + "," + matrix2[3] + ","
                + matrix2[4] + "," + matrix2[5] + ")";
    }

    protected void insertArrowsAndLabels(String prefixId,
                                         String wireId,
                                         List<Point> points,
                                         Element root,
                                         FeederNode feederNode,
                                         GraphMetadata metadata,
                                         DiagramLabelProvider initProvider,
                                         boolean feederArrowSymmetry) {
        InitialValue init = initProvider.getInitialValue(feederNode);

        boolean arrowSymmetry = feederNode.getDirection() == BusCell.Direction.TOP || feederArrowSymmetry;

        Optional<String> label1 = arrowSymmetry ? init.getLabel1() : init.getLabel2();
        Optional<Direction> direction1 = arrowSymmetry ? init.getArrowDirection1() : init.getArrowDirection2();

        Optional<String> label2 = arrowSymmetry ? init.getLabel2() : init.getLabel1();
        Optional<Direction> direction2 = arrowSymmetry ? init.getArrowDirection2() : init.getArrowDirection1();

        int iArrow1 = arrowSymmetry ? 1 : 2;
        int iArrow2 = arrowSymmetry ? 2 : 1;

        // we draw the arrow only if value 1 is present
        label1.ifPresent(lb ->
                drawArrowAndLabel(prefixId, wireId, points, root, lb, init.getLabel3(), direction1, 0, iArrow1, metadata));

        // we draw the arrow only if value 2 is present
        label2.ifPresent(lb -> {
            double shiftArrow2 = 2 * metadata.getComponentMetadata(ARROW).getSize().getHeight();
            drawArrowAndLabel(prefixId, wireId, points, root, lb, init.getLabel4(),
                    direction2, shiftArrow2, iArrow2, metadata);
        });
    }

    private void drawArrowAndLabel(String prefixId, String wireId, List<Point> points, Element root,
                                   String labelR, Optional<String> labelL, Optional<Direction> dir, double shift, int iArrow,
                                   GraphMetadata metadata) {
        ComponentMetadata cd = metadata.getComponentMetadata(ARROW);

        double shX = cd.getSize().getWidth() + LABEL_OFFSET;
        double shY = cd.getSize().getHeight() / 2;

        Element g = root.getOwnerDocument().createElement(GROUP);
        String arrowWireId = wireId + "_ARROW" + iArrow;
        g.setAttribute("id", arrowWireId);
        transformArrow(points, cd.getSize(), shift, g);

        double rotationAngle =  points.get(0).getY() > points.get(1).getY() ? 180 : 0;
        insertArrowSVGIntoDocumentSVG(prefixId, g, rotationAngle);
        Element label = createLabelElement(labelR, shX, shY, 0, g);
        g.appendChild(label);

        List<String> styles = new ArrayList<>(3);
        componentLibrary.getComponentStyleClass(ARROW).ifPresent(styles::add);
        styles.add(iArrow == 1 ? ARROW_ACTIVE_CLASS : ARROW_REACTIVE_CLASS);
        dir.ifPresent(direction -> styles.add(direction == Direction.UP ? UP_CLASS : DOWN_CLASS));
        g.setAttribute(CLASS, String.join(" ", styles));

        labelL.ifPresent(s -> {
            Element labelLeft = createLabelElement(s, -LABEL_OFFSET, shY, 0, g);
            labelLeft.setAttribute(STYLE, "text-anchor:end");
            g.appendChild(labelLeft);
        });

        root.appendChild(g);
        metadata.addArrowMetadata(new ArrowMetadata(arrowWireId, wireId, layoutParameters.getArrowDistance()));

    }

    /**
     * For global unicity in all type of container (voltage level, substation, zone), we prefix with the container Id and
     * we rely on the fact that node ids are unique inside a voltage level. We also prepend with a custom prefix id to
     * allow multiple diagrams unicity.
     */
    private static String getWireId(String prefixId, String containerId, Edge edge) {
        return escapeClassName(prefixId + "_" + containerId + "_" + edge.getNode1().getId() + "_" + edge.getNode2().getId());
    }

    /*
     * Drawing the voltageLevel graph edges
     */
    protected void drawEdges(String prefixId, Element root, VoltageLevelGraph graph, List<Edge> edges, GraphMetadata metadata, AnchorPointProvider anchorPointProvider, DiagramLabelProvider initProvider, DiagramStyleProvider styleProvider) {
        String voltageLevelId = graph.getVoltageLevelInfos().getId();

        for (Edge edge : edges) {
            String wireId = getWireId(prefixId, voltageLevelId, edge);

            Element g = root.getOwnerDocument().createElement(GROUP);
            g.setAttribute("id", wireId);
            List<String> wireStyles = styleProvider.getSvgWireStyles(edge, layoutParameters.isHighlightLineState());
            g.setAttribute(CLASS, String.join(" ", wireStyles));

            root.appendChild(g);

            Element polyline = root.getOwnerDocument().createElement(POLYLINE);
            WireConnection anchorPoints = WireConnection.searchBetterAnchorPoints(anchorPointProvider, edge.getNode1(), edge.getNode2());

            // Determine points of the polyline
            List<Point> pol = anchorPoints.calculatePolylinePoints(edge.getNode1(), edge.getNode2(),
                    layoutParameters.isDrawStraightWires());

            polyline.setAttribute(POINTS, pointsListToString(pol));
            g.appendChild(polyline);

            metadata.addWireMetadata(new GraphMetadata.WireMetadata(wireId,
                    escapeId(edge.getNode1().getId()),
                    escapeId(edge.getNode2().getId()),
                    layoutParameters.isDrawStraightWires(),
                    false));

            if (metadata.getComponentMetadata(ARROW) == null) {
                metadata.addComponentMetadata(new ComponentMetadata(ARROW,
                        null,
                        componentLibrary.getAnchorPoints(ARROW),
                        componentLibrary.getSize(ARROW),
                        componentLibrary.getComponentStyleClass(ARROW).orElse(null),
                        true, null));
            }

            if (edge.getNode1() instanceof FeederNode) {
                if (!(edge.getNode2() instanceof FeederNode)) {
                    insertArrowsAndLabels(prefixId, wireId, pol, root, (FeederNode) edge.getNode1(), metadata, initProvider,
                            layoutParameters.isFeederArrowSymmetry());
                }
            } else if (edge.getNode2() instanceof FeederNode) {
                Collections.reverse(pol);
                insertArrowsAndLabels(prefixId, wireId, pol, root, (FeederNode) edge.getNode2(), metadata, initProvider,
                        layoutParameters.isFeederArrowSymmetry());
            }
        }
    }

    /*
     * Drawing the zone graph edges (snakelines between station diagram)
     */
    protected void drawSnakeLines(String prefixId, Element root, ZoneGraph graph,
                                  GraphMetadata metadata, DiagramStyleProvider styleProvider,
                                  AnchorPointProvider anchorPointProvider) {
        for (BranchEdge edge : graph.getLineEdges()) {
            drawSnakeLines(edge, prefixId, root, metadata, styleProvider, anchorPointProvider);
        }
    }

    /*
     * Drawing the substation graph edges (snakelines between voltageLevel diagram)
     */
    protected void drawSnakeLines(String prefixId, Element root, AbstractBaseGraph graph,
                                  GraphMetadata metadata, DiagramStyleProvider styleProvider,
                                  AnchorPointProvider anchorPointProvider) {
        for (BranchEdge edge : graph.getLineEdges()) {
            drawSnakeLines(edge, prefixId, root, metadata, styleProvider, anchorPointProvider);
        }

        for (BranchEdge edge : graph.getTwtEdges()) {
            drawSnakeLines(edge, prefixId, root, metadata, styleProvider, anchorPointProvider);
        }
    }

    private void drawSnakeLines(BranchEdge edge, String prefixId, Element root, GraphMetadata metadata, DiagramStyleProvider styleProvider,
                                AnchorPointProvider anchorPointProvider) {
        Element g = root.getOwnerDocument().createElement(GROUP);
        String snakeLineId = escapeId(prefixId + edge.getId());
        g.setAttribute("id", snakeLineId);
        List<String> wireStyles = styleProvider.getSvgWireStyles(edge, layoutParameters.isHighlightLineState());
        g.setAttribute(CLASS, String.join(" ", wireStyles));
        root.appendChild(g);

        // Get the points of the snakeLine, already calculated during the layout application
        List<Point> pol = edge.getSnakeLine();
        if (!pol.isEmpty()) {
            adaptCoordSnakeLine(anchorPointProvider, edge, pol);
        }

        Element polyline = root.getOwnerDocument().createElement(POLYLINE);
        polyline.setAttribute(POINTS, pointsListToString(pol));
        g.appendChild(polyline);

        metadata.addWireMetadata(new GraphMetadata.WireMetadata(snakeLineId,
                escapeId(edge.getNode1().getId()),
                escapeId(edge.getNode2().getId()),
                layoutParameters.isDrawStraightWires(),
                true));
    }

    /*
     * Adaptation of the previously calculated snakeLine points, in order to use the anchor points
     * if a node is outside any graph
     */
    private void adaptCoordSnakeLine(AnchorPointProvider anchorPointProvider, BranchEdge edge, List<Point> pol) {
        Node n1 = edge.getNode1();
        Node n2 = edge.getNode2();

        VoltageLevelGraph g1 = n1.getGraph();
        VoltageLevelGraph g2 = n2.getGraph();

        int n = pol.size();

        // Getting the right polyline point from where we need to compute the best anchor point
        Point point;
        Point prevPoint;
        if (g2 == null) {
            point = pol.get(Math.max(n - 2, 0));
            prevPoint = pol.get(n - 1);
        } else {
            point = pol.get(1);
            prevPoint = pol.get(0);
        }

        WireConnection wireC = WireConnection.searchBetterAnchorPoints(anchorPointProvider, g1 == null ? n1 : n2, point);

        // Replacing the right points coordinates in the original polyline
        double xOld = prevPoint.getX();
        prevPoint.shift(wireC.getAnchorPoint1());
        if (xOld == point.getX()) {
            point.setX(prevPoint.getX());
        } else {
            point.setY(prevPoint.getY());
        }
    }

    protected String pointsListToString(List<Point> polyline) {
        return polyline.stream()
            .map(pt -> (pt.getX() + layoutParameters.getTranslateX()) + "," + (pt.getY() + layoutParameters.getTranslateY()))
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
                Map<String, List<Element>> subComponents = componentLibrary.getSvgElements(c);
                if (subComponents != null) {
                    Element group = document.createElement(GROUP);
                    group.setAttribute("id", c);

                    insertSVGComponentIntoDefsArea(c, group, subComponents);

                    defs.getOwnerDocument().adoptNode(group);
                    defs.appendChild(group);
                }
            });

            document.adoptNode(defs);
            document.getDocumentElement().appendChild(defs);
        }
    }

    protected void insertSVGComponentIntoDefsArea(String componentType, Element group, Map<String, List<Element>> subComponents) {
        for (Map.Entry<String, List<Element>> subComponent : subComponents.entrySet()) {
            if (subComponents.size() > 1) {
                Element subComponentGroup = group.getOwnerDocument().createElement("g");
                subComponentGroup.setAttribute("id", getHRefValue(subComponents.size(), componentType, subComponent.getKey()));
                addSvgSubComponentsToElement(subComponent.getValue(), subComponentGroup);
                group.getOwnerDocument().adoptNode(subComponentGroup);
                group.appendChild(subComponentGroup);
            } else {
                addSvgSubComponentsToElement(subComponent.getValue(), group);
            }
        }
    }

    private void addSvgSubComponentsToElement(List<Element> subComponentElements, Element group) {
        for (Element subComponentElement : subComponentElements) {
            org.w3c.dom.Node n = subComponentElement.cloneNode(true);
            group.getOwnerDocument().adoptNode(n);
            group.appendChild(n);
        }
    }

    @Override
    public GraphMetadata write(String prefixId,
                               ZoneGraph graph,
                               DiagramLabelProvider initProvider,
                               DiagramStyleProvider styleProvider,
                               Path svgFile) {
        try (Writer writer = Files.newBufferedWriter(svgFile)) {
            return write(prefixId, graph, initProvider, styleProvider, writer);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public GraphMetadata write(String prefixId,
                               ZoneGraph graph,
                               DiagramLabelProvider labelProvider,
                               DiagramStyleProvider styleProvider,
                               Writer writer) {
        DOMImplementation domImpl = DomUtil.getDocumentBuilder().getDOMImplementation();

        Document document = domImpl.createDocument(SVG_NAMESPACE, SVG_QUALIFIED_NAME, null);

        List<VoltageLevelGraph> vlGraphs = graph.getNodes().stream().map(SubstationGraph::getNodes).flatMap(Collection::stream).collect(Collectors.toList());

        Set<String> listUsedComponentSVG = new HashSet<>();
        addStyle(document, styleProvider, labelProvider, vlGraphs, listUsedComponentSVG);

        createDefsSVGComponents(document, listUsedComponentSVG);

        GraphMetadata metadata = writeGraph(prefixId, graph, vlGraphs, document, labelProvider, styleProvider);

        DomUtil.transformDocument(document, writer);

        return metadata;
    }

    private GraphMetadata writeGraph(String prefixId,
                                     ZoneGraph graph,
                                     List<VoltageLevelGraph> vlGraphs,
                                     Document document,
                                     DiagramLabelProvider initProvider,
                                     DiagramStyleProvider styleProvider) {
        GraphMetadata metadata = new GraphMetadata();

        Element root = document.createElement(GROUP);

        // Drawing grid lines
        if (layoutParameters.isShowGrid()) {
            for (VoltageLevelGraph vlGraph : vlGraphs) {
                if (vlGraph.isPositionNodeBusesCalculated()) {
                    root.appendChild(drawGrid(prefixId, vlGraph, document, metadata));
                }
            }
        }

        drawZone(prefixId, graph, root, metadata, initProvider, styleProvider);

        // the drawing of the voltageLevel graph labels is done at the end in order to
        // facilitate the move of a voltageLevel in the diagram
        for (VoltageLevelGraph vlGraph : vlGraphs) {
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
                          DiagramLabelProvider initProvider,
                          DiagramStyleProvider styleProvider) {
        for (SubstationGraph sGraph : graph.getNodes()) {
            drawSubstation(prefixId, sGraph, root, metadata, initProvider, styleProvider);
        }

        AnchorPointProvider anchorPointProvider = (type, id) -> componentLibrary.getAnchorPoints(type);

        drawSnakeLines(prefixId, root, graph, metadata, styleProvider, anchorPointProvider);
    }

    /*
     * Drawing the multi-terminal nodes
     */
    protected void drawMultiTerminalNodes(String prefixId,
                                          Element root,
                                          BaseGraph graph,
                                          GraphMetadata metadata,
                                          DiagramStyleProvider styleProvider,
                                          AnchorPointProvider anchorPointProvider) {
        graph.getMultiTermNodes().forEach(node -> {

            String nodeId = DiagramStyles.escapeId(prefixId + node.getId());
            Element g = root.getOwnerDocument().createElement(GROUP);
            g.setAttribute("id", nodeId);

            g.setAttribute(CLASS, String.join(" ",
                    styleProvider.getSvgNodeStyles(node, componentLibrary, layoutParameters.isShowInternalNodes())));

            incorporateComponents(prefixId, node, g, styleProvider);

            root.appendChild(g);

            setMetadata(metadata, node, nodeId, null, BusCell.Direction.UNDEFINED, anchorPointProvider);
        });
    }

    /*
     * Drawing the voltageLevel nodes infos
     */
    private void drawNodeInfos(ElectricalNodeInfo nodeInfo,
                               double xShift,
                               double yShift,
                               Element g,
                               String idNode,
                               double circleRadiusSize) {
        Element circle = g.getOwnerDocument().createElement("circle");

        circle.setAttribute("id", idNode + "_circle");
        circle.setAttribute("cx", String.valueOf(xShift));
        circle.setAttribute("cy", String.valueOf(yShift));
        circle.setAttribute("r", String.valueOf(circleRadiusSize / 2));
        circle.setAttribute("stroke-width", String.valueOf(circleRadiusSize));
        circle.setAttribute(CLASS, nodeInfo.getStyle());
        g.appendChild(circle);

        // v
        Element labelV = g.getOwnerDocument().createElement("text");
        labelV.setAttribute("id", idNode + "_v");
        String valueV = !Double.isNaN(nodeInfo.getV())
                ? String.valueOf(Precision.round(nodeInfo.getV(), 1))
                : "\u2014";  // em dash unicode for undefined value
        valueV += " kV";

        labelV.setAttribute("x", String.valueOf(xShift - circleRadiusSize));
        labelV.setAttribute("y", String.valueOf(yShift + 2.5 * circleRadiusSize));
        labelV.setAttribute(CLASS, LABEL_STYLE_CLASS);
        Text textV = g.getOwnerDocument().createTextNode(valueV);
        labelV.appendChild(textV);
        g.appendChild(labelV);

        // angle
        Element labelAngle = g.getOwnerDocument().createElement("text");
        labelAngle.setAttribute("id", idNode + "_angle");
        String valueAngle = !Double.isNaN(nodeInfo.getAngle())
                ? String.valueOf(Precision.round(nodeInfo.getAngle(), 1))
                : "\u2014";  // em dash unicode for undefined value
        valueAngle += " \u00b0";  // degree sign unicode for degree symbol

        labelAngle.setAttribute("x", String.valueOf(xShift - circleRadiusSize));
        labelAngle.setAttribute("y", String.valueOf(yShift + 4 * circleRadiusSize));
        labelAngle.setAttribute(CLASS, LABEL_STYLE_CLASS);
        Text textAngle = g.getOwnerDocument().createTextNode(valueAngle);
        labelAngle.appendChild(textAngle);
        g.appendChild(labelAngle);
    }

    private void drawNodesInfos(String prefixId,
                                Element root,
                                VoltageLevelGraph graph,
                                DiagramStyleProvider styleProvider) {
        double xInitPos = graph.getNodes().stream()
                .filter(n -> n.getType() == Node.NodeType.BUS)
                .mapToDouble(Node::getX).min().getAsDouble() + layoutParameters.getTranslateX() + CIRCLE_RADIUS_NODE_INFOS_SIZE;

        double maxY = graph.getNodes().stream().mapToDouble(Node::getY).max().getAsDouble();
        double yPos = graph.getY() + layoutParameters.getInitialYBus() + maxY - 120;

        List<ElectricalNodeInfo> nodes = styleProvider.getElectricalNodesInfos(graph);

        IntStream.range(0, nodes.size()).forEach(i -> {
            String idNode = prefixId + "NODE_" + i + "_" + graph.getVoltageLevelInfos().getId();
            Element gNode = root.getOwnerDocument().createElement(GROUP);
            gNode.setAttribute("id", idNode);

            drawNodeInfos(nodes.get(i), graph.getX() + xInitPos + (i * (2 * CIRCLE_RADIUS_NODE_INFOS_SIZE + 50)), yPos, gNode, idNode, CIRCLE_RADIUS_NODE_INFOS_SIZE);
            root.appendChild(gNode);
        });
    }
}
