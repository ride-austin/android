package com.rideaustin.ui.signup;

import android.app.Activity;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.MaterialDialog;
import com.rideaustin.App;
import com.rideaustin.R;
import com.rideaustin.api.model.UserExistsResponse;
import com.rideaustin.base.ApiSubscriber;
import com.rideaustin.base.BaseActivity;
import com.rideaustin.base.BaseActivityCallback;
import com.rideaustin.base.BaseFragment;
import com.rideaustin.databinding.EnterMobileBinding;
import com.rideaustin.models.UserRegistrationData;
import com.rideaustin.ui.signin.SignInActivity;
import com.rideaustin.ui.signin.SplashActivity;
import com.rideaustin.ui.utils.CommonMaterialDialogCreator;
import com.rideaustin.ui.utils.MaterialDialogCreator;
import com.rideaustin.ui.utils.phone.PhoneInputUtil;
import com.rideaustin.utils.Constants;

import rx.Subscription;
import rx.subscriptions.Subscriptions;
import timber.log.Timber;

/**
 * Created by kshumelchyk on 6/24/16.
 */
public class EnterMobileFragment extends BaseFragment {

    private static final int REQUEST_VALIDATE_PHONE = 1001;

    private EnterMobileBinding binding;
    private PhoneInputUtil phoneInputUtil;
    private Subscription faceBookSingUpSubscription = Subscriptions.empty();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_enter_mobile, container, false);
        phoneInputUtil = new PhoneInputUtil(getContext(), binding.country, binding.mobile);

        binding.mobile.setOnFocusChangeListener((v, hasFocus) -> {
            binding.mobile.setCompoundDrawablesWithIntrinsicBounds(0, 0, hasFocus ? 0 : R.drawable.ico_flag, 0);
        });
        binding.next.setOnClickListener(view -> {
            String mobile = phoneInputUtil.validate();
            if (TextUtils.isEmpty(mobile)) {
                binding.mobile.setError(getResources().getText(R.string.mobile_error));
                binding.mobile.requestFocus();
            } else if (!PhoneNumberUtils.isGlobalPhoneNumber(mobile)) {
                binding.mobile.setError(getResources().getText(R.string.invalid_mobile_error));
                binding.mobile.requestFocus();
            } else if (mobile.length() < Constants.PHONE_FIELD_LENGTH) {
                binding.mobile.setError(getResources().getText(R.string.phone_number_10_digits));
                binding.mobile.requestFocus();
            } else {
                UserRegistrationData userData = App.getDataManager().getUserRegistrationData() != null
                        ? App.getDataManager().getUserRegistrationData()
                        : new UserRegistrationData();
                userData.setPhoneNumber(mobile);
                App.getDataManager().setUserRegistrationData(userData);
                validateUserExists();
            }
        });
        return binding.getRoot();
    }

    private void validateUserExists() {
        final UserRegistrationData registrationData = App.getDataManager().getUserRegistrationData();
        App.getDataManager().checkIsUserExisted(registrationData.getEmail(), registrationData.getPhoneNumber())
                .subscribe(new ValidateUserSubscriber(getActivity()));
    }

    private void showPhoneNumberAlreadyInUseDialog(final String errorMessage) {
        MaterialDialog.Builder dialogBuilder = MaterialDialogCreator.createUserExistsDialog(errorMessage, (AppCompatActivity) getActivity());
        dialogBuilder.onPositive((dialog, which) -> {
            // Intent for removing all activities from the Task except SplashActivity
            Intent splashIntent = new Intent(getActivity(), SplashActivity.class);
            splashIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(splashIntent);
            // Intent for starting SignInActivity on the top
            Intent signInIntent = new Intent(getActivity(), SignInActivity.class);
            // Start a set of activities as a synthesized task stack
            getActivity().startActivities(new Intent[]{splashIntent, signInIntent});
        });
        dialogBuilder.onNegative((dialog, which) -> {
            if (errorMessage.contains("email")) {
                // NOTE: This should never happen as server already checked provided email, but still..
                // Something wrong with email Facebook provided
                // probably it have been already registered.
                // So, user tapped "Edit" - just go back to CreateAccountActivity
                getActivity().onBackPressed();
            } else if (errorMessage.contains("phone")) {
                phoneInputUtil.resetCountryCode();
                binding.mobile.setSelection(binding.mobile.length());
                binding.mobile.requestFocus();
            }
        });
        dialogBuilder.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_VALIDATE_PHONE) {
            if (resultCode == Activity.RESULT_OK) {
                if (App.getDataManager().getUserRegistrationData().isFacebookAuth()) {
                    faceBookSingUpSubscription =
                            FacebookSignupHelper
                                    .doFacebookSignUp((BaseActivity) getActivity(), App.getDataManager().getUserRegistrationData());
                } else {
                    startActivity(new Intent(getActivity(), CreateProfileActivity.class));
                }
            }
        }
    }

    /**
     * Currently these are the messages coming from server.
     * <p>
     * 'These username and phone number are already in use'
     * 'This email address is already in use'
     * 'This phone number address is already in use'
     * 'VoIP numbers are not allowed!'
     */
    private class ValidateUserSubscriber extends ApiSubscriber<UserExistsResponse> {

        public ValidateUserSubscriber(Activity activity) {
            super((BaseActivityCallback) activity);
        }

        @Override
        public void onNext(UserExistsResponse existsResponse) {
            Timber.d("validateUserExists: user is %s", existsResponse);
            if (existsResponse.isUserValid()) {
                startActivityForResult(ValidateMobileActivity.createIntent(getActivity(), App.getDataManager().getUserRegistrationData().getPhoneNumber()), REQUEST_VALIDATE_PHONE);
            } else {
                String errorMessage = existsResponse.getErrorDescription();
                if (errorMessage.contains("VoIP")) {
                    showNoVoIPDialog(errorMessage);
                } else {
                    errorMessage = errorMessage.replace("username", "email");
                    showPhoneNumberAlreadyInUseDialog(errorMessage);
                }
            }
        }
    }

    private void showNoVoIPDialog(String errorMessage) {
        CommonMaterialDialogCreator.createNoVoIPDialog(errorMessage, getActivity(), (dialog, which) -> {
            phoneInputUtil.resetCountryCode();
            binding.mobile.setSelection(binding.mobile.length());
            binding.mobile.requestFocus();
        }).show();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        faceBookSingUpSubscription.unsubscribe();
    }
}
