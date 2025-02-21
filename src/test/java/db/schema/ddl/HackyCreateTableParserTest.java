package db.schema.ddl;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Set;

public class HackyCreateTableParserTest {

    @DataProvider(name = "validDDLStatements")
    public Object[][] validDDLStatements() {
        return new Object[][]{
                {"CREATE TABLE banana (id integer primary key, name text)", "banana", Set.of("id", "name")},
                {"CREATE TABLE coconut (id integer primary key, name text)", "coconut", Set.of("id", "name")},
                {"CREATE TABLE mango (id integer primary key, name text)", "mango", Set.of("id", "name")},
                {"CREATE TABLE pistachio (id integer primary key, name text)", "pistachio", Set.of("id", "name")},
                {"CREATE TABLE vanilla (id integer primary key, name text)", "vanilla", Set.of("id", "name")},
                {"CREATE TABLE butterscotch (id integer primary key, name text)", "butterscotch", Set.of("id", "name")},
                {"CREATE TABLE grape (id integer primary key, name text)", "grape", Set.of("id", "name")},
                {"CREATE TABLE strawberry (id integer primary key, name text)", "strawberry", Set.of("id", "name")},
                {"CREATE TABLE watermelon (id integer primary key, name text)", "watermelon", Set.of("id", "name")},
                {"CREATE TABLE test (id integer primary key, name text)", "test", Set.of("id", "name")},
                // Additional cases with different column orders
                {"CREATE TABLE mixed (name text, id integer primary key)", "mixed", Set.of("id", "name")},
                {"CREATE TABLE multi_col (id integer primary key, name text, age integer, address text)", "multi_col",
                        Set.of("id", "name", "age", "address")},
                {"CREATE TABLE \"superheroes\" (id integer primary key autoincrement, name text not null, eye_color " +
                        "text, hair_color text, appearance_count integer, first_appearance text, first_appearance_year" +
                        " text)", "\"superheroes\"", Set.of("id", "name", "eye_color", "hair_color", "appearance_count",
                        "first_appearance", "first_appearance_year")}
        };
    }

    @Test(dataProvider = "validDDLStatements")
    public void testValidDDLParsing(String ddl, String expectedTable, Set<String> expectedColumns) {
        HackyCreateTableParser parser = HackyCreateTableParser.fromDDL(ddl);

        Assert.assertEquals(parser.getName(), expectedTable, "Table name should match");
        Assert.assertEquals(parser.getColumns(), expectedColumns, "Column names should match");
    }

    @DataProvider(name = "multiLineDDLStatements")
    public Object[][] multiLineDDLStatements() {
        return new Object[][]{
                {"""
                CREATE TABLE companies
                (
                    id integer primary key autoincrement,
                    name text,
                    domain text,
                    year_founded text,
                    industry text,
                    "size range" text,
                    locality text,
                    country text,
                    current_employees text,
                    total_employees text
                )""",
                        "companies",
                        Set.of("id", "name", "domain", "year_founded", "industry", "\"size range\"", "locality",
                                "country", "current_employees", "total_employees")}
        };
    }

    @Test(dataProvider = "multiLineDDLStatements")
    public void testMultiLineDDLParsing(String ddl, String expectedTable, Set<String> expectedColumns) {
        HackyCreateTableParser parser = HackyCreateTableParser.fromDDL(ddl);

        Assert.assertEquals(parser.getName(), expectedTable, "Table name should match");
        Assert.assertEquals(parser.getColumns(), expectedColumns, "Column names should match");
    }

    @Test
    public void testColumnOrder() {
        HackyCreateTableParser parser = HackyCreateTableParser.fromDDL("CREATE TABLE test (id integer primary key, name text, age integer, address text)");

        Assert.assertEquals(parser.ordinalForColumn("id"), 0);
        Assert.assertEquals(parser.ordinalForColumn("name"), 1);
        Assert.assertEquals(parser.ordinalForColumn("age"), 2);
        Assert.assertEquals(parser.ordinalForColumn("address"), 3);
        Assert.assertEquals(parser.ordinalForColumn("missing_column"), -1, "Should return -1 for non-existent columns");
    }

    @DataProvider(name = "invalidDDLStatements")
    public Object[][] invalidDDLStatements() {
        return new Object[][]{
                {"CREATE TABLE ()"},   // No table name
                {"CREATE TABLE test"}, // No column definition
                {"create table missing_brackets id integer primary key, name text"}, // Missing parentheses
                {"CREATE banana (id integer primary key, name text)"}, // Wrong keyword
                {"CREATE TABLE 123 (id integer primary key, name text)"}, // Invalid table name
        };
    }

    @Test(dataProvider = "invalidDDLStatements", expectedExceptions = IllegalArgumentException.class)
    public void testInvalidDDLParsing(String invalidDDL) {
        HackyCreateTableParser.fromDDL(invalidDDL);
    }

    @Test
    public void testCaseInsensitiveParsing() {
        HackyCreateTableParser parser = HackyCreateTableParser.fromDDL("cReAtE tAbLe TeSt (iD iNtEgEr pRiMaRy kEy, nAmE tExT)");
        Assert.assertEquals(parser.getName(), "TeSt", "Table name should preserve case");
        Assert.assertTrue(parser.getColumns().contains("iD"));
        Assert.assertTrue(parser.getColumns().contains("nAmE"));
    }

    // ✅ New Tests for Rowid Alias Detection
    @DataProvider(name = "rowIdAliasTests")
    public Object[][] rowIdAliasTests() {
        return new Object[][]{
                {"CREATE TABLE test (id integer primary key, name text)", "id", true},
                {"CREATE TABLE test (name text, id integer primary key)", "id", true},
                {"CREATE TABLE test (uid integer primary key, name text)", "uid", true},
                {"CREATE TABLE test (id integer primary key desc, name text)", "id", false},
                {"CREATE TABLE test (id integer unique, name text)", "id", false},
                {"CREATE TABLE test (id integer, name text)", "id", false},
        };
    }

    @Test(dataProvider = "rowIdAliasTests")
    public void testRowIdAliasDetection(String ddl, String columnName, boolean expectedIsRowId) {
        HackyCreateTableParser parser = HackyCreateTableParser.fromDDL(ddl);
        Assert.assertEquals(parser.isRowIdAlias(columnName), expectedIsRowId,
                "Column '" + columnName + "' should" + (expectedIsRowId ? "" : " not") + " be aliased to rowid.");
    }
}