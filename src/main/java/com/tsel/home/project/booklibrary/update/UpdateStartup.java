package com.tsel.home.project.booklibrary.update;

import com.tsel.home.project.booklibrary.update.shared.InstallationLock;
import com.tsel.home.project.booklibrary.update.shared.UpdateFiles;
import com.tsel.home.project.booklibrary.update.shared.UpdatePlan;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Properties;
import javax.swing.JOptionPane;

public final class UpdateStartup {

    private UpdateStartup() {}

    public static boolean allowLaunch() throws IOException, InterruptedException {
        return allowLaunch(false);
    }

    public static boolean allowLaunch(boolean lockHeld) throws IOException, InterruptedException {
        Path jar = ApplicationPaths.applicationJar();
        if (jar == null) {
            return true;
        }
        for (Path marker : List.of(jar.getParent().resolve(UpdateFiles.PENDING), ApplicationPaths.dataDirectory().resolve(UpdateFiles.PENDING))) {
            if (!Files.exists(marker)) {
                continue;
            }
            Properties state = UpdateFiles.readProperties(marker);
            Path planFile = Path.of(state.getProperty("plan")).toAbsolutePath();
            UpdatePlan plan = UpdatePlan.read(planFile);
            if (!plan.target().equals(jar)) {
                throw new IOException("Другая копия приложения обновляет эту библиотеку. Дождитесь завершения.");
            }
            long pid = Long.parseLong(state.getProperty("pid"));
            Instant started = Instant.parse(state.getProperty("started"));
            boolean running = ProcessHandle.of(pid).filter(process -> process.info().startInstant()
                .map(started::equals).orElse(false)).isPresent();
            if (running) {
                JOptionPane.showMessageDialog(null, "Обновление ещё выполняется. Дождитесь сообщения помощника и запустите приложение снова.",
                    "BookLibrary", JOptionPane.INFORMATION_MESSAGE);
            } else {
                if (lockHeld) {
                    resume(plan, planFile);
                } else {
                    try (InstallationLock ignored = InstallationLock.acquire(plan.dataDirectory())) {
                        resume(plan, planFile);
                    }
                }
            }
            return false;
        }
        return true;
    }

    private static void resume(UpdatePlan plan, Path planFile) throws IOException, InterruptedException {
        int answer = JOptionPane.showConfirmDialog(null,
            "Предыдущее обновление не завершено. Продолжить установку?\nРезервная папка: " + plan.backup()
                + "\nДля ручного восстановления верните application.jar из этой папки на место программы.",
            "BookLibrary — восстановление обновления", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (answer == JOptionPane.YES_OPTION) {
            UpdatePlan resumed = new UpdatePlan(plan.target(), plan.source(), plan.dataDirectory(), plan.backup(), plan.version(),
                plan.platform(), plan.sha256(), plan.originalSha256(), ProcessHandle.current().pid(),
                ProcessHandle.current().info().startInstant().orElseThrow().toEpochMilli());
            resumed.write(planFile);
            UpdateLauncher.launchAndAwaitReady(planFile);
        }
    }
}
