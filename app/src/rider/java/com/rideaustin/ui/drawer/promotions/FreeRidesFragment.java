package com.rideaustin.ui.drawer.promotions;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.rideaustin.App;
import com.rideaustin.R;
import com.rideaustin.api.config.GlobalConfig;
import com.rideaustin.api.config.RiderReferFriend;
import com.rideaustin.api.model.promocode.PromoCodeResponse;
import com.rideaustin.base.ApiSubscriber;
import com.rideaustin.base.BaseApiException;
import com.rideaustin.base.BaseFragment;
import com.rideaustin.databinding.FreeRidesBinding;
import com.rideaustin.schedulers.RxSchedulers;
import com.rideaustin.ui.utils.MaterialDialogCreator;
import com.rideaustin.utils.toast.RAToast;

import java.util.Locale;

import rx.Subscription;
import rx.subscriptions.Subscriptions;
import timber.log.Timber;

import static com.rideaustin.utils.CommonConstants.UNEXPECTED_STATE_KEY;

/**
 * Created by kshumelchyk on 10/5/16.
 */
public class FreeRidesFragment extends BaseFragment implements FreeRidesViewModel.FreeRidesListener {

    private static final String TEMPLATE_CODE_VALUE = "<codeValue>";
    private static final String TEMPLATE_CODE_LITERAL = "<codeLiteral>";
    private static final String TEMPLATE_DOWNLOAD_URL = "<downloadUrl>";

    private FreeRidesBinding binding;
    private PromoCodeResponse promoCodeInfo;

    private FreeRidesViewModel viewModel;
    private Subscription permissionSubscription = Subscriptions.empty();
    private Subscription loadSubscription = Subscriptions.empty();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_free_rides, container, false);
        viewModel = new FreeRidesViewModel(this);
        binding.setViewModel(viewModel);
        loadPromoCode();

        binding.textInvite.setOnClickListener(v -> sendTextInvite());
        binding.emailInvite.setOnClickListener(v -> sendEmailInvite());
        binding.socialShare.setOnClickListener(v -> shareOnSocialMedia());

        return binding.getRoot();
    }

    private String formatCodeValue(double value) {
        return String.format(Locale.US, "$%.2f", value);
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        loadSubscription.unsubscribe();
        permissionSubscription.unsubscribe();
    }

    private void loadPromoCode() {
        if (App.getDataManager().getCurrentUser() != null) {
            final long riderId = App.getDataManager().getCurrentUser().getRiderId();

            if (riderId == -1) {
                Exception e = new Exception(UNEXPECTED_STATE_KEY);
                Timber.e(e, "RiderId = -1. Promo Code can not be applied");
                RAToast.showShort(R.string.error_unknown);
                return;
            }
            loadSubscription.unsubscribe();
            loadSubscription = App.getDataManager().getPromoCodeService()
                    .getRiderPromoCode(riderId)
                    .subscribeOn(RxSchedulers.network())
                    .observeOn(RxSchedulers.main())
                    .subscribe(new ApiSubscriber<PromoCodeResponse>(getCallback()) {
                        @Override
                        public void onNext(PromoCodeResponse code) {
                            super.onNext(code);
                            boolean hasPromoCode = !TextUtils.isEmpty(code.getCodeLiteral());
                            viewModel.hasPromoCode.set(hasPromoCode);
                            if (hasPromoCode) {
                                promoCodeInfo = code;
                                binding.promoCode.setText(promoCodeInfo.getCodeLiteral());
                                String detailsText = App.getConfigurationManager().getLastConfiguration().getRiderReferFriend().getDetailtexttemplate();
                                binding.freeRidesIntro.setText(detailsText.replace(TEMPLATE_CODE_VALUE, formatCodeValue(promoCodeInfo.getCodeValue())));
                            }
                        }

                        @Override
                        public void onError(BaseApiException e) {
                            super.onError(e);
                            RAToast.showShort(R.string.promo_code_error);
                            viewModel.hasPromoCode.set(false);
                        }
                    });
        }
    }

    private void shareOnSocialMedia() {
        MaterialDialogCreator.createShareOnSocialMediaDialog(getActivity(), shareOption -> {
            switch (shareOption){
                case FACEBOOK:
                    shareOnFacebook();
                    break;
                case OTHER:
                    sendGenericInvite();
                    break;
            }
        }).show();
    }

    private void shareOnFacebook() {
        RiderReferFriend riderReferFriend = App.getConfigurationManager().getLastConfiguration().getRiderReferFriend();
        String inviteText = riderReferFriend.getSmsbodytemplate();
        inviteText = formatInviteTextBeforeSending(inviteText);

        ShareLinkContent content = new ShareLinkContent.Builder()
                .setContentUrl(Uri.parse(riderReferFriend.getDownloadUrl()))
                .setQuote(inviteText)
                .build();
        ShareDialog shareDialog = new ShareDialog(getActivity());
        shareDialog.show(content, ShareDialog.Mode.AUTOMATIC);
    }

    private String formatInviteTextBeforeSending(String inviteText) {
        RiderReferFriend riderReferFriend = App.getConfigurationManager().getLastConfiguration().getRiderReferFriend();
        return inviteText.replace(TEMPLATE_CODE_VALUE, formatCodeValue(promoCodeInfo.getCodeValue()))
                .replace(TEMPLATE_CODE_LITERAL, promoCodeInfo.getCodeLiteral())
                .replace(TEMPLATE_DOWNLOAD_URL, riderReferFriend.getDownloadUrl());
    }

    private void sendTextInvite() {
        Intent sendIntent = new Intent(Intent.ACTION_VIEW, Uri.fromParts(getString(R.string.scheme_sms), "", null));
        String inviteText = App.getConfigurationManager().getLastConfiguration().getRiderReferFriend()
                .getSmsbodytemplate();
        inviteText = formatInviteTextBeforeSending(inviteText);
        sendIntent.putExtra(getString(R.string.extra_sms), inviteText);
        startActivity(sendIntent);
    }

    private void sendEmailInvite() {
        // IMPORTANT: email clients stop displaying HTML tags, need to find out the reason

        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", "", null));
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.text_invite_title, App.getAppName()));
        String inviteText = App.getConfigurationManager().getLastConfiguration().getRiderReferFriend().getEmailbodytemplate();
        intent.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(formatInviteTextBeforeSending(inviteText)));
        startActivity(Intent.createChooser(intent, getString(R.string.text_email_action)));

        // this strips html tags as well, and in addition shows non-email client

//        Intent intent = new Intent(Intent.ACTION_SEND);
//        intent.setType(getString(R.string.scheme_html));
//        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.text_invite_title, App.getAppName()));
//        String inviteText = App.getConfigurationManager().getLastConfiguration().getRiderReferFriend().getEmailbodytemplate();
//        intent.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(formatInviteTextBeforeSending(inviteText)));
//        intent.putExtra(Intent.EXTRA_HTML_TEXT, Html.fromHtml(formatInviteTextBeforeSending(inviteText)));
//        startActivity(Intent.createChooser(intent, getString(R.string.text_email_action)));
    }

    private void sendGenericInvite() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType(getString(R.string.scheme_text));
        String textTemplate = App.getConfigurationManager().getLastConfiguration().getRiderReferFriend().getSmsbodytemplate();
        String htmlTemplate = App.getConfigurationManager().getLastConfiguration().getRiderReferFriend().getEmailbodytemplate();
        intent.putExtra(Intent.EXTRA_TEXT, formatInviteTextBeforeSending(textTemplate));
        intent.putExtra(Intent.EXTRA_HTML_TEXT, Html.fromHtml(formatInviteTextBeforeSending(htmlTemplate)));
        startActivity(Intent.createChooser(intent, getString(R.string.share_action)));
    }

    @Override
    public void onGlobalConfigUpdate(GlobalConfig globalConfig) {
        binding.appLogoText.setText(App.getFormattedAppName());
    }
}
