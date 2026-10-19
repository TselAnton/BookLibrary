package com.tsel.home.project.booklibrary.update;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tsel.home.project.booklibrary.update.helper.UpdateRestarter;
import com.tsel.home.project.booklibrary.update.shared.UpdateFiles;
import com.tsel.home.project.booklibrary.update.shared.UpdatePlan;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class UpdateRestartProcessTest {

    @Test
    void startsUpdatedJarOnlyAfterOwnerExitAndVerifiedInstallation(@TempDir Path directory) throws Exception {
        verifyRestart(directory, false);
    }

    @Test
    void failedInstallationDoesNotRestartOldOrDamagedJar(@TempDir Path directory) throws Exception {
        verifyRestart(directory, true);
    }

    private void verifyRestart(Path directory, boolean corrupt) throws Exception {
        Path file = UpdateFixtures.plan(directory, 99999999, 1);
        UpdatePlan initial = UpdatePlan.read(file);
        executableCandidate(directory, initial.source());
        Path helper = directory.resolve("помощник перезапуска.jar");
        try (InputStream resource = getClass().getResourceAsStream(AppVersion.helperResource())) {
            assertTrue(resource != null);
            Files.copy(resource, helper);
        }
        Path ownerReady = directory.resolve("owner-ready");
        Path ownerStop = directory.resolve("owner-stop");
        String java = Path.of(System.getProperty("java.home"), "bin", "java").toString();
        String classpath = Path.of("target/test-classes").toAbsolutePath() + File.pathSeparator + Path.of("target/classes").toAbsolutePath();
        Process owner = new ProcessBuilder(java, "-cp", classpath, UpdateProcessFixture.class.getName(), initial.dataDirectory().toString(),
            ownerReady.toString(), ownerStop.toString()).redirectErrorStream(true).redirectOutput(directory.resolve("owner.log").toFile()).start();
        Process restarter = null;
        Process installer = null;
        try {
            awaitFile(ownerReady);
            UpdatePlan plan = new UpdatePlan(initial.target(), initial.source(), initial.dataDirectory(), initial.backup(), initial.version(), initial.platform(),
                UpdateFiles.sha256(initial.source()), initial.originalSha256(), owner.pid(), owner.info().startInstant().orElseThrow().toEpochMilli());
            plan.write(file);
            restarter = new ProcessBuilder(java, "-jar", helper.toString(), file.toString(), "--restart", "--headless")
                .redirectErrorStream(true).redirectOutput(directory.resolve("restarter.log").toFile()).start();
            awaitFile(file.resolveSibling(UpdateRestarter.READY));
            assertTrue(UpdateRestarter.isReady(file));
            Path launched = plan.dataDirectory().resolve("restarted-test.properties");
            assertFalse(Files.exists(launched));
            installer = new ProcessBuilder(java, "-jar", helper.toString(), file.toString(), "--headless")
                .redirectErrorStream(true).redirectOutput(directory.resolve("installer.log").toFile()).start();
            awaitFile(file.resolveSibling("ready.properties"));
            assertTrue(owner.isAlive());
            assertEquals(plan.originalSha256(), UpdateFiles.sha256(plan.target()));
            assertFalse(Files.exists(launched));
            if (corrupt) {
                Files.writeString(plan.source(), "corrupted before replacement");
            }
            Files.writeString(ownerStop, "stop");
            assertTrue(owner.waitFor(10, TimeUnit.SECONDS));
            assertTrue(installer.waitFor(15, TimeUnit.SECONDS));
            assertEquals(corrupt ? 1 : 0, installer.exitValue());
            assertTrue(restarter.waitFor(15, TimeUnit.SECONDS));
            assertEquals(0, restarter.exitValue(), Files.readString(directory.resolve("restarter.log")));
            if (corrupt) {
                assertFalse(Files.exists(launched));
                assertFalse(Files.exists(file.resolveSibling("restarted.properties")));
                assertEquals(plan.originalSha256(), UpdateFiles.sha256(plan.target()));
            } else {
                Path restartState = file.resolveSibling("restarted.properties");
                awaitFile(restartState);
                long pid = Long.parseLong(UpdateFiles.readProperties(restartState).getProperty("pid"));
                ProcessHandle application = ProcessHandle.of(pid).orElse(null);
                if (application != null) {
                    application.onExit().get(10, TimeUnit.SECONDS);
                }
                awaitFile(launched);
                Properties launchedState = UpdateFiles.readProperties(launched);
                assertEquals(plan.version(), launchedState.getProperty("version"));
                assertEquals(plan.dataDirectory().toString(), launchedState.getProperty("data"));
                assertEquals(plan.dataDirectory().toString(), launchedState.getProperty("cwd"));
                assertEquals(plan.sha256(), UpdateFiles.sha256(plan.target()));
                assertEquals(plan.originalSha256(), UpdateFiles.sha256(plan.backup().resolve("application.jar")));
                assertFalse(Files.exists(plan.dataDirectory().resolve(UpdateFiles.PENDING)));
            }
        } finally {
            owner.destroyForcibly();
            if (installer != null) {
                installer.destroyForcibly();
            }
            if (restarter != null) {
                restarter.destroyForcibly();
            }
        }
    }

    private void executableCandidate(Path directory, Path file) throws Exception {
        Path source = directory.resolve("App.java");
        String program = String.join("\n",
            "package com.tsel.home.project.booklibrary;",
            "",
            "import java.io.OutputStream;",
            "import java.nio.file.Files;",
            "import java.nio.file.Path;",
            "import java.util.Properties;",
            "",
            "public class App {",
            "",
            "    public static void main(String[] args) throws Exception {",
            "        Properties result = new Properties();",
            "        result.setProperty(\"version\", \"6.0.0\");",
            "        result.setProperty(\"data\", System.getProperty(\"booklibrary.dataDirectory\"));",
            "        result.setProperty(\"cwd\", Path.of(\"\").toAbsolutePath().toString());",
            "        Path file = Path.of(System.getProperty(\"booklibrary.dataDirectory\"), \"restarted-test.properties\");",
            "        try (OutputStream output = Files.newOutputStream(file)) {",
            "            result.store(output, \"test\");",
            "        }",
            "    }",
            "}",
            ""
        );
        Files.writeString(source, program);
        String javac = Path.of(System.getProperty("java.home"), "bin", "javac").toString();
        Process compiler = new ProcessBuilder(javac, "-d", directory.toString(), source.toString()).redirectErrorStream(true)
            .redirectOutput(directory.resolve("compiler.log").toFile()).start();
        assertTrue(compiler.waitFor(15, TimeUnit.SECONDS));
        assertEquals(0, compiler.exitValue(), Files.readString(directory.resolve("compiler.log")));
        Manifest manifest;
        try (JarFile jar = new JarFile(file.toFile())) {
            manifest = jar.getManifest();
        }
        String entry = UpdateFiles.MAIN_CLASS.replace('.', '/') + ".class";
        try (JarOutputStream output = new JarOutputStream(Files.newOutputStream(file), manifest)) {
            output.putNextEntry(new JarEntry(entry));
            Files.copy(directory.resolve(entry), output);
            output.closeEntry();
        }
    }

    private void awaitFile(Path path) throws Exception {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(15);
        while (!Files.exists(path) && System.nanoTime() < deadline) {
            Thread.sleep(20);
        }
        assertTrue(Files.exists(path), "Missing process state: " + path);
    }
}
