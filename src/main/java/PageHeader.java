import java.nio.ByteBuffer;

public class PageHeader {
    private final PageType pageType;
    private final short firstFreeBlock;
    private final short cellsCount;
    private final short cellContentAreaStart;
    private final byte fragmentedFreeBytesCount;
    private final int rightmostPointer;

    private PageHeader(PageType pageType, short firstFreeBlock, short cellsCount, short cellAreaStart, byte fragmentedFreeBytesCount, int rightmostPointer) {
        this.pageType = pageType;
        this.firstFreeBlock = firstFreeBlock;
        this.cellsCount = cellsCount;
        this.cellContentAreaStart = cellAreaStart;
        this.fragmentedFreeBytesCount = fragmentedFreeBytesCount;
        this.rightmostPointer = rightmostPointer;
    }

    public static PageHeader fromByteBuffer(ByteBuffer data) {
        PageType pageType = PageType.fromByteBuffer(data);
        return new PageHeader(
                pageType,
                data.getShort(),
                data.getShort(),
                data.getShort(),
                data.get(),
                pageType.isInterior() ? data.getInt() : -1
        );
    }

    public PageType getPageType() {
        return pageType;
    }

    public short getFirstFreeBlock() {
        return firstFreeBlock;
    }

    public short getCellsCount() {
        return cellsCount;
    }

    public int getCellContentAreaStart() {
        return cellContentAreaStart == 0 ? 65536 : Short.toUnsignedInt(cellContentAreaStart);
    }

    public byte getFragmentedFreeBytesCount() {
        return fragmentedFreeBytesCount;
    }

    public int getRightmostPointer() {
        return rightmostPointer;
    }

    public boolean hasRightmostPointer() {
        return this.rightmostPointer > -1;
    }
}
