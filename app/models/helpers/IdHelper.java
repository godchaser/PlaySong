package models.helpers;

import java.util.UUID;

public class IdHelper {
    public static String getRandomId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}
