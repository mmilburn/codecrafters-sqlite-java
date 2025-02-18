import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HackyDDLParser {
    private static final Pattern CREATE_TABLE_PATTERN = Pattern.compile(
            "(?i)create table\\s+([A-Za-z_][A-Za-z0-9_]*)\\s*\\((.*)\\)"
    );

    private final String table;
    private final Map<String, Integer> colToIndex;
    private final Map<String, ColumnType> columnTypes;

    private HackyDDLParser(String table, Map<String, Integer> colToIndex, Map<String, ColumnType> columnTypes) {
        this.table = table;
        this.colToIndex = colToIndex;
        this.columnTypes = columnTypes;
    }

    public static HackyDDLParser fromCreateTable(String ddl) {
        Matcher matcher = CREATE_TABLE_PATTERN.matcher(ddl);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid CREATE TABLE statement: " + ddl);
        }

        String table = matcher.group(1);
        String columnDefinitions = matcher.group(2);

        AtomicInteger index = new AtomicInteger(0);
        Map<String, Integer> colOrder = new LinkedHashMap<>();
        Map<String, ColumnType> columnTypeMap = new LinkedHashMap<>();

        String[] columns = columnDefinitions.split(",");

        for (String columnDef : columns) {
            String[] parts = columnDef.trim().split("\\s+", 2);
            if (parts.length < 2) {
                throw new IllegalArgumentException("Invalid column definition: " + columnDef);
            }

            String columnName = parts[0].trim();
            String columnTypeStr = parts[1].trim().split("\\s+", 2)[0]; // Extract first part of type

            colOrder.put(columnName, index.getAndIncrement());
            columnTypeMap.put(columnName, ColumnType.fromSQLType(columnTypeStr));
        }

        return new HackyDDLParser(table, colOrder, columnTypeMap);
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

    @Override
    public String toString() {
        return "HackyDDLParser{" +
                "table='" + table + '\'' +
                ", colToIndex=" + colToIndex +
                ", columnTypes=" + columnTypes +
                '}';
    }
}
