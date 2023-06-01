package resources.routes.model;

import resources.model.ResponseWithFlags;

public class ChangeBlockedStateResponse extends ResponseWithFlags<Integer> {
    public static class Flags {
        public static final int ERROR_INVALID_ROUTE_ID = 10;
        public static final int ERROR_INVALID_ACTION_PARAM = 20;
        public static final int ERROR_INVALID_ACTION = 30;
        public static final int ERROR_ROUTE_NOT_FOUND = 40;
        public static final int ERROR_UNAUTHORIZED = 50;
        public static final int ERROR_UNEXPECTED = 60;
    }
}
