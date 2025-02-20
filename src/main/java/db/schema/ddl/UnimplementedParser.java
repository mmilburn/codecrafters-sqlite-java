package db.schema.ddl;

public final class UnimplementedParser implements DDLParser {
    @Override
    public String getName() {
        return "";
    }

    public static UnimplementedParser fromDDL(String ddl) {
        return new UnimplementedParser();
    }
}
