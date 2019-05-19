package com.rideaustin.ui.drawer.documents.tnc;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.rideaustin.api.config.TncCard;
import com.rideaustin.ui.common.BaseViewModel;
import com.rideaustin.ui.common.TakePhotoFragment;
import com.rideaustin.utils.Constants;

import java.util.Date;

/**
 * Created by crossover on 22.01.17.
 */

public class DriverTNCCardViewModel extends BaseViewModel<DriverTNCCardView> implements TakePhotoFragment.TakePhotoListener {

    private String photoFilePath;
    private TakePhotoFragment.Source photoSource;
    @Constants.TNCCardSide
    private String tncCardSide;
    private UpdateTNCViewModel updateTNCViewModel;
    private boolean isImageSelected;
    private boolean isDateSet;

    public DriverTNCCardViewModel(@NonNull DriverTNCCardView view, UpdateTNCViewModel updateTNCViewModel, String currentType) {
        super(view);
        this.updateTNCViewModel = updateTNCViewModel;
        this.tncCardSide = currentType;
        switch (tncCardSide) {
            case Constants.TNCCardSide.FRONT:
                photoFilePath = updateTNCViewModel.getDriverTncCardFrontImagePath();
                break;
            case Constants.TNCCardSide.BACK:
                photoFilePath = updateTNCViewModel.getDriverTncCardBackImagePath();
                break;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        //Renew photo after resume
        if (isImageSelected()) {
            onPhotoTaken(photoSource, photoFilePath);
        }
    }

    @Override
    public void onPhotoTaken(TakePhotoFragment.Source source, final String filePath) {

        photoFilePath = filePath;
        photoSource = source;
        isImageSelected = true;

        performOnView(DriverTNCCardView::onHideBottomSheet);

        if (tncCardSide.equals(Constants.TNCCardSide.FRONT)) {
            updateTNCViewModel.setDriverTncCardFrontImagePath(filePath);
        } else if (tncCardSide.equals(Constants.TNCCardSide.BACK)) {
            updateTNCViewModel.setDriverTncCardBackImagePath(filePath);
        }
        performOnView(view -> view.onTNCSelected(filePath));
    }

    @Override
    public void onCanceled() {
        performOnView(DriverTNCCardView::onHideBottomSheet);
    }

    public boolean isBackSide() {
        return tncCardSide.equals(Constants.TNCCardSide.BACK);
    }

    public TncCard getTncCard() {
        return updateTNCViewModel.getDriverRegistration().getTncCard();
    }

    @Nullable
    public Date getExpirationDate() {
        return updateTNCViewModel.getDriverTncExpirationDate();
    }

    public void setExpirationDate(Date date) {
        isDateSet = true;
        updateTNCViewModel.setDriverTncExpirationDate(date);
    }

    public boolean isImageSelected() {
        return isImageSelected;
    }

    public boolean isExpirationDateSet() {
        return isDateSet;
    }

    public boolean isMenuActionEnabled() {
        if (isLastScreen()) {
            return isImageSelected() && isExpirationDateSet();
        } else {
            return isImageSelected();
        }
    }

    public boolean isLastScreen() {
        return isBackSide() || !getTncCard().getBackPhotoEnabled();
    }
}
