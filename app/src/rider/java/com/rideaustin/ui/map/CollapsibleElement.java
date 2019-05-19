package com.rideaustin.ui.map;

/**
 * Created by hatak on 01.06.2017.
 */

public interface CollapsibleElement {
    void collapseSelf();

    void setCollapsibleListener(CollapsibleElementsListener listener);

}
