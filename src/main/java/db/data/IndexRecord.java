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
        System.err.println("Record size: " + size.getValue());
        int serialTypeListSize = size.asInt() - size.getBytesConsumed();
        System.err.println("Serial Type list size: " + serialTypeListSize);
        System.err.println("Buffer position: " + buffer.position());
        System.err.println("Buffer limit: " + buffer.limit());
        final int headerEnd = buffer.position() + serialTypeListSize;
        System.err.println("headerEnd: " + headerEnd);
        ByteBuffer headerSlice = buffer.duplicate().limit(headerEnd);

        Map<Integer, Supplier<Column>> lazyCols = new HashMap<>();
        final int[] offset = {0};

        for (int i = 0; headerSlice.hasRemaining(); i++) {
            SerialType type = SerialType.fromByteBuffer(headerSlice);
            int currentOffset = offset[0];

            lazyCols.put(i, Memoization.memoize(() -> {
                ByteBuffer dup = buffer.duplicate().position(headerEnd + currentOffset);
                System.err.println("Position reading column data: " + dup.position());
                return new Column(type, type.contentFromByteBuffer(dup));
            }));

            offset[0] += type.getSize();
        }

        // Ensure endOfCols is within buffer limits
        int endOfCols = headerEnd + offset[0];
        if (endOfCols >= buffer.limit()) {
            System.err.println("Warning: endOfCols exceeds buffer limit! Defaulting rowID to -1.");
            return new IndexRecord(size, lazyCols, Varint.fromValue(-1));
        }

        buffer.position(endOfCols); // Move to the rowID position
        Varint rowID = buffer.hasRemaining() ? Varint.fromByteBuffer(buffer) : Varint.fromValue(-1);

        return new IndexRecord(size, lazyCols, rowID);
    }

    public Varint getRowID() {
        return rowID;
    }
}
