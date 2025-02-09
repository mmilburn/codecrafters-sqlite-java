import java.nio.ByteBuffer;

public class PageHeader {
    private final byte pageType;
    private final short firstFreeBlock;
    private final short cellsCount;
    private final short cellAreaStart;
    private final byte fragmentedFreeBytesCount;
    private final int rightmostPointer;

    private PageHeader(byte pageType, short firstFreeBlock, short cellsCount, short cellAreaStart, byte fragmentedFreeBytesCount, int rightmostPointer) {
        this.pageType = pageType;
        this.firstFreeBlock = firstFreeBlock;
        this.cellsCount = cellsCount;
        this.cellAreaStart = cellAreaStart;
        this.fragmentedFreeBytesCount = fragmentedFreeBytesCount;
        this.rightmostPointer = rightmostPointer;
    }

    public static PageHeader fromByteBuffer(ByteBuffer data) {
        byte pageType = data.get();
        return new PageHeader(
                pageType,
                data.getShort(),
                data.getShort(),
                data.getShort(),
                data.get(),
                pageType == 0x02 || pageType == 0x05 ? data.getInt() : -1
        );
    }

    public byte getPageType() {
        return pageType;
    }

    public short getFirstFreeBlock() {
        return firstFreeBlock;
    }

    public short getCellsCount() {
        return cellsCount;
    }

    public short getCellAreaStart() {
        return cellAreaStart;
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
