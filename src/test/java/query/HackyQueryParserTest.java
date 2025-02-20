package query;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;

public class HackyQueryParserTest {

    @DataProvider(name = "validSQLQueries")
    public Object[][] validSQLQueries() {
        return new Object[][]{
                {"SELECT COUNT(*) FROM apples", List.of("COUNT(*)"), "apples", List.of()},
                {"SELECT name FROM apples", List.of("name"), "apples", List.of()},
                {"SELECT name, color FROM apples", List.of("name", "color"), "apples", List.of()},
                {"SELECT name, color FROM apples WHERE color = 'Yellow'",
                        List.of("name", "color"), "apples", List.of(new Condition("color", OperatorType.EQUALS, "Yellow"))},
                {"SELECT id, name FROM superheroes WHERE eye_color = 'Pink Eyes'",
                        List.of("id", "name"), "superheroes",
                        List.of(new Condition("eye_color", OperatorType.EQUALS, "Pink Eyes"))},
                {"SELECT age, city FROM users WHERE age > 30 AND city = 'New York'",
                        List.of("age", "city"), "users",
                        List.of(new Condition("age", OperatorType.GREATER_THAN, "30"), new Condition("city", OperatorType.EQUALS, "New York"))}
        };
    }

    @Test(dataProvider = "validSQLQueries")
    public void testValidSQLQueries(String query, List<String> expectedColumns, String expectedTable, List<Condition> expectedConditions) {
        HackyQueryParser parser = HackyQueryParser.fromSQLQuery(query);

        Assert.assertEquals(parser.getColsOrFuncs(), expectedColumns, "Columns do not match.");
        Assert.assertEquals(parser.getTable(), expectedTable, "Table name does not match.");
        Assert.assertEquals(parser.getConditions(), expectedConditions, "Conditions do not match.");
    }

    @DataProvider(name = "invalidSQLQueries")
    public Object[][] invalidSQLQueries() {
        return new Object[][]{
                {"SELECT FROM apples"},  // Missing column names
                {"SELECT name apples"},  // Missing FROM
                {"SELECT name, color"},  // Missing FROM clause
                {"DELETE FROM apples"},  // Invalid SQL command
                {"SELECT name, color WHERE color = 'Yellow'"},  // Missing FROM
                {"SELECT age city FROM users WHERE age > 30 AND city = 'New York'"} // Missing comma between columns
        };
    }

    @Test(dataProvider = "invalidSQLQueries", expectedExceptions = IllegalArgumentException.class)
    public void testInvalidSQLQueries(String query) {
        HackyQueryParser.fromSQLQuery(query); // Should throw IllegalArgumentException
    }
}
