package com.tsel.home.project.booklibrary;

import javafx.application.Platform;
import org.junit.jupiter.api.BeforeAll;

public class BaseTest {

    @BeforeAll
    public static void initPlatform() {
        Platform.startup(() -> {});
    }
}
