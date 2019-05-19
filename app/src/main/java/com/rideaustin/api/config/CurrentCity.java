
package com.rideaustin.api.config;

import com.google.gson.annotations.SerializedName;
import com.rideaustin.api.model.Coordinates;

import java.io.Serializable;
import java.util.List;

public class CurrentCity implements Serializable {

    @SerializedName("cityId")
    private Integer cityId;

    @SerializedName("cityName")
    private String cityName;

    @SerializedName("cityCenterLocation")
    private String cityCenterLocation;

    @SerializedName("cityBoundaryPolygon")
    private List<Coordinates> cityBoundaryPolygon = null;

    public final static long serialVersionUID = -3815516009893710520L;

    public Integer getCityId() {
        return cityId;
    }

    public void setCityId(Integer cityId) {
        this.cityId = cityId;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    @Deprecated
    public String getCityCenterLocation() {
        return cityCenterLocation;
    }

    public List<Coordinates> getCityBoundaryPolygon() {
        return cityBoundaryPolygon;
    }

    public void setCityBoundaryPolygon(List<Coordinates> cityBoundaryPolygon) {
        this.cityBoundaryPolygon = cityBoundaryPolygon;
    }
}
