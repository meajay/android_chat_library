package teacher.nconnect.com.chat.constants;

/**
 * Created by Ajay on 29-06-2018.
 */
public enum MessageType {
    TEXT("text"),
    IMAGE("image"),
    VIDEO("video"),
    AUDIO("audio"),
    DOCUMENT("document");

    private String type;

    MessageType(String type) {
        this.type = type;
    }

    public String getValue() {
        return type;
    }

    public static int getIntValue(String type) {
        if (type.equalsIgnoreCase(TEXT.getValue())) {
            return 1;
        }

        if (type.equalsIgnoreCase(IMAGE.getValue())) {
            return 2;
        }

        if (type.equalsIgnoreCase(VIDEO.getValue())) {
            return 3;
        }

        if (type.equalsIgnoreCase(AUDIO.getValue())) {
            return 4;
        }
        return 5;
    }
}
