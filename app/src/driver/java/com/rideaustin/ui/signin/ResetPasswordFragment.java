package com.rideaustin.ui.signin;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
import com.rideaustin.utils.ValidationHelper;
import com.rideaustin.utils.toast.RAToast;

/**
 * Created by kshumelchyk on 7/12/16.
 */
public class ResetPasswordFragment extends BaseFragment {
    private ResetPassBinding binding;
    private String email;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_reset_password, container, false);

        binding.reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                email = binding.email.getText().toString();
                if (email.isEmpty()) {
                    binding.email.setError(getResources().getText(R.string.email_error));
                    binding.email.requestFocus();
                } else if (!ValidationHelper.isValidEmail(email)) {
                    binding.email.setError(getResources().getText(R.string.invalid_email_error));
                    binding.email.requestFocus();
                } else
                    resetPassword();
            }
        });
        return binding.getRoot();
    }

    private void resetPassword() {
        App.getDataManager().getAuthService()
                .forgotPassword(email)
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
                        if (e.getCode() == 0)
                            RAToast.show(e.getMessage(), Toast.LENGTH_LONG);
                    }
                });
    }
}
