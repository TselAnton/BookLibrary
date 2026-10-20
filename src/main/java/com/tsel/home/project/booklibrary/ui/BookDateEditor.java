package com.tsel.home.project.booklibrary.ui;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;

/** Дата в часовом поясе пользователя с сохранением времени и точности исходного timestamp. */
public final class BookDateEditor extends HBox {

    private final DatePicker date = new DatePicker();
    private final TextField time = new TextField();
    private final ZoneId zone;
    private Long original;
    private String originalState;

    public BookDateEditor(String id, boolean nullable, ZoneId zone, Runnable onEdit) {
        super(8);
        this.zone = zone;
        setId(id);
        date.setId(id + "Date");
        date.setEditable(false);
        date.setPrefWidth(155);
        date.setPromptText("Выберите дату");
        time.setId(id + "Time");
        time.setPrefWidth(120);
        time.setPromptText("ЧЧ:ММ:СС");
        time.setTooltip(new Tooltip("Местное время: " + zone + ". Можно указать миллисекунды: 12:30:00.123"));
        date.valueProperty().addListener((observable, previous, current) -> {
            if (current != null && time.getText().isBlank()) {
                time.setText("00:00:00");
            }
            onEdit.run();
        });
        time.textProperty().addListener((observable, previous, current) -> onEdit.run());
        getChildren().addAll(date, time);
        if (nullable) {
            Button clear = new Button("Очистить");
            clear.disableProperty().bind(date.disabledProperty());
            clear.setOnAction(event -> {
                date.setValue(null);
                time.clear();
            });
            getChildren().add(clear);
        }
    }

    public void setValue(Long value) {
        original = value;
        ZonedDateTime local = value == null ? null : Instant.ofEpochMilli(value).atZone(zone);
        date.setValue(local == null ? null : local.toLocalDate());
        time.setText(local == null ? "" : local.toLocalTime().toString());
        originalState = state();
    }

    public Long value() {
        if (state().equals(originalState)) {
            return original;
        }
        LocalDate day = date.getValue();
        if (day == null) {
            if (!time.getText().isBlank()) {
                throw new IllegalArgumentException("Выберите дату для указанного времени");
            }
            return null;
        }
        try {
            LocalTime localTime = LocalTime.parse(time.getText().trim());
            java.time.LocalDateTime local = day.atTime(localTime);
            if (zone.getRules().getValidOffsets(local).isEmpty()) {
                throw new IllegalArgumentException("Указанное местное время отсутствует из-за перевода часов");
            }
            return ZonedDateTime.ofLocal(local, zone,
                original == null ? null : Instant.ofEpochMilli(original).atZone(zone).getOffset()).toInstant().toEpochMilli();
        } catch (java.time.format.DateTimeParseException e) {
            throw new IllegalArgumentException("Введите время в формате ЧЧ:ММ или ЧЧ:ММ:СС", e);
        }
    }

    public String state() {
        return date.getValue() + "|" + time.getText();
    }
}
