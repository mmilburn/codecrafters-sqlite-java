package db.btree.cell;

import db.data.IndexRecord;
import db.data.Varint;

import java.nio.ByteBuffer;

public record IndexLeafCell(
        Varint payloadSize,
        IndexRecord initialPayload,
        int firstOverflowPage
) implements Cell {

    public static IndexLeafCell fromByteBuffer(ByteBuffer data) {
        Varint size = Varint.fromByteBuffer(data);
        IndexRecord payload = IndexRecord.fromByteBuffer(data.duplicate().limit(data.position() + size.asInt()));
        //Overflow ignored.
        return new IndexLeafCell(size, payload, -1);
    }

    @Override
    public CellType cellType() {
        return CellType.INDEX_LEAF;
    }
}
