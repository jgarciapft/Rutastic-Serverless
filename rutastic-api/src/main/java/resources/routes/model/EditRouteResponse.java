package resources.routes.model;

import resources.model.ResponseWithFlags;

public class EditRouteResponse extends ResponseWithFlags<Integer> {
    public static class Flags {
        public static final int ERROR_INVALID_ROUTE_ID = 10;
        public static final int ERROR_INVALID_EDIT = 20;
        public static final int ERROR_NON_MATCHING_ROUTE = 30;
        public static final int ERROR_ROUTE_NOT_FOUND = 40;
        public static final int ERROR_UNAUTHORIZED = 50;
        public static final int ERROR_UNEXPECTED = 60;
    }
}
