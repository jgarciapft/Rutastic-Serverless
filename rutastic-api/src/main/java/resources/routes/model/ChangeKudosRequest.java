package resources.routes.model;

import resources.model.UserClaimedIdentity;

public class ChangeKudosRequest {

    private long requestedRouteId;
    private String action;
    private UserClaimedIdentity userClaimedIdentity;

    public ChangeKudosRequest() {
    }

    public ChangeKudosRequest(long requestedRouteId, String action, UserClaimedIdentity userClaimedIdentity) {
        this.requestedRouteId = requestedRouteId;
        this.action = action;
        this.userClaimedIdentity = userClaimedIdentity;
    }

    public long getRequestedRouteId() {
        return requestedRouteId;
    }

    public void setRequestedRouteId(long requestedRouteId) {
        this.requestedRouteId = requestedRouteId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public UserClaimedIdentity getUserClaimedIdentity() {
        return userClaimedIdentity;
    }

    public void setUserClaimedIdentity(UserClaimedIdentity userClaimedIdentity) {
        this.userClaimedIdentity = userClaimedIdentity;
    }
}
