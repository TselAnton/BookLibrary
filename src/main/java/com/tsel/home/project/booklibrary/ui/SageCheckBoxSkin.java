package com.tsel.home.project.booklibrary.ui;

import javafx.scene.control.CheckBox;
import javafx.scene.control.skin.CheckBoxSkin;
import javafx.scene.layout.Region;

/** Keeps the native checkbox behavior and renders the approved SVG tick. */
public final class SageCheckBoxSkin extends CheckBoxSkin {
    private final Region mark;

    public SageCheckBoxSkin(CheckBox control) {
        super(control);
        mark = getChildren().stream().map(node -> node.lookup(".mark"))
            .filter(Region.class::isInstance).map(Region.class::cast).findFirst().orElseThrow();
        updateMark();
        registerChangeListener(control.indeterminateProperty(), value -> updateMark());
    }

    private void updateMark() {
        mark.setShape(SvgIcons.singlePath(getSkinnable().isIndeterminate() ? "check-indeterminate" : "check"));
    }
}
