package com.rideaustin.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.Nullable;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.rideaustin.api.config.LocationHint;
import com.rideaustin.api.model.LocationHintCoord;
import com.rideaustin.utils.location.DistanceUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java8.util.Optional;
import rx.Observable;
import rx.subjects.BehaviorSubject;
import timber.log.Timber;

/**
 * Created by Sergey Petrov on 31/08/2017.
 */

public class LocationHintHelper {

    private static final int AROUND_DISTANCE = 20;
    private static final int EQUAL_DISTANCE = 1;

    public enum AreaType {
        PICKUP,
        DESTINATION
    }

    private Map<LatLng, LocationHintCoord> pickupsCache = new HashMap<>();
    private Map<LatLng, LocationHintCoord> destinationsCache = new HashMap<>();
    private List<LocationHint> pickupHints;
    private List<LocationHint> destinationHints;
    private @Nullable GoogleMap map;
    private @Nullable AreaType areaType;
    private @Nullable Marker selectedMarker;

    private List<Polygon> polygons = new ArrayList<>();
    private Map<LatLng, Marker> markers = new HashMap<>();
    private BehaviorSubject<LatLng> snappedLocation = BehaviorSubject.create();

    /**
     * This is called on UI thread.
     * If design will change, need to add synchronization
     */
    public void setHints(List<LocationHint> pickupHints, List<LocationHint> destinationHints) {
        this.pickupHints = pickupHints;
        this.destinationHints = destinationHints;
        pickupsCache.clear();
        destinationsCache.clear();
        drawHints(map, areaType);
    }

    /**
     * This is called on UI thread.
     * If design will change, need to add synchronization
     */
    @Nullable
    public LocationHintCoord findNearestLocationCoord(LatLng location, @Nullable AreaType areaType, boolean forceSnap) {
        LocationHintCoord coord = null;
        if (areaType == null || areaType.equals(AreaType.PICKUP)) {
            coord = findLocationCoord(location, pickupHints, pickupsCache, forceSnap);
        }
        if (coord == null && (areaType == null || areaType.equals(AreaType.DESTINATION))) {
            coord = findLocationCoord(location, destinationHints, destinationsCache, forceSnap);
        }
        return coord;
    }

    public void drawHints(@Nullable GoogleMap map, @Nullable AreaType areaType) {
        LatLng currentPosition = snappedLocation.getValue();
        clearHints();
        this.map = map;
        this.areaType = areaType;
        List<LocationHint> hints = areaType == AreaType.DESTINATION ? destinationHints : pickupHints;
        if (map != null && areaType != null && hints != null) {
            for (LocationHint hint : hints) {
                if (hint.getCoords() == null) {
                    continue;
                }
                int strokeColor = areaType == AreaType.DESTINATION ? 0xFFF44336 : 0xFF4CAF50;
                int fillColor = areaType == AreaType.DESTINATION ? 0x19F44336 : 0x194CAF50;
                PolygonOptions polygonOptions = new PolygonOptions();
                polygonOptions.zIndex(1);
                polygonOptions.strokeWidth(2);
                polygonOptions.strokeColor(strokeColor);
                polygonOptions.fillColor(fillColor);
                polygonOptions.addAll(hint.getBoundaries());
                polygons.add(map.addPolygon(polygonOptions));

                for (LocationHintCoord pickup : hint.getCoords()) {
                    LatLng pickupLocation = pickup.getDriverCoord().getLatLng();
                    boolean isSelected = isAround(pickupLocation, currentPosition, AROUND_DISTANCE);
                    Bitmap bitmap = getMarkerBitmap(areaType, false);
                    Marker marker = map.addMarker(new MarkerOptions()
                            .snippet(areaType.name())
                            .anchor(0.5f, 0.5f)
                            .position(pickupLocation)
                            .icon(BitmapDescriptorFactory.fromBitmap(bitmap)));
                    markers.put(pickupLocation, marker);
                    if (isSelected) {
                        selectedMarker = marker;
                        snappedLocation.onNext(pickupLocation);
                    }
                }
            }
        }
    }

    public void clearHints() {
        map = null;
        areaType = null;
        selectedMarker = null;
        snappedLocation.onNext(null);
        for (Polygon polygon : polygons) {
            polygon.remove();
        }
        polygons.clear();

        for (Marker marker : markers.values()) {
            marker.remove();
        }
        markers.clear();
    }

    public void snapToNearestLocation(@Nullable LatLng currentLocation, boolean forceSnap) {
        if (selectedMarker != null) {
            if (isAround(selectedMarker.getPosition(), currentLocation, EQUAL_DISTANCE)) {
                snappedLocation.onNext(selectedMarker.getPosition());
                return;
            }
            updateMarker(selectedMarker, false);
            selectedMarker = null;
        }
        if (currentLocation != null) {
            LocationHintCoord pickup = findNearestLocationCoord(currentLocation, areaType, forceSnap);
            if (pickup != null) {
                LatLng pickupLocation = pickup.getDriverCoord().getLatLng();
                Marker marker = markers.get(pickupLocation);
                if (marker != null) {
                    selectedMarker = marker;
                    updateMarker(marker, true);
                    snappedLocation.onNext(pickupLocation);
                    return;
                }
            }
        }
        snappedLocation.onNext(currentLocation);
    }

    public Observable<LatLng> observeSnappedLocation() {
        return snappedLocation.filter(it -> it != null)
                .serialize()
                .onBackpressureLatest();
    }

    public boolean isLocationAllowed(LatLng location, AreaType areaType) {
        List<LocationHint> hints = areaType == AreaType.DESTINATION ? destinationHints : pickupHints;
        if (hints != null) {
            for (LocationHint hint : hints) {
                if (hint.getCoords() == null) {
                    continue;
                }
                if (hint.contains(location)) {
                    for (LocationHintCoord pickup : hint.getCoords()) {
                        if (isAround(pickup.getDriverCoord().getLatLng(), location, AROUND_DISTANCE)) {
                            return true;
                        }
                    }
                    return false;
                }
            }
        }
        return true;
    }

    public String getHintName(LatLng location, String defaultName) {
        return Optional.ofNullable(findNearestLocationCoord(location, null, false)).map(LocationHintCoord::composeName).orElse(defaultName);
    }

    private LocationHintCoord findLocationCoord(LatLng location, List<LocationHint> hints, Map<LatLng, LocationHintCoord> cache, boolean forceSnap) {
        LocationHintCoord coord = cache.get(location);
        if (coord != null) {
            // return cached designated coord for that location
            return coord;
        }
        if (hints == null || hints.isEmpty()) {
            // no hints, return original location
            return null;
        }
        LocationHint hint = null;
        for (LocationHint h : hints) {
            if (h.contains(location)) {
                hint = h;
                break;
            }
        }

        if (hint == null || hint.getCoords() == null) {
            // location doesn't belong to any coord hint area
            return null;
        }
        int bestDistance = Integer.MAX_VALUE;
        LocationHintCoord bestCoord = null;
        for (LocationHintCoord p : hint.getCoords()) {
            int distance = getDistance(p.getDriverCoord().getLatLng(), location);
            boolean isMatch = forceSnap || distance < AROUND_DISTANCE;
            if (isMatch && distance < bestDistance) {
                bestDistance = distance;
                bestCoord = p;
                bestCoord.setAreaName(hint.getName());
            }
        }
        if (bestCoord != null) {
            if (!forceSnap) {
                // save nearest designated coord
                cache.put(location, bestCoord);
            }
            return bestCoord;
        }
        // nothing found
        return null;
    }

    private void updateMarker(Marker marker, boolean isSelected) {
        AreaType areaType = getAreaType(marker);
        if (areaType == null) {
            areaType = AreaType.PICKUP;
        }
        Bitmap bitmap = getMarkerBitmap(areaType, isSelected);
        marker.setIcon(BitmapDescriptorFactory.fromBitmap(bitmap));
    }

    private Bitmap getMarkerBitmap(AreaType areaType, boolean isSelected) {
        int radius = (int) ViewUtils.dpToPixels(isSelected ? 7.0f : 5.0f);
        int color = areaType == AreaType.DESTINATION ? 0xFFF44336 : 0xFF4CAF50;
        Bitmap bitmap = Bitmap.createBitmap(radius * 2, radius * 2, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        if (isSelected) {
            int strokeWidth = (int) ViewUtils.dpToPixels(3.0f);
            Paint paint = new Paint();
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(0xFFFFFFFF);
            canvas.drawCircle(radius, radius, radius, paint);
            paint.setStyle(Paint.Style.STROKE);
            paint.setAntiAlias(true);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setStrokeWidth(strokeWidth);
            paint.setColor(color);
            canvas.drawCircle(radius, radius, (int) (radius - Math.ceil(strokeWidth / 2)), paint);
        } else {
            Paint paint = new Paint();
            paint.setColor(color);
            canvas.drawCircle(radius, radius, radius, paint);
        }
        return bitmap;
    }

    private @Nullable AreaType getAreaType(Marker marker) {
        try {
            return AreaType.valueOf(marker.getSnippet());
        } catch (Exception e) {
            Timber.e(e);
            return null;
        }
    }

    private boolean isAround(LatLng start, LatLng finish, int distance) {
        return getDistance(start, finish) <= distance;
    }

    private int getDistance(LatLng start, LatLng finish) {
        if (start != null && finish != null) {
            return (int) DistanceUtil.distance(start, finish);
        }
        return Integer.MAX_VALUE;
    }
}
