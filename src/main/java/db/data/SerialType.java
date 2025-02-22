package db.data;

import util.UnusualSizes;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public class SerialType {
    private static final Map<Long, Integer> SERIAL_TYPE_SIZES = new HashMap<>();

    static {
        SERIAL_TYPE_SIZES.put(0L, 0);
        SERIAL_TYPE_SIZES.put(1L, 1);
        SERIAL_TYPE_SIZES.put(2L, 2);
        SERIAL_TYPE_SIZES.put(3L, 3);
        SERIAL_TYPE_SIZES.put(4L, 4);
        SERIAL_TYPE_SIZES.put(5L, 6);
        SERIAL_TYPE_SIZES.put(6L, 8);
        SERIAL_TYPE_SIZES.put(7L, 8);
        SERIAL_TYPE_SIZES.put(8L, 0);
        SERIAL_TYPE_SIZES.put(9L, 0);
    }

    private final Varint varint;

    private SerialType(Varint varint) {
        this.varint = varint;
    }

    public static SerialType fromByteBuffer(ByteBuffer buffer) {
        Varint varint = Varint.fromByteBuffer(buffer);
        return new SerialType(varint);
    }

    public static SerialType fromStringLength(int length) {
        return new SerialType(Varint.fromValue(13 + (length * 2L)));
    }

    public int getSize() {
        long val = varint.getValue();
        if (val > 11) {
            return (int) (val - (val % 2 == 0 ? 12 : 13)) / 2;
        }
        return SERIAL_TYPE_SIZES.get(val);
    }

    public Object contentFromByteBuffer(ByteBuffer data) {
        long val = varint.getValue();

        if (val > 11) {
            int len = (int) (val - (val % 2 == 0 ? 12 : 13)) / 2;
            if (data.remaining() < len) {
                System.err.println("Attempted to read " + len + " bytes but only " + data.remaining() + " left!");
                throw new BufferUnderflowException();
            }

            byte[] buf = new byte[len];
            data.get(buf);
            return buf;
        } else {
            switch ((int) val) {
                case 0 -> {
                    return null;
                }
                case 1 -> {
                    return data.get();
                }
                case 2 -> {
                    return data.getShort();
                }
                case 3 -> {
                    byte[] buf = new byte[3];
                    data.get(buf);
                    return UnusualSizes.from24BitValue(buf);
                }
                case 4 -> {
                    return data.getInt();
                }
                case 5 -> {
                    byte[] buf = new byte[6];
                    data.get(buf);
                    return UnusualSizes.from48BitValue(buf);
                }
                case 6 -> {
                    return data.getLong();
                }
                case 7 -> {
                    return data.getDouble();
                }
                case 8 -> {
                    return 0;
                }
                case 9 -> {
                    return 1;
                }
                default -> {
                    System.err.println("Unsupported Serial Type: " + val);
                    return null;
                }
            }
        }
    }

    public boolean isString() {
        long val = varint.getValue();
        return val > 11 && val % 2 == 1;
    }

    public boolean isBlob() {
        long val = varint.getValue();
        return val > 11 && val % 2 == 0;
    }

    public boolean isNull() {
        return varint.getValue() == 0;
    }

    public boolean isIntegral() {
        long val = varint.getValue();
        return val < 10 && val != 7;
    }

    public Varint getVarint() {
        return varint;
    }
}
