package com.rideaustin.api.config;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by hatak on 07.11.2017.
 */

public class DirectConnectConfig implements Serializable {

    @SerializedName("enabled")
    private boolean enabled;

    @SerializedName("title")
    private String title;

    @SerializedName("description")
    private String description;

    @SerializedName("requiresChauffeur")
    private boolean requiresChauffeur;

    public boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
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

    public boolean isRequiresChauffeur() {
        return requiresChauffeur;
    }

    public void setRequiresChauffeur(boolean requiresChauffeur) {
        this.requiresChauffeur = requiresChauffeur;
    }
}