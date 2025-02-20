package query;

import java.util.Arrays;

public enum OperatorType {
    EQUALS("="),
    NOT_EQUAL("!="),
    LESS_THAN("<"),
    GREATER_THAN(">"),
    LESS_THAN_EQUALS("<="),
    GREATER_THAN_EQUALS(">="),
    UNKNOWN("unknown");

    private final String type;

    OperatorType(String type) {
        this.type = type;
    }

    public static OperatorType from(String str) {
        return Arrays.stream(values())
                .filter(type -> type.type.equals(str))
                .findFirst()
                .orElse(UNKNOWN);
    }

    public String getType() {
        return type;
    }
}
