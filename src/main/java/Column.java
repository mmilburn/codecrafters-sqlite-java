import java.nio.charset.Charset;

public class Column {
    private final SerialType type;
    private final Object value;

    public Column(SerialType type, Object value) {
        this.type = type;
        this.value = value;
    }

    public String getAsString(Charset encoding) {
        if (type.isString() && value instanceof byte[] bytes) {
            return new String(bytes, encoding);
        }
        throw new ClassCastException("Column value is not a String.");
    }

    public byte[] getAsBlob() {
        if (type.isBlob() && value instanceof byte[] bytes) {
            return bytes;
        }
        throw new ClassCastException("Column value is not a Blob.");
    }

    public byte getAsByte() {
        if (value instanceof Byte b) {
            return b;
        }
        throw new ClassCastException("Column value is not a byte.");
    }

    public short getAsShort() {
        if (value instanceof Short s) {
            return s;
        }
        throw new ClassCastException("Column value is not a short.");
    }

    public int getAsInt() {
        if (value instanceof Integer i) {
            return i;
        }
        throw new ClassCastException("Column value is not an int.");
    }

    public long getAsLong() {
        if (value instanceof Long l) {
            return l;
        }
        throw new ClassCastException("Column value is not a long.");
    }

    public double getAsDouble() {
        if (value instanceof Double d) {
            return d;
        }
        throw new ClassCastException("Column value is not a double.");
    }

}
