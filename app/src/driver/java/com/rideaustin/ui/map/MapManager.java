package com.rideaustin.ui.map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.text.TextUtils;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.rideaustin.App;
import com.rideaustin.R;
import com.rideaustin.api.config.RiderLiveLocation;
import com.rideaustin.api.model.surgearea.SurgeArea;
import com.rideaustin.engine.EngineState;
import com.rideaustin.manager.location.RALocation;
import com.rideaustin.manager.location.RALocationType;
import com.rideaustin.ui.utils.SurgeAreaUtils;
import com.rideaustin.ui.utils.UIUtils;
import com.rideaustin.utils.Constants;
import com.rideaustin.utils.ImageHelper;
import com.rideaustin.utils.MapUtils;
import com.rideaustin.utils.TimeUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import java8.util.Optional;
import timber.log.Timber;

/**
 * Created by rost on 8/15/16.
 */
public class MapManager {
    private GoogleMap googleMap;
    private final Context context;
    private final MapDimensionsProvider provider;
    private final MapViewModel mapViewModel;
    @Nullable
    private Marker pickupMarker;
    @Nullable
    private Marker destinationMarker;
    @Nullable
    private Marker nextRideMarker;
    @Nullable
    private RiderMarker riderMarker;
    private CarMarker driverMarker;
    private Polyline direction;
    private final int paddingForMarker;
    private long locationReceivedTime;
    private long dt = -1;
    private long otherDriversPrevTime = -1;
    private long otherDriversDt = -1;
    private Map<RALocationType, CarMarker> otherDriversMarkers = new HashMap<>();
    private int routeStrokeWidth;
    private int routeStrokeColor;

    private List<Pair<Polygon, Marker>> surgeAreasOverlay = new ArrayList<>();

    public MapManager(Context context, @NonNull MapDimensionsProvider provider, MapViewModel mapViewModel) {
        this.context = context;
        this.provider = provider;
        this.mapViewModel = mapViewModel;
        paddingForMarker = context.getResources().getDimensionPixelSize(R.dimen.map_padding_for_marker);
        routeStrokeWidth = context.getResources().getDimensionPixelSize(R.dimen.map_route_stroke_width);
        routeStrokeColor = ContextCompat.getColor(context, R.color.map_route_stroke_color);
    }

    public void setGoogleMap(GoogleMap googleMap) {
        this.googleMap = googleMap;
        updatePaddings();
    }

    public void updatePaddings() {
        if (googleMap != null) {
            googleMap.setPadding(0, provider.getMapTopPadding(), 0, provider.getMapBottomPadding());
        }
    }

    public boolean isMapReady() {
        return googleMap != null;
    }

    public void showDriver(RALocation location, boolean zoomToDriver) {
        updateDriverMarker(location);
        if (zoomToDriver) {
            showDriverLocation(location.getCoordinates(), false);
        }
    }

    public void setSurgeAreasResponsesList(List<SurgeArea> surgeAreasResponsesList) {
        clearPolygonGroundOverlay();
        Set<String> categories = App.getInstance().getRideRequestManager().getSelectedCategories();
        for (SurgeArea surgeArea : surgeAreasResponsesList) {
            float highestFactor = SurgeAreaUtils.getHighestFactor(surgeArea, categories);
            if (!TextUtils.isEmpty(surgeArea.getCsvGeometry()) && highestFactor > 1f) {
                PolygonOptions surgeAreaPolygon = new PolygonOptions();
                surgeAreaPolygon.zIndex(1);
                surgeAreaPolygon.strokeWidth(5);
                int color = SurgeAreaUtils.getSurgeAreaColor(highestFactor);
                surgeAreaPolygon.strokeColor(color);
                surgeAreaPolygon.fillColor(color);
                List<LatLng> points = SurgeAreaUtils.parseCSVGeometry(surgeArea.getCsvGeometry());
                LatLng centerPoint = getPolygonCenterPoint(points);
                surgeAreaPolygon.addAll(points);
                surgeAreasOverlay.add(new Pair<>(googleMap.addPolygon(surgeAreaPolygon), addTextOverlay(centerPoint, UIUtils.formatSurgeFactor(highestFactor), 0, 18)));
            }
        }
    }

    public void setPolygonGroundOverlayVisibility(boolean visibility) {
        for (Pair<Polygon, Marker> polygonGroundOverlayPair : surgeAreasOverlay) {
            if (polygonGroundOverlayPair.first != null) {
                polygonGroundOverlayPair.first.setVisible(visibility);
            }
            if (polygonGroundOverlayPair.second != null) {
                polygonGroundOverlayPair.second.setVisible(visibility);
            }
        }
    }

    public void clearPolygonGroundOverlay() {
        for (Pair<Polygon, Marker> polygonGroundOverlayPair : surgeAreasOverlay) {
            if (polygonGroundOverlayPair.first != null) {
                polygonGroundOverlayPair.first.remove();
            }
            if (polygonGroundOverlayPair.second != null) {
                polygonGroundOverlayPair.second.remove();
            }
        }
    }

    private void updateDriverMarker(RALocation location) {
        long currentTime = TimeUtils.currentTimeMillis();
        dt = calcDt(currentTime, locationReceivedTime, dt);
        if (dt > Constants.ANIMATION_FILTER_TIME_MS) {
            if (driverMarker == null) {
                driverMarker = new DriverCarMarker(context, googleMap, location, Constants.NON_TRANSPARENT);
            } else {
                driverMarker.update(location, dt);
                boolean invalidData = location.getLocation().getAccuracy() < 0
                        || location.getLocation().getAccuracy() > Constants.LOCATION_HORIZONTAL_ACCURACY_FILTER;
                if (location.getOwner().isThisDeviceLocation() && !invalidData && mapViewModel.isTrackingDriverLocation()) {
                    repositionMarker(location.getCoordinates());
                }
            }
            locationReceivedTime = currentTime;
        }
    }

    /**
     * Shows current driver location.
     * Zoom depends on current state and shows:
     * <ul>
     * <li>driver and pickup in ACCEPTED/ARRIVED state if pickup is set</li>
     * <li>driver and destination in ACTIVE state if destination is set</li>
     * <li>only driver if ride exists but no markers</li>
     * <li>driver if no ride exist and map is not scrolled by driver</li>
     * </ul>
     *
     * @param location        driver's current location
     * @param userInteraction whether it was initiated by user
     */
    private void showDriverLocation(LatLng location, boolean userInteraction) {
        EngineState.Type type = App.getInstance().getStateManager().getCurrentEngineStateType();
        switch (type) {
            case ACCEPTED:
            case ARRIVED:
                if (pickupMarker == null) {
                    moveCameraToLocation(location);
                } else {
                    zoomToFit(location, pickupMarker.getPosition());
                }
                break;
            case STARTED:
                if (destinationMarker == null) {
                    moveCameraToLocation(location);
                } else {
                    zoomToFit(location, destinationMarker.getPosition());
                }
                break;
            default:
                if (userInteraction || mapViewModel.isTrackingDriverLocation()) {
                    // track driver location (with animation)
                    moveCameraToLocation(location);
                }
        }
    }

    private void repositionMarker(LatLng finalPosition) {
        if (googleMap != null) {
            updatePaddings();
            boolean contains = googleMap.getProjection().getVisibleRegion().latLngBounds.contains(finalPosition);
            if (!contains) {
                moveCameraToLocation(finalPosition);
            }
        }
    }

    private void showLocation(LatLng position) {
        if (googleMap != null && position != null) {
            int width = provider.getMapWidth();
            int height = provider.getMapHeight();
            // RA-9612: map should have dimensions
            if (width > 0 && height > 0) {
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, Constants.DEFAULT_CAMERA_ZOOM));
            }
        }
    }

    private void moveCameraToLocation(LatLng location) {
        if (googleMap != null) {
            int width = provider.getMapWidth();
            int height = provider.getMapHeight();
            // RA-9612: map should have dimensions
            if (width > 0 && height > 0) {
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(location, Constants.DEFAULT_CAMERA_ZOOM);
                googleMap.animateCamera(cameraUpdate);
            }
        }
    }

    public void showOtherDrivers(List<RALocation> locations) {
        clearOtherDrivers();
        final long currentTime = TimeUtils.currentTimeMillis();
        otherDriversDt = calcDt(currentTime, otherDriversPrevTime, otherDriversDt);

        final Set<RALocationType> receivedOwners = new HashSet<>();
        for (RALocation location : locations) {
            receivedOwners.add(location.getOwner());
        }

        final Set<RALocationType> unusedOwners = new HashSet<>(otherDriversMarkers.keySet());
        unusedOwners.removeAll(receivedOwners);

        for (RALocationType owner : unusedOwners) {
            otherDriversMarkers.get(owner).clear();
            otherDriversMarkers.remove(owner);
        }

        for (RALocation driverLocation : locations) {
            final RALocationType owner = driverLocation.getOwner();

            CarMarker markerInfo = otherDriversMarkers.get(owner);
            if (markerInfo == null) {
                createOtherDriverMarker(driverLocation, owner);
            } else {
                markerInfo.update(driverLocation, otherDriversDt);
                if (driverLocation.getOwner().isThisDeviceLocation() && mapViewModel.isTrackingDriverLocation()) {
                    repositionMarker(driverLocation.getCoordinates());
                }
            }
        }

        otherDriversPrevTime = currentTime;
    }

    private void createOtherDriverMarker(RALocation driverLocation, RALocationType owner) {
        // try-catch added because of https://issue-tracker.devfactory.com/browse/RA-8651
        CarMarker markerInfo;
        try {
            markerInfo = new CarMarker(context, googleMap, driverLocation, Constants.SEMI_TRANSPARENT, Optional.empty());
            otherDriversMarkers.put(owner, markerInfo);
        } catch (Exception e) {
            Timber.e(e, e.getMessage());
        }
    }

    public void clearOtherDrivers() {
        try {
            for (CarMarker info : otherDriversMarkers.values()) {
                info.clear();
            }
            otherDriversMarkers.clear();
        } catch (Exception e) {
            Timber.w(e, "Can't remove other drivers from map");
        }

    }

    private static long calcDt(long curTime, long prevTime, long prevDt) {
        long curDt = curTime - prevTime;
        if (curDt > Constants.MAX_CHANGE_CAR_POSITION_ANIMATION_DURATION) {
            curDt = Constants.MAX_CHANGE_CAR_POSITION_ANIMATION_DURATION;
        }

        if (prevDt > 0) {
            return (curDt * 2 + prevDt * 1) / 3;
        } else {
            return curDt;
        }
    }

    public void hideDriverIcon() {
        if (driverMarker != null) {
            driverMarker.clear();
            driverMarker = null;
        }
    }

    public void showRide(List<LatLng> direction, @NonNull LatLng pickup, @Nullable LatLng destination) {
        hideRide();
        addOrUpdatePickupMarker(pickup);
        if (destination != null) {
            addOrUpdateDestinationMarker(destination);
        }
        this.direction = drawDirectionOnMap(direction);
    }

    public void hideRide() {
        removePickupMarker();
        removeDestinationMarker();

        if (direction != null) {
            direction.remove();
            MapUtils.hasRoute = false;
        }
    }

    public void removePickupMarker() {
        if (pickupMarker != null) {
            pickupMarker.remove();
            pickupMarker = null;
        }
        if (riderMarker != null) {
            riderMarker.remove();
            riderMarker = null;
        }
    }

    public void removeDestinationMarker() {
        if (destinationMarker != null) {
            destinationMarker.remove();
            destinationMarker = null;
        }
    }

    @Nullable
    public LatLng getPickupLocation() {
        if (pickupMarker == null) {
            return null;
        }
        return pickupMarker.getPosition();
    }

    @Nullable
    public LatLng getDestinationLocation() {
        if (destinationMarker == null) {
            return null;
        }
        return destinationMarker.getPosition();
    }

    public void addOrUpdatePickupMarker(@NonNull LatLng location) {
        pickupMarker = addOrUpdateMarker(pickupMarker, location, R.drawable.icon_green, R.string.pickup_marker);
    }

    public void addOrUpdatePickupMarker(@NonNull LatLng location, @DrawableRes int icon) {
        pickupMarker = addOrUpdateMarker(pickupMarker, location, icon, R.string.pickup_marker);
    }

    public void addOrUpdateDestinationMarker(@NonNull LatLng location) {
        destinationMarker = addOrUpdateMarker(destinationMarker, location, R.drawable.icon_red, R.string.destination_marker);
    }

    public void addOrUpdateNextRideMarker(@NonNull LatLng location) {
        nextRideMarker = addOrUpdateMarker(nextRideMarker, location, R.drawable.blue_pin, R.string.next_ride_marker);
    }

    private Marker addOrUpdateMarker(@Nullable Marker oldMarker, @NonNull LatLng location, @DrawableRes int resId, @StringRes int description) {
        if (oldMarker != null) {
            // check position not changed
            if (oldMarker.getPosition() != null && oldMarker.getPosition().equals(location)) {
                // return the same marker
                return oldMarker;
            } else {
                // otherwise its better to recreate marker
                // since it could disappear during map animation
                oldMarker.remove();
            }
        }
        // create new marker
        return googleMap.addMarker(new MarkerOptions()
                .title(context.getString(description))
                .position(location)
                .icon(BitmapDescriptorFactory.fromBitmap(ImageHelper.createBitmap(context, resId))));
    }


    private Polyline drawDirectionOnMap(List<LatLng> direction) {
        PolylineOptions way = new PolylineOptions().width(routeStrokeWidth).color(routeStrokeColor).geodesic(true);
        for (int i = 0; i < direction.size(); i++) {
            LatLng point = direction.get(i);
            way.add(point);
        }
        MapUtils.hasRoute = !direction.isEmpty();
        return googleMap.addPolyline(way);
    }

    private void zoomOutCamera(@Nullable final LatLng pickup, @Nullable final LatLng destination) {
        // if both points are null, nothing to do here
        if (pickup == null && destination == null) {
            return;
        }
        int width = provider.getMapWidth();
        int height = provider.getMapHeight();
        // RA-9612: map should have dimensions
        if (width > 0 && height > 0) {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            if (pickup != null) {
                builder.include(pickup);
            }
            if (destination != null) {
                builder.include(destination);
            }
            LatLngBounds bounds = builder.build();

            // Add only left/right paddingForMarker
            // Bottom location marker requires no padding (car may trim but it's rare)
            // As for top space, seems google reserve some space internally for this purpose
            googleMap.setPadding(paddingForMarker, provider.getMapTopPadding(), paddingForMarker, paddingForMarker + provider.getMapBottomPadding());
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 0);
            googleMap.animateCamera(cameraUpdate);
            updatePaddings();
        }
    }

    private LatLng getPolygonCenterPoint(List<LatLng> polygonPointsList) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (int i = 0; i < polygonPointsList.size(); i++) {
            builder.include(polygonPointsList.get(i));
        }

        return builder.build().getCenter();
    }

    public Marker addTextOverlay(LatLng location, String text, int padding, int fontSize) {
        if ((location == null) || (text == null) || (fontSize <= 0)) {
            return null;
        }

        TextView textView = new TextView(context);
        textView.setText(text);
        textView.setTextSize(fontSize);

        Paint paintText = textView.getPaint();

        Rect boundsText = new Rect();
        paintText.getTextBounds(text, 0, textView.length(), boundsText);
        paintText.setTextAlign(Paint.Align.CENTER);

        Bitmap.Config conf = Bitmap.Config.ARGB_8888;
        Bitmap bmpText = Bitmap.createBitmap(boundsText.width() + 2
                * padding, boundsText.height() + 2 * padding, conf);
        Canvas canvasText = new Canvas(bmpText);
        paintText.setColor(Color.WHITE);

        canvasText.drawText(text, (float) canvasText.getWidth() / 2,
                (float) (canvasText.getHeight() - padding - boundsText.bottom), paintText);

        MarkerOptions markerOptions = new MarkerOptions()
                .title(text)
                .position(location)
                .icon(BitmapDescriptorFactory.fromBitmap(bmpText))
                .anchor(0.5f, 1);
        return googleMap.addMarker(markerOptions);
    }

    public void moveToDriverLocation() {
        if (driverMarker != null) {
            LatLng myLocation = driverMarker.getPosition();
            if (myLocation != null) {
                showDriverLocation(myLocation, true);
            }
        }
    }

    public void showPickupLocationOnMap(LatLng latLng) {
        addOrUpdatePickupMarker(latLng);
    }

    public void showDestinationLocationOnMap(Optional<LatLng> latLng) {
        if (latLng.isPresent()) {
            addOrUpdateDestinationMarker(latLng.get());
        }
    }

    public void showNextRideLocationOnMap(Optional<LatLng> latLng) {
        if (latLng.isPresent()) {
            addOrUpdateNextRideMarker(latLng.get());
        } else if (nextRideMarker != null) {
            nextRideMarker.remove();
            nextRideMarker = null;
        }
    }

    public void zoomToFit(LatLng latLng1, LatLng latLng2) {
        zoomOutCamera(latLng1, latLng2);
    }

    public void addOrUpdateRiderMarker(LatLng latLng) {
        if (riderMarker != null && riderMarker.isVisible()) {
            riderMarker.updatePosition(latLng);
        } else {
            RiderLiveLocation riderLiveLocation = App.getConfigurationManager().getLastConfiguration().getRiderLiveLocation();
            riderMarker = new RiderMarker(context, googleMap, latLng, riderLiveLocation.getExpirationTime());
        }
    }

    /**
     * Provides map dimensions and paddings.
     * Used to calculate visible area for markers in order to prevent marker fall behind other ui items.
     */
    interface MapDimensionsProvider {
        int getMapWidth();
        int getMapHeight();
        int getMapTopPadding();
        int getMapBottomPadding();
    }
}
