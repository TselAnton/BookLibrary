package com.tsel.home.project.booklibrary.search.filter.impl;

import com.tsel.home.project.booklibrary.search.SearchFieldDefinition;
import com.tsel.home.project.booklibrary.search.filter.AbstractSearchFilter;
import javafx.scene.control.CheckBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CheckBoxSearchFilter extends AbstractSearchFilter<CheckBox> {

    private static final Logger LOGGER = LogManager.getLogger(CheckBoxSearchFilter.class);

    private static final String REVERSE = "!";

    @Override
    protected Class<CheckBox> getProcessedFilterValueClass() {
        return CheckBox.class;
    }

    @Override
    public String getTooltipInfo() {
        return "для отрицания в начале поставить \"!\"";
    }

    @Override
    protected boolean filterByQuery(CheckBox checkBox, String searchQuery, SearchFieldDefinition definition) {
        LOGGER.debug("Search by query '{}' in value '{}'", searchQuery, checkBoxTextSafely(checkBox));
        return checkBox != null && definition.aliases()
            .stream()
            .filter(searchQuery::contains)
            .anyMatch(alias -> filterByCheckBox(checkBox, searchQuery, alias));

    }

    private static boolean filterByCheckBox(CheckBox checkBox, String searchQuery, String keyName) {
        if (searchQuery.startsWith(REVERSE) && keyName.equals(searchQuery.substring(1).trim())) {
            return !checkBox.isSelected();
        } else {
            return keyName.equals(searchQuery) && checkBox.isSelected();
        }
    }

    private String checkBoxTextSafely(CheckBox checkBox) {
        return checkBox != null ? checkBox.getText() : "null";
    }
}
