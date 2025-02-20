package db.data;

import util.Memoization;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class IndexRecord extends Record {
    private final Varint rowID;

    private IndexRecord(Varint headerSize, Map<Integer, Supplier<Column>> lazyCols, Varint rowID) {
        super(headerSize, lazyCols);
        this.rowID = rowID;
    }

    public static IndexRecord fromByteBuffer(ByteBuffer buffer) {
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
        //Our offset[0] should have accumulated the size of the last of the columns
        int endOfCols = buffer.position() + offset[0];
        if (endOfCols >= buffer.limit()) {
            System.err.println("End of columns offset: " + endOfCols +  "buffer.limit(): " + buffer.limit());
        }
        Varint rowID = Varint.fromByteBuffer(buffer.position(endOfCols)); // Extract rowID at the end
        return new IndexRecord(size, lazyCols, rowID);
    }

    public Varint getRowID() {
        return rowID;
    }
}

