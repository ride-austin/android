package com.rideaustin.api.model.campaigns;

import java.io.Serializable;

/**
 * Created on 5/19/18.
 *
 * @author sdelaysam
 */
public class Campaign implements Serializable {

    private long id;
    private String bannerText;
    private String bannerIcon;
    private Double estimatedFare;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getBannerText() {
        return bannerText;
    }

    public void setBannerText(String bannerText) {
        this.bannerText = bannerText;
    }

    public String getBannerIcon() {
        return bannerIcon;
    }

    public void setBannerIcon(String bannerIcon) {
        this.bannerIcon = bannerIcon;
    }

    public Double getEstimatedFare() {
        return estimatedFare;
    }

    public void setEstimatedFare(Double estimatedFare) {
        this.estimatedFare = estimatedFare;
    }
}
