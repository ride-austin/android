
package com.rideaustin.api.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class NamedPolygon implements Serializable {

    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("boundary")
    @Expose
    private List<Boundary> boundary = null;
    private final static long serialVersionUID = 800697894894693247L;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Boundary> getBoundary() {
        return boundary;
    }

    public void setBoundary(List<Boundary> boundary) {
        this.boundary = boundary;
    }


}
