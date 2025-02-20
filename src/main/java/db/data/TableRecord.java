package db.data;

import util.Memoization;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class TableRecord extends Record {

    private TableRecord(Varint headerSize, Map<Integer, Supplier<Column>> lazyCols) {
        super(headerSize, lazyCols);
    }

    public static TableRecord fromByteBuffer(ByteBuffer buffer) {
        Varint size = Varint.fromByteBuffer(buffer);
        int serialTypeListSize = size.asInt() - size.getBytesConsumed();
        int headerEnd = buffer.position() + serialTypeListSize;
        ByteBuffer headerSlice = buffer.duplicate().limit(headerEnd);
        buffer.position(headerEnd);

        Map<Integer, Supplier<Column>> lazyCols = new HashMap<>();
        final int[] offset = {0};
        for (int i = 0; headerSlice.hasRemaining(); i++) {
            SerialType type = SerialType.fromByteBuffer(headerSlice);
            int currentOffset = offset[0];
            lazyCols.put(i, Memoization.memoize(() -> {
                ByteBuffer dup = buffer.duplicate().position(buffer.position() + currentOffset);
                return new Column(type, type.contentFromByteBuffer(dup));
            }));
            offset[0] += type.getSize();
        }
        return new TableRecord(size, lazyCols);
    }
}
