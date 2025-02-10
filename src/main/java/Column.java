import util.HexDump;

public class Column {
    private final SerialType type;
    private final Object value;

    public Column(SerialType type, Object value) {
        this.type = type;
        this.value = value;
    }

    public Object getValue() {
        return value;
    }

    public Class<?> getValueClass() {
        return this.type.getValueClass();
    }

}
