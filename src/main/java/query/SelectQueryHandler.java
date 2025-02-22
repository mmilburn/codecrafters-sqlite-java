package query;

import config.ConfigContext;
import db.data.Record;
import db.schema.ddl.HackyCreateTableParser;

import java.util.stream.Stream;

public class SelectQueryHandler {
    private final ConfigContext configContext;
    private final QueryExecutorFactory executorFactory;
    private final ResultFormatter formatter;

    public SelectQueryHandler(ConfigContext configContext) {
        this.configContext = configContext;
        this.executorFactory = new QueryExecutorFactory(configContext);
        this.formatter = new ResultFormatter(configContext);
    }

    public void executeSelectQuery(String command) {
        QueryContext context = buildQueryContext(command);
        if (!context.isValid()) {
            System.err.println("Invalid query context: " + context.getError());
            return;
        }

        QueryExecutor executor = executorFactory.createExecutor(context);
        Stream<Record> results = executor.execute();

        formatter.formatAndPrint(results, context);
    }

    private QueryContext buildQueryContext(String command) {
        HackyQueryParser queryParser = HackyQueryParser.fromSQLQuery(command);
        HackyCreateTableParser ddlParser = configContext.getParserForTable(queryParser.getTable());

        if (ddlParser == null) {
            return QueryContext.error("Table not found: " + queryParser.getTable());
        }

        return new QueryContext(queryParser, ddlParser, configContext);
    }
}