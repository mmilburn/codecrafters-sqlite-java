package util;

public class HexDump {
    public static void printHexDump(byte[] data) {
        int bytesPerRow = 16; // Number of bytes per row

        System.out.println("Idx  | Hex                                             | ASCII");
        System.out.println("-----+-------------------------------------------------+-----------------");

        for (int row = 0; row < data.length; row += bytesPerRow) {
            // Print the row index in hex
            System.out.printf("%04x | ", row);

            // Print hex representation of each byte in the row
            for (int i = 0; i < bytesPerRow; i++) {
                int index = row + i;
                if (index < data.length) {
                    System.out.printf("%02x ", data[index]);
                } else {
                    System.out.print("   "); // Fill empty space for incomplete rows
                }
            }

            System.out.print("| "); // Separator for ASCII output

            // Print ASCII characters for each byte in the row
            for (int i = 0; i < bytesPerRow; i++) {
                int index = row + i;
                if (index < data.length) {
                    byte b = data[index];
                    // Print printable ASCII characters, use '.' for non-printables
                    System.out.print((b >= 32 && b <= 126) ? (char) b : '.');
                }
            }

            System.out.println(); // Move to the next row
        }
        System.out.println();
    }
}

