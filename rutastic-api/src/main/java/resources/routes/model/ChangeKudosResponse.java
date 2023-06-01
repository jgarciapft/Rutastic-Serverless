package resources.routes.model;

import resources.model.ResponseWithFlags;

public class ChangeKudosResponse extends ResponseWithFlags<Integer> {
    public static class Flags {
        public static final int ERROR_INVALID_ROUTE_ID = 10;
        public static final int ERROR_INVALID_ACTION_PARAM = 20;
        public static final int ERROR_ROUTE_NOT_FOUND = 30;
        public static final int ERROR_UNEXPECTED = 40;
    }
}
