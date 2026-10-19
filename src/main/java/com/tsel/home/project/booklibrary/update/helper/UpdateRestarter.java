package com.tsel.home.project.booklibrary.update.helper;

import com.tsel.home.project.booklibrary.update.shared.InstallationLock;
import com.tsel.home.project.booklibrary.update.shared.UpdateFiles;
import com.tsel.home.project.booklibrary.update.shared.UpdatePlan;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/** Ожидает установку и запускает новый JAR с правами исходного приложения. */
public final class UpdateRestarter {

    public static final String READY = "restart-ready.properties";

    private UpdateRestarter() {}

    public static void run(Path planFile) throws Exception {
        UpdatePlan plan = UpdatePlan.read(planFile);
        plan.validate();
        Properties state = new Properties();
        state.setProperty("plan", planFile.toRealPath().toString());
        state.setProperty("pid", Long.toString(ProcessHandle.current().pid()));
        state.setProperty("started", ProcessHandle.current().info().startInstant().orElseThrow().toString());
        UpdateFiles.writeProperties(planFile.resolveSibling(READY), state);
        ProcessHandle owner = ProcessHandle.of(plan.processId()).filter(process -> process.info().startInstant()
            .map(time -> time.toEpochMilli() == plan.processStarted()).orElse(false)).orElse(null);
        if (owner != null) {
            owner.onExit().get(5, TimeUnit.MINUTES);
        }
        long deadline = System.nanoTime() + TimeUnit.MINUTES.toNanos(30);
        while (System.nanoTime() < deadline) {
            Path resultFile = plan.dataDirectory().resolve(".booklibrary-update-result.properties");
            if (Files.exists(resultFile)) {
                Properties result = UpdateFiles.readProperties(resultFile);
                if (plan.backup().toString().equals(result.getProperty("backup")) && plan.version().equals(result.getProperty("version"))) {
                    if ("failed".equals(result.getProperty("status"))) {
                        return;
                    }
                    if ("success".equals(result.getProperty("status"))
                        && !Files.exists(plan.installation().resolve(UpdateFiles.PENDING))
                        && !Files.exists(plan.dataDirectory().resolve(UpdateFiles.PENDING))) {
                        restart(plan, planFile);
                        return;
                    }
                }
            }
            Thread.sleep(200);
        }
        throw new IOException("Истекло время ожидания установки. Запустите программу вручную после завершения помощника.");
    }

    public static boolean isReady(Path planFile) {
        Path file = planFile.resolveSibling(READY);
        if (!Files.exists(file)) {
            return false;
        }
        try {
            Properties state = UpdateFiles.readProperties(file);
            Instant started = Instant.parse(state.getProperty("started"));
            return planFile.toRealPath().toString().equals(state.getProperty("plan"))
                && ProcessHandle.of(Long.parseLong(state.getProperty("pid"))).filter(ProcessHandle::isAlive)
                    .flatMap(process -> process.info().startInstant()).map(started::equals).orElse(false);
        } catch (IOException | RuntimeException e) {
            return false;
        }
    }

    private static void restart(UpdatePlan plan, Path planFile) throws Exception {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
        while (true) {
            try (InstallationLock ignored = InstallationLock.acquire(plan.dataDirectory())) {
                plan.validate();
                if (!plan.sha256().equals(UpdateFiles.sha256(plan.target()))) {
                    throw new IOException("Установленный JAR не соответствует обновлению. Автоматический запуск отменён.");
                }
                UpdateFiles.validateJar(plan.target(), plan.version(), plan.platform());
                break;
            } catch (IOException e) {
                if (System.nanoTime() >= deadline) {
                    throw e;
                }
                Thread.sleep(200);
            }
        }
        boolean windows = System.getProperty("os.name").startsWith("Windows");
        Path java = Path.of(System.getProperty("java.home"), "bin", windows ? "javaw.exe" : "java");
        Process application = new ProcessBuilder(java.toString(), "-Dbooklibrary.dataDirectory=" + plan.dataDirectory(),
            "-jar", plan.target().toString()).directory(plan.dataDirectory().toFile()).redirectErrorStream(true)
            .redirectOutput(planFile.resolveSibling("restart-application.log").toFile()).start();
        Properties state = new Properties();
        state.setProperty("pid", Long.toString(application.pid()));
        state.setProperty("target", plan.target().toString());
        state.setProperty("version", plan.version());
        UpdateFiles.writeProperties(planFile.resolveSibling("restarted.properties"), state);
    }
}
