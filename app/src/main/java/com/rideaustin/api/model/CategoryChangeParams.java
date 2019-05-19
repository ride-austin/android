package com.rideaustin.api.model;

import android.support.annotation.VisibleForTesting;

import com.google.gson.annotations.SerializedName;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by crossover on 27/02/2017.
 */

public class CategoryChangeParams {
    private static final String SOURCE_ADMIN_EDIT = "ADMIN_EDIT";
    private static final String SOURCE_MISSED_REQUEST = "MISSED_REQUEST";

    @SerializedName("source")
    private String source;

    @SerializedName("disabled")
    private Set<String> disabled = new HashSet<>();

    public String getSource() {
        return source;
    }

    public boolean isAdminEdit() {
        return source != null && SOURCE_ADMIN_EDIT.equals(source);
    }

    public boolean isMissedRequest() {
        return source != null && SOURCE_MISSED_REQUEST.equals(source);
    }

    public Set<String> getDisabled() {
        return disabled;
    }

    @VisibleForTesting
    public static CategoryChangeParams adminEdit() {
        CategoryChangeParams params = new CategoryChangeParams();
        params.source = SOURCE_ADMIN_EDIT;
        return params;
    }

    @VisibleForTesting
    public static CategoryChangeParams missedRequest() {
        CategoryChangeParams params = new CategoryChangeParams();
        params.source = SOURCE_MISSED_REQUEST;
        return params;
    }
}
