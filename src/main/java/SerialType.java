import util.UnusualSizes;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public class SerialType {
    private static final Map<Long, Class<?>> SERIAL_TYPE_MAPPING = new HashMap<>();

    static {
        SERIAL_TYPE_MAPPING.put(0L, Void.class);
        SERIAL_TYPE_MAPPING.put(1L, Byte.class);
        SERIAL_TYPE_MAPPING.put(2L, Short.class);
        SERIAL_TYPE_MAPPING.put(3L, Integer.class);
        SERIAL_TYPE_MAPPING.put(4L, Integer.class);
        SERIAL_TYPE_MAPPING.put(5L, Long.class);
        SERIAL_TYPE_MAPPING.put(6L, Long.class);
        SERIAL_TYPE_MAPPING.put(7L, Double.class);
        SERIAL_TYPE_MAPPING.put(8L, Integer.class);
        SERIAL_TYPE_MAPPING.put(9L, Integer.class);
    }

    private final Varint varint;

    private SerialType(Varint varint) {
        this.varint = varint;
    }

    public static SerialType fromByteBuffer(ByteBuffer buffer) {
        return new SerialType(Varint.fromByteBuffer(buffer));
    }

    public Object contentFromByteBuffer(ByteBuffer data) {
        long val = varint.getValue();
        if (val > 11) {
            if (val % 2 == 0) {
                int len = (int) (val - 12) / 2;
                byte[] buf = new byte[len];
                data.get(buf);
                return buf;
            } else {
                int len = (int) (val - 13) / 2;
                byte[] buf = new byte[len];
                data.get(buf);
                //This is a string with a certain encoding, but we'll have to deal with it later.
                return buf;
            }
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

    public Class<?> getValueClass() {
        long val = varint.getValue();
        if (val > 11) {
            return isString() ? String.class : byte[].class;
        }
        return SERIAL_TYPE_MAPPING.getOrDefault(val, Object.class);
    }

    public boolean isString() {
        long val = varint.getValue();
        return val > 11 && val % 2 == 1;
    }

    public boolean isBlob() {
        long val = varint.getValue();
        return val > 11 && val % 2 == 0;
    }

    public Varint getVarint() {
        return varint;
    }
}
