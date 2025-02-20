package query;

import config.ConfigContext;
import db.btree.BTreePage;
import db.btree.PageType;
import db.btree.cell.CellType;
import db.btree.cell.TableInteriorCell;
import db.btree.cell.TableLeafCell;
import db.data.Column;
import db.data.ColumnType;
import db.data.Record;
import db.schema.HackyDDLParser;

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
        Integer rootPage = configContext.getRootPageForTable(parser.getTable());
        BTreePage page = lazyPages.get(rootPage).get();
        List<Integer> pageIndex = List.of(rootPage);
        if (page.getPageType() == PageType.TABLE_INTERIOR) {
            pageIndex = page.getCells().stream()
                    .filter(cell -> cell.cellType() == CellType.TABLE_INTERIOR)
                    .map(cell -> ((TableInteriorCell) cell).leftChildPointer()).toList();
        }

        if (parser.hasCountOperation()) {
            System.out.println(pageIndex.stream()
                    .map(index -> lazyPages.get(index).get().getCellsCount())
                    .reduce(0, Integer::sum)
            );
        } else if (!parser.getColsOrFuncs().isEmpty()) {
            processRecords(pageIndex, parser, ddlParser);
        } else {
            System.err.println("Support for query: " + command + " not implemented!");
        }
    }

    private void processRecords(List<Integer> pageIndex, HackyQueryParser parser, HackyDDLParser ddlParser) {
        pageIndex.forEach(index -> {
            lazyPages.get(index).get().getCells().stream()
                    .filter(cell -> cell.cellType() == CellType.TABLE_LEAF)
                    .map(cell -> (TableLeafCell) cell)
                    .map(leafCell -> filterAndFormatRecord(leafCell, parser, ddlParser))
                    .filter(str -> !str.isEmpty())
                    .forEach(System.out::println);
        });

    }

    private String filterAndFormatRecord(TableLeafCell leafCell, HackyQueryParser parser, HackyDDLParser ddlParser) {
        Predicate<Record> where = createWherePredicate(parser, ddlParser);
        List<String> vals = new ArrayList<>();
        Record rec = leafCell.initialPayload();

        if (where.test(rec)) {
            for (String colName : parser.getColsOrFuncs()) {
                if (ddlParser.isRowIdAlias(colName)) {
                    vals.add(String.valueOf(leafCell.rowID().getValue()));
                } else {
                    int index = ddlParser.indexForColumn(colName);
                    ColumnType type = ddlParser.getColumnType(colName);
                    Column col = rec.getColumnForIndex(index);
                    vals.add(String.valueOf(col.getValueAs(type, configContext.getCharset())));
                }
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
            System.err.println("query.Condition operator: " + condition.operator().getType() + " unsupported!");
        }

        return rec -> {
            String whereCol = condition.column();
            int index = ddlParser.indexForColumn(whereCol);
            ColumnType type = ddlParser.getColumnType(whereCol);
            Column col = rec.getColumnForIndex(index);
            return String.valueOf(col.getValueAs(type, configContext.getCharset())).equals(condition.value());
        };
    }
}
