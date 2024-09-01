package com.tsel.home.project.booklibrary;

import javafx.scene.control.CheckBox;
import org.hamcrest.Matcher;
import org.testfx.matcher.base.GeneralMatchers;

public final class CheckBoxMatcher {

    private CheckBoxMatcher() {}

    public static Matcher<CheckBox> isSelected() {
        return GeneralMatchers.typeSafeMatcher(
            CheckBox.class,
            "checkBox is selected",
            CheckBox::isSelected
        );
    }

    public static Matcher<CheckBox> isNotSelected() {
        return GeneralMatchers.typeSafeMatcher(
            CheckBox.class,
            "checkBox is not selected",
            checkBox -> !checkBox.isSelected()
        );
    }
}
