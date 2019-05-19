package com.rideaustin.ui.drawer.cars.sticker;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.rideaustin.App;
import com.rideaustin.api.model.Document;
import com.rideaustin.api.model.driver.Car;
import com.rideaustin.api.model.driver.Driver;
import com.rideaustin.api.model.driver.DriverPhotoType;
import com.rideaustin.base.ApiSubscriber2;
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
 * Created by hatak on 2/6/17.
 */

public class UpdateStickerViewModel extends BaseUpdateDocumentsViewModel<UpdateStickerView> {

    private Car car;
    private String photoFilePath;
    private Document document;
    private Date tncStickerExpirationDate;

    public UpdateStickerViewModel(@NonNull UpdateStickerView view, final Car car) {
        super(view);
        this.car = car;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!App.getDataManager().isLoggedIn()) {
            // app would be sent to login on resume
            return;
        }
        if (isImageSelected(photoFilePath)) {
            onPhotoTaken(photoFilePath);
        } else if (document != null) {
            performOnView(view -> view.onStickerDownloaded(document.getDocumentUrl()));
        } else {
            loadDocument(getView().getCallback());
        }
    }

    public void loadDocument(final BaseActivityCallback callback) {
        callback.showProgress();
        addSubscription(loadDocument(car, DriverPhotoType.CAR_STICKER)
                .observeOn(RxSchedulers.main())
                .subscribeOn(RxSchedulers.network())
                .subscribe(document -> {
                    this.document = document;
                    callback.hideProgress();
                    updateDateView(document);
                    performOnView(view -> view.onStickerDownloaded(document.getDocumentUrl()));
                }, throwable -> {
                    callback.hideProgress();
                    performOnView(UpdateStickerView::onStickerDownloadFailed);
                }, callback::hideProgress));
    }

    public void onPhotoTaken(String filePath) {
        this.photoFilePath = filePath;
        performOnView(view -> view.onStickerSelected(filePath));
    }

    public String getPhotoFilePath() {
        return photoFilePath;
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    public void uploadInspectionSticker(BaseActivityCallback callback) {
        Date date = getShownDate();
        if (date == null) {
            Exception e = new Exception(UNEXPECTED_STATE_KEY);
            Timber.e(e, "This should not happen: date == null");
        } else {
            if (TextUtils.isEmpty(photoFilePath) && document != null) {
                addSubscription(updateStickerObservable(date)
                        .subscribeOn(RxSchedulers.network())
                        .observeOn(RxSchedulers.main())
                        .subscribe(new ApiSubscriber2<Document>(callback) {
                            @Override
                            public void onNext(Document newDocument) {
                                document = newDocument;
                                performOnView(UpdateStickerView::onStickerUpdated);
                            }

                            @Override
                            public void onAnyError(BaseApiException e) {
                                performOnView((view) -> view.onStickerUploadFailed(e));
                            }
                        }));
            } else if (!TextUtils.isEmpty(photoFilePath)) {
                addSubscription(createNewStickerObservable(date)
                        .subscribeOn(RxSchedulers.network())
                        .observeOn(RxSchedulers.main())
                        .subscribe(new ApiSubscriber2<Driver>(callback) {
                            @Override
                            public void onNext(Driver driver) {
                                App.getDataManager().setCurrentDriver(driver);
                                performOnView(UpdateStickerView::onStickerUpdated);
                            }

                            @Override
                            public void onAnyError(BaseApiException e) {
                                performOnView((view) -> view.onStickerUploadFailed(e));
                            }
                        }));
            }
        }
    }

    private void updateDateView(Document document) {
        if (!TextUtils.isEmpty(document.getValidityDate())) {
            try {
                Date date = DateHelper.dateFromServerDateFormat(document.getValidityDate());
                performOnView(updateStickerView -> updateStickerView.showStickerDate(date));
            } catch (ParseException e) {
                Timber.e(e, "Cannot parse document date: " + document);
            }
        }
    }

    private Observable<Driver> createNewStickerObservable(Date validityDate) {
        return App.getDataManager().getDriverService().uploadDriverDocuments(
                App.getDataManager().getCurrentDriver().getId(),
                DriverPhotoType.CAR_STICKER.name(),
                car.getId(),
                DateHelper.dateToServerDateFormat(validityDate),
                ImageHelper.getTypedFileFromPath("fileData", photoFilePath));
    }

    private Observable<Document> updateStickerObservable(Date validityDate) {
        document.setValidityDate(DateHelper.dateToServerDateFormat(validityDate));
        return App.getDataManager().getDriverService().updateDocument(document.getId(), document);
    }

    @Nullable
    public Date getShownDate() {
        if (tncStickerExpirationDate != null) {
            return tncStickerExpirationDate;
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

    public Date getTncStickerExpirationDate() {
        return tncStickerExpirationDate;
    }

    public void setTncStickerExpirationDate(Date expirationDate) {
        this.tncStickerExpirationDate = expirationDate;
    }
}
