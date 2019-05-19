package com.rideaustin.ui.estimate;

import android.content.Intent;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.databinding.ObservableField;
import android.os.Bundle;

import com.rideaustin.App;
import com.rideaustin.api.model.campaigns.Campaign;
import com.rideaustin.base.ApiSubscriber2;
import com.rideaustin.entities.GeoPosition;
import com.rideaustin.schedulers.RxSchedulers;
import com.rideaustin.utils.Constants;
import com.rideaustin.utils.location.LocationHelper;

import java.io.IOException;
import java.util.Locale;

import java8.util.Optional;
import rx.Observable;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

/**
 * Created by vokol on 30.06.2016.
 */
public class FareEstimateViewModel extends BaseObservable {

    private final Intent intent;
    private final FareEstimateActivity activity;
    private final CompositeSubscription subscriptions = new CompositeSubscription();

    @Bindable
    private ObservableField<String> fareEstimate = new ObservableField<>();

    @Bindable
    private ObservableField<String> riderPickup = new ObservableField<>();

    @Bindable
    private ObservableField<String> riderDestination = new ObservableField<>();

    public FareEstimateViewModel(Intent intent, FareEstimateActivity activity) {
        this.intent = intent;
        this.activity = activity;
    }

    public void showFareEstimateAmount() {
        Bundle extras = intent.getExtras();
        final GeoPosition startAddress = extras.getParcelable(Constants.START_ADDRESS);
        final GeoPosition destinationAddress = extras.getParcelable(Constants.DESTINATION_ADDRESS);

        riderPickup.set(startAddress.getPlaceName());
        riderDestination.set(destinationAddress.getPlaceName());

        final boolean isSurge = extras.getBoolean(Constants.SURGE_AREA);

        activity.showProgress();
        subscriptions.add(App.getConfigurationManager()
                .getConfigurationUpdates()
                .first()
                .flatMap(globalConfig -> {
                    final Integer cityId = globalConfig.getCurrentCity().getCityId();
                    return getFareEstimate(startAddress.getLat(), startAddress.getLng(), destinationAddress.getLat(), destinationAddress.getLng(), cityId, isSurge);
                })
                .subscribeOn(RxSchedulers.network())
                .observeOn(RxSchedulers.main())
                .subscribe(new ApiSubscriber2<Result>(activity) {
                    @Override
                    public void onNext(Result result) {
                        super.onNext(result);
                        fareEstimate.set(result.getFareEstimate());
                    }
                }));
    }

    private Observable<Result> getFareEstimate(final double startLatitude, final double startLongitude, final double endLatitude, final double endLongitude, long cityId, final boolean isSurge) {
        return App.getDataManager().getFareEstimate(startLatitude, startLongitude,
                endLatitude, endLongitude, cityId, isSurge)
                .map(response -> {
                    double totalFare = Optional.ofNullable(response.getCampaignInfo())
                            .map(Campaign::getEstimatedFare)
                            .orElse(response.getTotalFare());
                    Result result = new Result();
                    result.setFareEstimate("$" + String.format(Locale.US, "%.2f", totalFare));
                    return result;
                }).flatMap(result -> {
                    try {
                        LocationHelper.getFromLocation(startLatitude, startLongitude)
                                .flatMap(addresses -> LocationHelper.getAddressString(addresses, false))
                                .ifPresent(result::setPickupAddress);

                        LocationHelper.getFromLocation(endLatitude, endLongitude)
                                .flatMap(addresses -> LocationHelper.getAddressString(addresses, false))
                                .ifPresent(result::setDestinationAddress);
                    } catch (IOException | IllegalArgumentException e) {
                        Timber.e(e, e.getMessage());
                    }
                    return Observable.just(result);
                });
    }

    public ObservableField<String> getFareEstimate() {
        return fareEstimate;
    }

    public ObservableField<String> getRiderPickup() {
        return riderPickup;
    }

    public ObservableField<String> getRiderDestination() {
        return riderDestination;
    }

    public void onStop() {
        subscriptions.clear();
    }

    private static class Result {
        private String fareEstimate;
        private String pickupAddress;
        private String destinationAddress;

        public String getFareEstimate() {
            return fareEstimate;
        }

        public void setFareEstimate(final String fareEstimate) {
            this.fareEstimate = fareEstimate;
        }

        public String getPickupAddress() {
            return pickupAddress;
        }

        public void setPickupAddress(final String pickupAddress) {
            this.pickupAddress = pickupAddress;
        }

        public String getDestinationAddress() {
            return destinationAddress;
        }

        public void setDestinationAddress(final String destinationAddress) {
            this.destinationAddress = destinationAddress;
        }
    }
}
