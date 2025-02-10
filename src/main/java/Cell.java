public sealed interface Cell permits IndexInteriorCell, IndexLeafCell, TableInteriorCell, TableLeafCell {
    CellType cellType();
}
