package com.rideaustin.api.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by crossover on 24/05/2017.
 */

public class SupportForm {

    @SerializedName("id")
    private Integer id;

    @SerializedName("title")
    private String title;

    @SerializedName("body")
    private String body;

    @SerializedName("headerText")
    private String headerText;

    @SerializedName("subtitle")
    private String subtitle;

    @SerializedName("actionTitle")
    private String actionTitle;

    @SerializedName("actionType")
    private String actionType;

    @SerializedName("supportFields")
    private List<SupportField> supportFields;

    public Integer getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }

    public String getHeaderText() {
        return headerText;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public String getActionTitle() {
        return actionTitle;
    }

    public String getActionType() {
        return actionType;
    }

    public List<SupportField> getSupportFields() {
        return supportFields;
    }

    @Override
    public String toString() {
        return "SupportForm{" +
                "id=" + id +
                ", title='" + title + '\'' +
                '}';
    }
}
