/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.svg;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.sld.library.AnchorOrientation;
import com.powsybl.sld.library.AnchorPoint;
import com.powsybl.sld.library.ComponentMetadata;
import com.powsybl.sld.library.ComponentSize;
import com.powsybl.sld.model.BusCell;
import com.powsybl.sld.svg.GraphMetadata.ArrowMetadata;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.powsybl.sld.library.ComponentTypeName.*;
import static org.junit.Assert.*;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 * @author Nicolas Duchene
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class GraphMetadataTest {

    private FileSystem fileSystem;
    private Path tmpDir;

    @Before
    public void setUp() throws IOException {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        tmpDir = Files.createDirectory(fileSystem.getPath("/tmp"));
    }

    @After
    public void tearDown() throws Exception {
        fileSystem.close();
    }

    @Test
    public void test() throws IOException {
        GraphMetadata metadata = new GraphMetadata();
        metadata.addComponentMetadata(new ComponentMetadata(
            BREAKER,
            "br1",
            ImmutableList.of(new AnchorPoint(5, 4, AnchorOrientation.NONE)),
            new ComponentSize(10, 12), "breaker", true, null));

        metadata.addNodeMetadata(new GraphMetadata.NodeMetadata("id1", "vid1", null, BREAKER, null, false, BusCell.Direction.UNDEFINED, false, null));
        metadata.addNodeMetadata(new GraphMetadata.NodeMetadata("id2", "vid2", null, BUSBAR_SECTION, null, false, BusCell.Direction.UNDEFINED, false, null));
        metadata.addWireMetadata(new GraphMetadata.WireMetadata("id3", "id1", "id2", false, false));
        metadata.addArrowMetadata(new ArrowMetadata("id1", "id3", 20));

        ObjectMapper objectMapper = JsonUtil.createObjectMapper();
        String json = objectMapper.writerWithDefaultPrettyPrinter()
                                  .writeValueAsString(metadata);

        GraphMetadata metadata2 = objectMapper.readValue(json, GraphMetadata.class);
        assertEquals(1, metadata2.getComponentMetadata().size());
        assertNotNull(metadata2.getComponentMetadata(BREAKER));
        Assert.assertEquals(1, metadata2.getComponentMetadata(BREAKER).getAnchorPoints().size());
        Assert.assertEquals(5, metadata2.getComponentMetadata(BREAKER).getAnchorPoints().get(0).getX(), 0);
        Assert.assertEquals(4, metadata2.getComponentMetadata(BREAKER).getAnchorPoints().get(0).getY(), 0);
        Assert.assertEquals(AnchorOrientation.NONE, metadata2.getComponentMetadata(BREAKER).getAnchorPoints().get(0).getOrientation());
        Assert.assertEquals(10, metadata2.getComponentMetadata(BREAKER).getSize().getWidth(), 0);
        Assert.assertEquals(12, metadata2.getComponentMetadata(BREAKER).getSize().getHeight(), 0);
        assertEquals(2, metadata2.getNodeMetadata().size());
        assertNotNull(metadata2.getNodeMetadata("id1"));
        assertEquals("id1", metadata2.getNodeMetadata("id1").getId());
        assertEquals("vid1", metadata2.getNodeMetadata("id1").getVId());
        Assert.assertEquals(BREAKER, metadata2.getNodeMetadata("id1").getComponentType());
        Assert.assertEquals(BUSBAR_SECTION, metadata2.getNodeMetadata("id2").getComponentType());
        assertEquals("id2", metadata2.getNodeMetadata("id2").getId());
        assertEquals("vid2", metadata2.getNodeMetadata("id2").getVId());
        assertNotNull(metadata2.getNodeMetadata("id2"));
        assertEquals(1, metadata2.getWireMetadata().size());
        assertNotNull(metadata2.getWireMetadata("id3"));
        assertEquals("id3", metadata2.getWireMetadata("id3").getId());
        assertEquals("id1", metadata2.getWireMetadata("id3").getNodeId1());
        assertEquals("id2", metadata2.getWireMetadata("id3").getNodeId2());
        assertFalse(metadata2.getWireMetadata("id3").isStraight());
        assertEquals("id3", metadata2.getArrowMetadata("id1").getWireId());
        assertEquals(AnchorOrientation.NONE, metadata2.getAnchorPoints(BREAKER, "br1").get(0).getOrientation());
        assertEquals(5, metadata2.getAnchorPoints(BREAKER, "br1").get(0).getX(), 0);
        assertEquals(4, metadata2.getAnchorPoints(BREAKER, "br1").get(0).getY(), 0);

        Path meta = tmpDir.resolve("meta.json");
        metadata.writeJson(meta);
        GraphMetadata metadata3 = GraphMetadata.parseJson(meta);
        assertEquals(1, metadata3.getComponentMetadata().size());
        assertNotNull(metadata3.getComponentMetadata(BREAKER));
        Assert.assertEquals(1, metadata3.getComponentMetadata(BREAKER).getAnchorPoints().size());
        Assert.assertEquals(5, metadata3.getComponentMetadata(BREAKER).getAnchorPoints().get(0).getX(), 0);
        Assert.assertEquals(4, metadata3.getComponentMetadata(BREAKER).getAnchorPoints().get(0).getY(), 0);
        Assert.assertEquals(AnchorOrientation.NONE, metadata3.getComponentMetadata(BREAKER).getAnchorPoints().get(0).getOrientation());
        Assert.assertEquals(10, metadata3.getComponentMetadata(BREAKER).getSize().getWidth(), 0);
        Assert.assertEquals(12, metadata3.getComponentMetadata(BREAKER).getSize().getHeight(), 0);
        assertEquals(2, metadata3.getNodeMetadata().size());
        assertNotNull(metadata3.getNodeMetadata("id1"));
        assertEquals("id1", metadata3.getNodeMetadata("id1").getId());
        assertEquals("vid1", metadata3.getNodeMetadata("id1").getVId());
        Assert.assertEquals(BREAKER, metadata3.getNodeMetadata("id1").getComponentType());
        Assert.assertEquals(BUSBAR_SECTION, metadata3.getNodeMetadata("id2").getComponentType());
        assertEquals("id2", metadata3.getNodeMetadata("id2").getId());
        assertEquals("vid2", metadata3.getNodeMetadata("id2").getVId());
        assertNotNull(metadata3.getNodeMetadata("id2"));
        assertEquals(1, metadata3.getWireMetadata().size());
        assertNotNull(metadata3.getWireMetadata("id3"));
        assertEquals("id3", metadata3.getWireMetadata("id3").getId());
        assertEquals("id1", metadata3.getWireMetadata("id3").getNodeId1());
        assertEquals("id2", metadata3.getWireMetadata("id3").getNodeId2());
        assertFalse(metadata3.getWireMetadata("id3").isStraight());
        assertEquals("id3", metadata3.getArrowMetadata("id1").getWireId());
    }

    @Test
    public void testGraphMetadataWithLine() throws IOException {
        GraphMetadata metadata = new GraphMetadata();
        metadata.addNodeMetadata(new GraphMetadata.NodeMetadata("bid1", "vid1", null, BUSBAR_SECTION, null, false, BusCell.Direction.UNDEFINED, false, null));
        metadata.addNodeMetadata(new GraphMetadata.NodeMetadata("lid1", "vid1", null, LINE, null, false, BusCell.Direction.UNDEFINED, false, null));
        metadata.addWireMetadata(new GraphMetadata.WireMetadata("wid1", "bid1", "lid1", false, false));
        metadata.addNodeMetadata(new GraphMetadata.NodeMetadata("bid2", "vid2", null, BUSBAR_SECTION, null, false, BusCell.Direction.UNDEFINED, false, null));
        metadata.addNodeMetadata(new GraphMetadata.NodeMetadata("lid2", "vid2", null, LINE, null, false, BusCell.Direction.UNDEFINED, false, null));
        metadata.addWireMetadata(new GraphMetadata.WireMetadata("wid2", "bid2", "lid2", false, false));
        metadata.addLineMetadata(new GraphMetadata.LineMetadata("lid", "lid1", "lid2"));

        ObjectMapper objectMapper = JsonUtil.createObjectMapper();
        String json = objectMapper.writerWithDefaultPrettyPrinter()
                                  .writeValueAsString(metadata);
        GraphMetadata metadata2 = objectMapper.readValue(json, GraphMetadata.class);
        checkMetadata(metadata2);

        Path meta = tmpDir.resolve("meta.json");
        metadata.writeJson(meta);
        GraphMetadata metadata3 = GraphMetadata.parseJson(meta);
        checkMetadata(metadata3);
    }

    private void checkMetadata(GraphMetadata metadata)  {
        assertEquals(4, metadata.getNodeMetadata().size());
        assertNotNull(metadata.getNodeMetadata("bid1"));
        assertEquals("bid1", metadata.getNodeMetadata("bid1").getId());
        assertEquals("vid1", metadata.getNodeMetadata("bid1").getVId());
        assertEquals(BUSBAR_SECTION, metadata.getNodeMetadata("bid1").getComponentType());
        assertEquals("lid1", metadata.getNodeMetadata("lid1").getId());
        assertEquals("vid1", metadata.getNodeMetadata("lid1").getVId());
        assertEquals(LINE, metadata.getNodeMetadata("lid1").getComponentType());
        assertEquals("bid2", metadata.getNodeMetadata("bid2").getId());
        assertEquals("vid2", metadata.getNodeMetadata("bid2").getVId());
        assertEquals(BUSBAR_SECTION, metadata.getNodeMetadata("bid2").getComponentType());
        assertEquals("lid2", metadata.getNodeMetadata("lid2").getId());
        assertEquals("vid2", metadata.getNodeMetadata("lid2").getVId());
        assertEquals(LINE, metadata.getNodeMetadata("lid2").getComponentType());

        assertEquals(2, metadata.getWireMetadata().size());
        assertNotNull(metadata.getWireMetadata("wid1"));
        assertEquals("wid1", metadata.getWireMetadata("wid1").getId());
        assertEquals("bid1", metadata.getWireMetadata("wid1").getNodeId1());
        assertEquals("lid1", metadata.getWireMetadata("wid1").getNodeId2());
        assertNotNull(metadata.getWireMetadata("wid2"));
        assertEquals("wid2", metadata.getWireMetadata("wid2").getId());
        assertEquals("bid2", metadata.getWireMetadata("wid2").getNodeId1());
        assertEquals("lid2", metadata.getWireMetadata("wid2").getNodeId2());

        assertEquals(1, metadata.getLineMetadata().size());
        assertNotNull(metadata.getLineMetadata("lid"));
        assertEquals("lid", metadata.getLineMetadata("lid").getId());
        assertEquals("lid1", metadata.getLineMetadata("lid").getNodeId1());
        assertEquals("lid2", metadata.getLineMetadata("lid").getNodeId2());
    }

}
