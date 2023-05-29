package resources.users.model;

import model.User;

import java.util.List;

public class GetAllUsersResponse {

    private List<User> allUsers;

    public GetAllUsersResponse() {
    }

    public List<User> getAllUsers() {
        return allUsers;
    }

    public void setAllUsers(List<User> allUsers) {
        this.allUsers = allUsers;
    }
}
