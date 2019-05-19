package com.rideaustin.ui.earnings;

import java.io.Serializable;

/**
 * Created by vokol on 15.08.2016.
 */
public class Earning implements Serializable {
    private final String dayOfTheWeek;
    private EarningAmount earningAmount;

    public Earning(String dayOfTheWeek, EarningAmount amount) {
        this.dayOfTheWeek = dayOfTheWeek;
        this.earningAmount = amount;
    }

    public String getDayOfTheWeek() {
        return dayOfTheWeek;
    }

    public EarningAmount getEarningAmount() {
        return earningAmount;
    }
}
