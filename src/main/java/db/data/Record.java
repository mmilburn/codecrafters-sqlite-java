package db.data;

import util.Memoization;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class Record {
    private final Varint headerSize;
    private final Map<Integer, Supplier<Column>> lazyCols;
    private Varint rowId = null;

    private Record(Varint headerSize, Map<Integer, Supplier<Column>> lazyCols, Varint rowId) {
        this.headerSize = headerSize;
        this.lazyCols = lazyCols;
        this.rowId = rowId;
    }

    private Record(Varint headerSize, Map<Integer, Supplier<Column>> lazyCols) {
        this.headerSize = headerSize;
        this.lazyCols = lazyCols;
    }

    public Column getColumnForIndex(int index) {
        return this.lazyCols.get(index).get();
    }

    public int getNumberOfColumns() {
        return this.lazyCols.size();
    }

    public Varint getHeaderSize() {
        return headerSize;
    }

    public Long getRowID() {
        if (rowId != null) {
            return rowId.getValue();
        }
        //this should be a record attached to an index cell.
        int rowIdIndex = this.getNumberOfColumns() - 1;
        return this.getColumnForIndex(rowIdIndex).getAsNullableLong();
    }

    public static Record fromByteBufferWithRowId(ByteBuffer buffer, Varint rowId) {
        return parseFromByteBuffer(buffer, rowId);
    }

    public static Record fromByteBuffer(ByteBuffer buffer) {
        return parseFromByteBuffer(buffer, null);
    }

    private static Record parseFromByteBuffer(ByteBuffer buffer, Varint rowId) {
        Varint size = Varint.fromByteBuffer(buffer);
        int serialTypeListSize = size.asInt() - size.getBytesConsumed();
        final int headerEnd = buffer.position() + serialTypeListSize;
        ByteBuffer headerSlice = buffer.duplicate().limit(headerEnd);
        buffer.position(headerEnd);

        Map<Integer, Supplier<Column>> lazyCols = new HashMap<>();
        final int[] offset = {0};

        for (int i = 0; headerSlice.hasRemaining(); i++) {
            SerialType type = SerialType.fromByteBuffer(headerSlice);
            int currentOffset = offset[0];

            lazyCols.put(i, Memoization.memoize(() -> {
                ByteBuffer dup = buffer.duplicate().position(headerEnd + currentOffset);
                return new Column(type, type.contentFromByteBuffer(dup));
            }));

            offset[0] += type.getSize();
        }

        if (rowId != null) {
            return new Record(size, lazyCols, rowId);
        }
        return new Record(size, lazyCols);
    }

    public static Record countRecord(long count) {
        Map<Integer, Supplier<Column>> lazyCols = new HashMap<>();
        lazyCols.put(0, Memoization.memoize(() -> new Column(new SerialType(Varint.fromValue(6)), count)));
        //DIRTY HACKS!!
        //Just to support COUNT(*)
        return new Record(Varint.fromValue(2), lazyCols);
    }
}
