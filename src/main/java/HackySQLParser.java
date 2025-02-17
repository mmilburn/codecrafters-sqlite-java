import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class HackySQLParser {
    private static final Pattern SQL_PATTERN = Pattern.compile(
            "(?i)select\\s+(.*?)\\s+from\\s+(\\S+)(?:\\s+where\\s+(.*))?"
    );

    private final List<String> columns;
    private final String table;
    private final Map<String, String> conditions;

    private HackySQLParser(List<String> columns, String table, Map<String, String> conditions) {
        this.columns = columns;
        this.table = table;
        this.conditions = conditions;
    }

    public static HackySQLParser fromSQLQuery(String query) {
        Matcher matcher = SQL_PATTERN.matcher(query);

        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid SQL query: " + query);
        }

        List<String> columns = Arrays.stream(matcher.group(1).split(","))
                .map(String::trim)
                .collect(Collectors.toList());
        String table = matcher.group(2);

        Map<String, String> conditions = new HashMap<>();
        if (matcher.group(3) != null) {
            extractConditions(matcher.group(3), conditions);
        }

        return new HackySQLParser(columns, table, conditions);
    }

    private static void extractConditions(String conditionString, Map<String, String> conditions) {
        String[] conditionsArray = conditionString.split("\\s+(?i)and\\s+");
        for (String condition : conditionsArray) {
            String[] parts = condition.split("\\s*(=|!=|<|>|<=|>=)\\s*");
            if (parts.length == 2) {
                conditions.put(parts[0].trim(), parts[1].trim());
            }
        }
    }

    public List<String> getColumns() {
        return columns;
    }

    public String getTable() {
        return table;
    }

    public Map<String, String> getConditions() {
        return conditions;
    }

    public boolean hasConditions() {
        return !conditions.isEmpty();
    }

    @Override
    public String toString() {
        return "HackySQLParser{" +
                "columns=" + columns +
                ", table='" + table + '\'' +
                ", conditions=" + conditions +
                '}';
    }
}
