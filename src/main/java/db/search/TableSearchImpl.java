package db.search;

import config.ConfigContext;
import db.btree.BTreePage;
import db.btree.PageType;
import db.btree.cell.Cell;
import db.btree.cell.TableInteriorCell;
import db.btree.cell.TableLeafCell;

public class TableSearchImpl extends BTreeSearch<Long, TableLeafCell> {
    public TableSearchImpl(ConfigContext configContext) {
        super(configContext);
    }

    @Override
    protected boolean isLeafPage(BTreePage page) {
        return page.getPageType() == PageType.TABLE_LEAF;
    }

    @Override
    protected TableLeafCell searchLeafPage(BTreePage page, SearchKey<Long> searchKey) {
        int count = page.getCellsCount();
        if (count == 0) {
            return null;
        }

        int left = 0, right = count - 1;
        while (left <= right) {
            int mid = left + (right - left) / 2;
            Cell midCell = page.getCell(mid);
            if (midCell instanceof TableLeafCell leafCell) {
                int cmp = searchKey.compareTo(leafCell.rowID().getValue());
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

    @Override
    protected TableLeafCell getEmptyResult() {
        return null;
    }

    @Override
    protected Long getKeyFromCell(Cell cell) {
        if (cell instanceof TableInteriorCell interiorCell) {
            return interiorCell.rowID().getValue();
        }
        throw new IllegalArgumentException("Invalid cell type");
    }

    @Override
    protected int getChildPointer(Cell cell) {
        if (cell instanceof TableInteriorCell interiorCell) {
            return interiorCell.leftChildPointer();
        }
        throw new IllegalArgumentException("Invalid cell type");
    }
}