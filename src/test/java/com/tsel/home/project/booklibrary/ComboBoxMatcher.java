package com.tsel.home.project.booklibrary;

import com.tsel.home.project.booklibrary.dto.ComboBoxDTO;
import java.util.Arrays;
import java.util.List;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import org.hamcrest.Matcher;
import org.testfx.matcher.base.GeneralMatchers;

public final class ComboBoxMatcher {

    private ComboBoxMatcher() {}

    @SuppressWarnings("unchecked")
    public static Matcher<ComboBox<ComboBoxDTO>> contains(String... items) {
        return GeneralMatchers.typeSafeMatcher(
            ComboBox.class,
            "ComboBox not have items: " + String.join(", ", items),
            comboBox -> containsItems(comboBox.getItems(), items)
        );
    }

    private static boolean containsItems(ObservableList<ComboBoxDTO> comboBoxDTOObservableList, String... items) {
        List<String> comboBoxItems = comboBoxDTOObservableList
            .stream()
            .map(ComboBoxDTO::getName)
            .toList();

        return comboBoxItems.containsAll(Arrays.asList(items));
    }
}
