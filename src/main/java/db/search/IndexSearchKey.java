package db.search;

import config.ConfigContext;
import db.data.Column;

public class IndexSearchKey implements SearchKey<Column> {
    private final Column searchColumn;
    private final ConfigContext configContext;

    public IndexSearchKey(Column searchColumn, ConfigContext configContext) {
        this.searchColumn = searchColumn;
        this.configContext = configContext;
    }

    @Override
    public int compareTo(Column other) {
        return searchColumn.getAsString(configContext.getCharset())
                .compareTo(other.getAsString(configContext.getCharset()));
    }
}