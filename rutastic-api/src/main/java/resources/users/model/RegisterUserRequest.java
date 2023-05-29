package resources.users.model;

import model.User;

public class RegisterUserRequest {

    private User newUser;

    public RegisterUserRequest() {
    }

    public RegisterUserRequest(User newUser) {
        this.newUser = newUser;
    }

    public User getNewUser() {
        return newUser;
    }

    public void setNewUser(User newUser) {
        this.newUser = newUser;
    }
}
