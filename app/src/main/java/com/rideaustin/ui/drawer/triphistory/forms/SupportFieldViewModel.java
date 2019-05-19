package com.rideaustin.ui.drawer.triphistory.forms;

import android.databinding.Bindable;
import android.support.annotation.StringDef;
import android.telephony.PhoneNumberUtils;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import com.rideaustin.App;
import com.rideaustin.BR;
import com.rideaustin.R;
import com.rideaustin.api.model.SupportField;
import com.rideaustin.databinding.SupportFormBinding;
import com.rideaustin.schedulers.RxSchedulers;
import com.rideaustin.ui.common.RxBaseObservable;
import com.rideaustin.ui.utils.phone.PhoneInputUtil;
import com.rideaustin.utils.Constants;
import com.rideaustin.utils.DateHelper;
import com.rideaustin.utils.ImageHelper;
import com.rideaustin.utils.KeyboardUtil;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import static com.rideaustin.ui.drawer.triphistory.forms.SupportFieldViewModel.FieldType.BOOL;
import static com.rideaustin.ui.drawer.triphistory.forms.SupportFieldViewModel.FieldType.DATE;
import static com.rideaustin.ui.drawer.triphistory.forms.SupportFieldViewModel.FieldType.PHONE;
import static com.rideaustin.ui.drawer.triphistory.forms.SupportFieldViewModel.FieldType.PHOTO;
import static com.rideaustin.ui.drawer.triphistory.forms.SupportFieldViewModel.FieldType.TEXT;

/**
 * Created by crossover on 25/05/2017.
 */

public class SupportFieldViewModel extends RxBaseObservable implements View.OnFocusChangeListener {
    private SupportField supportField;
    private PhoneInputUtil phoneInputUtil;
    private SupportFieldView supportFieldView;

    private String inputText = "";
    private String inputPhoto;
    private boolean inputBoolean;
    private Calendar inputDate = Calendar.getInstance();

    private SupportFormBinding binding;
    private DirtyListener dirtyListener;

    private TextWatcher watcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (dirtyListener != null) {
                dirtyListener.onDirtyChange();
            }
        }
    };

    public SupportFieldViewModel(SupportFieldView view, SupportFormBinding binding, SupportField field) {
        this.binding = binding;
        supportField = field;
        supportFieldView = view;
        if (DATE.equals(supportField.getFieldType())) {
            binding.formDate.formInput.setOnClickListener(v -> showDatePickerDialog());
            binding.formDate.formInput.setText(DateHelper.dateToUiDateTimeAtFormat(inputDate.getTime()));
        } else if (PHOTO.equals(supportField.getFieldType())) {
            binding.formPhoto.formImage.setOnClickListener(v -> showPhotoPicker());
        }

        settleFocusListeners();
    }

    private void settleFocusListeners() {
        switch (supportField.getFieldType()) {
            case PHONE:
                binding.formPhone.mobile.setOnFocusChangeListener(this);
                break;
            case TEXT:
                binding.formText.formInput.setOnFocusChangeListener(this);
                break;
            case DATE:
                binding.formDate.formInput.setOnFocusChangeListener(this);
                break;
            case BOOL:
                binding.formBoolean.radioButtonNo.setOnFocusChangeListener(this);
                binding.formBoolean.radioButtonYes.setOnFocusChangeListener(this);
                break;
            case PHOTO:
                binding.formPhoto.formImage.setOnFocusChangeListener(this);
                break;
            default:
                throw new UnsupportedOperationException("This field type is not supported");
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (v == binding.formPhoto.formImage) {
            if (hasFocus) {
                KeyboardUtil.hideKeyBoard(v.getContext(), v);
                RxSchedulers.schedule(this::showPhotoPicker, 200, TimeUnit.MILLISECONDS);
            }
            return;
        }
        if (hasFocus) {
            supportFieldView.hideBottomSheet();
        }
        if (v == binding.formDate.formInput) {
            if (hasFocus) {
                KeyboardUtil.hideKeyBoard(v.getContext(), v);
                RxSchedulers.schedule(this::showDatePickerDialog, 200, TimeUnit.MILLISECONDS);
            }
        }
    }

    private void showPhotoPicker() {
        supportFieldView.showPhotoPicker(photo -> {
            inputPhoto = photo;
            ImageHelper.loadImageIntoView(binding.formPhoto.formImage, photo);
            if (dirtyListener != null) {
                dirtyListener.onDirtyChange();
            }
        });
    }

    @Bindable
    public SupportField getSupportField() {
        return supportField;
    }

    private void showDatePickerDialog() {
        supportFieldView.showDatePickerDialog(calendar -> {
            inputDate = calendar;
            binding.formDate.formInput.setText(DateHelper.dateToUiDateTimeAtFormat(inputDate.getTime()));
        });
    }


    public void setPhoneUtils(PhoneInputUtil phoneInputUtil) {
        this.phoneInputUtil = phoneInputUtil;
    }

    @Bindable
    public String getInputText() {
        return inputText;
    }

    public void setInputText(String inputText) {
        this.inputText = inputText;
        notifyPropertyChanged(BR.inputText);
    }

    @Bindable
    public boolean isInputBoolean() {
        return inputBoolean;
    }

    public void setInputBoolean(boolean inputBoolean) {
        this.inputBoolean = inputBoolean;
        notifyPropertyChanged(BR.inputBoolean);
    }

    public String getStringInput() {
        switch (supportField.getFieldType()) {
            case DATE:
                return DateHelper.dateToServerDateTimeFormat(inputDate.getTime());
            case PHONE:
                return phoneInputUtil.validate();
            case TEXT:
                return inputText.trim();
            default:
                throw new IllegalArgumentException("This feature is not implemented yet");
        }
    }

    public boolean getBooleanInput() {
        return inputBoolean;
    }

    public boolean verify() {
        switch (supportField.getFieldType()) {
            case PHONE:
                String mobile = phoneInputUtil.validate();
                if (TextUtils.isEmpty(mobile) || mobile.equals(PhoneInputUtil.PLUS_SIGN)) {
                    if (supportField.isMandatory()) {
                        binding.formPhone.mobile.setError(App.getInstance().getText(R.string.mobile_error));
                        binding.formPhone.mobile.requestFocus();
                        return false;
                    }
                } else if (!PhoneNumberUtils.isGlobalPhoneNumber(mobile)) {
                    binding.formPhone.mobile.setError(App.getInstance().getText(R.string.invalid_mobile_error));
                    binding.formPhone.mobile.requestFocus();
                    return false;
                } else if (mobile.length() < Constants.PHONE_FIELD_LENGTH) {
                    binding.formPhone.mobile.setError(App.getInstance().getText(R.string.phone_number_10_digits));
                    binding.formPhone.mobile.requestFocus();
                    return false;
                }
                break;
            case TEXT:
                if (TextUtils.isEmpty(inputText.trim()) && supportField.isMandatory()) {
                    binding.formText.formInput.setError(App.getInstance().getText(R.string.field_required));
                    binding.formText.formInput.requestFocus();
                    return false;
                }
                break;
            case PHOTO:
                if (TextUtils.isEmpty(inputPhoto) && supportField.isMandatory()) {
                    Toast.makeText(App.getInstance(), R.string.please_select_photo, Toast.LENGTH_SHORT).show();
                    return false;
                }
                return true;
            case DATE:
            case BOOL:
            default:
                return true;
        }
        return true;
    }

    public String getPhotoPath() {
        return inputPhoto;
    }

    public void setDirtyListener(final DirtyListener listener) {
        this.dirtyListener = listener;
        switch (supportField.getFieldType()) {
            case PHONE:
                RxSchedulers.schedule(() -> binding.formPhone.mobile.addTextChangedListener(watcher), 500, TimeUnit.MILLISECONDS);
                break;
            case TEXT:
                binding.formText.formInput.addTextChangedListener(watcher);
                break;
            default:
        }
    }

    public interface DatePickedListener {
        void onDatePicked(Calendar calendar);
    }

    public interface PhotoPickedListener {
        void onPhotoPicked(String photo);
    }

    public interface SupportFieldView {

        void showDatePickerDialog(DatePickedListener listener);

        void showPhotoPicker(PhotoPickedListener listener);

        void hideBottomSheet();
    }

    @StringDef({PHONE, BOOL, TEXT, DATE, PHOTO})
    public @interface FieldType {
        String PHONE = "phone";
        String BOOL = "bool";
        String TEXT = "text";
        String DATE = "date";
        String PHOTO = "photo";
    }
}
