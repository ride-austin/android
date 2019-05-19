package com.rideaustin.ui.map;

import android.content.Context;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.rideaustin.R;
import com.rideaustin.schedulers.RxSchedulers;
import com.rideaustin.utils.ImageHelper;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.subscriptions.Subscriptions;

/**
 * Created by hatak on 20.03.2017.
 */

public class RiderMarker {

    private Subscription timerSubscription = Subscriptions.empty();
    private Marker marker;
    private final Context context;
    private final GoogleMap googleMap;
    private final int timeToLiveWithoutUpdate;

    public RiderMarker(final Context context, final GoogleMap googleMap, final LatLng position, int timeToLiveWithoutUpdate) {
        this.context = context;
        this.googleMap = googleMap;
        this.timeToLiveWithoutUpdate = timeToLiveWithoutUpdate;
        updatePosition(position);
    }

    public void remove() {
        if (marker != null) {
            marker.remove();
            marker = null;
        }
        timerSubscription.unsubscribe();
    }

    public void updatePosition(LatLng position) {
        marker = createOrUpdateMarker(position);
        resetWatchdog();
    }

    public boolean isVisible() {
        return marker != null;
    }

    /**
     * Try to reuse existing marker or create new,
     * if no existing  marker or position changed.
     * see rider's MainMapFragment#addOrUpdateMarker
     * <p/>
     * @param position marker position
     * @return marker
     */
    private Marker createOrUpdateMarker(LatLng position) {
        if (marker != null) {
            // check position not changed
            if (marker.getPosition() != null && marker.getPosition().equals(position)) {
                // return the same marker
                return marker;
            } else {
                // otherwise its better to recreate marker
                // since it could disappear during map animation
                marker.remove();
            }
        }
        // create new marker
        return googleMap.addMarker(new MarkerOptions()
                .position(position)
                .icon(BitmapDescriptorFactory.fromBitmap(ImageHelper.createBitmap(context, R.drawable.icn_location)))
                .title(context.getString(R.string.rider_marker_title)));

    }

    private void resetWatchdog() {
        timerSubscription.unsubscribe();
        timerSubscription = Observable.timer(timeToLiveWithoutUpdate, TimeUnit.SECONDS, RxSchedulers.computation())
                .observeOn(RxSchedulers.main())
                .subscribe(aLong -> remove());
    }
}
