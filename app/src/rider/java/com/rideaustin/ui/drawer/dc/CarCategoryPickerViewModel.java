package com.rideaustin.ui.drawer.dc;

import com.rideaustin.api.model.driver.RequestedCarType;
import com.rideaustin.ui.common.RxBaseViewModel;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.subjects.BehaviorSubject;

/**
 * Created by hatak on 31.10.2017.
 */

public class CarCategoryPickerViewModel extends RxBaseViewModel {

    private DirectConnectViewModel model;
    private BehaviorSubject<List<CarCategoryItem>> items = BehaviorSubject.create();

    public void setModel(DirectConnectViewModel model) {
        this.model = model;
        updateItems();
    }

    Observable<List<CarCategoryItem>> getItems() {
        return items.asObservable().onBackpressureLatest();
    }

    void selectCarCategoryAt(int index) {
        model.setSelectedCarTypeIndex(index);
        updateItems();
    }

    private void updateItems() {
        List<CarCategoryItem> items = new ArrayList<>();
        for (int i = 0; i < model.getCarTypes().size(); i++) {
            CarCategoryItem item = new CarCategoryItem();
            RequestedCarType carType = model.getCarTypes().get(i);
            item.setCarType(carType);
            item.setSelected(i == model.getSelectedCarTypeIndex());
            item.setSurgeFactor(model.getSurgeFactor(carType));
            items.add(item);
        }
        this.items.onNext(items);
    }
}
