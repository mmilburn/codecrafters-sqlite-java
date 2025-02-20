package db.data;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.nio.ByteBuffer;

public class VarintTest {

    @Test
    public void testSingleByteVarint() {
        ByteBuffer buffer = ByteBuffer.wrap(new byte[]{0x01});
        Varint varint = Varint.fromByteBuffer(buffer);
        Assert.assertEquals(varint.getValue(), 1L);
        Assert.assertEquals(varint.getBytesConsumed(), 1);
    }

    @Test
    public void testZeroVarint() {
        ByteBuffer buffer = ByteBuffer.wrap(new byte[]{0x00});
        Varint varint = Varint.fromByteBuffer(buffer);
        Assert.assertEquals(varint.getValue(), 0L);
        Assert.assertEquals(varint.getBytesConsumed(), 1);
    }

    @Test
    public void testSingleByteValue116() {
        ByteBuffer buffer = ByteBuffer.wrap(new byte[]{0x74});
        Varint varint = Varint.fromByteBuffer(buffer);
        Assert.assertEquals(varint.getValue(), 116L);
        Assert.assertEquals(varint.getBytesConsumed(), 1);
    }

    @Test
    public void testMaxSingleByteVarint() {
        ByteBuffer buffer = ByteBuffer.wrap(new byte[]{0x7F}); // 127
        Varint varint = Varint.fromByteBuffer(buffer);
        Assert.assertEquals(varint.getValue(), 127L);
        Assert.assertEquals(varint.getBytesConsumed(), 1);
    }

    @Test
    public void testTwoByteVarint128() {
        ByteBuffer buffer = ByteBuffer.wrap(new byte[]{(byte) 0x81, 0x00}); // 128
        Varint varint = Varint.fromByteBuffer(buffer);
        Assert.assertEquals(varint.getValue(), 128L);
        Assert.assertEquals(varint.getBytesConsumed(), 2);
    }

    @Test
    public void testTwoByteVarint300() {
        ByteBuffer buffer = ByteBuffer.wrap(new byte[]{(byte) 0x82, 0x2C}); // 300
        Varint varint = Varint.fromByteBuffer(buffer);
        Assert.assertEquals(varint.getValue(), 300L);
        Assert.assertEquals(varint.getBytesConsumed(), 2);
    }

    @Test
    public void testTwoByteVarint4226() {
        ByteBuffer buffer = ByteBuffer.wrap(new byte[]{(byte) 0xA1, 0x02});
        Varint varint = Varint.fromByteBuffer(buffer);
        Assert.assertEquals(varint.getValue(), 4226L);
        Assert.assertEquals(varint.getBytesConsumed(), 2);
    }

    @Test
    public void testThreeByteVarint() {
        ByteBuffer buffer = ByteBuffer.wrap(new byte[]{(byte) 0x83, (byte) 0x80, 0x01}); // 16384 (2^14)
        Varint varint = Varint.fromByteBuffer(buffer);
        Assert.assertEquals(varint.getValue(), 49153L);
        Assert.assertEquals(varint.getBytesConsumed(), 3);
    }

    @Test
    public void testThreeByteVarint32948() {
        ByteBuffer buffer = ByteBuffer.wrap(new byte[]{(byte) 0x82, (byte) 0x81, 0x34});
        Varint varint = Varint.fromByteBuffer(buffer);
        Assert.assertEquals(varint.getValue(), 32948L);
        Assert.assertEquals(varint.getBytesConsumed(), 3);
    }

    @Test
    public void testMaximumVarint() {
        // This is a 9-byte varint where all bits are used
        ByteBuffer buffer = ByteBuffer.wrap(new byte[]{
                (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
                (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, 0x7F});
        Varint varint = Varint.fromByteBuffer(buffer);
        Assert.assertEquals(varint.getValue(), Long.MAX_VALUE);
        Assert.assertEquals(varint.getBytesConsumed(), 9);
    }

    @Test
    public void testMalformedVarint_EndsEarly() {
        ByteBuffer buffer = ByteBuffer.wrap(new byte[]{(byte) 0x81}); // Only 1 byte, but continuation bit is set
        try {
            Varint.fromByteBuffer(buffer);
            Assert.fail("Expected BufferUnderflowException due to incomplete varint");
        } catch (Exception e) {
            Assert.assertTrue(e instanceof java.nio.BufferUnderflowException);
        }
    }

    @Test
    public void testBufferEdgeCases() {
        ByteBuffer buffer = ByteBuffer.wrap(new byte[]{(byte) 0x81, 0x7F});
        int initialPosition = buffer.position();
        Varint varint = Varint.fromByteBuffer(buffer);
        Assert.assertEquals(buffer.position(), initialPosition + varint.getBytesConsumed());
    }
}
