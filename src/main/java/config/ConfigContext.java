package config;

import db.schema.SqliteSchema;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ConfigContext {
    private final int pageSize;
    private final int numberOfPages;
    private final Charset charset;
    private final SqliteSchema sqliteSchema;

    private ConfigContext(Builder builder) {
        this.pageSize = builder.pageSize;
        this.numberOfPages = builder.numberOfPages;
        this.charset = builder.charset;
        this.sqliteSchema = builder.sqliteSchema;
    }

    public static Builder builder() {
        return new Builder();
    }

    public int getPageSize() {
        return pageSize;
    }

    public int getNumberOfPages() {
        return numberOfPages;
    }

    public Charset getCharset() {
        return charset;
    }

    public int getTableCount() {
        return sqliteSchema.getTableCount();
    }

    public List<String> getTableNames() {
        return sqliteSchema.getTableNames();
    }

    public Integer getRootPageForTable(String tableName) {
        return sqliteSchema.getRootPageForTable(tableName);
    }

    public String getSQLForTable(String tableName) {
        return sqliteSchema.getSQLForTable(tableName);
    }

    public int getIndexCount() {
        return sqliteSchema.getIndexCount();
    }

    public List<String> getIndexNames() {
        return sqliteSchema.getIndexNames();
    }

    public Integer getRootPageForIndex(String indexName) {
        return sqliteSchema.getRootPageForIndex(indexName);
    }

    public String getSQLForIndex(String indexName) {
        return sqliteSchema.getSQLForIndex(indexName);
    }

    public static class Builder {
        private int pageSize;
        private int numberOfPages;
        private Charset charset = StandardCharsets.UTF_8;
        private SqliteSchema sqliteSchema;

        public Builder withConfigOptionsFromHeader(SQLiteHeader header) {
            this.pageSize = header.getPageSize();
            this.numberOfPages = header.getNumberOfPages();
            this.charset = header.getCharset();
            return this;
        }

        public Builder withPageSize(int pageSize) {
            this.pageSize = pageSize;
            return this;
        }

        public Builder withNumberOfPages(int numberOfPages) {
            this.numberOfPages = numberOfPages;
            return this;
        }

        public Builder withCharset(Charset charset) {
            this.charset = charset;
            return this;
        }

        public Builder withSqliteSchema(SqliteSchema sqliteSchema) {
            this.sqliteSchema = sqliteSchema;
            return this;
        }

        public ConfigContext build() {
            return new ConfigContext(this);
        }
    }
}
