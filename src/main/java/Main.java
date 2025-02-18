import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class Main {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Missing <database path> and <command>");
            return;
        }

        //Queries we should support:
        //"SELECT COUNT(*) FROM apples"
        //"SELECT name FROM apples"
        //"SELECT name, color FROM apples"
        //"SELECT name, color FROM apples WHERE color = 'Yellow'"
        //"SELECT id, name FROM superheroes WHERE eye_color = 'Pink Eyes'"
        //Output from multiple columns should be pipe delimited.
        String databaseFilePath = args[0];
        String command = args[1].toLowerCase();
        Map<Integer, Supplier<BTreePage>> lazyPages = PageListFactory.lazyPageMap(getByteBufferFromFile(databaseFilePath));
        ConfigContext configContext = ((InitialPage) lazyPages.get(1).get()).getConfigContextBuilder().build();

        if (command.toLowerCase().startsWith("select ")) {
            HackyQueryParser parser = HackyQueryParser.fromSQLQuery(command);
            HackyDDLParser ddlParser = HackyDDLParser.fromCreateTable(configContext.getSQLForTable(parser.getTable()));
            BTreePage page = lazyPages.get(configContext.getRootPageForTable(parser.getTable())).get();
            if (parser.hasCountOperation()) {
                System.out.println(page.getCellsCount());
            } else if (!parser.getColsOrFuncs().isEmpty() && !parser.hasConditions()) {
                page.getCells().stream()
                        .filter(cell -> cell.cellType() == CellType.TABLE_LEAF)
                        .map(cell -> (TableLeafCell) cell)
                        .map(leafCell -> {
                            List<String> vals = new ArrayList<>();
                            Record rec = leafCell.initialPayload();
                            for (String colName : parser.getColsOrFuncs()) {
                                colName = colName.toLowerCase();
                                int index = ddlParser.indexForColumn(colName);
                                ColumnType type = ddlParser.getColumnType(colName);
                                Column col = rec.getColumnForIndex(index);
                                vals.add(String.valueOf(col.getValueAs(type, configContext.getCharset())));
                            }
                            return String.join("|", vals);
                        }).forEach(System.out::println);

            } else {
                System.err.println("Support for query: " + command + " not implemented!");
            }

        } else {
            switch (command) {
                case ".dbinfo" -> {
                    System.out.println("database page size: " + configContext.getPageSize());
                    System.out.println("number of tables: " + configContext.getTableCount());
                }
                case ".tables" -> System.out.print(String.join(" ", configContext.getTableNames()));
                default -> System.err.println("Missing or invalid command passed: " + command);
            }
        }
    }

    private static ByteBuffer getByteBufferFromFile(String filePath) {
        Path path = Path.of(filePath);
        ByteBuffer buffer = ByteBuffer.wrap(new byte[]{0});
        try (FileChannel channel = FileChannel.open(path, StandardOpenOption.READ)) {
            buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
        } catch (IOException ioNo) {
            System.err.println("Error reading file: " + ioNo.getMessage());
        }
        return buffer;
    }
}
