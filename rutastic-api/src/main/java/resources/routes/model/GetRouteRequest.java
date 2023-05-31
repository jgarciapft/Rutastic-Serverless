package resources.routes.model;

public class GetRouteRequest {

    private long requestedRouteId;

    public GetRouteRequest() {
    }

    public GetRouteRequest(long requestedRouteId) {
        this.requestedRouteId = requestedRouteId;
    }

    public long getRequestedRouteId() {
        return requestedRouteId;
    }

    public void setRequestedRouteId(long requestedRouteId) {
        this.requestedRouteId = requestedRouteId;
    }

}
