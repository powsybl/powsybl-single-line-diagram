/*
 * Copyright 2012-2013 Gephi Consortium
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.powsybl.sld.force.layout.gephi.graph.api;

/**
 * Interface to retrieve added and removed elements from the graph.
 * <p>
 * This interface is associated with a {@link GraphObserver} and provides an
 * easy access to the elements added or removed.
 */
public interface GraphDiff {

    /**
     * Gets all added nodes.
     *
     * @return an iterable over added nodes
     */
    public NodeIterable getAddedNodes();

    /**
     * Gets all removed nodes.
     *
     * @return an iterable over removed nodes
     */
    public NodeIterable getRemovedNodes();

    /**
     * Gets all added edges.
     *
     * @return an iterable over added edges
     */
    public EdgeIterable getAddedEdges();

    /**
     * Gets all removed edges.
     *
     * @return an iterable over removed edges
     */
    public EdgeIterable getRemovedEdges();
}
