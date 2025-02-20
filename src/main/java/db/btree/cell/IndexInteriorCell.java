package db.btree.cell;

import db.data.Varint;

import java.nio.ByteBuffer;

public record IndexInteriorCell(
        int leftChildPointer,
        Varint payloadSize,
        byte[] initialPayload,
        int firstOverflowPage
) implements Cell {

    public static IndexInteriorCell fromByteBuffer(ByteBuffer data) {
        int pointer = data.getInt();
        Varint size = Varint.fromByteBuffer(data);
        byte[] payload = new byte[size.asInt()];
        data.get(payload);
        //Overflow ignored.
        return new IndexInteriorCell(pointer, size, payload, -1);
    }

    @Override
    public CellType cellType() {
        return CellType.INDEX_INTERIOR;
    }
}
