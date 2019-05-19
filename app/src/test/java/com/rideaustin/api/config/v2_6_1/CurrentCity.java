
package com.rideaustin.api.config.v2_6_1;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class CurrentCity {

    @SerializedName("cityId")
    @Expose
    private Integer cityId;
    @SerializedName("cityName")
    @Expose
    private String cityName;
    @SerializedName("cityCenterLocation")
    @Expose
    private String cityCenterLocation;
    @SerializedName("cityBoundaryPolygon")
    @Expose
    private List<CityBoundaryPolygon> cityBoundaryPolygon = new ArrayList<CityBoundaryPolygon>();

    /**
     *
     * @return
     *     The cityId
     */
    public Integer getCityId() {
        return cityId;
    }

    /**
     *
     * @param cityId
     *     The cityId
     */
    public void setCityId(Integer cityId) {
        this.cityId = cityId;
    }

    /**
     *
     * @return
     *     The cityName
     */
    public String getCityName() {
        return cityName;
    }

    /**
     *
     * @param cityName
     *     The cityName
     */
    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    /**
     *
     * @return
     *     The cityCenterLocation
     */
    public String getCityCenterLocation() {
        return cityCenterLocation;
    }

    /**
     *
     * @param cityCenterLocation
     *     The cityCenterLocation
     */
    public void setCityCenterLocation(String cityCenterLocation) {
        this.cityCenterLocation = cityCenterLocation;
    }

    /**
     *
     * @return
     *     The cityBoundaryPolygon
     */
    public List<CityBoundaryPolygon> getCityBoundaryPolygon() {
        return cityBoundaryPolygon;
    }

    /**
     *
     * @param cityBoundaryPolygon
     *     The cityBoundaryPolygon
     */
    public void setCityBoundaryPolygon(List<CityBoundaryPolygon> cityBoundaryPolygon) {
        this.cityBoundaryPolygon = cityBoundaryPolygon;
    }

}
