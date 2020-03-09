/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.view;

import javafx.scene.Group;

import java.io.InputStream;
import java.util.Objects;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public final class VoltageLevelDiagramView extends AbstractContainerDiagramView {
    private VoltageLevelDiagramView(Group svgImage) {
        super(svgImage);
    }

    public static VoltageLevelDiagramView load(InputStream svgInputStream,
                                               InputStream metadataInputStream,
                                               SwitchPositionChangeListener listener,
                                               DisplayVoltageLevel displayVL) {
        Objects.requireNonNull(svgInputStream);
        Objects.requireNonNull(metadataInputStream);

        Group svgImage = loadSvgAndMetadata(svgInputStream, metadataInputStream, listener, displayVL);

        return new VoltageLevelDiagramView(svgImage);
    }
}
