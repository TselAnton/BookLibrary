package com.tsel.home.project.booklibrary.update;

import com.tsel.home.project.booklibrary.update.helper.UpdateRestarter;
import com.tsel.home.project.booklibrary.update.shared.UpdateFiles;
import com.tsel.home.project.booklibrary.update.shared.UpdatePlan;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.Base64;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.jar.JarFile;

public final class UpdateLauncher {

    private UpdateLauncher() {}

    public static Path prepare(Path downloaded, GitHubReleaseClient.Release release) throws IOException {
        Path target = ApplicationPaths.applicationJar();
        if (target == null) {
            throw new IOException("Автообновление доступно только при запуске собранного JAR.");
        }
        UpdateFiles.validateJar(target, null, null);
        Path directory = downloaded.getParent().toRealPath();
        Path helper = directory.resolve("updater.jar");
        try (InputStream input = UpdateLauncher.class.getResourceAsStream(AppVersion.helperResource())) {
            if (input == null) {
                throw new IOException("В сборке отсутствует отдельный помощник обновления.");
            }
            Files.copy(input, helper, StandardCopyOption.REPLACE_EXISTING);
        }
        verifyHelper(helper);
        Path data = ApplicationPaths.dataDirectory().toRealPath();
        Path backup = target.getParent().resolve("backup/updates/update-to-" + release.version() + "-" + UUID.randomUUID());
        UpdatePlan plan = new UpdatePlan(target, downloaded.toRealPath(), data, backup, release.version().toString(),
            release.platform(), release.sha256(), UpdateFiles.sha256(target), ProcessHandle.current().pid(),
            ProcessHandle.current().info().startInstant().orElseThrow().toEpochMilli());
        Path planFile = directory.resolve("plan.properties");
        plan.validate();
        plan.write(planFile);
        return planFile;
    }

    public static void launchAndAwaitReady(Path planFile) throws IOException, InterruptedException {
        UpdatePlan plan = UpdatePlan.read(planFile);
        Path helper = planFile.resolveSibling("updater.jar");
        verifyHelper(helper);
        Path pending = plan.installation().resolve(UpdateFiles.PENDING);
        if (Files.exists(pending)) {
            Properties state = UpdateFiles.readProperties(pending);
            long pid = Long.parseLong(state.getProperty("pid"));
            Instant started = Instant.parse(state.getProperty("started"));
            if (ProcessHandle.of(pid).filter(process -> process.info().startInstant().map(started::equals).orElse(false)).isPresent()) {
                throw new IOException("Помощник уже запущен. Закройте приложение и дождитесь завершения установки.");
            }
        }
        Files.deleteIfExists(planFile.resolveSibling("ready.properties"));
        boolean elevate = !writable(plan.installation());
        Path java = javaExecutable();
        ProcessBuilder builder;
        if (elevate) {
            if (!System.getProperty("os.name").startsWith("Windows")) {
                throw new IOException("Нет доступа к папке программы. Переместите JAR в доступную для записи папку.");
            }
            String arguments = "-jar \"" + helper + "\" \"" + planFile + "\"";
            String script = "Start-Process -FilePath '" + powershellQuote(java.toString()) + "' -ArgumentList '"
                + powershellQuote(arguments) + "' -Verb RunAs -WindowStyle Hidden -ErrorAction Stop";
            String encoded = Base64.getEncoder().encodeToString(script.getBytes(StandardCharsets.UTF_16LE));
            Path powershell = Path.of(System.getenv("SystemRoot"), "System32/WindowsPowerShell/v1.0/powershell.exe");
            builder = new ProcessBuilder(powershell.toString(), "-NoProfile", "-NonInteractive", "-WindowStyle", "Hidden", "-EncodedCommand", encoded);
        } else {
            builder = new ProcessBuilder(java.toString(), "-jar", helper.toString(), planFile.toString());
        }
        Files.deleteIfExists(planFile.resolveSibling(UpdateRestarter.READY));
        Process restarter = new ProcessBuilder(java.toString(), "-jar", helper.toString(), planFile.toString(), "--restart")
            .directory(planFile.getParent().toFile()).redirectErrorStream(true)
            .redirectOutput(planFile.resolveSibling("restart-start.log").toFile()).start();
        boolean handedOff = false;
        try {
            long restartDeadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
            while (!UpdateRestarter.isReady(planFile)) {
                if (!restarter.isAlive() || System.nanoTime() >= restartDeadline) {
                    throw new IOException("Не удалось подготовить автоматический перезапуск. Приложение остаётся открытым.");
                }
                Thread.sleep(100);
            }
            Process process = builder.directory(planFile.getParent().toFile()).redirectErrorStream(true)
                .redirectOutput(planFile.resolveSibling("helper-start.log").toFile()).start();
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(110);
            while (System.nanoTime() < deadline) {
                Path ready = planFile.resolveSibling("ready.properties");
                if (Files.exists(ready)) {
                    Properties state = UpdateFiles.readProperties(ready);
                    if (planFile.toRealPath().toString().equals(state.getProperty("plan"))) {
                        if (!restarter.isAlive()) {
                            throw new IOException("Процесс перезапуска завершился. Приложение остаётся открытым.");
                        }
                        handedOff = true;
                        return;
                    }
                }
                if (!process.isAlive() && (!elevate || process.exitValue() != 0)) {
                    throw new IOException("Помощник не запущен или запрос Windows отменён. Приложение остаётся открытым. Журнал: "
                        + planFile.resolveSibling("helper-start.log"));
                }
                Thread.sleep(100);
            }
            throw new IOException("Помощник не подтвердил запуск. Приложение остаётся открытым; закройте его для отмены ожидания помощника.");
        } finally {
            if (!handedOff) {
                restarter.destroyForcibly();
            }
        }
    }

    private static boolean writable(Path directory) throws IOException {
        try {
            Path probe = Files.createTempFile(directory, ".booklibrary-access-", ".tmp");
            Files.delete(probe);
            return true;
        } catch (AccessDeniedException | SecurityException e) {
            return false;
        }
    }

    private static Path javaExecutable() throws IOException {
        boolean windows = System.getProperty("os.name").startsWith("Windows");
        Path executable = Path.of(System.getProperty("java.home"), "bin", windows ? "javaw.exe" : "java");
        if (!Files.isRegularFile(executable)) {
            throw new IOException("Не найден Java runtime для помощника: " + executable);
        }
        return executable;
    }

    private static void verifyHelper(Path helper) throws IOException {
        UpdateFiles.regularFile(helper);
        try (JarFile jar = new JarFile(helper.toFile())) {
            if (jar.getManifest() == null || !"com.tsel.home.project.booklibrary.update.helper.UpdaterMain"
                .equals(jar.getManifest().getMainAttributes().getValue("Main-Class"))) {
                throw new IOException("Повреждён помощник обновления.");
            }
        }
    }

    private static String powershellQuote(String value) {
        return value.replace("'", "''");
    }
}
