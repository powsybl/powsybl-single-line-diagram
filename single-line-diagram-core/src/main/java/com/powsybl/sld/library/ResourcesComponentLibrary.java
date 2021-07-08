/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.library;

import com.powsybl.commons.exceptions.UncheckedSaxException;
import com.powsybl.sld.util.DomUtil;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.*;

/**
 * Library of resources components, that is, the SVG image files representing the components, together with the styles
 * associated to each component
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class ResourcesComponentLibrary implements ComponentLibrary {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourcesComponentLibrary.class);

    private final String name;

    private final Map<String, Map<String, List<Element>>> svgDocuments = new HashMap<>();

    private final Map<String, Component> components = new HashMap<>();

    private final List<String> cssFilenames = new ArrayList<>();

    private final List<URL> cssUrls = new ArrayList<>();

    /**
     * Constructs a new library containing the components in the given directories
     * @param name name of the library
     * @param directory main directory containing the resources components: SVG files, with associated components.xml
     *                 (containing the list of SVG files) and components.css (containing the style applied to each
     *                  component)
     * @param additionalDirectories directories for additional components (each directory containing SVG files,
     *                              associated components.xml and components.css).
     */
    public ResourcesComponentLibrary(String name, String directory, String... additionalDirectories) {
        this.name = Objects.requireNonNull(name);
        Objects.requireNonNull(directory);
        loadLibrary(directory);
        for (String addDir : additionalDirectories) {
            loadLibrary(addDir);
        }
    }

    @Override
    public String getName() {
        return name;
    }

    private void loadLibrary(String directory) {
        LOGGER.info("Loading component library from {}...", directory);

        // preload SVG documents
        DocumentBuilder db = DomUtil.getDocumentBuilder();
        Components.load(directory).getComponents().forEach(c -> {
            ComponentMetadata componentMetaData = c.getMetadata();
            String componentType = componentMetaData.getType();
            componentMetaData.getSubComponents().forEach(s -> {
                String resourceName = directory + "/" + s.getFileName();
                LOGGER.debug("Reading subComponent {}", resourceName);
                try {
                    Document doc = db.parse(getClass().getResourceAsStream(resourceName));
                    svgDocuments.computeIfAbsent(componentType, k -> new TreeMap<>()).put(s.getName(), getElements(doc));
                } catch (SAXException e) {
                    throw new UncheckedSaxException(e);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
            components.put(componentType, c);
        });

        cssFilenames.add(FilenameUtils.getName(directory) + "_components.css");
        cssUrls.add(getClass().getResource(directory + "/components.css"));
    }

    protected List<Element> getElements(Document doc) {
        // Getting the node corresponding to the svg tag
        Node svgNode = doc.getChildNodes().item(0);

        // Listing all the elements which are children of the svg node.
        List<Element> elements = new ArrayList<>();
        NodeList subComponentChildren = svgNode.getChildNodes();
        for (int i = 0; i < subComponentChildren.getLength(); i++) {
            org.w3c.dom.Node n = subComponentChildren.item(i);
            if (n instanceof Element) {
                elements.add((Element) n.cloneNode(true));
            }
        }
        return elements;
    }

    @Override
    public Map<String, List<Element>> getSvgElements(String type) {
        Objects.requireNonNull(type);
        return svgDocuments.get(type);
    }

    @Override
    public List<AnchorPoint> getAnchorPoints(String type) {
        Objects.requireNonNull(type);
        Component component = components.get(type);
        return component != null ? component.getMetadata().getAnchorPoints()
                                 : Collections.singletonList(new AnchorPoint(0, 0, AnchorOrientation.NONE));
    }

    @Override
    public ComponentSize getSize(String type) {
        Objects.requireNonNull(type);
        Component component = components.get(type);
        return component != null ? component.getMetadata().getSize() : new ComponentSize(0, 0);
    }

    @Override
    public Map<String, ComponentSize> getComponentsSize() {
        Map<String, ComponentSize> res = new HashMap<>();
        components.forEach((key, value) -> res.put(key, value.getMetadata().getSize()));
        return res;
    }

    @Override
    public List<String> getCssFilenames() {
        return cssFilenames;
    }

    @Override
    public List<URL> getCssUrls() {
        return cssUrls;
    }

    @Override
    public Optional<String> getComponentStyleClass(String type) {
        Objects.requireNonNull(type);
        Component component = components.get(type);
        return component != null ? Optional.ofNullable(component.getMetadata().getStyleClass()) : Optional.empty();
    }

    @Override
    public Optional<String> getSubComponentStyleClass(String type, String subComponent) {
        Objects.requireNonNull(type);
        Objects.requireNonNull(subComponent);
        Component component = components.get(type);
        if (component != null) {
            return component.getMetadata().getSubComponents().stream().filter(sc -> sc.getName().equals(subComponent)).findFirst().map(SubComponent::getStyleClass);
        }
        return Optional.empty();
    }

    @Override
    public boolean isAllowRotation(String type) {
        Objects.requireNonNull(type);
        Component component = components.get(type);
        return component == null || component.getMetadata().isAllowRotation();
    }

}
