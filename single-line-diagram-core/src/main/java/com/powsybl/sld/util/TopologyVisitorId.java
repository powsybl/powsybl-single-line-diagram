/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.util;

import com.powsybl.iidm.network.*;

import java.util.function.Consumer;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public class TopologyVisitorId implements TopologyVisitor {

    private final Consumer<String> idFunction;

    public TopologyVisitorId(Consumer<String> idFunction) {
        this.idFunction = idFunction;
    }

    @Override
    public void visitBusbarSection(BusbarSection e) {
        idFunction.accept(e.getId());
    }

    @Override
    public void visitDanglingLine(DanglingLine e) {
        idFunction.accept(e.getId());
    }

    @Override
    public void visitGenerator(Generator e) {
        idFunction.accept(e.getId());
    }

    @Override
    public void visitLine(Line e, Branch.Side s) {
        idFunction.accept(e.getId() + "_" + s.name());
    }

    @Override
    public void visitLoad(Load e) {
        idFunction.accept(e.getId());
    }

    @Override
    public void visitShuntCompensator(ShuntCompensator e) {
        idFunction.accept(e.getId());
    }

    @Override
    public void visitStaticVarCompensator(StaticVarCompensator e) {
        idFunction.accept(e.getId());
    }

    @Override
    public void visitThreeWindingsTransformer(ThreeWindingsTransformer e,
                                              ThreeWindingsTransformer.Side s) {
        idFunction.accept(e.getId() + "_" + s.name());
    }

    @Override
    public void visitTwoWindingsTransformer(TwoWindingsTransformer e, Branch.Side s) {
        idFunction.accept(e.getId() + "_" + s.name());
    }
}
