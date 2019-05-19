package com.rideaustin.ui.drawer.queue;

/**
 * Created by hatak on 14.10.16.
 */

public class QueueEntry {

    final private String carCategory;
    final private String queueValue;
    final private String imageUrl;

    public QueueEntry(final String carCategory, final String queueValue, final String imageUrl){
        this.carCategory = carCategory;
        this.queueValue = queueValue;
        this.imageUrl = imageUrl;
    }

    public String getCarCategory() {
        return carCategory;
    }

    public String getQueueValue() {
        return queueValue;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
