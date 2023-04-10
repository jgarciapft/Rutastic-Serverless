package model;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * Definition of User model as a JavaBean
 */
@XmlRootElement
public class User implements Serializable {

    private static final long SerialVersionUID = 1L;

    private String username;

    public User() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

}
