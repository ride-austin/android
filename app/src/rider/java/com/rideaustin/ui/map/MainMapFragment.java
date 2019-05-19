package com.rideaustin.ui.map;

import android.Manifest;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.DrawableRes;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.annotation.UiThread;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GestureDetectorCompat;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;
import com.facebook.appevents.AppEventsLogger;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.SphericalUtil;
import com.rideaustin.App;
import com.rideaustin.R;
import com.rideaustin.api.model.CarTypeConfiguration;
import com.rideaustin.api.model.Ride;
import com.rideaustin.api.model.driver.ActiveDriver;
import com.rideaustin.api.model.driver.RequestedCarType;
import com.rideaustin.base.BaseFragment;
import com.rideaustin.databinding.MainFragmentMapBinding;
import com.rideaustin.entities.GeoPosition;
import com.rideaustin.events.DriverRatedEvent;
import com.rideaustin.events.RideStatusEvent;
import com.rideaustin.manager.LocationSettingsException;
import com.rideaustin.manager.MapPreferencesManager;
import com.rideaustin.manager.MissingLocationPermissionException;
import com.rideaustin.manager.location.RALocation;
import com.rideaustin.schedulers.RxSchedulers;
import com.rideaustin.ui.campaigns.CampaignDetailsActivity;
import com.rideaustin.ui.contact.RideContactDialog;
import com.rideaustin.ui.drawer.NavigationDrawerActivity;
import com.rideaustin.ui.drawer.editprofile.EditProfileActivity;
import com.rideaustin.ui.map.views.MapView;
import com.rideaustin.ui.rate.RateDriverDialog;
import com.rideaustin.ui.ride.RideStatusService;
import com.rideaustin.ui.utils.CommonMaterialDialogCreator;
import com.rideaustin.ui.utils.MaterialDialogCreator;
import com.rideaustin.ui.viewstub.DriverDetailsFragment;
import com.rideaustin.ui.viewstub.RequestRideFragment;
import com.rideaustin.utils.Constants;
import com.rideaustin.utils.DialogUtils;
import com.rideaustin.utils.ImageHelper;
import com.rideaustin.utils.LatLngInterpolator;
import com.rideaustin.utils.LocalizeUtils;
import com.rideaustin.utils.LocationHintHelper;
import com.rideaustin.utils.MapUtils;
import com.rideaustin.utils.MarkerAnimation;
import com.rideaustin.utils.NetworkHelper;
import com.rideaustin.utils.PermissionUtils;
import com.rideaustin.utils.PickupHelper;
import com.rideaustin.utils.RecentPlacesHelper;
import com.rideaustin.utils.TouchWrapper;
import com.rideaustin.utils.ViewUtils;
import com.rideaustin.utils.location.DistanceUtil;
import com.rideaustin.utils.location.LocationHelper;
import com.rideaustin.utils.toast.RAToast;
import com.tbruyelle.rxpermissions.RxPermissions;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import java8.util.Optional;
import rx.Observable;
import rx.Subscription;
import rx.subjects.BehaviorSubject;
import rx.subscriptions.CompositeSubscription;
import rx.subscriptions.Subscriptions;
import timber.log.Timber;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.rideaustin.utils.CommonConstants.ANIMATION_DELAY_MS;
import static com.rideaustin.utils.CommonConstants.UNEXPECTED_STATE_KEY;
import static com.rideaustin.utils.Constants.CAR_ANIMATION_MS;

/**
 * Created by yshloma on 22.06.2016.
 */
public class MainMapFragment extends BaseFragment implements
        OnMapReadyCallback,
        IDrawDirectionForRide,
        MultipleCarFragment.CarCategoryListener,
        TouchWrapper.TouchWrapperListener,
        MapView {

    public static final int LOCATION_RESOLUTION_REQUEST = 9001;
    private static final int UPDATE_ZOOM_ANIMATION_MS = 100;
    private static final int DOUBLE_TAP_ANIMATION_MS = 400;

    public static final String DRIVER = "driver";
    public static final String RIDER = "rider";
    public static final String ADMIN = "admin";
    EventListener listener;
    private CompositeSubscription compositeSubscription = new CompositeSubscription();
    private Subscription permissionSubscription = Subscriptions.unsubscribed();
    private Subscription myLocationSubscription = Subscriptions.empty();
    private Subscription nearestDriversSubscription = Subscriptions.empty();
    private Subscription rideToRateSubscription = Subscriptions.empty();
    private Subscription toolbarTitleSubscription = Subscriptions.empty();
    private GoogleMap map;
    private LatLng pickupLocation;
    private Marker pickupMarker;
    private Marker destinationMarker;
    private Marker prevRideMarker;
    private Marker carMarker;
    private MarkerAnimation.Job carAnimation;
    private Target<Bitmap> carMarkerTarget;
    private int carMarkerDriverId;
    private MapViewModel mapViewModel;
    private MainFragmentMapBinding binding;
    private SupportMapFragment supportMapFragment;
    private Polyline wayOnMap;
    private MultipleCarFragment multipleCarFragment;
    private RequestRideFragment requestRideFragment;
    private DriverDetailsFragment detailsFragment;
    private LatLng nextCarPosition;
    private volatile boolean needFillDriverDetailsData = true;
    private int avatarOffset;
    private int paddingForMarker;
    private int topPaddingForMarker;
    private int routeStrokeWidth;
    private int routeStrokeColor;
    private MaterialDialog updateDialog;
    private RideContactDialog rideContactDialog;
    private Menu menu;
    private boolean isCameraAutoAnimationEnabled = true;
    private Subscription cameraAutoAnimationSubscription = Subscriptions.empty();
    private CompositeSubscription pickupPositionSubscription = new CompositeSubscription();
    private boolean locationResolutionCancelled = false;
    private CollapsibleElementsManager collapsibleElementsManager;
    private BehaviorSubject<LatLng> startPos = BehaviorSubject.create();
    private ValueAnimator initialAlphaAnimator;
    private CompositeSubscription initialAnimationSubscriptions = new CompositeSubscription();
    private GestureDetectorCompat swipeUpDetector;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        NavigationDrawerActivity.addRootFragmentClass(this.getClass().getName());
        binding = DataBindingUtil.inflate(inflater, R.layout.main_fragment_map, container, false);
        mapViewModel = new MapViewModel(this);
        collapsibleElementsManager = new CollapsibleElementsManager();
        supportMapFragment = SupportMapFragment.newInstance();

        multipleCarFragment = MultipleCarFragment.newInstance(this, mapViewModel);
        requestRideFragment = RequestRideFragment.newInstance();
        requestRideFragment.setDrawDirection(this);

        replaceFragment(supportMapFragment, R.id.map, false);
        binding.map.setListener(this);

        binding.pickupAddress.setOnClickListener(v -> showPickupSelection());
        binding.destinationAddress.setOnClickListener(v -> showDestinationSelection());

        binding.setMapModel(mapViewModel);

        binding.unmetRequirement.setOnClickListener(v -> {
            UnmetRequirementType type = mapViewModel.getUnmetRequirementType();
            doOnUnmetRequirement(type);
        });

        paddingForMarker = getResources().getDimensionPixelSize(R.dimen.map_padding_for_marker);
        avatarOffset = getResources().getDimensionPixelSize(R.dimen.driver_avatar_in_ride_big_version_size) / 2;
        topPaddingForMarker = getResources().getDimensionPixelSize(R.dimen.map_top_padding_for_marker);
        routeStrokeWidth = getResources().getDimensionPixelSize(R.dimen.map_route_stroke_width);
        routeStrokeColor = ContextCompat.getColor(getContext(), R.color.map_route_stroke_color);

        binding.comment.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                mapViewModel.sendComment();
            }
            return false;
        });

        swipeUpDetector = new GestureDetectorCompat(getContext(), new SwipeUpListener());
        binding.campaignPanel.setOnTouchListener((v, event) -> swipeUpDetector.onTouchEvent(event));
        binding.campaignPanel.setOnClickListener(v -> {
            mapViewModel.getCurrentCampaignProvider()
                    .ifPresent(provider -> startActivity(CampaignDetailsActivity.getInstance(getContext(), provider)));
        });

        return binding.getRoot();
    }

    public void doOnUnmetRequirement(UnmetRequirementType type) {
        if (type == UnmetRequirementType.PAYMENT_METHOD
                || type == UnmetRequirementType.PRIMARY_CARD_EXPIRED
                || type == UnmetRequirementType.UNPAID_BALANCE) {
            getCallback().navigateTo(R.id.navPayment);
        } else if (type == UnmetRequirementType.EMAIL_VERIFICATION) {
            Intent intent = new Intent(getActivity(), EditProfileActivity.class);
            intent.putExtra(EditProfileActivity.HAS_BACK_ACTION, true);
            startActivity(intent);
        }
    }

    private void showPickupSelection() {
        ((NavigationDrawerActivity) getActivity()).showAddressSelection(false);
    }

    private void showDestinationSelection() {
        ((NavigationDrawerActivity) getActivity()).showAddressSelection(true);
    }

    @Override
    public void onTimeToPickupRequested() {
        binding.timeToPickup.setVisibility(View.INVISIBLE);
        binding.minutes.setVisibility(View.INVISIBLE);
        binding.etaLoadedView.setVisibility(View.INVISIBLE);
        binding.etaLoadedView.stop();
        // prevent progress bar from restart when already animating
        if (binding.etaLoadingView.getVisibility() != View.VISIBLE) {
            binding.etaLoadingView.setVisibility(View.VISIBLE);
        }
        if (!binding.pickupLocation.isEnabled()) {
            binding.pickupLocation.setEnabled(true);
            binding.setPickupLocation.setText("");
        }
    }

    @Override
    public void onTimeToPickupResult(String timeToPickup) {
        binding.timeToPickup.setText(timeToPickup);
        binding.timeToPickup.setVisibility(View.VISIBLE);
        binding.minutes.setVisibility(View.VISIBLE);
        binding.etaLoadingView.setVisibility(View.INVISIBLE);
        binding.etaLoadedView.setVisibility(View.VISIBLE);
        binding.etaLoadedView.start();
        binding.setPickupLocation.setText(R.string.set_pickup_location);
        binding.setPickupLocation.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        binding.pickupLocation.setEnabled(true);
    }

    @Override
    public void onTimeToPickupCleared() {
        binding.timeToPickup.setVisibility(View.INVISIBLE);
        binding.minutes.setVisibility(View.INVISIBLE);
        binding.etaLoadingView.setVisibility(View.INVISIBLE);
        binding.etaLoadedView.setVisibility(View.INVISIBLE);
        binding.etaLoadedView.stop();
    }

    @Override
    public void onNoDriversAvailable() {
        binding.setPickupLocation.setText(R.string.no_available_cars);
        binding.setPickupLocation.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        binding.pickupLocation.setEnabled(true);
    }

    @Override
    public void onNotAvailableAtLocation() {
        binding.setPickupLocation.setText(R.string.coming_soon_in_your_area);
        binding.setPickupLocation.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
        binding.pickupLocation.setEnabled(false);
    }

    @Override
    public boolean isInRideRequest() {
        return requestRideFragment != null && requestRideFragment.isVisible();
    }

    @Override
    public boolean isInForeground() {
        return isVisible() && isResumed();
    }

    @Override
    public void showRoundUpPopup() {
        MaterialDialogCreator.createSimpleConfirmDialog(getString(R.string.choose_a_round_up), getActivity())
                .positiveText(R.string.btn_yes)
                .negativeText(R.string.btn_no)
                .cancelable(false)
                .onPositive((dialog, which) -> listener.onShowCharityScreen())
                .onNegative((dialog, which) -> mapViewModel.cancelRoundUp())
                .show();
    }

    /**
     * Returns whether can ask for location permissions.
     * Checks if:
     * a) request is not in process
     * b) permissions are not already granted
     *
     * @return <code>true</code> if can ask location permissions
     */
    private boolean canAskPermissions() {
        return permissionSubscription.isUnsubscribed()
                && !PermissionUtils.isLocationPermissionGranted(getActivity());
    }

    /**
     * Returns whether can try to request location.
     * It may require to ask permissions (if not granted)
     * Checks if:
     * a) permission request is not in process
     * b) permissions are already granted or request is not forbidden by user
     *
     * @return <code>true</code> if can request location
     */
    private boolean canRequestLocation() {
        return permissionSubscription.isUnsubscribed()
                && (PermissionUtils.isLocationPermissionGranted(getActivity())
                || !PermissionUtils.isLocationPermissionForbidden(getActivity()));
    }

    private void askForPermissions() {
        permissionSubscription.unsubscribe();
        permissionSubscription = new RxPermissions(getActivity())
                .request(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
                .observeOn(RxSchedulers.main())
                .subscribe(isGranted -> {
                    if (isGranted) {
                        App.getConfigurationManager().onLocationPermissionGranted();
                        if (map != null) {
                            //noinspection MissingPermission
                            map.setMyLocationEnabled(true);
                        }
                        requestMyLocation(false);
                    } else {
                        showNoLocationPermissionsMessage();
                    }
                }, throwable -> Timber.e(throwable, "Unable to process permissions"));
    }

    private void initializeMap() {
        if (map == null) {
            supportMapFragment.getMapAsync(this);
        }
        binding.map.setVisibility(View.VISIBLE);
    }

    public void setListener(EventListener listener) {
        this.listener = listener;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        replaceFragment(multipleCarFragment, R.id.multicar_panel_container, false);

        Ride ride = App.getDataManager().getCurrentRide();
        Timber.d("::onViewCreated:: Ride: %s", ride);
        if (mapViewModel.isActiveRide() && ride != null) {
            RAToast.showLong(R.string.ride_active);
        }

        binding.pickupLocation.setOnClickListener(v -> {
            binding.pickupLocation.setEnabled(false);
            onSetPickUpLocationClicked();
            RxSchedulers.schedule(() -> binding.pickupLocation.setEnabled(true), 200, TimeUnit.MILLISECONDS);
        });

        RideStatusService.startIfNeeded();
    }

    private void onSetPickUpLocationClicked() {
        if (!NetworkHelper.isNetworkAvailable()) {
            RAToast.show(R.string.network_error, Toast.LENGTH_SHORT);
            return;
        }

        if (!App.getDataManager().hasPaymentMethods()) {
            RAToast.show(R.string.error_set_payment_before, Toast.LENGTH_SHORT);
            return;
        }

        if (map == null || mapViewModel.getPickupLocation() == null) {
            return;
        }

        if (!App.getDataManager().getLocationHintHelper().isLocationAllowed(mapViewModel.getPickupLocation(), LocationHintHelper.AreaType.PICKUP)) {
            App.getDataManager().getLocationHintHelper().snapToNearestLocation(mapViewModel.getPickupLocation(), true);
            return;
        }

        if (mapViewModel.isPickupDistanceVerified()) {
            setPickupMarker();
            return;
        }

        compositeSubscription.add(PickupHelper.checkIsFar(mapViewModel.getPickupLocation(),
                () -> onAddressIsFar((dialog, which) -> setPickupMarker()),
                () -> setPickupMarker()));
    }

    @Override
    public void onAddressIsFar(MaterialDialog.SingleButtonCallback singleButtonCallback) {
        MaterialDialogCreator.createSimpleConfirmDialog(
                getString(R.string.warning_location_distance, PickupHelper.getDistantPickUpNotificationThreshold()),
                getActivity())
                .onPositive(singleButtonCallback)
                .show();
    }

    public void clearMap() {
        binding.pickupAddress.setText("");
        binding.destinationAddress.setText("");
        clearMapPanes();
    }

    private void clearMapPanes() {
        mapViewModel.setStartAddress(null);
        mapViewModel.setDestinationAddress(null);
        mapViewModel.clearCampaign();
        if (map != null) {
            mapViewModel.reloadCarTypes(map.getCameraPosition().target);
            removePickupMarker();
            removeDestinationMarker();
            removePrevRideMarker();
            clearDirectionOnMap();
            showPickupLocationView();
        }
        if (detailsFragment != null) {
            removeFragment(detailsFragment);
        }
        App.getDataManager().setSurgeArea(Collections.emptyList());
        multipleCarFragment.setPriorityCategories();
        hideRequestRideFragment();
        showMultipleCarFragment();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.home_menu, menu);
        MenuItem menuItem = menu.findItem(R.id.contact_driver);
        if (menuItem != null) {
            Button button = (Button) menuItem.getActionView().findViewById(R.id.button);
            button.setText(R.string.contact);
            button.setOnClickListener(v -> onOptionsItemSelected(menuItem));
        }
        this.menu = menu;
        manageOptionMenuButton();
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.myLocationButton:
                onMyLocationClick();
                return true;
            case R.id.contact_driver:
                Ride currentRide = App.getDataManager().getCurrentRide();
                if (isValidDriver(currentRide)) {
                    showRideContactDialog(currentRide);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private boolean isValidDriver(Ride currentRide) {
        return currentRide != null && currentRide.getActiveDriver() != null && currentRide.getActiveDriver().getDriver() != null;
    }

    // Contact is expected only in a ride that haven't started yet.
    // When there is no ride, there is no-one to contact. When Ride started, Rider do not need to contact Driver.
    @Override
    public void manageOptionMenuButton() {
        // RA-8954: activity menu could be changed by other fragment
        // check if menu contains what we expect
        if (menu != null) {
            MenuItem myLocation = menu.findItem(R.id.myLocationButton);
            MenuItem contactDriver = menu.findItem(R.id.contact_driver);
            if (myLocation != null && contactDriver != null) {
                if (mapViewModel.shouldShowContact()) {
                    myLocation.setVisible(false);
                    contactDriver.setVisible(true);
                    setRiderLocationVisible(false);
                } else {
                    myLocation.setVisible(true);
                    contactDriver.setVisible(false);
                    setRiderLocationVisible(true);
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void setRiderLocationVisible(boolean shouldBeVisible) {
        //noinspection MissingPermission
        if (map != null) {
            if (shouldBeVisible) {
                map.setMyLocationEnabled(PermissionUtils.isLocationPermissionGranted(getActivity()));
            } else {
                map.setMyLocationEnabled(false);
            }
        }
    }

    private void showRideContactDialog(Ride ride) {
        hideRideContactDialog();
        if (getFragmentManager() != null) {
            rideContactDialog = new RideContactDialog();
            rideContactDialog.setRide(ride);
            rideContactDialog.show(getFragmentManager(), RideContactDialog.class.getName());
        }
    }

    private void hideRideContactDialog() {
        if (rideContactDialog != null) {
            try {
                rideContactDialog.dismiss();
                rideContactDialog = null;
            } catch (Exception e) {
                Timber.e(e);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        enableDestinationAddress();
        clearDriverMarkerIfNeeded();
        //showRateDialogIfNeeded();
        manageOptionMenuButton();
        RefreshManager.getInstance().start();

        if (!mapViewModel.isActiveRide()) {
            LatLng location = pickupLocation != null ? pickupLocation : mapViewModel.getPickupLocation();
            if (location != null) {
                mapViewModel.reloadSurgeArea(location);
                mapViewModel.reloadCarTypes(location);
            }
        }

        refreshAddresses();
        mapViewModel.checkUpdate();
        mapViewModel.checkRidersCharity();

        if (map != null) {
            // RA-9554: update UI state after return
            // from background - ride status might change
            listenToAddressChanged();
            restoreUiState();
            restoreRideState();
            restoreCameraZoom(false);
            hidePlaceholder();
            showControls();
        }
    }

    private void restoreUiState() {
        if (mapViewModel.isActiveRide()) {
            hideMultipleCarFragment();
            disablePickupAddress();
            hidePickupLocationView();
        } else if (pickupMarker != null) {
            hideMultipleCarFragment();
            hidePickupLocationView();
            showRequestRideFragment();
        } else {
            hideRequestRideFragment();
            showMultipleCarFragment();
            showPickupLocationView();
        }
    }

    private void restoreCameraZoom(boolean animated) {
        boolean isActiveRide = mapViewModel.isActiveRide() && App.getDataManager().getCurrentRide() != null;
        boolean isRequestedState = mapViewModel.isRideRequested();
        boolean hasPickupMarker = pickupMarker != null;

        if (isActiveRide && !isRequestedState && hasPickupMarker) {
            // use normal flow to show active ride, but with no animation
            updateActiveRide(App.getDataManager().getCurrentRide(), animated);
        } else if (hasPickupMarker) {
            // no ride, but pickup marker set/restored (see restoreCoordinates())
            // seems we are requesting a ride
            zoomCamera(pickupMarker, destinationMarker, animated);
        } else {
            // show initial position
            // user really did not select pickup
            // or something went wrong
            showMyLocation(animated);
        }
    }

    private void restoreRideState() {
        String status = App.getPrefs().getRideStatus();
        if (TextUtils.isEmpty(status)) {
            if (RideStatusService.isRunning()) {
                // RA-9796: show ride requesting state
                onRideRequesting();
            } else {
                hideRequestRideState();
            }
        }
    }

    private void listenToAddressChanged() {
        compositeSubscription.add(mapViewModel.getStartAddressChanged()
                .observeOn(RxSchedulers.main())
                .subscribe(this::doOnStartAddressChanged));
        compositeSubscription.add(mapViewModel.getDestinationAddressChanged()
                .observeOn(RxSchedulers.main())
                .subscribe(this::doOnDestinationAddressChanged));
        compositeSubscription.add(mapViewModel.getPrevRideLocationObservable()
                .observeOn(RxSchedulers.main())
                .subscribe(this::doOnPrevRideLocation, throwable -> {
                    Timber.e(throwable);
                    doOnPrevRideLocation(Optional.empty());
                }));
    }

    private void doOnStartAddressChanged(@Nullable GeoPosition geoPosition) {
        if (geoPosition == null) {
            return;
        }
        if (pickupMarker != null) {
            addOrUpdatePickupMarker(map, getContext(), geoPosition.getLatLng());
            zoomCamera(pickupMarker, destinationMarker, false);
        } else {
            if (binding.greenPin.getVisibility() == View.GONE) {
                addOrUpdatePickupMarker(map, getContext(), geoPosition.getLatLng());
            }
            zoomCamera(geoPosition.getLatLng(), false);
        }
        mapViewModel.restoreCommentsFromCache();
        refreshPickup();
    }

    private void doOnDestinationAddressChanged(@Nullable GeoPosition geoPosition) {
        if (geoPosition != null) {
            mapViewModel.updateCommentsVisibility(true);
            mapViewModel.restoreCommentsFromCache();
            if (pickupMarker != null) {
                addOrUpdateDestinationMarker(map, getContext(), geoPosition.getLatLng());
            } else {
                if (binding.greenPin.getVisibility() == View.GONE) {
                    addOrUpdateDestinationMarker(map, getContext(), geoPosition.getLatLng());
                }
            }
            if (mapViewModel.isActiveRide() && !mapViewModel.isRideRequested()) {
                // update zoom and route
                updateActiveRide();
            } else if (pickupMarker != null) {
                // update only zoom (if pickup set)
                zoomCamera(pickupMarker, destinationMarker, false);
            }
        } else {
            removeDestinationMarker();
            mapViewModel.updateCommentsVisibility();
        }
        refreshDestination();
    }

    private void doOnPrevRideLocation(Optional<LatLng> location) {
        location.ifPresentOrElse(
                latLng -> addOrUpdatePrevRideMarker(map, getContext(), latLng),
                this::removePrevRideMarker);
    }

    private void clearDriverMarkerIfNeeded() {
        long rideToRate = App.getPrefs().getRideToRate();
        if (rideToRate != 0) {
            removeRideCarMarker();
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        if (hidden) {
            clearDesignatedPickups();
        } else {
            // payment methods may changed
            // better use observable like with unpaid balance
            mapViewModel.updateUnmetRequirements();
            refreshAddresses();
            if (binding.multicarPanelContainer.getVisibility() == VISIBLE) {
                drawDesignatedPickups();
            }
        }
    }

    private void refreshAddresses() {
        refreshPickup();
        refreshDestination();
    }

    private void refreshPickup() {
        binding.pickupAddress.setText(mapViewModel.getStartAddress() != null
                ? mapViewModel.getStartAddress().getPlaceName() : "");
    }

    private void refreshDestination() {
        binding.destinationAddress.setText(mapViewModel.getDestinationAddress() != null
                ? mapViewModel.getDestinationAddress().getPlaceName() : "");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mapViewModel.onDestroy();
        MapPreferencesManager.saveMarkerCoordinates(pickupMarker, destinationMarker);
        MapPreferencesManager.saveCurrentRideState(mapViewModel.getCurrentRideStatusEvent());
    }

    @Override
    public void onUpgradeNeeded() {
        DialogUtils.dismiss(updateDialog);
        updateDialog = CommonMaterialDialogCreator.createMandatoryUpdateDialog(getActivity());
    }

    @Override
    public void onPause() {
        super.onPause();
        RefreshManager.getInstance().pause();
        clearDesignatedPickups();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (canAskPermissions()) {
            askForPermissions();
        }
        initializeMap();
        checkGoogleServices();
        mapViewModel.onStart();
        rideToRateSubscription = App.getDataManager().ifRideRateNeededThen(this::rateDriverNeeded);
        toolbarTitleSubscription = mapViewModel.getToolbarTitle()
                .observeOn(RxSchedulers.main())
                .subscribe(this::setToolbarTitle, throwable -> {
                    Timber.e(throwable);
                    setToolbarTitle(App.getFormattedAppName());
                });
    }

    @Override
    public void onStop() {
        super.onStop();
        mapViewModel.onStop();
        compositeSubscription.clear();
        permissionSubscription.unsubscribe();
        myLocationSubscription.unsubscribe();
        nearestDriversSubscription.unsubscribe();
        cameraAutoAnimationSubscription.unsubscribe();
        rideToRateSubscription.unsubscribe();
        initialAnimationSubscriptions.clear();
        toolbarTitleSubscription.unsubscribe();
    }

    private void rateDriverNeeded(Ride ride) {
        RateDriverDialog.createNew(ride.getId())
                .show(getSupportFragmentManager())
                .setOnDismissListener(dialogInterface -> mapViewModel.checkUpdate());
    }

    @Nullable
    private ActiveDriver getActiveDriver(Ride response) {
        return response.getActiveDriver();
    }

    public void hideRequestRideState() {
        binding.requesting.setVisibility(GONE);
    }

    public void showRequestRideState() {
        binding.requesting.setVisibility(VISIBLE);
    }

    public void decorateRequestRideState(boolean isFemaleMode) {
        if (getContext() != null) {
            binding.requesting.setBackgroundColor(ContextCompat.getColor(getContext(), isFemaleMode ? R.color.female_only_pink : R.color.drawer_icon));
        }
    }

    public void onPickupCancelled() {
        clearMap();
        showMyLocation(false);
    }

    @Override
    public void onRideRequested() {
        hidePickupLocationView();
        showRequestRideState();
        hideMultipleCarFragment();
        disablePickupAddress();
        // fix restoring app in REQUESTED state
        if (!requestRideFragment.isVisible()) {
            showRequestRideFragment();
        }
        requestRideFragment.restoreRequestingStateWithRide();
        cleanupOnRequestedState();
        if (App.getDataManager().getCurrentRide() != null) {
            Bundle params = new Bundle();
            params.putLong("rideId", App.getDataManager().getCurrentRide().getId());
            AppEventsLogger.newLogger(getActivity()).logEvent("REQUESTED", params);
        }
    }

    public void onRideRequesting() {
        hidePickupLocationView();
        hideMultipleCarFragment();
        disablePickupAddress();
        if (!requestRideFragment.isVisible()) {
            showRequestRideFragment();
        }
        requestRideFragment.restoreRequestingStateWithoutRide();
        cleanupOnRequestedState();
    }

    @Override
    public void onRideNoAvailableDriver() {
        hideRequestRideState();
        noAvailableDriverUiState();
        enablePickupAddress();
    }

    @Override
    public void onRideAdminCancelled(final RideStatusEvent event) {
        canceledUiState();
    }

    @Override
    public void onRideDriverCancelled(final RideStatusEvent event) {
        canceledUiState();
    }

    @Override
    public void onRideRiderCancelled(final RideStatusEvent event) {
        canceledUiState();
        hideProgress();
    }

    /**
     * Adaptation to ride auto transfer
     * In 3.0 ride can be returned back to REQUESTED/NO_AVAILABLE_DRIVERS after DRIVER_ASSIGNED
     * What need to do:
     * a) remove direction from map
     * b) reset pickup marker not to show ETA
     * c) hide and reset driver details
     * d) remove driver's car marker
     * e) show nearest drivers
     * f) show pickup (and destination if set)
     */
    private void cleanupOnRequestedState() {
        clearDirectionOnMap();
        resetPickupMarkerUi();
        if (detailsFragment != null) {
            removeFragment(detailsFragment);
        }
        needFillDriverDetailsData = true;
        removeRideCarMarker();
        showNearestDrivers();

        // delay to avoid animation twitch
        compositeSubscription.add(RxSchedulers.schedule(() -> {
            LatLng pickup = mapViewModel.getStartAddress() != null ? mapViewModel.getStartAddress().getLatLng() : null;
            LatLng destination = mapViewModel.getDestinationAddress() != null ? mapViewModel.getDestinationAddress().getLatLng() : null;
            if (pickup != null) {
                if (destination != null) {
                    zoomCamera(pickup, destination, true);
                } else {
                    zoomCamera(pickup, true);
                }
            }
        }, Constants.ANIMATION_DELAY_MS, TimeUnit.MILLISECONDS));
    }

    @Override
    public void onRideDriverAssigned(final RideStatusEvent event) {
        Ride ride = event.getResp();
        restoreAddresses();
        restoreCoordinates();
        binding.cancelPendingRequest.setVisibility(GONE);
        hideRequestRideState();
        if (needUpdateDriverFragment(event)) {
            ActiveDriver activeDriver = getActiveDriver(ride);
            driverAssignedUiState(event, activeDriver);
        }
        clearNearestDrivers();
        updateActiveRide(ride, true);
        mapViewModel.restoreCommentsFromRide(ride);
        mapViewModel.updateCommentsVisibility();
    }

    @Override
    public void onRideDriverReached(final RideStatusEvent event) {
        Ride ride = event.getResp();
        restoreAddresses();
        restoreCoordinates();
        binding.cancelPendingRequest.setVisibility(GONE);
        hideRequestRideState();
        activateDetailsFragmentWithEvent(event);
        clearNearestDrivers();
        updateActiveRide(ride, true);
        mapViewModel.restoreCommentsFromRide(ride);
        mapViewModel.updateCommentsVisibility();
    }

    @Override
    public void onRideActive(final RideStatusEvent event) {
        Ride ride = event.getResp();
        restoreAddresses();
        activateDetailsFragmentWithEvent(event);
        clearNearestDrivers();
        hideRequestRideState();
        binding.cancelPendingRequest.setVisibility(GONE);
        if (detailsFragment != null) {
            detailsFragment.tripActive();
        }

        updateDestinationBasedOnRideState(ride);

        updateActiveRide(ride, true);
        mapViewModel.updateCommentsVisibility();
    }

    private void updateDestinationBasedOnRideState(Ride ride) {
        final Double endLong = ride.getEndLocationLong();
        final Double endLat = ride.getEndLocationLat();
        if (endLong != null && endLat != null) {
            final LatLng endLocation = new LatLng(endLat, endLong);
            if (map != null) {
                addOrUpdateDestinationMarker(map, getContext(), endLocation);
            }
        }
    }

    @Override
    public void onRideCompleted(final RideStatusEvent event) {
        // Show rate driver screen
        if (App.getDataManager().getRequestedCarType() == null) {
            Ride resp = event.getResp();
            App.getDataManager().setCarType(resp.getRequestedCarType());
        }

        clearMap();
        showMyLocation(false);
        removeRideCarMarker();
        showNearestDrivers();
        enablePickupAddress();

        Bundle params = new Bundle();
        params.putLong("rideId", event.getResp().getId());
        AppEventsLogger.newLogger(getActivity()).logEvent("COMPLETED", params);
    }

    @Override
    public void onRideFinished() {
        showRequestRideFragmentAndEnablePickupAddress();
        enablePickupAddress();
    }

    @Override
    public void onRideRequestError(final RideStatusEvent event) {
        hideRequestRideState();
        noAvailableDriverUiState();
    }

    @Override
    public void updateActiveRide() {
        updateActiveRide(App.getDataManager().getCurrentRide(), true);
    }

    public void checkRoundUp(boolean onUserInteraction) {
        if (mapViewModel != null) {
            mapViewModel.checkRoundUpPopup(onUserInteraction);
        }
    }

    @Override
    public void onAddressAvailable(String address) {
        binding.pickupAddress.setText(address);
        binding.pickupAddress.clearFocus();
        mapViewModel.restoreCommentsFromCache();
    }

    @Override
    public void onAddressNotAvailable() {
        binding.pickupAddress.setText("");
    }

    @Override
    public void onRideRated(final DriverRatedEvent event) {
        resetState();
    }

    private void checkGoogleServices() {
        boolean isEnabled = isGoogleServicesEnabled();
        mapViewModel.setGoogleServicesEnabled(isEnabled);
        if (!isEnabled) {
            NavigationDrawerActivity activity = ((NavigationDrawerActivity) getActivity());
            if (activity.hasPlaceholder()) {
                hidePlaceholder();
                showControls();
            }
        }
    }

    private boolean isGoogleServicesEnabled() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(getContext());
        return resultCode == ConnectionResult.SUCCESS;
    }

    private boolean needUpdateDriverFragment(RideStatusEvent event) {
        return detailsFragment == null
                || !detailsFragment.isVisible()
                || detailsFragment.needUpdate(event.getResp())
                || needFillDriverDetailsData;
    }

    private void activateDetailsFragmentWithEvent(RideStatusEvent event) {
        if (needUpdateDriverFragment(event)) {
            removeFragment(detailsFragment);
            detailsFragment = DriverDetailsFragment.newInstance(getActiveDriver(event.getResp()),
                    listener, mapViewModel);
            detailsFragment.setCollapsibleListener(collapsibleElementsManager);
            replaceFragment(detailsFragment, R.id.ride_panel_container, false);
            needFillDriverDetailsData = false;
        }
        binding.ridePanelContainer.setVisibility(VISIBLE);
        manageOptionMenuButton();
    }

    private void enablePickupAddress() {
        binding.pickupAddress.setEnabled(true);
    }

    private void disablePickupAddress() {
        binding.pickupAddress.setEnabled(false);
    }

    private void enableDestinationAddress() {
        binding.destinationAddress.setEnabled(true);
    }

    private void disableDestinationAddress() {
        binding.destinationAddress.setEnabled(false);
    }

    private void driverAssignedUiState(RideStatusEvent event, ActiveDriver activeDriver) {
        removeFragment(detailsFragment);
        detailsFragment = DriverDetailsFragment.newInstance(activeDriver,
                listener,
                mapViewModel);
        detailsFragment.setCollapsibleListener(collapsibleElementsManager);
        replaceFragment(detailsFragment, R.id.ride_panel_container, false);
        binding.ridePanelContainer.setVisibility(VISIBLE);
        needFillDriverDetailsData = false;
    }

    private void noAvailableDriverUiState() {
        mapViewModel.clearRideState();
        if (!requestRideFragment.isVisible() && !multipleCarFragment.isVisible()) {
            showRequestRideFragmentAndEnablePickupAddress();
        }
        hideRequestRideState();
        requestRideFragment.restoreInitialUIState();
        enableAddressInput();
        cleanupOnRequestedState();
    }

    private void canceledUiState() {
        mapViewModel.clearRideState();
        hideRequestRideState();
        hideRideContactDialog();

        needFillDriverDetailsData = true;
        removeRideCarMarker();
        removePickupMarker();
        removeDestinationMarker();
        removePrevRideMarker();

        getCallback().hideProgress();
        enableAddressInput();
        clearDirectionOnMap();

        clearMap();
        showMyLocation(false);
        showNearestDrivers();
    }

    private void drawDesignatedPickups() {
        if (!pickupPositionSubscription.hasSubscriptions()) {
            App.getDataManager().getLocationHintHelper().drawHints(map, LocationHintHelper.AreaType.PICKUP);
            pickupPositionSubscription.add(mapViewModel.observePickupLocation()
                    .observeOn(RxSchedulers.main())
                    .subscribe(it -> {
                        App.getDataManager().getLocationHintHelper().snapToNearestLocation(it, false);
                    }));
            pickupPositionSubscription.add(App.getDataManager().getLocationHintHelper()
                    .observeSnappedLocation()
                    .observeOn(RxSchedulers.main())
                    .subscribe(it -> {
                        animateCamera(it, 200);
                    }));
        }
    }

    private void clearDesignatedPickups() {
        App.getDataManager().getLocationHintHelper().clearHints();
        pickupPositionSubscription.clear();
    }

    private void hideMultipleCarFragment() {
        binding.multicarPanelContainer.setVisibility(GONE);
        hideFragment(multipleCarFragment);
        clearDesignatedPickups();
    }

    private void showMultipleCarFragment() {
        if (binding.multicarPanelContainer.getVisibility() == GONE) {
            binding.multicarPanelContainer.setVisibility(VISIBLE);
            showFragment(multipleCarFragment);
            multipleCarFragment.updateBottomOffset();
        }
        if (!isHidden()) {
            drawDesignatedPickups();
        }
    }

    private void hideRequestRideFragment() {
        removeFragment(requestRideFragment);
        binding.cancelPendingRequest.setVisibility(GONE);
        binding.ridePanelContainer.setVisibility(GONE);
        mapViewModel.setRideRequestShown(false);
        mapViewModel.updateCommentsVisibility();
    }

    public void showRequestRideFragment() {
        binding.ridePanelContainer.setVisibility(VISIBLE);
        if (!requestRideFragment.isAdded()) {
            replaceFragment(requestRideFragment, R.id.ride_panel_container, false);
        }
        requestRideFragment.restoreInitialUIState();
        requestRideFragment.updateBottomOffset();
        mapViewModel.setRideRequestShown(true);
        mapViewModel.updateCommentsVisibility();
        mapViewModel.restoreCommentsFromCache();
    }

    @UiThread
    private void removeRideCarMarker() {
        Timber.d("::removeRideCarMarker::");
        if (carMarkerTarget != null) {
            Glide.with(App.getInstance()).clear(carMarkerTarget);
            carMarkerTarget = null;
        }
        if (carMarker != null) {
            Timber.d("::removeRideCarMarker:: marker exists");
            carMarker.remove();
            carMarker = null;
        }
    }

    private void showRequestRideFragmentAndEnablePickupAddress() {
        RxSchedulers.schedule(() -> {
            binding.pickupAddress.setEnabled(true);
            hideMultipleCarFragment();
            showRequestRideFragment();
        });
    }

    private void hidePickupLocationView() {
        binding.pickupLocation.setVisibility(View.GONE);
        binding.greenPin.setVisibility(View.GONE);
        binding.map.setShowPickupMarker(false);
    }

    private void showPickupLocationView() {
        binding.pickupLocation.setVisibility(View.VISIBLE);
        binding.greenPin.setVisibility(View.VISIBLE);
        binding.map.setShowPickupMarker(true);
    }

    private void setPickupMarker() {
        pickupLocation = mapViewModel.getPickupLocation();
        showPickupOrDestination(true, pickupLocation);

        Timber.d("Setting pick up location to %s", pickupLocation);

        hidePickupLocationView();

        if (!mapViewModel.pickupEqualToStartAddress()) {
            showProgress(getString(R.string.loading_address));
            compositeSubscription.add(
                    LocationHelper.loadBestAddress(new LatLng(pickupLocation.latitude, pickupLocation.longitude))
                            .subscribeOn(RxSchedulers.computation())
                            .observeOn(RxSchedulers.main())
                            .doOnTerminate(this::hideProgress)
                            .doOnUnsubscribe(this::hideProgress)
                            .subscribe(
                                    this::onPickupAddressAvailable,
                                    this::onFailedToFetchPickupAddress
                            ));
        } else {
            onPickupAddressAvailable(mapViewModel.getStartAddress());
        }
    }

    private void onPickupAddressAvailable(GeoPosition geoPosition) {
        mapViewModel.setStartAddress(geoPosition);
        Timber.d("::onPickupAddressAvailable:: %s", geoPosition.getAddressLine());
        binding.pickupAddress.setText(geoPosition.getPlaceName());
        RecentPlacesHelper.saveAddress(geoPosition);

        if (mapViewModel.getDestinationAddress() != null) {
            showPickupOrDestination(false, mapViewModel.getDestinationAddress().getLatLng());
            mapViewModel.updateCommentsVisibility();
        }
        hideMultipleCarFragment();
        showRequestRideFragment();
    }

    private void onFailedToFetchPickupAddress(Throwable error) {
        Timber.e(error, "::onFailedToFetchPickupAddress::");
        binding.pickupAddress.setText(R.string.empty);
        showPickupLocationView();
        removePickupMarker();
        RAToast.show(R.string.unable_resolve_address, Toast.LENGTH_LONG);
    }

    /**
     * Request to show my location if location permission granted.
     * Otherwise, ask for location permission.
     */
    private void onMyLocationClick() {
        locationResolutionCancelled = false;
        if (PermissionUtils.isLocationPermissionGranted(getActivity())) {
            requestMyLocation(true);
        } else {
            askForPermissions();
        }
    }

    /**
     * Request to show my location if location permission is granted (or not forbidden).
     * Otherwise, there are two options:
     * a) show start address (if provided)
     * b) select address for current camera position
     * Next time user will tap on "Show my location" button, permission will be requested again.
     * see {@link MainMapFragment#onMyLocationClick()}
     */
    @MainThread
    private void showMyLocation(boolean animated) {
        if (canRequestLocation()) {
            requestMyLocation(animated);
        } else {
            showMyLocationFallback(animated);
        }
    }

    private void showMyLocationFallback(boolean animated) {
        if (mapViewModel.getStartAddress() != null) {
            LatLng position = mapViewModel.getStartAddress().getLatLng();
            zoomCamera(position, animated);
        } else {
            CameraPosition cameraPosition = getCameraPosition();
            if (cameraPosition != null) {
                mapViewModel.onCameraChange(cameraPosition);
            }
        }
    }

    private void requestMyLocation(boolean useAnimation) {
        myLocationSubscription.unsubscribe();
        myLocationSubscription = App.getLocationManager()
                .getLastLocation(true, true)
                .observeOn(RxSchedulers.main())
                .subscribe(location -> doOnMyLocation(location, useAnimation), this::doOnMyLocationError);
    }

    private void doOnMyLocation(RALocation location, boolean useAnimation) {
        if (!isAttached()) {
            // RA-11055: don't really know how it happens..
            // Call stack should guarantee fragment is attached and
            // subscription is active between onStart/onStop
            return;
        }
        LatLng position = new LatLng(location.getLocation().getLatitude(), location.getLocation().getLongitude());
        zoomCamera(position, useAnimation);
        startPos.onNext(position);
    }

    private void doOnMyLocationError(Throwable throwable) {
        if (!isAttached()) {
            // RA-11055: don't really know how it happens..
            // Call stack should guarantee fragment is attached and
            // subscription is active between onStart/onStop
            return;
        }
        if (throwable instanceof MissingLocationPermissionException) {
            askForPermissions();
        } else if (throwable instanceof LocationSettingsException) {
            LocationSettingsException settingsException = (LocationSettingsException) throwable;
            Status status = settingsException.getResult().getStatus();
            // TODO: consider other status codes as well
            if (status.getStatusCode() == LocationSettingsStatusCodes.RESOLUTION_REQUIRED) {
                doOnLocationResolutionRequired(status);
            }
        } else {
            showCantGetLocationMessage();
        }
    }

    private void doOnLocationResolutionRequired(Status status) {
        // RA-9566: user already cancelled resolution request
        // just proceed without GPS
        if (locationResolutionCancelled) {
            showMyLocationFallback(false);
        } else {
            // request location resolution
            try {
                status.startResolutionForResult(getActivity(), LOCATION_RESOLUTION_REQUEST);
            } catch (Exception e) {
                Timber.e(e, "Unable to request location resolution settings");
                showCantGetLocationMessage();
            }
        }
    }

    public void onLocationResolutionResult(int resultCode, Intent data) {
        switch (resultCode) {
            case Activity.RESULT_OK:
                locationResolutionCancelled = false;
                showMyLocation(false);
                break;
            case Activity.RESULT_CANCELED:
                locationResolutionCancelled = true;
                showCantGetLocationMessage();
                showMyLocationFallback(false);
                break;
        }
    }

    private void showNoLocationPermissionsMessage() {
        RAToast.showShort(getString(R.string.dont_have_permission, App.getAppName()));
    }

    private void showCantGetLocationMessage() {
        RAToast.showShort(R.string.location_error);
    }

    private void showNearestDrivers() {
        nearestDriversSubscription.unsubscribe();
        nearestDriversSubscription = App.getLocationManager()
                .getLastLocation(true, true)
                .subscribe(location -> {
                    Timber.d("::showNearestDrivers:: MapViewModel: %s MyLocation: %s", mapViewModel, location.getLocation());
                    if (mapViewModel != null) {
                        mapViewModel.loadDriversNearBy(new LatLng(location.getLocation().getLatitude(), location.getLocation().getLongitude()), true);
                    }
                }, throwable -> {
                });
    }

    private void clearNearestDrivers() {
        nearestDriversSubscription.unsubscribe();
        mapViewModel.clearNearestCars();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setOnMarkerClickListener(marker -> true); // This will prevent marker titles being opened.
        map.setContentDescription(getString(R.string.google_map));
        mapViewModel.setMap(map);

        map.getUiSettings().setMyLocationButtonEnabled(false);
        map.getUiSettings().setRotateGesturesEnabled(false);
        map.getUiSettings().setMapToolbarEnabled(false);
        map.getUiSettings().setTiltGesturesEnabled(false);
        map.getUiSettings().setZoomGesturesEnabled(false);
        // Ugly fix for RA-9464
        // Must be refactored in 3.0
        map.setOnCameraIdleListener(() -> {
            if (!isHidden()) {
                mapViewModel.onCameraChange(getCameraPosition());
            }
        });

        if (!isAttached()) {
            // RA-13680: called when not attached?
            Throwable throwable = new Throwable(UNEXPECTED_STATE_KEY);
            Timber.e(throwable, "onMapReady, fragment isAttached: %b, isAdded: %b, isRemoving: %b, isDetached: %b",
                    isAttached(), isAdded(), isRemoving(), isDetached());
            return;
        }

        if (!isResumed()) {
            // RA-13981: stop here to avoid possible subscriptions after onStop()
            // will execute everything onResume()
            return;
        }

        listenToAddressChanged();
        restoreAddresses();
        restoreCoordinates();
        restoreUiState();
        restoreRideState();

        boolean isActiveRide = mapViewModel.isActiveRide() && App.getDataManager().getCurrentRide() != null;
        if (isActiveRide) {
            // open faster without animation
            hidePlaceholder();
            showControls();
            restoreCameraZoom(false);
        } else {
            NavigationDrawerActivity activity = ((NavigationDrawerActivity) getActivity());
            boolean needInitialZoom = activity.hasPlaceholder() && destinationMarker == null;
            if (needInitialZoom) {
                if (canRequestLocation()) {
                    requestMyLocation(false);
                }
                // wait for position on map
                initialAnimationSubscriptions.add(startPos.asObservable()
                        // minimum 500ms
                        .delay(500, TimeUnit.MILLISECONDS, RxSchedulers.computation())
                        // maximum 3000ms
                        .timeout(3000, TimeUnit.MILLISECONDS, RxSchedulers.computation())
                        // take first position
                        .take(1)
                        .observeOn(RxSchedulers.main())
                        .subscribe(this::showMap, throwable -> showMap(null)));
            } else {
                // no placeholder or can't zoom
                hidePlaceholder();
                showControls();
                restoreCameraZoom(false);
            }
        }
    }

    private void showMap(LatLng position) {
        boolean needZoom = position != null;
        if (needZoom) {
            // zoom-out to show 500 meters area around
            int radius = 500;
            LatLng southwest = SphericalUtil.computeOffset(position, radius * Math.sqrt(2.0), 225);
            LatLng northeast = SphericalUtil.computeOffset(position, radius * Math.sqrt(2.0), 45);
            zoomCamera(northeast, southwest, false);
        }
        mapViewModel.setControlsAlpha(0f);
        initialAnimationSubscriptions.add(RxSchedulers.schedule(() -> {
            if (!isAttached()) {
                // RA-12374: probably bug in fragment management
                // fragment is not stopped (subscription is active)
                // but already detached from activity.
                // It happens when NavigationDrawerActivity redirects to splash due to sync problems
                // see {@link NavigationDrawerActivity#onGotoSplash()}
                return;
            }
            hidePlaceholder();
            if (needZoom) {
                doInitialZoom(position);
            } else {
                restoreCameraZoom(false);
            }
            if (initialAlphaAnimator != null) {
                initialAlphaAnimator.cancel();
            }
            initialAlphaAnimator = ValueAnimator.ofFloat(0f, 1f);
            initialAlphaAnimator.setInterpolator(new AccelerateInterpolator());
            initialAlphaAnimator.setStartDelay(needZoom ? 1500 : 500);
            initialAlphaAnimator.setDuration(500);
            initialAlphaAnimator.addUpdateListener(animation ->
                    mapViewModel.setControlsAlpha((float) animation.getAnimatedValue()));
            initialAlphaAnimator.start();

        }, ANIMATION_DELAY_MS, TimeUnit.MILLISECONDS));
    }

    /**
     * Do initial zoom after placeholder map hidden.
     * Need this for two reasons:
     * 1) custom animation duration
     * 2) callback to show actual zoom after finish (actual zoom may changed)
     *
     * @param position position to zoom into
     */
    private void doInitialZoom(LatLng position) {
        int width = binding.map.getWidth();
        int height = binding.map.getHeight();
        if (width > 0 && height > 0) {
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(position, Constants.DEFAULT_CAMERA_ZOOM);
            map.setPadding(0, 0, 0, 0);
            map.animateCamera(cameraUpdate, 1000, new GoogleMap.CancelableCallback() {
                @Override
                public void onFinish() {
                    if (getActivity() != null) {
                        restoreCameraZoom(true);
                    }
                }

                @Override
                public void onCancel() {
                }
            });
            map.setPadding(0, 0, 0, mapViewModel.getBottomOffset());
        } else {
            restoreCameraZoom(true);
        }
    }

    private void hidePlaceholder() {
        if (getActivity() instanceof NavigationDrawerActivity) {
            NavigationDrawerActivity activity = ((NavigationDrawerActivity) getActivity());
            activity.hidePlaceholder();
        }
    }

    private void showControls() {
        if (initialAlphaAnimator != null) {
            initialAlphaAnimator.cancel();
            initialAlphaAnimator = null;
        }
        initialAnimationSubscriptions.clear();
        mapViewModel.setControlsAlpha(1f);
    }

    void showPickupOrDestination(boolean isPickup, LatLng location) {
        if (map == null) {
            return;
        }
        if (isPickup) {
            addOrUpdatePickupMarker(map, App.getInstance(), location);
            //zoom out when change pickup point via text field
            if (destinationMarker != null) {
                zoomCamera(pickupMarker, destinationMarker, true);
                mapViewModel.setPickupLocation(location);
            } else {
                zoomCamera(location, true);
            }
            if (isPickupAddressEntered() && multipleCarFragment.isVisible()) {
                mapViewModel.reloadSurgeArea(location);
            }
            mapViewModel.reloadCarTypes(location);
        } else {
            addOrUpdateDestinationMarker(map, App.getInstance(), location);
            /**
             * Zoom to show pickup/destination only if:
             * a) ride is not in active state or
             * b) driver reached and ride was started
             */
            if (!mapViewModel.isActiveRide() || mapViewModel.isDriverReached()) {
                zoomCamera(pickupMarker, destinationMarker, true);
            }
        }
    }

    /**
     * Request to calculate and draw direction
     * for ride (if ride started) or for pickup
     */
    private void getDirection() {
        if (mapViewModel.isRideStartedByDriver()) {
            getDirectionForRide();
        } else {
            getDirectionForPickup();
        }
    }

    /**
     * Request to calculate direction from car to destination, if both markers are set.
     * Otherwise, clear direction on map to prevent showing inconsistent direction
     */
    private void getDirectionForRide() {
        if (carMarker != null && destinationMarker != null) {
            mapViewModel.drawDirectionFromCarToDestination(carMarker.getPosition(), destinationMarker.getPosition());
        } else {
            clearDirectionOnMap();
        }
    }

    /**
     * Request to calculate direction from car to pickup, if both markers are set.
     * Otherwise, clear direction on map to prevent showing inconsistent direction
     */
    private void getDirectionForPickup() {
        if (carMarker != null && pickupMarker != null) {
            mapViewModel.drawDirectionFromCarToPickup(carMarker.getPosition(), pickupMarker.getPosition());
        } else {
            clearDirectionOnMap();
        }
    }

    @Override
    public void drawDirectionOnMap(List<LatLng> direction) {
        clearDirectionOnMap();
        wayOnMap = map.addPolyline(new PolylineOptions()
                .width(routeStrokeWidth)
                .color(routeStrokeColor)
                .geodesic(true)
                .addAll(direction));
        MapUtils.hasRoute = !direction.isEmpty();
    }

    private void clearDirectionOnMap() {
        if (mapViewModel != null) {
            mapViewModel.cancelDrawDirection();
        }
        if (wayOnMap != null) {
            wayOnMap.remove();
            wayOnMap = null;
            MapUtils.hasRoute = false;
        }
    }

    @Override
    public void onBottomOffsetChanged(int bottomOffset) {
        if (map == null) {
            return;
        }
        map.setPadding(0, 0, 0, bottomOffset);
        boolean isActiveRide = mapViewModel.isActiveRide()
                && App.getDataManager().getCurrentRide() != null;
        boolean isRequestedState = mapViewModel.isRideRequested();
        boolean hasPickupMarker = pickupMarker != null;

        if (isActiveRide && !isRequestedState && hasPickupMarker) {
            if (mapViewModel.isDriverReached()) {
                bindCameraToCarInActiveRide(true);
            } else if (mapViewModel.isDriverAssigned()) {
                bindCameraToCarWhileAwaitingForDriver(true);
            }
        } else if (hasPickupMarker) {
            zoomCamera(pickupMarker, destinationMarker, true);
        }
    }

    /**
     * Zoom camera to show markers at maximum zoom.
     * If destination marker is null, zoom to show pickup marker.
     * Do nothing, if both markers are null.
     *
     * @param pickupMarker      marker for pickup position or null
     * @param destinationMarker marker for destination position or null
     * @param animated          show animation on zoom
     */
    private void zoomCamera(@Nullable Marker pickupMarker, @Nullable Marker destinationMarker, boolean animated) {
        if (pickupMarker != null && destinationMarker != null) {
            zoomCamera(pickupMarker.getPosition(), destinationMarker.getPosition(), animated);
        } else if (pickupMarker != null) {
            zoomCamera(pickupMarker.getPosition(), animated);
        }
    }

    /**
     * Zoom camera to show position (if provided)
     *
     * @param position position to show
     * @param animated show animation on zoom
     */
    private void zoomCamera(@Nullable LatLng position, boolean animated) {
        if (map != null && position != null) {
            int width = binding.map.getWidth();
            int height = binding.map.getHeight();
            // RA-9612: map should have dimensions
            if (width > 0 && height > 0) {
                // Note: All programmatic camera movements are calculated against size of the GoogleMap object
                // after first taking into account any padding that has been added to the map
                map.setPadding(0, 0, 0, 0);
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(position, Constants.DEFAULT_CAMERA_ZOOM);
                if (animated) {
                    map.animateCamera(cameraUpdate);
                } else {
                    map.moveCamera(cameraUpdate);
                }
                map.setPadding(0, 0, 0, mapViewModel.getBottomOffset());
            }
        }
    }

    /**
     * Zoom camera to show locations at maximum zoom.
     * Do nothing, if any location or map is null.
     *
     * @param pickup      pickup position or null
     * @param destination destination position or null
     * @param animated    show animation on zoom
     */

    private void zoomCamera(final LatLng pickup, final LatLng destination, boolean animated) {
        if (map != null && pickup != null && destination != null) {
            int width = binding.map.getWidth();
            int height = binding.map.getHeight();
            // RA-9612: map should have dimensions
            if (width > 0 && height > 0) {
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                builder.include(pickup);
                builder.include(destination);
                LatLngBounds bounds = builder.build();
                // Add temp paddings for map to display locations in visible area of the map
                // Use topPaddingMarker to add some more space on top for time pin.
                map.setPadding(paddingForMarker, topPaddingForMarker + getMapTopPadding(), paddingForMarker, mapViewModel.getBottomOffset() + avatarOffset);
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, width, height, 0);
                if (animated) {
                    map.animateCamera(cameraUpdate);
                } else {
                    map.moveCamera(cameraUpdate);
                }
                // Discard temp paddings immediately.
                // Animation would play fine and map won't relayout.
                map.setPadding(0, 0, 0, mapViewModel.getBottomOffset());
            }
        }
    }

    /**
     * Move camera to <code>position</code> without animation.
     *
     * @param position position to show
     */
    private void moveCamera(final LatLng position) {
        if (map != null && position != null) {
            int width = binding.map.getWidth();
            int height = binding.map.getHeight();
            // RA-9612: map should have dimensions
            if (width > 0 && height > 0) {
                // Note: All programmatic camera movements are calculated against size of the GoogleMap object
                // after first taking into account any padding that has been added to the map
                map.setPadding(0, 0, 0, 0);
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(position, Constants.DEFAULT_CAMERA_ZOOM));
                map.setPadding(0, 0, 0, mapViewModel.getBottomOffset());
            }
        }
    }

    private void animateCamera(final LatLng position, int duration) {
        if (map != null && position != null) {
            int width = binding.map.getWidth();
            int height = binding.map.getHeight();
            // RA-9612: map should have dimensions
            if (width > 0 && height > 0) {
                // Note: All programmatic camera movements are calculated against size of the GoogleMap object
                // after first taking into account any padding that has been added to the map
                map.setPadding(0, 0, 0, 0);
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(position, Constants.DEFAULT_CAMERA_ZOOM), duration, null);
                map.setPadding(0, 0, 0, mapViewModel.getBottomOffset());
            }
        }
    }


    /**
     * Returns map's current camera position.
     * Takes map paddings into consideration.
     *
     * @return current camera position
     */
    @Nullable
    private CameraPosition getCameraPosition() {
        CameraPosition cameraPosition = null;
        if (map != null) {
            map.setPadding(0, 0, 0, 0);
            cameraPosition = map.getCameraPosition();
            map.setPadding(0, 0, 0, mapViewModel.getBottomOffset());
        }
        return cameraPosition;
    }

    private int getMapTopPadding() {
        // map is the same size as mapContainer, so no need to subtract map's top
        return ViewUtils.getRectInParent(binding.destinationCardView, binding.mapContainer).bottom;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Don't know why, but removing this line breaks permission handling in RxPermission's ShadowActivity
        // but this line is not even executed... it's Android magic apparently...
        Timber.d("WTF is going on here??");
    }

    @Override
    public void drawDirection(TextView estimatedTime) {
        if (pickupMarker != null && destinationMarker != null) {
            disablePickupAddress();
            hidePickupLocationView();
        }
    }

    @Override
    public void activateLocationsForSearch() {
        enablePickupAddress();
        enableDestinationAddress();
    }

    public MapViewModel getMapViewModel() {
        return mapViewModel;
    }

    public MainFragmentMapBinding getBinding() {
        return binding;
    }

    private void updateActiveRide(Ride ride, boolean animated) {
        if (ride != null) {
            updateCarPosition(ride);
            buildDirection(ride, animated);
        }
    }

    private void buildDirection(Ride ride, boolean animated) {
        // RA-9059: skip ride events while map is not ready
        if (pickupMarker == null) {
            return;
        }
        if (mapViewModel.isDriverReached()) {
            bindCameraToCarInActiveRide(animated);
            pickupMarker.setTitle(getString(R.string.pickup_marker));
            pickupMarker.setIcon(BitmapDescriptorFactory.fromBitmap(ImageHelper.createBitmap(getActivity(), R.drawable.icon_green)));
        } else if (mapViewModel.isDriverAssigned()) {
            // RA-13214: ride has no active driver?
            Long seconds = ride != null && ride.getActiveDriver() != null ? ride.getActiveDriver().getDrivingTimeToRider() : null;
            if (seconds == null) {
                Exception e = new Exception(UNEXPECTED_STATE_KEY);
                Timber.e(e, "Driver assigned, but no driving time. Ride=" + ride);
            }
            String eta = LocalizeUtils.formatDriverEta(getContext(), seconds);
            App.getNotificationManager().notifyDriverEta(eta);
            Bitmap point = ImageHelper.drawTextOnMarker(getActivity(), eta);
            pickupMarker.setTitle(getString(R.string.eta_marker_with_eta, eta));
            pickupMarker.setIcon(BitmapDescriptorFactory.fromBitmap(point));
            bindCameraToCarWhileAwaitingForDriver(animated);
        }
        getDirection();
    }

    private void updateCarPosition(Ride ride) {
        if (ride == null || ride.getActiveDriver() == null || map == null) {
            return;
        }

        LatLng destinationPoint = new LatLng(ride.getActiveDriver().getLatitude(), ride.getActiveDriver().getLongitude());
        // initialize car marker
        Timber.d("::updateCarPosition:: Initialize driver marker:: Marker : %s", carMarker);
        if (carMarker == null) {
            Timber.d("::updateCarPosition:: carMarker == null");

            Optional<String> iconUrl = Optional.ofNullable(App.getDataManager().getRequestedCarType()).map(RequestedCarType::getMapIconUrl);
            // If there is no icon url, just show embedded car icon.
            // Otherwise, show empty marker and try to load car icon provided by server
            int markerRes = iconUrl.isPresent() ? R.drawable.transparent_pixel : R.drawable.car;

            carMarker = map.addMarker(new MarkerOptions()
                    .title(getString(R.string.car_marker))
                    .position(destinationPoint)
                    .icon(BitmapDescriptorFactory.fromResource(markerRes))
                    .anchor(0.5f, 0.5f));

            if (iconUrl.isPresent()) {
                carMarkerTarget = ImageHelper.loadCarIconIntoMarker(getContext(), iconUrl.get(), carMarker);
            }
        } else {
            Timber.d("::updateCarPosition:: carMarker != null");
            // check if animation required
            boolean needAnimation = carMarkerDriverId == ride.getActiveDriver().getId()
                    && needAnimateCarBetweenPositions(nextCarPosition, destinationPoint);

            if (needAnimation) {
                if (carAnimation != null) {
                    // cancel previous animation to prevent multiple runnables on same marker
                    carAnimation.cancel();
                }
                // reinitialize position of carMarker to make animation from new position, which we received from server
                carMarker.setPosition(new LatLng(nextCarPosition.latitude, nextCarPosition.longitude));
                // animate car marker
                carAnimation = MarkerAnimation.animateMarkerToGB(CAR_ANIMATION_MS, carMarker, destinationPoint, new LatLngInterpolator.Linear());
            } else {
                // set marker position without animation
                carMarker.setPosition(destinationPoint);
            }
        }
        // set rotation of car marker
        carMarker.setRotation(ride.getActiveDriver().getCourse());

        // save for next update animation
        nextCarPosition = destinationPoint;

        // save current marker's driver
        carMarkerDriverId = ride.getActiveDriver().getId();
    }

    // TODO: refactor to use driver's CarMarker, which already has this logic inside
    private boolean needAnimateCarBetweenPositions(LatLng startPosition, LatLng stopPosition) {
        if (!startPosition.equals(stopPosition)) {
            float distance = DistanceUtil.distance(startPosition, stopPosition);
            return distance > Constants.MIN_DISTANCE_BETWEEN_TO_ANIMATE_M && distance < Constants.MAX_DISTANCE_BETWEEN_TO_ANIMATE_M;
        }
        return false;
    }

    private void bindCameraToCarWhileAwaitingForDriver(boolean animated) {
        zoomCamera(carMarker != null ? carMarker : pickupMarker, pickupMarker, animated);
    }

    private void bindCameraToCarInActiveRide(boolean animated) {
        if (isCameraAutoAnimationEnabled) {
            zoomCamera(carMarker != null ? carMarker : pickupMarker, destinationMarker, animated);
        }
    }

    @Override
    public void onCarCategorySelected(RequestedCarType carType) {
        Timber.d("::onCarCategorySelected:: %s", carType);
        if (mapViewModel != null) {
            mapViewModel.loadDriversNearBy(mapViewModel.getPickupLocation(), true);
        }
        CarTypeConfiguration configuration = carType.getConfiguration();
        String description = carType.getDescription();
        if (configuration != null && configuration.getShowAlert() && description != null) {
            MaterialDialogCreator.createCarTypeDescriptionDialog(carType.getTitle().replaceAll("\\n", ""),
                    description.replaceAll("\\n", "<br/>"), getActivity());
        }
    }

    private void resetState() {
        needFillDriverDetailsData = true;
        enableAddressInput();

        MapPreferencesManager.clearSavedMarkersCoordinates();
        MapPreferencesManager.clearRideStatus();

        clearMap();
        showMyLocation(false);
    }

    private void enableAddressInput() {
        enablePickupAddress();
        enableDestinationAddress();
    }

    private void restoreCoordinates() {
        mapViewModel.getCurrentRideStatusEvent()
                .map(RideStatusEvent::getResp)
                .ifPresentOrElse(ride -> {
                    final Double startLong = ride.getStartLocationLong();
                    final Double startLat = ride.getStartLocationLat();
                    if (startLat != null && startLong != null) {
                        final LatLng startLocation = new LatLng(startLat, startLong);
                        if (map != null) {
                            addOrUpdatePickupMarker(map, getContext(), startLocation);
                        }
                    }
                    updateDestinationBasedOnRideState(ride);
                }, () -> {
                    if (map != null && App.getPrefs().isRideRequestShown()) {
                        final GeoPosition startPosition = App.getPrefs().getPickupGeoPosition();
                        final GeoPosition endPosition = App.getPrefs().getDestinationGeoPosition();
                        if (startPosition != null && startPosition.getLatLng() != null) {
                            addOrUpdatePickupMarker(map, getContext(), startPosition.getLatLng());
                        }
                        if (endPosition != null && endPosition.getLatLng() != null) {
                            addOrUpdateDestinationMarker(map, getContext(), endPosition.getLatLng());
                        }
                    }
                });
    }

    private void restoreAddresses() {
        Optional<GeoPosition> rideStart = mapViewModel.getCurrentRideStatusEvent()
                .map(RideStatusEvent::getResp)
                .map(Ride::getStartPosition);
        Optional<GeoPosition> cachedStart = Optional.ofNullable(App.getPrefs().getPickupGeoPosition());
        Optional<GeoPosition> startPosition = restoreGeoPosition(rideStart, cachedStart);
        if (startPosition.isPresent()) {
            binding.pickupAddress.setText(startPosition.get().getPlaceName());
            mapViewModel.setStartAddress(startPosition.get());
        } else {
            binding.pickupAddress.setText("");
        }

        Optional<GeoPosition> rideEnd = mapViewModel.getCurrentRideStatusEvent()
                .map(RideStatusEvent::getResp)
                .map(Ride::getEndPosition);
        Optional<GeoPosition> cachedEnd = Optional.ofNullable(App.getPrefs().getDestinationGeoPosition());
        Optional<GeoPosition> endPosition = restoreGeoPosition(rideEnd, cachedEnd);
        if (endPosition.isPresent()) {
            binding.destinationAddress.setText(endPosition.get().getPlaceName());
            mapViewModel.setDestinationAddress(endPosition.get());
        } else {
            binding.destinationAddress.setText("");
        }
        mapViewModel.updateCommentsVisibility();
        mapViewModel.restoreCommentsFromCache();
        mapViewModel.checkCampaign();
    }

    Optional<GeoPosition> restoreGeoPosition(Optional<GeoPosition> ridePosition, Optional<GeoPosition> cachedPosition) {
        if (ridePosition.map(GeoPosition::getLatLng).equals(cachedPosition.map(GeoPosition::getLatLng))) {
            // locations are equal, use cached location, as its address text might fit better
            return cachedPosition;
        }
        return ridePosition.or(() -> cachedPosition);
    }

    public void addOrUpdatePickupMarker(GoogleMap map, Context context, LatLng location) {
        pickupMarker = addOrUpdateMarker(pickupMarker, map, context, location, R.drawable.icon_green, R.string.pickup_marker);
    }

    public void addOrUpdateDestinationMarker(GoogleMap map, Context context, LatLng location) {
        destinationMarker = addOrUpdateMarker(destinationMarker, map, context, location, R.drawable.icon_red, R.string.destination_marker);
    }

    public void addOrUpdatePrevRideMarker(GoogleMap map, Context context, LatLng location) {
        prevRideMarker = addOrUpdateMarker(prevRideMarker, map, context, location, R.drawable.blue_pin, R.string.prev_ride_marker);
    }

    private Marker addOrUpdateMarker(Marker oldMarker, GoogleMap map, Context context, LatLng location, @DrawableRes int resId, @StringRes int description) {
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
        return map.addMarker(new MarkerOptions()
                .title(getString(description))
                .position(location)
                .icon(BitmapDescriptorFactory.fromBitmap(ImageHelper.createBitmap(context, resId))));
    }

    private void removePickupMarker() {
        if (pickupMarker != null) {
            pickupMarker.remove();
            pickupMarker = null;
        }
    }

    private void removeDestinationMarker() {
        if (destinationMarker != null) {
            destinationMarker.remove();
            destinationMarker = null;
        }
    }

    private void removePrevRideMarker() {
        if (prevRideMarker != null) {
            prevRideMarker.remove();
            prevRideMarker = null;
        }
    }

    private void resetPickupMarkerUi() {
        if (pickupMarker != null) {
            try {
                // reset pickup marker to green icon
                pickupMarker.setTitle(getString(R.string.pickup_marker));
                pickupMarker.setIcon(BitmapDescriptorFactory.fromBitmap(ImageHelper.createBitmap(getActivity(), R.drawable.icon_green)));
            } catch (Exception e) {
                Timber.e(e, "Unable to reset pickup marker UI");
            }
        }
    }

    @Override
    public void onCarTypesUpdated(List<RequestedCarType> carTypes) {
        Timber.d("::mapViewModel.getCarCategories:: Number of cars to display:  %d", carTypes.size());
        if (multipleCarFragment != null) {
            multipleCarFragment.setCarTypes(carTypes, mapViewModel.getPickupLocation());
            if (mapViewModel != null) {
                mapViewModel.reloadSurgeArea(mapViewModel.getPickupLocation());
            }
        }
    }

    @Override
    public void onSurgeAreasChanged() {
        multipleCarFragment.setPriorityCategories();
    }

    private boolean isPickupAddressEntered() {
        return mapViewModel.isPickAddressEntered();
    }

    private boolean isDestinationAddressEntered() {
        return !TextUtils.isEmpty(binding.destinationAddress.getText().toString());
    }

    public void setFemaleOnlyEnabled(boolean enabled) {
        mapViewModel.setFemaleOnlyEnabled(enabled);
    }

    public void setFingerprintedOnlyEnabled(boolean enabled) {
        mapViewModel.setFingerprintOnlyEnabled(enabled);
    }

    @Override
    public void onTouchMoved() {
        binding.pickupLocation.setVisibility(GONE);
        mapViewModel.resetTimeToNearestDriver();
    }

    @Override
    public void onTouched() {
        isCameraAutoAnimationEnabled = false;
        cameraAutoAnimationSubscription.unsubscribe();
    }

    @Override
    public void onTouchReleased(final boolean showMarker) {
        if (showMarker) {
            binding.pickupLocation.setVisibility(VISIBLE);
        }
        startCameraRelaxedTimer();
    }

    @Override
    public void onMapZoom(float zoomValue) {
        if (map != null) {
            map.setPadding(0, 0, 0, 0);
            map.animateCamera(CameraUpdateFactory.zoomBy(zoomValue), UPDATE_ZOOM_ANIMATION_MS, null);
            map.setPadding(0, 0, 0, mapViewModel.getBottomOffset());
        }
    }

    @Override
    public void onMapZoomIn() {
        if (map != null) {
            map.setPadding(0, 0, 0, 0);
            map.animateCamera(CameraUpdateFactory.zoomIn(), DOUBLE_TAP_ANIMATION_MS, null);
            map.setPadding(0, 0, 0, mapViewModel.getBottomOffset());
        }
    }

    @Override
    public void onMapScrollingEnabled(boolean enabled) {
        if (map != null) {
            map.getUiSettings().setScrollGesturesEnabled(enabled);
        }
    }

    private void startCameraRelaxedTimer() {
        cameraAutoAnimationSubscription.unsubscribe();
        cameraAutoAnimationSubscription = Observable.timer(10, TimeUnit.SECONDS, RxSchedulers.computation())
                .observeOn(RxSchedulers.main())
                .subscribe(aLong -> isCameraAutoAnimationEnabled = true);
    }

    private void swipeOutCampaign() {
        binding.campaignPanel.animate()
                .translationY(-binding.campaignPanel.getHeight())
                .setDuration(250)
                .withEndAction(() -> {
                    mapViewModel.hideCampaign();
                    new Handler().postDelayed(() -> binding.campaignPanel.setTranslationY(0.0f), 1000);
                });
    }

    public interface EventListener extends DriverDetailsFragment.DriverDetailsListener {
        void onShowCharityScreen();
    }

    private class SwipeUpListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (velocityY < -500) {
                swipeOutCampaign();
                return true;
            }
            return super.onFling(e1, e2, velocityX, velocityY);
        }
    }
}
