package com.rideaustin.ui.drawer.favorite;

import android.Manifest;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.rideaustin.App;
import com.rideaustin.R;
import com.rideaustin.base.BaseFragment;
import com.rideaustin.databinding.FragmentFavoritesBinding;
import com.rideaustin.entities.GeoPosition;
import com.rideaustin.manager.LocationSettingsException;
import com.rideaustin.manager.MissingLocationPermissionException;
import com.rideaustin.manager.location.RALocation;
import com.rideaustin.schedulers.RxSchedulers;
import com.rideaustin.ui.map.PlaceAutoCompleteAdapter;
import com.rideaustin.utils.Constants;
import com.rideaustin.utils.KeyboardUtil;
import com.rideaustin.utils.LocationHintHelper;
import com.rideaustin.utils.PermissionUtils;
import com.rideaustin.utils.toast.RAToast;
import com.tbruyelle.rxpermissions.RxPermissions;

import java8.util.Optional;
import rx.Subscription;
import rx.subscriptions.Subscriptions;
import timber.log.Timber;

import static com.rideaustin.ui.drawer.favorite.FavoritesViewModel.TYPE_DESTINATION;
import static com.rideaustin.ui.drawer.favorite.FavoritesViewModel.TYPE_HOME;
import static com.rideaustin.ui.drawer.favorite.FavoritesViewModel.TYPE_PICKUP;
import static com.rideaustin.ui.drawer.favorite.FavoritesViewModel.TYPE_WORK;
import static com.rideaustin.ui.map.MainMapFragment.LOCATION_RESOLUTION_REQUEST;

/**
 * Created by crossover on 03/07/2017.
 */

public class FavoritesMapFragment extends BaseFragment implements OnMapReadyCallback, FavoritesView {
    static final String FAVORITE_TYPE = "FAVORITE_TYPE";
    static final String GEOPOSITION = "GEOPOSITION";

    private Subscription permissionSubscription = Subscriptions.empty();
    private Subscription myLocationSubscription = Subscriptions.empty();
    private FavoritesViewModel viewModel;
    private GoogleMap gMap;
    private GoogleApiClient apiClient;
    private FragmentFavoritesBinding binding;
    private PlaceAutoCompleteAdapter autoCompleteAdapter;
    private AdapterView.OnItemClickListener itemClickListener;
    public FavoritesMapListener listener;


    private static FavoritesMapFragment newInstance(@FavoritesViewModel.FavoriteType String type, @Nullable GeoPosition geoPosition) {
        FavoritesMapFragment fragment = new FavoritesMapFragment();
        Bundle bundle = new Bundle();
        bundle.putString(FAVORITE_TYPE, type);
        if (geoPosition != null) {
            bundle.putParcelable(GEOPOSITION, geoPosition);
        }
        fragment.setArguments(bundle);
        return fragment;
    }

    public static FavoritesMapFragment newHomeInstance() {
        return newInstance(TYPE_HOME, null);
    }

    public static FavoritesMapFragment newWorkInstance() {
        return newInstance(TYPE_WORK, null);
    }

    public static FavoritesMapFragment newPickupInstance(@Nullable GeoPosition geoPosition) {
        return newInstance(TYPE_PICKUP, geoPosition);
    }

    public static FavoritesMapFragment newDestinationInstance(@Nullable GeoPosition geoPosition) {
        return newInstance(TYPE_DESTINATION, geoPosition);
    }

    public interface FavoritesMapListener {
        void onFavoritePlaceSelected(@FavoritesViewModel.FavoriteType String favoriteType,
                                     @Nullable GeoPosition selectedAddress);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        String type = Optional.ofNullable(arguments).map(it -> it.getString(FAVORITE_TYPE)).orElse(TYPE_HOME);
        GeoPosition geoPosition = Optional.ofNullable(arguments).map(it -> (GeoPosition) it.getParcelable(GEOPOSITION)).orElse(null);
        setHasOptionsMenu(true);
        viewModel = new FavoritesViewModel(this, type, geoPosition);
        apiClient = new GoogleApiClient.Builder(getContext())
                .addOnConnectionFailedListener(connectionResult -> Timber.e("Google api connection failed"))
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(@Nullable Bundle bundle) {
                        Timber.i("GooglePlayServices onConnected");
                    }

                    @Override
                    public void onConnectionSuspended(int code) {
                        Timber.i("GooglePlayServices connection suspended: %d", code);
                    }
                })
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .build();
        itemClickListener = (adapterView, view, position, id) -> {
            final AutocompletePrediction item = autoCompleteAdapter.getItem(position);
            if (item != null) {
                viewModel.addressItemSelected(apiClient, getCallback(), item);
            }
        };
        autoCompleteAdapter = new PlaceAutoCompleteAdapter(getContext(), apiClient);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setToolbarTitleAligned(viewModel.getTitleRes(), Gravity.CENTER);
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_favorites, container, false);
        binding.mapView.onCreate(savedInstanceState);
        binding.setViewModel(viewModel);

        binding.pickupAddress.setOnItemClickListener(itemClickListener);
        binding.pickupAddress.setAdapter(autoCompleteAdapter);
        binding.pickupAddress.setOnClickListener(v -> {
            setEditableInput(true);
            KeyboardUtil.showKeyBoard(getContext(), binding.pickupAddress);
        });
        binding.buttonDone.setOnClickListener(v -> onDoneClicked());
        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!App.getDataManager().isLoggedIn()) {
            // no sense to proceed
            return;
        }
        viewModel.onStart();
        binding.mapView.onStart();
        apiClient.connect();
        askForPermissions();
    }

    @Override
    public void onResume() {
        super.onResume();
        binding.mapView.onResume();
        if (PermissionUtils.isLocationPermissionGranted(getActivity())) {
            binding.mapView.getMapAsync(FavoritesMapFragment.this);
            binding.mapView.setVisibility(View.VISIBLE);
        }
        if (!App.getLocationManager().isLocationOn()) {
            RAToast.show(R.string.please_enable_gps_msg, Toast.LENGTH_LONG);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (binding != null) {
            binding.mapView.onSaveInstanceState(outState);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        binding.mapView.onPause();
        App.getDataManager().getLocationHintHelper().clearHints();
    }

    @Override
    public void onStop() {
        super.onStop();
        binding.mapView.onStop();
        viewModel.onStop();
        apiClient.disconnect();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (binding != null) {
            binding.mapView.onDestroy();
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (binding != null) {
            binding.mapView.onLowMemory();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.favorites_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.myLocationButton:
                onMyLocationClick();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;
        googleMap.getUiSettings().setMapToolbarEnabled(false);
        googleMap.getUiSettings().setAllGesturesEnabled(false);
        googleMap.getUiSettings().setRotateGesturesEnabled(false);
        googleMap.getUiSettings().setTiltGesturesEnabled(false);
        googleMap.getUiSettings().setScrollGesturesEnabled(true);
        googleMap.getUiSettings().setZoomGesturesEnabled(true);

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(PermissionUtils.isLocationPermissionGranted(getActivity()));
        }
        googleMap.setOnCameraIdleListener(() -> viewModel.onCameraChange(googleMap.getCameraPosition().target));
        binding.touchWrapper.setListener(new FavoritesTouchWrapperListener(googleMap) {

            @Override
            public void onTouched() {
                if (binding.pickupAddress.hasFocus()) {
                    binding.pickupAddress.clearFocus();
                    KeyboardUtil.hideKeyBoard(getActivity(), binding.pickupAddress);
                }
            }

            @Override
            public void onTouchReleased(boolean showMarker) {
                if (showMarker) {
                    viewModel.setCameraMoving(false);
                }
            }

            @Override
            public void onTouchMoved() {
                viewModel.setCameraMoving(true);
            }
        });
        GeoPosition position = viewModel.getSelectedAddress();
        if (position == null) {
            onMyLocationClick();
        } else {
            moveCamera(position.getLatLng());
            setAdapterBounds(position.getLatLng());
        }
        LocationHintHelper.AreaType areaType = viewModel.getAreaType();
        if (areaType != null) {
            App.getDataManager().getLocationHintHelper().drawHints(googleMap, areaType);
            untilPause(viewModel.observeSelectedLocation()
                    .observeOn(RxSchedulers.main())
                    .subscribe(it -> {
                        App.getDataManager().getLocationHintHelper().snapToNearestLocation(it, false);
                    }));
            untilPause(App.getDataManager().getLocationHintHelper().observeSnappedLocation()
                    .observeOn(RxSchedulers.main())
                    .subscribe(it -> {
                        if (gMap != null) {
                            gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(it, Constants.DEFAULT_CAMERA_ZOOM), 200, null);
                        }
                        setAdapterBounds(it);
                    }));
        }
    }

    private void onDoneClicked() {
        if (viewModel.getSelectedAddress() == null || listener == null) {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
            return;
        }
        if (viewModel.getAreaType() != null) {
            LatLng location = viewModel.getSelectedAddress().getLatLng();
            if (!App.getDataManager().getLocationHintHelper().isLocationAllowed(location, viewModel.getAreaType())) {
                App.getDataManager().getLocationHintHelper().snapToNearestLocation(location, true);
                return;
            }
        }
        viewModel.onSave(listener);
    }

    private void setAdapterBounds(LatLng latLng) {
        if (autoCompleteAdapter != null) {
            LatLngBounds bounds = LatLngBounds.builder().include(latLng).build();
            autoCompleteAdapter.setBounds(bounds);
        }
    }

    private void askForPermissions() {
        if (!PermissionUtils.isLocationPermissionGranted(getActivity())) {
            permissionSubscription.unsubscribe();
            permissionSubscription = new RxPermissions(getActivity())
                    .request(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
                    .subscribe(isGranted -> {
                        if (isGranted) {
                            binding.mapView.getMapAsync(FavoritesMapFragment.this);
                            binding.mapView.setVisibility(View.VISIBLE);
                            App.getConfigurationManager().onLocationPermissionGranted();
                        } else {
                            RAToast.showShort(getString(R.string.dont_have_permission, App.getAppName()));
                        }
                    });
        }
    }

    /**
     * Request to show my location if location permission granted.
     * Otherwise, ask for location permission.
     */
    private void onMyLocationClick() {
        if (PermissionUtils.isLocationPermissionGranted(getActivity())) {
            requestMyLocation();
        } else {
            askForPermissions();
        }
    }

    private void requestMyLocation() {
        myLocationSubscription.unsubscribe();
        myLocationSubscription = App.getLocationManager()
                .getLastLocation(true, true)
                .observeOn(RxSchedulers.main())
                .subscribe(this::doOnMyLocation, this::doOnMyLocationError);
    }

    private void doOnMyLocation(RALocation location) {
        if (!isAttached()) {
            return;
        }
        LatLng position = new LatLng(location.getLocation().getLatitude(), location.getLocation().getLongitude());
        moveCamera(position);
        setAdapterBounds(position);
    }

    private void doOnMyLocationError(Throwable throwable) {
        if (!isAttached()) {
            return;
        }
        if (throwable instanceof MissingLocationPermissionException) {
            askForPermissions();
        } else if (throwable instanceof LocationSettingsException) {
            LocationSettingsException settingsException = (LocationSettingsException) throwable;
            Status status = settingsException.getResult().getStatus();
            if (status.getStatusCode() == LocationSettingsStatusCodes.RESOLUTION_REQUIRED) {
                doOnLocationResolutionRequired(status);
            }
        } else {
            showCantGetLocationMessage();
        }
    }

    private void showCantGetLocationMessage() {
        RAToast.showShort(R.string.location_error);
    }

    private void doOnLocationResolutionRequired(Status status) {
        try {
            status.startResolutionForResult(getActivity(), LOCATION_RESOLUTION_REQUEST);
        } catch (Exception e) {
            Timber.e(e, "Unable to request location resolution settings");
            showCantGetLocationMessage();
        }
    }

    @Override
    public void moveCamera(final LatLng position) {
        if (gMap != null && position != null) {
            gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, Constants.DEFAULT_CAMERA_ZOOM));
        }
        KeyboardUtil.hideKeyBoard(getActivity(), binding.pickupAddress);
    }

    @Override
    public void setEditableInput(boolean editable) {
        binding.pickupAddress.setFocusable(editable);
        binding.pickupAddress.setFocusableInTouchMode(editable);
        binding.pickupAddress.setAdapter(editable ? autoCompleteAdapter : null);
        binding.pickupAddress.setOnItemClickListener(editable ? itemClickListener : null);
        if (editable) {
            binding.pickupAddress.requestFocus();
        } else {
            binding.pickupAddress.clearFocus();
        }
    }
}
