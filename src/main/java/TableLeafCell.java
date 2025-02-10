import java.nio.ByteBuffer;

public record TableLeafCell(
        Varint payloadSize,
        Varint rowID,
        byte[] initialPayload,
        int firstOverflowPage
) implements Cell {
    public static TableLeafCell fromByteBuffer(ByteBuffer data) {
        Varint payloadSize = Varint.fromByteBuffer(data);
        Varint rowId = Varint.fromByteBuffer(data);
        byte[] payload = new byte[payloadSize.asInt()];
        data.get(payload);
        //Overflow is currently ignored.
        return new TableLeafCell(payloadSize, rowId, payload, -1);
    }

    @Override
    public CellType cellType() {
        return CellType.TABLE_LEAF;
    }
}
