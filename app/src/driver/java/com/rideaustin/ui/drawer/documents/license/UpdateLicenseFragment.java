package com.rideaustin.ui.drawer.documents.license;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;
import com.rideaustin.App;
import com.rideaustin.R;
import com.rideaustin.base.BaseApiException;
import com.rideaustin.base.BaseFragment;
import com.rideaustin.databinding.DriverLicenseBinding;
import com.rideaustin.ui.common.TakePhotoFragment;
import com.rideaustin.ui.utils.CommonMaterialDialogCreator;
import com.rideaustin.ui.utils.MaterialDialogCreator;
import com.rideaustin.ui.utils.MenuUtil;
import com.rideaustin.utils.DateHelper;
import com.rideaustin.utils.ImageHelper;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by crossover on 02/02/2017.
 */

public class UpdateLicenseFragment extends BaseFragment implements UpdateLicenseView, TakePhotoFragment.TakePhotoListener {

    private DriverLicenseBinding binding;
    private UpdateLicenseViewModel viewModel;
    private UpdateLicenseFragmentCallback listener;
    private BottomSheetBehavior bottomSheetBehavior;
    private MenuItem nextMenuItem;
    private Target<Bitmap> licenseTarget;

    public static Fragment newInstance() {
        Bundle args = new Bundle();
        UpdateLicenseFragment fragment = new UpdateLicenseFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (UpdateLicenseFragmentCallback) context;
            listener.setTakePhotoListener(this);
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("UpdateLicenseFragment can be attached only to UpdateLicenseFragmentCallback", e);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_driver_license, container, false);
        binding.licenseText1.setText(R.string.update_your_license);
        binding.licenseText1.setGravity(Gravity.CENTER);
        binding.licenseText2.setVisibility(View.GONE);
        viewModel = new UpdateLicenseViewModel(this);

        setHasOptionsMenu(true);
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
        replaceFragment(takePhotoFragment, R.id.edit_picture_bottom, false);
        binding.selectExpirationDateView.setOnClickListener(v -> onSelectExpirationDateClicked());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        nextMenuItem = MenuUtil.inflateNextMenu(menu, inflater, false);
        nextMenuItem.setTitle(R.string.update);
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

    private void notifyCompleted() {
        viewModel.uploadLicense(getCallback());
    }

    private void onSelectExpirationDateClicked() {
        Calendar calendar = Calendar.getInstance();
        if (viewModel.getShownDate() != null) {
            calendar.setTime(viewModel.getShownDate());
        }
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog dialog = DatePickerDialog.newInstance(onExpirationDateSetListener, year, month, day);
        dialog.setMinDate(Calendar.getInstance()); // today
        dialog.show(getActivity().getFragmentManager(), "ExpirationDateSelector");
    }

    private DatePickerDialog.OnDateSetListener onExpirationDateSetListener = (view, year, monthOfYear, dayOfMonth) -> {
        Date expirationDate = DateHelper.getDate(year, monthOfYear, dayOfMonth);
        viewModel.setLicenseExpirationDate(expirationDate);
        showSelectedDate(expirationDate);
        enableNextStepIfDataValid();
    };

    private void showSelectedDate(Date expirationDate) {
        if (expirationDate == null) {
            binding.selectExpirationDateView.setText(R.string.select);
        } else {
            binding.selectExpirationDateView.setText(DateHelper.dateToSimpleDateFormat(expirationDate));
        }
    }

    private void enableNextStepIfDataValid() {
        if (viewModel.getShownDate() == null) {
            //If we don't show a date, just leave it disabled.
            return;
        }
        if (TextUtils.isEmpty(viewModel.getLicenseImagePath()) && viewModel.getLicenseExpirationDate() == null) {
            //If we haven't picked an image and haven't selected a date, then leave it disabled.
            return;
        }
        if (nextMenuItem != null) {
            nextMenuItem.setEnabled(true);
        }
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
        if (licenseTarget != null) {
            Glide.with(binding.license).clear(licenseTarget);
        }
    }


    private void showBottomSheet() {
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    private void hideBottomSheet() {
        int state = bottomSheetBehavior.isHideable()
                ? BottomSheetBehavior.STATE_HIDDEN
                : BottomSheetBehavior.STATE_COLLAPSED;
        bottomSheetBehavior.setState(state);
    }

    @Override
    public void onCanceled() {
        hideBottomSheet();
    }

    @Override
    public void onPhotoTaken(TakePhotoFragment.Source source, String filePath) {
        viewModel.onPhotoTaken(filePath);
        hideBottomSheet();
        enableNextStepIfDataValid();
    }

    @Override
    public void onLicenseSelected(String imagePath) {
        licenseTarget = ImageHelper.loadImageIntoViewWithDefaultProgress(binding.license, imagePath, R.drawable.icn_license);
    }

    @Override
    public void onLicenseUpdated() {
        MaterialDialog dialog = MaterialDialogCreator.createCenteredMessageDialog(App.getAppName(), getString(R.string.documents_updated), (AppCompatActivity) getActivity());
        dialog.setOnDismissListener(dialogInterface -> listener.onCompleted());
        dialog.show();
    }

    @Override
    public void onLicenseUploadFailed(BaseApiException e) {
        CommonMaterialDialogCreator.createNetworkFailDialog(getString(R.string.failed_license_upload), getContext()).onPositive((dialog, which) -> viewModel.uploadLicense(getCallback())).show();
    }

    @Override
    public void onLicenseDownloaded(String licensePictureUrl) {
        onLicenseSelected(licensePictureUrl);
    }

    @Override
    public void showLicenseDate(Date date) {
        showSelectedDate(date);
        enableNextStepIfDataValid();
    }

    @Override
    public void onLicenseDownloadFailed() {
        CommonMaterialDialogCreator.createNetworkFailDialog(getString(R.string.failed_license_fetch), getContext())
                .onPositive((dialog, which) -> viewModel.loadDocument(getCallback()))
                .onNegative((dialog, which) -> listener.onCompleted())
                .show();
    }

    public interface UpdateLicenseFragmentCallback {
        void setTakePhotoListener(TakePhotoFragment.TakePhotoListener photoListener);

        void onCompleted();
    }
}
