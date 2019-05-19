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
import com.rideaustin.api.config.DriverRegistration;
import com.rideaustin.ui.common.TakePhotoFragment;
import com.rideaustin.ui.utils.MenuUtil;
import com.rideaustin.utils.DateHelper;
import com.rideaustin.utils.ImageHelper;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.util.Calendar;
import java.util.Date;


/**
 * Created by rost on 8/9/16.
 */
public class DriverTNCStickerFragment extends BaseDriverSignUpFragment implements TakePhotoFragment.TakePhotoListener {
    private com.rideaustin.databinding.DriverTNCStickerBinding binding;
    private BottomSheetBehavior bottomSheetBehavior;
    private MenuItem nextMenuItem;
    private String photoFilePath;
    private TakePhotoFragment.Source photoSource;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_driver_tnc_sticker, container, false);
        setToolbarTitle(R.string.title_driver_tnc_sticker);
        setHasOptionsMenu(true);
        setHasHelpWidget(true);

        return binding.getRoot();
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.openTakePhotoControl.setOnClickListener(v -> showBottomSheet());
        showSelectedDate();

        View editPictureBottomView = binding.editPictureBottom;

        bottomSheetBehavior = BottomSheetBehavior.from(editPictureBottomView);
        bottomSheetBehavior.setHideable(true);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        final TakePhotoFragment takePhotoFragment = TakePhotoFragment.newInstance();
        takePhotoFragment.setTakePhotoListener(this);

        replaceFragment(takePhotoFragment, R.id.edit_picture_bottom, false);

        if (TextUtils.isEmpty(photoFilePath)) {
            photoFilePath = getDriverData().getDriverTncStickerImagePath();
        }

        //Renew photo after resume
        if (!TextUtils.isEmpty(photoFilePath)) {
            onPhotoTaken(photoSource, photoFilePath);
        }

        final DriverRegistration configuration = getSignUpInteractor().getDriverRegistrationConfiguration();
        binding.stickerTitle1.setText(configuration.getInspectionSticker().getTitle1());
        binding.stickerText1.setText(configuration.getInspectionSticker().getText1());

        binding.selectExpirationDateView.setOnClickListener(v -> onSelectExpirationDateClicked());

        setToolbarTitle(configuration.getInspectionSticker().getHeader());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        nextMenuItem = MenuUtil.inflateNextMenu(menu, inflater, false);
        enableNextStepIfDataValid();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.next:
                notifyCompleted();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showBottomSheet() {
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    @Override
    public void onPhotoTaken(TakePhotoFragment.Source source, final String filePath) {

        photoFilePath = filePath;
        photoSource = source;

        hideBottomSheet();

        getDriverData().setDriverTncStickerImagePath(filePath);
        ImageHelper.loadImageIntoView(binding.license, filePath);
        enableNextStepIfDataValid();
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

    private void onSelectExpirationDateClicked() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog dialog = DatePickerDialog.newInstance(onExpirationDateSetListener, year, month, day);
        dialog.setMinDate(calendar);
        dialog.show(getActivity().getFragmentManager(), "ExpirationDateSelector");
    }

    private void showSelectedDate() {
        Date expirationDate = getDriverData().getTncStickerExpirationDate();
        if (expirationDate == null) {
            binding.selectExpirationDateView.setText(R.string.select);
        } else {
            binding.selectExpirationDateView.setText(DateHelper.dateToSimpleDateFormat(expirationDate));
        }

    }

    private DatePickerDialog.OnDateSetListener onExpirationDateSetListener = (view, year, monthOfYear, dayOfMonth) -> {
        getDriverData().setTncStickerExpirationDate(DateHelper.getDate(year, monthOfYear, dayOfMonth));
        showSelectedDate();
        enableNextStepIfDataValid();
    };

    private void enableNextStepIfDataValid() {
        boolean isEnabled = !TextUtils.isEmpty(getDriverData().getDriverTncStickerImagePath())
                && getDriverData().getTncStickerExpirationDate() != null;
        if (nextMenuItem != null) {
            nextMenuItem.setEnabled(isEnabled);
        }
    }
}
