package com.rideaustin.ui.viewstub;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.MainThread;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.internal.MDButton;
import com.rideaustin.App;
import com.rideaustin.R;
import com.rideaustin.api.model.AvatarType;
import com.rideaustin.api.model.DriverLocation;
import com.rideaustin.api.model.Ride;
import com.rideaustin.api.model.driver.RequestedCarType;
import com.rideaustin.api.model.surgearea.SurgeArea;
import com.rideaustin.base.ApiSubscriber;
import com.rideaustin.base.ApiSubscriber2;
import com.rideaustin.base.BaseFragment;
import com.rideaustin.databinding.RequestRideStubBinding;
import com.rideaustin.entities.GeoPosition;
import com.rideaustin.events.RideStatusEvent;
import com.rideaustin.schedulers.RxSchedulers;
import com.rideaustin.ui.drawer.promotions.PromotionsActivity;
import com.rideaustin.ui.estimate.FareEstimateActivityHelper;
import com.rideaustin.ui.map.DriversNearByHelper;
import com.rideaustin.ui.map.IDrawDirectionForRide;
import com.rideaustin.ui.map.MainMapFragment;
import com.rideaustin.ui.map.MapViewModel;
import com.rideaustin.ui.map.UnmetRequirementType;
import com.rideaustin.ui.map.fare.PriorityFareDialog;
import com.rideaustin.ui.map.fare.PriorityFareDialogListener;
import com.rideaustin.ui.ride.RideStatusService;
import com.rideaustin.ui.utils.MaterialDialogCreator;
import com.rideaustin.utils.CarCategoriesHelper;
import com.rideaustin.utils.Constants;
import com.rideaustin.utils.DialogUtils;
import com.rideaustin.utils.NetworkHelper;
import com.rideaustin.utils.RetryWhenNoNetwork;
import com.rideaustin.utils.toast.RAToast;

import org.joda.time.DateTimeConstants;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import java8.util.Optional;
import rx.Observable;
import rx.Subscription;
import rx.subscriptions.CompositeSubscription;
import rx.subscriptions.Subscriptions;
import timber.log.Timber;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.rideaustin.api.model.RideStatus.RIDER_CANCELLED;
import static com.rideaustin.utils.CommonConstants.UNEXPECTED_STATE_KEY;

/**
 * Created by vokol on 30.06.2016.
 */
public class RequestRideFragment extends BaseFragment implements PriorityFareDialogListener {

    public static final String PICKUP_LOCATION_IS_STILL_LOADING = "Pickup location is still loading";
    public static final int RIDE_NOT_EXISTS = 0;

    private enum UiState {
        INITIAL, REQUESTING_WITH_RIDE, REQUESTING_WITHOUT_RIDE
    }

    private RequestRideStubBinding binding;
    private MainMapFragment mainMapFragment;
    private MapViewModel mapViewModel;
    private MaterialDialog cancelDialog;

    private CompositeSubscription prioritySubscriptions = new CompositeSubscription();
    private CompositeSubscription surgeAreaSubscriptions = new CompositeSubscription();
    private Subscription nearestDriversSubscription = Subscriptions.empty();
    private CompositeSubscription timeEstimateSubscriptions = new CompositeSubscription();
    private Subscription femaleModeSubscription = Subscriptions.empty();

    private IDrawDirectionForRide iDrawDirectionForRide;
    private UiState uiState = UiState.INITIAL;

    public static RequestRideFragment newInstance() {
        return new RequestRideFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        this.binding = DataBindingUtil.inflate(inflater, R.layout.request_ride_stub, container, false);

        mainMapFragment = (MainMapFragment) getActivity().getSupportFragmentManager()
                .findFragmentByTag(MainMapFragment.class.getName());
        mapViewModel = mainMapFragment.getMapViewModel();

        mainMapFragment.getBinding().cancelPendingRequest.setOnClickListener(v -> showCancelRideConfirmation());
        mainMapFragment.getBinding().cancelPendingRequest.setVisibility(GONE);

        binding.btnFareEstimate.setOnClickListener(v ->
                FareEstimateActivityHelper.startFareEstimateActivity(getActivity(), mapViewModel));

        hideTimeEstimate();

        binding.btnCancelPickup.setOnClickListener(v -> {
            mainMapFragment.onPickupCancelled();
            mainMapFragment.checkRoundUp(true);
            mapViewModel.updateCommentsVisibility();
        });

        binding.btnRequestRide.setOnClickListener(view -> {
            view.setEnabled(false);
            onRequestRideClicked();
        });

        binding.btnPromo.setOnClickListener(v ->
                startActivity(new Intent(getActivity(), PromotionsActivity.class)));

        binding.btnRequestRide.setText(R.string.request_ride);
        App.getDataManager().getSliderCarTypeObservable()
                .map(RequestedCarType::getTitle)
                .observeOn(RxSchedulers.main())
                .subscribe((title) -> binding.btnRequestRide.setText(getString(R.string.request_category, title)), Timber::e);

        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        updateUiState();
        subscribeToTimeEstimate();
        subscribeToFemaleMode();
    }

    private void showCancelRideConfirmation() {
        if (getActivity() == null) {
            try {
                // RA-14820
                Exception e = new Exception(UNEXPECTED_STATE_KEY);
                Timber.e(e, "Cancel ride request while fragment is not attached");
                mainMapFragment.getBinding().cancelPendingRequest.setVisibility(View.GONE);
            } catch (Exception e) {
                Timber.e(e);
            }
            return;
        }
        DialogUtils.dismiss(cancelDialog);
        cancelDialog = MaterialDialogCreator.createCancelRideDialog((AppCompatActivity) getActivity());
        cancelDialog.setContent(App.getInstance().getString(R.string.text_cancel_ride_dialog));
        cancelDialog.setOnShowListener(dialogInterface -> {
            MDButton positiveButton = cancelDialog.getActionButton(DialogAction.POSITIVE);
            positiveButton.setOnClickListener(v -> {
                doOnCancelRide();
                dialogInterface.dismiss();
            });
        });
    }

    private void doOnCancelRide() {
        if (App.getPrefs().hasRideId()) {
            cancelRide(App.getPrefs().getRideId(), AvatarType.RIDER.name());
        } else {
            binding.mainContainer.setVisibility(VISIBLE);
        }
        iDrawDirectionForRide.activateLocationsForSearch();
        mainMapFragment.showRequestRideFragment();
        mainMapFragment.hideRequestRideState();
    }

    private void updateUiState() {
        if (binding == null) {
            // RA-9598, RA-9669: this method can be called from map fragment
            // before this fragment started and it's view inflated
            return;
        }
        if (uiState == null) {
            clearState();
            return;
        }
        switch (uiState) {
            case INITIAL:
                showInitialState();
                break;
            case REQUESTING_WITH_RIDE:
                showRequestingStateWithRide();
                break;
            case REQUESTING_WITHOUT_RIDE:
                showRequestingStateWithoutRide();
                break;
        }
    }

    private void showInitialState() {
        updateBottomOffset();
        binding.mainContainer.setVisibility(VISIBLE);
        mainMapFragment.getBinding().cancelPendingRequest.setVisibility(GONE);
        mainMapFragment.getBinding().pickupAddress.setEnabled(true);
        binding.btnRequestRide.setEnabled(true);
        DialogUtils.dismiss(cancelDialog);
    }

    private void showRequestingStateWithRide() {
        mapViewModel.postBottomOffset(0);
        binding.mainContainer.setVisibility(GONE);
        mainMapFragment.getBinding().cancelPendingRequest.setVisibility(VISIBLE);
        binding.btnRequestRide.setEnabled(false);
    }

    private void showRequestingStateWithoutRide() {
        mapViewModel.postBottomOffset(0);
        binding.mainContainer.setVisibility(GONE);
        mainMapFragment.getBinding().cancelPendingRequest.setVisibility(View.GONE);
        binding.btnRequestRide.setEnabled(false);
    }

    private void clearState() {
        mapViewModel.postBottomOffset(0);
        binding.mainContainer.setVisibility(GONE);
        mainMapFragment.getBinding().cancelPendingRequest.setVisibility(View.GONE);
        binding.btnRequestRide.setEnabled(false);
        DialogUtils.dismiss(cancelDialog);
    }

    public void updateBottomOffset() {
        if (binding == null) {
            return;
        }
        binding.mainContainer.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (isAttached()) {
                    int measuredHeight = binding.mainContainer.getMeasuredHeight();
                    if (measuredHeight > 0) {
                        mapViewModel.postBottomOffset(measuredHeight + ((ViewGroup.MarginLayoutParams) binding.mainContainer.getLayoutParams()).bottomMargin);
                    }
                }
                binding.mainContainer.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
    }

    private void onRequestRideClicked() {
        UnmetRequirementType type = mapViewModel.getUnmetRequirementType();
        if (type != UnmetRequirementType.NONE) {
            mainMapFragment.doOnUnmetRequirement(type);
            return;
        }
        if (!NetworkHelper.isNetworkAvailable()) {
            RAToast.show(R.string.network_error, Toast.LENGTH_SHORT);
            binding.btnRequestRide.setEnabled(true);
            return;
        }
        GeoPosition startAddress = mapViewModel.getStartAddress();
        if (startAddress == null) {
            Timber.w(PICKUP_LOCATION_IS_STILL_LOADING);
            RAToast.show(PICKUP_LOCATION_IS_STILL_LOADING, Toast.LENGTH_SHORT);
            binding.btnRequestRide.setEnabled(true);
            return;
        }

        showProgress(getString(R.string.waiting_for_drivers));

        Integer cityId = App.getConfigurationManager().getLastConfiguration().getCurrentCity().getCityId();
        surgeAreaSubscriptions.add(App.getDataManager().getSurgeAreasService()
                .getSurgeAreasList(startAddress.getLat(), startAddress.getLng(), cityId)
                .map(resp -> resp.surgeAreas)
                .onErrorResumeNext(error -> {
                    Timber.e(error, "Cannot get surge area from server, returning local");
                    return Observable.just(Collections.emptyList());
                })
                .switchIfEmpty(Observable.just(Collections.emptyList()))
                .subscribeOn(RxSchedulers.network())
                .observeOn(RxSchedulers.main())
                .doOnTerminate(this::hideProgress)
                .doOnUnsubscribe(this::hideProgress)
                .subscribe((surgeAreas) -> {
                    if (surgeAreas.size() > 0) {
                        onSurgeAreaLoaded(surgeAreas);
                    } else {
                        onSurgeAreaLoaded(App.getDataManager().getSurgeAreas());
                    }
                }, this::onSurgeAreaLoadFailed));
    }

    private void onSurgeAreaLoadFailed(Throwable error) {
        Timber.e(error, "Cannot load surge area");
        onSurgeAreaLoaded(App.getDataManager().getSurgeAreas());
    }

    private void onSurgeAreaLoaded(Collection<SurgeArea> surgeAreas) {
        Optional<RequestedCarType> selectedCarType = App.getDataManager().getSliderSelectedCarType();
        if (!selectedCarType.isPresent()) {
            CarCategoriesHelper
                    .fetchCarCategoriesAvailableAtLocation(mapViewModel.getPickupLocation())
                    .subscribeOn(RxSchedulers.network())
                    .observeOn(RxSchedulers.main())
                    .subscribe(new ApiSubscriber2<List<RequestedCarType>>(getCallback()) {
                        @Override
                        public void onNext(List<RequestedCarType> requestedCarTypes) {
                            if (!requestedCarTypes.isEmpty()) {
                                App.getDataManager().setSliderSelectedCarType(requestedCarTypes.get(0));
                                onCarCategoryAvailable(surgeAreas, requestedCarTypes.get(0));
                            } else {
                                RAToast.show(R.string.no_car_categories_error_msg, Toast.LENGTH_SHORT);
                            }
                        }
                    });
        } else {
            onCarCategoryAvailable(surgeAreas, selectedCarType.get());
        }
    }

    private void onCarCategoryAvailable(final Collection<SurgeArea> surgeAreas, final RequestedCarType carType) {
        App.getDataManager().setSurgeArea(surgeAreas);
        if (!App.getDataManager().isSurge(carType.getCarCategory(), mapViewModel.getPickupLocation())) {
            performRequesting(false, carType.getCarCategory());
            return;
        }

        Bundle bundle = new Bundle();
        Optional<SurgeArea> surgeArea = App.getDataManager().findSurgeArea(carType.getCarCategory(), mapViewModel.getPickupLocation());
        if (!surgeArea.isPresent()) {
            String surgeAreaError = "Unable to find surge area for category: " + carType.getCarCategory() + " and location: " + mapViewModel.getPickupLocation();
            RAToast.show(surgeAreaError, Toast.LENGTH_SHORT);
            Timber.e(surgeAreaError);
            return;
        }

        bundle.putSerializable(Constants.SURGE_AREA, surgeArea.get());
        bundle.putSerializable(Constants.CAR_TYPE, carType);
        bundle.putParcelable(Constants.LOCATION, mapViewModel.getPickupLocation());

        PriorityFareDialog priorityFareDialog = PriorityFareDialog.createNew(this);
        priorityFareDialog.setArguments(bundle);

        prioritySubscriptions.clear();
        prioritySubscriptions.add(priorityFareDialog.getResultPublishSubject()
                .filter(Boolean::booleanValue)
                .subscribe(result -> performRequesting(true, carType.getCarCategory()), Timber::e));
        priorityFareDialog.show(getChildFragmentManager());
    }

    private void performRequesting(boolean accepted, String selectedCarCategory) {
        if (mapViewModel.isPickAddressEntered()) {
            mapViewModel.postBottomOffset(0);
            iDrawDirectionForRide.drawDirection(binding.tvEstimatePickupTime);
            binding.mainContainer.setVisibility(GONE);
            final Intent requestRideIntent = createRequestRideIntent(accepted, selectedCarCategory);
            getActivity().startService(requestRideIntent);

            mainMapFragment.getBinding().pickupLocation.setVisibility(View.GONE);
        }
    }

    private Intent createRequestRideIntent(boolean surgeAccepted, String selectedCarCategory) {
        Intent intent = new Intent(App.getInstance(), RideStatusService.class);

        intent.putExtra(Constants.START_ADDRESS, mapViewModel.getStartAddress());
        intent.putExtra(Constants.SURGE_ACCEPTED, surgeAccepted);
        intent.putExtra(Constants.DESTINATION_ADDRESS, mapViewModel.getDestinationAddress());
        intent.putExtra(Constants.PICKUP_COMMENTS, mapViewModel.getOptionalComments().orElse(null));
        intent.putExtra(Constants.SELECTED_CAR_CATEGORY, selectedCarCategory);
        return intent;
    }

    @Override
    public void onStop() {
        super.onStop();
        prioritySubscriptions.clear();
        timeEstimateSubscriptions.clear();
        surgeAreaSubscriptions.clear();
        nearestDriversSubscription.unsubscribe();
        femaleModeSubscription.unsubscribe();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        uiState = null;
        clearState();
    }

    @MainThread
    public void restoreInitialUIState() {
        uiState = UiState.INITIAL;
        updateUiState();
    }

    @MainThread
    public void restoreRequestingStateWithRide() {
        uiState = UiState.REQUESTING_WITH_RIDE;
        updateUiState();
    }

    @MainThread
    public void restoreRequestingStateWithoutRide() {
        uiState = UiState.REQUESTING_WITHOUT_RIDE;
        updateUiState();
    }


    void cancelRide(long rideId, String avatarType) {
        RideStatusService.stop();
        App.getDataManager().cancelRide(rideId, avatarType)
                .subscribeOn(RxSchedulers.network())
                .observeOn(RxSchedulers.main())
                .retryWhen(new RetryWhenNoNetwork(1000))
                .subscribe(isSuccess -> {
                    App.getPrefs().updateRideInfo(0L, RIDER_CANCELLED.name());
                    App.getStateManager().post(RideStatusEvent.riderCancelled());
                    Ride ride = new Ride();
                    ride.setId(rideId);
                    ride.setStatus(RIDER_CANCELLED.name());
                    App.getNotificationManager().notifyRiderCancelled(ride);
                }, throwable -> {
                    RideStatusService.startIfNeeded();
                    Timber.e(throwable, "Error when cancelling ride");
                });
    }

    private void subscribeToTimeEstimate() {
        timeEstimateSubscriptions.clear();
        timeEstimateSubscriptions.add(Observable.interval(0, 30, TimeUnit.SECONDS, RxSchedulers.computation())
                .startWith(0L)
                .subscribeOn(RxSchedulers.computation())
                .observeOn(RxSchedulers.main())
                .subscribe(time -> loadDriversNearBy(), throwable -> Timber.e(throwable, "Error on estimate request")));
    }

    private void subscribeToFemaleMode() {
        femaleModeSubscription = App.getDataManager().isFemaleOnlyAllowedObservable()
                .observeOn(RxSchedulers.main())
                .subscribe(isFemaleMode -> {
                    if (isFemaleMode) {
                        binding.btnRequestRide.setBackgroundResource(R.drawable.rounded_pink_button_states);
                    } else {
                        binding.btnRequestRide.setBackgroundResource(R.drawable.rounded_blue_button_states);
                    }
                    if (mainMapFragment != null) {
                        mainMapFragment.decorateRequestRideState(isFemaleMode);
                    }
                }, Timber::e);
    }

    public void setDrawDirection(IDrawDirectionForRide drawInterface) {
        this.iDrawDirectionForRide = drawInterface;
    }

    private void showEstimatedPickupTime(int time) {
        binding.tvEstimatePickupTime.setVisibility(VISIBLE);
        if (time >= 1) {
            binding.tvEstimatePickupTime.setText(getResources().getQuantityString(R.plurals.text_pickup_time, time, time));
        } else {
            binding.tvEstimatePickupTime.setText(getString(R.string.text_pickup_time_less_tan_min));
        }
    }

    private void hideTimeEstimate() {
        binding.tvEstimatePickupTime.setVisibility(View.INVISIBLE);
    }

    public void loadDriversNearBy() {
        GeoPosition position = mapViewModel.getStartAddress();
        if (position == null) {
            hideTimeEstimate();
            return;
        }

        nearestDriversSubscription.unsubscribe();
        nearestDriversSubscription = DriversNearByHelper.getAvailableDriversNearBy(position.getLatLng())
                .subscribeOn(RxSchedulers.network())
                .observeOn(RxSchedulers.main())
                .subscribe(new ApiSubscriber<List<DriverLocation>>(null) {
                    @Override
                    public void onNext(List<DriverLocation> drivers) {
                        // Collections.min might throw NoSuchElementException in case when list is empty
                        // so we need to prevent it
                        if (drivers.isEmpty()) {
                            return;
                        }
                        DriverLocation driverLocation = Collections.min(drivers, (lhs, rhs) -> lhs.getDrivingTimeToRider() - rhs.getDrivingTimeToRider());
                        showEstimatedPickupTime(driverLocation.getDrivingTimeToRider() / DateTimeConstants.SECONDS_PER_MINUTE);
                    }
                });
    }

    @Override
    public void onDialogDismiss() {
        binding.btnRequestRide.setEnabled(true);
    }
}
