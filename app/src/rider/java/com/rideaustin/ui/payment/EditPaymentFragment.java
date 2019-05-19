package com.rideaustin.ui.payment;

import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;

import com.devmarvel.creditcardentry.fields.CreditEntryFieldBase;
import com.devmarvel.creditcardentry.internal.CreditCardFieldDelegate;
import com.devmarvel.creditcardentry.library.CardType;
import com.rideaustin.App;
import com.rideaustin.R;
import com.rideaustin.api.model.Payment;
import com.rideaustin.base.ApiSubscriber2;
import com.rideaustin.base.BaseFragment;
import com.rideaustin.databinding.FragmentPaymentEditBinding;
import com.rideaustin.schedulers.RxSchedulers;
import com.rideaustin.utils.KeyboardUtil;
import com.rideaustin.utils.ViewUtils;
import com.rideaustin.utils.toast.RAToast;

/**
 * Created on 07/03/2018
 *
 * @author sdelaysam
 */

public class EditPaymentFragment extends BaseFragment implements CreditCardFieldDelegate {

    private Payment payment;
    private FragmentPaymentEditBinding binding;
    private int textColor;

    public void setPayment(Payment payment) {
        this.payment = payment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setToolbarTitleAligned(R.string.title_payment_edit, Gravity.LEFT);
        textColor = ContextCompat.getColor(getContext(), R.color.charcoalGrey);
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_payment_edit, container, false);
        setupInput(binding.expirationInput, R.color.hint_color);
        setupInput(binding.cvvInput, R.color.disabled_text_color);
        binding.cvvInput.setEnabled(false);
        if (payment != null) {
            PaymentType paymentType = PaymentType.parse(payment.getCardBrand());
            binding.iconPaymentType.setImageResource(paymentType.getIconResId());
            binding.textPaymentCard.setText(getString(R.string.payment_card_template, payment.getCardNumber()));
            binding.cvvInput.setType(paymentType.getCardType());
            binding.btnSave.setOnClickListener(v -> onSave());
        }
        checkButton();
        return binding.getRoot();
    }

    @Override
    public void onCardTypeChange(CardType type) {
    }

    @Override
    public void onCreditCardNumberValid(String remainder) {
    }

    @Override
    public void onExpirationDateValid(String remainder) {
        KeyboardUtil.hideKeyBoard(getContext(), binding.expirationInput);
        binding.expirationInput.clearFocus();

    }

    @Override
    public void onSecurityCodeValid(String remainder) {
    }

    @Override
    public void onZipCodeValid() {
    }

    @Override
    public void onBadInput(EditText field) {
        Animation shake = AnimationUtils.loadAnimation(getContext(), com.devmarvel.creditcardentry.R.anim.shake);
        field.startAnimation(shake);

        field.setTextColor(Color.RED);
        final Handler handler = new Handler();
        handler.postDelayed(() -> field.setTextColor(textColor), 1000);
    }

    @Override
    public void focusOnField(CreditEntryFieldBase field, String initialValue) {
        field.requestFocus();
        field.setSelection(field.getText().length());
    }

    @Override
    public void focusOnPreviousField(CreditEntryFieldBase field) {

    }

    @Override
    public void onFieldValidChanged(CreditEntryFieldBase field, boolean valid) {
        checkButton();
    }

    private void setupInput(CreditEntryFieldBase input, @ColorRes int color) {
        input.setDelegate(this);
        input.setTextColor(textColor);
        input.setHintTextColor(ContextCompat.getColor(getContext(), color));
        input.setPadding(0, 0, 0, 0);
        ViewUtils.setCursorColor(input, ContextCompat.getColor(getContext(), color));
    }

    private void checkButton() {
        binding.btnSave.setEnabled(binding.expirationInput.isValid());
    }

    private void onSave() {
        Integer month = getExpMonth();
        Integer year = getExpYear();
        if (month != null && year != null) {
            untilDestroy(App.getDataManager().updatePayment(payment, month, year)
                    .subscribeOn(RxSchedulers.network())
                    .observeOn(RxSchedulers.main())
                    .subscribe(new ApiSubscriber2<Void>(getCallback()) {
                        @Override
                        public void onNext(Void aVoid) {
                            super.onNext(aVoid);
                            getCallback().hideProgress();
                            if (getActivity() != null) {
                                getActivity().onBackPressed();
                            }
                        }
                    }));
        } else {
            RAToast.showShort("Unable to parse data");
        }
    }

    private Integer getExpMonth() {
        return getDateFragment(0);
    }

    private Integer getExpYear() {
        Integer year = getDateFragment(1);
        if (year != null) {
            return 2000 + year;
        }
        return null;
    }

    private Integer getDateFragment(int position) {
        String expDate = binding.expirationInput.getText().toString();
        if (expDate.contains("/")) {
            String[] split = expDate.split("/");
            if(split.length > 1) {
                try {
                    return Integer.valueOf(split[position]);
                } catch (NumberFormatException ignore) {}
            }
        }
        return null;
    }

}
