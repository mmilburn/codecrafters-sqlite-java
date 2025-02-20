package db.schema;

import db.schema.ddl.DDLParser;
import db.schema.ddl.HackyCreateIndexParser;
import db.schema.ddl.HackyCreateTableParser;
import db.schema.ddl.UnimplementedParser;

public class SchemaEntry {
    private final SchemaType type;
    private final String schemaName;
    private final String tableName;
    private final Integer rootPage;
    private final String sql;
    private final DDLParser parser;

    public SchemaEntry(SchemaType type, String schemaName, String tableName, Integer rootPage, String sql) {
        this.type = type;
        this.schemaName = schemaName;
        this.tableName = tableName;
        this.rootPage = rootPage;
        this.sql = sql;
        this.parser = switch (type) {
            case TABLE -> HackyCreateTableParser.fromDDL(sql);
            case INDEX -> HackyCreateIndexParser.fromDDL(sql);
            default -> UnimplementedParser.fromDDL(sql);
        };
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

    public Integer getRootPage() {
        return rootPage;
    }

    public String getSql() {
        return sql;
    }

    public HackyCreateTableParser getTableParser() {
        return (type == SchemaType.TABLE) ? (HackyCreateTableParser) parser : null;
    }

    public HackyCreateIndexParser getIndexParser() {
        return (type == SchemaType.INDEX) ? (HackyCreateIndexParser) parser : null;
    }
}
