package com.rideaustin.api.config;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Sergey Petrov on 12/09/2017.
 */

public class GenderSelection implements Serializable {

    @SerializedName("title")
    private String title;

    @SerializedName("subtitle")
    private String subtitle;

    @SerializedName("options")
    private List<String> options;

    public String getTitle() {
        return title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public List<String> getOptions() {
        return options;
    }

    @Override
    public String toString() {
        return "GenderSelection{" +
                "title='" + title + '\'' +
                ", subtitle='" + subtitle + '\'' +
                ", options=" + options +
                '}';
    }
}
