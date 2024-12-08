package io.alapierre.io;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

/**
 * @author Adrian Lapierre {@literal al@alapierre.io}
 * Copyrights by original author 6.12.2024
 */
public class ByteBufferOutputStream extends OutputStream {
    private ByteBuffer buffer;

    /**
     * Tworzy nowy ByteBufferOutputStream z domyślną pojemnością.
     */
    public ByteBufferOutputStream() {
        this(32);
    }

    /**
     * Tworzy nowy ByteBufferOutputStream o określonej początkowej pojemności.
     *
     * @param initialCapacity początkowa pojemność bufora.
     */
    public ByteBufferOutputStream(int initialCapacity) {
        if (initialCapacity < 0) {
            throw new IllegalArgumentException("Negative initial size: " + initialCapacity);
        }
        buffer = ByteBuffer.allocate(initialCapacity);
    }

    /**
     * Zapewnia wystarczającą pojemność bufora.
     *
     * @param minCapacity minimalna wymagana pojemność.
     */
    private void ensureCapacity(int minCapacity) {
        if (minCapacity > buffer.capacity()) {
            int newCapacity = Math.max(buffer.capacity() * 2, minCapacity);
            ByteBuffer newBuffer = ByteBuffer.allocate(newCapacity);
            buffer.flip();
            newBuffer.put(buffer);
            buffer = newBuffer;
        }
    }

    /**
     * Zapisuje jeden bajt do bufora.
     *
     * @param b bajt do zapisania.
     */
    @Override
    public synchronized void write(int b) {
        ensureCapacity(buffer.position() + 1);
        buffer.put((byte) b);
    }

    /**
     * Zapisuje tablicę bajtów do bufora.
     *
     * @param b   tablica bajtów do zapisania.
     * @param off offset początkowy.
     * @param len liczba bajtów do zapisania.
     */
    @Override
    public synchronized void write(byte[] b, int off, int len) {
        if (b == null) {
            throw new NullPointerException();
        }
        if (off < 0 || len < 0 || off + len > b.length) {
            throw new IndexOutOfBoundsException();
        }
        ensureCapacity(buffer.position() + len);
        buffer.put(b, off, len);
    }

    /**
     * Tworzy `InputStream`, który bezpośrednio odczytuje dane z tego bufora.
     *
     * @return InputStream odczytujący dane bez kopiowania.
     */
    public InputStream toInputStream() {
        ByteBuffer readOnlyBuffer = buffer.asReadOnlyBuffer();
        readOnlyBuffer.flip();
        return new InputStream() {
            private final ReadableByteChannel channel = new ReadableByteChannel() {
                @Override
                public int read(ByteBuffer dst) {
                    if (!readOnlyBuffer.hasRemaining()) {
                        return -1;
                    }
                    int bytesToRead = Math.min(dst.remaining(), readOnlyBuffer.remaining());
                    ByteBuffer slice = readOnlyBuffer.slice();
                    slice.limit(bytesToRead);
                    dst.put(slice);
                    readOnlyBuffer.position(readOnlyBuffer.position() + bytesToRead);
                    return bytesToRead;
                }

                @Override
                public boolean isOpen() {
                    return true;
                }

                @Override
                public void close() throws IOException {
                    // Zamknięcie kanału
                }
            };

            @Override
            public int read() throws IOException {
                ByteBuffer singleByteBuffer = ByteBuffer.allocate(1);
                if (channel.read(singleByteBuffer) == -1) {
                    return -1;
                }
                singleByteBuffer.flip();
                return singleByteBuffer.get() & 0xFF;
            }

            @Override
            public void close() throws IOException {
                channel.close();
            }
        };
    }

    /**
     * Zwraca aktualny rozmiar danych w buforze.
     *
     * @return rozmiar danych.
     */
    public synchronized int size() {
        return buffer.position();
    }

    /**
     * Zamyka strumień. Ta metoda nie ma efektu.
     */
    @Override
    public void close() {
    }

    /**
     * Resetuje bufor, usuwając wszystkie zapisane dane.
     */
    public synchronized void reset() {
        buffer.clear();
    }
}
