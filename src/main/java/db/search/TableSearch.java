package db.search;

import config.ConfigContext;
import db.btree.BTreePage;
import db.btree.PageType;
import db.btree.cell.Cell;
import db.btree.cell.TableInteriorCell;
import db.btree.cell.TableLeafCell;

public class TableSearch {
    public static TableLeafCell searchFromRootPage(ConfigContext configContext, BTreePage rootPage, long rowId) {
        BTreePage currentPage = rootPage;

        while (true) {
            if (currentPage.getPageType() == PageType.TABLE_LEAF) {
                return searchLeafPage(currentPage, rowId);
            }

            int count = currentPage.getCellsCount();
            if (count == 0) {
                return null;
            }
            int left = 0, right = count - 1;
            int nextPage = -1;

            while (left <= right) {
                int mid = left + (right - left) / 2;
                Cell midCell = currentPage.getCell(mid);

                if (midCell instanceof TableInteriorCell interiorCell) {
                    long midRowId = interiorCell.rowID().getValue();
                    int cmp = Long.compare(rowId, midRowId);

                    if (cmp == 0) {
                        nextPage = interiorCell.leftChildPointer();
                        break;
                    } else if (cmp < 0) {
                        nextPage = interiorCell.leftChildPointer();
                        right = mid - 1;
                    } else {
                        left = mid + 1;
                    }
                }
            }

            if (left >= count) {
                nextPage = currentPage.getPageHeader().getRightmostPointer();
            }
            if (nextPage == -1 || !configContext.hasPage(nextPage)) {
                return null;
            }
            currentPage = configContext.getPage(nextPage);
        }
    }

    private static TableLeafCell searchLeafPage(BTreePage page, long rowId) {
        int count = page.getCellsCount();

        if (count == 0) {
            return null;
        }

        int left = 0, right = count - 1;

        while (left <= right) {
            int mid = left + (right - left) / 2;
            Cell midCell = page.getCell(mid);

            if (midCell instanceof TableLeafCell leafCell) {
                long midRowId = leafCell.rowID().getValue();
                int cmp = Long.compare(rowId, midRowId);

                if (cmp == 0) {
                    return leafCell;
                } else if (cmp < 0) {
                    right = mid - 1;
                } else {
                    left = mid + 1;
                }

            }
        }
        return null;
    }
}
