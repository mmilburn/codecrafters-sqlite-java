import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class HackyQueryParser {
    private static final Pattern SQL_PATTERN = Pattern.compile(
            "(?i)select\\s+(.*?)\\s+from\\s+(\\S+)(?:\\s+where\\s+(.*))?"
    );

    private final List<String> colsOrFuncs;
    private final String table;
    private final Map<String, String> conditions;

    private HackyQueryParser(List<String> colsOrFuncs, String table, Map<String, String> conditions) {
        this.colsOrFuncs = colsOrFuncs;
        this.table = table;
        this.conditions = conditions;
    }

    public static HackyQueryParser fromSQLQuery(String query) {
        Matcher matcher = SQL_PATTERN.matcher(query);

        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid SQL query: " + query);
        }

        List<String> colOrFuncs = Arrays.stream(matcher.group(1).split(","))
                .map(String::trim)
                .filter(str -> !str.contains(" "))
                .collect(Collectors.toList());
        if (colOrFuncs.isEmpty()) {
            throw new IllegalArgumentException("Invalid SQL query: " + query +
                    "\nNo columns or functions found!\nIs a comma missing?");
        }
        String table = matcher.group(2);

        Map<String, String> conditions = new HashMap<>();
        if (matcher.group(3) != null) {
            extractConditions(matcher.group(3), conditions);
        }

        return new HackyQueryParser(colOrFuncs, table, conditions);
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

    public List<String> getColsOrFuncs() {
        return colsOrFuncs;
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

    public boolean hasCountOperation() {
        return !colsOrFuncs.stream()
                .filter(str -> str.equalsIgnoreCase("count(*)"))
                .toList()
                .isEmpty();
    }

    @Override
    public String toString() {
        return "HackyQueryParser{" +
                "columns=" + colsOrFuncs +
                ", table='" + table + '\'' +
                ", conditions=" + conditions +
                '}';
    }
}
