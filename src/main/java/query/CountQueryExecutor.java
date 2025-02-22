package query;

import config.ConfigContext;
import db.btree.BTreePage;
import db.btree.PageType;
import db.btree.cell.CellType;
import db.btree.cell.TableInteriorCell;
import db.data.Record;

import java.util.List;
import java.util.stream.Stream;

public class CountQueryExecutor implements QueryExecutor {
    private final ConfigContext configContext;
    private final QueryContext queryContext;

    public CountQueryExecutor(ConfigContext configContext, QueryContext queryContext) {
        this.configContext = configContext;
        this.queryContext = queryContext;
    }

    @Override
    public Stream<Record> execute() {
        List<Integer> pageIndexes = getPageIndexes();
        long count = pageIndexes.stream()
                .mapToInt(index -> configContext.getPage(index).getCellsCount())
                .sum();

        return Stream.of(createCountRecord(count));
    }

    private List<Integer> getPageIndexes() {
        BTreePage rootPage = queryContext.getTableRootPage();

        if (rootPage.getPageType() == PageType.TABLE_LEAF) {
            return List.of(queryContext.getTableRootPageNumber());
        }

        return rootPage.getCellStream()
                .filter(cell -> cell.cellType() == CellType.TABLE_INTERIOR)
                .map(cell -> ((TableInteriorCell) cell).leftChildPointer())
                .toList();
    }

    private Record createCountRecord(long count) {
        return Record.countRecord(count);
    }
}