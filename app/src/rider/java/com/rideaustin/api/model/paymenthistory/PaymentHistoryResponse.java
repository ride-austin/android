package com.rideaustin.api.model.paymenthistory;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by supreethks on 19/11/16.
 */

public class PaymentHistoryResponse {


    @SerializedName("content")
    private List<PaymentHistory> paymentHistoryList;

    @SerializedName("lastPage")
    private boolean lastPage;

    public List<PaymentHistory> getPaymentHistoryList() {
        return paymentHistoryList;
    }

    public boolean isLastPage() {
        return lastPage;
    }

    @Override
    public String toString() {
        return "PaymentHistoryResponse{" +
                "paymentHistoryList=" + paymentHistoryList +
                ", lastPage=" + lastPage +
                '}';
    }
}
