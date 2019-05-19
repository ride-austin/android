package com.rideaustin.api.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Sergey Petrov on 16/03/2017.
 */

public class SupportTopic {

    @SerializedName("id")
    private Integer id;

    @SerializedName("description")
    private String description;

    @SerializedName("hasChildren")
    private boolean hasChildren;

    @SerializedName("hasForms")
    private boolean hasForms;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean getHasChildren() {
        return hasChildren;
    }

    public boolean getHasForms() {
        return hasForms;
    }

    @Override
    public String toString() {
        return "SupportTopic{" +
                "id=" + id +
                ", description='" + description + '\'' +
                '}';
    }
}

