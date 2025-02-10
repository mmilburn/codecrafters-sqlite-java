import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SqliteSchema {
    private HashMap<SchemaType, HashMap<String, SchemaEntry>> schemaTable = new HashMap<>();

    private SqliteSchema(HashMap<SchemaType, HashMap<String, SchemaEntry>> schemaTable) {
        this.schemaTable = schemaTable;
    }


    public static SqliteSchema fromCellsWithCharset(List<Cell> cells, Charset encoding) {
        Map<SchemaType, Map<String, SchemaEntry>> tables = cells.stream()
                // Only process TABLE_LEAF cells.
                .filter(cell -> cell.cellType() == CellType.TABLE_LEAF)
                // Cast to TableLeafCell.
                .map(cell -> (TableLeafCell) cell)
                // Filter out any cells that don't have exactly 5 columns, logging them.
                .filter(leafCell -> {
                    List<Column> cols = leafCell.initialPayload().getColumns();
                    if (cols.size() != 5) {
                        System.err.println(cols);
                        return false;
                    }
                    return true;
                })
                // Map each TableLeafCell to a SchemaEntry.
                .map(leafCell -> {
                    List<Column> cols = leafCell.initialPayload().getColumns();
                    // Assume the first column is at index 0 (instead of getFirst()).
                    String typeStr = new String((byte[]) cols.get(0).getValue(), encoding);
                    String schemaName = new String((byte[]) cols.get(1).getValue(), encoding);
                    String tableName = new String((byte[]) cols.get(2).getValue(), encoding);
                    byte rootPage = (byte) cols.get(3).getValue();
                    String sql = new String((byte[]) cols.get(4).getValue(), encoding);
                    SchemaType schemaType = SchemaType.from(typeStr);
                    return new SchemaEntry(schemaType, schemaName, tableName, rootPage, sql);
                })
                // Group by SchemaType then map by tableName.
                .collect(Collectors.groupingBy(
                        SchemaEntry::getType,
                        HashMap::new,
                        Collectors.toMap(
                                SchemaEntry::getTableName,
                                Function.identity(),
                                (existing, replacement) -> existing,
                                HashMap::new
                        )
                ));

        // Convert to a HashMap of HashMaps.
        HashMap<SchemaType, HashMap<String, SchemaEntry>> schemaTable =
                tables.entrySet().stream()
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                e -> new HashMap<>(e.getValue()),
                                (a, b) -> a,
                                HashMap::new
                        ));

        return new SqliteSchema(schemaTable);
    }

    public HashMap<SchemaType, HashMap<String, SchemaEntry>> getSchemaTable() {
        return schemaTable;
    }

    public long getTableCount() {
        return schemaTable.get(SchemaType.TABLE).size();
    }

    public List<String> getTableNames() {
        return schemaTable.get(SchemaType.TABLE).keySet().stream().sorted().toList();

    }
}
