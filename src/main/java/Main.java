import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Missing <database path> and <command>");
            return;
        }

        String databaseFilePath = args[0];
        String command = args[1];
        List<BTreePage> pages = PageListFactory.fromByteBuffer(getByteBufferFromFile(databaseFilePath));
        InitialPage firstPage = ((InitialPage) pages.getFirst());
        SQLiteHeader header = firstPage.getSqLiteHeader();

        switch (command) {
            case ".dbinfo" -> {
                System.out.println("database page size: " + header.getPageSize());
                //Use cells count for now...
                System.out.println("number of tables: " + firstPage.getPageHeader().getCellsCount());
            }
            default -> System.err.println("Missing or invalid command passed: " + command);
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
