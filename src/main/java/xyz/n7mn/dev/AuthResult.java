package xyz.n7mn.dev;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

class AuthResult {

    private String status;
    private String message;
    private String uuid;
    private String username;

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public String getUuidStr() {
        return uuid;
    }

    public UUID getUuid(){

        String s = uuid.replaceFirst("([0-9a-fA-F]{8})([0-9a-fA-F]{4})([0-9a-fA-F]{4})([0-9a-fA-F]{4})([0-9a-fA-F]+)", "$1-$2-$3-$4-$5");
        return UUID.fromString(s);
    }

    public String getUsername() {
        return username;
    }
}
