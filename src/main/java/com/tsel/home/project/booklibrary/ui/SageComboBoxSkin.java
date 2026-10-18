package com.tsel.home.project.booklibrary.ui;

import javafx.scene.control.ComboBox;
import javafx.scene.control.skin.ComboBoxListViewSkin;
import javafx.scene.layout.Region;

/** Uses the same SVG chevron as the other dropdown actions. */
public final class SageComboBoxSkin<T> extends ComboBoxListViewSkin<T> {
    public SageComboBoxSkin(ComboBox<T> control) {
        super(control);
        getChildren().stream().map(node -> node.lookup(".arrow"))
            .filter(Region.class::isInstance).map(Region.class::cast).findFirst()
            .ifPresent(arrow -> arrow.setShape(SvgIcons.singlePath("down")));
    }
}
