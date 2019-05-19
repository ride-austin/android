package com.rideaustin.ui.viewstub;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.internal.MDButton;
import com.rideaustin.App;
import com.rideaustin.R;
import com.rideaustin.api.model.AvatarType;
import com.rideaustin.api.model.Ride;
import com.rideaustin.api.model.RideStatus;
import com.rideaustin.api.model.driver.ActiveDriver;
import com.rideaustin.base.ApiSubscriber;
import com.rideaustin.base.ApiSubscriber2;
import com.rideaustin.base.BaseFragment;
import com.rideaustin.databinding.DriverDetailsStubBinding;
import com.rideaustin.schedulers.RxSchedulers;
import com.rideaustin.ui.estimate.FareEstimateActivityHelper;
import com.rideaustin.ui.map.CollapsibleElement;
import com.rideaustin.ui.map.CollapsibleElementsListener;
import com.rideaustin.ui.map.MapViewModel;
import com.rideaustin.ui.utils.MaterialDialogCreator;
import com.rideaustin.utils.Constants;
import com.rideaustin.utils.DialogUtils;
import com.rideaustin.utils.ShareUtils;
import com.rideaustin.utils.TimeUtils;
import com.rideaustin.utils.ViewUtils;

import java.net.HttpURLConnection;
import java.util.concurrent.TimeUnit;

import java8.util.Optional;
import rx.Observable;
import rx.Subscription;
import rx.subscriptions.Subscriptions;
import timber.log.Timber;


/**
 * Created by vokol on 01.07.2016.
 */
public class DriverDetailsFragment extends BaseFragment implements CollapsibleElement {

    private Subscription cancellationCountDown;
    private MaterialDialog cancellationFeeDialog;
    private DriverDetailsListener listener;
    private DriverDetailsStubBinding binding;
    private final DriverDetailsViewModel detailsViewModel = new DriverDetailsViewModel();

    private ActiveDriver activeDriver;
    private MapViewModel mapViewModel;
    private BottomSheetBehavior bottomSheetBehavior;
    private CollapsibleElementsListener collapsibleElementsListener;
    private Subscription cancelRideSubscription = Subscriptions.empty();

    public static DriverDetailsFragment newInstance(ActiveDriver activeDriver,
                                                    DriverDetailsListener listener,
                                                    final MapViewModel mapViewModel) {
        DriverDetailsFragment driverDetailsFragment = new DriverDetailsFragment();
        driverDetailsFragment.setListener(listener);
        driverDetailsFragment.activeDriver = activeDriver;
        driverDetailsFragment.mapViewModel = mapViewModel;
        return driverDetailsFragment;
    }

    public void setListener(DriverDetailsListener listener) {
        this.listener = listener;
    }

    public boolean needUpdate(@Nullable Ride ride) {
        Optional<Integer> previousId = Optional.ofNullable(activeDriver).map(ActiveDriver::getId);
        Optional<Integer> currentId = Optional.ofNullable(ride).map(Ride::getActiveDriver).map(ActiveDriver::getId);
        return !previousId.equals(currentId);
    }

    private void updateBottomOffset() {
        if (isAttached()) {
            int measuredHeight;
            if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                int imageHeight = binding.driverImage.getMeasuredHeight();
                measuredHeight = binding.rideDetails.getMeasuredHeight() - imageHeight / 2;
            } else {
                int imageHeight = binding.driverImageSmall.getMeasuredHeight();
                measuredHeight = bottomSheetBehavior.getPeekHeight() - imageHeight / 2;
            }

            if (measuredHeight > 0 && mapViewModel != null) {
                mapViewModel.postBottomOffset(measuredHeight);
            }
        }
    }

    private boolean isCancellationFree(final ActiveDriver activeDriver) {
        if (activeDriver != null) {
            if (App.getDataManager().getRequestedCarType() != null && !TextUtils.isEmpty(App.getDataManager().getRequestedCarType().getTitle())) {
                String requestedCategory = App.getDataManager().getRequestedCarType().getTitle().toLowerCase();
                String hondaCategory = Constants.CarCategory.HONDA_CAR.getRequestValue().toLowerCase();

                if (requestedCategory.contains(hondaCategory)) {
                    return true;
                }
            } else {
                return false;
            }
        }
        return false;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.driver_details_stub, container, false);
        binding.setDetailsViewModel(detailsViewModel);
        binding.markImage.setRotation(180);
        binding.driverContainerSmall.setVisibility(View.VISIBLE);
        binding.driverContainer.setVisibility(View.VISIBLE);
        binding.carContainer.setVisibility(View.VISIBLE);

        bottomSheetBehavior = BottomSheetBehavior.from(binding.rideDetails);
        bottomSheetBehavior.setHideable(false);
        bottomSheetBehavior.setPeekHeight((int) ViewUtils.dpToPixels(120));
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        stateCollapsed();

        binding.rideDetails.setOnClickListener(v -> {
            if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            } else {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                stateTransition();
                if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    stateCollapsed();
                } else if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    stateExpanded();
                }
                updateBottomOffset();
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                binding.containerLayout.setAlpha(slideOffset);
                binding.driverContainerSmall.setAlpha(1 - slideOffset);
            }
        });

        binding.cancelTrip.setOnClickListener(v -> onCancelRideClicked());
        binding.fareEstimateButton.setOnClickListener(v -> FareEstimateActivityHelper.startFareEstimateActivity(getActivity(), mapViewModel));
        //
        // This feature have been removed, but maybe we will add it later.
        // https://issue-tracker.devfactory.com/browse/RA-9298
        //binding.promoCodeButton.setOnClickListener(v -> startActivity(new Intent(getActivity(), PromotionsActivity.class)));
        //

        if (collapsibleElementsListener != null) {
            collapsibleElementsListener.registerCollapsibleElements(this);
        }

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (collapsibleElementsListener != null) {
            collapsibleElementsListener.unregisterCollapsibleElement(this);
        }
    }

    private void stateCollapsed() {
        binding.containerLayout.setAlpha(0);
        binding.containerLayout.setVisibility(View.INVISIBLE);
        binding.driverContainerSmall.setAlpha(1);
        binding.driverContainerSmall.setVisibility(View.VISIBLE);
    }

    private void stateExpanded() {
        binding.containerLayout.setAlpha(1);
        binding.containerLayout.setVisibility(View.VISIBLE);
        binding.driverContainerSmall.setAlpha(0);
        binding.driverContainerSmall.setVisibility(View.INVISIBLE);
        if (collapsibleElementsListener != null) {
            collapsibleElementsListener.notifyExpansionOf(this);
        }
    }

    private void stateTransition() {
        binding.containerLayout.setVisibility(View.VISIBLE);
        binding.driverContainerSmall.setVisibility(View.VISIBLE);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!App.getDataManager().isLoggedIn()) {
            // RA-13225: app is going to be restarted
            return;
        }
        initSplitFareButton();
        initLiveEtaButton();
        detailsViewModel.onStart();
        detailsViewModel.fillDriverDetailsView(activeDriver, binding, getActivity());
        detailsViewModel.freeCancellationExpiresOn.addOnPropertyChangedCallback(freePeriodExpiredCallback);
        binding.rideDetails.getViewTreeObserver().addOnGlobalLayoutListener(onGlobalLayoutListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        detailsViewModel.onStop();
        detailsViewModel.freeCancellationExpiresOn.removeOnPropertyChangedCallback(freePeriodExpiredCallback);
        binding.rideDetails.getViewTreeObserver().removeOnGlobalLayoutListener(onGlobalLayoutListener);
    }

    private void onCancelRideClicked() {
        if (cancellationFeeDialog != null) {
            DialogUtils.dismiss(cancellationFeeDialog);
        }
        cancellationFeeDialog = MaterialDialogCreator.createCancelRideDialog((AppCompatActivity) getActivity());
        setCancellationPromptMessage(cancellationFeeDialog);
        cancellationFeeDialog.setOnShowListener(dialogInterface -> {
            MDButton positiveButton = cancellationFeeDialog.getActionButton(DialogAction.POSITIVE);
            positiveButton.setOnClickListener(v1 -> {
                cancelRideConfirmed();
                dialogInterface.dismiss();
            });
        });
        cancellationFeeDialog.setOnDismissListener(dialog -> stopTimer());
    }

    private void cancelRideConfirmed() {
        showProgress(getString(R.string.cancelling_ride));
        cancelRideSubscription.unsubscribe();
        cancelRideSubscription = App.getDataManager().getRidesService()
                .getCurrentRide(AvatarType.RIDER.name())
                .flatMap(rideResponse -> {
                    // if no active ride, just go further
                    // no need to cancel anything
                    if (rideResponse == null) {
                        return Observable.just(false);
                    }
                    // make sure the ride has not started. this might happen if the rider taps cancel,
                    // but does not complete the alert action before the driver starts trip
                    if (RideStatus.valueOf(rideResponse.getStatus()) == RideStatus.ACTIVE) {
                        String message = App.getInstance().getString(R.string.message_active_ride_cancel);
                        return Observable.error(ApiSubscriber2.createError(HttpURLConnection.HTTP_BAD_REQUEST, message));
                    }
                    return App.getDataManager().cancelRide(rideResponse.getId(), AvatarType.RIDER.name());
                })
                .subscribeOn(RxSchedulers.network())
                .observeOn(RxSchedulers.main())
                .doOnTerminate(this::hideProgress)
                .doOnUnsubscribe(this::hideProgress)
                .subscribe(new ApiSubscriber2<Object>(getCallback()) {
                    @Override
                    public void onCompleted() {
                        App.getPrefs().updateRideInfo(0L, "");
                        // wait for dialog to clear ride state in map view model
                        // see MapViewModel.clearRideState() usage
                    }
                });
    }


    private void initLiveEtaButton() {
        binding.shareLiveEta.setOnClickListener(v -> App.getDataManager().getRideEtaShareText()
                .observeOn(RxSchedulers.main())
                .subscribe(new ApiSubscriber<String>(getCallback()) {
                    @Override
                    public void onNext(String shareText) {
                        Timber.d("::getRideEtaShareText:: hareText: %s", shareText);
                        ShareUtils.shareText(App.getInstance(), shareText);
                    }
                }));
    }

    private void initSplitFareButton() {
        binding.btnFareSplit.setOnClickListener(v -> {
            Timber.d("::on Split Fare clicked::");
            if (listener != null) {
                listener.onSplitFare();
            }
        });
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        Timber.d("::onHiddenChanged:: Hidden: %s", hidden);
        if (hidden) {
            stopTimer();
            dismissCancellationDialog();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        cancelRideSubscription.unsubscribe();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopTimer();
        dismissCancellationDialog();
    }

    private void stopTimer() {
        if (cancellationCountDown != null && !cancellationCountDown.isUnsubscribed()) {
            cancellationCountDown.unsubscribe();
            cancellationCountDown = null;
        }
    }

    private void dismissCancellationDialog() {
        DialogUtils.dismiss(cancellationFeeDialog);
        cancellationFeeDialog = null;
    }

    private void setCancellationPromptMessage(final MaterialDialog dialog) {
        stopTimer();
        String message = App.getInstance().getString(R.string.text_cancel_ride_dialog);
        Long freePeriodExpiresOn = detailsViewModel.freeCancellationExpiresOn.get();

        if (isCancellationFree(activeDriver) || freePeriodExpiresOn == null) {
            Timber.d("::setCancellationPromptMessage:: cancel free");
            dialog.setContent(message);
            return;
        }

        if (TimeUtils.currentTimeMillis() >= freePeriodExpiresOn) {
            //Time elapsed show cancellation fee
            Timber.d("::setCancellationPromptMessage:: free period expired");
            cancellationWithFee(dialog);
        } else {
            //We have time to cancel
            stopTimer();
            Timber.d("::setCancellationPromptMessage:: start countdown=" + ((freePeriodExpiresOn - TimeUtils.currentTimeMillis()) / 1000));
            cancellationCountDown = Observable.interval(0/*initial delay*/, 1, TimeUnit.SECONDS, RxSchedulers.computation())
                    .observeOn(RxSchedulers.main())
                    .subscribe(counter -> {
                        long seconds = (detailsViewModel.freeCancellationExpiresOn.get() - TimeUtils.currentTimeMillis()) / 1000;
                        Float fee = 0f;
                        if (!TextUtils.isEmpty(detailsViewModel.getCancellationFee())) {
                            fee = Float.valueOf(detailsViewModel.getCancellationFee());
                        }
                        if (cancellationFeeDialog != null) {
                            if (seconds <= 0) {
                                Timber.d("::setCancellationPromptMessage:: free period expired");
                                cancellationWithFee(cancellationFeeDialog);
                                stopTimer();
                            } else {
                                cancellationFeeDialog.setContent(Html.fromHtml(getString(R.string.cancel_trip_without_cancellation_fee, seconds, fee)));
                            }
                        }
                    }, throwable -> Timber.e(throwable, "Error on cancellation countdown"));
        }
    }

    private void cancellationWithFee(MaterialDialog dialog) {
        Float cancellationFee = 0f;
        if (!TextUtils.isEmpty(detailsViewModel.getCancellationFee())) {
            cancellationFee = Float.valueOf(detailsViewModel.getCancellationFee());
        }
        dialog.setContent(Html.fromHtml(getString(R.string.cancel_trip_with_cancellation_fee, cancellationFee)));
    }

    public void tripActive() {
        detailsViewModel.tripActive();
    }

    @Override
    public void collapseSelf() {
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    @Override
    public void setCollapsibleListener(CollapsibleElementsListener listener) {
        this.collapsibleElementsListener = listener;
    }

    public interface DriverDetailsListener {
        void onSplitFare();
    }

    private ViewTreeObserver.OnGlobalLayoutListener onGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            updateBottomOffset();
            binding.rideDetails.getViewTreeObserver().removeOnGlobalLayoutListener(this);
        }
    };

    private android.databinding.Observable.OnPropertyChangedCallback freePeriodExpiredCallback = new android.databinding.Observable.OnPropertyChangedCallback() {
        @Override
        public void onPropertyChanged(android.databinding.Observable observable, int i) {
            if (cancellationFeeDialog != null) {
                setCancellationPromptMessage(cancellationFeeDialog);
            }
        }
    };
}
