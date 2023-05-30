package resources.kudoEntries.model;

import resources.model.UserClaimedIdentity;

public class GetUserKudoEntriesForRouteRequest {

    private String requestedUsername;
    private long requestedRouteId;
    private UserClaimedIdentity userClaimedIdentity;

    public GetUserKudoEntriesForRouteRequest() {
    }

    public GetUserKudoEntriesForRouteRequest(String requestedUsername, long requestedRouteId, UserClaimedIdentity userClaimedIdentity) {
        this.requestedUsername = requestedUsername;
        this.requestedRouteId = requestedRouteId;
        this.userClaimedIdentity = userClaimedIdentity;
    }

    public String getRequestedUsername() {
        return requestedUsername;
    }

    public void setRequestedUsername(String requestedUsername) {
        this.requestedUsername = requestedUsername;
    }

    public long getRequestedRouteId() {
        return requestedRouteId;
    }

    public void setRequestedRouteId(long requestedRouteId) {
        this.requestedRouteId = requestedRouteId;
    }

    public UserClaimedIdentity getUserClaimedIdentity() {
        return userClaimedIdentity;
    }

    public void setUserClaimedIdentity(UserClaimedIdentity userClaimedIdentity) {
        this.userClaimedIdentity = userClaimedIdentity;
    }
}
