package db.schema.ddl;

/**
 * Interface for parsing DDL statements.
 * All implementing classes MUST provide a static factory method:
 * {@code public static ParserType fromDDL(String ddl)}
 */
public sealed interface DDLParser permits HackyCreateIndexParser, HackyCreateTableParser, UnimplementedParser {
    String getName();
}
