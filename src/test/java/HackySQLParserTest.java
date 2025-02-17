import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import java.util.List;
import java.util.Map;

public class HackySQLParserTest {

    @DataProvider(name = "validSQLQueries")
    public Object[][] validSQLQueries() {
        return new Object[][]{
                {"SELECT COUNT(*) FROM apples", List.of("COUNT(*)"), "apples", Map.of()},
                {"SELECT name FROM apples", List.of("name"), "apples", Map.of()},
                {"SELECT name, color FROM apples", List.of("name", "color"), "apples", Map.of()},
                {"SELECT name, color FROM apples WHERE color = 'Yellow'",
                        List.of("name", "color"), "apples", Map.of("color", "'Yellow'")},
                {"SELECT id, name FROM superheroes WHERE eye_color = 'Pink Eyes'",
                        List.of("id", "name"), "superheroes", Map.of("eye_color", "'Pink Eyes'")},
                {"SELECT age, city FROM users WHERE age > 30 AND city = 'New York'",
                        List.of("age", "city"), "users", Map.of("age", "30", "city", "'New York'")}
        };
    }

    @Test(dataProvider = "validSQLQueries")
    public void testValidSQLQueries(String query, List<String> expectedColumns, String expectedTable, Map<String, String> expectedConditions) {
        HackySQLParser parser = HackySQLParser.fromSQLQuery(query);

        Assert.assertEquals(parser.getColumns(), expectedColumns, "Columns do not match.");
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
                //TODO: fix this!
                //{"SELECT age city FROM users WHERE age > 30 AND city = 'New York'"} // Missing comma between columns
        };
    }

    @Test(dataProvider = "invalidSQLQueries", expectedExceptions = IllegalArgumentException.class)
    public void testInvalidSQLQueries(String query) {
        HackySQLParser.fromSQLQuery(query); // Should throw IllegalArgumentException
    }
}
