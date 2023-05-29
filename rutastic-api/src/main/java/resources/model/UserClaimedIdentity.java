package resources.model;

public class UserClaimedIdentity {

    private String username;

    public UserClaimedIdentity() {
    }

    public UserClaimedIdentity(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
