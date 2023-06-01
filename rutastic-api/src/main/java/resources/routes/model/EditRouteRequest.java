package resources.routes.model;

import model.Route;
import resources.model.UserClaimedIdentity;

public class EditRouteRequest {

    private long routeId;
    private Route modifiedRoute;
    private UserClaimedIdentity userClaimedIdentity;

    public EditRouteRequest() {
    }

    public EditRouteRequest(long routeId, Route modifiedRoute, UserClaimedIdentity userClaimedIdentity) {
        this.routeId = routeId;
        this.modifiedRoute = modifiedRoute;
        this.userClaimedIdentity = userClaimedIdentity;
    }

    public long getRouteId() {
        return routeId;
    }

    public void setRouteId(long routeId) {
        this.routeId = routeId;
    }

    public Route getModifiedRoute() {
        return modifiedRoute;
    }

    public void setModifiedRoute(Route modifiedRoute) {
        this.modifiedRoute = modifiedRoute;
    }

    public UserClaimedIdentity getUserClaimedIdentity() {
        return userClaimedIdentity;
    }

    public void setUserClaimedIdentity(UserClaimedIdentity userClaimedIdentity) {
        this.userClaimedIdentity = userClaimedIdentity;
    }
}
