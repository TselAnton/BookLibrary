package com.tsel.home.project.booklibrary.update;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.DoubleConsumer;

final class GitHubTransport implements ReleaseTransport {

    private final HttpClient client = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(15)).followRedirects(HttpClient.Redirect.NORMAL).build();

    @Override
    public byte[] read(URI uri, long limit) throws IOException, InterruptedException {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            transfer(uri, output, limit, -1, Duration.ofSeconds(30), progress -> {});
            return output.toByteArray();
        }
    }

    @Override
    public void download(URI uri, Path target, long expectedSize, DoubleConsumer progress) throws IOException, InterruptedException {
        try (OutputStream output = Files.newOutputStream(target)) {
            transfer(uri, output, expectedSize, expectedSize, Duration.ofMinutes(5), progress);
        }
    }

    private void transfer(
        URI uri, OutputStream output, long limit, long expectedSize, Duration timeout, DoubleConsumer progress
    ) throws IOException, InterruptedException {
        if (!"https".equals(uri.getScheme())) {
            throw new IOException("Обновления разрешены только по HTTPS.");
        }
        HttpRequest request = HttpRequest.newBuilder(uri).timeout(timeout)
            .header("User-Agent", "BookLibrary/" + AppVersion.current())
            .header("Accept", uri.getHost().equals("api.github.com") ? "application/vnd.github+json" : "application/octet-stream")
            .GET().build();
        HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
        try (InputStream input = response.body()) {
            if (response.statusCode() != 200) {
                String message = switch (response.statusCode()) {
                    case 404 -> "Релиз или файл не найден. Возможно, стабильный релиз ещё не опубликован.";
                    case 403, 429 -> "GitHub временно ограничил запросы. Повторите проверку позже.";
                    default -> "GitHub вернул ошибку HTTP " + response.statusCode() + ".";
                };
                throw new IOException(message);
            }
            ScheduledExecutorService timer = Executors.newSingleThreadScheduledExecutor(action -> {
                Thread thread = new Thread(action, "booklibrary-download-timeout");
                thread.setDaemon(true);
                return thread;
            });
            ScheduledFuture<?> deadline = timer.schedule(() -> {
                try {
                    input.close();
                } catch (IOException ignored) {
                    // Чтение завершится ошибкой в рабочем потоке.
                }
            }, timeout.toMillis(), TimeUnit.MILLISECONDS);
            try {
                byte[] buffer = new byte[65536];
                long total = 0;
                int count;
                while ((count = input.read(buffer)) != -1) {
                    if (Thread.currentThread().isInterrupted()) {
                        throw new InterruptedException("Скачивание отменено");
                    }
                    total += count;
                    if (total > limit) {
                        throw new IOException("Размер ответа превышает ожидаемый.");
                    }
                    output.write(buffer, 0, count);
                    if (expectedSize > 0) {
                        progress.accept((double) total / expectedSize);
                    }
                }
                if (deadline.isDone() || expectedSize >= 0 && total != expectedSize) {
                    throw new IOException("Скачивание не завершено или превышено время ожидания.");
                }
            } finally {
                deadline.cancel(false);
                timer.shutdownNow();
            }
        }
    }
}
