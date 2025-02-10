import java.util.Objects;

public enum SchemaType {
    TABLE("table"),
    INDEX("index"),
    VIEW("view"),
    TRIGGER("trigger"),
    UNKNOWN("unknown");

    private final String type;

    SchemaType(String type) {
        this.type = type;
    }

    public static SchemaType from(String str) {
       for (SchemaType type : values()) {
           if (type.type.equals(str.toLowerCase())) {
               return type;
           }
       }
       return UNKNOWN;
    }
}
