package com.tsel.home.project.booklibrary;

import javafx.application.Platform;
import org.junit.jupiter.api.BeforeAll;

public abstract class UnitJavaFXTest {

    @BeforeAll
    public static void initPlatform() {
        Platform.startup(() -> {});
    }
}
