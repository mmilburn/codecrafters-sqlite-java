package db.btree;

import db.btree.cell.CellType;

import java.nio.ByteBuffer;

public enum PageType {
    // Core B-tree pages
    TABLE_INTERIOR((byte) 0x05, "Table interior page", CellType.TABLE_INTERIOR),
    TABLE_LEAF((byte) 0x0D, "Table leaf page", CellType.TABLE_LEAF),
    INDEX_INTERIOR((byte) 0x02, "Index interior page", CellType.TABLE_INTERIOR),
    INDEX_LEAF((byte) 0x0A, "Index leaf page", CellType.INDEX_LEAF),

    // Special pages
    FREELIST_LEAF((byte) 0x06, "Freelist leaf page", CellType.NONE),
    FREELIST_TRUNK((byte) 0x07, "Freelist trunk page", CellType.NONE),
    OVERFLOW((byte) 0x03, "Overflow page", CellType.NONE),
    POINTER_MAP((byte) 0x04, "Pointer map page", CellType.NONE),
    LOCK_BYTE((byte) 0x01, "Lock-byte page (page 1)", CellType.NONE),
    FREE((byte) 0x00, "Free page", CellType.NONE),
    UNKNOWN((byte) -1, "Unknown page type", CellType.NONE);

    private final byte code;
    private final String description;
    private final CellType cellType;

    PageType(byte code, String description, CellType cellType) {
        this.code = code;
        this.description = description;
        this.cellType = cellType;
    }

    public static PageType fromByteBuffer(ByteBuffer data) {
        byte b = data.get();
        for (PageType type : values()) {
            if (type.code == b) {
                return type;
            }
        }
        return UNKNOWN;
    }

    public boolean isBTreePage() {
        return switch (this) {
            case TABLE_INTERIOR, TABLE_LEAF, INDEX_INTERIOR, INDEX_LEAF -> true;
            default -> false;
        };
    }

    public boolean isLeaf() {
        return this == TABLE_LEAF || this == INDEX_LEAF;
    }

    public boolean isInterior() {
        return this == TABLE_INTERIOR || this == INDEX_INTERIOR;
    }

    public byte getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public CellType getCellType() {
        return cellType;
    }
}
