package resources.routes.model;

import resources.model.UserClaimedIdentity;

public class DeleteRouteRequest {

    private long routeId;
    private UserClaimedIdentity userClaimedIdentity;

    public DeleteRouteRequest() {
    }

    public DeleteRouteRequest(long routeId, UserClaimedIdentity userClaimedIdentity) {
        this.routeId = routeId;
        this.userClaimedIdentity = userClaimedIdentity;
    }

    public long getRouteId() {
        return routeId;
    }

    public void setRouteId(long routeId) {
        this.routeId = routeId;
    }

    public UserClaimedIdentity getUserClaimedIdentity() {
        return userClaimedIdentity;
    }

    public void setUserClaimedIdentity(UserClaimedIdentity userClaimedIdentity) {
        this.userClaimedIdentity = userClaimedIdentity;
    }
}
