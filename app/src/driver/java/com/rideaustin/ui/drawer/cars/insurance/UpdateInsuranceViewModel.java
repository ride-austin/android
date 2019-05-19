package com.rideaustin.ui.drawer.cars.insurance;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.rideaustin.App;
import com.rideaustin.api.model.Document;
import com.rideaustin.api.model.driver.Car;
import com.rideaustin.api.model.driver.Driver;
import com.rideaustin.api.model.driver.DriverPhotoType;
import com.rideaustin.base.ApiSubscriber;
import com.rideaustin.base.BaseActivityCallback;
import com.rideaustin.base.BaseApiException;
import com.rideaustin.schedulers.RxSchedulers;
import com.rideaustin.ui.drawer.cars.BaseUpdateDocumentsViewModel;
import com.rideaustin.utils.DateHelper;
import com.rideaustin.utils.ImageHelper;

import java.text.ParseException;
import java.util.Date;

import rx.Observable;
import timber.log.Timber;

import static com.rideaustin.utils.CommonConstants.UNEXPECTED_STATE_KEY;

/**
 * Created by crossover on 02/02/2017.
 */

public class UpdateInsuranceViewModel extends BaseUpdateDocumentsViewModel<UpdateInsuranceView> {

    //User selected file
    private String photoFilePath;
    //User selected date
    private Date insuranceExpirationDate;

    private Car car;
    private Document document;

    public UpdateInsuranceViewModel(@NonNull UpdateInsuranceView view, Car car) {
        super(view);
        this.car = car;
    }

    public void onPhotoTaken(String filePath) {
        this.photoFilePath = filePath;
        performOnView(view -> view.onInsuranceSelected(filePath));
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!App.getDataManager().isLoggedIn()) {
            return;
        }
        if (isImageSelected(photoFilePath)) {
            onPhotoTaken(photoFilePath);
        } else if (document != null) {
            updateDateView(document);
            performOnView(view -> view.onInsuranceDownloaded(document.getDocumentUrl()));

        } else {
            loadDocument(getView().getCallback());
        }
    }

    public void loadDocument(final BaseActivityCallback callback) {
        callback.showProgress();
        addSubscription(loadDocument(car, DriverPhotoType.INSURANCE)
                .observeOn(RxSchedulers.main())
                .subscribeOn(RxSchedulers.network())
                .subscribe(document -> {
                    this.document = document;
                    updateDateView(document);
                    callback.hideProgress();
                    performOnView(view -> view.onInsuranceDownloaded(document.getDocumentUrl()));
                }, throwable -> {
                    callback.hideProgress();
                    performOnView(UpdateInsuranceView::onInsuranceDownloadFailed);
                }, callback::hideProgress));
    }

    private void updateDateView(Document document) {
        if (!TextUtils.isEmpty(document.getValidityDate())) {
            try {
                Date date = DateHelper.dateFromServerDateFormat(document.getValidityDate());
                performOnView(updateLicenseView -> updateLicenseView.showInsuranceDate(date));
            } catch (ParseException e) {
                Timber.e(e, "Cannot parse document date: " + document);
            }
        }
    }

    public void setInsuranceExpirationDate(Date insuranceExpirationDate) {
        this.insuranceExpirationDate = insuranceExpirationDate;
    }

    public Date getInsuranceExpirationDate() {
        return insuranceExpirationDate;
    }

    public String getInsuranceImagePath() {
        return photoFilePath;
    }

    @Nullable
    public Date getShownDate() {
        if (insuranceExpirationDate != null) {
            return insuranceExpirationDate;
        } else if (document != null && !TextUtils.isEmpty(document.getValidityDate())) {
            try {
                return DateHelper.dateFromServerDateFormat(document.getValidityDate());
            } catch (ParseException e) {
                return null;
            }
        } else {
            return null;
        }
    }

    public void uploadInsurance(BaseActivityCallback callback) {
        Date date = getShownDate();
        if (date == null) {
            Exception e = new Exception(UNEXPECTED_STATE_KEY);
            Timber.e(e, "This should not happen: date == null");
        } else {
            if (TextUtils.isEmpty(photoFilePath) && document != null) {
                addSubscription(updateInsuranceObservable(date)
                        .subscribeOn(RxSchedulers.network())
                        .observeOn(RxSchedulers.main())
                        .subscribe(new ApiSubscriber<Document>(callback) {
                            @Override
                            public void onNext(Document newDocument) {
                                document = newDocument;
                                performOnView(UpdateInsuranceView::onInsuranceUpdated);
                            }

                            @Override
                            public void onError(BaseApiException e) {
                                performOnView((view) -> view.onInsuranceUploadFailed(e));
                            }
                        }));
            } else if (!TextUtils.isEmpty(photoFilePath)) {
                addSubscription(createNewInsuranceObservable(date)
                        .subscribeOn(RxSchedulers.network())
                        .observeOn(RxSchedulers.main())
                        .subscribe(new ApiSubscriber<Driver>(callback) {
                            @Override
                            public void onNext(Driver driver) {
                                App.getDataManager().setCurrentDriver(driver);
                                performOnView(UpdateInsuranceView::onInsuranceUpdated);
                            }

                            @Override
                            public void onError(BaseApiException e) {
                                performOnView((view) -> view.onInsuranceUploadFailed(e));
                            }
                        }));
            }
        }
    }


    private Observable<Document> updateInsuranceObservable(Date validityDate) {
        document.setValidityDate(DateHelper.dateToServerDateFormat(validityDate));
        return App.getDataManager().getDriverService().updateDocument(document.getId(), document);
    }

    private Observable<Driver> createNewInsuranceObservable(Date validityDate) {
        return App.getDataManager().getDriverService().uploadDriverDocuments(
                App.getDataManager().getCurrentDriver().getId(),
                DriverPhotoType.INSURANCE.name(),
                car.getId(),
                DateHelper.dateToServerDateFormat(validityDate),
                ImageHelper.getTypedFileFromPath("fileData", photoFilePath));
    }
}
