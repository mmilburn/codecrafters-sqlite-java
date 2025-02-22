package query;

import java.util.stream.Stream;
import db.data.Record;

public interface QueryExecutor {
    Stream<Record> execute();
}