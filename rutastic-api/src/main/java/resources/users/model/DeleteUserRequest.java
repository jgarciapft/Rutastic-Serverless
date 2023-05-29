package resources.users.model;

import resources.model.UserClaimedIdentity;

public class DeleteUserRequest {

    private String requestedUsernameForDeletion;
    private UserClaimedIdentity userClaimedIdentity;

    public DeleteUserRequest() {
    }

    public DeleteUserRequest(String requestedUsernameForDeletion, UserClaimedIdentity userClaimedIdentity) {
        this.requestedUsernameForDeletion = requestedUsernameForDeletion;
        this.userClaimedIdentity = userClaimedIdentity;
    }

    public String getRequestedUsernameForDeletion() {
        return requestedUsernameForDeletion;
    }

    public void setRequestedUsernameForDeletion(String requestedUsernameForDeletion) {
        this.requestedUsernameForDeletion = requestedUsernameForDeletion;
    }

    public UserClaimedIdentity getUserClaimedIdentity() {
        return userClaimedIdentity;
    }

    public void setUserClaimedIdentity(UserClaimedIdentity userClaimedIdentity) {
        this.userClaimedIdentity = userClaimedIdentity;
    }
}
