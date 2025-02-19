import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class SelectQueryHandler {

    private final ConfigContext configContext;
    private final Map<Integer, Supplier<BTreePage>> lazyPages;
    //Queries we should support:
    //"SELECT COUNT(*) FROM apples"
    //"SELECT name FROM apples"
    //"SELECT name, color FROM apples"
    //"SELECT name, color FROM apples WHERE color = 'Yellow'"
    //"SELECT id, name FROM superheroes WHERE eye_color = 'Pink Eyes'"
    //Output from multiple columns should be pipe delimited.

    public SelectQueryHandler(ConfigContext configContext, Map<Integer, Supplier<BTreePage>> lazyPages) {
        this.configContext = configContext;
        this.lazyPages = lazyPages;
    }

    public void executeSelectQuery(String command) {
        HackyQueryParser parser = HackyQueryParser.fromSQLQuery(command);
        HackyDDLParser ddlParser = HackyDDLParser.fromCreateTable(configContext.getSQLForTable(parser.getTable()));
        BTreePage page = lazyPages.get(configContext.getRootPageForTable(parser.getTable())).get();

        if (parser.hasCountOperation()) {
            System.out.println(page.getCellsCount());
        } else if (!parser.getColsOrFuncs().isEmpty()) {
            processRecords(page, parser, ddlParser);
        } else {
            System.err.println("Support for query: " + command + " not implemented!");
        }
    }

    private void processRecords(BTreePage page, HackyQueryParser parser, HackyDDLParser ddlParser) {
        page.getCells().stream()
                .filter(cell -> cell.cellType() == CellType.TABLE_LEAF)
                .map(cell -> (TableLeafCell) cell)
                .map(leafCell -> filterAndFormatRecord(leafCell, parser, ddlParser))
                .filter(str -> !str.isEmpty())
                .forEach(System.out::println);
    }

    private String filterAndFormatRecord(TableLeafCell leafCell, HackyQueryParser parser, HackyDDLParser ddlParser) {
        Predicate<Record> where = createWherePredicate(parser, ddlParser);
        List<String> vals = new ArrayList<>();
        Record rec = leafCell.initialPayload();

        if (where.test(rec)) {
            for (String colName : parser.getColsOrFuncs()) {
                colName = colName.toLowerCase();
                int index = ddlParser.indexForColumn(colName);
                ColumnType type = ddlParser.getColumnType(colName);
                Column col = rec.getColumnForIndex(index);
                vals.add(String.valueOf(col.getValueAs(type, configContext.getCharset())));
            }
        }
        return String.join("|", vals);
    }

    private Predicate<Record> createWherePredicate(HackyQueryParser parser, HackyDDLParser ddlParser) {
        if (parser.getConditions().isEmpty()) {
            return rec -> true;
        }
        if (parser.getConditions().size() > 1) {
            System.err.println("Only one condition in the query is supported.");
        }

        Condition condition = parser.getConditions().getFirst();
        if (condition.operator() != OperatorType.EQUALS) {
            System.err.println("Condition operator: " + condition.operator().getType() + " unsupported!");
        }

        return rec -> {
            String whereCol = condition.column().toLowerCase();
            int index = ddlParser.indexForColumn(whereCol);
            ColumnType type = ddlParser.getColumnType(whereCol);
            Column col = rec.getColumnForIndex(index);
            return String.valueOf(col.getValueAs(type, configContext.getCharset())).equals(condition.value());
        };
    }
}
