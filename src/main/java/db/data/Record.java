package db.data;

import java.util.Map;
import java.util.function.Supplier;

public abstract class Record {
    protected final Varint headerSize;
    protected final Map<Integer, Supplier<Column>> lazyCols;

    protected Record(Varint headerSize, Map<Integer, Supplier<Column>> lazyCols) {
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
}
