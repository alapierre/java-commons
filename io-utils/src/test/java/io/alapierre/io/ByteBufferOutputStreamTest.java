package io.alapierre.io;


import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import com.github.noconnor.junitperf.JUnitPerfRule;
import com.github.noconnor.junitperf.JUnitPerfTest;
import lombok.val;
import org.junit.Rule;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @author Adrian Lapierre {@literal al@alapierre.io}
 * Copyrights by original author 6.12.2024
 */
public class ByteBufferOutputStreamTest {

    @Test
    public void testWriteSingleByte() throws IOException {
        try (ByteBufferOutputStream outputStream = new ByteBufferOutputStream()) {

            outputStream.write(65);

            InputStream inputStream = outputStream.toInputStream();
            int read = inputStream.read();

            assertEquals(65, read);
            assertEquals(-1, inputStream.read()); // Koniec strumienia
        }
    }

    @Test
    public void testWriteByteArray() throws IOException {
        try (ByteBufferOutputStream outputStream = new ByteBufferOutputStream()) {
            byte[] data = "Hello, ByteBuffer!".getBytes();

            outputStream.write(data);

            InputStream inputStream = outputStream.toInputStream();
            byte[] buffer = new byte[data.length];
            int bytesRead = inputStream.read(buffer);

            assertEquals(data.length, bytesRead);
            assertArrayEquals(data, buffer);
        }
    }

    @Test
    public void testWritePartialArray() throws IOException {
        try (ByteBufferOutputStream outputStream = new ByteBufferOutputStream()) {
            byte[] data = "Hello, ByteBuffer!".getBytes();

            outputStream.write(data, 7, 10); // Zapisujemy "ByteBuffer"

            InputStream inputStream = outputStream.toInputStream();
            byte[] buffer = new byte[10];
            int bytesRead = inputStream.read(buffer);

            assertEquals(10, bytesRead);
            assertArrayEquals("ByteBuffer".getBytes(), buffer);
        }
    }

    @Test
    public void testSize() throws IOException {
        try (ByteBufferOutputStream outputStream = new ByteBufferOutputStream()) {
            byte[] data = "Test size".getBytes();

            outputStream.write(data);

            assertEquals(data.length, outputStream.size());
        }
    }

    @Test
    public void testReset() throws IOException {
        try (ByteBufferOutputStream outputStream = new ByteBufferOutputStream()) {
            byte[] data = "To be cleared".getBytes();

            outputStream.write(data);
            assertEquals(data.length, outputStream.size());

            outputStream.reset();
            assertEquals(0, outputStream.size());

            InputStream inputStream = outputStream.toInputStream();
            assertEquals(-1, inputStream.read()); // Strumień powinien być pusty
        }
    }

    @Test
    public void testDynamicResize() throws IOException {
        try (ByteBufferOutputStream outputStream = new ByteBufferOutputStream(4)) {
            byte[] data = "Dynamic resize test".getBytes();

            outputStream.write(data);

            InputStream inputStream = outputStream.toInputStream();
            byte[] buffer = new byte[data.length];
            int bytesRead = inputStream.read(buffer);

            assertEquals(data.length, bytesRead);
            assertArrayEquals(data, buffer);
        }
    }

    @Test
    public void testToInputStreamWithoutCopying() throws IOException {

        try (ByteBufferOutputStream outputStream = new ByteBufferOutputStream()) {
            byte[] data = "No copying".getBytes();

            outputStream.write(data);
            InputStream inputStream1 = outputStream.toInputStream();
            InputStream inputStream2 = outputStream.toInputStream();

            byte[] buffer1 = inputStream1.readAllBytes();
            byte[] buffer2 = inputStream2.readAllBytes();

            assertArrayEquals(data, buffer1);
            assertArrayEquals(data, buffer2);
        }
    }


}
