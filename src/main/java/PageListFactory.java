import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class PageListFactory {
    public static Map<Integer, Supplier<BTreePage>> lazyPageMap(ByteBuffer data) {
        Map<Integer, Supplier<BTreePage>> pageSuppliers = new HashMap<>();
        InitialPage firstPage = InitialPage.fromByteBuffer(data);
        pageSuppliers.put(1, () -> firstPage);
        int pageSize = firstPage.getSqLiteHeader().getPageSize();
        int totalPages = firstPage.getSqLiteHeader().getNumberOfPages();
        for (int i = 2; i <= totalPages; i++) {
            pageSuppliers.put(i, () -> RegularPage.sizedPageFromByteBuffer(data.duplicate().limit(pageSize), pageSize));
        }
        return pageSuppliers;
    }
}
