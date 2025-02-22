package db.search;

import config.ConfigContext;
import db.btree.BTreePage;
import db.btree.PageType;
import db.btree.cell.Cell;
import db.btree.cell.IndexInteriorCell;
import db.btree.cell.IndexLeafCell;
import db.data.Column;
import db.data.Record;

import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.PriorityQueue;

public class IndexSearch {
    public static PriorityQueue<Long> searchFromRootPage(ConfigContext configContext, BTreePage rootPage, Column searchKey) {
        BTreePage currentPage = rootPage;
        Charset charset = configContext.getCharset();
        while (true) {
            if (currentPage.getPageType() == PageType.INDEX_LEAF) {
                return searchLeafPage(configContext, currentPage, searchKey);
            }

            int count = currentPage.getCellsCount();
            if (count == 0) {
                return new PriorityQueue<>();
            }
            int left = 0, right = count - 1;
            int nextPage = -1;

            while (left <= right) {
                int mid = left + (right - left) / 2;
                Cell midCell = currentPage.getCell(mid);

                if (midCell instanceof IndexInteriorCell interiorCell) {
                    Record record = interiorCell.initialPayload();
                    Column midKey = record.getColumnForIndex(0);
                    int cmp = compareKeys(charset, searchKey, midKey);
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
                System.err.println("Attempting to use rightmost pointer: " + currentPage.getPageHeader().getRightmostPointer());
                nextPage = currentPage.getPageHeader().getRightmostPointer();
            }

            if (nextPage == -1 || !configContext.hasPage(nextPage)) {
                return new PriorityQueue<>();
            }
            currentPage = configContext.getPage(nextPage);
        }
    }

    private static PriorityQueue<Long> searchLeafPage(ConfigContext configContext, BTreePage page, Column searchKey) {
        Charset charset = configContext.getCharset();
        int count = page.getCellsCount();
        PriorityQueue<Long> matchingRows = new PriorityQueue<>();
        HashSet<Long> seenRows = new HashSet<>();

        if (count == 0) {
            return matchingRows;
        }

        int left = 0, right = page.getCellsCount() - 1;
        int foundIndex = -1;

        while (left <= right) {
            int mid = left + (right - left) / 2;
            Cell midCell = page.getCell(mid);
            if (midCell instanceof IndexLeafCell leafCell) {
                Record record = leafCell.initialPayload();
                Column midKey = record.getColumnForIndex(0);

                int cmp = compareKeys(charset, searchKey, midKey);
                if (cmp == 0) {
                    foundIndex = mid;
                    break;
                } else if (cmp < 0) {
                    right = mid - 1;
                } else {
                    left = mid + 1;
                }
            }
        }

        if (foundIndex == -1) {
            return matchingRows;
        }

        for (int i = foundIndex; i >= 0; i--) {
            Cell cell = page.getCell(i);
            if (cell instanceof IndexLeafCell leafCell) {
                Record record = leafCell.initialPayload();
                Column key = record.getColumnForIndex(0);
                if (compareKeys(charset, searchKey, key) == 0) {
                    long rowId = record.getRowID();
                    if (seenRows.add(rowId)) {
                        matchingRows.add(rowId);
                    }
                } else {
                    break;
                }
            }
        }

        for (int i = foundIndex + 1; i < count; i++) {
            Cell cell = page.getCell(i);
            if (cell instanceof IndexLeafCell leafCell) {
                Record record = leafCell.initialPayload();
                int index = record.getNumberOfColumns() - 1;
                Column key = record.getColumnForIndex(0);
                if (compareKeys(charset, searchKey, key) == 0) {
                    long rowId = record.getRowID();
                    if (seenRows.add(rowId)) {
                        matchingRows.add(rowId);
                    }
                } else {
                    break;
                }
            }
        }
        return matchingRows;
    }

    private static int compareKeys(Charset charset, Column key1, Column key2) {
        return key1.getAsString(charset).compareTo(key2.getAsString(charset));
    }
}
