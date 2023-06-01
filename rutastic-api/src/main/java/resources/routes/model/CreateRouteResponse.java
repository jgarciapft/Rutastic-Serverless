package resources.routes.model;

import resources.model.ResponseWithFlags;

public class CreateRouteResponse extends ResponseWithFlags<Integer> {

    private long newRouteId;

    public CreateRouteResponse() {
    }

    public CreateRouteResponse(long newRouteId) {
        this.newRouteId = newRouteId;
    }

    public long getNewRouteId() {
        return newRouteId;
    }

    public void setNewRouteId(long newRouteId) {
        this.newRouteId = newRouteId;
    }

    public static class Flags {
        public static final int ERROR_INVALID_ROUTE = 10;
        public static final int ERROR_SAVING_ROUTE = 20;
    }
}
