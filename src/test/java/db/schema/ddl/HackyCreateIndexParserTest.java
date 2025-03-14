package db.schema.ddl;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class HackyCreateIndexParserTest {

    @Test
    public void testValidCreateIndexStatement() {
        String ddl = "CREATE INDEX idx_companies_country ON companies (country)";
        HackyCreateIndexParser parser = HackyCreateIndexParser.fromDDL(ddl);

        assertEquals(parser.getName(), "idx_companies_country");
        assertEquals(parser.getTableName(), "companies");
        assertEquals(parser.getColumnName(), "country");
    }

    @Test
    public void testCreateIndexWithExtraWhitespace() {
        String ddl = "  CREATE    INDEX    idx_test   ON   mytable    (   column_name   )  ";
        HackyCreateIndexParser parser = HackyCreateIndexParser.fromDDL(ddl);

        assertEquals(parser.getName(), "idx_test");
        assertEquals(parser.getTableName(), "mytable");
        assertEquals(parser.getColumnName(), "column_name");
    }

    @Test
    public void testCaseInsensitivity() {
        String ddl = "create INDEX idx_test on mytable (column_name)";
        HackyCreateIndexParser parser = HackyCreateIndexParser.fromDDL(ddl);

        assertEquals(parser.getName(), "idx_test");
        assertEquals(parser.getTableName(), "mytable");
        assertEquals(parser.getColumnName(), "column_name");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testInvalidCreateIndexStatement() {
        String ddl = "CREATE INDEX idx_test mytable (column_name)"; // Missing ON keyword
        HackyCreateIndexParser.fromDDL(ddl);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testEmptyStatement() {
        HackyCreateIndexParser.fromDDL("");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testNonIndexStatement() {
        String ddl = "CREATE TABLE mytable (id INTEGER)";
        HackyCreateIndexParser.fromDDL(ddl);
    }

    @Test
    public void testToString() {
        String ddl = "CREATE INDEX idx_test ON mytable (column_name)";
        HackyCreateIndexParser parser = HackyCreateIndexParser.fromDDL(ddl);

        String expected = "HackyCreateIndexParser{" +
                "indexName='idx_test'" +
                ", tableName='mytable'" +
                ", columnName='column_name'" +
                '}';

        assertEquals(parser.toString(), expected);
    }
}