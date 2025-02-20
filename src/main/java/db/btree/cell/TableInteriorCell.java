package db.btree.cell;

import db.data.Varint;

import java.nio.ByteBuffer;

public record TableInteriorCell(int leftChildPointer, Varint rowID) implements Cell {

    public static TableInteriorCell fromByteBuffer(ByteBuffer data) {
        return new TableInteriorCell(data.getInt(), Varint.fromByteBuffer(data));
    }

    @Override
    public CellType cellType() {
        return CellType.TABLE_INTERIOR;
    }
}
