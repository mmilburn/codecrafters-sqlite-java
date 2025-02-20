package db.btree.cell;

import db.data.Record;
import db.data.Varint;

import java.nio.ByteBuffer;

public record IndexInteriorCell(
        int leftChildPointer,
        Varint payloadSize,
        Record initialPayload,
        int firstOverflowPage
) implements Cell {

    public static IndexInteriorCell fromByteBuffer(ByteBuffer data) {
        int pointer = data.getInt();
        Varint size = Varint.fromByteBuffer(data);
        Record payload = Record.fromByteBuffer(data.duplicate().limit(data.position() + size.asInt()));
        //Overflow ignored.
        return new IndexInteriorCell(pointer, size, payload, -1);
    }

    @Override
    public CellType cellType() {
        return CellType.INDEX_INTERIOR;
    }
}
