package com.rideaustin.api.config;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by Sergey Petrov on 22/08/2017.
 */

public class UnpaidConfig implements Serializable {

    @SerializedName("enabled")
    private boolean enabled;

    @SerializedName("title")
    private String title;

    @SerializedName("subtitle")
    private String subTitle;

    @SerializedName("warningMessage")
    private String warningMessage;

    @SerializedName("iconSmallURL")
    private String iconSmallUrl;

    @SerializedName("iconLargeURL")
    private String iconLargeUrl;

    public boolean isEnabled() {
        return enabled;
    }

    public String getTitle() {
        return title;
    }

    public String getSubTitle() {
        return subTitle;
    }

    public String getWarningMessage() {
        return warningMessage;
    }

    public String getIconSmallUrl() {
        return iconSmallUrl;
    }

    public String getIconLargeUrl() {
        return iconLargeUrl;
    }

    @Override
    public String toString() {
        return "UnpaidConfig{" +
                "enabled=" + enabled +
                ", title='" + title + '\'' +
                ", subTitle='" + subTitle + '\'' +
                ", warningMessage='" + warningMessage + '\'' +
                ", iconSmallUrl='" + iconSmallUrl + '\'' +
                ", iconLargeUrl='" + iconLargeUrl + '\'' +
                '}';
    }
}
