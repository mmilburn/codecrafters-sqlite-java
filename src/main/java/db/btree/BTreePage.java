package db.btree;

import config.PageHeader;
import db.btree.cell.Cell;

import java.util.stream.Stream;

public interface BTreePage {
    PageHeader getPageHeader();

    PageType getPageType();

    short[] getCellPointers();

    Cell getCell(Integer index);

    int getCellsCount();

    Stream<Cell> getCellStream();
}
