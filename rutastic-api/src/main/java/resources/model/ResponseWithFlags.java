package resources.model;

import java.util.HashMap;
import java.util.Map;

public class ResponseWithFlags<Flags> {

    private static final String EMPTY_MESSAGE = "";

    private final Map<Flags, String> flags;

    public ResponseWithFlags() {
        flags = new HashMap<>();
    }

    public void flag(Flags flag, String message) {
        flags.put(flag, message);
    }

    public void flag(Flags flag) {
        flags.put(flag, EMPTY_MESSAGE);
    }

    public boolean flagged(Flags flag) {
        return flags.containsKey(flag);
    }

    public String getFlagMessage(Flags flag) {
        return flags.get(flag);
    }
}
