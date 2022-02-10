module com.tsel.home.project.booklibrary {

    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;

    exports com.tsel.home.project.booklibrary;
    exports com.tsel.home.project.booklibrary.dto;
    exports com.tsel.home.project.booklibrary.data;
    exports com.tsel.home.project.booklibrary.search;
    exports com.tsel.home.project.booklibrary.controller;
    exports com.tsel.home.project.booklibrary.repository.impl;
    exports com.tsel.home.project.booklibrary.converter;

    opens com.tsel.home.project.booklibrary to javafx.fxml;
    opens com.tsel.home.project.booklibrary.dto to javafx.fxml;
    opens com.tsel.home.project.booklibrary.search to javafx.fxml;
    opens com.tsel.home.project.booklibrary.controller to javafx.fxml;
    opens com.tsel.home.project.booklibrary.repository.impl to javafx.fxml;
}
