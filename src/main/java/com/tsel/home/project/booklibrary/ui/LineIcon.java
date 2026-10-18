package com.tsel.home.project.booklibrary.ui;

import javafx.scene.Group;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;

/** Shared, resolution-independent outline icons in a 24-unit coordinate system. */
public final class LineIcon extends StackPane {
    private String name;
    public LineIcon() { setName("book"); }
    public LineIcon(String name) { setName(name); }
    public String getName() { return name; }

    public void setName(String name) {
        this.name = name;
        String path = switch (name) {
            case "plus" -> "M12 4 V20 M4 12 H20";
            case "search" -> "M21 21 L16 16 M18 10 A8 8 0 1 1 2 10 A8 8 0 1 1 18 10";
            case "filter" -> "M3 4 H21 L14 12 V20 L10 18 V12 Z";
            case "ruble" -> "M8 21 V3 H14 A5 5 0 0 1 14 13 H5 M5 17 H15";
            case "dice" -> "M4 3 H20 Q21 3 21 4 V20 Q21 21 20 21 H4 Q3 21 3 20 V4 Q3 3 4 3 M7 7 H8 M16 7 H17 M11.5 12 H12.5 M7 17 H8 M16 17 H17";
            case "folder" -> "M3 7 V4 H10 L12 7 H21 V20 H3 Z";
            case "close" -> "M5 5 L19 19 M19 5 L5 19";
            case "minimize" -> "M5 12 H19";
            case "maximize" -> "M5 5 H19 V19 H5 Z";
            case "restore" -> "M8 8 V4 H20 V16 H16 M4 8 H16 V20 H4 Z";
            case "down" -> "M6 9 L12 15 L18 9";
            default -> "M12 5 C9 2 4 2 2 3 V20 C6 19 9 20 12 22 C15 20 18 19 22 20 V3 C18 2 15 2 12 5 V22 M5 3 V17 C8 17 10 18 12 19 C14 18 16 17 19 17 V3";
        };
        SVGPath shape = new SVGPath();
        shape.setContent(path);
        shape.setFill(Color.TRANSPARENT);
        shape.setStroke(Color.web("#2F6B4F"));
        shape.setStrokeWidth(1.7);
        shape.setStrokeLineCap(javafx.scene.shape.StrokeLineCap.ROUND);
        shape.setStrokeLineJoin(javafx.scene.shape.StrokeLineJoin.ROUND);
        shape.getStyleClass().add("icon-stroke");
        Group drawing = new Group(shape);
        drawing.setScaleX(.78);
        drawing.setScaleY(.78);
        getChildren().setAll(drawing);
        setMinSize(20, 20);
        setPrefSize(20, 20);
        setMaxSize(20, 20);
        setMouseTransparent(true);
    }
}
