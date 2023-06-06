package aws.model;

import java.io.Serializable;

public class RDSManagedCredentials implements Serializable {
    private static final long SerialVersionUID = 1L;

    private String username;
    private String password;

    public RDSManagedCredentials() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
