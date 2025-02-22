package db.data;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import util.Memoization;

public class Record {
    protected final Varint headerSize;
    protected final Map<Integer, Supplier<Column>> lazyCols;
    protected final boolean hasRowID;

    protected Record(Varint headerSize, Map<Integer, Supplier<Column>> lazyCols, boolean hasRowID) {
        this.headerSize = headerSize;
        this.lazyCols = lazyCols;
        this.hasRowID = hasRowID;
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

    public boolean hasRowID() {
        return hasRowID;
    }

    public Long getRowID() {
        if (!hasRowID) {
            return null;
        }
        int rowIdIndex = this.getNumberOfColumns() - 1;
        return this.getColumnForIndex(rowIdIndex).getAsNullableLong();
    }

    public static Record fromByteBuffer(ByteBuffer buffer, boolean hasRowID) {
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

        return new Record(size, lazyCols, hasRowID);
    }
}
