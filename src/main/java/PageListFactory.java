import java.nio.ByteBuffer;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class PageListFactory {
    public static List<BTreePage> fromByteBuffer(ByteBuffer data) {
        InitialPage firstPage = InitialPage.fromByteBuffer(data);
        int pageSize = firstPage.getSqLiteHeader().getPageSize();
        int totalPages = firstPage.getSqLiteHeader().getNumberOfPages();
        return Stream.concat(
                Stream.of(firstPage),
                IntStream.range(0, totalPages - 1).mapToObj(i -> RegularPage.sizedPageFromByteBuffer(data, pageSize))
        ).toList();
    }
}
