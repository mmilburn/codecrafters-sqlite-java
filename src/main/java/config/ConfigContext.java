package config;

import db.btree.BTreePage;
import db.btree.FirstPage;
import db.btree.PageListFactory;
import db.schema.SqliteSchema;
import db.schema.ddl.HackyCreateTableParser;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class ConfigContext {
    private final int pageSize;
    private final int numberOfPages;
    private final Charset charset;
    private final SqliteSchema sqliteSchema;
    private final Map<Integer, Supplier<BTreePage>> lazyPages;

    public ConfigContext(ByteBuffer databaseFile) {
        this.lazyPages = PageListFactory.lazyPageMap(databaseFile);
        FirstPage firstPage = (FirstPage) lazyPages.get(1).get();
        SQLiteHeader header = firstPage.getSqLiteHeader();
        this.pageSize = header.getPageSize();
        this.numberOfPages = header.getNumberOfPages();
        this.charset = header.getCharset();
        this.sqliteSchema = firstPage.getSqliteSchema();
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

    public Integer getRootPageForIndexedColumn(String tableName, String columnName) {
        return sqliteSchema.getRootPageForIndexedColumn(tableName, columnName);
    }

    public HackyCreateTableParser getParserForTable(String tableName) {
        return sqliteSchema.getParserForTable(tableName);
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

    public BTreePage getPage(Integer index) {
        return lazyPages.get(index).get();
    }
}
