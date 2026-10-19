package com.tsel.home.project.booklibrary.update;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tsel.home.project.booklibrary.update.shared.Version;
import org.junit.jupiter.api.Test;

class VersionTest {

    @Test
    void comparesNumbersAndPreReleases() {
        assertTrue(Version.parse("4.10.0").compareTo(Version.parse("4.9.99")) > 0);
        assertTrue(Version.parse("5.0.0").compareTo(Version.parse("5.0.0-SNAPSHOT")) > 0);
        assertTrue(Version.parse("5.0.0-SNAPSHOT").compareTo(Version.parse("4.1.0")) > 0);
        assertTrue(Version.parse("5.0.0-preview.10").compareTo(Version.parse("5.0.0-preview.9")) > 0);
        assertEquals(0, Version.parse("v5.0.0").compareTo(Version.parse("5.0.0")));
        assertThrows(IllegalArgumentException.class, () -> Version.parse("5.0"));
        assertThrows(IllegalArgumentException.class, () -> Version.parse("05.0.0"));
    }
}
