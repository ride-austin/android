package com.rideaustin.utils.location;

import android.location.Address;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.annotation.WorkerThread;
import android.text.TextUtils;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.common.data.DataBufferUtils;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.AutocompletePredictionBuffer;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.rideaustin.App;
import com.rideaustin.entities.GeoPosition;
import com.rideaustin.utils.MathUtils;
import com.rideaustin.utils.PendingResultObservable;
import com.rideaustin.utils.RxponentialBackoffRetry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import java8.util.Optional;
import rx.Observable;
import timber.log.Timber;

/**
 * Created by vharshyn on 23.07.2016.
 */
public final class LocationHelper {

    private static final double MIN_LATITUDE = -90.0;
    private static final double MAX_LATITUDE = 90.0;
    private static final double MIN_LONGITUDE = -180.0;
    private static final double MAX_LONGITUDE = 180.0;
    private static final int MAX_LOCATIONS = 3;
    private static final String LOCATION_DELIMITER = " ";
    private static final String COMPONENT_DELIMITER = ",";

    @Nullable
    public static LatLng from(@NonNull String string, boolean latFirst) {
        if (string.contains(COMPONENT_DELIMITER)) {
            String[] components = string.split(COMPONENT_DELIMITER);
            if (components.length == 2) {
                Double lat = Double.valueOf(components[latFirst ? 0 : 1].trim());
                Double lng = Double.valueOf(components[latFirst ? 1 : 0].trim());
                if (lat != null && lng != null) {
                    return new LatLng(lat, lng);
                }
            }
        }
        return null;
    }

    @Nullable
    public static List<LatLng> listFrom(@NonNull String string, boolean latFirst) {
        if (string.contains(LOCATION_DELIMITER)) {
            String[] arr = string.split(LOCATION_DELIMITER);
            List<LatLng> list = null;
            for (String str : arr) {
                LatLng loc = from(str, latFirst);
                if (loc != null) {
                    if (list == null) {
                        list = new ArrayList<>(arr.length);
                    }
                    list.add(loc);
                }
            }
            return list;
        }
        return null;
    }


    /**
     * @param address  Input {@link Address} to query. Cannot be null;
     * @param maxLines Maximum number of lines to include. Should be 1 or more, otherwise it will cause IllegalArgumentException.
     *                 Safe to use {@link Integer#MAX_VALUE} to include all lines.
     * @return A Single line, comma separated address String. Won't be null or empty.
     * @throws IllegalArgumentException if address lines are empty or maxLines is invalid
     *                                  <p>
     *                                  use {@code {@link LocationHelper#getAddressString }}
     */
    @NonNull
    @Deprecated
    public static String getAddressString(@NonNull Address address, int maxLines) throws IllegalArgumentException {
        final int numLines = Math.min(maxLines, address.getMaxAddressLineIndex() + 1);
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < numLines; i++) {
            String line = address.getAddressLine(i);
            if (!TextUtils.isEmpty(line)) {
                if (builder.length() > 0) {
                    builder.append(", ");
                }
                builder.append(line);
            }
        }
        if (builder.length() == 0) {
            builder.append(address.getLocality());
        }
        if (builder.length() == 0) {
            throw new IllegalArgumentException("Address String cannot be resolved");
        } else {
            return builder.toString();
        }
    }


    /**
     * @param full    true if full address is required
     * @param address Cannot be null or empty
     * @return Single line short address or comma delimited full address.
     * @throws IllegalArgumentException if address cannot be resolved
     */
    public static String getAddressString(@NonNull Address address, boolean full) throws IllegalArgumentException {

        if (full) {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                String line = address.getAddressLine(i);
                if (!TextUtils.isEmpty(line)) {
                    if (builder.length() > 0) {
                        builder.append(", ");
                    }
                    builder.append(line);
                }
            }
            return builder.toString();
        } else {
            String thoroughfare = address.getThoroughfare();
            if (TextUtils.isEmpty(thoroughfare)) {
                if (!TextUtils.isEmpty(address.getSubLocality())) {
                    return address.getSubLocality();
                }
                if (!TextUtils.isEmpty(address.getLocality())) {
                    return address.getLocality();
                }
                if (!TextUtils.isEmpty(address.getAdminArea())) {
                    return address.getAdminArea();
                }
                if (!TextUtils.isEmpty(address.getCountryName())) {
                    return address.getCountryName();
                }
                throw new IllegalArgumentException("Address String cannot be resolved");

            } else {
                String subThoroughfare = address.getSubThoroughfare();
                if (!TextUtils.isEmpty(subThoroughfare) && !thoroughfare.startsWith(subThoroughfare)) {
                    thoroughfare = subThoroughfare + " " + thoroughfare;
                }
                return thoroughfare;
            }
        }
    }

    /**
     * @param full      true if full address is required
     * @param addresses Cannot be null or empty
     * @return Single line short address or comma delimited full address.
     * @throws IllegalArgumentException if address list is empty
     */
    public static Optional<String> getAddressString(@NonNull List<Address> addresses, boolean full) throws IllegalArgumentException {
        return filterBestAddress(addresses)
                .map(address -> getAddressString(address, full));
    }

    /**
     * @param candidateAddresses
     * @return The best address.
     * Tries to find the address with both PostalCode and Street Address (called Thoroughfare in API),
     * if not then tries to find an address with PostalCode
     * if not then tries to find an address with Street Address
     * if not returns the first address in element
     * if not throws Exception
     * @throws IllegalArgumentException if list is empty or null
     */
    @NonNull
    public static Optional<Address> filterBestAddress(List<Address> candidateAddresses) throws IllegalArgumentException {
        Address bestAddress = null;
        if (candidateAddresses.size() == 1) {
            bestAddress = candidateAddresses.get(0);
        } else {
            List<Address> zips = new ArrayList<>();
            List<Address> streets = new ArrayList<>();

            for (Address address : candidateAddresses) {
                if (!TextUtils.isEmpty(address.getPostalCode()) && !TextUtils.isEmpty(address.getThoroughfare())) {
                    bestAddress = address;
                    break;
                }
                if (!TextUtils.isEmpty(address.getPostalCode())) {
                    zips.add(address);
                }
                if (!TextUtils.isEmpty(address.getThoroughfare())) {
                    streets.add(address);
                }
            }
            if (zips.size() > 0) {
                bestAddress = zips.get(0);
            }
            if (streets.size() > 0) {
                bestAddress = streets.get(0);
            }
            if (candidateAddresses.size() > 0) {
                bestAddress = candidateAddresses.get(0);
            }
        }
        return Optional.ofNullable(bestAddress);
    }

    @WorkerThread
    @NonNull
    public static Optional<List<Address>> getFromLocation(LatLng location) throws IOException {
        return getFromLocation(location.latitude, location.longitude);
    }

    @WorkerThread
    @NonNull
    public static Optional<List<Address>> getFromLocation(double latitude, double longitude) throws IOException {
        return Optional.ofNullable(App.getGeocoder().getFromLocation(latitude, longitude, MAX_LOCATIONS));
    }

    /**
     * Don't forget to catch error.
     */
    public static Observable<Optional<String>> getZipCodeOrFail(LatLng latLng) {
        Timber.d("::getZipCode::");
        return Observable.
                fromCallable(() -> LocationHelper.getFromLocation(latLng)
                        .flatMap(LocationHelper::filterBestAddress)
                        .map(Address::getPostalCode))
                .retryWhen(new RxponentialBackoffRetry().getNotificationHandler());
    }

    public static Observable<Optional<String>> getZipCode(LatLng latLng) {
        Timber.d("::getZipCode::");
        return Observable.fromCallable(() -> LocationHelper.getFromLocation(latLng)
                .flatMap(LocationHelper::filterBestAddress)
                .map(Address::getPostalCode));
    }

    public static Observable<GeoPosition> getPlaceById(GoogleApiClient apiClient, String placeId) {
        return getCustomPlaces(placeId)
                .switchIfEmpty(placeByIdProvider.get(apiClient, placeId));
    }

    public static Observable<List<AutocompletePrediction>> getAutocompletePredictions(GoogleApiClient apiClient, String query, LatLngBounds bounds, AutocompleteFilter filter) {
        return autocompletePredictionsProvider.get(apiClient, query, bounds, filter);
    }

    public static Observable<GeoPosition> loadBestAddress(final LatLng latLng) {
        return Observable
                .defer(() -> {
                    try {
                        return LocationHelper.getFromLocation(latLng)
                                .flatMap(LocationHelper::filterBestAddress)
                                .map(address -> {
                                    String addressLine = LocationHelper.getAddressString(address, false);
                                    String fullAddress = LocationHelper.getAddressString(address, true);
                                    // during address resolution, best address might have
                                    // location different from what user selected
                                    // so in geo position we use original location
                                    GeoPosition geoPosition = new GeoPosition(latLng, addressLine, fullAddress);
                                    geoPosition.setZipCode(address.getPostalCode());
                                    return Observable.just(geoPosition);
                                })
                                .orElse(Observable.error(new IOException("can't get address for location: " + latLng)));
                    } catch (IllegalArgumentException | IOException e) {
                        return Observable.error(e);
                    }
                }).retryWhen(new RxponentialBackoffRetry().getNotificationHandler());
    }

    public static boolean isLocationNotEmpty(LatLng location) {
        return location != null && (int)(location.latitude) != 0 && (int)(location.longitude) != 0;
    }

    public static boolean isLocationValid(LatLng location) {
        return isLocationNotEmpty(location)
                && location.latitude >= MIN_LATITUDE
                && location.latitude <= MAX_LATITUDE
                && location.longitude >= MIN_LONGITUDE
                && location.longitude <= MAX_LONGITUDE;
    }

    public static boolean areLocationsEqual(@Nullable LatLng location, @Nullable LatLng other) {
        if (location == other) {
            return true;
        }
        return location != null && other != null
                && MathUtils.almostEqual(location.latitude, other.latitude, 1E-6)
                && MathUtils.almostEqual(location.longitude, other.longitude, 1E-6);
    }


    public static boolean isLocationNotEmpty(Location location) {
        return location != null && (int)(location.getLatitude()) != 0 && (int)(location.getLongitude()) != 0;
    }

    public static boolean isLocationValid(Location location) {
        return isLocationNotEmpty(location)
                && location.getLatitude() >= MIN_LATITUDE
                && location.getLatitude() <= MAX_LATITUDE
                && location.getLongitude() >= MIN_LONGITUDE
                && location.getLongitude() <= MAX_LONGITUDE;
    }

    private static Observable<GeoPosition> getCustomPlaces(String placeId) {
        return CustomLocations.fromPlaceId(placeId);
    }

    private static Observable<GeoPosition> getPlaceFromGeoApi(GoogleApiClient apiClient, String placeId) {
        return Observable.create(new PendingResultObservable<>(Places.GeoDataApi.getPlaceById(apiClient, placeId)))
                .flatMap(places -> {
                    GeoPosition geoPosition = null;
                    if (places.getStatus().isSuccess() && places.getCount() > 0) {
                        Place place = places.get(0).freeze();
                        geoPosition = new GeoPosition(
                                place.getLatLng(),
                                place.getAddress().toString(),
                                place.getAddress().toString()
                        );
                        geoPosition.setPlaceId(place.getId());
                    }
                    places.release();
                    if (geoPosition == null) {
                        return Observable.error(new IOException("Place Cannot be found"));
                    } else {
                        return Observable.just(geoPosition);
                    }
                })
                .retryWhen(new RxponentialBackoffRetry().getNotificationHandler());
    }

    private static Observable<List<AutocompletePrediction>> getAutocompletePredictionsFromGeoApi(GoogleApiClient apiClient, String query, LatLngBounds bounds, AutocompleteFilter filter) {
        return Observable.fromCallable(() -> {
            // Submit the query to the autocomplete API and retrieve a PendingResult that will
            // contain the results when the query completes.
            PendingResult<AutocompletePredictionBuffer> results = Places.GeoDataApi.getAutocompletePredictions(apiClient, query, bounds, filter);
            // This method should have been called off the main UI thread. Block and wait for at most 10s
            // for a result from the API.
            AutocompletePredictionBuffer autocompletePredictions = results.await(10, TimeUnit.SECONDS);

            // Confirm that the query completed successfully, otherwise return null
            final Status status = autocompletePredictions.getStatus();
            if (!status.isSuccess()) {
                if (!status.isCanceled()) {
                    autocompletePredictions.release();
                    throw new RuntimeException("Unable to get place prediction: " + status.toString());
                }
                return null;
            }

            // Freeze the results immutable representation that can be stored safely.
            return DataBufferUtils.freezeAndClose(autocompletePredictions);
        });
    }

    @VisibleForTesting
    public static PlaceByIdProvider placeByIdProvider = LocationHelper::getPlaceFromGeoApi;

    @VisibleForTesting
    public static AutocompletePredictionsProvider autocompletePredictionsProvider = LocationHelper::getAutocompletePredictionsFromGeoApi;

    @VisibleForTesting
    public interface PlaceByIdProvider {
        Observable<GeoPosition> get(GoogleApiClient apiClient, String placeId);
    }

    @VisibleForTesting
    public static void resetProviders() {
        placeByIdProvider = LocationHelper::getPlaceFromGeoApi;
        autocompletePredictionsProvider = LocationHelper::getAutocompletePredictionsFromGeoApi;
    }

    @VisibleForTesting
    public interface AutocompletePredictionsProvider {
        Observable<List<AutocompletePrediction>> get(GoogleApiClient apiClient, String query, LatLngBounds bounds, AutocompleteFilter filter);
    }
}
