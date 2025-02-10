import java.nio.ByteBuffer;
import java.nio.ShortBuffer;

public class RegularPage implements BTreePage {
    private final PageHeader pageHeader;
    private final short[] cellPointers;
    //private final byte[] unallocated;
    //private final Cell[] cells;
    //private final byte[] reserved;


    private RegularPage(PageHeader pageHeader, short[] cellPointers) {
        this.pageHeader = pageHeader;
        this.cellPointers = cellPointers;
    }

    public static BTreePage sizedPageFromByteBuffer(ByteBuffer data, int pageSize) {
        int start = data.position();
        PageHeader pageHeader = PageHeader.fromByteBuffer(data);
        int cellCount = pageHeader.getCellsCount();
        ShortBuffer shortBuffer = data.asShortBuffer();
        short[] cellPointers = new short[cellCount];
        shortBuffer.get(cellPointers, 0, cellCount);
        //Update position to reflect reading in the cellPointers.
        data.position(data.position() + cellCount * Short.BYTES);
        //Update position to the next page.
        data.position(start + pageSize);
        return new RegularPage(pageHeader, cellPointers);
    }

    @Override
    public PageHeader getPageHeader() {
        return pageHeader;
    }

    @Override
    public short[] getCellPointers() {
        return cellPointers;
    }
}
