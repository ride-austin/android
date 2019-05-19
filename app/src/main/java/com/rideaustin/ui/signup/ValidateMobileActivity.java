package com.rideaustin.ui.signup;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.text.TextUtils;

import com.rideaustin.R;
import com.rideaustin.base.BaseActivity;
import com.rideaustin.base.Transition;
import com.rideaustin.databinding.ValidateMobileBinding;
import com.rideaustin.manager.sms.IncomingSmsReceiver;
import com.rideaustin.models.UserRegistrationData;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by kshumelchyk on 6/29/16.
 */
public class ValidateMobileActivity extends BaseActivity {

    private static final String KEY_PHONE_NUMBER = "KEY_PHONE_NUMBER";
    private static final Pattern VERIFICATION_PATTERN = Pattern.compile("^(Your verification code is )(\\d{4})$");

    private VerifyMobileFragment fragment;
    private ValidateMobileBinding binding;
    private String phoneNumber;
    private BroadcastReceiver smsReceiver;

    public static Intent createIntent(Context context, String phoneNumber) {
        return getStartIntent(context).putExtra(KEY_PHONE_NUMBER, phoneNumber);
    }

    private static Intent getStartIntent(Context context) {
        return new Intent(context, ValidateMobileActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            phoneNumber = extras.getString(KEY_PHONE_NUMBER, null);
        }

        binding = DataBindingUtil.setContentView(this, R.layout.activity_validate_mobile);
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());

        fragment = VerifyMobileFragment.getInstance(phoneNumber);
        fragment.setVerificationListener(new VerifyMobileFragment.VerificationListener() {
            @Override
            public void onNumberVerified(UserRegistrationData userdata) {
                setResult(RESULT_OK);
                finish();
            }

            @Override
            public void onNumberNotVerified() {
                setResult(RESULT_CANCELED);
                finish();
            }
        });
        replaceFragment(fragment, R.id.content_frame, false, Transition.NONE);
        registerSmsReceiver();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterSmsReceiver();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean shouldBeLoggedIn() {
        return false;
    }

    private void registerSmsReceiver() {
        smsReceiver = new IncomingSmsReceiver(this::doOnSmsMessage);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.provider.Telephony.SMS_RECEIVED");
        registerReceiver(smsReceiver, intentFilter);
    }

    private void unregisterSmsReceiver() {
        unregisterReceiver(smsReceiver);
    }

    private void doOnSmsMessage(SmsMessage message) {
        if (!TextUtils.isEmpty(message.getMessageBody())) {
            Matcher matcher = VERIFICATION_PATTERN.matcher(message.getMessageBody());
            if (matcher.matches() && matcher.groupCount() == 2 && fragment != null) {
                fragment.setPinText(matcher.group(2));
            }
        }
    }
}
