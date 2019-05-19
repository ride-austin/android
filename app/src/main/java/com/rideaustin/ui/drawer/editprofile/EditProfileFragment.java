package com.rideaustin.ui.drawer.editprofile;

import android.app.Activity;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.design.widget.BottomSheetBehavior;
import android.telephony.PhoneNumberUtils;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.rideaustin.BuildConfig;
import com.rideaustin.R;
import com.rideaustin.api.config.GenderSelection;
import com.rideaustin.api.model.User;
import com.rideaustin.base.BaseFragment;
import com.rideaustin.databinding.FragmentEditProfileBinding;
import com.rideaustin.schedulers.RxSchedulers;
import com.rideaustin.ui.common.TakePhotoFragment;
import com.rideaustin.ui.drawer.NavigationDrawerActivity;
import com.rideaustin.ui.signup.ValidateMobileActivity;
import com.rideaustin.ui.utils.CommonMaterialDialogCreator;
import com.rideaustin.ui.utils.Fonts;
import com.rideaustin.ui.utils.ProfileValidator;
import com.rideaustin.ui.utils.phone.PhoneInputUtil;
import com.rideaustin.utils.Constants;
import com.rideaustin.utils.DialogUtils;
import com.rideaustin.utils.KeyboardUtil;
import com.rideaustin.utils.localization.AndroidLocalizer;

import timber.log.Timber;

public class EditProfileFragment extends BaseFragment<EditProfileFragmentViewModel> implements TakePhotoFragment.TakePhotoListener {
    private static final int REQUEST_VALIDATE_PHONE = 1001;

    private FragmentEditProfileBinding binding;
    private BottomSheetBehavior bottomSheetBehavior;
    private PhoneInputUtil phoneInputUtil;

    private ProfileValidator validator;
    private MaterialDialog dialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setViewModel(obtainViewModel(EditProfileFragmentViewModel.class));
        setToolbarTitleAligned(R.string.edit_account, Gravity.LEFT);
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_edit_profile, container, false);
        binding.setEditProfileFragmentViewModel(getViewModel());
        validator = new ProfileValidator(new AndroidLocalizer(getContext()));
        TextWatcher dirtyTextWatcher = new DirtyFieldWatcher(getViewModel());
        setDirtyTextWatcher(dirtyTextWatcher,
                binding.firstName,
                binding.lastName,
                binding.mobile,
                binding.nickname);

        View editPictureBottomView = binding.editPictureBottom;
        bottomSheetBehavior = BottomSheetBehavior.from(editPictureBottomView);
        bottomSheetBehavior.setHideable(true);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        binding.profile.setOnClickListener(view -> {
            KeyboardUtil.hideKeyBoard(getActivity(), binding.profile);
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        });
        binding.changeAvatar.setOnClickListener(view -> {
            KeyboardUtil.hideKeyBoard(getActivity(), binding.profile);
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        });

        binding.password.setOnClickListener(v -> startActivity(new Intent(getActivity(), UpdatePasswordActivity.class)));
        binding.gender.setOnClickListener(v -> getViewModel().onChooseGender());

        final TakePhotoFragment takePhotoFragment = TakePhotoFragment.newInstance();
        takePhotoFragment.setTakePhotoListener(this);
        replaceFragment(takePhotoFragment, R.id.edit_picture_bottom, false);

        phoneInputUtil = new PhoneInputUtil(getContext(), binding.country, binding.mobile);

        binding.emailAddress.setOnClickListener(v -> onEmailClick());

        binding.done.setOnClickListener(view -> {
            if (validateInputData()) {
                editUserOnServer();
            }
        });

        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        untilStop(getViewModel().getProfileUpdateEvents()
                .observeOn(RxSchedulers.main())
                .subscribe(isUpdated -> onEditComplete()));
        untilStop(getViewModel().getSelectedGenderEvents()
                .observeOn(RxSchedulers.main())
                .subscribe(this::onGenderSelection));
        untilStop(getViewModel().getAlreadyUsedMobileEvents()
                .observeOn(RxSchedulers.main())
                .subscribe(this::onPhoneInUseChecked));

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        DialogUtils.dismiss(dialog);
    }

    private boolean validateInputData() {
        String firstNameError = validator.checkFirstName(binding.firstName.getText().toString().trim());
        String lastNameError = validator.checkLastName(binding.lastName.getText().toString().trim());
        String mobile = phoneInputUtil.validate();
        String emailAddress = binding.emailAddress.getText().toString().trim();
        boolean isValid = true;

        if (!TextUtils.isEmpty(firstNameError)) {
            binding.firstName.setError(firstNameError);
            binding.firstName.requestFocus();
            isValid = false;
        } else if (!TextUtils.isEmpty(lastNameError)) {
            binding.lastName.setError(lastNameError);
            binding.lastName.requestFocus();
            isValid = false;
        } else if (TextUtils.isEmpty(mobile) || mobile.equals(PhoneInputUtil.PLUS_SIGN)) {
            binding.mobile.setError(getResources().getText(R.string.mobile_error));
            binding.mobile.requestFocus();
            isValid = false;
        } else if (!PhoneNumberUtils.isGlobalPhoneNumber(mobile)) {
            binding.mobile.setError(getResources().getText(R.string.invalid_mobile_error));
            binding.mobile.requestFocus();
            isValid = false;
        } else if (mobile.length() < Constants.PHONE_FIELD_LENGTH) {
            binding.mobile.setError(getResources().getText(R.string.phone_number_10_digits));
            binding.mobile.requestFocus();
            isValid = false;
        } else if (emailAddress.isEmpty()) {
            binding.emailAddress.setError(getResources().getText(R.string.email_error));
            binding.emailAddress.requestFocus();
            isValid = false;
        } else if (getViewModel().isPhoneNumberChanged(mobile)) {
            getViewModel().checkInUse(mobile);
            isValid = false;
        }
        return isValid;
    }

    private void hideBottomSheet() {
        int state = bottomSheetBehavior.isHideable()
                ? BottomSheetBehavior.STATE_HIDDEN
                : BottomSheetBehavior.STATE_COLLAPSED;
        bottomSheetBehavior.setState(state);
    }

    private void setDirtyTextWatcher(TextWatcher dirtyTextWatcher, EditText... forms) {
        for (EditText form : forms) {
            form.addTextChangedListener(dirtyTextWatcher);
        }
    }

    private void editUserOnServer() {
        User user = getViewModel().user.get();
        user.setFirstName(binding.firstName.getText().toString());
        user.setLastName(binding.lastName.getText().toString());
        user.setFullName(user.getFirstName() + " " + user.getLastName());
        user.setPhoneNumber(phoneInputUtil.validate());
        user.setEmail(binding.emailAddress.getText().toString());
        user.setNickName(binding.nickname.getText().toString());
        getViewModel().saveUser(user);
    }

    public void onPhoneInUseChecked(String mobile) {
        if (BuildConfig.FLAVOR.contains(Constants.ENV_PROD)) {
            editUserOnServer();
        } else {
            CommonMaterialDialogCreator.showSkipPhoneVerificationDialog(getActivity(),
                    () -> startActivityForResult(ValidateMobileActivity.createIntent(getActivity(), mobile), REQUEST_VALIDATE_PHONE),
                    () -> editUserOnServer());
        }
    }

    private void onGenderSelection(SelectedGender selectedGender) {
        DialogUtils.dismiss(dialog);
        dialog = new MaterialDialog.Builder(getActivity())
                .typeface(Fonts.MONTSERRAT_REGULAR_OTF, Fonts.MONTSERRAT_LIGHT_OTF)
                .title(selectedGender.getGender().getSubtitle())
                .items(selectedGender.getGender().getOptions())
                .alwaysCallSingleChoiceCallback()
                .itemsCallbackSingleChoice(selectedGender.getIndex(), (dialog, view, which, text) -> {
                    checkSelectionAllowed(selectedGender.getGender(), which);
                    return true;
                })
                .onPositive((dialog, which) -> selectGender(selectedGender.getGender()))
                .positiveText(R.string.btn_ok)
                .negativeText(R.string.btn_cancel)
                .cancelable(false)
                .show();
        checkSelectionAllowed(selectedGender.getGender(), selectedGender.getIndex());
    }

    private void selectGender(final GenderSelection selection) {
        getViewModel().selectGender(selection.getOptions().get(dialog.getSelectedIndex()));
    }

    private void checkSelectionAllowed(final GenderSelection selection, int position) {
        boolean enabled = position > -1 && position < selection.getOptions().size();
        dialog.getActionButton(DialogAction.POSITIVE).setEnabled(enabled);
    }

    private void onEmailClick() {
        if (!getViewModel().emailVerified.get()) {
            VerifyEmailDialog dialog = new VerifyEmailDialog();
            dialog.show(getChildFragmentManager(), VerifyEmailDialog.class.getName());
        }
    }

    public void onEditComplete() {
        if (getActivity() instanceof NavigationDrawerActivity) {
            getActivity().getSupportFragmentManager().popBackStackImmediate();
        } else if (getActivity() instanceof EditProfileActivity) {
            ((EditProfileActivity) getActivity()).close();
        } else {
            getActivity().onBackPressed();
        }
    }

    @Override
    public void onPhotoTaken(TakePhotoFragment.Source source, String filePath) {
        getViewModel().setDirtyFlag(true);
        getViewModel().sendImage(filePath);
        hideBottomSheet();
    }

    @Override
    public void onCanceled() {
        hideBottomSheet();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Timber.d("::onActivityResult::" + "requestCode = [" + requestCode + "], resultCode = [" + resultCode + "], data = [" + data + "]");
        switch (requestCode) {
            case REQUEST_VALIDATE_PHONE:
                if (resultCode == Activity.RESULT_OK) {
                    editUserOnServer();
                }
                break;
        }
    }

    private static class DirtyFieldWatcher implements TextWatcher {

        EditProfileFragmentViewModel viewModel;

        DirtyFieldWatcher(EditProfileFragmentViewModel viewModel) {
            this.viewModel = viewModel;
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            viewModel.setDirtyFlag(true);
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    }
}
