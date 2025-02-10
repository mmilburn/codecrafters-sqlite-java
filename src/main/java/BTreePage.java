public interface BTreePage {
    PageHeader getPageHeader();
    short[] getCellPointers();
}
