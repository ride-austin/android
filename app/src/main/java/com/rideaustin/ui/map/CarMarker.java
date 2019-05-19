package com.rideaustin.ui.map;

import android.content.Context;
import android.graphics.Bitmap;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.rideaustin.App;
import com.rideaustin.R;
import com.rideaustin.manager.location.RALocation;
import com.rideaustin.utils.Constants;
import com.rideaustin.utils.ImageHelper;
import com.rideaustin.utils.LatLngInterpolator;
import com.rideaustin.utils.MarkerAnimation;
import com.rideaustin.utils.location.DistanceUtil;

import java8.util.Optional;

/**
 * Created by hatak on 22.02.2017.
 */

public class CarMarker implements AnimateObject {
    protected final Marker marker;
    private MarkerAnimation.Job currentAnimationJob;
    private Target<Bitmap> target;
    private Optional<String> iconUrl = Optional.empty();

    public CarMarker(Context context,
                     final GoogleMap googleMap,
                     RALocation driverLocation,
                     final float transparency,
                     Optional<String> iconUrl) {
        int markerRes = iconUrl.isPresent() ? R.drawable.transparent_pixel : R.drawable.car;
        marker = googleMap.addMarker(
                new MarkerOptions()
                        .title(context.getString(R.string.car_marker))
                        .position(driverLocation.getCoordinates())
                        .rotation(driverLocation.getLocation().getBearing())
                        .alpha(transparency)
                        .icon(BitmapDescriptorFactory.fromResource(markerRes))
                        .anchor(0.5f, 0.5f));
        setIcon(iconUrl);
    }

    public void update(RALocation driverLocation, long dt) {
        if (currentAnimationJob != null) {
            currentAnimationJob.cancel();
        }

        float distance = DistanceUtil.distance(driverLocation.getCoordinates(), this.marker.getPosition());
        if (distance < Constants.MAX_DISTANCE_BETWEEN_TO_ANIMATE_M && distance > Constants.MIN_DISTANCE_BETWEEN_TO_ANIMATE_M) {
            currentAnimationJob = MarkerAnimation.animateMapObjectToPosition(dt,
                    this,
                    driverLocation.getCoordinates(),
                    driverLocation.getLocation().getBearing(),
                    new LatLngInterpolator.Linear()
            );
        } else {
            currentAnimationJob = null;
            marker.setPosition(driverLocation.getCoordinates());
            marker.setRotation(driverLocation.getLocation().getBearing());
        }
    }

    public void setIcon(Optional<String> iconUrl) {
        if (!this.iconUrl.equals(iconUrl)) {
            this.iconUrl = iconUrl;
            if (target != null) {
                Glide.with(App.getInstance()).clear(target);
                target = null;
            }
            if (iconUrl.isPresent()) {
                target = ImageHelper.loadCarIconIntoMarker(App.getInstance(), iconUrl.get(), marker);
            } else {
                marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.car));
            }
        }
    }

    public void clear() {
        if (currentAnimationJob != null) {
            currentAnimationJob.cancel();
        }
        if (target != null) {
            Glide.with(App.getInstance()).clear(target);
            target = null;
        }
        marker.remove();
    }

    @Override
    public LatLng getPosition() {
        return marker.getPosition();
    }

    @Override
    public float getRotation() {
        return marker.getRotation();
    }

    @Override
    public void setRotation(float finalCourse) {
        marker.setRotation(finalCourse);
    }

    @Override
    public void setPosition(LatLng target) {
        marker.setPosition(target);
    }

}
