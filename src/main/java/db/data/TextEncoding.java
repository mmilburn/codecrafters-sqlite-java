package db.data;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public enum TextEncoding {
    UTF_8(1, StandardCharsets.UTF_8),
    UTF_16LE(2, StandardCharsets.UTF_16LE),
    UTF_16BE(3, StandardCharsets.UTF_16BE),
    UNKNOWN(-1, StandardCharsets.UTF_8);

    private final int code;
    private final Charset charset;

    TextEncoding(int code, Charset charset) {
        this.code = code;
        this.charset = charset;
    }

    public static TextEncoding fromByteBuffer(ByteBuffer data) {
        int encoding = data.getInt();
        switch (encoding) {
            case 1 -> {
                return UTF_8;
            }
            case 2 -> {
                return UTF_16LE;
            }
            case 3 -> {
                return UTF_16BE;
            }
            default -> {
                System.err.println("Unknown text encoding: " + encoding);
                return UNKNOWN;
            }
        }
    }

    public int getCode() {
        return code;
    }

    public Charset getCharset() {
        return charset;
    }
}
