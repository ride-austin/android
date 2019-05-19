package com.rideaustin.ui.drawer.promotions;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rideaustin.App;
import com.rideaustin.R;
import com.rideaustin.api.config.GlobalConfig;
import com.rideaustin.api.model.promocode.PromoCodeResponse;
import com.rideaustin.base.BaseActivity;
import com.rideaustin.base.BaseFragment;
import com.rideaustin.base.Transition;
import com.rideaustin.databinding.PromotionsBinding;
import com.rideaustin.stub.SimpleTextWatcher;
import com.rideaustin.ui.drawer.promotions.details.PromotionsDetailsFragment;
import com.rideaustin.ui.utils.MaterialDialogCreator;
import com.rideaustin.utils.localization.AndroidLocalizer;

/**
 * Created by kshumelchyk on 10/4/16.
 */
public class PromotionsFragment extends BaseFragment implements PromotionsView {
    private PromotionsBinding binding;

    private PromotionsViewModel viewModel;

    private TextWatcher promoCodeTextWatcher = new SimpleTextWatcher() {
        @Override
        public void onTextChanged(CharSequence text, int start, int before, int count) {
            binding.apply.setEnabled(!TextUtils.isEmpty(text));
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_promotions, container, false);
        getActivity().setTitle(R.string.promotions_title);
        viewModel = new PromotionsViewModel(this, new AndroidLocalizer(getActivity()));
        binding.setViewModel(viewModel);

        binding.promoDescriptionTextView.setText(getString(R.string.promo_description, App.getAppName()));
        binding.apply.setOnClickListener(v -> onApplyPromoCodeClicked());
        binding.promoCode.addTextChangedListener(promoCodeTextWatcher);

        boolean referralEnabled = App.getConfigurationManager().getLastConfiguration().getRiderReferFriend().isEnabled();
        binding.inviteFriendsBlock.setVisibility(referralEnabled ? View.VISIBLE : View.GONE);
        if (referralEnabled) {
            binding.inviteFriends.setOnClickListener(v -> onInviteFriendsClicked());
        }
        binding.creditsBalance.setOnClickListener(this::openCreditDetails);


        return binding.getRoot();
    }

    private void openCreditDetails(View view) {
        viewModel.getPromoCodeBalance().ifPresent(promoCodeBalance -> {
            double remainder = promoCodeBalance.getRemainder();
            ((BaseActivity) getActivity()).replaceFragment(PromotionsDetailsFragment.create(remainder), R.id.content_frame, true, Transition.FORWARD);
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        viewModel.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        viewModel.onStop();
    }

    private void onApplyPromoCodeClicked() {
        viewModel.applyPromoCode();
    }

    private void onInviteFriendsClicked() {
        startActivity(new Intent(getContext(), FreeRidesActivity.class));
    }

    private void showRedeemSuccessDialog(Double amount, String literal) {
        final String message = getString(R.string.text_promo_success, literal, amount.doubleValue());
        MaterialDialogCreator.createCenteredMessageDialog(getString(R.string.text_promocode), message, (AppCompatActivity) getActivity());
    }

    @Override
    public void showInvalidRiderStateDialog(String codeLiteral, String errorMessage) {
        MaterialDialogCreator.createCenteredMessageDialog(getString(R.string.text_promocode), errorMessage, (AppCompatActivity) getActivity());
    }

    @Override
    public void onConfigurationUpdated(GlobalConfig config) {
        binding.promoDescriptionTextView.setText(getString(R.string.promo_description, App.getAppName()));
    }

    @Override
    public void onPromoCodeApplied(PromoCodeResponse promoCode) {
        showRedeemSuccessDialog(promoCode.getCodeValue(), promoCode.getCodeLiteral());
    }
}