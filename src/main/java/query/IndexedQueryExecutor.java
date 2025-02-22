package query;

import config.ConfigContext;
import db.btree.BTreePage;
import db.btree.cell.TableLeafCell;
import db.data.Column;
import db.data.Record;
import db.search.IndexSearchImpl;
import db.search.IndexSearchKey;
import db.search.RowIdSearchKey;
import db.search.TableSearchImpl;

import java.util.Objects;
import java.util.PriorityQueue;
import java.util.stream.Stream;

public class IndexedQueryExecutor implements QueryExecutor {
    private final ConfigContext configContext;
    private final QueryContext queryContext;

    public IndexedQueryExecutor(ConfigContext configContext, QueryContext queryContext) {
        this.configContext = configContext;
        this.queryContext = queryContext;
    }

    @Override
    public Stream<Record> execute() {
        Condition whereCondition = queryContext.getFirstCondition();
        BTreePage tablePage = queryContext.getTableRootPage();
        BTreePage indexPage = queryContext.getIndexRootPage(whereCondition.column());

        IndexSearchImpl indexSearch = new IndexSearchImpl(configContext);
        TableSearchImpl tableSearch = new TableSearchImpl(configContext);

        Column searchValue = Column.fromString(configContext.getCharset(), whereCondition.value());
        PriorityQueue<Long> rowIds = indexSearch.search(indexPage,
                new IndexSearchKey(searchValue, configContext));

        return rowIds.stream()
                .map(rowId -> tableSearch.search(tablePage, new RowIdSearchKey(rowId)))
                .filter(Objects::nonNull)
                .map(TableLeafCell::initialPayload);
    }
}
