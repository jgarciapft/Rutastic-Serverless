package model;

import helper.DateTimeUtils;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Date;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;

/**
 * Definition of Route model as a JavaBean
 */
public class Route implements Serializable {

    private static final long SerialVersionUID = 1L;

    public static final String DEFAULT_SKILL_LEVEL = "facil";
    public static final int DEFAULT_KUDOS = 0;
    public static final boolean DEFAULT_BLOCKED_STATE = false;

    public static final String CATEGORY_SEPARATOR = ", ";

    private long id;
    private String createdByUser;
    private String title;
    private String description;
    private int distance;
    private int duration;
    private int elevation;
    private String creationDate;
    private String categories;
    private String skillLevel;
    private int kudos;
    private boolean blocked;

    public Route() {
        // Default values
        kudos = DEFAULT_KUDOS;
        blocked = DEFAULT_BLOCKED_STATE;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getCreatedByUser() {
        return createdByUser;
    }

    public void setCreatedByUser(String createdByUser) {
        this.createdByUser = createdByUser;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getElevation() {
        return elevation;
    }

    public void setElevation(int elevation) {
        this.elevation = elevation;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }

    public String getCategories() {
        return categories;
    }

    public void setCategories(String categories) {
        this.categories = categories;
    }

    public String getSkillLevel() {
        return skillLevel;
    }

    public void setSkillLevel(String skillLevel) {
        this.skillLevel = skillLevel;
    }

    public int getKudos() {
        return kudos;
    }

    public void setKudos(int kudos) {
        this.kudos = kudos;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }
}
