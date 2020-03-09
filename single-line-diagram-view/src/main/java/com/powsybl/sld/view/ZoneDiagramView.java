/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.view;

import javafx.scene.Group;

import java.io.InputStream;
import java.util.Objects;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public final class ZoneDiagramView extends AbstractContainerDiagramView {
    private ZoneDiagramView(Group svgImage) {
        super(svgImage);
    }

    public static ZoneDiagramView load(InputStream svgInputStream,
                                       InputStream metadataInputStream,
                                       SwitchPositionChangeListener listener,
                                       DisplayVoltageLevel displayVL) {
        Objects.requireNonNull(svgInputStream);
        Objects.requireNonNull(metadataInputStream);

        Group svgImage = loadSvgAndMetadata(svgInputStream, metadataInputStream, listener, displayVL);

        return new ZoneDiagramView(svgImage);
    }
}
