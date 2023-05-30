package resources.kudoEntries.model;

import resources.model.UserClaimedIdentity;

public class GetUserKudoEntriesRequest {

    private String requestedUsername;
    private UserClaimedIdentity userClaimedIdentity;

    public GetUserKudoEntriesRequest() {
    }

    public GetUserKudoEntriesRequest(String requestedUsername, UserClaimedIdentity userClaimedIdentity) {
        this.requestedUsername = requestedUsername;
        this.userClaimedIdentity = userClaimedIdentity;
    }

    public String getRequestedUsername() {
        return requestedUsername;
    }

    public void setRequestedUsername(String requestedUsername) {
        this.requestedUsername = requestedUsername;
    }

    public UserClaimedIdentity getUserClaimedIdentity() {
        return userClaimedIdentity;
    }

    public void setUserClaimedIdentity(UserClaimedIdentity userClaimedIdentity) {
        this.userClaimedIdentity = userClaimedIdentity;
    }
}
