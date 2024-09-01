package com.tsel.home.project.booklibrary.utils.table;

import static java.util.Comparator.comparingInt;

import java.util.Comparator;
import javafx.scene.control.CheckBox;

public final class TableComparators {

    public static final Comparator<String> STRING_NUMBER_COMPARATOR = comparingInt(Integer::parseInt);

    public static final Comparator<Object> NON_COMPARATOR = (x1, x2) -> 0;

    public static final Comparator<Object> CHECK_BOX_COMPARATOR = TableComparators::compareCheckBoxes;

    public static int compareCheckBoxes(Object c1, Object c2) {
        if (c1 == null) {
            return c2 == null ? 0 : 1;
        }
        return c2 == null ? -1 : Boolean.compare(((CheckBox) c2).isSelected(), ((CheckBox) c1).isSelected());
    }

    private TableComparators() {}
}
