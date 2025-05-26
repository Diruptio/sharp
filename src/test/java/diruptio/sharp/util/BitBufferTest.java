package diruptio.sharp.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class BitBufferTest {
    @Test
    public void testExpand() {
        BitBuffer buffer = new BitBuffer(8);
        assertEquals(8, buffer.getCapacity());

        buffer.expand(8);
        assertEquals(16, buffer.getCapacity());

        buffer.expand(0);
        assertEquals(16, buffer.getCapacity());

        assertThrows(IllegalArgumentException.class, () -> buffer.expand(-10));
    }

    @Test
    public void testFlip() {
        BitBuffer buffer = new BitBuffer(8);
        buffer.writeBit(true);
        buffer.writeBit(false);
        buffer.writeBit(true);
        assertEquals(3, buffer.getSize());
        assertEquals(8, buffer.getCapacity());

        buffer.flip();
        assertEquals(3, buffer.getSize());
        assertEquals(3, buffer.getCapacity());
    }

    @Test
    public void testReadWriteBit() {
        BitBuffer buffer = new BitBuffer();
        buffer.writeBit(true);
        buffer.writeBit(false);
        buffer.writeBit(true);
        buffer.writeBit(true);
        buffer.writeBit(false);
        buffer.writeBit(false);
        buffer.flip();
        assertTrue(buffer.readBit());
        assertFalse(buffer.readBit());
        assertTrue(buffer.readBit());
        assertTrue(buffer.readBit());
        assertFalse(buffer.readBit());
        assertFalse(buffer.readBit());
    }

    @Test
    public void testReadWriteBits() {
        BitBuffer buffer = new BitBuffer();

        BitBuffer bitsToWrite = new BitBuffer();
        bitsToWrite.writeBit(true);
        bitsToWrite.writeBit(false);
        bitsToWrite.writeBit(true);
        bitsToWrite.writeBit(true);
        bitsToWrite.writeBit(false);
        bitsToWrite.writeBit(false);
        bitsToWrite.flip();
        buffer.writeBits(bitsToWrite);
        buffer.flip();

        BitBuffer readBits = buffer.readBits(6);
        assertTrue(readBits.readBit());
        assertFalse(readBits.readBit());
        assertTrue(readBits.readBit());
        assertTrue(readBits.readBit());
        assertFalse(readBits.readBit());
        assertFalse(readBits.readBit());
    }

    @Test
    public void testReadWriteByte() {
        BitBuffer buffer = new BitBuffer();
        buffer.writeByte(Byte.MAX_VALUE);
        buffer.writeByte((byte) 42);
        buffer.writeByte((byte) 0);
        buffer.writeByte((byte) -10);
        buffer.writeByte(Byte.MIN_VALUE);
        buffer.flip();
        assertEquals(Byte.MAX_VALUE, buffer.readByte());
        assertEquals(42, buffer.readByte());
        assertEquals(0, buffer.readByte());
        assertEquals(-10, buffer.readByte());
        assertEquals(Byte.MIN_VALUE, buffer.readByte());
    }

    @Test
    public void testReadWriteInt() {
        BitBuffer buffer = new BitBuffer();
        buffer.writeInt(Integer.MAX_VALUE);
        buffer.writeInt(42);
        buffer.writeInt(0);
        buffer.writeInt(-10);
        buffer.writeInt(Integer.MIN_VALUE);
        buffer.flip();
        assertEquals(Integer.MAX_VALUE, buffer.readInt());
        assertEquals(42, buffer.readInt());
        assertEquals(0, buffer.readInt());
        assertEquals(-10, buffer.readInt());
        assertEquals(Integer.MIN_VALUE, buffer.readInt());
    }

    @Test
    public void testReadWriteString() {
        BitBuffer buffer = new BitBuffer();
        String testString1 = "Hello, World!";
        String testString2 = "Sharp BitBuffer Test";
        buffer.writeString(testString1);
        buffer.writeString(testString2);
        buffer.flip();
        assertEquals(testString1, buffer.readString());
        assertEquals(testString2, buffer.readString());
    }
}
