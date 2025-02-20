package db.btree;

import config.ConfigContext;
import config.PageHeader;
import config.SQLiteHeader;
import db.btree.cell.Cell;
import db.schema.SqliteSchema;

import java.nio.ByteBuffer;
import java.util.List;

public class InitialPage implements BTreePage {
    private final SQLiteHeader sqLiteHeader;
    private final SqliteSchema sqliteSchema;
    private final ConfigContext.Builder configContextBuilder;
    private final BTreePage delegate;

    private InitialPage(SQLiteHeader header, BTreePage delegate) {
        ConfigContext.Builder tempConfig;
        this.sqLiteHeader = header;
        tempConfig = ConfigContext.builder().withConfigOptionsFromHeader(this.sqLiteHeader);
        this.delegate = delegate;
        this.sqliteSchema = SqliteSchema.fromCellsWithCharset(this.delegate.getCells(), tempConfig.build().getCharset());
        tempConfig = tempConfig.withSqliteSchema(this.sqliteSchema);
        this.configContextBuilder = tempConfig;
    }

    public static InitialPage fromByteBuffer(ByteBuffer data) {
        SQLiteHeader header = SQLiteHeader.fromByteBuffer(data);
        //Headers supposedly occupy the first 100 bytes.
        int bytesRead = data.position();
        if (bytesRead != 100) {
            throw new IllegalStateException("Expected to read a 100 byte header, read " + bytesRead + " bytes.");
        }
        return new InitialPage(header, RegularPage.pageFromByteBuffer(data));
    }

    @Override
    public PageHeader getPageHeader() {
        return delegate.getPageHeader();
    }

    @Override
    public PageType getPageType() {
        return delegate.getPageType();
    }

    @Override
    public short[] getCellPointers() {
        return delegate.getCellPointers();
    }

    @Override
    public List<Cell> getCells() {
        return this.delegate.getCells();
    }

    @Override
    public int getCellsCount() {
        return this.delegate.getCellsCount();
    }

    public SQLiteHeader getSqLiteHeader() {
        return sqLiteHeader;
    }

    public SqliteSchema getSqliteSchema() {
        return sqliteSchema;
    }

    public ConfigContext.Builder getConfigContextBuilder() {
        return configContextBuilder;
    }
}
