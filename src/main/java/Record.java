import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class Record {
    private final Varint headerSize;
    private final Map<Integer, Supplier<Column>> lazyCols;

    private Record(Varint headerSize, Map<Integer, Supplier<Column>> lazyCols) {
        this.headerSize = headerSize;
        this.lazyCols = lazyCols;
    }

    public static Record fromByteBuffer(ByteBuffer buffer) {
        Varint size = Varint.fromByteBuffer(buffer);
        int serialTypeListSize = size.asInt() - size.getBytesConsumed();
        int headerEnd = buffer.position() + serialTypeListSize;
        ByteBuffer headerSlice = buffer.duplicate().limit(headerEnd);
        buffer.position(headerEnd);
        Map<Integer, Supplier<Column>> lazyCols = new HashMap<>();
        int offset = 0;
        for (int i = 0; headerSlice.hasRemaining(); i++) {
            SerialType type = SerialType.fromByteBuffer(headerSlice);
            ByteBuffer duplicated = buffer.duplicate().position(buffer.position() + offset);
            lazyCols.put(i, () -> new Column(type, type.contentFromByteBuffer(duplicated)));
            offset += type.getSize();
        }
        return new Record(size, lazyCols);
    }

    public Varint getHeaderSize() {
        return headerSize;
    }

    public Column getColumnForIndex(int index) {
        return this.lazyCols.get(index).get();
    }

    public int getNumberOfColumns() {
        return this.lazyCols.size();
    }
}
