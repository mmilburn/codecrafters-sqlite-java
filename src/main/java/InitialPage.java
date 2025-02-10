import java.nio.ByteBuffer;
import java.util.List;

public class InitialPage implements BTreePage {
    private final SQLiteHeader sqLiteHeader;
    private final BTreePage delegate;

    private InitialPage(SQLiteHeader header, BTreePage delegate) {
        this.sqLiteHeader = header;
        this.delegate = delegate;
    }

    public static InitialPage fromByteBuffer(ByteBuffer data) {
        SQLiteHeader header = SQLiteHeader.fromByteBuffer(data);
        //Headers supposedly occupy the first 100 bytes.
        int bytesRead = data.position();
        if (bytesRead != 100) {
            throw new IllegalStateException("Expected to read a 100 byte header, read " + bytesRead + " bytes.");
        }
        int pageSize = header.getPageSize() - bytesRead;
        return new InitialPage(header, RegularPage.sizedPageFromByteBuffer(data, pageSize));
    }

    @Override
    public PageHeader getPageHeader() {
        return delegate.getPageHeader();
    }

    @Override
    public short[] getCellPointers() {
        return delegate.getCellPointers();
    }

    @Override
    public List<Cell> getCells() {
        return this.delegate.getCells();
    }

    public SQLiteHeader getSqLiteHeader() {
        return sqLiteHeader;
    }
}
