package db.btree.cell;

import db.data.Record;
import db.data.RecordFactory;
import db.data.Varint;

import java.nio.ByteBuffer;

public record TableLeafCell(
        Varint recordSize,
        Varint rowID,
        Record initialPayload,
        int firstOverflowPage
) implements Cell {
    public static TableLeafCell fromByteBuffer(ByteBuffer data) {
        Varint recordSize = Varint.fromByteBuffer(data);
        Varint rowId = Varint.fromByteBuffer(data);
        Record payload = RecordFactory.createTableRecord(data.duplicate().limit(data.position() + recordSize.asInt()), rowId);
        //int overflow = data.getInt();
        //Overflow is currently ignored.
        return new TableLeafCell(recordSize, rowId, payload, -1);
    }

    @Override
    public CellType cellType() {
        return CellType.TABLE_LEAF;
    }

    @Override
    public Varint recordSize() {
        return recordSize;
    }

    @Override
    public Varint rowID() {
        return rowID;
    }

    @Override
    public Record initialPayload() {
        return initialPayload;
    }

    @Override
    public int firstOverflowPage() {
        return firstOverflowPage;
    }
}
