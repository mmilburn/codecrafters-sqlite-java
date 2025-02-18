public record Condition(String column, OperatorType operator, String value) {
    @Override
    public String toString() {
        return column + " " + operator.getType() + " " + value;
    }
}
