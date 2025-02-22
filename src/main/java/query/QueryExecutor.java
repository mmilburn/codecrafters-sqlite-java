package query;

import db.data.Record;

import java.util.stream.Stream;

public interface QueryExecutor {
    Stream<Record> execute();
}