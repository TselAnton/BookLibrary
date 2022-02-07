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
    opens com.tsel.home.project.booklibrary to javafx.fxml;
}
