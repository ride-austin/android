package com.rideaustin.ui.signin;

import android.animation.ValueAnimator;
import android.app.Dialog;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatRadioButton;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RadioGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.rideaustin.App;
import com.rideaustin.R;
import com.rideaustin.api.config.GlobalConfig;
import com.rideaustin.base.BaseActivity;
import com.rideaustin.databinding.SplashBinding;
import com.rideaustin.ui.utils.CommonMaterialDialogCreator;
import com.rideaustin.ui.utils.ResourceHelper;
import com.rideaustin.utils.AppInfoUtil;
import com.rideaustin.utils.DialogUtils;
import com.rideaustin.utils.ImageHelper;
import com.rideaustin.utils.gradle.BuildConfigProxy;
import com.rideaustin.utils.gradle.Environment;
import com.rideaustin.utils.toast.RAToast;

import java.util.List;

import timber.log.Timber;

/**
 * Created by kshumelchyk on 6/22/16.
 */
public class SplashActivity extends BaseActivity implements SplashView {

    public static final String LOGOUT_REASON = "logout_reason";

    private SplashBinding binding;
    private SplashViewModel viewModel;
    private String backgroundUrl;
    private String logoUrl;
    private Target<Bitmap> backgroundTarget;
    private Target<Bitmap> logoTarget;
    private boolean shouldFadeIn = true;
    private Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new SplashViewModel(this);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_splash);

        Animation animFadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        binding.signIn.startAnimation(animFadeIn);

        if (getIntent().hasExtra(LOGOUT_REASON)) {
            String reason = getIntent().getStringExtra(LOGOUT_REASON);
            if (reason.length() > 25) {
                RAToast.showLong(reason);
            } else {
                RAToast.showShort(reason);
            }
            shouldFadeIn = false;
        } else {
            shouldFadeIn = true;
        }

        binding.signIn.setVisibility(View.INVISIBLE);
        binding.labelVersion.setText(AppInfoUtil.getAppVersionName());
        binding.signIn.setOnClickListener(view -> startActivity(new Intent(SplashActivity.this, SignInActivity.class)));
    }

    private void handleEnvPicker() {
        if (!AppInfoUtil.isProd()) {
            binding.envContainer.setVisibility(View.VISIBLE);
            final List<Environment> environments = BuildConfigProxy.getEnvironments();
            binding.envContainer.removeAllViews();
            for (final Environment environment : environments) {

                AppCompatRadioButton button = new AppCompatRadioButton(this);
                button.setLayoutParams(new RadioGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                button.setText(environment.getEnv());
                button.setOnClickListener(v -> {
                    if (button.isChecked()) {
                        BuildConfigProxy.setSelectedEnv(button.getText().toString());
                        if (BuildConfigProxy.isCustomEnvironmentSelected()) {
                            new EndpointFragment().show(this);
                        }
                    }
                });
                binding.envContainer.addView(button);
                if (environment.getEnv().equals(BuildConfigProxy.getSelectedEnv())) {
                    binding.envContainer.check(button.getId());
                    button.setChecked(true);
                }
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (shouldFadeIn) {
            viewModel.onStart();
            viewModel.subscribeForConfigChanges();
            binding.mainContainer.setAlpha(0);
            onBackgroundAlphaChanged(0);
        } else {
            onGlobalConfigUpdate(App.getConfigurationManager().getLastConfiguration(), false);
            viewModel.onStart();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        viewModel.onStop();
        cancelImageLoad();
        cancelDialog();
    }

    private void cancelImageLoad() {
        backgroundUrl = null;
        logoUrl = null;
        if (backgroundTarget != null) {
            Glide.with(binding.mainContainer).clear(backgroundTarget);
            backgroundTarget = null;
        }
        if (logoTarget != null) {
            Glide.with(binding.logo).clear(logoTarget);
            logoTarget = null;
        }
    }

    private void cancelDialog() {
        DialogUtils.dismiss(dialog);
    }

    @Override
    public void onGlobalConfigUpdate(final GlobalConfig globalConfig, boolean shouldFadeIn) {
        if (backgroundUrl == null || !backgroundUrl.equals(globalConfig.getGeneralInformation().getSplashUrl())) {
            backgroundUrl = globalConfig.getGeneralInformation().getSplashUrl();
            if (backgroundTarget != null) {
                Glide.with(binding.mainContainer).clear(backgroundTarget);
            }
            BackgroundListener listener = new BackgroundListener(binding.mainContainer);
            backgroundTarget = ImageHelper.loadImageAsBackgroundIntoView(binding.mainContainer, backgroundUrl, listener);
        }
        if (logoUrl == null || !logoUrl.equals(globalConfig.getGeneralInformation().getLogoUrl())) {
            logoUrl = globalConfig.getGeneralInformation().getLogoUrl();
            if (logoTarget != null) {
                Glide.with(binding.logo).clear(logoTarget);
            }
            int placeholder = ResourceHelper.getWhiteLogoDrawableRes(globalConfig);
            logoTarget = ImageHelper.loadImageIntoView(binding.logo, globalConfig.getGeneralInformation().getLogoUrl(), placeholder);
        }
    }

    @Override
    public SigninApiSubscriber getSubscriber() {
        return new SigninApiSubscriber(this, false);
    }

    @Override
    public void displayEnvironmentPicker() {
        handleEnvPicker();
    }

    @Override
    public void onNetworkUnavailable() {
        cancelDialog();
        dialog = CommonMaterialDialogCreator.createNetworkFailDialog(getString(R.string.network_error), this)
                .onPositive((dialog, which) -> {
                    viewModel.performSignIn();
                    dialog.dismiss();
                })
                .onNegative((dialog, which) -> finish())
                .show();
    }

    @Override
    public void onUpgradeNeeded() {
        cancelDialog();
        dialog = CommonMaterialDialogCreator.createMandatoryUpdateDialog(this);
    }

    @Override
    public void onUpgradeCheckFailed() {
        cancelDialog();
        dialog = CommonMaterialDialogCreator
                .createNonCancelNetworkFailDialog(getString(R.string.network_error), this)
                .onPositive((dialog, which) -> {
                    viewModel.performVersionCheck();
                    dialog.dismiss();
                })
                .onNegative((dialog, which) -> finish())
                .show();
    }

    @Override
    public void onUpgradeNotNeeded() {
        if (!App.getDataManager().isAuthorised()) {
            binding.signIn.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onSignInError() {
        if (!App.getDataManager().isAuthorised()) {
            handleEnvPicker();
            binding.signIn.setVisibility(View.VISIBLE);
        } else {
            onNetworkUnavailable();
        }
    }

    @Override
    public boolean shouldBeLoggedIn() {
        return false;
    }

    /**
     * Listener for background view bitmap.
     */
    private class BackgroundListener implements RequestListener<Bitmap> {

        private static final int FADE_DURATION_MS = 1000;

        private View view;

        private BackgroundListener(View view) {
            this.view = view;
        }

        private void onComplete() {
            if (shouldFadeIn) {
                ValueAnimator fadeIn = ValueAnimator.ofFloat(0, 1).setDuration(FADE_DURATION_MS);
                fadeIn.addUpdateListener(anim -> {
                    float rate = anim.getAnimatedFraction();
                    view.setAlpha(rate);
                    onBackgroundAlphaChanged(rate);
                });
                fadeIn.start();
                shouldFadeIn = false;
            }
        }

        @Override
        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
            // unable to load background, just show embedded one
            Timber.e(e, "Unable to load background");
            view.setBackgroundResource(ResourceHelper.getSplashBackgroundDrawableRes(App.getConfigurationManager().getLastConfiguration()));
            onComplete();
            return false;
        }

        @Override
        public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
            onComplete();
            return false;
        }
    }

}
