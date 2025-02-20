package db.btree.cell;

import db.data.Record;
import db.data.Varint;

import java.nio.ByteBuffer;

public record IndexLeafCell(
        Varint payloadSize,
        Record initialPayload,
        int firstOverflowPage
) implements Cell {

    public static IndexLeafCell fromByteBuffer(ByteBuffer data) {
        Varint size = Varint.fromByteBuffer(data);
        Record payload = Record.fromByteBuffer(data.duplicate().limit(data.position() + size.asInt()));
        //Overflow ignored.
        return new IndexLeafCell(size, payload, -1);
    }

    @Override
    public CellType cellType() {
        return CellType.INDEX_LEAF;
    }
}
