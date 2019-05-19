package com.rideaustin.ui.signup;

import android.app.Activity;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.rideaustin.App;
import com.rideaustin.BuildConfig;
import com.rideaustin.R;
import com.rideaustin.api.model.User;
import com.rideaustin.api.model.UserExistsResponse;
import com.rideaustin.base.ApiSubscriber;
import com.rideaustin.base.BaseActivity;
import com.rideaustin.base.BaseActivityCallback;
import com.rideaustin.base.BaseApiException;
import com.rideaustin.base.BaseFragment;
import com.rideaustin.databinding.AccountBinding;
import com.rideaustin.models.UserRegistrationData;
import com.rideaustin.schedulers.RxSchedulers;
import com.rideaustin.ui.drawer.NavigationDrawerActivity;
import com.rideaustin.ui.drawer.editprofile.EditProfileActivity;
import com.rideaustin.ui.signin.SignInActivity;
import com.rideaustin.ui.utils.CommonMaterialDialogCreator;
import com.rideaustin.ui.utils.MaterialDialogCreator;
import com.rideaustin.ui.utils.ProfileValidator;
import com.rideaustin.ui.utils.phone.PhoneInputUtil;
import com.rideaustin.utils.CommonConstants;
import com.rideaustin.utils.Constants;
import com.rideaustin.utils.FacebookHelper;
import com.rideaustin.utils.NetworkHelper;
import com.rideaustin.utils.PasswordTextWatcher;
import com.rideaustin.utils.ValidationHelper;
import com.rideaustin.utils.toast.RAToast;

import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Set;

import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

/**
 * Created by kshumelchyk on 6/23/16.
 */
public class CreateAccountFragment extends BaseFragment {

    private static final int REQUEST_VALIDATE_PHONE = 1001;

    private AccountBinding binding;
    private UserRegistrationData facebookUser;
    private CallbackManager callbackManager;
    private PhoneInputUtil phoneInputUtil;
    private CompositeSubscription subscriptions = new CompositeSubscription();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_create_account, container, false);

        binding.facebook.setCompoundDrawablesWithIntrinsicBounds(R.drawable.facebook, 0, 0, 0);
        callbackManager = CallbackManager.Factory.create();

        binding.password.addTextChangedListener(new PasswordTextWatcher(binding.password, getActivity()));

        binding.next.setOnClickListener(view -> {
            String email = binding.email.getText().toString().trim();
            String mobile = phoneInputUtil.validate();
            String password = binding.password.getText().toString().trim();

            if (email.isEmpty()) {
                binding.email.setError(getResources().getText(R.string.email_error));
                binding.email.requestFocus();
            } else if (!ValidationHelper.isValidEmail(email)) {
                binding.email.setError(getResources().getText(R.string.invalid_email_error));
                binding.email.requestFocus();
            } else if (TextUtils.isEmpty(mobile) || mobile.equals(PhoneInputUtil.PLUS_SIGN)) {
                binding.mobile.setError(getResources().getText(R.string.mobile_error));
                binding.mobile.requestFocus();
            } else if (!PhoneNumberUtils.isGlobalPhoneNumber(mobile)) {
                binding.mobile.setError(getResources().getText(R.string.invalid_mobile_error));
                binding.mobile.requestFocus();
            } else if (mobile.length() < Constants.PHONE_FIELD_LENGTH) {
                binding.mobile.setError(getResources().getText(R.string.phone_number_10_digits));
                binding.mobile.requestFocus();
            } else if (password.isEmpty()) {
                binding.password.setError(getResources().getText(R.string.no_password_error));
                binding.password.requestFocus();
            } else if (password.length() < Constants.MIN_PASSWORD_LENGTH) {
                binding.password.setError(getResources().getText(R.string.password_error));
                binding.password.requestFocus();
            } else {
                UserRegistrationData userData = new UserRegistrationData();
                userData.setEmail(email);
                userData.setPhoneNumber(mobile);
                userData.setPassword(password);
                App.getDataManager().setUserRegistrationData(userData);

                showConfirmationDialog();
            }
        });

        phoneInputUtil = new PhoneInputUtil(getContext(), binding.country, binding.mobile);

        binding.facebook.setOnClickListener(view -> doFacebookLogin());
        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        showExtraMessage();
    }

    private void showExtraMessage() {
        if (getArguments() != null && getArguments().containsKey(CreateAccountActivity.EXTRA_MESSAGE_KEY)) {
            String message = getArguments().getString(CreateAccountActivity.EXTRA_MESSAGE_KEY);
            getArguments().remove(CreateAccountActivity.EXTRA_MESSAGE_KEY);
            MaterialDialogCreator.createSimpleErrorDialog(message, getActivity());
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_VALIDATE_PHONE:
                if (resultCode == Activity.RESULT_OK) {
                    if (App.getDataManager().getUserRegistrationData().isFacebookAuth()) {
                        subscriptions.add(FacebookSignupHelper.doFacebookSignUp((BaseActivity) getActivity(),
                                App.getDataManager().getUserRegistrationData()));
                    } else {
                        startActivity(new Intent(getActivity(), CreateProfileActivity.class));
                    }
                } else {
                    onBackPressed();
                }
                break;
            default:
                callbackManager.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    private void doFacebookLogin() {
        final ArrayList<String> permissions = new ArrayList<>();
        permissions.add(Constants.FacebookFields.PUBLIC_PROFILE);
        permissions.add(Constants.FacebookFields.EMAIL);

        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {

            @Override
            public void onSuccess(LoginResult loginResult) {
                showProgress();
                Set<String> deniedPermissions = loginResult.getRecentlyDeniedPermissions();
                if (deniedPermissions.contains(Constants.FacebookFields.EMAIL)) {
                    hideProgress();
                    RAToast.show(R.string.fb_email_error, Toast.LENGTH_SHORT);
                } else {
                    GraphRequest request = FacebookHelper.getMeRequest(loginResult.getAccessToken(), (object, response) -> {
                        facebookUser = getFacebookRegistrationData(loginResult, object);
                        if (facebookUser != null && !TextUtils.isEmpty(facebookUser.getEmail())) {
                            App.getDataManager().setUserRegistrationData(facebookUser);
                            registerFacebookUser();
                        } else {
                            hideProgress();
                            RAToast.show(getContext().getString(R.string.fb_login_error, App.getFormattedAppName()), Toast.LENGTH_LONG);
                        }
                    });

                    Bundle parameters = new Bundle();
                    parameters.putString(Constants.FacebookFields.FIELDS_KEY, Constants.FacebookFields.FIELDS_VALUE);
                    request.setParameters(parameters);
                    request.executeAsync();
                }
            }

            @Override
            public void onCancel() {
                hideProgress();
            }

            @Override
            public void onError(FacebookException e) {
                hideProgress();
                if (!NetworkHelper.isNetworkAvailable())
                    RAToast.show(R.string.network_error, Toast.LENGTH_SHORT);
                else {
                    RAToast.show(e.getLocalizedMessage(), Toast.LENGTH_SHORT);
                }
            }
        });

        FacebookHelper.refreshTokenAndLogin(this, permissions);
    }

    // Register Facebook user on server using Facebook token
    private void registerFacebookUser() {
        if (!isAttached()) {
            return;
        }
        subscriptions.add(App.getDataManager().loginFacebook(facebookUser.getPassword()).observeOn(RxSchedulers.main()).subscribe(new ApiSubscriber<Integer>(getCallback()) {
            @Override
            public void onNext(Integer respCode) {
                super.onNext(respCode);
                facebookUser.setFacebookAuth(true);
                App.getDataManager().setUserRegistrationData(facebookUser);
                switch (respCode) {
                    case HttpURLConnection.HTTP_OK:
                        Timber.d("Facebook user already exist in database");
                        subscriptions.add(App.getDataManager().loginEmail(facebookUser.getEmail(), facebookUser.getPassword()).observeOn(RxSchedulers.main()).subscribe(new ApiSubscriber<User>(getCallback()) {
                            @Override
                            public void onNext(User user) {
                                super.onNext(user);
                                boolean isUserValid = ProfileValidator.validateUser(user, App.getInstance());
                                if (isUserValid) {
                                    startActivity(new Intent(getActivity(), NavigationDrawerActivity.class));
                                } else {
                                    startActivity(new Intent(getActivity(), EditProfileActivity.class));
                                }
                            }

                            @Override
                            public void onCompleted() {
                                super.onCompleted();
                                getActivity().finishAffinity();
                            }

                            @Override
                            public void onError(BaseApiException e) {
                                super.onError(e);
                                hideProgress();
                                App.getDataManager().clearAuth();
                            }
                        }));
                        break;
                    case HttpURLConnection.HTTP_ACCEPTED:
                    case HttpURLConnection.HTTP_CREATED: // on swagger we have different versions, need for testing RA-1630
                        Timber.d("New Facebook user created");
                        startActivity(new Intent(getActivity(), FacebookLoginActivity.class));
                        break;
                    default:
                        RAToast.show(R.string.network_error, Toast.LENGTH_SHORT);
                        hideProgress();
                }
            }

            @Override
            public void onError(BaseApiException e) {
                super.onError(e);
                RAToast.show(getContext().getString(R.string.fb_login_error, App.getFormattedAppName()), Toast.LENGTH_LONG);
                hideProgress();
            }

            @Override
            public void onCompleted() {
                // Do not hide progress here.
                // Show progress while logging in with facebook credentials
                // or progress already hidden in onError.
            }
        }));
    }

    /**
     * Creates registration data object from data provided by Facebook
     * May return <code>null</code> if data is not valid
     *
     * @param loginResult login result from Facebook
     * @param object      user data provided by Facebook
     * @return registration data or null
     */
    @Nullable
    private UserRegistrationData getFacebookRegistrationData(LoginResult loginResult, JSONObject object) {
        try {
            UserRegistrationData facebookUser = new UserRegistrationData();
            facebookUser.setPassword(loginResult.getAccessToken().getToken());
            if (object.has(Constants.FacebookFields.ID))
                facebookUser.setSocialId(object.getString(Constants.FacebookFields.ID));
            if (object.has(Constants.FacebookFields.EMAIL))
                facebookUser.setEmail(object.getString(Constants.FacebookFields.EMAIL));
            if (object.has(Constants.FacebookFields.NAME)) {
                facebookUser.setFirstName(object.getString(Constants.FacebookFields.NAME));
                // for some users FB doesn't return first and last name, so we need this hack in order to complete registration
                facebookUser.setLastName(CommonConstants.FACEBOOK_EMPTY_LAST_NAME);
            }
            if (object.has(Constants.FacebookFields.FIRST_NAME) && object.has(Constants.FacebookFields.LAST_NAME)) {
                facebookUser.setFirstName(object.getString(Constants.FacebookFields.FIRST_NAME));
                facebookUser.setLastName(object.getString(Constants.FacebookFields.LAST_NAME));
            }
            return facebookUser;
        } catch (Exception e) {
            Timber.e(e, "Unable to create Facebook user");
            return null;
        }
    }

    private void showConfirmationDialog() {
        MaterialDialog.Builder confirmation = MaterialDialogCreator.createConfirmDialog(getString(R.string.confirm_dialog), binding.email.getText().toString(), getActivity());
        confirmation.onPositive((dialog, which) -> validateUserExists())
                .onNegative((dialog, which) -> binding.email.requestFocus()).build();
        confirmation.show();
    }

    private void validateUserExists() {
        final UserRegistrationData registrationData = App.getDataManager().getUserRegistrationData();
        subscriptions.add(App.getDataManager().checkIsUserExisted(
                registrationData.getEmail(),
                registrationData.getPhoneNumber()
        ).subscribe(new ValidateUserSubscriber(getActivity())));
    }

    private void showPhoneNumberAlreadyInUseDialog(final String errorMessage) {
        MaterialDialog.Builder dialogBuilder = MaterialDialogCreator.createUserExistsDialog(
                errorMessage,
                (AppCompatActivity) getActivity()
        );

        dialogBuilder.onPositive((dialog, which) -> {
            startActivity(new Intent(getActivity(), SignInActivity.class));
            getActivity().finish();
        });

        dialogBuilder.onNegative((dialog, which) -> {
            if (errorMessage.contains("email")) {
                binding.email.setText("");
                binding.email.requestFocus();
            } else if (errorMessage.contains("phone")) {
                phoneInputUtil.resetCountryCode();
                binding.mobile.setSelection(binding.mobile.length());
                binding.mobile.requestFocus();
            }
        });

        dialogBuilder.show();
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
            if (BuildConfig.FLAVOR.contains(Constants.ENV_PROD)) {
                showVerificationResult(existsResponse);
            } else {
                String errorMessage = existsResponse.getErrorDescription();
                if (errorMessage != null && (errorMessage.contains("email") || errorMessage.contains("username") || errorMessage.contains("VoIP"))) {
                    showVerificationResult(existsResponse);
                } else {
                    CommonMaterialDialogCreator.showSkipPhoneVerificationDialog(getActivity(),
                            () -> showVerificationResult(existsResponse),
                            () -> startActivity(new Intent(getContext(), CreateProfileActivity.class)));
                }
            }
        }
    }

    private void showVerificationResult(UserExistsResponse existsResponse) {
        if (existsResponse.isUserValid()) {
            String phoneNumber = App.getDataManager().getUserRegistrationData().getPhoneNumber();
            startActivityForResult(ValidateMobileActivity.createIntent(getActivity(), phoneNumber), REQUEST_VALIDATE_PHONE);
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
        subscriptions.clear();
        if (callbackManager != null) {
            LoginManager.getInstance().unregisterCallback(callbackManager);
        }
    }
}
