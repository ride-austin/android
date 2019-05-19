package com.rideaustin.ui.payment;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rideaustin.App;
import com.rideaustin.R;
import com.rideaustin.api.config.UnpaidConfig;
import com.rideaustin.api.model.UnpaidBalance;
import com.rideaustin.base.ApiSubscriber2;
import com.rideaustin.base.BaseFragment;
import com.rideaustin.databinding.FragmentUnpaidBinding;
import com.rideaustin.schedulers.RxSchedulers;
import com.rideaustin.ui.utils.CommonMaterialDialogCreator;
import com.rideaustin.utils.UnpaidHelper;

import java8.util.Optional;
import rx.Subscription;
import rx.subscriptions.Subscriptions;
import timber.log.Timber;

/**
 * Created by Sergey Petrov on 22/08/2017.
 */

public class UnpaidFragment extends BaseFragment {

    private FragmentUnpaidBinding binding;
    private Subscription unpaidSubscription = Subscriptions.empty();
    private Subscription paymentSubscription = Subscriptions.empty();

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Optional<UnpaidConfig> config = Optional.ofNullable(App.getConfigurationManager().getLastConfiguration().getUnpaidConfig());
        setToolbarTitleAligned(config.map(UnpaidConfig::getTitle).orElse(getString(R.string.unpaid_title)), Gravity.CENTER);
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_unpaid, container, false);
        binding.unpaidSubTitle.setText(config.map(UnpaidConfig::getSubTitle).orElse(getString(R.string.unpaid_subtitle)));
        App.getDataManager().getLocalSelectedPayment().ifPresent(payment -> {
            PaymentType paymentType = PaymentType.parse(payment.getCardBrand());
            binding.iconPaymentType.setImageResource(paymentType.getIconResId());
            binding.textPaymentCard.setText(paymentType == PaymentType.BEVO_BUCKS
                    ? payment.getCardNumber()
                    : getString(R.string.payment_card_template, payment.getCardNumber()));
        });
        binding.btnSubmit.setOnClickListener(v -> submitPayment());
        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        subscribeToUnpaid();
    }

    @Override
    public void onStop() {
        super.onStop();
        unpaidSubscription.unsubscribe();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        paymentSubscription.unsubscribe();
    }

    private void subscribeToUnpaid() {
        if (isUnpaidEnabled()) {
            unpaidSubscription = App.getDataManager()
                    .getUnpaidBalanceObservable()
                    .observeOn(RxSchedulers.main())
                    .subscribe(this::updateUnpaid, Timber::e);
        } else {
            goBack();
        }
    }

    private void updateUnpaid(Optional<UnpaidBalance> optional) {
        if (UnpaidHelper.isUnpaid(optional)) {
            String amount = App.getDataManager().getUnpaidBalance().map(UnpaidBalance::getAmount).orElse("0");
            binding.unpaidAmount.setText(App.getInstance().getString(R.string.money, amount));
        } else {
            goBack();
        }
    }

    private boolean isUnpaidEnabled() {
        return Optional.ofNullable(App.getConfigurationManager().getLastConfiguration().getUnpaidConfig())
                .map(UnpaidConfig::isEnabled).orElse(false);
    }

    private void submitPayment() {
        if (App.getDataManager().isPaymentSelected(PaymentType.BEVO_BUCKS)) {
            String url = App.getDataManager().getUnpaidBalance().map(UnpaidBalance::getBevoBucksUrl).orElse("");
            if (!TextUtils.isEmpty(url)) {
                Intent intent = new Intent(getContext(), BevoBucksPaymentActivity.class).putExtra(BevoBucksPaymentActivity.URL_KEY, url);
                // when returned from BevoBucksPaymentActivity,
                // onStart will start listening to unpaid balance and return to payments list
                startActivityForResult(intent, BevoBucksPaymentActivity.UNPAID_CODE);
                return;
            }
        }
        // otherwise pay with primary card
        paymentSubscription.unsubscribe();
        paymentSubscription = App.getDataManager().payUnpaidBalance()
                .observeOn(RxSchedulers.main())
                .subscribe(new ApiSubscriber2<Void>(getCallback()) {
                    @Override
                    public void onNext(Void aVoid) {
                        super.onNext(aVoid);
                        if (getActivity() != null) {
                            // Unsubscribe to prevent go back before success popup is closed
                            unpaidSubscription.unsubscribe();
                            CommonMaterialDialogCreator.showSupportSuccessDialog(getActivity(),
                                    Optional.of(""), false, UnpaidFragment.this::goBack);
                        }
                    }
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == BevoBucksPaymentActivity.UNPAID_CODE) {
            goBack();
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }

    }

    private void goBack() {
        if (getCallback() != null) {
            // no need to check lifecycle here, it's validated further
            getCallback().navigateTo(R.id.navPayment);
        }
    }

}
