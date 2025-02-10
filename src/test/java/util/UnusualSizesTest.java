package util;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static util.UnusualSizes.from24BitValue;
import static util.UnusualSizes.from48BitValue;

public class UnusualSizesTest {
    @Test
    public void test24BitConversion() {
        // Test minimum value
        byte[] minBytes = {(byte) 0x80, 0x00, 0x00};
        assertEquals(from24BitValue(minBytes), -8_388_608);

        // Test maximum value
        byte[] maxBytes = {0x7F, (byte) 0xFF, (byte) 0xFF};
        assertEquals(from24BitValue(maxBytes), 8_388_607);

        // Test -1
        byte[] negOneBytes = {(byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
        assertEquals(from24BitValue(negOneBytes), -1);
    }

    @Test
    public void test48BitConversion() {
        // Maximum positive value
        byte[] maxPositive = {0x7F, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
        assertEquals(from48BitValue(maxPositive), 140737488355327L);

        // Minimum negative value
        byte[] minNegative = {(byte) 0x80, 0x00, 0x00, 0x00, 0x00, 0x00};
        assertEquals(from48BitValue(minNegative), -140737488355328L);

        // -1 (all ones)
        byte[] allOnes = {(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
        assertEquals(from48BitValue(allOnes), -1L);
    }
}
