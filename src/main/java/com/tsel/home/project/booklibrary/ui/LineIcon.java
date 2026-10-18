package com.tsel.home.project.booklibrary.ui;

import javafx.scene.Group;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/** Resolution-independent SVG icons, all aligned to the same 24-unit viewport. */
public final class LineIcon extends StackPane {
    private String name;
    public LineIcon() { this("book"); }
    public LineIcon(String name) {
        setMinSize(20, 20); setPrefSize(20, 20); setMaxSize(20, 20);
        setMouseTransparent(true);
        setName(name);
    }
    public String getName() { return name; }

    public void setName(String name) {
        Group drawing = new Group(new Rectangle(24, 24, Color.TRANSPARENT));
        drawing.getChildren().addAll(SvgIcons.createPaths(name));
        drawing.setScaleX(20.0 / 24); drawing.setScaleY(20.0 / 24);
        getChildren().setAll(drawing);
        this.name = name;
    }
}
