
package com.rideaustin.api.config.v2_6_1;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class GeocodingConfiguration {

    @SerializedName("queryHints")
    @Expose
    private List<QueryHint> queryHints = new ArrayList<QueryHint>();

    /**
     * @return The queryHints
     */
    public List<QueryHint> getQueryHints() {
        return queryHints;
    }

    /**
     * @param queryHints The queryHints
     */
    public void setQueryHints(List<QueryHint> queryHints) {
        this.queryHints = queryHints;
    }

}
