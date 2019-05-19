
package com.rideaustin.api.config.v2_6_1;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class DriverRegistration {

    @SerializedName("requirements")
    @Expose
    private List<String> requirements = new ArrayList<String>();
    @SerializedName("description")
    @Expose
    private String description;

    /**
     * @return The requirements
     */
    public List<String> getRequirements() {
        return requirements;
    }

    /**
     * @param requirements The requirements
     */
    public void setRequirements(List<String> requirements) {
        this.requirements = requirements;
    }

    /**
     * @return The description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description The description
     */
    public void setDescription(String description) {
        this.description = description;
    }

}
