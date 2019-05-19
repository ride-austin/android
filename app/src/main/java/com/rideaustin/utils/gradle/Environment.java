package com.rideaustin.utils.gradle;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by hatak on 04.11.16.
 */
public class Environment {

    @SerializedName("endpoint")
    @Expose
    private String endpoint;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("env")
    @Expose
    private String env;

    /**
     *
     * @return
     * The endpoint
     */
    public String getEndpoint() {
        return endpoint;
    }

    /**
     *
     * @param endpoint
     * The endpoint
     */
    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    /**
     *
     * @return
     * The name
     */
    public String getName() {
        return name;
    }

    /**
     *
     * @param name
     * The name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     *
     * @return
     * The env
     */
    public String getEnv() {
        return env;
    }

    /**
     *
     * @param env
     * The env
     */
    public void setEnv(String env) {
        this.env = env;
    }

    @Override
    public String toString() {
        return "Environment{" +
                "endpoint='" + endpoint + '\'' +
                '}';
    }
}
