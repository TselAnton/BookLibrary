package com.tsel.home.project.booklibrary.update.helper;

import com.tsel.home.project.booklibrary.update.shared.InstallationLock;
import com.tsel.home.project.booklibrary.update.shared.StartupDialogs;
import com.tsel.home.project.booklibrary.update.shared.UpdatePlan;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/** Самостоятельный помощник: только стандартная библиотека JDK, без JavaFX и зависимостей приложения. */
public final class UpdaterMain {

    private UpdaterMain() {}

    public static void main(String[] arguments) {
        boolean headless = Arrays.asList(arguments).contains("--headless");
        if (arguments.length == 0) {
            return;
        }
        Path planFile = Path.of(arguments[0]).toAbsolutePath();
        if (Arrays.asList(arguments).contains("--restart")) {
            try {
                UpdateRestarter.run(planFile);
            } catch (Exception e) {
                try (PrintWriter log = new PrintWriter(Files.newBufferedWriter(planFile.resolveSibling("restart-error.log"), StandardCharsets.UTF_8))) {
                    e.printStackTrace(log);
                } catch (Exception ignored) {
                    e.printStackTrace();
                }
                if (!headless) {
                    StartupDialogs.message("BookLibrary — перезапуск", "Автоматический запуск не выполнен.\n" + e.getMessage()
                        + "\nПосле успешной установки программу можно запустить вручную.\nЖурнал: " + planFile.resolveSibling("restart-error.log"));
                }
                System.exit(1);
            }
            return;
        }
        UpdateInstaller installer = new UpdateInstaller();
        boolean failureReported = false;
        try {
            installer.prepare(planFile);
            boolean restartRequested = UpdateRestarter.isReady(planFile);
            UpdatePlan plan = UpdatePlan.read(planFile);
            ProcessHandle owner = ProcessHandle.of(plan.processId()).filter(process -> process.info().startInstant()
                .map(time -> time.toEpochMilli() == plan.processStarted()).orElse(false)).orElse(null);
            if (owner != null) {
                owner.onExit().get(120, TimeUnit.SECONDS);
            }
            try (InstallationLock ignored = InstallationLock.acquire(plan.dataDirectory())) {
                try {
                    installer.install(planFile);
                } catch (Exception installationError) {
                    failureReported = true;
                    try {
                        installer.failed(planFile, installationError);
                    } catch (Exception recoveryError) {
                        installationError.addSuppressed(recoveryError);
                    }
                    throw installationError;
                }
            }
            if (!headless && !restartRequested) {
                StartupDialogs.message("BookLibrary — обновление завершено", "Установлена версия " + plan.version()
                    + ". При следующем запуске откроется обновлённое приложение.\nРезервная копия: " + plan.backup());
            }
        } catch (Exception e) {
            try (PrintWriter log = new PrintWriter(Files.newBufferedWriter(planFile.resolveSibling("update-error.log"), StandardCharsets.UTF_8))) {
                e.printStackTrace(log);
                try {
                    if (!failureReported) {
                        installer.failedBeforeInstall(planFile, e);
                    }
                } catch (Exception reportError) {
                    reportError.printStackTrace(log);
                }
            } catch (Exception ignored) {
                e.printStackTrace();
            }
            if (!headless) {
                StartupDialogs.message("BookLibrary — ошибка обновления", "Обновление не завершено.\n" + e.getMessage()
                    + "\nЖурнал: " + planFile.resolveSibling("update-error.log"));
            }
            System.exit(1);
        }
    }
}
