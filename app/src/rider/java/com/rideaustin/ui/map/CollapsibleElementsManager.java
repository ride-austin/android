package com.rideaustin.ui.map;

import java.util.HashSet;
import java.util.Set;

import java8.util.stream.StreamSupport;

/**
 * Created by hatak on 01.06.2017.
 */

public class CollapsibleElementsManager implements CollapsibleElementsListener {

    private Set<CollapsibleElement> collapsibleElements = new HashSet<>();

    @Override
    public void registerCollapsibleElements(CollapsibleElement... elements) {
        for (CollapsibleElement element : elements) {
            if (element != null) {
                collapsibleElements.add(element);
            }
        }
    }

    @Override
    public void unregisterCollapsibleElement(CollapsibleElement element) {
        collapsibleElements.remove(element);
    }

    @Override
    public void notifyExpansionOf(CollapsibleElement expandingElement) {
        StreamSupport.stream(collapsibleElements)
                .filter(element -> !element.equals(expandingElement))
                .forEach(CollapsibleElement::collapseSelf);
    }

}
