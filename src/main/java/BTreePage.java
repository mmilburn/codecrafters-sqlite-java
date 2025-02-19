import java.util.List;

public interface BTreePage {
    PageHeader getPageHeader();

    PageType getPageType();

    short[] getCellPointers();

    List<Cell> getCells();

    int getCellsCount();
}
