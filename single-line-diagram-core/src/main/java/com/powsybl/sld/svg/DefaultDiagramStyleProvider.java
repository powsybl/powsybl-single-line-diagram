/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.svg;

import com.powsybl.sld.layout.LayoutParameters;
import com.powsybl.sld.library.ComponentLibrary;
import com.powsybl.sld.library.ComponentTypeName;
import com.powsybl.sld.model.*;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.powsybl.sld.svg.DiagramStyles.*;

/**
 * @author Giovanni Ferrari <giovanni.ferrari at techrain.eu>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class DefaultDiagramStyleProvider implements DiagramStyleProvider {

    @Override
    public List<String> getSvgWireStyles(Edge edge, boolean highlightLineState) {
        List<String> styles = new ArrayList<>();
        styles.add(WIRE_STYLE_CLASS);
        if (isConstantColor(edge)) {
            styles.add(CONSTANT_COLOR_CLASS);
        }
        getEdgeStyle(edge).ifPresent(styles::add);
        if (highlightLineState) {
            getHighlightLineStateStyle(edge).ifPresent(styles::add);
        }
        return styles;
    }

    /**
     * Return the style if any applied to given edge
     * @param edge the edge on which the style if any is applied to
     * @return the style if any
     */
    protected Optional<String> getEdgeStyle(Edge edge) {
        return Optional.empty();
    }

    /**
     * Return the highlight style if any to apply to given edge (if {@link LayoutParameters#isHighlightLineState()})
     * @param edge the edge on which the style if any is applied to
     * @return the highlight style if any
     */
    protected Optional<String> getHighlightLineStateStyle(Edge edge) {
        return Optional.empty();
    }

    @Override
    public List<String> getSvgNodeStyles(Node node, ComponentLibrary componentLibrary, boolean showInternalNodes) {

        List<String> styles = new ArrayList<>();
        componentLibrary.getComponentStyleClass(node.getComponentType()).ifPresent(styles::add);

        if (node instanceof FeederNode && node.getCell() != null) {
            BusCell.Direction direction = ((BusCell) node.getCell()).getDirection();
            styles.add(direction == BusCell.Direction.BOTTOM ? DiagramStyles.BOTTOM_FEEDER : DiagramStyles.TOP_FEEDER);
        }
        if (isConstantColor(node)) {
            styles.add(CONSTANT_COLOR_CLASS);
        }
        if (!showInternalNodes && isEquivalentToInternalNode(node)) {
            styles.add(HIDDEN_NODE_CLASS);
        }
        if (node.getType() == Node.NodeType.SWITCH) {
            styles.add(node.isOpen() ? DiagramStyles.OPEN_SWITCH_STYLE_CLASS : DiagramStyles.CLOSED_SWITCH_STYLE_CLASS);
        }

        return styles;
    }

    private static boolean isEquivalentToInternalNode(Node node) {
        return node.getComponentType().equals(ComponentTypeName.NODE);
    }

    @Override
    public List<String> getSvgNodeDecoratorStyles(DiagramLabelProvider.NodeDecorator nodeDecorator, Node node, ComponentLibrary componentLibrary) {
        List<String> styles = new ArrayList<>();
        componentLibrary.getComponentStyleClass(nodeDecorator.getType()).ifPresent(styles::add);
        return styles;
    }

    @Override
    public List<String> getZoneLineStyles(LineEdge edge, ComponentLibrary componentLibrary) {
        List<String> styles = new ArrayList<>();
        styles.add(WIRE_STYLE_CLASS);
        if (isConstantColor(edge)) {
            styles.add(CONSTANT_COLOR_CLASS);
        }
        return styles;
    }

    /**
     * Return whether {@link DiagramStyles#CONSTANT_COLOR_CLASS} should be applied to the given node
     * @param node the node considered
     * @return true if the constant color class should be applied, false otherwise
     */
    protected boolean isConstantColor(Node node) {
        return true;
    }

    /**
     * Return whether {@link DiagramStyles#CONSTANT_COLOR_CLASS} should be applied to the given edge
     * @param edge the edge considered
     * @return true if the constant color class should be applied, false otherwise
     */
    protected boolean isConstantColor(Edge edge) {
        return true;
    }

    @Override
    public List<String> getSvgNodeSubcomponentStyles(Node node, String subComponentName) {
        return new ArrayList<>();
    }

    @Override
    public void reset() {
        // Nothing to reset for this implementation
    }

    @Override
    public List<String> getCssFilenames() {
        return Arrays.asList("tautologies.css", "defaultConstantColors.css");
    }

    @Override
    public List<URL> getCssUrls() {
        return getCssFilenames().stream().map(n -> getClass().getResource("/" + n))
            .collect(Collectors.toList());
    }
}
