package com.rideaustin.base;

import android.Manifest;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.FloatRange;
import android.support.annotation.IdRes;
import android.support.annotation.IntegerRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.rideaustin.App;
import com.rideaustin.CurrentAvatarType;
import com.rideaustin.R;
import com.rideaustin.api.config.GlobalConfig;
import com.rideaustin.api.config.RideCancellationConfig;
import com.rideaustin.api.model.AvatarType;
import com.rideaustin.api.model.RideStatus;
import com.rideaustin.manager.notification.InAppMessage;
import com.rideaustin.schedulers.RxSchedulers;
import com.rideaustin.ui.common.BaseView;
import com.rideaustin.ui.common.LifecycleSubscriptions;
import com.rideaustin.ui.common.RxBaseViewModel;
import com.rideaustin.ui.common.ViewModelsProvider;
import com.rideaustin.ui.feedback.RideCancellationFeedbackFragment;
import com.rideaustin.ui.signin.SplashActivity;
import com.rideaustin.ui.utils.MaterialDialogCreator;
import com.rideaustin.ui.utils.ResourceHelper;
import com.rideaustin.ui.widgets.LoadingWheel;
import com.rideaustin.utils.Constants;
import com.rideaustin.utils.DialogUtils;
import com.rideaustin.utils.ImageHelper;
import com.rideaustin.utils.TimeUtils;
import com.rideaustin.utils.toast.RAToast;
import com.tbruyelle.rxpermissions.RxPermissions;

import rx.Subscription;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

/**
 * @author shumelchyk
 */
public abstract class BaseActivity<T extends RxBaseViewModel> extends AppCompatActivity implements BaseActivityCallback, FragmentHost, BaseView {

    private static final float BG_SHOWN_THRESHOLD = 0.6f;
    public static final String B_START = "<b>";
    public static final String B_END = "</b>";
    public static final String BR = "<br>";
    protected boolean stopped;
    private MaterialDialog progress;
    private String progressMessage;
    private LoadingWheel loadingWheel;
    private boolean backgroundShown = true;
    @Nullable private TextView title;
    @Nullable private TextView subtitle;
    @Nullable private ImageView avatar;
    @Nullable private ImageView appTitleLogo;
    private BitmapImageViewTarget imageViewTarget;
    private CompositeSubscription subscriptions = new CompositeSubscription();
    private CompositeSubscription inAppMessageSubscriptions = new CompositeSubscription();
    private LifecycleSubscriptions lifecycleSubscriptions = new LifecycleSubscriptions();
    private T viewModel;
    private SparseArray<MaterialDialog> shownDialogs = new SparseArray<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        if (CurrentAvatarType.getAvatarType() == AvatarType.DRIVER) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        TimeUtils.init(this);
    }

    protected void setToolbar(Toolbar toolbar) {
        if (toolbar != null) {
            this.title = (TextView) toolbar.findViewById(R.id.toolbarTitle);
            this.subtitle = (TextView) toolbar.findViewById(R.id.toolbarSubtitle);
            this.avatar = (ImageView) toolbar.findViewById(R.id.toolbarAvatar);
            this.appTitleLogo = (ImageView) toolbar.findViewById(R.id.app_title_logo);
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
    }

    @Override
    public void setToolbarTitle(@NonNull String title) {
        GlobalConfig lastConfiguration = App.getConfigurationManager().getLastConfiguration();
        if (title.equals(lastConfiguration.getGeneralInformation().getApplicationNamePipe())) {
            if (appTitleLogo != null) {
                appTitleLogo.setBackgroundResource(ResourceHelper.getBlackLogoDrawableRes(lastConfiguration));
                appTitleLogo.setVisibility(View.VISIBLE);
            }
            if (avatar != null) {
                avatar.setVisibility(View.GONE);
            }
            if (this.title != null) {
                this.title.setVisibility(View.GONE);
            }
            if (this.subtitle != null) {
                this.subtitle.setVisibility(View.GONE);
            }
        } else {
            if (this.title != null) {
                this.title.setVisibility(View.VISIBLE);
            }
            if (appTitleLogo != null) {
                appTitleLogo.setVisibility(View.GONE);
            }
            setToolbarTitleWithGravity(title, Gravity.CENTER_HORIZONTAL);
        }
    }

    @Override
    public void setToolbarTitle(@NonNull @StringRes int stringId) {
        setToolbarTitleWithGravity(getString(stringId), Gravity.CENTER_HORIZONTAL);
    }

    @Override
    public void setToolbarTitleAligned(@StringRes int stringId, @IntegerRes int gravity) {
        setToolbarTitleWithGravity(getString(stringId), gravity);
    }

    @Override
    public void setToolbarTitleAligned(String title, @IntegerRes int gravity) {
        setToolbarTitleWithGravity(title, gravity);
    }

    private void setToolbarTitleWithGravity(String title, @IntegerRes int gravity) {
        if (this.title != null) {
            this.title.setVisibility(View.VISIBLE);
            this.title.setTypeface(this.title.getTypeface(), Typeface.NORMAL);
            if (this.title.getLayoutParams() instanceof Toolbar.LayoutParams) {
                Toolbar.LayoutParams params = (Toolbar.LayoutParams) this.title.getLayoutParams();
                params.gravity = gravity;
                this.title.setLayoutParams(params);
            } else if (this.title.getLayoutParams() instanceof LinearLayout.LayoutParams) {
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) this.title.getLayoutParams();
                params.gravity = gravity;
                this.title.setLayoutParams(params);
            }
            this.title.setText(title);
        }
        if (appTitleLogo != null) {
            appTitleLogo.setVisibility(View.GONE);
        }
        if (this.subtitle != null) {
            this.subtitle.setVisibility(View.GONE);
        }
    }

    @Override
    public void setToolbarSubtitle(@NonNull String subtitle) {
        if (appTitleLogo != null) {
            appTitleLogo.setVisibility(View.GONE);
        }
        if (title != null) {
            if (CurrentAvatarType.getAvatarType() == AvatarType.DRIVER) {
                title.setTypeface(title.getTypeface(), Typeface.BOLD);
                if (this.subtitle != null) {
                    this.subtitle.setVisibility(View.VISIBLE);
                    this.subtitle.setText(subtitle);
                }
            } else {
                title.setText(subtitle);
            }
        }
    }

    @Override
    public void setToolbarSubtitle(@NonNull @StringRes int stringId) {
        setToolbarSubtitle(getString(stringId));
    }

    @Override
    public void setToolbarAvatar(@Nullable String photoUrl) {
        if (avatar != null) {
            avatar.setVisibility(View.VISIBLE);
            imageViewTarget = ImageHelper.loadRoundImageIntoView(this, avatar, photoUrl, R.drawable.ic_user_icon);
        }
        if (title != null) {
            if (this.title.getLayoutParams() instanceof Toolbar.LayoutParams) {
                Toolbar.LayoutParams params = (Toolbar.LayoutParams) this.title.getLayoutParams();
                params.gravity = Gravity.START;
                this.title.setLayoutParams(params);
            } else if (this.title.getLayoutParams() instanceof LinearLayout.LayoutParams) {
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) this.title.getLayoutParams();
                params.gravity = Gravity.START;
                this.title.setLayoutParams(params);
            }
        }
    }

    @Override
    public void clearToolbarTitles() {
        setToolbarTitleWithGravity("", Gravity.CENTER_HORIZONTAL);
        if (avatar != null) {
            avatar.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (viewModel != null) {
            viewModel.onResume();
        }
        // added due to RA-9951 - problem on some devices
        stopped = false;
        checkLoginStatus();
        subscribeToInAppMessages();
    }

    @Override
    protected void onPause() {
        super.onPause();
        lifecycleSubscriptions.onStop();
        if (viewModel != null) {
            viewModel.onPause();
        }
        hideProgress();
        inAppMessageSubscriptions.clear();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (viewModel != null) {
            viewModel.onStart();
            subscribeToProgressEvents();
        }
        stopped = false;
        checkLoginStatus();
        if (!isFinishing() && needToHandlePermissionRequest()) {
            // listen during onStart..onStop
            // not during onResume..onPause
            // because permission UI causes onPause and unsubscribe
            subscribeToPermissionRequest();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        lifecycleSubscriptions.onStop();
        if (viewModel != null) {
            viewModel.onStop();
        }
        stopped = true;
        subscriptions.clear();
        if (imageViewTarget != null) {
            Glide.with(this).clear(imageViewTarget);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // to prevent illegal fragment transactions
        stopped = true;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        lifecycleSubscriptions.onDestroy();
        if (viewModel != null) {
            viewModel.onDestroy();
        }
        clearViewModels();
        inAppMessageSubscriptions.unsubscribe();
    }

    @Override
    public void showProgress() {
        showProgress(getString(R.string.loading));
    }

    @Override
    public void showProgress(String message) {
        hideLoadingWheel();
        if (progressMessage == null || !progressMessage.equals(message)) {
            hideProgressDialog();

            // RA-8897: check if activity is not finishing
            if (isFinishing()) {
                return;
            }

            progressMessage = message;
            progress = new MaterialDialog.Builder(this)
                    .content(message)
                    .cancelable(false)
                    .progress(true, 0)
                    .build();

            try {
                progress.show();
            } catch (Exception e) {
                Timber.e(e, "Unable to show progress dialog");
            }
        }
    }

    @Override
    public void showLoadingWheel() {
        hideProgressDialog();
        if (loadingWheel == null) {
            loadingWheel = (LoadingWheel) getLayoutInflater().inflate(R.layout.view_loading_wheel, null);
            loadingWheel.placeInCenter(backgroundShown, false);
            loadingWheel.show(this);
        } else {
            loadingWheel.placeInCenter(backgroundShown, true);
        }
        loadingWheel.showBackground(backgroundShown);
    }

    @Override
    public void onBackgroundAlphaChanged(@FloatRange(from = 0f, to = 1f) float alpha) {
        boolean shown = alpha > BG_SHOWN_THRESHOLD;
        if (backgroundShown != shown) {
            backgroundShown = shown;
            if (loadingWheel != null) {
                loadingWheel.placeInCenter(shown, true);
                loadingWheel.showBackground(shown);
            }
        }
    }

    @Override
    public void hideProgress() {
        hideProgressDialog();
        hideLoadingWheel();
    }

    private void hideProgressDialog() {
        if (DialogUtils.isShowing(progress)) {
            DialogUtils.dismiss(progress, "Unable to hide progress dialog");
            progress = null;
            progressMessage = null;
        }
    }

    private void hideLoadingWheel() {
        if (loadingWheel != null) {
            loadingWheel.hide();
            loadingWheel = null;
        }
    }

    protected Fragment getFragment(int res) {
        return FragmentHelper.getFragment(this, res);
    }

    /**
     * Clear backstack up to {@code backStackName}
     *
     * @param backStackName - name of transaction which will be on top after this operation
     */
    protected void clearBackstack(String backStackName) {
        FragmentHelper.clearBackStack(this, backStackName);
    }

    public void replaceFragment(Fragment f, int container, boolean addToStack) {
        FragmentHelper.replaceFragment(this, f, container, addToStack);
    }

    public void replaceFragment(Fragment f, int container, boolean addToStack, Transition transition) {
        FragmentHelper.replaceFragment(this, f, container, addToStack, transition);
    }

    public void replaceFragment(Fragment f, int container, boolean addToStack, String backStackName, Transition transition) {
        FragmentHelper.replaceFragment(this, f, container, addToStack, backStackName, transition);
    }

    public void addFragment(Fragment f, int container, boolean addToStack) {
        FragmentHelper.addFragment(this, f, container, addToStack);
    }

    public void addFragment(Fragment f, int container, boolean addToStack, Transition transition) {
        FragmentHelper.addFragment(this, f, container, addToStack, transition);
    }

    public void removeFragment(Fragment f) {
        FragmentHelper.removeFragment(this, f);
    }

    public void removeFragment(Fragment f, Transition transition) {
        FragmentHelper.removeFragment(this, f, transition);
    }

    public void hideFragment(Fragment f) {
        FragmentHelper.hideFragment(this, f, Transition.FORWARD);
    }

    public void hideFragment(Fragment f, Transition transition) {
        FragmentHelper.hideFragment(this, f, transition);
    }

    public void showFragment(Fragment f) {
        FragmentHelper.showFragment(this, f);
    }

    public void showFragment(Fragment f, Transition transition) {
        FragmentHelper.showFragment(this, f, transition);
    }

    @Nullable
    public Fragment findFragmentById(@IdRes int id) {
        return FragmentHelper.findFragmentById(this, id);
    }

    @Override
    public boolean isInvalid() {
        return stopped || isFinishing();
    }

    @Override
    public void commitTransaction(FragmentTransaction transaction) {
        try {
            transaction.commit();
        } catch (IllegalStateException e) {
            Timber.e(e, e.getMessage());
        }
    }

    @Override
    public void onBackPressed() {
        if (!isFinishing() && !stopped) {
            super.onBackPressed();
//            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        }
    }

    public void onMenuToggleClick() {
        //Do nothing by default
    }

    @Override
    public void setToolbarIndicatorEnabled(boolean enabled) {
        //Do nothing by default
    }

    @Override
    public boolean navigateTo(@IdRes int id) {
        return false;
    }

    @Override
    public void hideMenu(@IdRes int id) {
        //Do nothing by default
    }

    @Override
    public void showMenu(@IdRes int id) {
        //Do nothing by default
    }

    /**
     * Override this method and return false
     * in case activity doesn't require to have authenticated user
     *
     * @return return true by default
     */
    public boolean shouldBeLoggedIn() {
        return true;
    }

    private void checkLoginStatus() {
        if (!App.getDataManager().isLoggedIn() && shouldBeLoggedIn()) {
            // launch splash activity
            Intent intent = new Intent(this, SplashActivity.class);
            intent.putExtras(getIntent()); // save intent to be passed back again after auto-login
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            // finish all activities in task
            finishAffinity();
        }
    }

    protected boolean needToHandlePermissionRequest() {
        return true;
    }

    private void subscribeToPermissionRequest() {
        subscriptions.add(App.getConfigurationManager()
                .getLocationPermissionRequest()
                .take(1) // take only one request
                .observeOn(RxSchedulers.main())
                .subscribe(this::doOnLocationPermissionRequest));
    }

    private void doOnLocationPermissionRequest(Boolean value) {
        if (!stopped && !isFinishing()) {
            subscriptions.add(new RxPermissions(this)
                    .request(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
                    .subscribe(isGranted -> {
                        if (isGranted) {
                            App.getConfigurationManager().onLocationPermissionGranted();
                        } else {
                            RAToast.showShort(getString(R.string.dont_have_permission, App.getAppName()));
                        }
                    }, throwable -> Timber.e(throwable, "Unable to process permissions")));
        }
    }

    private void subscribeToInAppMessages() {
        inAppMessageSubscriptions.clear();
        inAppMessageSubscriptions.add(App.getConfigurationManager().getLiveConfig()
                .flatMap(globalConfig -> App.getInstance().getInAppMessageManager().getNewMessages())
                .observeOn(RxSchedulers.main())
                .subscribe(this::onInAppMessage, Timber::e));
        inAppMessageSubscriptions.add(App.getInstance().getInAppMessageManager()
                .getConsumedMessage()
                .observeOn(RxSchedulers.main())
                .subscribe(this::onInAppMessageConsumed, Timber::e));
    }

    protected void onInAppMessage(InAppMessage message) {
        if (shownDialogs.get(message.getId()) != null) {
            return;
        }

        if (requiresRideCancellationFeedback(message)) {
            message.consume();
            showRideCancellationFeedback(message.getRideId(), null);
            return;
        }

        MaterialDialog dialog = MaterialDialogCreator
                .createInAppMessageDialog(message.getTitle(), message.getMessage(), this,
                        () -> App.getInstance().getInAppMessageManager().read(message));
        shownDialogs.put(message.getId(), dialog);
    }

    private void onInAppMessageConsumed(InAppMessage message) {
        MaterialDialog dialog = shownDialogs.get(message.getId());
        DialogUtils.dismiss(dialog);
        shownDialogs.remove(message.getId());
    }

    @CallSuper
    protected void clearViewModels() {
        ViewModelsProvider.clear(getClass());
        viewModel = null;
    }

    protected final T getViewModel() {
        return viewModel;
    }

    protected final void setViewModel(T viewModel) {
        this.viewModel = viewModel;
    }

    protected final <VM extends RxBaseViewModel> VM obtainViewModel(Class<VM> viewModelClass) {
        return ViewModelsProvider.of(getClass()).get(viewModelClass);
    }

    protected final void untilPause(Subscription subscription) {
        lifecycleSubscriptions.untilPause(subscription);
    }

    protected final void untilStop(Subscription subscription) {
        lifecycleSubscriptions.untilStop(subscription);
    }

    protected final void untilDestroy(Subscription subscription) {
        lifecycleSubscriptions.untilDestroy(subscription);
    }

    private void subscribeToProgressEvents() {
        if (viewModel != null) {
            untilStop(viewModel.getProgressEvents()
                    .subscribeOn(RxSchedulers.main())
                    .subscribe(progressEvent -> {
                        switch (progressEvent.getType()) {
                            case SHOW_PROGRESS:
                                showProgress();
                                break;
                            case SHOW_PROGRESS_WITH_MESSAGE:
                                showProgress(progressEvent.getMessage().orElse(""));
                                break;
                            case SHOW_LOADING_WHEEL:
                                showLoadingWheel();
                                break;
                            case HIDE_PROGRESS:
                                hideProgress();
                                break;
                        }
                    }));
        }
    }

    private boolean requiresRideCancellationFeedback(InAppMessage message) {
        boolean riderCancelled = !Constants.IS_DRIVER
                && message.getRideStatus() == RideStatus.RIDER_CANCELLED
                && message.getRideId() != 0L;
        if (riderCancelled) {
            RideCancellationConfig config = App.getConfigurationManager().getLastConfiguration().getRideCancellationConfig();
            if (config != null && config.isEnabled()) {
                if (message.getReceivedAt() > 0 && message.getRide().getDriverAcceptedOn() > 0) {
                    long duration = message.getReceivedAt() - message.getRide().getDriverAcceptedOn();
                    return duration >= config.getCancellationThreshold() * 1000;
                }
            }
        }
        return false;
    }

    public final void showRideCancellationFeedback(long rideId, @Nullable RideCancellationFeedbackFragment.Action action) {
        if (!isInvalid()) {
            RideCancellationFeedbackFragment fragment = new RideCancellationFeedbackFragment();
            fragment.setup(rideId, action);
            fragment.show(getSupportFragmentManager(), RideCancellationFeedbackFragment.class.getName());
        }
    }

    public final void hideRideCancellationFeedback() {
        RideCancellationFeedbackFragment fragment = (RideCancellationFeedbackFragment) getSupportFragmentManager()
                .findFragmentByTag(RideCancellationFeedbackFragment.class.getName());
        if (fragment != null && !fragment.isInProgress()) {
            fragment.dismissAllowingStateLoss();
        }
    }

}
