package com.rideaustin.ui.drawer.dc;

import com.rideaustin.App;
import com.rideaustin.api.model.driver.DirectConnectDriver;
import com.rideaustin.api.model.driver.RequestedCarType;
import com.rideaustin.ui.common.RxBaseViewModel;

import java.util.ArrayList;
import java.util.List;

import java8.util.Optional;
import rx.Observable;
import rx.subjects.BehaviorSubject;
import timber.log.Timber;


/**
 * Created by hatak on 03.11.2017.
 */

public class DirectConnectViewModel extends RxBaseViewModel {

    public enum Step {
        ENTER_DRIVER_ID, DRIVER_SUMMARY, CATEGORY_PICKER, PAYMENT_PICKER, ADD_PAYMENT
    }

    private BehaviorSubject<Step> navigateSubject = BehaviorSubject.create(Step.ENTER_DRIVER_ID);
    private String directConnectId;
    private DirectConnectDriver driver;
    private List<RequestedCarType> carTypes;
    private int selectedCarTypeIndex;

    public void navigateTo(Step step) {
        navigateSubject.onNext(step);
    }

    public void setDirectConnectId(String directConnectId) {
        this.directConnectId = directConnectId;
    }

    public String getDirectConnectId() {
        return directConnectId;
    }

    public void setDriver(DirectConnectDriver driver) {
        this.driver = driver;
        this.carTypes = findCarTypes();
    }

    public DirectConnectDriver getDriver() {
        return driver;
    }

    public List<RequestedCarType> getCarTypes() {
        return carTypes;
    }

    public void setSelectedCarTypeIndex(int selectedCarTypeIndex) {
        this.selectedCarTypeIndex = selectedCarTypeIndex;
    }

    public int getSelectedCarTypeIndex() {
        return selectedCarTypeIndex;
    }

    public Optional<RequestedCarType> getCarType() {
        if (selectedCarTypeIndex > -1 && selectedCarTypeIndex < carTypes.size()) {
            return Optional.ofNullable(carTypes.get(selectedCarTypeIndex));
        }
        return Optional.empty();
    }

    public float getSurgeFactor(RequestedCarType carType) {
        if (carType != null && driver.getFactors() != null) {
            Float factor = driver.getFactors().get(carType.getCarCategory());
            if (factor != null) {
                return factor;
            }
        }
        return 1.0f;
    }

    Observable<Step> getNavigateEvents() {
        return navigateSubject.asObservable().onBackpressureBuffer();
    }

    private List<RequestedCarType> findCarTypes() {
        List<RequestedCarType> result = new ArrayList<>();
        List<RequestedCarType> allTypes = App.getConfigurationManager().getLastConfiguration().getCarTypes();
        int cheapestIndex = -1;
        float cheapestPrice = Float.MAX_VALUE;
        if (allTypes != null && driver.getCategories() != null && !driver.getCategories().isEmpty()) {
            int index = 0;
            for (RequestedCarType carType : allTypes) {
                if (driver.getCategories().contains(carType.getCarCategory())) {
                    float price = Float.MAX_VALUE;
                    try {
                        price = Float.valueOf(carType.getMinimumFare()) * driver.getFactors().get(carType.getCarCategory());
                    } catch (Exception e) {
                        Timber.e(e);
                    }

                    if (price < cheapestPrice) {
                        cheapestPrice = price;
                        cheapestIndex = index;
                    }
                    result.add(carType);
                    index++;
                }
            }
        }
        selectedCarTypeIndex = cheapestIndex;
        return result;
    }

}
