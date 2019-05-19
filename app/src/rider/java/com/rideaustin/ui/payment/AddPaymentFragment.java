package com.rideaustin.ui.payment;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.MaterialDialog;
import com.devmarvel.creditcardentry.library.CreditCard;
import com.rideaustin.App;
import com.rideaustin.R;
import com.rideaustin.api.model.Payment;
import com.rideaustin.base.ApiSubscriber2;
import com.rideaustin.base.BaseApiException;
import com.rideaustin.base.BaseFragment;
import com.rideaustin.databinding.FragmentPaymentAddBinding;
import com.rideaustin.schedulers.RxSchedulers;
import com.rideaustin.ui.utils.MaterialDialogCreator;
import com.rideaustin.utils.toast.RAToast;
import com.stripe.android.model.Card;

import rx.Subscription;
import rx.subscriptions.Subscriptions;
import timber.log.Timber;

/**
 * Created by v.garshyn on 09.07.16.
 */
public class AddPaymentFragment extends BaseFragment {

    private FragmentPaymentAddBinding binding;
    private Subscription addSubscription = Subscriptions.empty();
    private Payment payment;
    private EventListener eventListener;

    public void setEventListener(EventListener eventListener) {
        this.eventListener = eventListener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setToolbarTitleAligned(R.string.title_payment_add, Gravity.LEFT);
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_payment_add, container, false);
        binding.creditCardForm.setOnCardValidCallback(creditCard -> checkButton());
        binding.creditCardForm.setOnFocusChangeListener((v, hasFocus) -> checkButton());
        checkButton();
        return binding.getRoot();
    }

    private void checkButton() {
        binding.btnAddPayment.setEnabled(binding.creditCardForm.isCreditCardValid());
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        binding.btnAddPayment.setOnClickListener(v -> executeNewPaymentAdd(binding.creditCardForm.getCreditCard()));
    }

    @Override
    public void onResume() {
        super.onResume();
        RxSchedulers.schedule(() -> {
            // post action, immediate transaction on resume may fail
            if (payment != null && eventListener != null) {
                goBackWithPayment(payment);
            }
        });
    }

    private void executeNewPaymentAdd(CreditCard creditCard) {
        // Stripe sample card
        // Card card = new Card("4242424242424242", 12, 2017, "123");
        Card card = new Card(creditCard.getCardNumber(), creditCard.getExpMonth(), creditCard.getExpYear(), creditCard.getSecurityCode());
        if (card.validateCard()) {
            addSubscription.unsubscribe();
            addSubscription = App.getDataManager().addNewPayment(card)
                    .observeOn(RxSchedulers.main())
                    .doOnUnsubscribe(() -> getCallback().hideProgress())
                    .subscribe(new ApiSubscriber2<Payment>(getCallback()) {

                        @Override
                        public void onNext(Payment payment) {
                            // RA-10355: clear before credit card view save its state
                            // see CreditCardForm.onSaveInstanceState()
                            clear();
                            doOnPaymentAdded(payment);
                        }

                        @Override
                        public void onUnknownError(BaseApiException e) {
                            super.onUnknownError(e);
                            // other cases with show toasts
                            RAToast.showShort(e.getMessage());
                        }

                        private void clear() {
                            binding.creditCardForm.clearFocus();
                            binding.creditCardForm.clearForm();
                            checkButton();
                        }

                    });
        } else {
            MaterialDialog errorDialog = MaterialDialogCreator.createSimpleErrorDialog(
                    getString(R.string.payment_add_error),
                    getActivity());
            errorDialog.show();
        }
    }

    private void doOnPaymentAdded(Payment payment) {
        if (isResumed() && eventListener != null) {
            goBackWithPayment(payment);
        } else {
            // save payment to execute transaction onResume()
            this.payment = payment;
        }
    }

    private void goBackWithPayment(Payment payment) {
        try {
            eventListener.onAddPaymentAdded(payment);
            this.payment = null;
        } catch (Exception e) {
            // unable to execute transaction
            Timber.e(e, "Unable to get back with payment: " + payment);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        addSubscription.unsubscribe();
    }

    public interface EventListener {
        void onAddPaymentAdded(Payment payment);

    }
}
