package com.rideaustin.models;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by Sergey Petrov on 03/05/2017.
 */

public class TermsResponse {

    @Nullable
    private String terms;

    @Nullable
    private String error;

    private boolean shouldRetry;

    private TermsResponse(@Nullable String terms, @Nullable String error, boolean shouldRetry) {
        this.terms = terms;
        this.error = error;
        this.shouldRetry = shouldRetry;
    }

    public String getTerms() {
        return terms;
    }

    public String getError() {
        return error;
    }

    public boolean shouldRetry() {
        return shouldRetry;
    }

    public boolean isSuccessfull() {
        return terms != null && error == null;
    }

    public static TermsResponse withTerms(@NonNull String terms) {
        return new TermsResponse(terms, null, false);
    }

    public static TermsResponse withError(@NonNull String error, boolean shouldRetry) {
        return new TermsResponse(null, error, shouldRetry);
    }
}
