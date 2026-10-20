package com.tsel.home.project.booklibrary.provider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tsel.home.project.booklibrary.helper.StorageWriteBatch;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class StorageWriteBatchTest {

    @Test
    void failedRecoveryPreservesOriginalAndRecoveryErrors(@TempDir Path directory) throws Exception {
        Path file = directory.resolve("storage.json");
        Files.writeString(file, "[]");
        StorageWriteBatch batch = new StorageWriteBatch() {
            @Override
            protected void write(Path path, byte[] content) throws IOException {
                throw new IOException("Недоступен диск");
            }
        };
        batch.add(file, new byte[] {1});
        IllegalStateException error = assertThrows(IllegalStateException.class, batch::commit);
        assertTrue(error.getMessage().contains("восстановить"));
        assertEquals(1, error.getCause().getSuppressed().length);
        assertEquals("Недоступен диск", error.getCause().getCause().getMessage());
    }

    @Test
    void snapshotsAreCollectedBeforeAnyWrite(@TempDir Path directory) throws Exception {
        Path file = directory.resolve("storage.json");
        Files.writeString(file, "[]");
        Path invalid = Files.createDirectory(directory.resolve("not-a-file"));
        StorageWriteBatch batch = new StorageWriteBatch();
        batch.add(file, new byte[] {1});
        batch.add(invalid, new byte[] {2});
        assertThrows(IllegalStateException.class, batch::commit);
        assertEquals("[]", Files.readString(file));
    }
}
