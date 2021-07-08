/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.library;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class SubComponentAdapter extends XmlAdapter<AdaptedSubComponent, SubComponent> {

    @Override
    public SubComponent unmarshal(AdaptedSubComponent adapted) {
        return new SubComponent(adapted.getName(), adapted.getFileName(), adapted.getStyleClass());
    }

    @Override
    public AdaptedSubComponent marshal(SubComponent subComponent) {
        AdaptedSubComponent adapted = new AdaptedSubComponent();
        adapted.setName(subComponent.getName());
        adapted.setFileName(subComponent.getFileName());

        return adapted;
    }
}
