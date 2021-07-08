/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.library;

import com.google.common.collect.Lists;
import org.w3c.dom.Element;

import java.net.URL;
import java.util.*;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public interface ComponentLibrary {

    static List<ComponentLibrary> findAll() {
        return Lists.newArrayList(ServiceLoader.load(ComponentLibrary.class));
    }

    static Optional<ComponentLibrary> find(String name) {
        Objects.requireNonNull(name);
        return findAll().stream().filter(cl -> cl.getName().equals(name)).findFirst();
    }

    String getName();

    List<AnchorPoint> getAnchorPoints(String type);

    Map<String, List<Element>> getSvgElements(String type);

    ComponentSize getSize(String type);

    boolean isAllowRotation(String type);

    Map<String, ComponentSize> getComponentsSize();

    List<String> getCssFilenames();

    List<URL> getCssUrls();

    Optional<String> getComponentStyleClass(String componentType);

    Optional<String> getSubComponentStyleClass(String type, String subComponent);
}
