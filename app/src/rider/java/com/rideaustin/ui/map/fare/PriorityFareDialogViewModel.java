package com.rideaustin.ui.map.fare;

import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.os.Bundle;
import android.text.TextUtils;

import com.google.android.gms.maps.model.LatLng;
import com.rideaustin.App;
import com.rideaustin.api.model.driver.RequestedCarType;
import com.rideaustin.api.model.surgearea.SurgeArea;
import com.rideaustin.schedulers.RxSchedulers;
import com.rideaustin.ui.common.RxBaseObservable;
import com.rideaustin.ui.utils.SurgeAreaUtils;
import com.rideaustin.utils.Constants;

import java.text.DecimalFormat;
import java.util.HashMap;

import java8.util.Optional;
import timber.log.Timber;

/**
 * Created by ysych on 7/27/16.
 */
public class PriorityFareDialogViewModel extends RxBaseObservable {

    public static final String EMPTY = "";
    public static final float DISABLED_FACTOR = 1f;
    private PriorityFareView view;
    private ObservableField<String> fare = new ObservableField<>(EMPTY);
    private ObservableInt minimum = new ObservableInt(0);
    private ObservableField<String> min = new ObservableField<>(EMPTY);
    private ObservableField<String> mile = new ObservableField<>(EMPTY);
    private SurgeArea surgeArea;
    private RequestedCarType carType;
    private LatLng location;

    public ObservableField<String> getFare() {
        return fare;
    }

    public ObservableInt getMinimum() {
        return minimum;
    }

    public ObservableField<String> getMile() {
        return mile;
    }

    public ObservableField<String> getMin() {
        return min;
    }

    public PriorityFareDialogViewModel(PriorityFareView view, Bundle args) {
        this.view = view;
        fillFareInfo(args);
    }

    private void fillFareInfo(Bundle args) {
        if (args.containsKey(Constants.SURGE_AREA) && args.containsKey(Constants.CAR_TYPE)) {
            surgeArea = (SurgeArea) args.getSerializable(Constants.SURGE_AREA);
            carType = (RequestedCarType) args.getSerializable(Constants.CAR_TYPE);
            location = args.getParcelable(Constants.LOCATION);
            updateArea(SurgeAreaUtils.getPriceFactor(surgeArea, carType.getCarCategory()), carType);
        } else {
            Timber.e("You should pass SURGE_AREA and CAR_TYPE as an argument.");
        }
    }

    private void updateArea(float surgeFactor, RequestedCarType carType) {
        if (surgeFactor >= 1f && carType != null) {
            if (this.surgeArea.getCarCategoriesFactors() == null) {
                this.surgeArea.setCarCategoriesFactors(new HashMap<>());
            }
            this.surgeArea.getCarCategoriesFactors().put(carType.getCarCategory(), surgeFactor);
            DecimalFormat formatter = new DecimalFormat("#0.00");
            fare.set(formatter.format(surgeFactor));
            if (!(TextUtils.isEmpty(carType.getMinimumFare()) || TextUtils.isEmpty(carType.getRatePerMinute()) || TextUtils.isEmpty(carType.getRatePerMinute()))) {
                minimum.set((int) (surgeFactor * Float.valueOf(carType.getMinimumFare())));
                min.set(formatter.format(Float.valueOf(carType.getRatePerMinute()) * surgeFactor));
                mile.set(formatter.format(Float.valueOf(carType.getRatePerMile()) * surgeFactor));
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        addSubscription(App.getDataManager().getSurgeAreaUpdates()
                // RA-9363: check if location belongs to surge area
                .map(surgeAreas -> SurgeAreaUtils.findByLocation(surgeAreas, location))
                .filter(surgeArea -> surgeArea != null)
                .observeOn(RxSchedulers.main())
                .subscribe(surgeUpdate -> {
                    String carCategory = carType.getCarCategory();
                    Optional<SurgeArea> highestSurgeArea = App.getDataManager().findSurgeArea(carCategory, location);
                    float highestSurgeFactor = highestSurgeArea.map(surgeArea -> SurgeAreaUtils.getPriceFactor(surgeArea, carCategory)).orElse(DISABLED_FACTOR);
                    float currentSurgeFactor = SurgeAreaUtils.getPriceFactor(this.surgeArea, carCategory);
                    //Current Surge area updates are minimum 0.25
                    if (Math.abs(highestSurgeFactor - currentSurgeFactor) > 0.001f) {
                        updateArea(highestSurgeFactor, carType);
                        if (highestSurgeFactor > DISABLED_FACTOR) {
                            view.onPriorityFareUpdated();
                        } else {
                            view.onPriorityFareDisabled();
                        }
                    }
                }, throwable -> Timber.e(throwable, throwable.getMessage())));
    }
}
