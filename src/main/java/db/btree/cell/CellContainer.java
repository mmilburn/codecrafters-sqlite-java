package db.btree.cell;

import java.util.Iterator;
import java.util.Map;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class CellContainer {
    private final Map<Integer, Supplier<Cell>> cells;

    public CellContainer(Map<Integer, Supplier<Cell>> cells) {
        this.cells = cells;
    }

    public Iterator<Cell> getCellIterator() {
        return new Iterator<>() {
            private final Iterator<Supplier<Cell>> supplierIterator = cells.values().iterator();

            @Override
            public boolean hasNext() {
                return supplierIterator.hasNext();
            }

            @Override
            public Cell next() {
                return supplierIterator.next().get();
            }
        };
    }

    public Stream<Cell> getCellStream() {
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(getCellIterator(), Spliterator.ORDERED),
                false // We should only be using streams for smaller operations, no need to parallelize.
        );
    }
}
