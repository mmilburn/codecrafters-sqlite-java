package db.btree;

import config.PageHeader;
import config.SQLiteHeader;
import db.btree.cell.Cell;
import db.schema.SQLiteSchema;

import java.nio.ByteBuffer;
import java.util.List;

public class FirstPage implements BTreePage {
    private final SQLiteHeader sqLiteHeader;
    private final SQLiteSchema sqliteSchema;
    private final BTreePage delegate;

    private FirstPage(SQLiteHeader header, BTreePage delegate) {
        this.sqLiteHeader = header;
        this.delegate = delegate;
        this.sqliteSchema = SQLiteSchema.fromCellsWithCharset(this.delegate.getCells(), this.sqLiteHeader.getCharset());
    }

    public static FirstPage fromByteBuffer(ByteBuffer data) {
        SQLiteHeader header = SQLiteHeader.fromByteBuffer(data);
        //Headers supposedly occupy the first 100 bytes.
        int bytesRead = data.position();
        if (bytesRead != 100) {
            throw new IllegalStateException("Expected to read a 100 byte header, read " + bytesRead + " bytes.");
        }
        return new FirstPage(header, RegularPage.pageFromByteBuffer(data));
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

    public SQLiteSchema getSqliteSchema() {
        return sqliteSchema;
    }
}
