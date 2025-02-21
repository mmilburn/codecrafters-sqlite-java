package query;

import config.ConfigContext;
import db.btree.BTreePage;
import db.btree.PageType;
import db.btree.cell.CellType;
import db.btree.cell.TableInteriorCell;
import db.btree.cell.TableLeafCell;
import db.data.Column;
import db.data.ColumnType;
import db.data.TableRecord;
import db.schema.ddl.HackyCreateTableParser;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class SelectQueryHandler {

    private final ConfigContext configContext;
    //Queries we should support:
    //"SELECT COUNT(*) FROM apples"
    //"SELECT name FROM apples"
    //"SELECT name, color FROM apples"
    //"SELECT name, color FROM apples WHERE color = 'Yellow'"
    //"SELECT id, name FROM superheroes WHERE eye_color = 'Pink Eyes'"
    //Output from multiple columns should be pipe delimited.

    public SelectQueryHandler(ConfigContext configContext) {
        this.configContext = configContext;
    }

    public void executeSelectQuery(String command) {
        HackyQueryParser queryParser = HackyQueryParser.fromSQLQuery(command);
        HackyCreateTableParser ddlParser = configContext.getParserForTable(queryParser.getTable());
        if (ddlParser == null) {
            System.err.println("Couldn't get table parser for: " + queryParser.getTable());
            return;
        }
        Integer rootPage = configContext.getRootPageForTable(queryParser.getTable());
        Integer rootPageForIndex = -1;
        if (!queryParser.getConditions().isEmpty()) {
            rootPageForIndex = configContext.getRootPageForIndexedColumn(
                    queryParser.getTable(), queryParser.getConditions().getFirst().column()
            );
            if (rootPageForIndex != -1) {
                BTreePage indexPage = configContext.getPage(rootPageForIndex);
                System.err.println("indexPage pageType: " + indexPage.getPageType());
            }
        }
        BTreePage page = configContext.getPage(rootPage);
        List<Integer> pageIndex = List.of(rootPage);
        if (page.getPageType() == PageType.TABLE_INTERIOR) {
            pageIndex = page.getCellStream()
                    .filter(cell -> cell.cellType() == CellType.TABLE_INTERIOR)
                    .map(cell -> ((TableInteriorCell) cell).leftChildPointer()).toList();
        }

        if (queryParser.hasCountOperation()) {
            System.out.println(pageIndex.stream()
                    .map(index -> configContext.getPage(index).getCellsCount())
                    .reduce(0, Integer::sum)
            );
        } else if (!queryParser.getColsOrFuncs().isEmpty()) {
            processRecords(pageIndex, queryParser, ddlParser);
        } else {
            System.err.println("Support for query: " + command + " not implemented!");
        }
    }

    private void processRecords(List<Integer> pageIndex, HackyQueryParser parser, HackyCreateTableParser ddlParser) {
        pageIndex.forEach(index -> {
            configContext.getPage(index).getCellStream()
                    .filter(cell -> cell.cellType() == CellType.TABLE_LEAF)
                    .map(cell -> (TableLeafCell) cell)
                    .map(leafCell -> filterAndFormatRecord(leafCell, parser, ddlParser))
                    .filter(str -> !str.isEmpty())
                    .forEach(System.out::println);
        });

    }

    private String filterAndFormatRecord(TableLeafCell leafCell, HackyQueryParser parser, HackyCreateTableParser ddlParser) {
        Predicate<TableRecord> where = createWherePredicate(parser, ddlParser);
        List<String> vals = new ArrayList<>();
        TableRecord rec = leafCell.initialPayload();

        if (where.test(rec)) {
            for (String colName : parser.getColsOrFuncs()) {
                if (ddlParser.isRowIdAlias(colName)) {
                    vals.add(String.valueOf(leafCell.rowID().getValue()));
                } else {
                    int index = ddlParser.ordinalForColumn(colName);
                    ColumnType type = ddlParser.getColumnType(colName);
                    Column col = rec.getColumnForIndex(index);
                    vals.add(String.valueOf(col.getValueAs(type, configContext.getCharset())));
                }
            }
        }
        return String.join("|", vals);
    }

    private Predicate<TableRecord> createWherePredicate(HackyQueryParser parser, HackyCreateTableParser ddlParser) {
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
            int index = ddlParser.ordinalForColumn(whereCol);
            ColumnType type = ddlParser.getColumnType(whereCol);
            Column col = rec.getColumnForIndex(index);
            return String.valueOf(col.getValueAs(type, configContext.getCharset())).equals(condition.value());
        };
    }
}
