import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class Record {
    private final Varint headerSize;
    private final List<Column> columns;

    private Record(Varint headerSize, List<Column> columns) {
        this.headerSize = headerSize;
        this.columns = columns;
    }

    public static Record fromByteBuffer(ByteBuffer buffer) {
        Varint size = Varint.fromByteBuffer(buffer);
        int serialTypeListSize = size.asInt() - size.getBytesConsumed();
        int headerEnd = buffer.position() + serialTypeListSize;
        ByteBuffer headerSlice = buffer.duplicate().limit(headerEnd);
        buffer.position(headerEnd);
        List<Column> columns = new ArrayList<>();
        while (headerSlice.hasRemaining()) {
            SerialType type = SerialType.fromByteBuffer(headerSlice);
            columns.add(new Column(type, type.contentFromByteBuffer(buffer)));
        }
        return new Record(size, columns);
    }

    public Varint getHeaderSize() {
        return headerSize;
    }

    public List<Column> getColumns() {
        return columns;
    }
}
