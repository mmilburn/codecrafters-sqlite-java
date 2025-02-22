package db.data;

import java.nio.ByteBuffer;

public class RecordFactory {
    public static Record createTableRecord(ByteBuffer buffer) {
        return Record.fromByteBuffer(buffer, false);
    }

    public static Record createIndexRecord(ByteBuffer buffer) {
        return Record.fromByteBuffer(buffer, true);
    }
}
