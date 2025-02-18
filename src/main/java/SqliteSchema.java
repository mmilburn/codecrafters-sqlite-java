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
                    if (leafCell.initialPayload().getNumberOfColumns() != 5) {
                        return false;
                    }
                    return true;
                })
                // Map each TableLeafCell to a SchemaEntry.
                .map(leafCell -> {
                    Record rec = leafCell.initialPayload();
                    SchemaType schemaType = SchemaType.from(rec.getColumnForIndex(0).getAsString(encoding));
                    String schemaName = rec.getColumnForIndex(1).getAsString(encoding);
                    String tableName = rec.getColumnForIndex(2).getAsString(encoding);
                    byte rootPage = rec.getColumnForIndex(3).getAsByte();
                    String sql = rec.getColumnForIndex(4).getAsString(encoding);
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
        //There's an implicit requirement that we print out the tables in a sorted order.
        return schemaTable.get(SchemaType.TABLE).keySet().stream().sorted().toList();

    }

    public int getRootPageForTable(String tableName) {
        SchemaEntry entry = schemaTable.get(SchemaType.TABLE).get(tableName);
        if (entry != null) {
            return entry.getRootPage();
        }
        return -1;
    }

    public String getSQLForTable(String tableName) {
        SchemaEntry entry = schemaTable.get(SchemaType.TABLE).get(tableName);
        if (entry != null) {
            return entry.getSql();
        }
        return "";
    }
}
