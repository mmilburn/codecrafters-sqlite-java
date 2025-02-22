package db.search;

import config.ConfigContext;
import db.btree.BTreePage;
import db.btree.cell.Cell;

public abstract class BTreeSearch<T, R> {
    protected final ConfigContext configContext;

    protected BTreeSearch(ConfigContext configContext) {
        this.configContext = configContext;
    }

    public R search(BTreePage rootPage, SearchKey<T> searchKey) {
        BTreePage currentPage = rootPage;

        while (true) {
            if (isLeafPage(currentPage)) {
                return searchLeafPage(currentPage, searchKey);
            }

            int nextPage = findNextPage(currentPage, searchKey);
            if (nextPage == -1 || !configContext.hasPage(nextPage)) {
                return getEmptyResult();
            }
            currentPage = configContext.getPage(nextPage);
        }
    }

    protected abstract boolean isLeafPage(BTreePage page);

    protected abstract R searchLeafPage(BTreePage page, SearchKey<T> searchKey);

    protected abstract R getEmptyResult();

    protected abstract T getKeyFromCell(Cell cell);

    protected int findNextPage(BTreePage page, SearchKey<T> searchKey) {
        int count = page.getCellsCount();
        if (count == 0) {
            return -1;
        }

        int left = 0, right = count - 1;
        int nextPage = -1;

        while (left <= right) {
            int mid = left + (right - left) / 2;
            Cell midCell = page.getCell(mid);
            T midKey = getKeyFromCell(midCell);

            int cmp = searchKey.compareTo(midKey);
            if (cmp == 0) {
                nextPage = getChildPointer(midCell);
                break;
            } else if (cmp < 0) {
                nextPage = getChildPointer(midCell);
                right = mid - 1;
            } else {
                left = mid + 1;
            }
        }

        if (left >= count) {
            return page.getPageHeader().getRightmostPointer();
        }

        return nextPage;
    }

    protected abstract int getChildPointer(Cell cell);
}