package util;

public class UnusualSizes {
    public static int from24BitValue(byte[] bytes) {
        if (bytes.length != 3) {
            throw new IllegalArgumentException("Byte array must be exactly 3 bytes");
        }

        // Combine bytes as unsigned values
        int combined = ((bytes[0] & 0xFF) << 16) |
                ((bytes[1] & 0xFF) << 8) |
                (bytes[2] & 0xFF);

        // Check if sign bit is set (bit 23 of 24-bit number)
        if ((combined & 0x00800000) != 0) {
            // Sign-extend upper 8 bits
            combined |= 0xFF000000;
        }

        return combined;
    }

    public static long from48BitValue(byte[] bytes) {
        if (bytes.length != 6) {
            throw new IllegalArgumentException("Byte array must be exactly 6 bytes");
        }

        // Combine bytes into a 48-bit value (stored in lower 48 bits of long)
        long value = ((long) (bytes[0] & 0xFF) << 40)
                | ((long) (bytes[1] & 0xFF) << 32)
                | ((long) (bytes[2] & 0xFF) << 24)
                | ((long) (bytes[3] & 0xFF) << 16)
                | ((long) (bytes[4] & 0xFF) << 8)
                | (long) (bytes[5] & 0xFF);

        // Sign-extend if negative (bit 47 is set)
        if ((value & 0x800000000000L) != 0) {
            value |= 0xFFFF000000000000L;  // Set upper 16 bits to 1s
        }

        return value;
    }
}
