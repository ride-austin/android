package com.rideaustin.ui.signup.driver;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.MaterialDialog;
import com.rideaustin.R;
import com.rideaustin.base.ApiSubscriber;
import com.rideaustin.databinding.DriverCarPhotoBinding;
import com.rideaustin.ui.common.TakePhotoFragment;
import com.rideaustin.ui.utils.MaterialDialogCreator;
import com.rideaustin.ui.utils.MenuUtil;
import com.rideaustin.utils.Constants;
import com.rideaustin.utils.ImageHelper;


/**
 * Created by rost on 8/9/16.
 */
public class DriverCarPhotoFragment extends BaseDriverSignUpFragment implements TakePhotoFragment.TakePhotoListener {

    public static final String PHOTO_TYPE_KEY = "photo_type_key";

    private ApiSubscriber carSubscriber;
    private DriverCarPhotoBinding binding;
    private BottomSheetBehavior bottomSheetBehavior;
    private MenuItem nextMenuItem;
    private String photoFilePath;
    private TakePhotoFragment.Source photoSource;
    private String sureDialogMessage;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_driver_car_photo, container, false);
        carSubscriber = new ApiSubscriber<Boolean>(getCallback()) {
            @Override
            public void onCompleted() {
                super.onCompleted();
                DriverCarPhotoFragment.this.notifyCompleted();
            }

            @Override
            public void onNext(Boolean aVoid) {
            }
        };
        setToolbarTitle(R.string.title_driver_vehicle_information);
        setHasOptionsMenu(true);
        setHasHelpWidget(true);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final String type = getCurrentType();

        initUiByType(type);

        binding.openTakePhotoControl.setOnClickListener(v -> showBottomSheet());

        View editPictureBottomView = binding.editPictureBottom;

        bottomSheetBehavior = BottomSheetBehavior.from(editPictureBottomView);
        bottomSheetBehavior.setHideable(true);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        final TakePhotoFragment takePhotoFragment = TakePhotoFragment.newInstance();
        takePhotoFragment.setTakePhotoListener(this);

        replaceFragment(takePhotoFragment, R.id.edit_picture_bottom, false);

        if (TextUtils.isEmpty(photoFilePath)) {
            photoFilePath = getDriverData().getCarPhotoFile(type);
        }
        //Renew photo after resume
        if (!TextUtils.isEmpty(photoFilePath)) {
            onPhotoTaken(photoSource, photoFilePath);
        }
    }

    private String getCurrentType() {
        Bundle args = getArguments();
        return args.getString(PHOTO_TYPE_KEY);
    }

    private void initUiByType(String type) {

        @DrawableRes int resCarDetail = R.drawable.icon_car_front;
        @StringRes int resTitle = R.string.title_driver_vehicle_information;

        if (Constants.CarPhotoType.FRONT.equals(type)) {
            resCarDetail = R.drawable.icon_car_front;
            resTitle = R.string.car_photo_front;
            sureDialogMessage = getString(R.string.car_photo_dialog_front);
        } else if (Constants.CarPhotoType.BACK.equals(type)) {
            resCarDetail = R.drawable.icon_car_back;
            resTitle = R.string.car_photo_back;
            sureDialogMessage = getString(R.string.car_photo_dialog_back);
        } else if (Constants.CarPhotoType.INSIDE.equals(type)) {
            resCarDetail = R.drawable.icon_car_inside;
            resTitle = R.string.car_photo_inside;
            sureDialogMessage = getString(R.string.car_photo_dialog_inside);
        } else if (Constants.CarPhotoType.TRUNK.equals(type)) {
            resCarDetail = R.drawable.icon_car_trunk;
            resTitle = R.string.car_photo_trunk;
            sureDialogMessage = getString(R.string.car_photo_dialog_trunk);
        } else {
            notifyCompleted();
        }
        binding.textDetail.setText(resTitle);
        binding.carDetail.setImageResource(resCarDetail);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        nextMenuItem = MenuUtil.inflateNextMenu(menu, inflater, isImageSelected());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (!carSubscriber.isUnsubscribed()) {
            carSubscriber.unsubscribe();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.next:
                if (!isImageSelected()) {
                    showSureDialog(getCurrentType());
                } else {
                    notifyCompleted();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean isImageSelected() {
        return !TextUtils.isEmpty(getDriverData().getCarPhotoFile(getCurrentType()));
    }

    private void showSureDialog(String type) {
        final MaterialDialog dialog = MaterialDialogCreator
                .createSimpleDialog((Constants.CarPhotoType.TRUNK.equals(type) ? sureDialogMessage : getString(R.string.car_photo_confirm, sureDialogMessage)), (AppCompatActivity) getActivity());
        dialog.show();
    }

    private void showBottomSheet() {
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    @Override
    public void onPhotoTaken(TakePhotoFragment.Source source, final String filePath) {
        photoFilePath = filePath;
        photoSource = source;

        hideBottomSheet();
        getDriverData().addCarPhotoFile(getCurrentType(), filePath);
        ImageHelper.loadImageIntoViewWithCompression(binding.carDetail, filePath);
        if (nextMenuItem != null) {
            nextMenuItem.setEnabled(isImageSelected());
        }
    }

    @Override
    public void onCanceled() {
        hideBottomSheet();
    }

    private void hideBottomSheet() {
        int state = bottomSheetBehavior.isHideable()
                ? BottomSheetBehavior.STATE_HIDDEN
                : BottomSheetBehavior.STATE_COLLAPSED;
        bottomSheetBehavior.setState(state);
    }
}
