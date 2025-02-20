package db.schema.ddl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class HackyCreateIndexParser implements DDLParser {
    private static final Pattern CREATE_INDEX_PATTERN = Pattern.compile(
            "(?i)create\\s+index\\s+(\\w+)\\s+on\\s+(\\w+)\\s*\\(\\s*(\\w+)\\s*\\)"
    );

    private final String indexName;
    private final String tableName;
    private final String columnName;

    private HackyCreateIndexParser(String indexName, String tableName, String columnName) {
        this.indexName = indexName;
        this.tableName = tableName;
        this.columnName = columnName;
    }

    public static HackyCreateIndexParser fromDDL(String ddl) {
        ddl = ddl.replaceAll("\\s+", " ").trim();

        Matcher matcher = CREATE_INDEX_PATTERN.matcher(ddl);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid CREATE INDEX statement: " + ddl);
        }

        String indexName = matcher.group(1);
        String tableName = matcher.group(2);
        String columnName = matcher.group(3);

        return new HackyCreateIndexParser(indexName, tableName, columnName);
    }

    @Override
    public String getName() {
        return indexName;
    }

    public String getTableName() {
        return tableName;
    }

    public String getColumnName() {
        return columnName;
    }

    @Override
    public String toString() {
        return "HackyCreateIndexParser{" +
                "indexName='" + indexName + '\'' +
                ", tableName='" + tableName + '\'' +
                ", columnName='" + columnName + '\'' +
                '}';
    }
}