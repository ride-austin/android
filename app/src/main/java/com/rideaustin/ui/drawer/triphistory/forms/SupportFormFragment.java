package com.rideaustin.ui.drawer.triphistory.forms;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rideaustin.R;
import com.rideaustin.api.model.SupportField;
import com.rideaustin.api.model.SupportForm;
import com.rideaustin.base.BaseFragment;
import com.rideaustin.databinding.FragmentTripSupportFormBinding;
import com.rideaustin.databinding.SupportFormBinding;
import com.rideaustin.ui.common.TakePhotoFragment;
import com.rideaustin.ui.utils.CommonMaterialDialogCreator;
import com.rideaustin.ui.utils.phone.PhoneInputUtil;
import com.rideaustin.utils.toast.RAToast;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.util.Calendar;
import java.util.List;

import java8.util.Optional;

/**
 * Created by crossover on 24/05/2017.
 */

public class SupportFormFragment extends BaseFragment implements SupportFormViewModel.SupportFormView, SupportFieldViewModel.SupportFieldView {

    private static final String RIDE_KEY = "ride_key";
    private static final String PARENT_TOPIC_KEY = "parent_topic_key";

    private BottomSheetBehavior bottomSheetBehavior;
    private FragmentTripSupportFormBinding binding;
    private TakePhotoFragment takePhotoFragment;
    private SupportFormViewModel viewModel;

    public static SupportFormFragment newInstance(long rideId, int topicId) {
        SupportFormFragment fragment = new SupportFormFragment();
        Bundle args = new Bundle();
        args.putLong(RIDE_KEY, rideId);
        args.putInt(PARENT_TOPIC_KEY, topicId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_trip_support_form, container, false);
        long rideId = getArguments() != null && getArguments().containsKey(RIDE_KEY) ? getArguments().getLong(RIDE_KEY) : -1;
        int parentTopicId = getArguments() != null && getArguments().containsKey(PARENT_TOPIC_KEY) ? getArguments().getInt(PARENT_TOPIC_KEY) : -1;
        viewModel = new SupportFormViewModel(this, rideId, parentTopicId);
        binding.setViewModel(viewModel);
        String title = viewModel.getTitle();
        if (title != null) {
            setToolbarTitleAligned(title, Gravity.LEFT);
        } else {
            setToolbarTitleAligned(R.string.trip_history_support_topics_title, Gravity.LEFT);
        }
        viewModel.setupForm();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        View editPictureBottomView = binding.editPictureBottom;

        bottomSheetBehavior = BottomSheetBehavior.from(editPictureBottomView);
        bottomSheetBehavior.setHideable(true);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        takePhotoFragment = TakePhotoFragment.newInstance();
        replaceFragment(takePhotoFragment, R.id.edit_picture_bottom, false);
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
    public void onUnexpectedState() {
        // TODO: Decide what to do here
        // If data lost on app restart, we would go to login recently
        // If no data because of illegal state, better to throw an Exception
    }

    @Override
    public void onActionClicked(String message) {
        if (isAttached()) {
            CommonMaterialDialogCreator.showSupportSuccessDialog(getActivity(), Optional.ofNullable(message), true);
        }
    }

    @Override
    public void onError() {
        RAToast.showShort(R.string.error_unknown);
        onBackPressed();
    }

    @Override
    public void onSupportFields(SupportForm supportForm, List<SupportField> supportFields) {
        binding.fieldsLayout.removeAllViews();
        for (SupportField field : supportForm.getSupportFields()) {
            SupportFormBinding fieldBinding = DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout.support_form, binding.fieldsLayout, false);
            SupportFieldViewModel fieldViewModel = new SupportFieldViewModel(this, fieldBinding, field);
            fieldBinding.setViewModel(fieldViewModel);
            if (SupportFieldViewModel.FieldType.PHONE.equals(field.getFieldType())) {
                PhoneInputUtil phoneInputUtil = new PhoneInputUtil(getContext(), fieldBinding.formPhone.country, fieldBinding.formPhone.mobile);
                fieldViewModel.setPhoneUtils(phoneInputUtil);
            }
            binding.fieldsLayout.addView(fieldBinding.getRoot());
            viewModel.putFieldViewModel(fieldViewModel);
        }
    }

    @Override
    public void showDatePickerDialog(SupportFieldViewModel.DatePickedListener listener) {
        final Calendar calendar = Calendar.getInstance();

        DatePickerDialog dialog = DatePickerDialog.newInstance((view, year, monthOfYear, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, monthOfYear);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            TimePickerDialog timePickerDialog = TimePickerDialog.newInstance((view1, hourOfDay, minute, second) -> {
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);
                calendar.set(Calendar.SECOND, second);
                listener.onDatePicked(calendar);
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
            timePickerDialog.show(getActivity().getFragmentManager(), "TimePicker");
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        dialog.setMaxDate(calendar);
        dialog.show(getActivity().getFragmentManager(), "DatePicker");
    }

    @Override
    public void showPhotoPicker(SupportFieldViewModel.PhotoPickedListener listener) {
        takePhotoFragment.setTakePhotoListener(new TakePhotoFragment.TakePhotoListener() {
            @Override
            public void onPhotoTaken(TakePhotoFragment.Source source, String filePath) {
                hideBottomSheet();
                listener.onPhotoPicked(filePath);
            }

            @Override
            public void onCanceled() {
                hideBottomSheet();
            }
        });
        showBottomSheet();
    }

    private void showBottomSheet() {
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    @Override
    public void hideBottomSheet() {
        int state = bottomSheetBehavior.isHideable()
                ? BottomSheetBehavior.STATE_HIDDEN
                : BottomSheetBehavior.STATE_COLLAPSED;
        bottomSheetBehavior.setState(state);
    }
}