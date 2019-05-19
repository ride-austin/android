package com.rideaustin.api.model.campaigns;

import java.io.Serializable;
import java.util.List;

/**
 * Created on 5/19/18.
 *
 * @author sdelaysam
 */
public class CampaignDetails implements Serializable {

    private String headerIcon;
    private String headerTitle;
    private String body;
    private String footer;
    private List<CampaignArea> areas;

    public String getHeaderIcon() {
        return headerIcon;
    }

    public void setHeaderIcon(String headerIcon) {
        this.headerIcon = headerIcon;
    }

    public String getHeaderTitle() {
        return headerTitle;
    }

    public void setHeaderTitle(String headerTitle) {
        this.headerTitle = headerTitle;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getFooter() {
        return footer;
    }

    public void setFooter(String footer) {
        this.footer = footer;
    }

    public List<CampaignArea> getAreas() {
        return areas;
    }

    public void setAreas(List<CampaignArea> areas) {
        this.areas = areas;
    }
}
