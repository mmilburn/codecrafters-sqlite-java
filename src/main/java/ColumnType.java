import java.util.HashMap;
import java.util.Map;

public enum ColumnType {
    INTEGER("INTEGER", Integer.class, "getAsInt"),
    INT("INT", Integer.class, "getAsInt"),
    BIGINT("BIGINT", Long.class, "getAsLong"),
    SMALLINT("SMALLINT", Short.class, "getAsShort"),
    TINYINT("TINYINT", Byte.class, "getAsByte"),
    REAL("REAL", Double.class, "getAsDouble"),
    DOUBLE("DOUBLE", Double.class, "getAsDouble"),
    TEXT("TEXT", String.class, "getAsString"),
    VARCHAR("VARCHAR", String.class, "getAsString"),
    BLOB("BLOB", byte[].class, "getAsBlob");

    private final String sqlType;
    private final Class<?> javaType;
    private final String columnMethod;

    private static final Map<String, ColumnType> TYPE_MAP = new HashMap<>();

    static {
        for (ColumnType type : values()) {
            TYPE_MAP.put(type.sqlType, type);
        }
    }

    ColumnType(String sqlType, Class<?> javaType, String columnMethod) {
        this.sqlType = sqlType;
        this.javaType = javaType;
        this.columnMethod = columnMethod;
    }

    public static ColumnType fromSQLType(String sqlType) {
        return TYPE_MAP.getOrDefault(sqlType.toUpperCase(), TEXT); // Default to TEXT if unknown
    }

    public String getColumnMethod() {
        return columnMethod;
    }

    public Class<?> getJavaType() {
        return javaType;
    }
}
