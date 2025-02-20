package db.btree;

import util.Memoization;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class PageListFactory {
    public static Map<Integer, Supplier<BTreePage>> lazyPageMap(ByteBuffer data) {
        Map<Integer, Supplier<BTreePage>> pageSuppliers = new HashMap<>();
        FirstPage firstPage = FirstPage.fromByteBuffer(data);
        pageSuppliers.put(1, () -> firstPage);
        int pageSize = firstPage.getSqLiteHeader().getPageSize();
        int totalPages = firstPage.getSqLiteHeader().getNumberOfPages();
        for (int i = 2; i <= totalPages; i++) {
            ByteBuffer duplicated = data.duplicate();
            duplicated.position(pageSize * (i - 1));
            duplicated.limit(duplicated.position() + pageSize);
            //Slice here since cell pointers are relative to the current page!
            pageSuppliers.put(i, Memoization.memoize(() -> RegularPage.pageFromByteBuffer(duplicated.slice())));
        }
        return pageSuppliers;
    }
}
