package com.rideaustin.ui.map;

/**
 * Created by hatak on 01.06.2017.
 */

public interface CollapsibleElementsListener {

    void registerCollapsibleElements(final CollapsibleElement... elements);

    void unregisterCollapsibleElement(final CollapsibleElement element);

    void notifyExpansionOf(CollapsibleElement expandingElement);

}
