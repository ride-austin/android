package com.rideaustin.api.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Sergey Petrov on 16/03/2017.
 */

public class SupportRequest {

    @SerializedName("comments")
    private String comments;

    @SerializedName("rideId")
    private Long rideId;

    @SerializedName("topicId")
    private Integer topicId;

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public Long getRideId() {
        return rideId;
    }

    public void setRideId(Long rideId) {
        this.rideId = rideId;
    }

    public Integer getTopicId() {
        return topicId;
    }

    public void setTopicId(Integer topicId) {
        this.topicId = topicId;
    }

    @Override
    public String toString() {
        return "SupportRequest{" +
                "comments='" + comments + '\'' +
                ", rideId=" + rideId +
                ", topicId=" + topicId +
                '}';
    }
}
