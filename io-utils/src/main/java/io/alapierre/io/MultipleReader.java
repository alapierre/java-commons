package io.alapierre.io;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static io.alapierre.io.IOUtils.closeQuietly;
import static io.alapierre.io.IOUtils.toByteArray;


/**
 * Umożliwia wielokrotne oddczytanie Streama lub Readera z bufora w pamięci
 *
 * Created by adrian on 2017-12-05.
 */
@Slf4j
@SuppressWarnings("WeakerAccess")
public class MultipleReader {

    protected final byte[] content;

    public MultipleReader(byte[] content) {
        this.content = content;
    }

    public MultipleReader(Reader in) throws IOException {
        try {
            content = toByteArray(in, StandardCharsets.UTF_8);
        } finally {
            closeQuietly(in);
        }
    }

    public MultipleReader(InputStream is) throws IOException {
        try {
            content = toByteArray(is);
        } finally {
            closeQuietly(is);
        }
    }

    public static MultipleReader fromOutputStream(IOConsumer<OutputStream> function) throws IOException {
        ByteArrayOutputStream outputStream = null;
        try {
            outputStream = new ByteArrayOutputStream();
            function.accept(outputStream);
        } finally {
            closeQuietly(outputStream);
        }
        return new MultipleReader(outputStream.toByteArray());
    }

    public InputStream getInputStream() {
        return new ByteArrayInputStream(content);
    }

    public Reader getReader() {
        return new StringReader(new String(content, StandardCharsets.UTF_8));
    }

    public byte[] asByteArrray() {
        return content;
    }

    public File asTemporaryFile(String prefix, String suffix) throws IOException {
        File tmp = File.createTempFile(prefix, suffix);
        tmp.deleteOnExit();
        Files.write(tmp.toPath(), content);
        return tmp;
    }

}
