package com.tsel.home.project.booklibrary.update.helper;

import com.tsel.home.project.booklibrary.update.shared.UpdateFiles;
import com.tsel.home.project.booklibrary.update.shared.UpdatePlan;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.List;
import java.util.Properties;
import java.util.stream.Stream;

/**
 * Устанавливает только проверенный JAR. Данные копируются в резервную папку и не изменяются.
 * Вызывающий процесс обязан удерживать блокировку каталога данных после завершения приложения.
 */
public final class UpdateInstaller {

    private final Replacement replacement;

    public UpdateInstaller() {
        this((source, target) -> Files.move(source, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING));
    }

    UpdateInstaller(Replacement replacement) {
        this.replacement = replacement;
    }

    public void prepare(Path planFile) throws IOException {
        UpdatePlan plan = UpdatePlan.read(planFile);
        plan.validate();
        verifyCandidate(plan);
        Properties state = new Properties();
        state.setProperty("plan", planFile.toRealPath().toString());
        state.setProperty("pid", Long.toString(ProcessHandle.current().pid()));
        state.setProperty("started", ProcessHandle.current().info().startInstant().orElseThrow().toString());
        for (Path pending : markers(plan)) {
            if (Files.exists(pending) && !planFile.toRealPath().toString().equals(UpdateFiles.readProperties(pending).getProperty("plan"))) {
                throw new IOException("Уже подготовлено другое обновление: " + pending);
            }
        }
        try {
            for (Path pending : markers(plan)) {
                UpdateFiles.writeProperties(pending, state);
            }
            UpdateFiles.writeProperties(planFile.resolveSibling("ready.properties"), state);
        } catch (IOException e) {
            clearMarkers(plan, planFile);
            throw e;
        }
    }

    public void install(Path planFile) throws IOException {
        UpdatePlan plan = UpdatePlan.read(planFile);
        plan.validate();
        verifyCandidate(plan);
        if (!UpdateFiles.sha256(plan.target()).equals(plan.sha256())) {
            backup(plan);
            Path replacement = Files.createTempFile(plan.installation(), ".booklibrary-new-", ".jar");
            try {
                copyVerified(plan.source(), replacement);
                if (!UpdateFiles.sha256(replacement).equals(plan.sha256())) {
                    throw new IOException("Контрольная сумма подготовленного JAR не совпадает.");
                }
                UpdateFiles.force(replacement);
                saveDataLocation(plan);
                this.replacement.replace(replacement, plan.target());
            } finally {
                Files.deleteIfExists(replacement);
            }
        } else {
            verifyBackup(plan);
            saveDataLocation(plan);
        }
        if (!UpdateFiles.sha256(plan.target()).equals(plan.sha256())) {
            throw new IOException("Не удалось подтвердить замену JAR. Требуется восстановление из " + plan.backup());
        }
        result(plan, "success", "Установлена версия " + plan.version());
        clearMarkers(plan, planFile);
    }

    public void failed(Path planFile, Exception error) throws IOException {
        failed(planFile, error, true);
    }

    public void failedBeforeInstall(Path planFile, Exception error) throws IOException {
        failed(planFile, error, false);
    }

    private void failed(Path planFile, Exception error, boolean mayRestore) throws IOException {
        UpdatePlan plan = UpdatePlan.read(planFile);
        String current = Files.isRegularFile(plan.target(), LinkOption.NOFOLLOW_LINKS) ? UpdateFiles.sha256(plan.target()) : "";
        if (!current.equals(plan.originalSha256()) && !current.equals(plan.sha256())) {
            if (!mayRestore) {
                throw new IOException("JAR изменился до завершения приложения. Восстановление не выполнялось.", error);
            }
            if (!plan.installation().toRealPath().equals(plan.installation())
                || !plan.backup().getParent().equals(plan.installation().resolve("backup/updates"))) {
                throw new IOException("Недопустимый путь восстановления.");
            }
            verifyBackup(plan);
            Path restored = Files.createTempFile(plan.installation(), ".booklibrary-restore-", ".jar");
            try {
                copyVerified(plan.backup().resolve("application.jar"), restored);
                Files.move(restored, plan.target(), StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } finally {
                Files.deleteIfExists(restored);
            }
            current = UpdateFiles.sha256(plan.target());
        }
        try {
            result(plan, "failed", error.getMessage() == null ? error.getClass().getSimpleName() : error.getMessage());
        } finally {
            if (current.equals(plan.originalSha256())) {
                clearMarkers(plan, planFile);
            }
        }
    }

    private void verifyCandidate(UpdatePlan plan) throws IOException {
        if (UpdateFiles.sha256(plan.target()).equals(plan.sha256())) {
            UpdateFiles.validateJar(plan.target(), plan.version(), plan.platform());
            return;
        }
        if (!UpdateFiles.sha256(plan.source()).equals(plan.sha256())) {
            throw new IOException("Скачанный JAR повреждён или изменён. Установленный файл сохранён.");
        }
        UpdateFiles.validateJar(plan.source(), plan.version(), plan.platform());
    }

    private void backup(UpdatePlan plan) throws IOException {
        Files.createDirectories(plan.backup());
        if (!plan.backup().toRealPath().equals(plan.backup())) {
            throw new IOException("Резервная папка не должна содержать символические ссылки.");
        }
        if (Files.exists(plan.backup().resolve("complete.properties"))) {
            verifyBackup(plan);
            return;
        }
        Properties index = new Properties();
        backupFile(plan.target(), plan.backup().resolve("application.jar"), plan.backup(), index);
        Path repository = plan.dataDirectory().resolve("repository");
        if (Files.exists(repository, LinkOption.NOFOLLOW_LINKS)) {
            try (Stream<Path> paths = Files.walk(repository)) {
                for (Path source : paths.toList()) {
                    if (Files.isSymbolicLink(source)) {
                        throw new IOException("Не удаётся безопасно скопировать ссылку: " + source);
                    }
                    if (Files.isRegularFile(source, LinkOption.NOFOLLOW_LINKS)) {
                        Path target = plan.backup().resolve("data/repository").resolve(repository.relativize(source));
                        Files.createDirectories(target.getParent());
                        backupFile(source, target, plan.backup(), index);
                    }
                }
            }
        }
        for (String settings : List.of(".properties")) {
            Path source = plan.dataDirectory().resolve(settings);
            if (Files.exists(source, LinkOption.NOFOLLOW_LINKS)) {
                Path target = plan.backup().resolve("data").resolve(settings);
                Files.createDirectories(target.getParent());
                backupFile(source, target, plan.backup(), index);
            }
        }
        Path location = plan.installation().resolve(UpdateFiles.DATA_LOCATION);
        if (Files.exists(location, LinkOption.NOFOLLOW_LINKS)) {
            backupFile(location, plan.backup().resolve("data-location.properties"), plan.backup(), index);
        }
        UpdateFiles.writeProperties(plan.backup().resolve("files.properties"), index);
        Properties complete = new Properties();
        complete.setProperty("originalSha256", plan.originalSha256());
        complete.setProperty("indexSha256", UpdateFiles.sha256(plan.backup().resolve("files.properties")));
        complete.setProperty("created", Instant.now().toString());
        complete.setProperty("targetVersion", plan.version());
        complete.setProperty("dataDirectory", plan.dataDirectory().toString());
        UpdateFiles.writeProperties(plan.backup().resolve("complete.properties"), complete);
        verifyBackup(plan);
    }

    private void verifyBackup(UpdatePlan plan) throws IOException {
        Path completeFile = plan.backup().resolve("complete.properties");
        UpdateFiles.regularFile(completeFile);
        Properties complete = UpdateFiles.readProperties(completeFile);
        Path indexFile = plan.backup().resolve("files.properties");
        UpdateFiles.regularFile(indexFile);
        if (!plan.originalSha256().equals(complete.getProperty("originalSha256"))
            || !UpdateFiles.sha256(indexFile).equals(complete.getProperty("indexSha256"))
            || !UpdateFiles.sha256(plan.backup().resolve("application.jar")).equals(plan.originalSha256())) {
            throw new IOException("Резервная копия повреждена: " + plan.backup());
        }
        Properties index = UpdateFiles.readProperties(indexFile);
        for (String relative : index.stringPropertyNames()) {
            Path file = plan.backup().resolve(relative).normalize();
            if (!file.startsWith(plan.backup())) {
                throw new IOException("Путь выходит за пределы резервной папки: " + file);
            }
            UpdateFiles.regularFile(file);
            if (!UpdateFiles.sha256(file).equals(index.getProperty(relative))) {
                throw new IOException("Ошибка проверки резервной копии: " + file);
            }
        }
    }

    private void backupFile(Path source, Path target, Path root, Properties index) throws IOException {
        if (!target.getParent().toRealPath().equals(target.getParent()) || !target.startsWith(root)) {
            throw new IOException("Недопустимый путь резервной копии: " + target);
        }
        copyVerified(source, target);
        index.setProperty(root.relativize(target).toString(), UpdateFiles.sha256(target));
    }

    private void copyVerified(Path source, Path target) throws IOException {
        UpdateFiles.regularFile(source);
        String hash = UpdateFiles.sha256(source);
        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
        UpdateFiles.force(target);
        if (!hash.equals(UpdateFiles.sha256(target)) || !hash.equals(UpdateFiles.sha256(source))) {
            throw new IOException("Файл изменился во время копирования: " + source);
        }
    }

    private void saveDataLocation(UpdatePlan plan) throws IOException {
        Properties location = new Properties();
        location.setProperty("dataDirectory", plan.dataDirectory().toString());
        UpdateFiles.writeProperties(plan.installation().resolve(UpdateFiles.DATA_LOCATION), location);
    }

    private void result(UpdatePlan plan, String status, String message) throws IOException {
        Properties result = new Properties();
        result.setProperty("status", status);
        result.setProperty("message", message);
        result.setProperty("backup", plan.backup().toString());
        result.setProperty("version", plan.version());
        UpdateFiles.writeProperties(plan.dataDirectory().resolve(".booklibrary-update-result.properties"), result);
    }

    private List<Path> markers(UpdatePlan plan) {
        return List.of(plan.installation().resolve(UpdateFiles.PENDING), plan.dataDirectory().resolve(UpdateFiles.PENDING)).stream()
            .distinct().toList();
    }

    private void clearMarkers(UpdatePlan plan, Path planFile) throws IOException {
        for (Path marker : markers(plan)) {
            if (Files.exists(marker) && planFile.toAbsolutePath().toString().equals(UpdateFiles.readProperties(marker).getProperty("plan"))) {
                Files.delete(marker);
            }
        }
    }

    @FunctionalInterface
    interface Replacement {

        void replace(Path source, Path target) throws IOException;
    }
}
