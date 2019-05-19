package com.rideaustin.base;

/**
 * Created by Sergey Petrov on 17/08/2017.
 */

public interface ProgressCallback {
    void showProgress();
    void showProgress(String message);
    void showLoadingWheel();
    void hideProgress();
}
