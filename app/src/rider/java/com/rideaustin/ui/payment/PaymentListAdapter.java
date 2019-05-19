package com.rideaustin.ui.payment;

import android.databinding.DataBindingUtil;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;

import com.rideaustin.R;
import com.rideaustin.api.model.Payment;
import com.rideaustin.databinding.FragmentPaymentItemBinding;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by v.garshyn on 09.07.16.
 */
class PaymentListAdapter extends BaseAdapter implements AdapterView.OnItemClickListener {

    private List<Payment> payments = new ArrayList<>(0);
    private PaymentInteractionListener paymentInteractionListener;

    public PaymentListAdapter(PaymentInteractionListener listener) {
        paymentInteractionListener = listener;
    }

    @Override
    public int getCount() {
        return payments.size();
    }

    @Override
    public Payment getItem(int position) {
        return payments.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        final Payment payment = getItem(position);
        FragmentPaymentItemBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.fragment_payment_item, parent, false);
        PaymentType paymentType = PaymentType.parse(payment.getCardBrand());
        switch (paymentType) {
            case BEVO_BUCKS:
                binding.textPaymentCard.setText(payment.getCardNumber());
                binding.btnPaymentInfo.setVisibility(View.VISIBLE);
                binding.btnPaymentEdit.setVisibility(View.GONE);
                binding.btnPaymentInfo.setOnClickListener(v -> {
                    if (paymentInteractionListener != null) {
                        paymentInteractionListener.onPaymentInfo(payment);
                    }
                });
                break;
            default:
                binding.textPaymentCard.setText(parent.getResources().getString(R.string.payment_card_template, payment.getCardNumber()));
                binding.btnPaymentInfo.setVisibility(View.GONE);
                binding.btnPaymentEdit.setVisibility(View.VISIBLE);
                binding.btnPaymentEdit.setOnClickListener(v -> {
                    if (paymentInteractionListener != null) {
                        paymentInteractionListener.onPaymentEdit(payment, v);
                    }
                });
                break;
        }

        binding.iconPaymentType.setImageResource(paymentType.getIconResId());
        binding.iconPaymentPrimary.setVisibility(payment.isLocalPrimary() ? View.VISIBLE : View.INVISIBLE);
        return binding.getRoot();
    }

    public void updatePaymentsData(List<Payment> data) {
        if (data != null && !data.isEmpty()) {
            payments = data;
            notifyDataSetChanged();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Payment payment = getItem(position);
        if (paymentInteractionListener != null) {
            paymentInteractionListener.onPaymentClick(payment);
        }
    }

    public interface PaymentInteractionListener {
        void onPaymentEdit(Payment payment, View anchorView);

        void onPaymentClick(Payment payment);

        void onPaymentInfo(Payment payment);
    }
}
