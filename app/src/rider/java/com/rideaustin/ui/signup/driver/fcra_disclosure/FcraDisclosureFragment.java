package com.rideaustin.ui.signup.driver.fcra_disclosure;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;

import com.rideaustin.App;
import com.rideaustin.R;
import com.rideaustin.api.model.Avatar;
import com.rideaustin.api.model.User;
import com.rideaustin.api.model.driver.DriverRegistration;
import com.rideaustin.databinding.FCRADisclosureBinding;
import com.rideaustin.ui.signup.driver.BaseDriverSignUpFragment;
import com.rideaustin.ui.utils.MenuUtil;
import com.rideaustin.ui.utils.PasswordMaskTransformationMethod;
import com.rideaustin.ui.utils.ProfileValidator;
import com.rideaustin.utils.DateHelper;
import com.rideaustin.utils.KeyboardUtil;
import com.rideaustin.utils.localization.AndroidLocalizer;

import java.util.Calendar;

/**
 * Created by RideClient on 8/16/16.
 */
public class FcraDisclosureFragment extends BaseDriverSignUpFragment implements FcraDisclosureView {

    private static final int DRIVER_LICENSE_LENGTH = 20;
    private static final int ADDRESS_MIN_LENGTH = 2;
    private static final int SOCIAL_SECURITY_NUMBER_MIN_LENGTH = 9;
    private static final char SOCIAL_SECURITY_NUMBER_PLACEHOLDER_CHAR = '-';
    private static final char SOCIAL_SECURITY_NUMBER_MASK_CHAR = 'X';
    private static final int SOCIAL_SECURITY_NUMBER_MASK_LENGTH = SOCIAL_SECURITY_NUMBER_MIN_LENGTH + 2; //XXX-XX-XXXX 9 numbers + 2 placeholders
    private static final int DRIVER_LICENSE_MIN_LENGTH = 7;
    private static final int STATE_CODE_LENGTH = 2;
    private static final int INITIAL_DRIVERS_YEARS = 21;

    private FcraDisclosureViewModel viewModel;

    private MenuItem nextMenuItem;
    private Calendar selectedCalendar;
    private FCRADisclosureBinding frcaDisclosureBinding;
    private ProfileValidator validator;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        selectedCalendar = Calendar.getInstance();
        selectedCalendar.set(Calendar.YEAR, selectedCalendar.get(Calendar.YEAR) - INITIAL_DRIVERS_YEARS);
        validator = new ProfileValidator(new AndroidLocalizer(getContext()));
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle
            savedInstanceState) {
        frcaDisclosureBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_fcra_disclosure, container, false);
        viewModel = new FcraDisclosureViewModel(this);
        setHasOptionsMenu(true);
        setHasHelpWidget(true);
        setToolbarTitle(R.string.title_fcra_disclosure);

        frcaDisclosureBinding.dlsInput.setFilters(new InputFilter[]{new InputFilter.AllCaps(), new InputFilter
                .LengthFilter(STATE_CODE_LENGTH)});

        frcaDisclosureBinding.dateOfBirthInput.setOnClickListener(v -> showDatePickerDialog());

        frcaDisclosureBinding.driverLicenseNumberInput.setFilters(new InputFilter[]{new InputFilter.AllCaps(), new InputFilter
                .LengthFilter(DRIVER_LICENSE_LENGTH)});

        frcaDisclosureBinding.dateOfBirthInput.setFocusable(true);
        frcaDisclosureBinding.dateOfBirthInput.setFocusableInTouchMode(true);
        frcaDisclosureBinding.dateOfBirthInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                showDatePickerDialog();
            } else {
                KeyboardUtil.hideKeyBoard(getContext(), v);
            }
        });

        frcaDisclosureBinding.lastNameInput.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                switch (keyCode) {
                    case KeyEvent.KEYCODE_DPAD_CENTER:
                    case KeyEvent.KEYCODE_ENTER:
                        showDatePickerDialog();
                        return true;
                    default:
                        break;
                }
            }
            return false;
        });

        ssnMaskCreator();

        frcaDisclosureBinding.middleNameConfirm.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                frcaDisclosureBinding.middleNameInput.setText("");
            }

            frcaDisclosureBinding.middleNameInput.setEnabled(!isChecked);
        });

        frcaDisclosureBinding.acknowledgeReceipt.setFocusable(true);
        frcaDisclosureBinding.acknowledgeReceipt.setFocusableInTouchMode(true);

        frcaDisclosureBinding.acknowledgeReceipt.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                KeyboardUtil.hideKeyBoard(getContext(), v);
            }
        });
        return frcaDisclosureBinding.getRoot();
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
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        KeyboardUtil.showKeyBoard(getContext(), frcaDisclosureBinding.firstNameInput);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        nextMenuItem = MenuUtil.inflateNextMenu(menu, inflater, true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.next:
                if (checkIsFieldCompleted()) {
                    setDriverInfo();
                    KeyboardUtil.hideKeyBoard(getContext(), frcaDisclosureBinding.getRoot());
                    notifyCompleted();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean checkIsFieldCompleted() {

        String firstNameError = validator.checkFirstName(frcaDisclosureBinding.firstNameInput.getText().toString().trim());

        if (!TextUtils.isEmpty(firstNameError)) {
            frcaDisclosureBinding.firstNameInput.requestFocus();
            frcaDisclosureBinding.firstNameInput.setError(firstNameError);
            return false;
        } else {
            frcaDisclosureBinding.firstNameInput.setError(null);
        }

        String middleNameError = validator.checkMiddleName(frcaDisclosureBinding.middleNameInput.getText().toString().trim());

        if (!frcaDisclosureBinding.middleNameConfirm.isChecked() && !TextUtils.isEmpty(middleNameError)) {
            frcaDisclosureBinding.middleNameInput.requestFocus();
            frcaDisclosureBinding.middleNameInput.setError(middleNameError);
            return false;
        } else {
            frcaDisclosureBinding.middleNameInput.setError(null);
        }

        String lastNameError = validator.checkLastName(frcaDisclosureBinding.lastNameInput.getText().toString().trim());

        if (!TextUtils.isEmpty(lastNameError)) {
            frcaDisclosureBinding.lastNameInput.requestFocus();
            frcaDisclosureBinding.lastNameInput.setError(lastNameError);
            return false;
        } else {
            frcaDisclosureBinding.lastNameInput.setError(null);
        }

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, -1 * INITIAL_DRIVERS_YEARS);

        if (TextUtils.isEmpty(frcaDisclosureBinding.dateOfBirthInput.getText()) || selectedCalendar.after(calendar)) {
            frcaDisclosureBinding.dateOfBirthInput.requestFocus();
            frcaDisclosureBinding.dateOfBirthInput.setError(getString(R.string.valid_birth_data));
            return false;
        } else {
            frcaDisclosureBinding.dateOfBirthInput.setError(null);
        }

        String ssnStrWithPlaceholders = getSsnWithoutPlacholders();
        if (ssnStrWithPlaceholders.length() < SOCIAL_SECURITY_NUMBER_MIN_LENGTH ||
                calculateSumOfNumbers(ssnStrWithPlaceholders) == 0) {
            frcaDisclosureBinding.socialSecurityNumber.requestFocus();
            frcaDisclosureBinding.socialSecurityNumber.setError(getString(R.string.social_security_number_requited));
            return false;
        } else {
            frcaDisclosureBinding.socialSecurityNumber.setError(null);
        }


        if (!frcaDisclosureBinding.currentZipcodeInput.getText().toString().matches("^\\d{5}(?:[-\\s]\\d{4})?$") ||
                calculateSumOfNumbers(frcaDisclosureBinding.currentZipcodeInput.getText().toString()) == 0) {
            frcaDisclosureBinding.currentZipcodeInput.requestFocus();
            frcaDisclosureBinding.currentZipcodeInput.setError(getString(R.string.valid_zip_code));
            return false;
        } else {
            frcaDisclosureBinding.currentZipcodeInput.setError(null);
        }

        if (frcaDisclosureBinding.driverLicenseNumberInput.length() < DRIVER_LICENSE_MIN_LENGTH) {
            frcaDisclosureBinding.driverLicenseNumberInput.requestFocus();
            frcaDisclosureBinding.driverLicenseNumberInput.setError(getString(R.string.driver_license_required));
            return false;
        } else {
            frcaDisclosureBinding.driverLicenseNumberInput.setError(null);
        }

        if (!frcaDisclosureBinding.dlsInput.getText().toString().matches("^" +
                "(?-i:A[LKSZRAEP]|C[AOT]|D[EC]|F[LM]|G[AU]|HI|I[ADLN]|K[SY]|LA|M[ADEHINOPST]|N[CDEHJMVY]|O[HKR]|P[ARW]|RI|S" +
                "[CD]|T[NX]|UT|V[AIT]|W[AIVY])$")) {
            frcaDisclosureBinding.dlsInput.requestFocus();
            frcaDisclosureBinding.dlsInput.setError(getString(R.string.valid_state_code));
            return false;
        } else {
            frcaDisclosureBinding.dlsInput.setError(null);
        }

        if (frcaDisclosureBinding.addressInput.getText().toString().trim().length() < ADDRESS_MIN_LENGTH) {
            frcaDisclosureBinding.addressInput.requestFocus();
            frcaDisclosureBinding.addressInput.setError(String.format(getString(R.string.address_must_be), ADDRESS_MIN_LENGTH));
            return false;
        } else {
            frcaDisclosureBinding.addressInput.setError(null);
        }

        if (!frcaDisclosureBinding.acknowledgeReceipt.isChecked()) {
            frcaDisclosureBinding.acknowledgeReceipt.requestFocus();
            frcaDisclosureBinding.acknowledgeReceipt.setError(getString(R.string.valid_acknowledge_summary));
            return false;
        } else {
            frcaDisclosureBinding.acknowledgeReceipt.setError(null);
        }

        return true;
    }

    private int calculateSumOfNumbers(String text) {
        int sum = 0;
        for (int i = 0; i < text.length(); i++) {
            try {
                sum += Integer.parseInt(String.valueOf(text.charAt(i)));
            } catch (NumberFormatException e) {
                // ignore
            }
        }
        return sum;
    }

    private String getSsnWithoutPlacholders() {
        String ssnStrWithPlaceholders = frcaDisclosureBinding.socialSecurityNumber.getText().toString();
        ssnStrWithPlaceholders = ssnStrWithPlaceholders.replace("" + SOCIAL_SECURITY_NUMBER_PLACEHOLDER_CHAR, "");
        return ssnStrWithPlaceholders;
    }

    private void showDatePickerDialog() {
        DialogFragment datePickerFragment = new DatePickerFragment(frcaDisclosureBinding, selectedCalendar);

        datePickerFragment.show(getChildFragmentManager(), DatePickerFragment.class.getName());
    }

    private void ssnMaskCreator() {
        frcaDisclosureBinding.socialSecurityNumber.setFilters(new InputFilter[]{new InputFilter.LengthFilter(SOCIAL_SECURITY_NUMBER_MASK_LENGTH)});
        frcaDisclosureBinding.socialSecurityNumber.setTransformationMethod(new PasswordMaskTransformationMethod(SOCIAL_SECURITY_NUMBER_MASK_CHAR, SOCIAL_SECURITY_NUMBER_PLACEHOLDER_CHAR));
        frcaDisclosureBinding.socialSecurityNumber.addTextChangedListener(new TextWatcher() {
            int lengthBefore = 0;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                lengthBefore = frcaDisclosureBinding.socialSecurityNumber.length();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (lengthBefore <= frcaDisclosureBinding.socialSecurityNumber.length()) {
                    if (frcaDisclosureBinding.socialSecurityNumber.length() == 3 || frcaDisclosureBinding.socialSecurityNumber.length() == 6) {
                        frcaDisclosureBinding.socialSecurityNumber.setText(String.format("%s%s", frcaDisclosureBinding.socialSecurityNumber.getText(), SOCIAL_SECURITY_NUMBER_PLACEHOLDER_CHAR));
                    }
                } else {
                    if (frcaDisclosureBinding.socialSecurityNumber.length() == 4 || frcaDisclosureBinding.socialSecurityNumber.length() == 7) {
                        frcaDisclosureBinding.socialSecurityNumber.setText(frcaDisclosureBinding.socialSecurityNumber.getText().subSequence(0, frcaDisclosureBinding.socialSecurityNumber.length() - 1));
                    }
                }
            }
        });
    }

    private void setDriverInfo() {
        DriverRegistration driverRegistration = getDriverData().getDriverRegistration();

        // We have unreproducable crashes related to Null Pointer, so at least lets avoid crash
        User user = App.getDataManager().getCurrentUser() != null ? App.getDataManager().getCurrentUser() : new User();

        driverRegistration.setEmail(user.getEmail());
        driverRegistration.getUser().setEnabled(true);
        driverRegistration.getUser().setPhoneNumber(user.getPhoneNumber());
        driverRegistration.getUser().setEmail(user.getEmail());
        driverRegistration.getUser().setFirstName(frcaDisclosureBinding.firstNameInput.getText().toString().trim());
        driverRegistration.getUser().setMiddleName(frcaDisclosureBinding.middleNameInput.getText().toString().trim());
        driverRegistration.getUser().setLastName(frcaDisclosureBinding.lastNameInput.getText().toString().trim());
        driverRegistration.getUser().setDateOfBirth(DateHelper.dateToServerDateFormat(selectedCalendar.getTime()));

        driverRegistration.setSsn(getSsnWithoutPlacholders());
        driverRegistration.getUser().getAddress().setZipCode(frcaDisclosureBinding.currentZipcodeInput.getText().toString());
        driverRegistration.getUser().getAddress().setAddress(frcaDisclosureBinding.addressInput.getText().toString());
        driverRegistration.setLicenseNumber(frcaDisclosureBinding.driverLicenseNumberInput.getText().toString());
        driverRegistration.setLicenseState(frcaDisclosureBinding.dlsInput.getText().toString());
        driverRegistration.setType(Avatar.DRIVER);
    }

    @Override
    public void onDisclosureAgreementUpdated(String disclosureAgreement) {
        frcaDisclosureBinding.fcraAgreementView.setText(disclosureAgreement);
    }

    @SuppressLint("ValidFragment")
    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        private Calendar selectedCalendar;
        private FCRADisclosureBinding frcaDisclosureBinding;

        public DatePickerFragment() {

        }

        public DatePickerFragment(FCRADisclosureBinding frcaDisclosureBinding, Calendar selectedCalendar) {
            this.frcaDisclosureBinding = frcaDisclosureBinding;
            this.selectedCalendar = selectedCalendar;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Calendar c = selectedCalendar;
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);
            DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), this, year, month, day);

            //Set max date
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.YEAR, -1 * INITIAL_DRIVERS_YEARS);
            datePickerDialog.getDatePicker().setMaxDate(calendar.getTimeInMillis());
            return datePickerDialog;
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            final Calendar c = Calendar.getInstance();
            c.clear();
            c.set(Calendar.YEAR, year);
            c.set(Calendar.MONTH, month);
            c.set(Calendar.DAY_OF_MONTH, day);
            selectedCalendar.setTimeInMillis(c.getTimeInMillis());
            frcaDisclosureBinding.dateOfBirthInput.setText(DateHelper.dateToSimpleDateFormat(selectedCalendar.getTime()));
        }
    }
}
