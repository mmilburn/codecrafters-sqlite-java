package db.schema.ddl;

import db.data.ColumnType;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HackyCreateTableParser {
    private static final Pattern CREATE_TABLE_PATTERN = Pattern.compile(
            "(?i)create table\\s+(\"[^\"]+\"|'[^']+'|[A-Za-z_][A-Za-z0-9_]*)\\s*\\((.*)\\)"
    );

    private static final Pattern COLUMN_PATTERN = Pattern.compile(
            "(?i)\\s*(\"[^\"]+\"|\\w+)\\s+([A-Za-z]+(?:\\s+[A-Za-z]+)*)"
    );
    private static final Pattern ROW_ID_ALIAS = Pattern.compile("(?i)\\binteger primary key\\b(?!\\s+desc)");

    private final String table;
    private final Map<String, Integer> colToIndex;
    private final Map<String, ColumnType> columnTypes;
    private final String rowIdAlias;

    private HackyCreateTableParser(String table, Map<String, Integer> colToIndex, Map<String, ColumnType> columnTypes, String rowIdAlias) {
        this.table = table;
        this.colToIndex = colToIndex;
        this.columnTypes = columnTypes;
        this.rowIdAlias = rowIdAlias;
    }

    public static HackyCreateTableParser fromCreateTable(String ddl) {
        //A simple way to deal with multi-line CREATE TABLE statements and odd spacing.
        ddl = ddl.replaceAll("\\s+", " ").trim();

        Matcher matcher = CREATE_TABLE_PATTERN.matcher(ddl);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid CREATE TABLE statement: " + ddl);
        }

        String table = matcher.group(1);
        String columnDefinitions = matcher.group(2);

        AtomicInteger index = new AtomicInteger(0);
        Map<String, Integer> colOrder = new LinkedHashMap<>();
        Map<String, ColumnType> columnTypeMap = new LinkedHashMap<>();
        String rowIdAlias = null;

        Matcher columnMatcher = COLUMN_PATTERN.matcher(columnDefinitions);

        while (columnMatcher.find()) {
            String columnName = columnMatcher.group(1).trim();
            String columnTypeStr = columnMatcher.group(2).trim();

            if (ROW_ID_ALIAS.matcher(columnTypeStr).find()) {
                rowIdAlias = columnName;
            }

            colOrder.put(columnName, index.getAndIncrement());
            columnTypeMap.put(columnName, ColumnType.fromSQLType(columnTypeStr.split("\\s+")[0]));
        }

        return new HackyCreateTableParser(table, colOrder, columnTypeMap, rowIdAlias);
    }

    public String getTable() {
        return table;
    }

    public Set<String> getColumns() {
        return colToIndex.keySet();
    }

    public int indexForColumn(String columnName) {
        return colToIndex.getOrDefault(columnName, -1);
    }

    public ColumnType getColumnType(String columnName) {
        return columnTypes.getOrDefault(columnName, ColumnType.TEXT);
    }

    public boolean isRowIdAlias(String columnName) {
        return rowIdAlias != null && rowIdAlias.equalsIgnoreCase(columnName);
    }

    @Override
    public String toString() {
        return "db.schema.HackyDDLParser{" +
                "table='" + table + '\'' +
                ", colToIndex=" + colToIndex +
                ", columnTypes=" + columnTypes +
                ", rowIdAlias='" + rowIdAlias + '\'' +
                '}';
    }
}
