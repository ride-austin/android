package com.rideaustin.ui.signup.driver;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.rideaustin.R;
import com.rideaustin.models.DriverRegistrationData;
import com.rideaustin.ui.common.TakePhotoFragment;
import com.rideaustin.ui.utils.CommonMaterialDialogCreator;
import com.rideaustin.ui.utils.MenuUtil;
import com.rideaustin.utils.ImageHelper;

/**
 * Created by rost on 8/9/16.
 */
public class DriverPhotoFragment extends BaseDriverSignUpFragment implements TakePhotoFragment.TakePhotoListener {
    private com.rideaustin.databinding.DriverPhotoBinding binding;
    private BottomSheetBehavior bottomSheetBehavior;
    private MenuItem nextMenuItem;
    private String photoFilePath;
    private TakePhotoFragment.Source photoSource;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_driver_photo, container, false);

        setToolbarTitle(R.string.title_driver_photo);
        setHasOptionsMenu(true);
        setHasHelpWidget(true);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.openTakePhotoControl.setOnClickListener(v -> showBottomSheet());

        View editPictureBottomView = binding.editPictureBottom;

        bottomSheetBehavior = BottomSheetBehavior.from(editPictureBottomView);
        bottomSheetBehavior.setHideable(true);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        final TakePhotoFragment takePhotoFragment = TakePhotoFragment.newInstance();
        takePhotoFragment.setTakePhotoListener(this);

        replaceFragment(takePhotoFragment, R.id.edit_picture_bottom, false);

        if (TextUtils.isEmpty(photoFilePath)) {
            photoFilePath = getDriverData().getDriverPhotoImagePath();
        }

        //Renew photo after resume
        if (!TextUtils.isEmpty(photoFilePath)) {
            onPhotoTaken(photoSource, photoFilePath);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        final DriverRegistrationData driverRegistrationData = getDriverData();
        boolean hasDriverPhoto = !TextUtils.isEmpty(driverRegistrationData.getDriverPhotoImagePath());
        nextMenuItem = MenuUtil.inflateNextMenu(menu, inflater, hasDriverPhoto);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.next:
                showClearConfirmation();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showClearConfirmation() {
        CommonMaterialDialogCreator.createYesNoDialog(getString(R.string.driver_clear_photo), getContext())
                .onPositive((dialog, which) -> notifyCompleted())
                .show();
    }

    private void showBottomSheet() {
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    @Override
    public void onPhotoTaken(TakePhotoFragment.Source source, final String filePath) {
        photoFilePath = filePath;
        photoSource = source;

        hideBottomSheet();

        getDriverData().setDriverPhotoImagePath(filePath);
        ImageHelper.loadImageIntoView(binding.photo, filePath);

        if (nextMenuItem != null) {
            nextMenuItem.setEnabled(true);
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
