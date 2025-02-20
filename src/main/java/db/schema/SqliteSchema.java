package db.schema;

import db.btree.cell.Cell;
import db.btree.cell.CellType;
import db.btree.cell.TableLeafCell;
import db.data.Record;

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
                .filter(cell -> cell.cellType() == CellType.TABLE_LEAF)
                .map(cell -> (TableLeafCell) cell)
                .filter(leafCell -> {
                    return leafCell.initialPayload().getNumberOfColumns() == 5;
                })
                .map(leafCell -> {
                    Record rec = leafCell.initialPayload();
                    SchemaType schemaType = SchemaType.from(rec.getColumnForIndex(0).getAsString(encoding));
                    String schemaName = rec.getColumnForIndex(1).getAsString(encoding);
                    String tableName = rec.getColumnForIndex(2).getAsString(encoding);
                    Integer rootPage = rec.getColumnForIndex(3).getAsNullableInt();
                    String sql = rec.getColumnForIndex(4).getAsString(encoding);
                    return new SchemaEntry(schemaType, schemaName, tableName, rootPage, sql);
                })
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

    private int getSchemaTypeCount(SchemaType type) {
        return schemaTable.get(type).size();
    }

    private List<String> getSchemaTypeNames(SchemaType type) {
        Map<String, SchemaEntry> entries = schemaTable.get(type);
        if (entries == null) {
            return List.of();
        }
        //There's an implicit requirement that we print out the tables in a sorted order.
        return entries.keySet().stream().sorted().toList();
    }

    private int getRootPageForSchemaTypeWithName(SchemaType type, String name) {
        SchemaEntry entry = schemaTable.get(type).get(name);
        if (entry != null) {
            Integer rootPage = entry.getRootPage();
            if (rootPage == null || rootPage == 0) {
                System.err.println("Warning: " + type + " " + name + " has no valid root page.");
                return -1;
            }
            return rootPage;
        }
        return -1;
    }

    private String getSQLForSchemaTypeWithName(SchemaType type, String name) {
        SchemaEntry entry = schemaTable.get(type).get(name);
        if (entry != null) {
            return entry.getSql();
        }
        return "";
    }

    public int getTableCount() {
        return getSchemaTypeCount(SchemaType.TABLE);
    }

    public List<String> getTableNames() {
        return getSchemaTypeNames(SchemaType.TABLE);
    }

    public int getRootPageForTable(String tableName) {
        return getRootPageForSchemaTypeWithName(SchemaType.TABLE, tableName);
    }

    public String getSQLForTable(String tableName) {
        return getSQLForSchemaTypeWithName(SchemaType.TABLE, tableName);
    }

    public int getIndexCount() {
        return getSchemaTypeCount(SchemaType.INDEX);
    }

    public List<String> getIndexNames() {
        return getSchemaTypeNames(SchemaType.INDEX);
    }

    public int getRootPageForIndex(String indexName) {
        return getRootPageForSchemaTypeWithName(SchemaType.INDEX, indexName);
    }

    public String getSQLForIndex(String indexName) {
        return getSQLForSchemaTypeWithName(SchemaType.INDEX, indexName);
    }
}
