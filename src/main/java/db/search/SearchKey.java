package db.search;

public interface SearchKey<T> {
    int compareTo(T other);
}