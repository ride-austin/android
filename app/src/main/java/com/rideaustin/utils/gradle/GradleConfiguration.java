package com.rideaustin.utils.gradle;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hatak on 04.11.16.
 */
public class GradleConfiguration {

    @SerializedName("environments")
    @Expose
    private List<Environment> environments = new ArrayList<Environment>();

    /**
     *
     * @return
     * The environments
     */
    public List<Environment> getEnvironments() {
        return environments;
    }

    /**
     *
     * @param environments
     * The environments
     */
    public void setEnvironments(List<Environment> environments) {
        this.environments = environments;
    }

}