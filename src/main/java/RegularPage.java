import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class RegularPage implements BTreePage {
    private final PageHeader pageHeader;
    private final short[] cellPointers;
    //private final byte[] unallocated;
    private final List<Cell> cells;
    //private final byte[] reserved;


    private RegularPage(PageHeader pageHeader, short[] cellPointers, List<Cell> cells) {
        this.pageHeader = pageHeader;
        this.cellPointers = cellPointers;
        this.cells = cells;
    }

    public static BTreePage sizedPageFromByteBuffer(ByteBuffer data, int pageSize) {
        int start = data.position();
        PageHeader pageHeader = PageHeader.fromByteBuffer(data);
        int cellCount = pageHeader.getCellsCount();
        PageType pageType = pageHeader.getPageType();
        ShortBuffer shortBuffer = data.asShortBuffer();
        short[] cellPointers = new short[cellCount];
        shortBuffer.get(cellPointers, 0, cellCount);
        //Update position to reflect reading in the cellPointers.
        data.position(data.position() + cellCount * Short.BYTES);
        List<Cell> cells = IntStream.range(0, cellPointers.length)
                .mapToObj(i -> {
                    short pointer = cellPointers[i];
                    // Duplicate the buffer so we don’t change the shared buffer’s position.
                    ByteBuffer bufferClone = data.duplicate();
                    bufferClone.position(start + Short.toUnsignedInt(pointer));
                    return CellFactory.fromByteBuffer(bufferClone, pageType);
                })
                .collect(Collectors.toList());
        //Update position to the next page.
        data.position(start + pageSize);
        return new RegularPage(pageHeader, cellPointers, cells);
    }

    @Override
    public PageHeader getPageHeader() {
        return pageHeader;
    }

    @Override
    public short[] getCellPointers() {
        return cellPointers;
    }

    @Override
    public List<Cell> getCells() {
        return cells;
    }
}
