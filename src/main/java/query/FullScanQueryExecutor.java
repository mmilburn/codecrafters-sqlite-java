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

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class FullScanQueryExecutor implements QueryExecutor {
    private final ConfigContext configContext;
    private final QueryContext queryContext;

    public FullScanQueryExecutor(ConfigContext configContext, QueryContext queryContext) {
        this.configContext = configContext;
        this.queryContext = queryContext;
    }

    @Override
    public Stream<Record> execute() {
        List<Integer> pageIndexes = getPageIndexes();
        Predicate<Record> wherePredicate = createWherePredicate();

        return pageIndexes.stream()
                .flatMap(this::getPageRecords)
                .filter(wherePredicate);
    }

    private List<Integer> getPageIndexes() {
        BTreePage rootPage = queryContext.getTableRootPage();

        if (rootPage.getPageType() == PageType.TABLE_LEAF) {
            return List.of(queryContext.getTableRootPageNumber());
        }

        // If we have an interior page, collect all child page numbers
        return rootPage.getCellStream()
                .filter(cell -> cell.cellType() == CellType.TABLE_INTERIOR)
                .map(cell -> ((TableInteriorCell) cell).leftChildPointer())
                .toList();
    }

    private Stream<Record> getPageRecords(Integer pageIndex) {
        return configContext.getPage(pageIndex)
                .getCellStream()
                .filter(cell -> cell.cellType() == CellType.TABLE_LEAF)
                .map(cell -> ((TableLeafCell) cell).initialPayload());
    }

    private Predicate<Record> createWherePredicate() {
        if (!queryContext.hasConditions()) {
            return record -> true;
        }

        // For now, we only support a single condition
        Condition condition = queryContext.getFirstCondition();
        if (condition.operator() != OperatorType.EQUALS) {
            System.err.println("Condition operator: " + condition.operator().getType() + " unsupported!");
            return _ -> true;
        }

        return record -> {
            String columnName = condition.column();
            int columnIndex = queryContext.getColumnIndex(columnName);
            ColumnType columnType = queryContext.getColumnType(columnName);
            Column column = record.getColumnForIndex(columnIndex);

            if (column == null) {
                return false;
            }

            Object value = column.getValueAs(columnType, configContext.getCharset());
            return String.valueOf(value).equals(condition.value());
        };
    }
}
