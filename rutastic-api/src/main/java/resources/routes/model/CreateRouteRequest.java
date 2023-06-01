package resources.routes.model;

import model.Route;
import resources.model.UserClaimedIdentity;

public class CreateRouteRequest {

    private Route route;
    private UserClaimedIdentity userClaimedIdentity;

    public CreateRouteRequest() {
    }

    public CreateRouteRequest(Route route, UserClaimedIdentity userClaimedIdentity) {
        this.route = route;
        this.userClaimedIdentity = userClaimedIdentity;
    }

    public Route getRoute() {
        return route;
    }

    public void setRoute(Route route) {
        this.route = route;
    }

    public UserClaimedIdentity getUserClaimedIdentity() {
        return userClaimedIdentity;
    }

    public void setUserClaimedIdentity(UserClaimedIdentity userClaimedIdentity) {
        this.userClaimedIdentity = userClaimedIdentity;
    }
}
