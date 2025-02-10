import java.nio.ByteBuffer;

public class Varint {
    private final long value;
    private final int bytesConsumed;

    private Varint(long value, int bytesConsumed) {
        this.value = value;
        this.bytesConsumed = bytesConsumed;
    }

    public static Varint fromByteBuffer(ByteBuffer data) {
        long result = 0;
        int bytesUsed = 0;
        int shift = 0;

        // SQLite varints can be up to 9 bytes
        for (int i = 0; i < 9; i++) {
            byte currentByte = data.get();
            bytesUsed++;
            // Mask off continuation bit and add to result
            long valuePart = (long) (currentByte & 0x7F);
            result |= valuePart << shift;
            shift += 7;
            // Check continuation bit (0x80)
            if ((currentByte & 0x80) == 0) {
                break;
            }
        }
        return new Varint(result, bytesUsed);
    }

    public long getValue() {
        return value;
    }

    public int asInt() {
        return (int) value;
    }

    public int getBytesConsumed() {
        return bytesConsumed;
    }
}
