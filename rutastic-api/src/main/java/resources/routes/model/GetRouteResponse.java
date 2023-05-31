package resources.routes.model;

import model.Route;

public class GetRouteResponse {

    private Route route;

    public GetRouteResponse() {
    }

    public GetRouteResponse(Route route) {
        this.route = route;
    }

    public Route getRoute() {
        return route;
    }

    public void setRoute(Route route) {
        this.route = route;
    }
}
