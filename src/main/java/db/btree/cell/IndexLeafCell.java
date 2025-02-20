package db.btree.cell;

import db.data.Varint;

import java.nio.ByteBuffer;

public record IndexLeafCell(
        Varint payloadSize,
        byte[] initialPayload,
        int firstOverflowPage
) implements Cell {

    public static IndexLeafCell fromByteBuffer(ByteBuffer data) {
        Varint size = Varint.fromByteBuffer(data);
        byte[] payload = new byte[size.asInt()];
        data.get(payload);
        //Overflow ignored.
        return new IndexLeafCell(size, payload, -1);
    }

    @Override
    public CellType cellType() {
        return CellType.INDEX_LEAF;
    }
}
