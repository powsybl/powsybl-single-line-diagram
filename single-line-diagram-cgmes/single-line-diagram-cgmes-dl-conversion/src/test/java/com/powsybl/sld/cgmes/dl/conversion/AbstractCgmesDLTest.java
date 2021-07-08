/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.cgmes.dl.conversion;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Before;

import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public abstract class AbstractCgmesDLTest {

    protected static String NAMESPACE = "http://network#";

    protected static String DEFAULT_DIAGRAM_NAME = "default";

    protected PropertyBags terminalsPropertyBags;
    protected PropertyBags busesPropertyBags;
    protected PropertyBags busbarsPropertyBags;
    protected PropertyBags linesPropertyBags;
    protected PropertyBags danglingLinesPropertyBags;
    protected PropertyBags generatorsPropertyBags;
    protected PropertyBags loadsPropertyBags;
    protected PropertyBags shuntsPropertyBags;
    protected PropertyBags switchesPropertyBags;
    protected PropertyBags tranformersPropertyBags;
    protected PropertyBags tranformers3wPropertyBags;
    protected PropertyBags hvdcLinesPropertyBags;
    protected PropertyBags svcsPropertyBags;
    protected PropertyBags terminals;
    protected PropertyBags busbarNodes;
    protected PropertyBags voltageLevels;

    @Before
    public void setUp() {
        terminalsPropertyBags = new PropertyBags(Arrays.asList(createTerminalPropertyBag(NAMESPACE + "Generator", "1", 2, 10, 1),
                                                               createTerminalPropertyBag(NAMESPACE + "Generator", "1", 6, 10, 2),
                                                               createTerminalPropertyBag(NAMESPACE + "Load", "1", 2, 10, 1),
                                                               createTerminalPropertyBag(NAMESPACE + "Load", "1", 6, 10, 2),
                                                               createTerminalPropertyBag(NAMESPACE + "Shunt", "1", 2, 10, 1),
                                                               createTerminalPropertyBag(NAMESPACE + "Shunt", "1", 6, 10, 2),
                                                               createTerminalPropertyBag(NAMESPACE + "Switch", "1", 2, 10, 1),
                                                               createTerminalPropertyBag(NAMESPACE + "Switch", "1", 6, 10, 2),
                                                               createTerminalPropertyBag(NAMESPACE + "Switch", "2", 14, 10, 1),
                                                               createTerminalPropertyBag(NAMESPACE + "Switch", "2", 18, 10, 2),
                                                               createTerminalPropertyBag(NAMESPACE + "Transformer", "1", 2, 10, 1),
                                                               createTerminalPropertyBag(NAMESPACE + "Transformer", "1", 6, 10, 2),
                                                               createTerminalPropertyBag(NAMESPACE + "Transformer", "2", 14, 10, 1),
                                                               createTerminalPropertyBag(NAMESPACE + "Transformer", "2", 18, 10, 2),
                                                               createTerminalPropertyBag(NAMESPACE + "Transformer3w", "1", 2, 10, 1),
                                                               createTerminalPropertyBag(NAMESPACE + "Transformer3w", "1", 6, 10, 2),
                                                               createTerminalPropertyBag(NAMESPACE + "Transformer3w", "2", 14, 10, 1),
                                                               createTerminalPropertyBag(NAMESPACE + "Transformer3w", "2", 18, 10, 2),
                                                               createTerminalPropertyBag(NAMESPACE + "Transformer3w", "3", 10, 16, 1),
                                                               createTerminalPropertyBag(NAMESPACE + "Transformer3w", "3", 10, 20, 2),
                                                               createTerminalPropertyBag(NAMESPACE + "Svc", "1", 2, 10, 1),
                                                               createTerminalPropertyBag(NAMESPACE + "Svc", "1", 6, 10, 2)));
        busesPropertyBags = new PropertyBags(Arrays.asList(createBusPropertyBag(NAMESPACE + "Bus", "Bus", NAMESPACE + "VoltageLevel", "VoltageLevel", 20, 5, 1),
                                                           createBusPropertyBag(NAMESPACE + "Bus", "Bus", NAMESPACE + "VoltageLevel", "VoltageLevel", 20, 40, 2)));
        busbarsPropertyBags = new PropertyBags(Arrays.asList(createBusbarPropertyBag(NAMESPACE + "Busbar", "Busbar", 20, 5, 1),
                                                             createBusbarPropertyBag(NAMESPACE + "Busbar", "Busbar", 20, 40, 2)));
        linesPropertyBags = new PropertyBags(Arrays.asList(createPropertyBag(NAMESPACE + "Line", "Line", 20, 5, 1),
                                                           createPropertyBag(NAMESPACE + "Line", "Line", 20, 40, 2)));
        danglingLinesPropertyBags = new PropertyBags(Arrays.asList(createPropertyBag(NAMESPACE + "DanglingLine", "DanglingLine", 20, 5, 1),
                                                                   createPropertyBag(NAMESPACE + "DanglingLine", "DanglingLine", 20, 40, 2)));
        generatorsPropertyBags = new PropertyBags(Arrays.asList(createPropertyBag(NAMESPACE + "Generator", "Generator", 10, 10, 0, 90)));
        loadsPropertyBags = new PropertyBags(Arrays.asList(createPropertyBag(NAMESPACE + "Load", "Load", 10, 10, 0, 90)));
        shuntsPropertyBags = new PropertyBags(Arrays.asList(createPropertyBag(NAMESPACE + "Shunt", "Shunt", 10, 10, 0, 90)));
        switchesPropertyBags = new PropertyBags(Arrays.asList(createSwitchPropertyBag(NAMESPACE + "Switch", "Switch", 10, 10, 90)));
        tranformersPropertyBags = new PropertyBags(Arrays.asList(createPropertyBag(NAMESPACE + "Transformer", "Transformer", 10, 10, 0, 90)));
        tranformers3wPropertyBags = new PropertyBags(Arrays.asList(createPropertyBag(NAMESPACE + "Transformer3w", "Transformer3w", 10, 13, 0, 90)));
        hvdcLinesPropertyBags = new PropertyBags(Arrays.asList(createPropertyBag(NAMESPACE + "HvdcLine", "HvdcLine", 20, 5, 1),
                                                               createPropertyBag(NAMESPACE + "HvdcLine", "HvdcLine", 20, 40, 2)));
        svcsPropertyBags = new PropertyBags(Arrays.asList(createPropertyBag(NAMESPACE + "Svc", "Svc", 10, 10, 0, 90)));
        terminals = new PropertyBags(Arrays.asList(createTerminal(NAMESPACE + "Generator_0", 1, NAMESPACE + "Generator"),
                                                   createTerminal(NAMESPACE + "Load_0", 1, NAMESPACE + "Load"),
                                                   createTerminal(NAMESPACE + "Shunt_0", 1, NAMESPACE + "Shunt"),
                                                   createTerminal(NAMESPACE + "Svc_0", 1, NAMESPACE + "Svc"),
                                                   createTerminal(NAMESPACE + "Line_0", 1, NAMESPACE + "Line"),
                                                   createTerminal(NAMESPACE + "Line_1", 2, NAMESPACE + "Line"),
                                                   createTerminal(NAMESPACE + "DanglingLine_0", 1, NAMESPACE + "DanglingLine"),
                                                   createTerminal(NAMESPACE + "DanglingLine_1", 2, NAMESPACE + "DanglingLine"),
                                                   createTerminal(NAMESPACE + "Switch_0", 1, NAMESPACE + "Switch"),
                                                   createTerminal(NAMESPACE + "Switch_1", 2, NAMESPACE + "Switch"),
                                                   createTerminal(NAMESPACE + "Transformer_0", 1, NAMESPACE + "Transformer"),
                                                   createTerminal(NAMESPACE + "Transformer_1", 2, NAMESPACE + "Transformer"),
                                                   createTerminal(NAMESPACE + "Transformer3w_0", 1, NAMESPACE + "Transformer3w"),
                                                   createTerminal(NAMESPACE + "Transformer3w_1", 2, NAMESPACE + "Transformer3w"),
                                                   createTerminal(NAMESPACE + "Transformer3w_0", 3, NAMESPACE + "Transformer3w"),
                                                   createTerminal(NAMESPACE + "HvdcLine_0", 1, NAMESPACE + "HvdcLine"),
                                                   createTerminal(NAMESPACE + "HvdcLine_1", 2, NAMESPACE + "HvdcLine")));
        busbarNodes = new PropertyBags(Arrays.asList(createBusbarNode(NAMESPACE + "Busbar", NAMESPACE + "BusbarNode")));
        voltageLevels = new PropertyBags(Collections.emptyList());
    }

    protected PropertyBag createPropertyBag(String identifiedObject, String name, double x, double y, int seq, String diagramName) {
        PropertyBag propertyBag = new PropertyBag(Arrays.asList("identifiedObject", "name", "x", "y", "seq"));
        propertyBag.put("identifiedObject", identifiedObject);
        propertyBag.put("name", name);
        propertyBag.put("x", Double.toString(x));
        propertyBag.put("y", Double.toString(y));
        propertyBag.put("seq", Integer.toString(seq));
        propertyBag.put("diagramName", diagramName);
        return propertyBag;
    }

    protected PropertyBag createPropertyBag(String identifiedObject, String name, double x, double y, int seq) {
        return createPropertyBag(identifiedObject, name, x, y, seq, DEFAULT_DIAGRAM_NAME);
    }

    protected PropertyBag createPropertyBag(String identifiedObject, String name, double x, double y, int seq, int rotation, String diagramName) {
        PropertyBag propertyBag = new PropertyBag(Arrays.asList("identifiedObject", "name", "x", "y", "seq", "rotation"));
        propertyBag.put("identifiedObject", identifiedObject);
        propertyBag.put("name", name);
        propertyBag.put("x", Double.toString(x));
        propertyBag.put("y", Double.toString(y));
        propertyBag.put("seq", Integer.toString(seq));
        propertyBag.put("rotation", Integer.toString(rotation));
        propertyBag.put("diagramName", diagramName);
        return propertyBag;
    }

    protected PropertyBag createPropertyBag(String identifiedObject, String name, double x, double y, int seq, int rotation) {
        return createPropertyBag(identifiedObject, name, x, y, seq, rotation, DEFAULT_DIAGRAM_NAME);
    }

    protected PropertyBag createTerminalPropertyBag(String terminalEquipment, String terminalSide, double x, double y, int seq, String diagramName) {
        PropertyBag propertyBag = new PropertyBag(Arrays.asList("terminalEquipment", "terminalSide", "x", "y", "seq"));
        propertyBag.put("terminalEquipment", terminalEquipment);
        propertyBag.put("terminalSide", terminalSide);
        propertyBag.put("x", Double.toString(x));
        propertyBag.put("y", Double.toString(y));
        propertyBag.put("seq", Integer.toString(seq));
        propertyBag.put("diagramName", diagramName);
        return propertyBag;
    }

    protected PropertyBag createTerminalPropertyBag(String terminalEquipment, String terminalSide, double x, double y, int seq) {
        return createTerminalPropertyBag(terminalEquipment, terminalSide, x, y, seq, DEFAULT_DIAGRAM_NAME);
    }

    protected PropertyBag createBusPropertyBag(String identifiedObject, String name, String voltageLevel, String vlName, double x, double y, int seq, String diagramName) {
        PropertyBag propertyBag = new PropertyBag(Arrays.asList("identifiedObject", "name", "voltageLevel", "vlname", "x", "y", "seq"));
        propertyBag.put("identifiedObject", identifiedObject);
        propertyBag.put("name", name);
        propertyBag.put("voltageLevel", voltageLevel);
        propertyBag.put("vlname", vlName);
        propertyBag.put("x", Double.toString(x));
        propertyBag.put("y", Double.toString(y));
        propertyBag.put("seq", Integer.toString(seq));
        propertyBag.put("diagramName", diagramName);
        return propertyBag;
    }

    protected PropertyBag createBusPropertyBag(String identifiedObject, String name, String voltageLevel, String vlName, double x, double y, int seq) {
        return createBusPropertyBag(identifiedObject, name, voltageLevel, vlName, x, y, seq, DEFAULT_DIAGRAM_NAME);
    }

    protected PropertyBag createBusbarPropertyBag(String identifiedObject, String name, double x, double y, int seq, String diagramName) {
        PropertyBag propertyBag = new PropertyBag(Arrays.asList("identifiedObject", "name", "x", "y", "seq"));
        propertyBag.put("busbarSection", identifiedObject);
        propertyBag.put("name", name);
        propertyBag.put("x", Double.toString(x));
        propertyBag.put("y", Double.toString(y));
        propertyBag.put("seq", Integer.toString(seq));
        propertyBag.put("diagramName", diagramName);
        return propertyBag;
    }

    protected PropertyBag createBusbarPropertyBag(String identifiedObject, String name, double x, double y, int seq) {
        return createBusbarPropertyBag(identifiedObject, name, x, y, seq, DEFAULT_DIAGRAM_NAME);
    }

    protected PropertyBag createSwitchPropertyBag(String identifiedObject, String name, double x, double y, int rotation, String diagramName) {
        PropertyBag propertyBag = new PropertyBag(Arrays.asList("identifiedObject", "name", "x", "y", "rotation"));
        propertyBag.put("identifiedObject", identifiedObject);
        propertyBag.put("name", name);
        propertyBag.put("x", Double.toString(x));
        propertyBag.put("y", Double.toString(y));
        propertyBag.put("rotation", Integer.toString(rotation));
        propertyBag.put("diagramName", diagramName);
        return propertyBag;
    }

    protected PropertyBag createSwitchPropertyBag(String identifiedObject, String name, double x, double y, int rotation) {
        return createSwitchPropertyBag(identifiedObject, name, x, y, rotation, DEFAULT_DIAGRAM_NAME);
    }

    protected PropertyBag createTerminal(String terminal, int terminalSide, String equipment) {
        PropertyBag propertyBag = new PropertyBag(Arrays.asList("terminal", "terminalSide", "equipment"));
        propertyBag.put("terminal", terminal);
        propertyBag.put("terminalSide", Integer.toString(terminalSide));
        propertyBag.put("equipment", equipment);
        return propertyBag;
    }

    protected PropertyBag createBusbarNode(String busbar, String busbarNode) {
        PropertyBag propertyBag = new PropertyBag(Arrays.asList("busbarNode", "busbarSection"));
        propertyBag.put("busbarNode", busbarNode);
        propertyBag.put("busbarSection", busbar);
        return propertyBag;
    }

    protected PropertyBag createVoltageLevelPropertyBag(String connectivityNode, String name, String aSwitch, double x, double y, int seq, String diagramName) {
        PropertyBag propertyBag = new PropertyBag(Arrays.asList("connectivityNode", "switch", "name", "type", "x", "y", "seq"));
        propertyBag.put("connectivityNode", connectivityNode);
        propertyBag.put("switch", aSwitch);
        propertyBag.put("name", name);
        propertyBag.put("type", "");
        propertyBag.put("x", Double.toString(x));
        propertyBag.put("y", Double.toString(y));
        propertyBag.put("seq", Integer.toString(seq));
        propertyBag.put("diagramName", diagramName);
        return propertyBag;
    }

    protected PropertyBag createVoltageLevelPropertyBag(String connectivityNode, String name, String aSwitch, double x, double y, int seq) {
        return createVoltageLevelPropertyBag(connectivityNode, name, aSwitch, x, y, seq, DEFAULT_DIAGRAM_NAME);
    }
}
