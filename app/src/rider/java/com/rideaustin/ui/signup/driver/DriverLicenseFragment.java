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
import com.rideaustin.databinding.DriverLicenseBinding;
import com.rideaustin.ui.common.TakePhotoFragment;
import com.rideaustin.ui.utils.MenuUtil;
import com.rideaustin.utils.DateHelper;
import com.rideaustin.utils.ImageHelper;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import org.joda.time.DateTime;

import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by rost on 8/9/16.
 */
public class DriverLicenseFragment extends BaseDriverSignUpFragment implements TakePhotoFragment.TakePhotoListener {
    private DriverLicenseBinding binding;
    private BottomSheetBehavior bottomSheetBehavior;
    private MenuItem nextMenuItem;
    private String photoFilePath;
    private TakePhotoFragment.Source photoSource;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_driver_license, container, false);

        setToolbarTitle(R.string.title_driver_license);
        setHasOptionsMenu(true);
        setHasHelpWidget(true);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.openTakePhotoControl.setOnClickListener(v -> showBottomSheet());
        binding.selectExpirationDateView.setOnClickListener(v -> onSelectExpirationDateClicked());
        showSelectedDate();

        View editPictureBottomView = binding.editPictureBottom;

        bottomSheetBehavior = BottomSheetBehavior.from(editPictureBottomView);
        bottomSheetBehavior.setHideable(true);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        final TakePhotoFragment takePhotoFragment = TakePhotoFragment.newInstance();
        takePhotoFragment.setTakePhotoListener(this);

        replaceFragment(takePhotoFragment, R.id.edit_picture_bottom, false);

        if (!TextUtils.isEmpty(getDriverData().getLicenseFilePath())) {
            // RA-14045: prefer to use file path from model (file can be moved)
            photoFilePath = getDriverData().getLicenseFilePath();
        }

        // Renew photo after resume
        if (!TextUtils.isEmpty(photoFilePath)) {
            onPhotoTaken(photoSource, photoFilePath);
        }
    }

    private void onSelectExpirationDateClicked() {
        DateTime tomorrow = DateTime.now().plusDays(1);
        DatePickerDialog dialog = DatePickerDialog.newInstance(onExpirationDateSetListener, tomorrow.getYear(), tomorrow.getMonthOfYear(), tomorrow.getDayOfMonth());
        dialog.setMinDate(new GregorianCalendar(tomorrow.getYear(), tomorrow.getMonthOfYear(), tomorrow.getDayOfMonth()));
        dialog.show(getActivity().getFragmentManager(), "ExpirationDateSelector");
    }

    private void showSelectedDate() {
        Date expirationDate = getDriverData().getLicenseExpirationDate();
        if (expirationDate == null) {
            binding.selectExpirationDateView.setText(R.string.select);
        } else {
            binding.selectExpirationDateView.setText(DateHelper.dateToSimpleDateFormat(expirationDate));
        }
    }

    private DatePickerDialog.OnDateSetListener onExpirationDateSetListener = (view, year, monthOfYear, dayOfMonth) -> {
        getDriverData().setLicenseExpirationDate(DateHelper.getDate(year, monthOfYear, dayOfMonth));
        showSelectedDate();
        enableNextStepIfDataValid();
    };

    private void enableNextStepIfDataValid() {
        boolean isEnabled = !TextUtils.isEmpty(getDriverData().getLicenseFilePath())
                && getDriverData().getLicenseExpirationDate() != null;
        if (nextMenuItem != null) {
            nextMenuItem.setEnabled(isEnabled);
        }
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

        getDriverData().setLicenseFilePath(filePath);
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
}
