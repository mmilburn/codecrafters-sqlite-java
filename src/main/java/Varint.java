import java.nio.ByteBuffer;
import java.util.Objects;

public class Varint {
    private final long value;
    private final int bytesConsumed;

    private Varint(long value, int bytesConsumed) {
        this.value = value;
        this.bytesConsumed = bytesConsumed;
    }

    public static Varint fromValue(long value) {
        int bitsRequired = value == 0 ? 1 : (64 - Long.numberOfLeadingZeros(value));
        int bytesConsumed = (bitsRequired + 7 - 1) / 7;
        return new Varint(value, bytesConsumed);
    }

    public static Varint fromByteBuffer(ByteBuffer data) {
        long result = 0;
        int bytesUsed = 0;
        // SQLite varints can be up to 9 bytes.
        for (int i = 0; i < 9; i++) {
            byte currentByte = data.get();
            bytesUsed++;
            // Shift the current result left by 7 and add the next 7 bits.
            result = (result << 7) | (currentByte & 0x7F);
            // If the continuation bit is not set, break.
            if ((currentByte & 0x80) == 0) {
                break;
            }
        }
        return new Varint(result, bytesUsed);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Varint varint = (Varint) o;
        if (value == varint.value) {
            if (bytesConsumed != varint.bytesConsumed) {
                throw new RuntimeException("Varints have the same value but are different sizes!");
            }
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, bytesConsumed);
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
