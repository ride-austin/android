package com.rideaustin.ui.driver;

import com.rideaustin.api.config.CurrentCity;
import com.rideaustin.api.config.SupportedCity;

/**
 * Created by crossover on 21/11/2016.
 */

public class CityModel {
    private Integer cityId;
    private String cityName;
    private String logoUrl;

    public static CityModel fromCurrentCity(CurrentCity currentCity, String logoUrl) {
        CityModel cityModel = new CityModel();
        cityModel.cityId = currentCity.getCityId();
        cityModel.cityName = currentCity.getCityName();
        cityModel.logoUrl = logoUrl;
        return cityModel;
    }

    public static CityModel fromSupportedCity(SupportedCity supportedCity) {
        CityModel cityModel = new CityModel();
        cityModel.cityId = supportedCity.getCityId();
        cityModel.cityName = supportedCity.getCityName();
        cityModel.logoUrl = supportedCity.getLogoBlackUrl();
        return cityModel;
    }

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

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }
}
