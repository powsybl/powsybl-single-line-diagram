package com.powsybl.sld.force.layout;

import com.google.common.io.ByteStreams;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.Pseudograph;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static org.junit.Assert.assertEquals;

public class TestForceLayout {

    public String toString(String resourceName) {
        try {
            InputStream resourceIs = Objects.requireNonNull(getClass().getResourceAsStream(resourceName));
            return normalizeLineSeparator(new String(ByteStreams.toByteArray(resourceIs), StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    protected static String normalizeLineSeparator(String str) {
        return str.replace("\r\n", "\n")
            .replace("\r", "\n");
    }

    @Test
    public void test4Points() {
        String neoYokio = "NeoYokio";
        String tokyo = "Tokyo";
        String kyoto = "Kyoto";
        String osaka = "Osaka";

        Graph<String, DefaultEdge> graph = new Pseudograph<>(DefaultEdge.class);
        graph.addVertex(neoYokio);
        graph.addVertex(tokyo);
        graph.addVertex(kyoto);
        graph.addVertex(osaka);

        graph.addEdge(neoYokio, tokyo);
        graph.addEdge(tokyo, kyoto);
        graph.addEdge(kyoto, osaka);
        graph.addEdge(osaka, tokyo);

        ForceLayout<String, DefaultEdge> forceLayout = new ForceLayout<>(graph);
        forceLayout.execute();

        StringWriter svgSw = new StringWriter();
        forceLayout.toSVG(s -> s, svgSw);

        assertEquals(toString("/Graph4Points.svg"), normalizeLineSeparator(svgSw.toString()));
    }

    @Test
    public void test10Points() {
        Graph<Integer, DefaultEdge> graph = new Pseudograph<>(DefaultEdge.class);

        for (int i = 0; i < 10; i++) {
            graph.addVertex(i);
        }

        graph.addEdge(0, 1);
        graph.addEdge(0, 3);
        graph.addEdge(3, 4);
        graph.addEdge(3, 6);
        graph.addEdge(7, 9);
        graph.addEdge(5, 9);
        graph.addEdge(8, 3);
        graph.addEdge(1, 9);
        graph.addEdge(1, 2);
        graph.addEdge(4, 2);
        graph.addEdge(7, 2);
        // graph.addEdge(2, 2); // does not seem to end if we set self-loop node

        ForceLayout<Integer, DefaultEdge> forceLayout = new ForceLayout<>(graph);
        forceLayout.execute();

        StringWriter svgSw = new StringWriter();
        forceLayout.toSVG(i -> Integer.toString(i), svgSw);

        assertEquals(toString("/Graph10Points.svg"), normalizeLineSeparator(svgSw.toString()));
    }
}
