package com.rideaustin.ui.signin;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.rideaustin.App;
import com.rideaustin.R;
import com.rideaustin.base.ApiSubscriber;
import com.rideaustin.base.BaseApiException;
import com.rideaustin.base.BaseFragment;
import com.rideaustin.databinding.ResetPassBinding;
import com.rideaustin.schedulers.RxSchedulers;
import com.rideaustin.utils.Constants;
import com.rideaustin.utils.ValidationHelper;
import com.rideaustin.utils.toast.RAToast;

/**
 * Created by kshumelchyk on 7/12/16.
 */
public class ResetPasswordFragment extends BaseFragment {
    private ResetPassBinding binding;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_reset_password, container, false);

        final Bundle arguments = getArguments();
        if(arguments != null){
            final String email = arguments.getString(Constants.USER_EMAIL_KEY);
            if(!TextUtils.isEmpty(email)){
                binding.email.setText(email);
            }
        }

        binding.reset.setOnClickListener(v -> {
            if (TextUtils.isEmpty(binding.email.getText())) {
                binding.email.setError(getResources().getText(R.string.email_error));
                binding.email.requestFocus();
            } else if (!ValidationHelper.isValidEmail(binding.email.getText())) {
                binding.email.setError(getResources().getText(R.string.invalid_email_error));
                binding.email.requestFocus();
            } else
                resetPassword();
        });
        return binding.getRoot();
    }

    private void resetPassword() {
        App.getDataManager().getAuthService().forgotPassword(binding.email.getText().toString().trim())
                .subscribeOn(RxSchedulers.network())
                .observeOn(RxSchedulers.main())
                .subscribe(new ApiSubscriber<Void>(getCallback()) {
                    @Override
                    public void onNext(Void aVoid) {
                        super.onNext(aVoid);
                        RAToast.show(R.string.email_sent, Toast.LENGTH_LONG);
                    }

                    @Override
                    public void onError(BaseApiException e) {
                        super.onError(e);
                        RAToast.show(e.getBody(), Toast.LENGTH_LONG);
                    }
                });
    }
}
