package com.rideaustin.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.rideaustin.App;
import com.rideaustin.base.retrofit.RetrofitException;

import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

/**
 * Created by kshumelchyk on 7/14/16.
 */
public class NetworkHelper {

    public static boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) App.getInstance().getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();

        return (netInfo != null && netInfo.isConnectedOrConnecting());
    }

    public static boolean causedByNetwork(Throwable throwable) {
        return throwable instanceof UnknownHostException
                || throwable instanceof SocketException
                || throwable instanceof SocketTimeoutException
                || (throwable instanceof RetrofitException && ((RetrofitException) throwable).causedByNetwork());
    }
}
