package com.tsel.home.project.booklibrary.ui;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import org.w3c.dom.Element;

/** Loads the bundled path-only SVGs without rasterization or external resource access. */
final class SvgIcons {
    private static final Map<String, List<PathData>> CACHE = new ConcurrentHashMap<>();
    private SvgIcons() {}

    static List<SVGPath> createPaths(String name) {
        return CACHE.computeIfAbsent(name, SvgIcons::load).stream().map(data -> {
            SVGPath path = new SVGPath();
            path.setContent(data.content());
            path.setFill(paint(data.fill()));
            path.setStroke(paint(data.stroke()));
            path.setStrokeWidth(data.width());
            path.setStrokeLineCap(StrokeLineCap.ROUND);
            path.setStrokeLineJoin(StrokeLineJoin.ROUND);
            if (!"none".equals(data.fill())) path.getStyleClass().add("icon-fill");
            if (!"none".equals(data.stroke())) path.getStyleClass().add("icon-stroke");
            return path;
        }).toList();
    }

    static SVGPath singlePath(String name) {
        List<SVGPath> paths = createPaths(name);
        if (paths.size() != 1) throw new IllegalArgumentException("Expected a single path: " + name);
        return paths.get(0);
    }

    private static Color paint(String value) { return "none".equals(value) ? Color.TRANSPARENT : Color.web(value); }

    private static List<PathData> load(String name) {
        if (!name.matches("[a-z-]+")) throw new IllegalArgumentException("Invalid icon name: " + name);
        String resource = "/com/tsel/home/project/booklibrary/icons/" + name + ".svg";
        try (InputStream input = SvgIcons.class.getResourceAsStream(resource)) {
            if (input == null) throw new IllegalArgumentException("Unknown icon: " + name);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newDefaultInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            var nodes = factory.newDocumentBuilder().parse(input).getElementsByTagName("path");
            List<PathData> paths = new ArrayList<>();
            for (int i = 0; i < nodes.getLength(); i++) {
                Element path = (Element) nodes.item(i);
                paths.add(new PathData(path.getAttribute("d"), path.getAttribute("fill"),
                    path.getAttribute("stroke"), Double.parseDouble(path.getAttribute("stroke-width"))));
            }
            if (paths.isEmpty()) throw new IllegalArgumentException("Empty SVG icon: " + name);
            return List.copyOf(paths);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot load SVG icon: " + name, e);
        }
    }

    private record PathData(String content, String fill, String stroke, double width) {}
}
