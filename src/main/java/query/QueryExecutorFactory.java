package query;

import config.ConfigContext;

public class QueryExecutorFactory {
    private final ConfigContext configContext;

    public QueryExecutorFactory(ConfigContext configContext) {
        this.configContext = configContext;
    }

    public QueryExecutor createExecutor(QueryContext context) {
        if (context.hasIndexableCondition()) {
            return new IndexedQueryExecutor(configContext, context);
        } else if (context.hasCountOperation()) {
            return new CountQueryExecutor(configContext, context);
        } else {
            return new FullScanQueryExecutor(configContext, context);
        }
    }
}