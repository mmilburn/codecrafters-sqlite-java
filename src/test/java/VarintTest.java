import org.testng.Assert;
import org.testng.annotations.Test;

import java.nio.ByteBuffer;

public class VarintTest {

    @Test
    public void testSingleByteVarint() {
        // A single byte varint: 0x01 should decode to 1.
        ByteBuffer buffer = ByteBuffer.allocate(1);
        buffer.put((byte) 0x01);
        buffer.flip();
        Varint varint = Varint.fromByteBuffer(buffer);
        Assert.assertEquals(varint.getValue(), 1L);
        Assert.assertEquals(varint.getBytesConsumed(), 1);
    }

    @Test
    public void testZeroVarint() {
        // A single byte varint: 0x00 should decode to 0.
        ByteBuffer buffer = ByteBuffer.allocate(1);
        buffer.put((byte) 0x00);
        buffer.flip();
        Varint varint = Varint.fromByteBuffer(buffer);
        Assert.assertEquals(varint.getValue(), 0L);
        Assert.assertEquals(varint.getBytesConsumed(), 1);
    }

    @Test
    public void testTwoByteVarint128() {
        // For value 128, the expected big-endian encoding is:
        // First byte: 0x81 (continuation bit set, lower 7 bits = 1)
        // Second byte: 0x00 (continuation bit clear, lower 7 bits = 0)
        ByteBuffer buffer = ByteBuffer.allocate(2);
        buffer.put((byte) 0x81);
        buffer.put((byte) 0x00);
        buffer.flip();
        Varint varint = Varint.fromByteBuffer(buffer);
        Assert.assertEquals(varint.getValue(), 128L);
        Assert.assertEquals(varint.getBytesConsumed(), 2);
    }

    @Test
    public void testTwoByteVarint300() {
        // To encode 300 in big-endian varint:
        // 300 in binary is 0b1_0010_1100.
        // Split into two groups:
        //   Upper bits: 300 >> 7 = 2, lower bits: 300 & 0x7F = 44.
        // First byte: 0x80 | 2 = 0x82, second byte: 44 = 0x2C.
        ByteBuffer buffer = ByteBuffer.allocate(2);
        buffer.put((byte) 0x82);
        buffer.put((byte) 0x2C);
        buffer.flip();
        Varint varint = Varint.fromByteBuffer(buffer);
        Assert.assertEquals(varint.getValue(), 300L);
        Assert.assertEquals(varint.getBytesConsumed(), 2);
    }

    // Optionally, add tests for larger varints if needed.
}

