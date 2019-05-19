package com.rideaustin.ui.drawer.cars.add;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.rideaustin.App;
import com.rideaustin.R;
import com.rideaustin.api.config.DriverRegistration;
import com.rideaustin.api.config.GlobalConfig;
import com.rideaustin.api.config.InspectionSticker;
import com.rideaustin.api.model.driver.Car;
import com.rideaustin.api.model.driver.Driver;
import com.rideaustin.base.BaseActivity;
import com.rideaustin.base.BaseActivityCallback;
import com.rideaustin.base.BaseApiException;
import com.rideaustin.base.Transition;
import com.rideaustin.databinding.AddCarBinding;
import com.rideaustin.ui.base.EngineStatelessActivity;
import com.rideaustin.ui.common.TakePhotoFragment;
import com.rideaustin.ui.genericsupport.GenericContactSupportFragment;
import com.rideaustin.ui.utils.CommonMaterialDialogCreator;
import com.rideaustin.ui.utils.MaterialDialogCreator;
import com.rideaustin.utils.Constants;

import java.io.Serializable;

import java8.util.Optional;

public class AddCarActivity extends EngineStatelessActivity implements AddCarView, TakePhotoFragment.TakePhotoListener {

    private TakePhotoFragment.TakePhotoListener takePhotoListener;

    private AddCarBinding binding;
    private AddCarViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // RA-8945: if activity is restored after app restart
        // fragments are also restored in super.onCreate(Bundle)
        // need to restore all data before this to prevent NPE
        viewModel = new AddCarViewModel(this);
        if (savedInstanceState != null) {
            viewModel.restore(savedInstanceState);
        }
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_car);
        setToolbar(binding.toolbar);
        binding.setViewModel(viewModel);
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());

        replaceFragment(AddCarSequence.first().newFragmentInstance(), R.id.content_frame, false, Transition.NONE);
    }

    @Override
    public void onStart() {
        super.onStart();
        viewModel.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        viewModel.onStop();
    }

    @Override
    public void onBackPressed() {
        Fragment fragment = getFragment(R.id.content_frame);
        if (fragment instanceof BaseAddCarFragment) {
            BaseAddCarFragment baseAddCarFragment = (BaseAddCarFragment) fragment;
            if (baseAddCarFragment.canGoBack()) {
                super.onBackPressed();
            } else {
                if (viewModel.isViewVisible()) {
                    MaterialDialogCreator.createSimpleDialog(getString(R.string.are_you_sure), this).onPositive((dialog, which) -> viewModel.cancelCar(this)).show();
                }

            }
        } else {
            super.onBackPressed();
        }
        binding.needHelp.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        viewModel.save(outState);
    }

    @Override
    public void onCompleted(AddCarSequence sequence) {
        switch (sequence) {
            case LICENSE_PLATE:
                viewModel.createCar(this);
                break;
            case CAR_PHOTO_TRUNK:
                viewModel.uploadCarPhotos(this);
                break;
            case INSPECTION_STICKER:
                viewModel.uploadSticker(this);
                break;
            case INSURANCE:
                viewModel.uploadInsurance(this);
                break;
            default: {
                if (viewModel.isLastSequence(sequence)) {
                    finish();
                } else {
                    goNextScreen(sequence);
                }
            }
            break;
        }
    }

    private void goNextScreen(AddCarSequence sequence) {
        replaceFragment(viewModel.getNextSequence(sequence).newFragmentInstance(), R.id.content_frame, true);
    }

    @Override
    public void onConfigUpdated(final GlobalConfig globalConfig) {
        viewModel.setLogoUrl(globalConfig.getGeneralInformation().getLogoUrl());

        binding.needHelp.setOnClickListener(v -> {
            Optional<Driver> driver = App.getDataManager().getDriver();
            GenericContactSupportFragment messageFragment = GenericContactSupportFragment.newInstance(Optional.empty(), driver.map(Driver::getCityId));
            replaceFragment(messageFragment, R.id.content_frame, true);
            binding.needHelp.setVisibility(View.GONE);
        });
    }

    /**
     * TODO: use {@link BaseActivity#getViewModel()}
     */
    @Override
    public AddCarViewModel getCarViewModel() {
        return viewModel;
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public BaseActivityCallback getCallback() {
        return this;
    }

    @Override
    public void onCarCreated(Car car) {
        goNextScreen(AddCarSequence.LICENSE_PLATE);
    }

    @Override
    public void onCarUpdated(Car car) {
        goNextScreen(AddCarSequence.INSURANCE);
    }

    @Override
    public void carCancelled() {
        finish();
    }

    @Override
    public void onCarPhotosUploaded(Car car) {
        goNextScreen(AddCarSequence.CAR_PHOTO_TRUNK);
    }

    @Override
    public void onStickerUploaded(Car car) {
        goNextScreen(AddCarSequence.INSPECTION_STICKER);
    }

    @Override
    public void onInsuranceUploaded(Car car) {
        viewModel.updateCar(this);
    }

    @Override
    public void onVehicleRequirementsFailed() {
        if (viewModel.isViewVisible()) {
            MaterialDialogCreator.createInfoDialogWithCallback(App.getAppName(), getString(R.string.network_error), (dialog, which) -> {
                dialog.dismiss();
            }, (AppCompatActivity) getCallback()).setOnDismissListener(dialog -> finish());
        }
    }

    @Override
    public void onCarCreateFailed(BaseApiException e) {
        if (viewModel.isViewVisible()) {
            CommonMaterialDialogCreator.createNetworkFailDialog(getString(R.string.failed_car_create), this).onPositive((dialog, which) -> viewModel.createCar(this)).show();
        }
    }

    @Override
    public void onCarUpdateFailed(BaseApiException e) {
        if (viewModel.isViewVisible()) {
            CommonMaterialDialogCreator.createNetworkFailDialog(getString(R.string.updating_car_failed), this).onPositive((dialog, which) -> viewModel.updateCar(this)).show();
        }
    }

    @Override
    public void onCarPhotosUploadFailed(BaseApiException e) {
        if (viewModel.isViewVisible()) {
            CommonMaterialDialogCreator.createNetworkFailDialog(getString(R.string.failed_photos_upload), this).onPositive((dialog, which) -> viewModel.uploadCarPhotos(this)).show();
        }
    }

    @Override
    public void onStickerUploadFailed(BaseApiException e) {
        if (viewModel.isViewVisible()) {
            String sticker = Optional.ofNullable(viewModel)
                    .map(AddCarViewModel::getDriverRegistration)
                    .map(DriverRegistration::getInspectionSticker)
                    .map(InspectionSticker::getHeader)
                    .orElse(getString(R.string.inspection_sticker));
            String message = getString(R.string.failed_sticker_upload, sticker);
            CommonMaterialDialogCreator.createNetworkFailDialog(message, this)
                    .onPositive((dialog, which) -> viewModel.uploadSticker(this)).show();
        }
    }

    @Override
    public void onInsuranceUploadFailed(BaseApiException e) {
        if (viewModel.isViewVisible()) {
            CommonMaterialDialogCreator.createNetworkFailDialog(getString(R.string.failed_insurance_upload), this).onPositive((dialog, which) -> viewModel.uploadInsurance(this)).show();
        }
    }

    @Override
    public void carCancelledFailed() {
        if (viewModel.isViewVisible()) {
            CommonMaterialDialogCreator.createNetworkFailDialog(getString(R.string.failed_to_cancel), this)
                    .neutralText(R.string.abort)
                    .onPositive((dialog, which) -> viewModel.cancelCar(this))
                    .onNeutral((dialog, which) -> finish()).show();
        }
    }

    @Override
    public TakePhotoFragment.TakePhotoListener getTakePhotoListener() {
        return this;
    }

    @Override
    public void setTakePhotoListener(TakePhotoFragment.TakePhotoListener listener) {
        takePhotoListener = listener;
    }

    @Override
    public void onPhotoTaken(TakePhotoFragment.Source source, String filePath) {
        if (takePhotoListener != null) {
            takePhotoListener.onPhotoTaken(source, filePath);
        }
    }

    @Override
    public void onCanceled() {
        if (takePhotoListener != null) {
            takePhotoListener.onCanceled();
        }
    }

    public enum AddCarSequence implements Serializable {
        VEHICLE_INFORMATION,
        SETUP_VEHICLE_YEAR,
        SETUP_VEHICLE_MAKE,
        SETUP_VEHICLE_MODEL,
        SETUP_VEHICLE_COLOR,
        LICENSE_PLATE,

        CAR_PHOTO_FRONT,
        CAR_PHOTO_BACK,
        CAR_PHOTO_INSIDE,
        CAR_PHOTO_TRUNK,

        INSPECTION_STICKER,

        INSURANCE,

        INFORMATION_SUMMARY;

        public static AddCarSequence first() {
            return values()[0];
        }

        BaseAddCarFragment newFragmentInstance() {
            switch (this) {
                case CAR_PHOTO_FRONT:
                    return DriverCarPhotoFragment.newInstance(this, Constants.CarPhotoType.FRONT);
                case CAR_PHOTO_BACK:
                    return DriverCarPhotoFragment.newInstance(this, Constants.CarPhotoType.BACK);
                case CAR_PHOTO_INSIDE:
                    return DriverCarPhotoFragment.newInstance(this, Constants.CarPhotoType.INSIDE);
                case CAR_PHOTO_TRUNK:
                    return DriverCarPhotoFragment.newInstance(this, Constants.CarPhotoType.TRUNK);
                case VEHICLE_INFORMATION:
                    return DriverVehicleInformationFragment.newInstance(this);
                case SETUP_VEHICLE_YEAR:
                    return SetupVehicleListFragment.newInstance(this, Constants.CarPropertyType.YEAR);
                case SETUP_VEHICLE_MAKE:
                    return SetupVehicleListFragment.newInstance(this, Constants.CarPropertyType.MAKE);
                case SETUP_VEHICLE_MODEL:
                    return SetupVehicleListFragment.newInstance(this, Constants.CarPropertyType.MODEL);
                case SETUP_VEHICLE_COLOR:
                    return SetupVehicleListFragment.newInstance(this, Constants.CarPropertyType.COLOR);
                case INSPECTION_STICKER:
                    return DriverTNCStickerFragment.newInstance(this);
                case LICENSE_PLATE:
                    return LicensePlateFragment.newInstance(this);
                case INFORMATION_SUMMARY:
                    return DriverVehicleInformationSummaryFragment.newInstance(this);
                case INSURANCE:
                    return DriverInsuranceFragment.newInstance(this);
                default:
                    throw new UnsupportedOperationException("Corresponding value has not a fragment");
            }
        }
    }
}
