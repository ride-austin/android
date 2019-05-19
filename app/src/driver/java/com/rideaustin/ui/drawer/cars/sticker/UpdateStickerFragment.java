package com.rideaustin.ui.drawer.cars.sticker;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;
import com.rideaustin.App;
import com.rideaustin.R;
import com.rideaustin.api.config.DriverRegistration;
import com.rideaustin.api.config.InspectionSticker;
import com.rideaustin.base.BaseApiException;
import com.rideaustin.base.BaseFragment;
import com.rideaustin.ui.common.TakePhotoFragment;
import com.rideaustin.ui.utils.CommonMaterialDialogCreator;
import com.rideaustin.ui.utils.MaterialDialogCreator;
import com.rideaustin.ui.utils.MenuUtil;
import com.rideaustin.utils.DateHelper;
import com.rideaustin.utils.ImageHelper;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.util.Calendar;
import java.util.Date;

import java8.util.Optional;

/**
 * Created by hatak on 2/6/17.
 */

public class UpdateStickerFragment extends BaseFragment implements TakePhotoFragment.TakePhotoListener, UpdateStickerView {

    private com.rideaustin.databinding.DriverTNCStickerBinding binding;
    private BottomSheetBehavior bottomSheetBehavior;
    private MenuItem nextMenuItem;
    private UpdateStickerFragmentCallback listener;
    private UpdateStickerViewModel viewModel;
    private Target<Bitmap> stickerTarget;

    public static UpdateStickerFragment newInstance() {
        Bundle args = new Bundle();
        UpdateStickerFragment fragment = new UpdateStickerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (UpdateStickerFragmentCallback) context;
            listener.setTakePhotoListener(this);
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("UpdateStickerFragment can be attached only to UpdateStickerFragmentCallback", e);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_driver_tnc_sticker, container, false);

        setHasOptionsMenu(true);
        viewModel = new UpdateStickerViewModel(this, listener.getCar());

        DriverRegistration registrationConfiguration = listener.getRegistrationConfiguration();

        binding.stickerTitle1.setText(registrationConfiguration.getInspectionSticker().getTitle1());
        binding.stickerText1.setText(registrationConfiguration.getInspectionSticker().getText1());

        binding.selectExpirationDateView.setOnClickListener(v -> onSelectExpirationDateClicked());

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
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        nextMenuItem = MenuUtil.inflateNextMenu(menu, inflater, false);
        nextMenuItem.setTitle(R.string.update);
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
        viewModel.uploadInspectionSticker(getCallback());
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
        if (stickerTarget != null) {
            Glide.with(binding.license).clear(stickerTarget);
        }
    }

    private void showBottomSheet() {
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    @Override
    public void onPhotoTaken(TakePhotoFragment.Source source, final String filePath) {
        viewModel.onPhotoTaken(filePath);
        hideBottomSheet();
        if (nextMenuItem != null && viewModel.isImageSelected(viewModel.getPhotoFilePath())) {
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

    @Override
    public void onStickerSelected(String imagePath) {
        stickerTarget = ImageHelper.loadImageIntoView(binding.license, imagePath, R.drawable.icn_insurance);
    }

    @Override
    public void onStickerUpdated() {
        MaterialDialog dialog = MaterialDialogCreator.createCenteredMessageDialog(App.getAppName(), getString(R.string.documents_updated), (AppCompatActivity) getActivity());
        dialog.setOnDismissListener(dialogInterface -> listener.onCompleted());
        dialog.show();
    }

    @Override
    public void onStickerUploadFailed(BaseApiException e) {
        String message = getString(R.string.failed_sticker_upload, getStickerName());
        CommonMaterialDialogCreator.createNetworkFailDialog(message, getContext())
                .onPositive((dialog, which) -> viewModel.uploadInspectionSticker(getCallback())).show();
    }

    @Override
    public void onStickerDownloaded(String insurancePictureUrl) {
        onStickerSelected(insurancePictureUrl);
    }

    @Override
    public void onStickerDownloadFailed() {
        String message = getString(R.string.failed_sticker_fetch, getStickerName());
        CommonMaterialDialogCreator.createNetworkFailDialog(message, getContext())
                .onPositive((dialog, which) -> viewModel.loadDocument(getCallback()))
                .onNegative((dialog, which) -> listener.onCompleted())
                .show();
    }

    @Override
    public void showStickerDate(Date date) {
        showSelectedDate(date);
        enableNextStepIfDataValid();
    }

    private String getStickerName() {
        return Optional.ofNullable(listener)
                .map(UpdateStickerFragmentCallback::getRegistrationConfiguration)
                .map(DriverRegistration::getInspectionSticker)
                .map(InspectionSticker::getHeader)
                .orElse(getString(R.string.inspection_sticker));
    }

    private void onSelectExpirationDateClicked() {
        Calendar calendar = Calendar.getInstance();
        if (viewModel.getTncStickerExpirationDate() != null) {
            calendar.setTime(viewModel.getTncStickerExpirationDate());
        }
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog dialog = DatePickerDialog.newInstance(onExpirationDateSetListener, year, month, day);
        dialog.setMinDate(Calendar.getInstance()); // today
        dialog.show(getActivity().getFragmentManager(), "ExpirationDateSelector");
    }

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
        if (TextUtils.isEmpty(viewModel.getPhotoFilePath()) && viewModel.getTncStickerExpirationDate() == null) {
            //If we haven't picked an image and haven't selected a date, then leave it disabled.
            return;
        }
        if (nextMenuItem != null) {
            nextMenuItem.setEnabled(true);
        }
    }

    private DatePickerDialog.OnDateSetListener onExpirationDateSetListener = (view, year, monthOfYear, dayOfMonth) -> {
        Date expirationDate = DateHelper.getDate(year, monthOfYear, dayOfMonth);
        viewModel.setTncStickerExpirationDate(expirationDate);
        showSelectedDate(expirationDate);
        enableNextStepIfDataValid();
    };
}
