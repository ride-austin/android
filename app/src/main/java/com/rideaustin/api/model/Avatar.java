package com.rideaustin.api.model;

import android.support.annotation.StringDef;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by v.garshyn on 24.06.16.
 * <p>
 * This class represent user role
 * P.S. it is my suspicion cause it is not documented and get after research
 *
 * @author vharshyn
 */
public class Avatar implements Serializable {

    public static final String RIDER = "RIDER";
    public static final String DRIVER = "DRIVER";

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({RIDER, DRIVER})
    public @interface UserRole {
    }

    @SerializedName("id")
    private long id;

    @SerializedName("type")
    private String type;

    @SerializedName("active")
    private boolean active;

    public long getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    /**
     * Check is it rider type
     *
     * @return
     */
    public boolean isRider() {
        return RIDER.equals(type);
    }

    /**
     * Check is it driver type
     *
     * @return
     */
    public boolean isDriver() {
        return DRIVER.equals(type);
    }

    public boolean isActive() {
        return active;
    }

    @Override
    public String toString() {
        return "{ " + id + ", " + type + " }";
    }
}
