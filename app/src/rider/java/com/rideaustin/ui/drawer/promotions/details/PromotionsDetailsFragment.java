package com.rideaustin.ui.drawer.promotions.details;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rideaustin.R;
import com.rideaustin.base.BaseFragment;
import com.rideaustin.databinding.FragmentPromotionsDetailsBinding;
import com.rideaustin.schedulers.RxSchedulers;

import java8.util.Optional;
import rx.Subscription;
import rx.subscriptions.Subscriptions;

/**
 * Created by hatak on 01.09.2017.
 */

public class PromotionsDetailsFragment extends BaseFragment {

    private static final String BALANCE_KEY = "balance_key";

    private FragmentPromotionsDetailsBinding binding;
    private PromotionsDetailsViewModel viewModel;
    private Subscription promoCodesSubscription = Subscriptions.empty();

    public static PromotionsDetailsFragment create(final double totalBalance) {
        PromotionsDetailsFragment fragment = new PromotionsDetailsFragment();
        Bundle bundle = new Bundle();
        fragment.setArguments(bundle);
        bundle.putDouble(BALANCE_KEY, totalBalance);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_promotions_details, container, false);
        getActivity().setTitle(R.string.credits_title);
        viewModel = new PromotionsDetailsViewModel();
        binding.setViewModel(viewModel);
        setupCreditDetails();
        fillData();
        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        viewModel.loadPromoCodes(getCallback());
    }

    private void setupCreditDetails() {
        CreditDetailsAdapter adapter = new CreditDetailsAdapter();
        binding.creditsList.setAdapter(adapter);
        binding.creditsList.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    private void fillData() {
        Optional.ofNullable(getArguments())
                .map(bundle -> bundle.getDouble(BALANCE_KEY))
                .ifPresent(balance -> viewModel.setTotalBalance(balance));

        promoCodesSubscription = viewModel.getPromoCodes()
                .subscribeOn(RxSchedulers.network())
                .observeOn(RxSchedulers.main())
                .subscribe(promoCodes -> {
                    CreditDetailsAdapter adapter = (CreditDetailsAdapter) binding.creditsList.getAdapter();
                    adapter.setData(promoCodes);
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        promoCodesSubscription.unsubscribe();
    }
}
