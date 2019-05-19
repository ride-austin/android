package com.rideaustin.ui.earnings;

import java.io.Serializable;

/**
 * Created by vokol on 15.08.2016.
 */
public class EarningAmount implements Serializable {
    private final double totalFare;
    private final double raFee;
    private final double driverPayment;
    private int tripsPerformed;
    private final double tipsAmount;

    public EarningAmount(double totalFare, double raFee, double driverPayment, Double tips) {
        this.totalFare = totalFare;
        this.raFee = raFee;
        this.driverPayment = driverPayment;
        this.tipsAmount = tips;
    }

    public double getDriverPayment() {
        return driverPayment;
    }

    public double getTotalFare() {
        return totalFare;
    }

    public double getRaFee() {
        return raFee;
    }

    public void setTripsPerformed(int tripsPerformed) {
        this.tripsPerformed = tripsPerformed;
    }

    public int getTripsPerformed() {
        return tripsPerformed;
    }

    public double getTipsAmount() {
        return tipsAmount;
    }
}
