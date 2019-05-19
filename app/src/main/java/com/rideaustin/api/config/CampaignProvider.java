package com.rideaustin.api.config;

import java.io.Serializable;

/**
 * Created on 5/19/18.
 *
 * @author sdelaysam
 */
public class CampaignProvider implements Serializable {

    private long id;
    private String menuTitle;
    private String menuIcon;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getMenuTitle() {
        return menuTitle;
    }

    public void setMenuTitle(String menuTitle) {
        this.menuTitle = menuTitle;
    }

    public String getMenuIcon() {
        return menuIcon;
    }

    public void setMenuIcon(String menuIcon) {
        this.menuIcon = menuIcon;
    }
}
