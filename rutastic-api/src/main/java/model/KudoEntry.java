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
    private long submissionDate;

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

    public long getSubmissionDate() {
        return submissionDate;
    }

    public void setSubmissionDate(long submissionDate) {
        this.submissionDate = submissionDate;
    }
}
