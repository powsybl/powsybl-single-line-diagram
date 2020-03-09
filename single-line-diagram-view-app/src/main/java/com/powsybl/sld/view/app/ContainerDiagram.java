/*
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.view.app;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Container;
import com.powsybl.iidm.network.Network;
import com.powsybl.sld.ZoneId;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class ContainerDiagram {
    public enum ContainerDiagramType {
        VOLTAGE_LEVEL,
        SUBSTATION,
        ZONE
    }

    private ContainerDiagramType type;
    private List<Container> containers;
    private String containerName;

    public ContainerDiagram(ContainerDiagramType type, List<Container> containers) {
        this.type = Objects.requireNonNull(type);
        this.containers = Objects.requireNonNull(containers);
        if (containers.isEmpty()) {
            throw new PowsyblException("Container diagram without any container");
        }
    }

    public Network getNetwork() {
        return containers.get(0).getNetwork();
    }

    public ContainerDiagramType getContainerDiagramType() {
        return type;
    }

    public String getId() {
        if (containers.size() == 1) {
            return (containerName != null ? containerName : "") + containers.get(0).getId();
        } else {
            return (containerName != null ? containerName : "") + "[" + containers.stream().map(Container::getId).collect(Collectors.joining(",")) + "]";
        }
    }

    public String getName() {
        if (containers.size() == 1) {
            return (containerName != null ? containerName : "") + containers.get(0).getName();
        } else {
            return (containerName != null ? containerName : "") + "[" + containers.stream().map(Container::getName).collect(Collectors.joining(",")) + "]";
        }
    }

    public ZoneId getZoneId() {
        return ZoneId.create(containers.stream().map(Container::getId).collect(Collectors.toList()));
    }

    public void setContainerName(String containerName) {
        this.containerName = containerName;
    }
}
