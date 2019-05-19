package com.rideaustin.api.model.surgearea;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

/**
 * @author sdelaysam.
 */

public class SurgeAreas implements Serializable {

    @SerializedName("surgeAreas")
    private List<SurgeArea> surgeAreas;

    public List<SurgeArea> getSurgeAreas() {
        return surgeAreas;
    }

    @Override
    public String toString() {
        return "SurgeAreas{" +
                "surgeAreas=" + surgeAreas +
                '}';
    }
}
