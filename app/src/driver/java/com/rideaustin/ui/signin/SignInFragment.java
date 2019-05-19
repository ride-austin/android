package com.rideaustin.ui.signin;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.rideaustin.App;
import com.rideaustin.R;
import com.rideaustin.base.ApiSubscriber;
import com.rideaustin.base.BaseApiException;
import com.rideaustin.base.BaseFragment;
import com.rideaustin.databinding.SignInBinding;
import com.rideaustin.schedulers.RxSchedulers;
import com.rideaustin.utils.Constants;
import com.rideaustin.utils.FacebookHelper;
import com.rideaustin.utils.NetworkHelper;
import com.rideaustin.utils.ValidationHelper;
import com.rideaustin.utils.toast.RAToast;

import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Set;

import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

/**
 * Created by kshumelchyk on 7/9/16.
 */
public class SignInFragment extends BaseFragment {
    private SignInBinding binding;
    private CallbackManager callbackManager;
    private String email;
    private String password;
    private String facebookEmail;
    private String facebookPass;

    private CompositeSubscription subscriptions = new CompositeSubscription();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        callbackManager = CallbackManager.Factory.create();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_sign_in, container, false);

        binding.done.setOnClickListener(v -> {
            email = binding.email.getText().toString().trim();
            password = binding.password.getText().toString().trim();

            if (email.isEmpty()) {
                binding.email.setError(getString(R.string.email_error));
                binding.email.requestFocus();
            } else if (!ValidationHelper.isValidEmail(email)) {
                binding.email.setError(getString(R.string.invalid_email_error));
                binding.email.requestFocus();
            } else if (password.isEmpty()) {
                binding.password.setError(getString(R.string.no_password_error));
                binding.password.requestFocus();
            } else if (password.length() < Constants.MIN_PASSWORD_LENGTH) {
                binding.password.setError(getString(R.string.password_error));
                binding.password.requestFocus();
            } else
                doLogin();
        });

        binding.forgotPassword.setOnClickListener(v -> startActivity(new Intent(getActivity(), ForgotPasswordActivity.class)));

        binding.facebook.setOnClickListener(view -> doFacebookLogin());

        return binding.getRoot();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // RA-8910: probably caused by fragment recreation
        // moved initialization of CallbackManager to onCreate() instead of onCreateView()
        // and perform this check this as well
        if (callbackManager != null) {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        subscriptions.clear();
        if (callbackManager != null) {
            LoginManager.getInstance().unregisterCallback(callbackManager);
        }
    }

    private void doLogin() {
        subscriptions.add(App.getDataManager().loginEmail(email, password)
                .observeOn(RxSchedulers.main())
                .subscribe(new SigninApiSubscriber(getActivity(), true)));
    }


    // TODO extract facebook related part in separate manager
    private void doFacebookLogin() {
        ArrayList<String> permissions = new ArrayList<>();
        permissions.add(Constants.FacebookFields.PUBLIC_PROFILE);
        permissions.add(Constants.FacebookFields.EMAIL);

        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {

            @Override
            public void onSuccess(LoginResult loginResult) {
                showProgress();
                facebookPass = loginResult.getAccessToken().getToken();

                Set<String> deniedPermissions = loginResult.getRecentlyDeniedPermissions();
                if (deniedPermissions.contains(Constants.FacebookFields.EMAIL)) {
                    hideProgress();
                    RAToast.show(R.string.fb_email_error, Toast.LENGTH_SHORT);
                } else {
                    GraphRequest request = FacebookHelper.getMeRequest(loginResult.getAccessToken(), (object, response) -> {
                        facebookEmail = getFacebookEmail(object);
                        if (!TextUtils.isEmpty(facebookEmail)) {
                            registerFacebookUser();
                        } else {
                            hideProgress();
                            RAToast.show(App.getInstance().getString(R.string.fb_login_error, App.getFormattedAppName()), Toast.LENGTH_LONG);
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
                else
                    RAToast.show(e.getLocalizedMessage(), Toast.LENGTH_SHORT);
            }
        });

        FacebookHelper.refreshTokenAndLogin(this, permissions);
    }

    // Login Facebook user on server using Facebook token
    private void registerFacebookUser() {
        if (!isAttached()) {
            return;
        }
        subscriptions.add(App.getDataManager().loginFacebook(facebookPass)
                .observeOn(RxSchedulers.main())
                .subscribe(new ApiSubscriber<Integer>(getCallback()) {
                    @Override
                    public void onNext(Integer respCode) {
                        super.onNext(respCode);
                        switch (respCode) {
                            case HttpURLConnection.HTTP_OK:
                                subscriptions.add(App.getDataManager().loginEmail(facebookEmail, facebookPass)
                                        .observeOn(RxSchedulers.main())
                                        .doOnError(throwable -> hideProgress())
                                        .subscribe(new SigninApiSubscriber(getActivity(), true)));
                                break;
                            case HttpURLConnection.HTTP_ACCEPTED:
                                hideProgress();
                                RAToast.show(R.string.facebook_error, Toast.LENGTH_LONG);
                                break;
                            default:
                                hideProgress();
                                RAToast.show(R.string.network_error, Toast.LENGTH_LONG);
                        }
                    }

                    @Override
                    public void onError(BaseApiException e) {
                        super.onError(e);
                        hideProgress();
                        RAToast.show(App.getInstance().getString(R.string.fb_login_error, App.getFormattedAppName()), Toast.LENGTH_LONG);
                    }

                    @Override
                    public void onCompleted() {
                        // Do not hide progress here.
                        // Show progress while logging in with facebook credentials
                        // or progress already hidden in onError.
                    }
                }));
    }

    private String getFacebookEmail(JSONObject object) {
        try {
            return object.getString(Constants.FacebookFields.EMAIL);
        } catch (Exception e) {
            Timber.e(e, "Unable to get email from Facebook");
            return null;
        }
    }
}
