package db.btree.cell;

import db.btree.PageType;

import java.nio.ByteBuffer;

public class CellFactory {
    public static Cell fromByteBuffer(ByteBuffer data, PageType pageType) {
        return switch (pageType) {
            case INDEX_INTERIOR -> IndexInteriorCell.fromByteBuffer(data);
            case INDEX_LEAF -> IndexLeafCell.fromByteBuffer(data);
            case TABLE_INTERIOR -> TableInteriorCell.fromByteBuffer(data);
            case TABLE_LEAF -> TableLeafCell.fromByteBuffer(data);
            default -> {
                throw new UnsupportedOperationException("db.btree.PageType " + pageType + " does not support cells.");
            }
        };
    }
}

