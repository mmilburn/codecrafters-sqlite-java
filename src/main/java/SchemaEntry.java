public class SchemaEntry {
    private final SchemaType type;
    private final String schemaName;
    private final String tableName;
    private final byte rootPage;
    private final String sql;

    public SchemaEntry(SchemaType type, String schemaName, String tableName, byte rootPage, String sql) {
        this.type = type;
        this.schemaName = schemaName;
        this.tableName = tableName;
        this.rootPage = rootPage;
        this.sql = sql;
    }

    public SchemaType getType() {
        return type;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public String getTableName() {
        return tableName;
    }

    public int getRootPage() {
        return Byte.toUnsignedInt(rootPage);
    }

    public String getSql() {
        return sql;
    }
}
