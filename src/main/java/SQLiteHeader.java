import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class SQLiteHeader {
    private final String header;
    private final short pageSize;
    private final byte writeVersion;
    private final byte readVersion;
    private final byte reservedSpace;
    private final byte maxEmbeddedPayloadFraction;
    private final byte minEmbeddedPayloadFraction;
    private final byte leafPayloadFraction;
    private final int fileChangeCounter;
    private final int numberOfPages;
    private final int firstFreelistPage;
    private final int totalFreelistPages;
    private final int schemaCookie;
    private final int schemaFormat;
    private final int defaultPageCacheSize;
    private final int pageNumOfLargestRootBtree;
    private final int textEncoding;
    private final int userVersion;
    private final int incrementalVacuumMode;
    private final int applicationID;
    private final byte[] reserved;
    private final int versionValidFor;
    private final int sqliteVersionNumber;

    private SQLiteHeader(String header, short pageSize, byte writeVersion, byte readVersion, byte reservedSpace, byte maxEmbeddedPayloadFraction, byte minEmbeddedPayloadFraction, byte leafPayloadFraction, int fileChangeCounter, int numberOfPages, int firstFreelistPage, int totalFreelistPages, int schemaCookie, int schemaFormat, int defaultPageCacheSize, int pageNumOfLargestRootBtree, int textEncoding, int userVersion, int incrementalVacuumMode, int applicationID, byte[] reserved, int versionValidFor, int sqliteVersionNumber) {
        this.header = header;
        this.pageSize = pageSize;
        this.writeVersion = writeVersion;
        this.readVersion = readVersion;
        this.reservedSpace = reservedSpace;
        this.maxEmbeddedPayloadFraction = maxEmbeddedPayloadFraction;
        this.minEmbeddedPayloadFraction = minEmbeddedPayloadFraction;
        this.leafPayloadFraction = leafPayloadFraction;
        this.fileChangeCounter = fileChangeCounter;
        this.numberOfPages = numberOfPages;
        this.firstFreelistPage = firstFreelistPage;
        this.totalFreelistPages = totalFreelistPages;
        this.schemaCookie = schemaCookie;
        this.schemaFormat = schemaFormat;
        this.defaultPageCacheSize = defaultPageCacheSize;
        this.pageNumOfLargestRootBtree = pageNumOfLargestRootBtree;
        this.textEncoding = textEncoding;
        this.userVersion = userVersion;
        this.incrementalVacuumMode = incrementalVacuumMode;
        this.applicationID = applicationID;
        this.reserved = reserved;
        this.versionValidFor = versionValidFor;
        this.sqliteVersionNumber = sqliteVersionNumber;
    }


    public static SQLiteHeader fromByteBuffer(ByteBuffer buffer) {
        String header = readHeaderString(buffer);
        short pageSize = buffer.getShort();
        byte writeVersion = buffer.get();
        byte readVersion = buffer.get();
        byte reservedSpace = buffer.get();
        byte maxEmbeddedPayloadFraction = buffer.get();
        byte minEmbeddedPayloadFraction = buffer.get();
        byte leafPayloadFraction = buffer.get();
        int fileChangeCounter = buffer.getInt();
        int numberOfPages = buffer.getInt();
        int firstFreelistPage = buffer.getInt();
        int totalFreelistPages = buffer.getInt();
        int schemaCookie = buffer.getInt();
        int schemaFormat = buffer.getInt();
        int defaultPageCacheSize = buffer.getInt();
        int pageNumOfLargestRootBtree = buffer.getInt();
        int textEncoding = buffer.getInt();
        int userVersion = buffer.getInt();
        int incrementalVacuumMode = buffer.getInt();
        int applicationID = buffer.getInt();
        byte[] reserved = readReservedBytes(buffer);
        int versionValidFor = buffer.getInt();
        int sqliteVersionNumber = buffer.getInt();

        return new SQLiteHeader(header, pageSize, writeVersion, readVersion, reservedSpace,
                maxEmbeddedPayloadFraction, minEmbeddedPayloadFraction, leafPayloadFraction,
                fileChangeCounter, numberOfPages, firstFreelistPage, totalFreelistPages,
                schemaCookie, schemaFormat, defaultPageCacheSize, pageNumOfLargestRootBtree,
                textEncoding, userVersion, incrementalVacuumMode, applicationID, reserved,
                versionValidFor, sqliteVersionNumber);
    }

    private static String readHeaderString(ByteBuffer buffer) {
        byte[] bytes = new byte[16];
        buffer.get(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private static byte[] readReservedBytes(ByteBuffer buffer) {
        byte[] bytes = new byte[20];
        buffer.get(bytes);
        return bytes;
    }

    public String getHeader() {
        return header;
    }

    public int getPageSize() {
        if (pageSize == 1) {
            return 65536;
        }
        return Short.toUnsignedInt(pageSize);
    }

    public byte getWriteVersion() {
        return writeVersion;
    }

    public byte getReadVersion() {
        return readVersion;
    }

    public byte getReservedSpace() {
        return reservedSpace;
    }

    public byte getMaxEmbeddedPayloadFraction() {
        return maxEmbeddedPayloadFraction;
    }

    public byte getMinEmbeddedPayloadFraction() {
        return minEmbeddedPayloadFraction;
    }

    public byte getLeafPayloadFraction() {
        return leafPayloadFraction;
    }

    public int getFileChangeCounter() {
        return fileChangeCounter;
    }

    public int getNumberOfPages() {
        return numberOfPages;
    }

    public int getFirstFreelistPage() {
        return firstFreelistPage;
    }

    public int getTotalFreelistPages() {
        return totalFreelistPages;
    }

    public int getSchemaCookie() {
        return schemaCookie;
    }

    public int getSchemaFormat() {
        return schemaFormat;
    }

    public int getDefaultPageCacheSize() {
        return defaultPageCacheSize;
    }

    public int getPageNumOfLargestRootBtree() {
        return pageNumOfLargestRootBtree;
    }

    public int getTextEncoding() {
        return textEncoding;
    }

    public int getUserVersion() {
        return userVersion;
    }

    public int getIncrementalVacuumMode() {
        return incrementalVacuumMode;
    }

    public int getApplicationID() {
        return applicationID;
    }

    public byte[] getReserved() {
        return reserved;
    }

    public int getVersionValidFor() {
        return versionValidFor;
    }

    public int getSqliteVersionNumber() {
        return sqliteVersionNumber;
    }
}
