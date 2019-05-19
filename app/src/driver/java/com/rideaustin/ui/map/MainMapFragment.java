package com.rideaustin.ui.map;

import android.Manifest;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.annotation.UiThread;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.internal.MDButton;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.rideaustin.App;
import com.rideaustin.R;
import com.rideaustin.api.config.GlobalConfig;
import com.rideaustin.api.config.RideCancellationConfig;
import com.rideaustin.api.model.QueueResponse;
import com.rideaustin.api.model.Ride;
import com.rideaustin.api.model.RiderLocationUpdate;
import com.rideaustin.api.model.UpgradeRequestStatus;
import com.rideaustin.api.model.driver.Driver;
import com.rideaustin.databinding.MainFragmentMapBinding;
import com.rideaustin.engine.EngineState;
import com.rideaustin.manager.LocationSettingsException;
import com.rideaustin.manager.MissingLocationPermissionException;
import com.rideaustin.manager.location.RALocation;
import com.rideaustin.schedulers.RxSchedulers;
import com.rideaustin.ui.base.FloatNavigatorBaseFragment;
import com.rideaustin.ui.contact.RideContactDialog;
import com.rideaustin.ui.drawer.NavigationDrawerActivity;
import com.rideaustin.ui.genericsupport.GenericContactSupportActivity;
import com.rideaustin.ui.map.strategy.AcceptedStrategy;
import com.rideaustin.ui.map.strategy.OfflineStrategy;
import com.rideaustin.ui.map.strategy.OnlineStrategy;
import com.rideaustin.ui.map.strategy.StrategyFactory;
import com.rideaustin.ui.map.strategy.UIStrategy;
import com.rideaustin.ui.stacked.NextRideDialog;
import com.rideaustin.ui.terms.TermsActivity;
import com.rideaustin.ui.utils.CommonMaterialDialogCreator;
import com.rideaustin.ui.utils.MaterialDialogCreator;
import com.rideaustin.ui.widgets.dialogs.RADialog;
import com.rideaustin.ui.widgets.dialogs.RADialogIcon;
import com.rideaustin.utils.Constants;
import com.rideaustin.utils.DeviceInfoUtil;
import com.rideaustin.utils.DialogUtils;
import com.rideaustin.utils.PermissionUtils;
import com.rideaustin.utils.ViewUtils;
import com.rideaustin.utils.toast.RAToast;
import com.tbruyelle.rxpermissions.RxPermissions;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import java8.util.Optional;
import rx.Subscription;
import rx.subscriptions.CompositeSubscription;
import rx.subscriptions.Subscriptions;
import timber.log.Timber;

import static com.google.android.gms.maps.GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE;

/**
 * Created by yshloma on 22.06.2016.
 */
public class MainMapFragment extends FloatNavigatorBaseFragment implements OnMapReadyCallback,
        MapFragmentInterface,
        MapFragmentInterface.MapPaddingListener,
        MapViewModel.MapViewListener,
        MapManager.MapDimensionsProvider,
        DistanceTracker.DestinationReachedListener {

    private MainFragmentMapBinding binding;
    private MapManager mapManager;
    private MapViewModel mapViewModel;

    private UIStrategy currentStrategy = null;
    private boolean isMapReady = false;
    private boolean isTermsReminderShown = false;
    @Nullable
    private MenuType menuType;
    private Subscription locationSubscription = Subscriptions.empty();
    private Subscription surgeAreasSubscription = Subscriptions.empty();
    private Subscription permissionSubscription = Subscriptions.empty();
    private CompositeSubscription commonSubscriptions = new CompositeSubscription();

    private int actionBarHeight;
    private List<Dialog> dialogsToDismiss = new LinkedList<>();
    private MaterialDialog confirmCancelDialog;
    private MaterialDialog motionDetectedDialog;
    private MaterialDialog arriveNotAllowedDialog;
    private MaterialDialog tripConfirmationDialog;
    private MaterialDialog updateDialog;
    private MaterialDialog noLocationDialog;
    private MaterialDialog termsDialog;
    private RADialog rideUpgradeDialog;
    private RateRiderDialog rateRiderDialog;
    private NextRideDialog nextRideDialog;
    private RideContactDialog rideContactDialog;
    private ValueAnimator paddingAnimator;

    private DistanceTracker distanceTracker = new DistanceTracker();
    private int topPadding = 0;
    private int bottomPadding = 0;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        // setup existing fragments if any
        getPendingAcceptFragment(false);
        getRiderActionsFragment(false);
        getPickupDestinationFragment(false);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable final Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.main_fragment_map, container, false);
        mapViewModel = new MapViewModel(this);
        binding.setMapModel(mapViewModel);
        binding.mapView.onCreate(savedInstanceState);
        mapManager = new MapManager(getContext(), this, mapViewModel);
        binding.myLocation.setOnClickListener(v -> {
            if (isMapReady) {
                mapManager.moveToDriverLocation();
                mapViewModel.setTrackDriverLocation(true);
            }
        });
        binding.upgradeCarType.setOnClickListener(v -> {
            mapViewModel.onUpgradeCarTypeClicked();
        });
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        TypedValue typedValue = new TypedValue();
        if (getActivity().getTheme().resolveAttribute(android.R.attr.actionBarSize, typedValue, true)) {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(typedValue.data, getResources().getDisplayMetrics());
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!App.getDataManager().isLoggedIn()) {
            // no sense to proceed
            return;
        }
        distanceTracker.setDestinationReachedListener(this);
        updateToolbar();
        mapViewModel.onStart();
        askPermissions();
    }

    @Override
    public void onResume() {
        super.onResume();
        binding.mapView.onResume();
        if (PermissionUtils.isLocationPermissionGranted(getActivity())) {
            binding.mapView.getMapAsync(MainMapFragment.this);
            binding.mapView.setVisibility(View.VISIBLE);
        }
        if (!App.getLocationManager().isLocationOn()) {
            RAToast.show(R.string.please_enable_gps_msg, Toast.LENGTH_LONG);
        }
        clearDismissedDialogs();
        updateFabMenu();
    }

    @Override
    public void onPause() {
        super.onPause();
        binding.mapView.onPause();
        // unsubscribe to prevent strategy updates after detach
        mapViewModel.onMapClose();
        if (currentStrategy != null) {
            // detach before onSaveInstanceState to prevent
            // possible illegal state on fragment transaction
            currentStrategy.detach();
        }
        isMapReady = false;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        if (menuType != null) {
            switch (menuType) {
                case CONTACT:
                    createContactMenu(menu, inflater);
                    break;
                case SUPPORT:
                    createSupportMenu(menu, inflater);
                    break;
                case DECLINE:
                    createDeclineMenu(menu, inflater);
                    break;
                default:
                    break;
            }
        }
    }

    private void createContactMenu(Menu menu, MenuInflater inflater) {
        createButtonMenu(menu, inflater, R.string.menu_contact);
    }

    private void createSupportMenu(Menu menu, MenuInflater inflater) {
        createButtonMenu(menu, inflater, R.string.menu_support);
    }

    private void createDeclineMenu(Menu menu, MenuInflater inflater) {
        createButtonMenu(menu, inflater, R.string.menu_decline);
    }

    private void createButtonMenu(Menu menu, MenuInflater inflater, @StringRes int title) {
        inflater.inflate(R.menu.button_menu, menu);
        MenuItem menuItem = menu.findItem(R.id.item);
        Button button = (Button) menuItem.getActionView().findViewById(R.id.button);
        button.setText(title);
        button.setOnClickListener(v -> onOptionsItemSelected(menuItem));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (currentStrategy != null && currentStrategy.hasView()) {
            return currentStrategy.onMenuItemSelected(item.getItemId());
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStop() {
        super.onStop();
        distanceTracker.setDestinationReachedListener(null);
        mapViewModel.onStop();
        commonSubscriptions.clear();
        permissionSubscription.unsubscribe();
        surgeAreasSubscription.unsubscribe();
        locationSubscription.unsubscribe();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mapViewModel.onDestroyView();
        commonSubscriptions.clear();
        permissionSubscription.unsubscribe();
        surgeAreasSubscription.unsubscribe();
        locationSubscription.unsubscribe();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding.mapView.onDestroy();
        commonSubscriptions.clear();
        permissionSubscription.unsubscribe();
        surgeAreasSubscription.unsubscribe();
        locationSubscription.unsubscribe();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (!isResumed()) {
            Timber.d("::onMapReady:: not resumed");
            return;
        }

        if (!PermissionUtils.isLocationPermissionGranted(getActivity())) {
            Timber.d("::onMapReady:: no permission");
            return;
        }

        if (isMapReady) {
            return;
        }

        Timber.d("::onMapReady:: ok");
        //googleMap.setMyLocationEnabled(true);
        googleMap.setOnMarkerClickListener(marker -> true); // This will prevent marker titles being opened.
        googleMap.setContentDescription(getString(R.string.google_map));
        googleMap.getUiSettings().setMyLocationButtonEnabled(false);
        googleMap.getUiSettings().setRotateGesturesEnabled(false);
        googleMap.getUiSettings().setMapToolbarEnabled(false);
        googleMap.getUiSettings().setTiltGesturesEnabled(false);
        googleMap.setOnCameraMoveStartedListener(reason -> {
            if (reason == REASON_GESTURE) {
                mapViewModel.setTrackDriverLocation(false);
            }
        });


        isMapReady = true;
        mapManager.setGoogleMap(googleMap);
        mapViewModel.onMapReady();
        listenToSurgeAreas();
    }

    @Override
    public void updateFabMenu() {
        binding.upgradeCarType.setVisibility(mapViewModel.isUpgradeButtonEnabled() ? View.VISIBLE : View.GONE);
    }

    private void listenToSurgeAreas() {
        surgeAreasSubscription.unsubscribe();
        surgeAreasSubscription = mapViewModel.getSurgeAreasObservable()
                .observeOn(RxSchedulers.main())
                .onErrorReturn(throwable -> {
                    Timber.e(throwable);
                    return new ArrayList<>();
                })
                .subscribe(mapManager::setSurgeAreasResponsesList);
    }

    private void askPermissions() {
        // IMPORTANT: ask permissions onStart(), not onResume(), to prevent infinite requests
        if (!PermissionUtils.isLocationPermissionGranted(getActivity())) {
            permissionSubscription.unsubscribe();
            permissionSubscription = new RxPermissions(getActivity())
                    .request(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
                    .subscribe(isGranted -> {
                        if (isGranted) {
                            binding.mapView.getMapAsync(MainMapFragment.this);
                            binding.mapView.setVisibility(View.VISIBLE);
                            App.getConfigurationManager().onLocationPermissionGranted();
                        } else {
                            RAToast.showShort(getString(R.string.dont_have_permission, App.getAppName()));
                        }
                    });
        }
    }

    private void updateToolbar() {
        if (currentStrategy != null) {
            if (currentStrategy.hasView()) {
                currentStrategy.updateToolbar();
            }
        } else {
            // sometimes, title is empty when open without permissions
            // need to set default title here
            clearToolbarTitle();
            showAppNameTitle();
        }
    }

    @Override
    public void loadSurgeAreas() {
        mapViewModel.loadSurgeAreas();
    }

    @Override
    public void clearSurgeAreas() {
        mapViewModel.clearSurgeAreas();
        mapManager.clearPolygonGroundOverlay();
    }

    @Override
    public void showConfirmCancelDialog() {
        Activity activity = getActivity();
        if (activity == null) {
            return;
        }
        RideCancellationConfig config = App.getConfigurationManager().getLastConfiguration().getRideCancellationConfig();
        if (config != null && config.isEnabled()) {
            App.getInstance().getStateManager().getCurrentRideIfAny().ifPresent(ride -> {
                ((NavigationDrawerActivity) activity).showRideCancellationFeedback(ride.getId(), this::onRideCancelConfirmed);
            });

        } else {
            confirmCancelDialog = MaterialDialogCreator.createConfirmationDialog(getString(R.string.text_cancel_ride_confirmation), activity);
            confirmCancelDialog.setOnShowListener(dialogInterface -> {
                MDButton positiveButton = confirmCancelDialog.getActionButton(DialogAction.POSITIVE);
                positiveButton.setOnClickListener(v -> {
                    onRideCancelConfirmed(null, null);
                    dialogInterface.dismiss();
                });
            });
        }
    }

    private void onRideCancelConfirmed(String code, @Nullable String comment) {
        if (currentStrategy != null) {
            currentStrategy.onCancelRideSelected(code, comment);
        }
    }

    @Override
    public void hideConfirmCancelDialog() {
        tryToDismiss(confirmCancelDialog);
        confirmCancelDialog = null;
        if (getActivity() != null) {
            ((NavigationDrawerActivity) getActivity()).hideRideCancellationFeedback();
        }
    }

    @Override
    public void showMotionDetectedDialog(EngineState state) {
        final boolean arrived = state.getType().equals(EngineState.Type.ARRIVED);
        motionDetectedDialog = MaterialDialogCreator.createForgotToStartEndTripDialog(arrived ? getString(R.string.motion_detected_start_dialog_title)
                        : getString(R.string.motion_detected_end_dialog_title),
                arrived ? getString(R.string.motion_detected_dialog_message) : getString(R.string.motion_detected_finish),
                (dialog, which) -> currentStrategy.onFlowAction(true),
                (AppCompatActivity) getActivity());
    }

    @Override
    public void hideMotionDetectedDialog() {
        tryToDismiss(motionDetectedDialog);
        motionDetectedDialog = null;
    }

    @Override
    public void showInactiveWarning() {
        binding.inactiveWarning.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideInactiveWarning() {
        binding.inactiveWarning.setVisibility(View.GONE);
    }

    @Override
    public void showTopFragment(Fragment fragment) {
        Fragment f = getSupportFragmentManager().findFragmentById(R.id.topPanel);
        if (f != fragment) {
            replaceFragment(fragment, R.id.topPanel, false);
        }
    }

    @Override
    public void hideTopFragment() {
        Fragment f = getSupportFragmentManager().findFragmentById(R.id.topPanel);
        if (f != null) {
            removeFragment(f);
            onTopPaddingUpdated(0);
        }
    }

    @Override
    public void showBottomFragment(Fragment fragment) {
        Fragment f = getSupportFragmentManager().findFragmentById(R.id.bottomPanel);
        if (f != fragment) {
            replaceFragment(fragment, R.id.bottomPanel, false);
        }
    }

    @Override
    public void hideBottomFragment() {
        Fragment f = getSupportFragmentManager().findFragmentById(R.id.bottomPanel);
        if (f != null) {
            removeFragment(f);
            onBottomPaddingUpdated(0);
        }
    }

    @Override
    public PickupDestinationFragment getPickupDestinationFragment() {
        return getPickupDestinationFragment(true);
    }

    @Nullable
    private PickupDestinationFragment getPickupDestinationFragment(boolean create) {
        PickupDestinationFragment fragment = null;
        Fragment f = getSupportFragmentManager().findFragmentById(R.id.topPanel);
        if (f instanceof PickupDestinationFragment) {
            fragment = (PickupDestinationFragment) f;
        } else if (create) {
            fragment = new PickupDestinationFragment();
        }
        if (fragment != null) {
            fragment.setMapPaddingListener(this);
        }
        return fragment;
    }

    @Override
    public RideActionsFragment getRiderActionsFragment() {
        return getRiderActionsFragment(true);
    }

    @Nullable
    private RideActionsFragment getRiderActionsFragment(boolean create) {
        RideActionsFragment fragment = null;
        Fragment f = getSupportFragmentManager().findFragmentById(R.id.bottomPanel);
        if (f instanceof RideActionsFragment) {
            fragment = (RideActionsFragment) f;
        } else if (create) {
            fragment = new RideActionsFragment();
        }
        if (fragment != null) {
            fragment.setMapPaddingListener(this);
        }
        return fragment;
    }

    @Override
    public PendingAcceptFragment getPendingAcceptFragment() {
        return getPendingAcceptFragment(true);
    }

    @Nullable
    private PendingAcceptFragment getPendingAcceptFragment(boolean create) {
        PendingAcceptFragment fragment = null;
        Fragment f = getSupportFragmentManager().findFragmentById(R.id.bottomPanel);
        if (f instanceof PendingAcceptFragment) {
            fragment = (PendingAcceptFragment) f;
        } else if (create) {
            fragment = new PendingAcceptFragment();
        }
        if (fragment != null) {
            fragment.setMapPaddingListener(this);
        }
        return fragment;
    }

    @Override
    public void showRide(List<LatLng> direction, @NonNull LatLng pickup, @Nullable LatLng destination) {
        mapManager.showRide(direction, pickup, destination);
    }

    @Override
    public void hideRide() {
        mapManager.hideRide();
    }

    @Override
    public void updateRiderLocation(RiderLocationUpdate locationUpdate) {
        if (locationUpdate != null && locationUpdate.getLat() != null && locationUpdate.getLng() != null) {
            mapManager.addOrUpdateRiderMarker(new LatLng(locationUpdate.getLat(), locationUpdate.getLng()));
        }
    }

    @Override
    public void showLoading() {
        showProgress();
    }

    @Override
    public void hideLoading() {
        hideProgress();
    }

    @Override
    public void showRatingWidget(double driverPayment, float startingRating, OnRateChangedListener onRateChanged, OnButtonClickListener onClicked) {
        if (rateRiderDialog != null) {
            rateRiderDialog.dismiss();
        }
        rateRiderDialog = RateRiderDialog.newInstance(onClicked, onRateChanged, startingRating, driverPayment);
        rateRiderDialog.show(getChildFragmentManager(), "rateRiderDialog");
    }

    @Override
    public void hideRatingWidget() {
        try {
            rateRiderDialog.dismiss();
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    @Override
    public void showDriverOnMap(boolean zoomToDriver) {
        locationSubscription.unsubscribe();
        locationSubscription = App.getLocationManager()
                .getLocationUpdates()
                // start with cached location (may be invalid or too old)
                .startWith(App.getLocationManager().getLastOfDefault())
                .filter(RALocation::isValid)
                .subscribe(raLocation -> {
                    Timber.d("loaded location %s", raLocation);
                    hideNoLocationAvailableDialog();
                    mapManager.showDriver(raLocation, zoomToDriver);
                    distanceTracker.setDestinationLocation(mapManager.getPickupLocation());
                    distanceTracker.updateDriverLocation(raLocation.getCoordinates());
                }, this::doOnLocationError);
    }

    @Override
    public void hideDriverOnMap() {
        mapManager.hideDriverIcon();
        locationSubscription.unsubscribe();
    }

    @Override
    public void showGenericContactSupport() {
        if (getActivity() != null) {
            startActivity(new Intent(getActivity(), GenericContactSupportActivity.class));
        }
    }

    @Override
    public void showTermsDialog(String message) {
        DialogUtils.dismiss(termsDialog);
        String title = App.getAppName();
        termsDialog = MaterialDialogCreator.createDialogWithCallback(getActivity(), title, message, R.string.read,
                (dialog, which) -> showTerms(true))
                .cancelable(false)
                .show();
    }

    /**
     * Show terms UI in one of the cases:
     * a) terms are not accepted yet and reminder was not shown in this session
     * b) someone decided to show it with {@code force} flag
     *
     * @param force whether to show terms anyway
     */
    private void showTerms(boolean force) {
        boolean shouldShow = force
                || (!App.getDataManager().getDriver().map(Driver::isAgreedToLegalTerms).orElse(true) && !isTermsReminderShown);
        if (shouldShow) {
            commonSubscriptions.add(App.getConfigurationManager()
                    .getLastAndRequestUpdates()
                    // default embedded config has empty terms by default
                    // wait until config contains terms
                    .filter(config -> config.getCurrentTerms() != null && !config.getCurrentTerms().isEmpty())
                    // take only one valid config
                    .take(1)
                    .subscribe(response -> {
                        startActivity(new Intent(getActivity(), TermsActivity.class));
                        isTermsReminderShown = true;
                    }));
        }
    }

    @Override
    public void showNextRideDialog() {
        hideNextRideDialog();
        if (getFragmentManager() != null) {
            nextRideDialog = new NextRideDialog();
            nextRideDialog.show(getFragmentManager(), NextRideDialog.class.getName());
        }
    }

    @Override
    public void hideNextRideDialog() {
        if (nextRideDialog != null) {
            try {
                nextRideDialog.dismiss();
                nextRideDialog = null;
            } catch (Exception e) {
                Timber.e(e);
            }
        }
    }

    private void doOnLocationError(Throwable throwable) {
        mapManager.hideDriverIcon();
        if (throwable instanceof MissingLocationPermissionException) {
            showMissingLocationPermissionMessage();
        } else if (throwable instanceof LocationSettingsException) {
            LocationSettingsException settingsException = (LocationSettingsException) throwable;
            Status status = settingsException.getResult().getStatus();
            // TODO: consider other status codes as well
            if (status.getStatusCode() == LocationSettingsStatusCodes.RESOLUTION_REQUIRED) {
                try {
                    status.startResolutionForResult(getActivity(), 0);
                } catch (Exception e) {
                    Timber.e(e, "Unable to request location resolution settings");
                    showNoLocationAvailableDialog();
                }
            }
        } else {
            showNoLocationAvailableDialog();
        }
    }

    private void tryToDismiss(Dialog dialog) {
        // RA-9801: can be called when host is paused
        // (and about to be destroyed?)
        if (!DialogUtils.dismiss(dialog) && DialogUtils.isShowing(dialog)) {
            // schedule to dismiss the dialog on next resume
            dialogsToDismiss.add(dialog);
        }
    }

    private void clearDismissedDialogs() {
        for (Dialog dialog : dialogsToDismiss) {
            // RA-9801: dialog was dismissed without legal context
            // try again when host window is ready.
            DialogUtils.dismiss(dialog);
        }
        dialogsToDismiss.clear();
    }


    @Override
    public void showNoLocationAvailableDialog() {
        if (noLocationDialog == null && isAttached()) {
            noLocationDialog = MaterialDialogCreator.createSimpleErrorDialog(getString(R.string.location_error), getActivity());
            noLocationDialog.setOnDismissListener(dialog -> noLocationDialog = null);
            noLocationDialog.show();
        }
    }

    @Override
    public void hideNoLocationAvailableDialog() {
        DialogUtils.dismiss(noLocationDialog);
        noLocationDialog = null;
    }

    @Override
    public void showNearestDrivers(List<RALocation> driverLocations) {
        mapManager.showOtherDrivers(driverLocations);
    }

    @Override
    public void hideNearestDrivers() {
        mapManager.clearOtherDrivers();
    }

    public void showMissingLocationPermissionMessage() {
        RAToast.showShort(App.getInstance().getString(R.string.dont_have_permission, App.getAppName()));
    }

    @Override
    public void showMenu(@NonNull MenuType type) {
        menuType = type;
        setHasOptionsMenu(true);
        if (getActivity() != null) {
            getActivity().invalidateOptionsMenu();
        }
    }

    @Override
    public void hideMenu() {
        menuType = null;
        setHasOptionsMenu(false);
    }

    @Override
    @Nullable
    public MenuType getMenuType() {
        return menuType;
    }

    @Override
    public void showRideContactDialog(final Ride ride) {
        hideRideContactDialog();
        if (getFragmentManager() != null) {
            rideContactDialog = new RideContactDialog();
            rideContactDialog.setRide(ride);
            rideContactDialog.show(getFragmentManager(), RideContactDialog.class.getName());
        }
    }

    @Override
    public void hideRideContactDialog() {
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
    public void navigateTo(String place) {
        getFloatNavigatorCallback().navigateTo(place);
    }

    @Override
    public void navigateTo(LatLng place) {
        getFloatNavigatorCallback().navigateTo(place);
    }

    @Override
    public void setToolbarTitle(String title) {
        getCallback().setToolbarTitle(title);
    }

    @Override
    public void setToolbarSubtitle(String subtitle) {
        getCallback().setToolbarSubtitle(subtitle);
    }

    /**
     * Must be called after setting toolbar title and subtitle for left justify
     *
     * @param photoUrl
     */
    @Override
    public void setAvatar(String photoUrl) {
        getCallback().setToolbarAvatar(photoUrl);
    }

    @Override
    public void clearToolbarTitle() {
        getCallback().clearToolbarTitles();
    }

    @Override
    public void showAppNameTitle() {
        getCallback().setToolbarTitle(App.getFormattedAppName());
    }

    @Override
    public void showToolbarButton(@StringRes int stringId, ButtonStyle style, OnButtonClickListener onClicked, boolean progress) {
        if (getActivity() == null) {
            // edge case - strategy is attached but fragment not
            // not sure how it may happen, strategy seems respect fragment's lifecycle
            return;
        }
        Button button = ((NavigationDrawerActivity) getActivity()).getToolbarActionButton();
        applyToolbarButtonStyle(button, style);
        button.setVisibility(View.VISIBLE);
        button.setText(stringId);
        button.setOnClickListener(v -> onClicked.onClicked());

        if (button.isEnabled() == progress) {
            button.setEnabled(!progress);
            if (paddingAnimator != null) {
                paddingAnimator.cancel();
            }
            int startValue = button.getPaddingStart();
            int endValue = progress ? getResources().getDimensionPixelSize(R.dimen.padding_14dp) : 0;
            paddingAnimator = ValueAnimator.ofInt(startValue, endValue);
            paddingAnimator.addUpdateListener(valueAnimator -> button.setPaddingRelative((Integer) paddingAnimator.getAnimatedValue(), 0, 0, 0));
            paddingAnimator.setDuration(Constants.ANIMATION_DELAY_MS);
            paddingAnimator.start();
            ProgressBar progressBar = ((NavigationDrawerActivity) getActivity()).getToolbarActionProgress();
            progressBar.setVisibility(progress && !DeviceInfoUtil.isEspresso() ? View.VISIBLE : View.GONE);
        }
    }

    private void applyToolbarButtonStyle(Button button, ButtonStyle style) {
        switch (style) {
            case FATAL:
                button.setBackgroundResource(R.drawable.bg_toolbar_action_fatal);
                break;
            case WARN:
                button.setBackgroundResource(R.drawable.bg_toolbar_action_warn);
                break;
            case REGULAR:
                button.setBackgroundResource(R.drawable.bg_toolbar_action_regular);
                break;
        }
    }

    @Override
    public void showPickupMarker(final LatLng latLng) {
        Timber.d("::showPickupMarker::");
        mapManager.showPickupLocationOnMap(latLng);
    }

    @Override
    public void showDestinationMarker(final Optional<LatLng> latLng) {
        Timber.d("::showPickupMarker::");
        mapManager.showDestinationLocationOnMap(latLng);
    }

    @Override
    public void showNextRideMarker(Optional<LatLng> latLng) {
        mapManager.showNextRideLocationOnMap(latLng);
    }

    @Override
    public void hidePickupMarker() {
        Timber.d("::hidePickupMarker::");
        mapManager.removePickupMarker();
    }

    @Override
    public void zoomToCurrentLocationWithGivenLocation(final double lat, final double lng) {
        Timber.d("::zoomToCurrentLocationWithGivenLocation:: (%f, %f)", lat, lng);
        commonSubscriptions.add(App.getLocationManager().getLastLocation(true, true)
                .subscribeOn(RxSchedulers.computation())
                .observeOn(RxSchedulers.main())
                .subscribe(driverLocation -> {
                    mapManager.zoomToFit(driverLocation.getCoordinates(), new LatLng(lat, lng));
                }, Timber::e));
    }

    @UiThread
    @Override
    public void onConfigurationUpdated(final GlobalConfig globalConfig) {
        updateToolbar();
    }

    @Override
    public void onUiStateChanged(EngineState engineState) {
        Timber.d(":::onUiStateChanged:: %s", engineState.getType());
        if (currentStrategy != null) {
            currentStrategy.detach();
            currentStrategy.destroy(MainMapFragment.this);
        }

        currentStrategy = StrategyFactory.createStrategyForState(engineState);
        if (currentStrategy instanceof AcceptedStrategy) {
            distanceTracker.reset();
        } else if (currentStrategy instanceof OnlineStrategy) {
            mapViewModel.checkUpdate();
            showTerms(false);
        } else if (currentStrategy instanceof OfflineStrategy) {
            showTerms(false);
        }

        currentStrategy.attach(MainMapFragment.this);
        mapManager.updatePaddings();
        updateFabMenu();
    }

    @Override
    public void onUpgradeNeeded() {
        DialogUtils.dismiss(updateDialog);
        updateDialog = CommonMaterialDialogCreator.createMandatoryUpdateDialog(getActivity());
    }

    @Override
    public void showRideUpgradeAcceptedDialog() {
        RADialog.Builder builder = new RADialog.Builder()
                .setIcon(RADialogIcon.CHECK)
                .setTitle(R.string.upgrade_rider_confirmed)
                .setContent(R.string.upgraded_to_suv)
                .setOnDismissListener(() -> rideUpgradeDialog = null);
        if (rideUpgradeDialog == null) {
            rideUpgradeDialog = builder.build();
            rideUpgradeDialog.show(getFragmentManager());
        } else {
            builder.apply(rideUpgradeDialog);
            if (!rideUpgradeDialog.isVisible()) {
                rideUpgradeDialog.show(getFragmentManager());
            }
        }
    }

    @Override
    public void showRideUpgradeFailedDialog(UpgradeRequestStatus status) {
        int titleId = status.equals(UpgradeRequestStatus.EXPIRED)
                ? R.string.upgrade_expired
                : R.string.upgrade_rider_denied;
        RADialog.Builder builder = new RADialog.Builder()
                .setIcon(RADialogIcon.CROSS)
                .setTitle(titleId)
                .setContent(R.string.upgrade_failed)
                .setOnDismissListener(() -> rideUpgradeDialog = null);
        if (rideUpgradeDialog == null) {
            rideUpgradeDialog = builder.build();
            rideUpgradeDialog.show(getFragmentManager());
        } else {
            builder.apply(rideUpgradeDialog);
            if (!rideUpgradeDialog.isVisible()) {
                rideUpgradeDialog.show(getFragmentManager());
            }
        }
    }

    @Override
    public void onRideUpgradeRequested() {
        if (rideUpgradeDialog == null) {
            rideUpgradeDialog = new RADialog.Builder()
                    .setSingleActionListener(() -> {
                        rideUpgradeDialog.dismiss();
                        mapViewModel.onCancelUpgradeClicked();
                    })
                    .setIcon(RADialogIcon.WAITING)
                    .setTitle(R.string.upgrade_to_suv)
                    .setContent(R.string.upgrade_waiting_rider)
                    .setSingleActionText(R.string.upgrade_cancel)
                    .setOnDismissListener(() -> rideUpgradeDialog = null)
                    .build();
            rideUpgradeDialog.show(getFragmentManager());
        } else {
            if (!rideUpgradeDialog.isVisible()) {
                rideUpgradeDialog.show(getFragmentManager());
            }
        }
    }

    @Override
    public void clearRideUpgradeDialog() {
        if (rideUpgradeDialog != null) {
            rideUpgradeDialog.dismiss();
            rideUpgradeDialog = null;
        }
    }

    @Override
    public void showArriveNotAllowedDialog(int meters) {
        hideArriveNotAllowedDialog();
        arriveNotAllowedDialog = MaterialDialogCreator.createSimpleErrorDialog(getString(R.string.arrive_not_allowed_dialog_msg, meters), getContext());
    }

    @Override
    public void hideArriveNotAllowedDialog() {
        tryToDismiss(arriveNotAllowedDialog);
        arriveNotAllowedDialog = null;
    }

    @Override
    public void showStartTripConfirmationDialog(Runnable onConfirmed) {
        hideTripConfirmationDialog();
        tripConfirmationDialog = MaterialDialogCreator.createConfirmationDialog(getString(R.string.starting_trip_confirmation_dialog_msg)
                , getContext()
                , (dialog1, which) -> onConfirmed.run()
                , (dialog1, which) -> {
                });
    }

    @Override
    public void showFinishTripConfirmationDialog(Runnable onConfirmed) {
        hideTripConfirmationDialog();
        tripConfirmationDialog = MaterialDialogCreator.createConfirmationDialog(getString(R.string.ending_trip_confirmation_dialog_msg)
                , getContext()
                , (dialog1, which) -> onConfirmed.run()
                , (dialog1, which) -> {
                });
    }

    @Override
    public void hideTripConfirmationDialog() {
        tryToDismiss(tripConfirmationDialog);
        tripConfirmationDialog = null;
    }

    @Override
    public void onRideUpgradeCancelled(String message) {
        if (rideUpgradeDialog != null) {
            CommonMaterialDialogCreator.showSupportSuccessDialog(getActivity(), Optional.ofNullable(message), false, () -> rideUpgradeDialog = null);
        }
    }

    @Override
    public int getMapWidth() {
        return binding.mapView.getWidth();
    }

    @Override
    public int getMapHeight() {
        return binding.mapView.getHeight();
    }

    @Override
    public int getMapTopPadding() {
        int topPadding = actionBarHeight;
        topPadding += binding.topPanel.getHeight();
        topPadding += binding.errorPanel.getHeight();
        return topPadding;
    }

    @Override
    public int getMapBottomPadding() {
        return bottomPadding;
    }

    @Override
    public void onTopPaddingUpdated(int value) {
        if (topPadding == value) {
            return;
        }
        topPadding = value;
        mapManager.updatePaddings();
    }

    @Override
    public void onBottomPaddingUpdated(int value) {
        if (bottomPadding == value) {
            return;
        }
        bottomPadding = value;
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams)binding.fabContainer.getLayoutParams();
        if (lp == null) {
            lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        lp.bottomMargin = Math.max(value, (int) ViewUtils.dpToPixels(15));
        binding.fabContainer.setLayoutParams(lp);
        mapManager.updatePaddings();
    }

    @Override
    public void onDestinationReached() {
        if (currentStrategy != null && currentStrategy instanceof AcceptedStrategy) {
            // switches to the next state (Arrived)
            currentStrategy.onFlowAction(false);
            // showing notification
            MaterialDialogCreator.createInfoDialog(
                    App.getAppName(),
                    getString(R.string.you_have_arrived_to_pickup_location),
                    (AppCompatActivity) getActivity()
            );
        } else {
            Timber.v("::onDestinationReached:: Current strategy %s", currentStrategy);
        }
    }

    @Override
    public void showGoingOnlinePopup(String selectedCar, String message) {
        MaterialDialogCreator.createOnlinePopup(selectedCar, message, getActivity()).show();
    }

    @Override
    public void settleQueueList(List<QueueResponse> queues) {
        if (getActivity() != null) {
            ((NavigationDrawerActivity) getActivity()).settleQueueList(queues);
        }
    }
}
