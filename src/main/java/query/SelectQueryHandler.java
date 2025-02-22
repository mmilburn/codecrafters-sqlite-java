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
import db.schema.ddl.HackyCreateTableParser;
import db.search.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.PriorityQueue;
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
        Integer tableRootPage = configContext.getRootPageForTable(queryParser.getTable());
        BTreePage tablePage = configContext.getPage(tableRootPage);
        Integer rootPageForIndex = -1;
        if (!queryParser.getConditions().isEmpty()) {
            Condition whereCondition = queryParser.getConditions().getFirst();
            String columnName = whereCondition.column();
            Column columnValue = Column.fromString(configContext.getCharset(), whereCondition.value());
            ColumnType type = ddlParser.getColumnType(columnName);
            rootPageForIndex = configContext.getRootPageForIndexedColumn(queryParser.getTable(), columnName);

            if (rootPageForIndex != -1) {
                BTreePage indexPage = configContext.getPage(rootPageForIndex);
                IndexSearchImpl indexSearch = new IndexSearchImpl(configContext);
                TableSearchImpl tableSearch = new TableSearchImpl(configContext);

                PriorityQueue<Long> rowIds = indexSearch.search(indexPage, new IndexSearchKey(columnValue, configContext));
                rowIds.stream()
                        .map(rowId -> tableSearch.search(tablePage, new RowIdSearchKey(rowId)))
                        .filter(Objects::nonNull)
                        .map(leafCell -> String.join("|", getRowVals(leafCell, queryParser, ddlParser)))
                        .filter(str -> !str.isEmpty())
                        .forEach(System.out::println);
            }
        }
        List<Integer> pageIndex = List.of(tableRootPage);
        if (tablePage.getPageType() == PageType.TABLE_INTERIOR) {
            pageIndex = tablePage.getCellStream()
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

    private List<String> getRowVals(TableLeafCell leafCell, HackyQueryParser parser, HackyCreateTableParser ddlParser) {
        List<String> vals = new ArrayList<>();
        Record rec = leafCell.initialPayload();

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
        return vals;
    }

    private String filterAndFormatRecord(TableLeafCell leafCell, HackyQueryParser parser, HackyCreateTableParser ddlParser) {
        Predicate<Record> where = createWherePredicate(parser, ddlParser);
        List<String> vals = new ArrayList<>();
        Record rec = leafCell.initialPayload();

        if (where.test(rec)) {
            vals = getRowVals(leafCell, parser, ddlParser);
        }
        return String.join("|", vals);
    }

    private Predicate<Record> createWherePredicate(HackyQueryParser parser, HackyCreateTableParser ddlParser) {
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
