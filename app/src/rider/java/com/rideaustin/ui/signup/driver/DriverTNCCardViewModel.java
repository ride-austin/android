package com.rideaustin.ui.signup.driver;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;

import com.rideaustin.api.config.DriverRegistration;
import com.rideaustin.models.DriverRegistrationData;
import com.rideaustin.schedulers.RxSchedulers;
import com.rideaustin.ui.common.TakePhotoFragment;
import com.rideaustin.utils.Constants;
import com.rideaustin.utils.ImageHelper;
import com.rideaustin.utils.TimeUtils;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

import rx.Single;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by hatak on 07.12.16.
 */

public class DriverTNCCardViewModel implements TakePhotoFragment.TakePhotoListener {

    private final DriverSignUpInteractor signUpInteractor;
    private final DriverTNCCardListener listener;
    private CompositeSubscription subscriptions = new CompositeSubscription();
    private String photoFilePath;
    private TakePhotoFragment.Source photoSource;
    @Constants.TNCCardSide
    private String tncCardSide;

    public DriverTNCCardViewModel(final DriverSignUpInteractor signUpInteractor, final DriverTNCCardListener listnerer) {
        this.signUpInteractor = signUpInteractor;
        this.listener = listnerer;
    }

    public void onStart() {
        //Renew photo after resume
        if (!TextUtils.isEmpty(photoFilePath)) {
            onPhotoTaken(photoSource, photoFilePath);
        }
        final DriverRegistrationData data = signUpInteractor.getDriverRegistrationData();
        switch (tncCardSide) {
            case Constants.TNCCardSide.FRONT:
                if (!TextUtils.isEmpty(data.getDriverTncCardFrontImagePath())) {
                    listener.onTNCSelected(Constants.TNCCardSide.FRONT, data.getDriverTncCardFrontImagePath());
                }
                break;
            case Constants.TNCCardSide.BACK:
                if (!TextUtils.isEmpty(data.getDriverTncCardBackImagePath())) {
                    listener.onTNCSelected(Constants.TNCCardSide.BACK, data.getDriverTncCardBackImagePath());
                }
                break;
        }
    }

    public DriverRegistration getDriverRegistration() {
        return signUpInteractor.getDriverRegistrationConfiguration();
    }

    public void onStop() {
        subscriptions.clear();
    }

    @Override
    public void onPhotoTaken(TakePhotoFragment.Source source, final String filePath) {

        photoFilePath = filePath;
        photoSource = source;

        listener.onHideBottomSheet();

        final DriverRegistrationData data = signUpInteractor.getDriverRegistrationData();

        if (tncCardSide.equals(Constants.TNCCardSide.FRONT)) {
            data.setDriverTncCardFrontImagePath(filePath);
            listener.onTNCSelected(Constants.TNCCardSide.FRONT, filePath);
        } else if (tncCardSide.equals(Constants.TNCCardSide.BACK)) {
            data.setDriverTncCardBackImagePath(filePath);
            listener.onTNCSelected(Constants.TNCCardSide.BACK, filePath);
        }
    }

    public void onContinue() {
        final DriverRegistrationData data = signUpInteractor.getDriverRegistrationData();
        if (tncCardSide.equals(Constants.TNCCardSide.FRONT)) {
            data.setDriverTncCardImagePath(data.getDriverTncCardFrontImagePath());
            listener.onTncCardsCombined();
        } else if (tncCardSide.equals(Constants.TNCCardSide.BACK)) {
            if (data.driverTncCardHasTwoSides()) {
                combineTncCards(data.getDriverTncCardFrontImagePath(), data.getDriverTncCardBackImagePath());
            } else if (!data.driverTncCardHasFrontSide() && data.driverTncCardHasBackSide()) {
                data.setDriverTncCardImagePath(data.getDriverTncCardBackImagePath());
                listener.onTncCardsCombined();
            } else {
                listener.onTncCardsCombined();
            }
        }
    }

    private void combineTncCards(final String frontSidePath, final String backSidePath) {
        listener.onShowProgress();
        subscriptions.add(Single.fromCallable(() -> combineTncImages(frontSidePath, backSidePath))
                .subscribeOn(RxSchedulers.computation())
                .observeOn(RxSchedulers.main())
                .subscribe(result -> listener.onTncCardsCombined(), throwable -> listener.onTncCardCombineFailed(throwable.getLocalizedMessage())));
    }

    /**
     * Should be executed off UI thread
     */
    private boolean combineTncImages(final String frontSidePath, final String backSidePath) throws IOException, ExecutionException, InterruptedException {
        final DriverRegistrationData driverRegistrationData = signUpInteractor.getDriverRegistrationData();
        File file = new File(driverRegistrationData.getDriverTncCardImagePath());
        File combinedFile = new File(file.getParent() + "/" + TimeUtils.currentTimeMillis() + ".jpg");
        if (combinedFile.createNewFile()) {
            Bitmap tncFront = ImageHelper.getScaledBitmapFromFile(frontSidePath);
            Bitmap tncBack = ImageHelper.getScaledBitmapFromFile(backSidePath);
            Bitmap bitmap = ImageHelper.combineImages(tncFront, tncBack);
            try {
                String filePath = combinedFile.getAbsolutePath();
                ImageHelper.saveBitmapToFile(bitmap, filePath, Bitmap.CompressFormat.JPEG, 85);
                driverRegistrationData.setDriverTncCardImagePath(filePath);
                return true;
            } finally {
                if (tncFront != null) tncFront.recycle();
                if (tncBack != null) tncBack.recycle();
                if (bitmap != null) bitmap.recycle();
            }
        }
        return false;
    }

    @Override
    public void onCanceled() {
        listener.onHideBottomSheet();
    }

    public void setTNCCardSide(final String TNCCardSide) {
        this.tncCardSide = TNCCardSide;
    }

    public boolean isBackSide() {
        return tncCardSide.equals(Constants.TNCCardSide.BACK);
    }

    public boolean isLastScreen() {
        return isBackSide() || !getDriverRegistration().getTncCard().getBackPhotoEnabled();
    }

    public interface DriverTNCCardListener {

        void onHideBottomSheet();

        void onTncCardsCombined();

        void onTncCardCombineFailed(final String message);

        void onShowProgress();

        void onHideProgress();

        Context getContext();

        void onTNCSelected(@Constants.TNCCardSide String side, String filePath);
    }
}
