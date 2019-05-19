package com.rideaustin.api.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by yura on 7/23/16.
 */
public class Map {

    @SerializedName("url")
    private String url;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return "Map{" +
                "url='" + url + '\'' +
                '}';
    }
}
