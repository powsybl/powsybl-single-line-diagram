/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.library;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@XmlJavaTypeAdapter(SubComponentAdapter.class)
public class SubComponent {

    private String name;

    private String fileName;

    private final String styleClass;

    public SubComponent(String name, String fileName, String styleClass) {
        this.name = name;
        this.fileName = fileName;
        this.styleClass = styleClass;
    }

    public String getName() {
        return name;
    }

    public String getFileName() {
        return fileName;
    }

    public String getStyleClass() {
        return styleClass;
    }
}
