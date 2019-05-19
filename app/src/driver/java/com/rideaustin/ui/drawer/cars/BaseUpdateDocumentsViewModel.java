package com.rideaustin.ui.drawer.cars;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.rideaustin.App;
import com.rideaustin.api.model.Document;
import com.rideaustin.api.model.driver.Car;
import com.rideaustin.api.model.driver.DriverPhotoType;
import com.rideaustin.ui.common.BaseView;
import com.rideaustin.ui.common.BaseViewModel;

import java.util.Collections;
import java.util.List;

import rx.Observable;
import rx.functions.Func1;

import static com.rideaustin.utils.MathUtils.compare;

/**
 * Created by hatak on 2/7/17.
 */

public class BaseUpdateDocumentsViewModel<T extends BaseView> extends BaseViewModel<T> {

    public BaseUpdateDocumentsViewModel(@NonNull T view) {
        super(view);
    }

    private static final Func1<List<Document>, Observable<Document>> FILTER = documents -> {
        if (documents == null || documents.isEmpty()) {
            return Observable.empty();
        } else {
            return Observable.just(Collections.max(documents, (lhs, rhs) -> {
                long x = lhs.isRemoved() ? 0 : lhs.getId();
                long y = rhs.isRemoved() ? 0 : rhs.getId();
                return compare(x, y);
            }));
        }
    };

    public Observable<Document> loadDocument(Car car, DriverPhotoType photoType) {
        Long driverId = App.getDataManager().getCurrentDriver().getId();
        return App.getDataManager().getBaseDriverService()
                .getCarDocuments(driverId, car.getId(), photoType.name())
                .flatMap(FILTER);
    }

    public Observable<Document> loadDocument(Integer cityId, DriverPhotoType photoType) {
        Long driverId = App.getDataManager().getCurrentDriver().getId();
        return App.getDataManager().getBaseDriverService()
                .getDriverDocuments(driverId, photoType.name(), cityId)
                .flatMap(FILTER);
    }

    public Observable<Document> loadDocument(DriverPhotoType photoType) {
        Long driverId = App.getDataManager().getCurrentDriver().getId();
        return App.getDataManager().getBaseDriverService()
                .getDriverDocuments(driverId, photoType.name())
                .flatMap(FILTER);
    }

    public boolean isImageSelected(String photoPath) {
        return !TextUtils.isEmpty(photoPath);
    }
}
