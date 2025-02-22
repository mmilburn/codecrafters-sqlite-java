package db.search;

public record RowIdSearchKey(long rowId) implements SearchKey<Long> {

    @Override
    public int compareTo(Long other) {
        return Long.compare(rowId, other);
    }
}