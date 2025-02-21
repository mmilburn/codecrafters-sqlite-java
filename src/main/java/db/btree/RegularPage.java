package db.btree;

import config.PageHeader;
import db.btree.cell.Cell;
import db.btree.cell.CellContainer;
import db.btree.cell.CellFactory;
import util.Memoization;

import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class RegularPage implements BTreePage {
    private final PageHeader pageHeader;
    private final short[] cellPointers;
    //private final byte[] unallocated;
    private final Map<Integer, Supplier<Cell>> cells;
    //private final byte[] reserved;
    private final CellContainer cellContainer;


    private RegularPage(PageHeader pageHeader, short[] cellPointers, Map<Integer, Supplier<Cell>> cells) {
        this.pageHeader = pageHeader;
        this.cellPointers = cellPointers;
        this.cells = cells;
        this.cellContainer = new CellContainer(this.cells);
    }

    public static BTreePage pageFromByteBuffer(ByteBuffer data) {
        PageHeader pageHeader = PageHeader.fromByteBuffer(data);
        int cellCount = pageHeader.getCellsCount();
        final PageType pageType = pageHeader.getPageType();
        ShortBuffer shortBuffer = data.asShortBuffer();
        final short[] cellPointers = new short[cellCount];
        shortBuffer.get(cellPointers, 0, cellCount);
        Map<Integer, Supplier<Cell>> cells = new HashMap<>();
        for (int i = 0; i < cellPointers.length; i++) {
            final int pointer = Short.toUnsignedInt(cellPointers[i]);
            cells.put(i, Memoization.memoize(() -> {
                ByteBuffer dup = data.duplicate();
                dup.position(pointer);
                return CellFactory.fromByteBuffer(dup, pageType);
            }));
        }
        return new RegularPage(pageHeader, cellPointers, cells);
    }

    @Override
    public PageHeader getPageHeader() {
        return pageHeader;
    }

    @Override
    public PageType getPageType() {
        return pageHeader.getPageType();
    }

    @Override
    public short[] getCellPointers() {
        return cellPointers;
    }

    @Override
    public Cell getCell(Integer index) {
        return cells.get(index).get();
    }

    @Override
    public int getCellsCount() {
        return this.pageHeader.getCellsCount();
    }

    @Override
    public Stream<Cell> getCellStream() {
        return cellContainer.getCellStream();
    }
}
