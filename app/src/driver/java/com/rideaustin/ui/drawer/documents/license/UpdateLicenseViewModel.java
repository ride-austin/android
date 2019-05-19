package com.rideaustin.ui.drawer.documents.license;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.rideaustin.App;
import com.rideaustin.api.model.Document;
import com.rideaustin.api.model.driver.Driver;
import com.rideaustin.api.model.driver.DriverPhotoType;
import com.rideaustin.base.ApiSubscriber;
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
 * Created by crossover on 02/02/2017.
 */

public class UpdateLicenseViewModel extends BaseUpdateDocumentsViewModel<UpdateLicenseView> {

    //User selected file
    private String photoFilePath;
    //User selected date
    private Date licenseExpirationDate;

    Document document;


    public UpdateLicenseViewModel(@NonNull UpdateLicenseView view) {
        super(view);
    }

    public void onPhotoTaken(String filePath) {
        this.photoFilePath = filePath;
        performOnView(view -> view.onLicenseSelected(filePath));
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
            performOnView(view -> view.onLicenseDownloaded(document.getDocumentUrl()));

        } else {
            performOnView(view -> loadDocument(view.getCallback()));
        }
    }

    public void loadDocument(BaseActivityCallback callback) {
        addSubscription(loadDocument(DriverPhotoType.LICENSE)
                .observeOn(RxSchedulers.main())
                .subscribeOn(RxSchedulers.network())
                .subscribe(new ApiSubscriber2<Document>(callback) {
                    @Override
                    public void onNext(Document document) {
                        UpdateLicenseViewModel.this.document = document;
                        updateDateView(document);
                        callback.hideProgress();
                        performOnView(view -> view.onLicenseDownloaded(document.getDocumentUrl()));
                    }

                    @Override
                    public void onAnyError(BaseApiException e) {
                        super.onAnyError(e);
                        callback.hideProgress();
                        performOnView(UpdateLicenseView::onLicenseDownloadFailed);
                    }
                }));
    }

    private void updateDateView(Document document) {
        if (!TextUtils.isEmpty(document.getValidityDate())) {
            try {
                Date date = DateHelper.dateFromServerDateFormat(document.getValidityDate());
                performOnView(updateLicenseView -> updateLicenseView.showLicenseDate(date));
            } catch (ParseException e) {
                Timber.e(e, "Cannot parse document date: " + document);
            }
        }
    }

    public void setLicenseExpirationDate(Date licenseExpirationDate) {
        this.licenseExpirationDate = licenseExpirationDate;
    }

    public Date getLicenseExpirationDate() {
        return licenseExpirationDate;
    }

    public String getLicenseImagePath() {
        return photoFilePath;
    }


    @Nullable
    public Date getShownDate() {
        if (licenseExpirationDate != null) {
            return licenseExpirationDate;
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

    public void uploadLicense(BaseActivityCallback callback) {
        Date date = getShownDate();
        if (date == null) {
            Exception e = new Exception(UNEXPECTED_STATE_KEY);
            Timber.e(e, "This should not happen: date == null");
        } else {
            if (TextUtils.isEmpty(photoFilePath) && document != null) {
                addSubscription(updateLicenseObservable(date)
                        .subscribeOn(RxSchedulers.network())
                        .observeOn(RxSchedulers.main())
                        .subscribe(new ApiSubscriber<Document>(callback) {
                            @Override
                            public void onNext(Document newDocument) {
                                document = newDocument;
                                performOnView(UpdateLicenseView::onLicenseUpdated);
                            }

                            @Override
                            public void onError(BaseApiException e) {
                                performOnView((view) -> view.onLicenseUploadFailed(e));
                            }
                        }));
            } else if (!TextUtils.isEmpty(photoFilePath)) {
                addSubscription(createNewLicenseObservable(date)
                        .subscribeOn(RxSchedulers.network())
                        .observeOn(RxSchedulers.main())
                        .subscribe(new ApiSubscriber<Driver>(callback) {
                            @Override
                            public void onNext(Driver driver) {
                                App.getDataManager().setCurrentDriver(driver);
                                performOnView(UpdateLicenseView::onLicenseUpdated);
                            }

                            @Override
                            public void onError(BaseApiException e) {
                                performOnView((view) -> view.onLicenseUploadFailed(e));
                            }
                        }));
            }
        }
    }

    private Observable<Document> updateLicenseObservable(Date validityDate) {
        document.setValidityDate(DateHelper.dateToServerDateFormat(validityDate));
        return App.getDataManager().getDriverService().updateDocument(document.getId(), document);
    }

    private Observable<Driver> createNewLicenseObservable(Date validityDate) {
        return App.getDataManager().getDriverService().uploadDriverDocuments(
                App.getDataManager().getCurrentDriver().getId(),
                DriverPhotoType.LICENSE.name(),
                DateHelper.dateToServerDateFormat(validityDate),
                ImageHelper.getTypedFileFromPath("fileData", photoFilePath));
    }
}
