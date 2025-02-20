package query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class HackyQueryParser {
    private static final Pattern SQL_PATTERN = Pattern.compile(
            "(?i)select\\s+(.*?)\\s+from\\s+(\\S+)(?:\\s+where\\s+(.*))?"
    );
    //Capture everything inside single or double quotes, then try capturing a non-space group.
    private static final Pattern CONDITION = Pattern.compile("(\\S+)\\s*(=|!=|<|>|<=|>=)\\s*('.*'|\".*\"|\\S+)");

    private final List<String> colsOrFuncs;
    private final String table;
    private final List<Condition> conditions;

    private HackyQueryParser(List<String> colsOrFuncs, String table, List<Condition> conditions) {
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
        List<Condition> conditions = new ArrayList<>();
        if (matcher.group(3) != null) {
            conditions = extractConditions(matcher.group(3));
        }

        return new HackyQueryParser(colOrFuncs, table, conditions);
    }

    private static List<Condition> extractConditions(String conditionString) {
        List<Condition> conditions = new ArrayList<>();
        String[] conditionsArray = conditionString.split("\\s+(?i)and\\s+");
        for (String condition : conditionsArray) {
            Matcher matcher = CONDITION.matcher(condition);
            if (matcher.matches()) {
                conditions.add(new Condition(
                        matcher.group(1).trim(),
                        OperatorType.from(matcher.group(2)),
                        //Remove enclosing single or double quotes.
                        matcher.group(3).replaceAll("^(['\"])(.*)\\1$", "$2").trim()
                ));
            } else {
                System.err.println("Didn't match: " + condition);
            }
        }
        return conditions;
    }

    public List<String> getColsOrFuncs() {
        return colsOrFuncs;
    }

    public String getTable() {
        return table;
    }

    public List<Condition> getConditions() {
        return conditions;
    }

    public boolean hasCountOperation() {
        return !colsOrFuncs.stream()
                .filter(str -> str.equalsIgnoreCase("count(*)"))
                .toList()
                .isEmpty();
    }

    @Override
    public String toString() {
        return "query.HackyQueryParser{" +
                "columns=" + colsOrFuncs +
                ", table='" + table + '\'' +
                ", conditions=" + conditions +
                '}';
    }
}
