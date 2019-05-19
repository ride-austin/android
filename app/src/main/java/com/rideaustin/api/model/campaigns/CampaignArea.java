package com.rideaustin.api.model.campaigns;

import com.rideaustin.api.model.NamedPolygon;

/**
 * Created on 5/19/18.
 *
 * @author sdelaysam
 */
public class CampaignArea extends NamedPolygon {

    private String color;

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}
