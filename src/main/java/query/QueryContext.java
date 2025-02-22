package query;

import config.ConfigContext;
import db.btree.BTreePage;
import db.data.ColumnType;
import db.schema.ddl.HackyCreateTableParser;

import java.util.List;
import java.util.Optional;

public class QueryContext {
    private final HackyQueryParser queryParser;
    private final HackyCreateTableParser ddlParser;
    private final ConfigContext configContext;
    private final String error;
    private final BTreePage tableRootPage;

    public QueryContext(HackyQueryParser queryParser, HackyCreateTableParser ddlParser,
                        ConfigContext configContext) {
        this.queryParser = queryParser;
        this.ddlParser = ddlParser;
        this.configContext = configContext;

        // Initialize table root page
        Integer rootPageNumber = configContext.getRootPageForTable(queryParser.getTable());
        this.tableRootPage = rootPageNumber != null ? configContext.getPage(rootPageNumber) : null;

        // Validate the context and set error if any
        this.error = validate();
    }

    private QueryContext(String error) {
        this.queryParser = null;
        this.ddlParser = null;
        this.configContext = null;
        this.tableRootPage = null;
        this.error = error;
    }

    public static QueryContext error(String message) {
        return new QueryContext(message);
    }

    private String validate() {
        if (queryParser == null) {
            return "Query parser is null";
        }
        if (ddlParser == null) {
            return "DDL parser is null";
        }
        if (tableRootPage == null) {
            return "Table root page not found";
        }

        // Validate columns exist in table
        for (String column : queryParser.getColsOrFuncs()) {
            if (!column.equalsIgnoreCase("count(*)") && !isRowIdAlias(column) && !ddlParser.hasColumn(column)) {
                return "Column not found: " + column;
            }
        }

        // Validate conditions
        for (Condition condition : queryParser.getConditions()) {
            if (!ddlParser.hasColumn(condition.column())) {
                return "Column in condition not found: " + condition.column();
            }
            if (condition.operator() != OperatorType.EQUALS) {
                return "Unsupported operator: " + condition.operator();
            }
        }

        return null;
    }

    public boolean isValid() {
        return error == null;
    }

    public String getError() {
        return error;
    }

    public String getTableName() {
        return queryParser.getTable();
    }

    public List<String> getSelectedColumns() {
        return queryParser.getColsOrFuncs();
    }

    public List<Condition> getConditions() {
        return queryParser.getConditions();
    }

    public boolean hasConditions() {
        return !queryParser.getConditions().isEmpty();
    }

    public Condition getFirstCondition() {
        return queryParser.getConditions().getFirst();
    }

    public boolean hasCountOperation() {
        return queryParser.hasCountOperation();
    }

    public boolean hasIndexableCondition() {
        if (!hasConditions()) {
            return false;
        }

        Condition condition = getFirstCondition();
        Integer indexRootPage = configContext.getRootPageForIndexedColumn(
                getTableName(),
                condition.column()
        );
        return indexRootPage != null && indexRootPage != -1;
    }

    public Integer getTableRootPageNumber() {
        return configContext.getRootPageForTable(queryParser.getTable());
    }

    public BTreePage getTableRootPage() {
        return tableRootPage;
    }

    public BTreePage getIndexRootPage(String columnName) {
        Integer indexRootPage = configContext.getRootPageForIndexedColumn(
                getTableName(),
                columnName
        );
        return indexRootPage != null ? configContext.getPage(indexRootPage) : null;
    }

    public int getColumnIndex(String columnName) {
        return ddlParser.ordinalForColumn(columnName);
    }

    public ColumnType getColumnType(String columnName) {
        return ddlParser.getColumnType(columnName);
    }

    public boolean isRowIdAlias(String columnName) {
        return ddlParser.isRowIdAlias(columnName);
    }

    public ConfigContext getConfigContext() {
        return configContext;
    }

    public Optional<String> getColumnValue(String columnName) {
        return getConditions().stream()
                .filter(c -> c.column().equals(columnName))
                .map(Condition::value)
                .findFirst();
    }

    public String getQueryString() {
        return queryParser.toString();
    }

    public boolean isColumnSelected(String columnName) {
        return queryParser.getColsOrFuncs().contains(columnName);
    }

    public boolean requiresFullTableScan() {
        return !hasIndexableCondition() && !hasCountOperation();
    }

    @Override
    public String toString() {
        if (!isValid()) {
            return "Invalid QueryContext: " + error;
        }
        return "QueryContext{" +
                "table='" + getTableName() + '\'' +
                ", columns=" + getSelectedColumns() +
                ", conditions=" + getConditions() +
                ", hasIndex=" + hasIndexableCondition() +
                ", isCount=" + hasCountOperation() +
                '}';
    }
}