package db.data;

import java.nio.ByteBuffer;

public class RecordFactory {
    public static Record createTableRecord(ByteBuffer buffer, Varint rowId) {
        return Record.fromByteBufferWithRowId(buffer, rowId);
    }

    public static Record createIndexRecord(ByteBuffer buffer) {
        return Record.fromByteBuffer(buffer);
    }
}
