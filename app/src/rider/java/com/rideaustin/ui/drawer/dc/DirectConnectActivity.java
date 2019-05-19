package com.rideaustin.ui.drawer.dc;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.rideaustin.App;
import com.rideaustin.R;
import com.rideaustin.api.model.Payment;
import com.rideaustin.base.Transition;
import com.rideaustin.databinding.ActivityDirectConnectBinding;
import com.rideaustin.schedulers.RxSchedulers;
import com.rideaustin.ui.BackPressable;
import com.rideaustin.ui.base.BaseRiderActivity;
import com.rideaustin.ui.payment.AddPaymentFragment;
import com.rideaustin.ui.payment.EditPaymentFragment;
import com.rideaustin.ui.payment.PaymentFragment;

/**
 * Created by hatak on 03.11.2017.
 */

public class DirectConnectActivity extends BaseRiderActivity<DirectConnectViewModel> implements PaymentFragment.EventListener, AddPaymentFragment.EventListener {

    ActivityDirectConnectBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (!App.getDataManager().isLoggedIn()) {
            super.onCreate(null);
            return;
        }

        super.onCreate(savedInstanceState);
        setViewModel(obtainViewModel(DirectConnectViewModel.class));
        binding = DataBindingUtil.setContentView(this, R.layout.activity_direct_connect);
        setToolbar(binding.toolbar);
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
        untilDestroy(getViewModel().getNavigateEvents()
                .observeOn(RxSchedulers.main())
                .subscribe(this::onNavigate));
    }

    @Override
    public void onBackPressed() {
        Fragment f = getSupportFragmentManager().findFragmentById(R.id.content_frame);
        if (f instanceof BackPressable) {
            BackPressable backPressableFragment = (BackPressable) f;
            boolean isConsumed = backPressableFragment.onBackPressed();
            if (!isConsumed) {
                super.onBackPressed();
            }
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onAddPayment() {
        getViewModel().navigateTo(DirectConnectViewModel.Step.ADD_PAYMENT);
    }

    @Override
    public void onEditPayment(Payment payment) {
        EditPaymentFragment fragment = new EditPaymentFragment();
        fragment.setPayment(payment);
        replaceFragment(fragment, R.id.content_frame, true);
    }

    @Override
    public void onAddPaymentAdded(Payment payment) {
        onBackPressed();
    }

    private void onNavigate(DirectConnectViewModel.Step step) {
        switch (step) {
            case ENTER_DRIVER_ID:
                replaceFragment(new SelectDriverFragment(), R.id.content_frame, false, Transition.NONE);
                break;
            case DRIVER_SUMMARY:
                replaceFragment(new ConnectDriverFragment(), R.id.content_frame, true);
                break;
            case CATEGORY_PICKER:
                replaceFragment(new CarCategoryPickerFragment(), R.id.content_frame, true);
                break;
            case PAYMENT_PICKER:
                PaymentFragment paymentFragment = new PaymentFragment();
                paymentFragment.setEventListener(this);
                replaceFragment(paymentFragment, R.id.content_frame, true);
                break;
            case ADD_PAYMENT:
                AddPaymentFragment addPaymentFragment = new AddPaymentFragment();
                addPaymentFragment.setEventListener(this);
                replaceFragment(addPaymentFragment, R.id.content_frame, true);
                break;

        }
    }
}
