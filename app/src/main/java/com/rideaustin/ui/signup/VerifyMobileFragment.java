package com.rideaustin.ui.signup;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.dpizarro.pinview.library.PinView;
import com.rideaustin.App;
import com.rideaustin.R;
import com.rideaustin.api.model.auth.PhoneNumber;
import com.rideaustin.api.model.auth.PhoneNumberVerification;
import com.rideaustin.base.ApiSubscriber2;
import com.rideaustin.base.BaseActivityCallback;
import com.rideaustin.base.BaseApiException;
import com.rideaustin.base.BaseFragment;
import com.rideaustin.models.UserRegistrationData;
import com.rideaustin.schedulers.RxSchedulers;
import com.rideaustin.ui.utils.CommonMaterialDialogCreator;
import com.rideaustin.ui.utils.MaterialDialogCreator;
import com.rideaustin.utils.KeyboardUtil;
import com.rideaustin.utils.toast.RAToast;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.subscriptions.CompositeSubscription;
import rx.subscriptions.Subscriptions;
import timber.log.Timber;

import static com.rideaustin.utils.Constants.RESEND_SMS_CODE_DELAY_S;

/**
 * Created by kshumelchyk on 6/29/16.
 * <p>
 * This fragment doesn't use data binding because PinView component is crashing with it
 */
public class VerifyMobileFragment extends BaseFragment {

    private static final String KEY_PHONE_NUMBER = "KEY_PHONE_NUMBER";
    private PinView pin;
    private UserRegistrationData userData;
    private String token;
    private CompositeSubscription subscriptions = new CompositeSubscription();
    private String phoneNumber;
    private VerificationListener verificationListener;
    private Button resendCode;
    private Button next;
    private Subscription permissionSubscription = Subscriptions.empty();

    public static VerifyMobileFragment getInstance(String phoneNumber) {
        VerifyMobileFragment fragment = new VerifyMobileFragment();
        Bundle args = new Bundle();
        args.putString(KEY_PHONE_NUMBER, phoneNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public void setPinText(String pinText) {
        if (pinText.length() == 4) {
            pin.setPinText(pinText);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            phoneNumber = args.getString(KEY_PHONE_NUMBER);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        View view = inflater.inflate(R.layout.fragment_verify_mobile, container, false);
        pin = (PinView) view.findViewById(R.id.pin);
        TextView verifyText = (TextView) view.findViewById(R.id.verifyText);
        phoneNumber = getArguments().getString(KEY_PHONE_NUMBER);

        String message = verifyText.getText().toString() + " " + phoneNumber; //getE164Number(phoneNumber);
        verifyText.setText(message);

        next = (Button) view.findViewById(R.id.next);
        next.setOnClickListener(nextButton -> {
            String code = pin.getPinResults();
            if (!code.isEmpty()) {
                KeyboardUtil.hideKeyBoard(getActivity(), nextButton);
                verifyPhoneNumber(token, code);
            }

        });

        Button changeMobile = (Button) view.findViewById(R.id.changeMobile);
        changeMobile.setOnClickListener(changeMobileButton -> getActivity().onBackPressed());

        resendCode = (Button) view.findViewById(R.id.resend);
        resendCode.setOnClickListener(resendCodeButton -> {
            sendPhoneNumber(phoneNumber);
            RAToast.show(R.string.verification_sent, Toast.LENGTH_SHORT);
        });

        sendPhoneNumber(phoneNumber);

        return view;
    }

    private void sendPhoneNumber(final String phoneNumber) {
        resendCode.setEnabled(false);
        subscriptions.add(Observable.timer(RESEND_SMS_CODE_DELAY_S, TimeUnit.SECONDS, RxSchedulers.computation())
                .observeOn(RxSchedulers.main())
                .subscribe(timer -> {
                    Timber.d("::sendPhoneNumber:: Enabling resend button");
                    resendCode.setEnabled(true);
                }));
        subscriptions.add(App.getDataManager()
                .getAuthService()
                .sendPhoneNumber(phoneNumber)
                .subscribeOn(RxSchedulers.network())
                .observeOn(RxSchedulers.main())
                .subscribe(new ApiSubscriber2<PhoneNumber>((BaseActivityCallback) getActivity()) {
                    @Override
                    public void onNext(final PhoneNumber phoneNumber) {
                        super.onNext(phoneNumber);
                        token = phoneNumber.getToken();
                    }

                    @Override
                    public void onBadRequestError(BaseApiException e) {
                        super.onBadRequestError(e);
                        String message = e.getMessage();
                        if (message != null && message.contains("VoIP")) {
                            CommonMaterialDialogCreator.createNoVoIPDialog(message, getActivity(), (dialog, which) -> {
                                dialog.dismiss();
                                verificationListener.onNumberNotVerified();
                            }).show();
                        } else {
                            MaterialDialogCreator.createSimpleErrorDialog(getString(R.string.failed_sending_phone_for_verification), getActivity());
                        }
                    }
                }));
    }

    private void verifyPhoneNumber(final String token, final String code) {
        next.setEnabled(false);
        showProgress();
        subscriptions.add(App.getDataManager()
                .getAuthService()
                .verifyPhoneNumber(token, code)
                .subscribeOn(RxSchedulers.network())
                .observeOn(RxSchedulers.main())
                .subscribe(new ApiSubscriber2<PhoneNumberVerification>((BaseActivityCallback) getActivity()) {
                    @Override
                    public void onNext(final PhoneNumberVerification phoneNumberVerification) {
                        super.onNext(phoneNumberVerification);
                        verificationListener.onNumberVerified(userData);
                    }

                    @Override
                    public void onAnyError(final BaseApiException e) {
                        super.onAnyError(e);
                        hideProgress();
                        next.setEnabled(true);
                    }
                }));
    }

    @Override
    public void onPause() {
        super.onPause();
        KeyboardUtil.hideKeyBoard(getContext(), pin);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        subscriptions.clear();
        permissionSubscription.unsubscribe();
    }

    public void setVerificationListener(VerificationListener verificationListener) {
        this.verificationListener = verificationListener;
    }

    public interface VerificationListener {
        void onNumberVerified(UserRegistrationData userdata);

        void onNumberNotVerified();
    }
}