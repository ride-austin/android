package com.rideaustin.ui.drawer.documents.tnc;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.request.RequestOptions;
import com.rideaustin.App;
import com.rideaustin.api.config.DriverRegistration;
import com.rideaustin.api.model.Document;
import com.rideaustin.api.model.driver.Driver;
import com.rideaustin.api.model.driver.DriverPhotoType;
import com.rideaustin.base.ApiSubscriber;
import com.rideaustin.base.BaseActivityCallback;
import com.rideaustin.schedulers.RxSchedulers;
import com.rideaustin.ui.common.TakePhotoFragment;
import com.rideaustin.ui.drawer.cars.BaseUpdateDocumentsViewModel;
import com.rideaustin.utils.DateHelper;
import com.rideaustin.utils.ImageHelper;
import com.rideaustin.utils.TimeUtils;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.concurrent.ExecutionException;

import rx.Single;
import timber.log.Timber;

/**
 * Created by crossover on 22/01/2017.
 */

public class UpdateTNCViewModel extends BaseUpdateDocumentsViewModel<UpdateTNCView> implements TakePhotoFragment.TakePhotoListener {

    private DriverRegistration driverRegistration;
    private String driverTncCardFrontImagePath;
    private String driverTncCardBackImagePath;
    private String driverTncCardImagePath;
    private Date driverTncExpirationDate;

    private Document document;

    public UpdateTNCViewModel(@NonNull UpdateTNCView view, DriverRegistration driverRegistration) {
        super(view);
        this.driverRegistration = driverRegistration;
    }

    public DriverRegistration getDriverRegistration() {
        return driverRegistration;
    }

    public Document getDocument() {
        return document;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!App.getDataManager().isLoggedIn()) {
            return;
        }
        if (document != null) {
            performOnView(view -> view.onDocumentDownloaded(document));
        } else {
            loadDocument(getView().getCallback());
        }
    }

    public void loadDocument(final BaseActivityCallback callback) {
        callback.showProgress();
        addSubscription(loadDocument(DriverPhotoType.CHAUFFEUR_LICENSE)
                .observeOn(RxSchedulers.main())
                .subscribeOn(RxSchedulers.network())
                .subscribe(document -> {
                    this.document = document;
                    performOnView(view -> view.onDocumentDownloaded(document));
                    callback.hideProgress();
                }, throwable -> {
                    callback.hideProgress();
                    performOnView(UpdateTNCView::onDocumentDownloadFailed);
                }, callback::hideProgress));
    }

    @Override
    public void onPhotoTaken(TakePhotoFragment.Source source, String filePath) {
        performOnView(view -> view.onPhotoTaken(source, filePath));
    }

    @Override
    public void onCanceled() {
        performOnView(UpdateTNCView::onPhotoCanceled);
    }

    private void combineTncCards(final Context context, final String frontSidePath, final String backSidePath) {
        addSubscription(Single.fromCallable(() -> combineTncImages(context, frontSidePath, backSidePath))
                .subscribeOn(RxSchedulers.network())
                .observeOn(RxSchedulers.main())
                .subscribe(result -> performOnView(UpdateTNCView::onTncCardsCombined),
                        throwable -> performOnView(view -> view.onTncCardCombineFailed(throwable.getLocalizedMessage()))));
    }

    /**
     * Should be executed off UI thread
     */
    private boolean combineTncImages(final Context context, final String frontSidePath, final String backSidePath) throws IOException, ExecutionException, InterruptedException {
        File combinedFile = new File(new File(frontSidePath).getParent() + "/" + TimeUtils.currentTimeMillis() + ".jpg");
        if (combinedFile.createNewFile()) {
            driverTncCardImagePath = combinedFile.getAbsolutePath();
            FileOutputStream outputStream = null;
            Bitmap bitmap = null;
            try {
                Bitmap tncFront = loadBitmap(context, frontSidePath);
                Bitmap tncBack = loadBitmap(context, backSidePath);
                bitmap = ImageHelper.combineImages(tncFront, tncBack);
                outputStream = new FileOutputStream(combinedFile);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream);
                outputStream.flush();
                return true;
            } finally {
                IOUtils.closeQuietly(outputStream);
                if (bitmap != null) {
                    bitmap.recycle();
                }
            }
        } else {
            return false;
        }
    }

    /**
     * Should be executed off UI thread
     */
    private Bitmap loadBitmap(Context context, final String frontSidePath) throws InterruptedException, ExecutionException {
        return Glide.with(context)
                .asBitmap()
                .load(frontSidePath)
                .apply(new RequestOptions().format(DecodeFormat.PREFER_RGB_565).disallowHardwareConfig())
                .submit(1000, 1000)
                .get();
    }

    String getDriverTncCardFrontImagePath() {
        return driverTncCardFrontImagePath;
    }

    String getDriverTncCardBackImagePath() {
        return driverTncCardBackImagePath;
    }

    @Nullable
    Date getDriverTncExpirationDate() {
        if (driverTncExpirationDate == null) {
            driverTncExpirationDate = getDateFromDocument();
        }
        return driverTncExpirationDate;
    }

    @Nullable
    private Date getDateFromDocument() {
        if (document != null && document.getValidityDate() != null) {
            try {
                return DateHelper.dateFromServerDateFormat(document.getValidityDate());
            } catch (ParseException e) {
                Timber.e(e, "Can't parse TNC expiration date: " + document.getValidityDate());
            }
        }
        return null;
    }

    void setDriverTncCardFrontImagePath(String driverTncCardFrontImagePath) {
        this.driverTncCardFrontImagePath = driverTncCardFrontImagePath;
    }

    void setDriverTncCardBackImagePath(String driverTncCardBackImagePath) {
        this.driverTncCardBackImagePath = driverTncCardBackImagePath;
    }

    void setDriverTncExpirationDate(Date driverTncExpirationDate) {
        this.driverTncExpirationDate = driverTncExpirationDate;
    }

    public void onSave() {
        if (driverTncCardFrontImagePath != null && driverTncCardBackImagePath != null) {
            // combine image is user uploaded both
            performOnView(view -> combineTncCards(view.getContext(), driverTncCardFrontImagePath, driverTncCardBackImagePath));
        } else {
            // use front image only
            performOnView(UpdateTNCView::onTncCardsCombined);
        }
    }

    public void postPhotos(BaseActivityCallback callback) {
        final Driver driver = App.getDataManager().getCurrentDriver();

        String imagePath = driverTncCardImagePath != null ? driverTncCardImagePath : driverTncCardFrontImagePath;
        addSubscription(App.getDataManager().uploadTNCCard(imagePath, driverTncExpirationDate, driver.getId(), driver.getCityId())
                .subscribeOn(RxSchedulers.network())
                .observeOn(RxSchedulers.main())
                .subscribe(new ApiSubscriber<Driver>(callback) {
                    @Override
                    public void onNext(Driver driver) {
                        //App.getDataManager().setCurrentDriver(driver);
                        performOnView(UpdateTNCView::onDocumentUploaded);
                    }
                }));
    }
}
