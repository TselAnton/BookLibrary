package com.tsel.home.project.booklibrary.update;

import com.tsel.home.project.booklibrary.update.shared.InstallationLock;
import java.nio.file.Files;
import java.nio.file.Path;

/** An isolated process used to check real OS locks and helper handoff. */
public final class UpdateProcessFixture {

    public static void main(String[] arguments) throws Exception {
        try (InstallationLock ignored = InstallationLock.acquire(Path.of(arguments[0]))) {
            Files.writeString(Path.of(arguments[1]), "ready");
            while (!Files.exists(Path.of(arguments[2]))) {
                Thread.sleep(20);
            }
        }
    }
}
