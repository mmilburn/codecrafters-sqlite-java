package db.data;

import java.lang.reflect.Method;
import java.nio.charset.Charset;

public class Column {
    private final SerialType type;
    private final Object value;

    public Column(SerialType type, Object value) {
        this.type = type;
        this.value = value;
    }

    public SerialType getType() {
        return type;
    }

    public Object getValueAs(ColumnType columnType, Charset encoding) {
        if (type.isNull()) {
            return null;
        }
        try {
            Method method = this.getClass().getMethod(columnType.getColumnMethod(), Charset.class);
            return method.invoke(this, encoding);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get column value as " + columnType, e);
        }
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

    public Integer getAsNullableInt() {
        if (value == null || type.isNull()) {
            return null;
        }
        if (value instanceof Integer i) {
            return i;
        }
        if (value instanceof Byte b) {
            return (int) b;
        }
        throw new ClassCastException("Column value is not an integer.");
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

    @Override
    public String toString() {
        return "Column{" +
                "type=" + type +
                ", value=" + formatValue() +
                '}';
    }

    private String formatValue() {
        if (value == null) {
            return "NULL";
        }
        if (value instanceof byte[]) {
            return "BLOB(" + ((byte[]) value).length + " bytes)";
        }
        return value.toString();
    }
}
