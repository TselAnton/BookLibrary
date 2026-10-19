package com.tsel.home.project.booklibrary.update;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.function.DoubleConsumer;

interface ReleaseTransport {

    byte[] read(URI uri, long limit) throws IOException, InterruptedException;

    void download(URI uri, Path target, long expectedSize, DoubleConsumer progress) throws IOException, InterruptedException;
}
