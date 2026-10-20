package com.tsel.home.project.booklibrary;

import javafx.application.Platform;
import org.junit.jupiter.api.BeforeAll;

public abstract class UnitJavaFXTest {
    private static boolean initialized;

    @BeforeAll
    public static synchronized void initPlatform() {
        if (initialized) return;
        Platform.startup(() -> Platform.setImplicitExit(false));
        initialized = true;
    }
}
