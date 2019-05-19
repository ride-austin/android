package com.rideaustin.ui.drawer.cars.add;

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
public class DriverTNCStickerFragment extends BaseAddCarFragment implements TakePhotoFragment.TakePhotoListener {
    private com.rideaustin.databinding.DriverTNCStickerBinding binding;
    private BottomSheetBehavior bottomSheetBehavior;
    private MenuItem nextMenuItem;
    private String photoFilePath;
    private TakePhotoFragment.Source photoSource;

    public static DriverTNCStickerFragment newInstance(AddCarActivity.AddCarSequence sequence) {
        DriverTNCStickerFragment fragment = new DriverTNCStickerFragment();
        Bundle args = new Bundle();
        args.putSerializable(SEQUENCE_KEY, sequence);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_driver_tnc_sticker, container, false);
        setToolbarTitle(R.string.title_driver_tnc_sticker);
        setHasOptionsMenu(true);

        return binding.getRoot();
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.openTakePhotoControl.setOnClickListener(v -> showBottomSheet());

        View editPictureBottomView = binding.editPictureBottom;

        showSelectedDate();

        bottomSheetBehavior = BottomSheetBehavior.from(editPictureBottomView);
        bottomSheetBehavior.setHideable(true);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        final TakePhotoFragment takePhotoFragment = TakePhotoFragment.newInstance();

        replaceFragment(takePhotoFragment, R.id.edit_picture_bottom, false);


        photoFilePath = addCarViewModel.getStickerImagePath();

        if (isImageSelected()) {
            onPhotoTaken(photoSource, photoFilePath);
        }

        final DriverRegistration configuration = addCarViewModel.getDriverRegistration();
        binding.stickerTitle1.setText(configuration.getInspectionSticker().getTitle1());
        binding.stickerText1.setText(configuration.getInspectionSticker().getText1());
        setToolbarTitle(configuration.getInspectionSticker().getHeader());

        binding.selectExpirationDateView.setOnClickListener(v -> onSelectExpirationDateClicked());
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

    private boolean isImageSelected() {
        return !TextUtils.isEmpty(photoFilePath);
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

        addCarViewModel.setStickerImagePath(filePath);
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

    private void showSelectedDate() {
        Date expirationDate = addCarViewModel.getTncStickerExpirationDate();
        if (expirationDate == null) {
            binding.selectExpirationDateView.setText(R.string.select);
        } else {
            binding.selectExpirationDateView.setText(DateHelper.dateToSimpleDateFormat(expirationDate));
        }

    }

    private void enableNextStepIfDataValid() {
        if (TextUtils.isEmpty(addCarViewModel.getStickerImagePath())) {
            return;
        }
        if (addCarViewModel.getTncStickerExpirationDate() == null) {
            return;
        }
        if (nextMenuItem != null) {
            nextMenuItem.setEnabled(true);
        }
    }

    private DatePickerDialog.OnDateSetListener onExpirationDateSetListener = (view, year, monthOfYear, dayOfMonth) -> {
        addCarViewModel.setTncStickerExpirationDate(DateHelper.getDate(year, monthOfYear, dayOfMonth));

        showSelectedDate();
        enableNextStepIfDataValid();
    };
}
