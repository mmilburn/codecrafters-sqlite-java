package db.btree.cell;

public enum CellType {
    TABLE_INTERIOR,
    TABLE_LEAF,
    INDEX_INTERIOR,
    INDEX_LEAF,
    // For page types that aren’t B-tree pages, you might use NONE.
    NONE
}