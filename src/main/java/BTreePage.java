import java.util.List;

public interface BTreePage {
    PageHeader getPageHeader();

    short[] getCellPointers();

    List<Cell> getCells();

    int getCellsCount();
}
