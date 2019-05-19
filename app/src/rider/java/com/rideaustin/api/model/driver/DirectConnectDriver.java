package com.rideaustin.api.model.driver;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by hatak on 30.10.2017.
 */

public class DirectConnectDriver implements Serializable {

    @SerializedName("firstName")
    private String firstName;

    @SerializedName("lastName")
    private String lastName;

    @SerializedName("photoUrl")
    private String photoUrl;

    @SerializedName("rating")
    private Double rating;

    @SerializedName("categories")
    private List<String> categories;

    @SerializedName("factors")
    private Map<String, Float> factors = new HashMap<>();

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public List<String> getCategories() {
        return categories;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    public Map<String, Float> getFactors() {
        return factors;
    }

    public void setFactors(Map<String, Float> factors) {
        this.factors = factors;
    }
}


