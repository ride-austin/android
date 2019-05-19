package com.rideaustin.api.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by ysych on 29.06.2016.
 */
public class Charity implements Serializable {

    private static final long serialVersionUID = 1758744679005546172L;

    @SerializedName("id")
    private int id;
    @SerializedName("name")
    private String name;
    @SerializedName("imageUrl")
    private String imageUrl;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    @Override
    public String toString() {
        return "Charity{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                '}';
    }
}
