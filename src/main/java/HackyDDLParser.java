import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class HackyDDLParser {
    private static final Pattern CREATE_TABLE_PATTERN = Pattern.compile(
            "(?i)create table\\s+([A-Za-z_][A-Za-z0-9_]*)\\s+\\((.*)\\)"
    );

    private final String table;
    private final Map<String, Integer> colToIndex;

    private HackyDDLParser(String table, Map<String, Integer> colToIndex) {
        this.table = table;
        this.colToIndex = colToIndex;
    }

    public static HackyDDLParser fromCreateTable(String ddl) {
        Matcher matcher = CREATE_TABLE_PATTERN.matcher(ddl);

        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid create table statement: " + ddl);
        }
        String table = matcher.group(1);
        AtomicInteger index = new AtomicInteger(0);
        Map<String, Integer> colOrder = Arrays.stream(matcher.group(2).split(","))
                .map(String::trim)
                .map(entry -> entry.split(" ")[0])
                .collect(Collectors.toMap(word -> word, word -> index.getAndIncrement(), (a, b) -> a));
        return new HackyDDLParser(table, colOrder);
    }

    public String getTable() {
        return table;
    }

    public Set<String> getColumns() {
        return colToIndex.keySet();
    }

    public int indexForColumn(String columnName) {
        return colToIndex.getOrDefault(columnName, -1);
    }
}
