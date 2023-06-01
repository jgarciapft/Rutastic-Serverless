package model;

import java.io.Serializable;

/**
 * Definition of KudoEntry model as a JavaBean
 */
public class KudoEntry implements Serializable {

    private static final long SerialVersionUID = 1L;

    private String user;
    private long route;
    private int modifier;
    private String submissionDate;

    public KudoEntry() {
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public long getRoute() {
        return route;
    }

    public void setRoute(long route) {
        this.route = route;
    }

    public int getModifier() {
        return modifier;
    }

    public void setModifier(int modifier) {
        this.modifier = modifier;
    }

    public String getSubmissionDate() {
        return submissionDate;
    }

    public void setSubmissionDate(String submissionDate) {
        this.submissionDate = submissionDate;
    }
}
