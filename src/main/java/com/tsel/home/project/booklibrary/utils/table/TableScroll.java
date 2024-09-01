package com.tsel.home.project.booklibrary.utils.table;

import static java.lang.String.format;

import javafx.scene.input.ScrollEvent;
import lombok.Getter;

@Getter
public final class TableScroll {

    private static final String TABLE_SCALE_PATTERN = "-fx-font-size: %spt";

    private static final int MIN_FONT = 9;
    private static final int MAX_FONT = 16;

    private int currentFontScale = MIN_FONT;

    public String scroll(ScrollEvent scrollEvent) {
        currentFontScale = scrollEvent.getDeltaY() >= 0
            ? Math.min(currentFontScale + 1, MAX_FONT)
            : Math.max(currentFontScale - 1, MIN_FONT);

        return format(TABLE_SCALE_PATTERN, currentFontScale);
    }
}
