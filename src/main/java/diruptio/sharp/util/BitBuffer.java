package diruptio.sharp.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import org.jetbrains.annotations.NotNull;

/**
 * A BitBuffer is a dynamic buffer that allows writing and reading bits, bytes, integers, and strings.
 * It automatically expands its capacity as needed.
 */
public class BitBuffer {
    protected byte[] bytes = new byte[0];
    protected int capacity = 0;
    protected int bitPosition = 0;
    protected int bytePosition = 0;
    protected int size = 0;

    /** Create a new BitBuffer with an initial capacity of {@code 0} bits */
    public BitBuffer() {
        expand(0);
    }

    /**
     * Create a new BitBuffer with a specified initial capacity in bits
     *
     * @param initialCapacity The initial capacity in bits
     */
    public BitBuffer(int initialCapacity) {
        expand(initialCapacity);
    }

    /**
     * Expand the buffer's capacity by a specified number of bits.
     *
     * @param additionalCapacity The number of bits to expand the capacity by
     * @throws IllegalArgumentException If the additional capacity is negative
     */
    public void expand(int additionalCapacity) {
        if (additionalCapacity == 0) {
            return;
        }
        if (additionalCapacity < 0) {
            throw new IllegalArgumentException("Additional capacity must be non-negative");
        }
        capacity += additionalCapacity;
        int newByteCapacity = (int) Math.ceil((double) capacity / Byte.SIZE);
        if (newByteCapacity > bytes.length) {
            byte[] newBytes = new byte[newByteCapacity];
            System.arraycopy(bytes, 0, newBytes, 0, bytes.length);
            bytes = newBytes;
        }
    }

    /** Resize the buffer to match the current size */
    public void resize() {
        if (size == capacity) {
            return;
        }

        capacity = size;
        byte[] newBytes = new byte[(int) Math.ceil((double) size / Byte.SIZE)];
        System.arraycopy(bytes, 0, newBytes, 0, Math.min(bytes.length, newBytes.length));
        bytes = newBytes;
    }

    /** Reset the position of the buffer to the beginning */
    public void resetPosition() {
        bitPosition = 0;
        bytePosition = 0;
    }

    /** Flip the buffer by resizing and resetting the position */
    public void flip() {
        resize();
        resetPosition();
    }

    /**
     * Write a single bit to the buffer.
     * If the buffer is full, it will expand its capacity.
     *
     * @param value The bit value ({@code true} for {@code 1}, {@code false} for {@code 0})
     */
    public void writeBit(boolean value) {
        if (size >= capacity) {
            expand(capacity / 4 + 1);
        }

        if (value) {
            bytes[bytePosition] = (byte) (bytes[bytePosition] | (1 << bitPosition));
        } else {
            bytes[bytePosition] = (byte) (bytes[bytePosition] & ~(1 << bitPosition));
        }
        size++;
        bitPosition++;
        if (bitPosition == 8) {
            bitPosition = 0;
            bytePosition++;
        }
    }

    /**
     * Read a single bit from the buffer
     *
     * @return The bit value ({@code true} for {@code 1}, {@code false} for {@code 0})
     * @throws IndexOutOfBoundsException If there are no more bits to read
     */
    public boolean readBit() {
        if (bitPosition >= size) {
            throw new IndexOutOfBoundsException("No more bits to read");
        }

        boolean value = (bytes[bytePosition] & (1 << bitPosition)) != 0;
        bitPosition++;
        if (bitPosition == 8) {
            bitPosition = 0;
            bytePosition++;
        }
        return value;
    }

    public void writeBits(@NotNull BitBuffer buffer) {
        expand(Math.max(0, buffer.size - (capacity - size)));
        for (int i = 0; i < buffer.size; i++) {
            writeBit(buffer.readBit());
        }
    }

    public @NotNull BitBuffer readBits(int count) {
        if (count < 0 || count > size - bitPosition) {
            throw new IndexOutOfBoundsException("Cannot read " + count + " bits from current position");
        }

        BitBuffer result = new BitBuffer(count);
        for (int i = 0; i < count; i++) {
            result.writeBit(readBit());
        }
        result.resetPosition();

        return result;
    }

    protected void writeByte(byte value, byte size) {
        if (size < 1 || size > 8) {
            throw new IllegalArgumentException("Size must be between 1 and 8 bits");
        }
        for (byte i = 0; i < size; i++) {
            writeBit((value & (1 << i)) != 0);
        }
    }

    protected byte readByte(byte size) {
        if (size < 1 || size > 8) {
            throw new IllegalArgumentException("Size must be between 1 and 8 bits");
        }
        byte value = 0;
        for (byte i = 0; i < size; i++) {
            if (readBit()) {
                value |= (byte) (1 << i);
            }
        }
        return value;
    }

    public void writeByte(byte value) {
        writeByte(value, (byte) 8);
    }

    public byte readByte() {
        return readByte((byte) 8);
    }

    public void writeBytes(byte @NotNull [] value) {
        for (byte b : value) {
            writeByte(b);
        }
    }

    public byte[] readBytes(int length) {
        byte[] result = new byte[length];
        for (int i = 0; i < length; i++) {
            result[i] = readByte();
        }
        return result;
    }

    public void writeInt(int value) {
        writeBit(value < 0);

        byte size = 0;
        if (value >= 0) {
            for (byte i = 0; i < 31; i++) {
                if ((value & (1 << i)) != 0) {
                    size = (byte) (i + 1);
                }
            }
        } else {
            for (byte i = 0; i < 31; i++) {
                if ((value & (1 << i)) == 0) {
                    size = (byte) (i + 1);
                }
            }
        }
        writeByte(size, (byte) 5);

        if (value >= 0) {
            for (byte i = 0; i < size; i++) {
                writeBit((value & (1 << i)) != 0);
            }
        } else {
            for (byte i = 0; i < size; i++) {
                writeBit((~value & (1 << i)) != 0);
            }
        }
    }

    public int readInt() {
        boolean negative = readBit();
        byte size = readByte((byte) 5);

        int value = 0;
        for (int i = 0; i < size; i++) {
            if (readBit()) {
                value |= (1 << i);
            }
        }

        return negative ? ~value : value;
    }

    public void writeString(@NotNull String value) {
        byte[] bytes = value.getBytes();
        writeInt(bytes.length);
        for (byte b : bytes) {
            writeByte(b);
        }
    }

    public String readString() {
        int length = readInt();
        byte[] bytes = new byte[length];
        for (int i = 0; i < length; i++) {
            bytes[i] = readByte();
        }
        return new String(bytes);
    }

    /**
     * Get the underlying byte array of the BitBuffer
     *
     * @return The byte array
     */
    public byte[] getBytes() {
        return bytes;
    }

    /**
     * Get the current capacity of the BitBuffer in bits. The capacity is how many bits are allocated.
     *
     * @return The capacity
     */
    public int getCapacity() {
        return capacity;
    }

    /**
     * Get the current size of the BitBuffer in bits. The size is how many bits are currently written.
     *
     * @return The size
     */
    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public static @NotNull BitBuffer fromObjectInputStream(@NotNull ObjectInputStream inputStream, int length) {
        try {
            BitBuffer buffer = new BitBuffer(length);
            inputStream.readFully(buffer.getBytes(), 0, buffer.bytes.length);
            buffer.size = length;
            return buffer;
        } catch (IOException e) {
            throw new RuntimeException("Failed to read from ObjectInputStream", e);
        }
    }
}
