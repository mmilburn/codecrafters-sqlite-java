package db.search;

import config.ConfigContext;
import db.btree.BTreePage;
import db.btree.PageType;
import db.btree.cell.Cell;
import db.btree.cell.IndexInteriorCell;
import db.btree.cell.IndexLeafCell;
import db.data.Column;
import db.data.Record;

import java.util.HashSet;
import java.util.PriorityQueue;

public class IndexSearchImpl extends BTreeSearch<Column, PriorityQueue<Long>> {
    public IndexSearchImpl(ConfigContext configContext) {
        super(configContext);
    }

    @Override
    protected boolean isLeafPage(BTreePage page) {
        return page.getPageType() == PageType.INDEX_LEAF;
    }

    @Override
    protected PriorityQueue<Long> searchLeafPage(BTreePage page, SearchKey<Column> searchKey) {
        int count = page.getCellsCount();
        PriorityQueue<Long> matchingRows = new PriorityQueue<>();
        HashSet<Long> seenRows = new HashSet<>();

        if (count == 0) {
            return matchingRows;
        }

        // Find first matching index
        int foundIndex = findFirstMatch(page, count, searchKey);
        if (foundIndex == -1) {
            return matchingRows;
        }

        // Collect matches before the found index
        collectMatchesBefore(page, foundIndex, searchKey, matchingRows, seenRows);
        // Collect matches after the found index
        collectMatchesAfter(page, foundIndex, count, searchKey, matchingRows, seenRows);

        return matchingRows;
    }

    private int findFirstMatch(BTreePage page, int count, SearchKey<Column> searchKey) {
        int left = 0, right = count - 1;

        while (left <= right) {
            int mid = left + (right - left) / 2;
            Cell midCell = page.getCell(mid);
            if (midCell instanceof IndexLeafCell leafCell) {
                Column midKey = leafCell.initialPayload().getColumnForIndex(0);
                int cmp = searchKey.compareTo(midKey);
                if (cmp == 0) {
                    return mid;
                } else if (cmp < 0) {
                    right = mid - 1;
                } else {
                    left = mid + 1;
                }
            }
        }
        return -1;
    }

    private void collectMatchesBefore(BTreePage page, int startIndex, SearchKey<Column> searchKey,
                                      PriorityQueue<Long> matchingRows, HashSet<Long> seenRows) {
        for (int i = startIndex; i >= 0; i--) {
            if (!collectMatch(page.getCell(i), searchKey, matchingRows, seenRows)) {
                break;
            }
        }
    }

    private void collectMatchesAfter(BTreePage page, int startIndex, int count, SearchKey<Column> searchKey,
                                     PriorityQueue<Long> matchingRows, HashSet<Long> seenRows) {
        for (int i = startIndex + 1; i < count; i++) {
            if (!collectMatch(page.getCell(i), searchKey, matchingRows, seenRows)) {
                break;
            }
        }
    }

    private boolean collectMatch(Cell cell, SearchKey<Column> searchKey,
                                 PriorityQueue<Long> matchingRows, HashSet<Long> seenRows) {
        if (cell instanceof IndexLeafCell leafCell) {
            Record record = leafCell.initialPayload();
            Column key = record.getColumnForIndex(0);
            if (searchKey.compareTo(key) == 0) {
                long rowId = record.getRowID();
                if (seenRows.add(rowId)) {
                    matchingRows.add(rowId);
                }
                return true;
            }
        }
        return false;
    }

    @Override
    protected PriorityQueue<Long> getEmptyResult() {
        return new PriorityQueue<>();
    }

    @Override
    protected Column getKeyFromCell(Cell cell) {
        if (cell instanceof IndexInteriorCell interiorCell) {
            return interiorCell.initialPayload().getColumnForIndex(0);
        }
        throw new IllegalArgumentException("Invalid cell type");
    }

    @Override
    protected int getChildPointer(Cell cell) {
        if (cell instanceof IndexInteriorCell interiorCell) {
            return interiorCell.leftChildPointer();
        }
        throw new IllegalArgumentException("Invalid cell type");
    }
}