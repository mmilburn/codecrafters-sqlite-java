import java.util.Arrays;

public enum OperatorType {
    EQUALS("="),
    NOT_EQUAL("!="),
    LESS_THAN("<"),
    GREATER_THAN(">"),
    LESS_THAN_EQUALS("<="),
    GREATER_THAN_EQUALS(">="),
    UNKNOWN("unknown");

    private final String operator;

    OperatorType(String operator) {
        this.operator = operator;
    }

    public static OperatorType from(String str) {
        return Arrays.stream(values())
                .filter(type -> type.operator.equals(str))
                .findFirst()
                .orElse(UNKNOWN);
    }
}
