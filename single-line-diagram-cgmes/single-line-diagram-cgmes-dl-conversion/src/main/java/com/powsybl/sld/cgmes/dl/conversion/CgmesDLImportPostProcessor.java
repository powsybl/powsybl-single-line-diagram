/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.cgmes.dl.conversion;

import com.google.auto.service.AutoService;
import com.powsybl.cgmes.conversion.CgmesImportPostProcessor;
import com.powsybl.cgmes.model.CgmesSubset;
import com.powsybl.iidm.network.Network;
import com.powsybl.triplestore.api.QueryCatalog;
import com.powsybl.triplestore.api.TripleStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
@AutoService(CgmesImportPostProcessor.class)
public class CgmesDLImportPostProcessor implements CgmesImportPostProcessor {

    private static final String NAME = "cgmesDLImport";
    private static final Logger LOG = LoggerFactory.getLogger(CgmesDLImportPostProcessor.class);

    private final QueryCatalog queryCatalog;

    CgmesDLImportPostProcessor(QueryCatalog queryCatalog) {
        this.queryCatalog = Objects.requireNonNull(queryCatalog);
    }

    public CgmesDLImportPostProcessor() {
        this(new QueryCatalog("CGMES-DL.sparql"));
    }

    @Override
    public String getName() {
        return NAME;
    }

    private boolean isDlProfileAvailable(Network network, TripleStore tripleStore) {
        return (tripleStore != null) && (tripleStore.contextNames().contains(ContextUtils.contextNameFor(CgmesSubset.DIAGRAM_LAYOUT, tripleStore, network.getId())));
    }

    @Override
    public void process(Network network, TripleStore tripleStore) {
        if (isDlProfileAvailable(network, tripleStore)) {
            LOG.info("Execute {} CGMES import post processor on network {}", getName(), network.getId());

            CgmesDLModel cgmesDLModel = new CgmesDLModel(tripleStore, queryCatalog);
            new CgmesDLImporter(network, cgmesDLModel).importDLData();
        } else {
            LOG.info("DL profile not found for network {}", network.getId());
        }
    }
}
