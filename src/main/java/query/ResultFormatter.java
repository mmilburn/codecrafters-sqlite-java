package query;

import config.ConfigContext;
import db.data.Column;
import db.data.ColumnType;
import db.data.Record;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class ResultFormatter {
    private final ConfigContext configContext;

    public ResultFormatter(ConfigContext configContext) {
        this.configContext = configContext;
    }

    public void formatAndPrint(Stream<Record> results, QueryContext context) {
        results
                .map(record -> formatRecord(record, context))
                .filter(str -> !str.isEmpty())
                .forEach(System.out::println);
    }

    private String formatRecord(Record record, QueryContext context) {
        List<String> values = new ArrayList<>();
        //Short circuit for COUNT(*)
        if (context.hasCountOperation()) {
            return String.valueOf(record.getColumnForIndex(0).getAsNullableLong());
        }

        for (String column : context.getSelectedColumns()) {
            if (context.isRowIdAlias(column)) {
                values.add(String.valueOf(record.getRowID()));
            } else {
                int index = context.getColumnIndex(column);
                ColumnType type = context.getColumnType(column);
                Column col = record.getColumnForIndex(index);
                values.add(String.valueOf(
                        col.getValueAs(type, configContext.getCharset())));
            }
        }

        return String.join("|", values);
    }
}