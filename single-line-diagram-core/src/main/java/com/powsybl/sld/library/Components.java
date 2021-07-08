/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.library;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@XmlRootElement(name = "components")
public class Components {

    @XmlElement(name = "component")
    private final List<Component> components = new ArrayList<>();

    public List<Component> getComponents() {
        return components;
    }

    public static Components load(String directory) {
        return load(Components.class.getResourceAsStream(directory + "/components.xml"));
    }

    public static Components load(InputStream is) {
        try {
            JAXBContext jc = JAXBContext.newInstance(Components.class);
            Unmarshaller unmarshaller = jc.createUnmarshaller();
            return (Components) unmarshaller.unmarshal(is);
        } catch (JAXBException e) {
            throw new UncheckedJaxbException(e);
        }
    }

    private static class UncheckedJaxbException extends RuntimeException {
        public UncheckedJaxbException(JAXBException cause) {
            super(cause);
        }

        @Override
        public synchronized JAXBException getCause() {
            return (JAXBException) super.getCause();
        }
    }
}
