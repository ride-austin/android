package com.rideaustin.ui.drawer;

import android.Manifest;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;
import com.rideaustin.App;
import com.rideaustin.R;
import com.rideaustin.api.config.Accessibility;
import com.rideaustin.api.config.CampaignProvider;
import com.rideaustin.api.config.DirectConnectConfig;
import com.rideaustin.api.config.GlobalConfig;
import com.rideaustin.api.model.Avatar;
import com.rideaustin.api.model.CampaignParams;
import com.rideaustin.api.model.Payment;
import com.rideaustin.api.model.User;
import com.rideaustin.api.model.driver.RequestedDriverType;
import com.rideaustin.base.Transition;
import com.rideaustin.databinding.ActivityNavigationDrawerBinding;
import com.rideaustin.manager.notification.InAppMessage;
import com.rideaustin.manager.notification.SplitFareMessage;
import com.rideaustin.schedulers.RxSchedulers;
import com.rideaustin.ui.BackPressable;
import com.rideaustin.ui.base.BaseRiderActivity;
import com.rideaustin.ui.campaigns.CampaignDetailsActivity;
import com.rideaustin.ui.drawer.dc.DirectConnectActivity;
import com.rideaustin.ui.drawer.donate.DonateFragment;
import com.rideaustin.ui.drawer.editprofile.EditProfileFragment;
import com.rideaustin.ui.drawer.promotions.PromotionsActivity;
import com.rideaustin.ui.drawer.settings.SettingsFragment;
import com.rideaustin.ui.drawer.triphistory.SupportTopicsModel;
import com.rideaustin.ui.drawer.triphistory.TripHistoryFragment;
import com.rideaustin.ui.drawer.triphistory.TripHistoryModel;
import com.rideaustin.ui.drawer.triphistory.forms.SupportFormsModel;
import com.rideaustin.ui.driver.DriverActivity;
import com.rideaustin.ui.driver.FemaleOnlyInfoFragment;
import com.rideaustin.ui.driver.FingerprintedOnlyInfoFragment;
import com.rideaustin.ui.map.MainMapFragment;
import com.rideaustin.ui.map.UnmetRequirementType;
import com.rideaustin.ui.map.address.AddressSelectionFragment;
import com.rideaustin.ui.payment.AddPaymentFragment;
import com.rideaustin.ui.payment.EditPaymentFragment;
import com.rideaustin.ui.payment.PaymentFragment;
import com.rideaustin.ui.payment.UnpaidFragment;
import com.rideaustin.ui.signin.SplashActivity;
import com.rideaustin.ui.signin.StartupError;
import com.rideaustin.ui.splitfare.FareSplitFragment;
import com.rideaustin.ui.utils.MaterialDialogCreator;
import com.rideaustin.utils.CommonConstants;
import com.rideaustin.utils.Constants;
import com.rideaustin.utils.DialogUtils;
import com.rideaustin.utils.ImageHelper;
import com.rideaustin.utils.KeyboardUtil;
import com.rideaustin.utils.NetworkHelper;
import com.rideaustin.utils.ObjectUtils;
import com.rideaustin.utils.PermissionUtils;
import com.rideaustin.utils.SerializationHelper;
import com.rideaustin.utils.toast.RAToast;
import com.tbruyelle.rxpermissions.RxPermissions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.branch.referral.Branch;
import java8.util.Optional;
import java8.util.stream.StreamSupport;
import rx.Subscription;
import rx.subscriptions.CompositeSubscription;
import rx.subscriptions.Subscriptions;
import timber.log.Timber;

import static com.rideaustin.utils.CommonConstants.UNEXPECTED_STATE_KEY;

public class NavigationDrawerActivity extends BaseRiderActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        NavigationDrawerHeaderViewModel.View,
        PaymentFragment.EventListener,
        AddPaymentFragment.EventListener {

    private static Set<String> rootFragmentClassesInBackStack = new HashSet<>();
    private MainMapFragment mainMapFragment;
    private String currentFragmentTag;

    private com.rideaustin.databinding.NavigationDrawerHeaderBinding navigationDrawerHeaderBinding;
    private ActionBarDrawerToggle toggle;
    private ActivityNavigationDrawerBinding binding;
    private NavigationDrawerEventListener listener;
    private CompositeSubscription subscriptions = new CompositeSubscription();
    private Subscription permissionSubscription = Subscriptions.empty();
    private Subscription checkTokenSubscription = Subscriptions.empty();
    private List<Target> menuIconTargets = new ArrayList<>();

    private NavigationDrawerHeaderViewModel viewModel;
    private MaterialDialog accessibilityDialog;
    private View mapPlaceholder;
    private boolean goBackOnToolbarIcon;

    public static void addRootFragmentClass(String rootFragmentClass) {
        if (rootFragmentClass != null) {
            rootFragmentClassesInBackStack.add(rootFragmentClass);
        }
    }

    public static void removeRootFragmentClass(String rootFragmentClass) {
        if (rootFragmentClass != null) {
            rootFragmentClassesInBackStack.remove(rootFragmentClass);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        readDeepLink(getIntent());

        // RA-12047: this activity is launcher now
        // if user is authorized, we will try to start
        // using cached data and sync with server after
        // If some conditions not met, startup is interrupted and app goes to splash.
        // Be careful, putting logic in onStart() - check if activity is initialized
        if (!App.getDataManager().isAuthorised()) {
            // no token, go to splash
            super.onCreate(null);
            onGotoSplash();
            return;
        }
        // app has token, but data not loaded, trying fast run
        boolean fastRun = App.getDataManager().getCurrentUser() == null;
        if (fastRun) {
            if (!NetworkHelper.isNetworkAvailable()) {
                // no network, may have problems, better return to splash
                super.onCreate(null);
                App.getDataManager().setStartupError(StartupError.NO_NETWORK);
                onGotoSplash();
                return;
            }
            App.getDataManager().restoreData();
            if (App.getDataManager().getCurrentUser() == null) {
                // no data persisted, return to splash
                super.onCreate(null);
                onGotoSplash();
                return;
            }
        }
        super.onCreate(savedInstanceState);
        // need animation only if its not a launcher intent
        boolean needAnimation = getIntent().getAction() == null;
        if (!needAnimation) {
            overridePendingTransition(0, 0);
        }
        binding = DataBindingUtil.setContentView(this, R.layout.activity_navigation_drawer);
        if (fastRun) {
            showPlaceholder();
        }

        setToolbar(binding.toolbar);
        listener = new NavigationDrawerEventListener(this);
        toggle = new ActionBarDrawerToggle(this, binding.drawerLayout, binding.toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            @Override
            public void onDrawerStateChanged(int newState) {
                if (newState == DrawerLayout.STATE_SETTLING) {
                    //Check driver item visibility
                    if (App.getDataManager().getCurrentUser() != null) {
                        List<Avatar> avatars = App.getDataManager().getCurrentUser().getAvatars();
                        boolean isVisible = true;
                        if (avatars != null) {
                            for (Avatar item : avatars) {
                                if (item.isDriver()) {
                                    isVisible = false;
                                    break;
                                }
                            }
                            Menu menu = binding.navigationView.getMenu();
                            MenuItem menuItem = menu.findItem(R.id.navDriveWithRideApp);
                            if (menuItem != null) {
                                menuItem.setVisible(isVisible);
                            }
                            MenuItem promoCode = menu.findItem(R.id.navPromotions);

                            if (promoCode != null) {
                                promoCode.setVisible(!App.getPrefs().hasRideId());
                            }

                            updateDirectConnect(App.getConfigurationManager().getLastConfiguration());
                            updateCampaignProviders(App.getConfigurationManager().getLastConfiguration());

                            invalidateOptionsMenu();
                        }
                    }
                } else if (newState == DrawerLayout.STATE_IDLE) {
                    KeyboardUtil.hideKeyBoard(NavigationDrawerActivity.this, binding.rootView);
                }
            }
        };
        binding.toolbar.setNavigationOnClickListener(v -> {
            if (toggle.isDrawerIndicatorEnabled() && !goBackOnToolbarIcon) {
                int drawerLockMode = binding.drawerLayout.getDrawerLockMode(GravityCompat.START);
                if (binding.drawerLayout.isDrawerVisible(GravityCompat.START)
                        && (drawerLockMode != DrawerLayout.LOCK_MODE_LOCKED_OPEN)) {
                    binding.drawerLayout.closeDrawer(GravityCompat.START);
                } else if (drawerLockMode != DrawerLayout.LOCK_MODE_LOCKED_CLOSED) {
                    binding.drawerLayout.openDrawer(GravityCompat.START);
                }
            } else {
                onMenuToggleClick();
            }
        });

        binding.navigationView.setItemIconTintList(null);
        binding.drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        binding.navigationView.setNavigationItemSelectedListener(this);
        navigationDrawerHeaderBinding = DataBindingUtil.inflate(getLayoutInflater(), R.layout.navigation_drawer_header, binding.rootView, false);
        viewModel = new NavigationDrawerHeaderViewModel(this);
        viewModel.onCreate();
        navigationDrawerHeaderBinding.setNavigationDrawerHeaderViewModel(viewModel);
        binding.navigationView.addHeaderView(navigationDrawerHeaderBinding.getRoot());
        handleMenuItemVisibility(binding.navigationView.getMenu());

        restoreMainMapFragment();
        replaceMapFragment(Transition.NONE);

        handleIntent(getIntent());
        executeDeepLink();
        App.getDataManager().startServerEventsListening();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        this.setIntent(intent);
        Timber.d("::onNewIntent::");
        readDeepLink(intent);
        handleIntent(intent);
        executeDeepLink();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (viewModel != null) {
            // RA-12047: Activity can be in "going-to-splash" state
            // see onCreate() for details about "going-to-splash"
            subscribeToConfigurationUpdates();
        }

        // Branch init
        Branch.getInstance().initSession((referringParams, error) -> {
            if (error == null) {
                if (referringParams != null) {
                    // params are the deep linked params associated with the link that the user clicked -> was re-directed to this app
                    // params will be empty if no data found
                    Timber.i("BRANCH SDK:" + referringParams.toString());
                }
                if (referringParams != null && referringParams.has("~channel")) {
                    CampaignParams params = new CampaignParams();
                    params.utmSource = referringParams.optString("~channel", null);
                    params.promoCode = referringParams.optString("promo_code", null);
                    params.marketingTitle = referringParams.optString("$marketing_title", null);
                    params.utmMedium = referringParams.optString("~feature", null);
                    params.utmCampaign = referringParams.optString("~campaign", null);
                    App.getDataManager().campaignParams = params;
                } else {
                    App.getDataManager().campaignParams = null;
                }

            } else {
                Timber.i("BRANCH SDK:" + error.getMessage());
            }
        }, this.getIntent().getData(), this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (viewModel != null) {
            // RA-12047: Activity can be in "going-to-splash" state
            // see onCreate() for details about "going-to-splash"
            // RA-12323: Somehow onResume() is also called, but should not
            // if activity is being destroyed during onCreate()
            viewModel.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (viewModel != null) {
            // RA-12047: Activity can be in "going-to-splash" state
            // see onCreate() for details about "going-to-splash"
            // RA-12323: Somehow onResume() is also called, but should not
            // if activity is being destroyed during onCreate()
            // Probably onPause() may also be called
            viewModel.onPause();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        subscriptions.clear();
        for (Target target : menuIconTargets) {
            Glide.with(App.getInstance()).clear(target);
        }
        menuIconTargets.clear();
    }

    public void showPlaceholder() {
        mapPlaceholder = getLayoutInflater().inflate(R.layout.view_startup, null);
        binding.topView.addView(mapPlaceholder);
    }

    public void hidePlaceholder() {
        if (hasPlaceholder()) {
            Animation a = AnimationUtils.loadAnimation(this, R.anim.placeholder);
            a.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    if (binding != null) {
                        binding.topView.removeAllViews();
                    }
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
            mapPlaceholder.startAnimation(a);
            mapPlaceholder = null;
        }
    }

    public boolean hasPlaceholder() {
        return mapPlaceholder != null;
    }

    @Override
    protected boolean needToHandlePermissionRequest() {
        return false; // map fragment ask for permission itself
    }

    private void subscribeToConfigurationUpdates() {
        final Menu menu = binding.navigationView.getMenu();
        subscriptions.add(App.getConfigurationManager()
                .getConfigurationUpdates()
                .observeOn(RxSchedulers.main())
                .subscribe(globalConfig -> {
                    final MenuItem item = menu.findItem(R.id.navDriveWithRideApp);
                    if (item != null) {
                        item.setTitle(getString(R.string.drive_with_ride_city, App.getAppName()));
                    }
                    final MenuItem accessibilityOption = menu.findItem(R.id.navAccessibility);
                    boolean accessibilityEnabled = Optional.ofNullable(globalConfig.getAccessibility())
                            .map(Accessibility::getEnabled).orElse(false);
                    if (accessibilityEnabled) {
                        accessibilityOption.setVisible(true);
                    } else {
                        accessibilityOption.setVisible(false);
                    }
                    final MenuItem womanOnlyOption = menu.findItem(R.id.navFemaleOnly);
                    if (womanOnlyOption != null) {
                        Optional<RequestedDriverType> driverType = globalConfig.getFemaleDriverType();
                        boolean visible = driverType.isPresent();
                        womanOnlyOption.setVisible(visible);
                        if (visible) {
                            String title = driverType.map(RequestedDriverType::getDisplayTitle).orElse(getString(R.string.menu_female_only_mode));
                            womanOnlyOption.setTitle(title);
                        }
                    }
                    final MenuItem fingerprintedOnlyOption = menu.findItem(R.id.navFingerprintedOnly);
                    if (fingerprintedOnlyOption != null) {
                        Optional<RequestedDriverType> driverType = globalConfig.getFingerprintedDriverType();
                        boolean visible = driverType.isPresent();
                        fingerprintedOnlyOption.setVisible(visible);
                        if (visible) {
                            String title = driverType.map(RequestedDriverType::getDisplayTitle).orElse(getString(R.string.menu_fingerprinted_only_mode));
                            fingerprintedOnlyOption.setTitle(title);
                        }
                    }
                    updateDirectConnect(globalConfig);
                    updateCampaignProviders(globalConfig);
                }, Timber::e));
    }

    private void updateDirectConnect(GlobalConfig config) {
        final Menu menu = binding.navigationView.getMenu();
        final MenuItem menuItem = menu.findItem(R.id.navDirectConnect);
        if (App.getPrefs().hasRideId()) {
            menuItem.setVisible(false);
            return;
        }
        if (mainMapFragment.getMapViewModel().getUnmetRequirementType() != UnmetRequirementType.NONE) {
            menuItem.setVisible(false);
            return;
        }

        final DirectConnectConfig directConnect = config.getDirectConnectConfig();
        if (directConnect != null) {
            if (directConnect.getEnabled()) {
                menuItem.setTitle(directConnect.getTitle());
                menuItem.setVisible(true);
            } else {
                menuItem.setVisible(false);
            }
        } else {
            menuItem.setVisible(false);
        }
    }

    private void updateCampaignProviders(GlobalConfig config) {
        final Menu menu = binding.navigationView.getMenu();
        while (menu.findItem(R.id.navCampaign) != null) {
            menu.removeItem(R.id.navCampaign);
        }
        int order = menu.findItem(R.id.navDirectConnect).getOrder();
        for (CampaignProvider campaignProvider : config.getCampaignProviders()) {
            MenuItem item = menu.add(Menu.NONE, R.id.navCampaign, order, campaignProvider.getMenuTitle());
            if (TextUtils.isEmpty(campaignProvider.getMenuIcon())) {
                item.setIcon(R.drawable.cap_metro);
            } else {
                menuIconTargets.add(ImageHelper.loadImageIntoMenuItem(this, item, campaignProvider.getMenuIcon()));
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (viewModel != null) {
            // RA-12047: Activity can be in "going-to-splash" state
            // see onCreate() for details about "going-to-splash"
            viewModel.onDestroy();
        }
        App.getDataManager().stopServerEventsListening();
        permissionSubscription.unsubscribe();
        checkTokenSubscription.unsubscribe();
    }

    private void handleMenuItemVisibility(Menu menu) {
        User currentUser = App.getDataManager().getCurrentUser();
        if (currentUser != null && !currentUser.isActive()) {
            menu.removeItem(R.id.navPayment);
            menu.removeItem(R.id.navDonate);
            menu.removeItem(R.id.navPromotions);
            menu.removeItem(R.id.navDriveWithRideApp);
        }
    }


    public void setToolbarIndicatorEnabled(boolean enabled) {
        toggle.setDrawerIndicatorEnabled(enabled);
    }

    @Override
    public void onMenuToggleClick() {
        Fragment fragmentByTag = getSupportFragmentManager().findFragmentByTag(currentFragmentTag);
        if (fragmentByTag instanceof BackPressable) {
            BackPressable backPressableFragment = (BackPressable) fragmentByTag;
            boolean isConsumed = backPressableFragment.onBackPressed();
            if (!isConsumed) {
                goBack();
            }
        } else {
            goBack();
        }
    }

    @Override
    public void onBackPressed() {
        Fragment fragmentByTag = getSupportFragmentManager().findFragmentByTag(currentFragmentTag);
        if (fragmentByTag instanceof BackPressable) {
            BackPressable backPressableFragment = (BackPressable) fragmentByTag;
            boolean isConsumed = backPressableFragment.onBackPressed();
            if (!isConsumed) {
                backButtonPressed();
            }
        } else {
            backButtonPressed();
        }
    }

    private void backButtonPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            goBack();
        }
    }

    private void goBack() {
        String fragmentTag = getSupportFragmentManager().getBackStackEntryAt(getSupportFragmentManager().getBackStackEntryCount() - 1).getName();
        if (currentFragmentTag.equals(MainMapFragment.class.getName())) {
            Timber.d("::onMenuToggleClick:: Finishing activity.");
            finish();
        } else if (fragmentTag.equals(AddressSelectionFragment.class.getName())) {
            hideAddressSelection();
        } else if (rootFragmentClassesInBackStack.contains(fragmentTag)) {
            Timber.d("::onMenuToggleClick:: Replacing with map fragment");
            replaceMapFragment(Transition.BACKWARD);
            KeyboardUtil.hideKeyBoard(this, binding.rootView);
        } else if (!isInvalid()) {
            Timber.d("::onMenuToggleClick:: Popping fragment");
            getSupportFragmentManager().popBackStackImmediate();
            KeyboardUtil.hideKeyBoard(this, binding.rootView);
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.navCampaign) {
            return navigateToCampaignProvider(item.getTitle().toString());
        } else {
            return navigateTo(item.getItemId());
        }
    }

    public void showAddressSelection(boolean isDestination) {
        if (isInvalid()) {
            // RA-13679: onClick can be called after onSavedInstanceState?
            return;
        }
        AddressSelectionFragment fragment = AddressSelectionFragment.newInstance(isDestination);
        fragment.setMapViewModel(mainMapFragment.getMapViewModel());

        currentFragmentTag = fragment.getClass().getName();
        getSupportFragmentManager().beginTransaction()
                .hide(mainMapFragment)
                .add(R.id.rootView, fragment, currentFragmentTag)
                .addToBackStack(currentFragmentTag)
                .commit();
        showToolbarBackArrow(true);
    }

    public void hideAddressSelection() {
        clearMenuBackstack();
        setToolbarTitle(App.getFormattedAppName());
        currentFragmentTag = mainMapFragment.getClass().getName();
        KeyboardUtil.hideKeyBoard(this, binding.rootView);
        showToolbarBackArrow(false);
    }

    private void showToolbarBackArrow(boolean show) {
        ValueAnimator animator = show
                ? ValueAnimator.ofFloat(0, 1)
                : ValueAnimator.ofFloat(1, 0);
        animator.addUpdateListener(valueAnimator -> {
            float slideOffset = (Float) valueAnimator.getAnimatedValue();
            toggle.onDrawerSlide(binding.drawerLayout, slideOffset);
        });

        animator.setInterpolator(new DecelerateInterpolator());
        animator.setDuration(250);
        animator.start();

        binding.drawerLayout.setDrawerLockMode(show
                ? DrawerLayout.LOCK_MODE_LOCKED_CLOSED
                : DrawerLayout.LOCK_MODE_UNLOCKED);
        goBackOnToolbarIcon = show;
    }

    private void replaceSettingsFragment() {
        if (isInvalid() || isCurrentFragment(SettingsFragment.class)) {
            return;
        }
        toggle.setDrawerIndicatorEnabled(false);
        if (isPreviousFragment(EditProfileFragment.class)) {
            getSupportFragmentManager().popBackStackImmediate();
            return;
        }
        clearMenuBackstack();
        getSupportFragmentManager().executePendingTransactions();
        replaceFragment(new SettingsFragment(), R.id.rootView, false);
    }

    private void replaceDonateFragment() {
        if (isInvalid() || isCurrentFragment(DonateFragment.class)) {
            return;
        }
        toggle.setDrawerIndicatorEnabled(false);
        clearMenuBackstack();
        getSupportFragmentManager().executePendingTransactions();
        replaceFragment(new DonateFragment(), R.id.rootView, false);
    }

    private void replacePaymentFragment(final Payment payment) {
        if (isInvalid() || isCurrentFragment(PaymentFragment.class)) {
            return;
        }
        toggle.setDrawerIndicatorEnabled(false);

        if (isPreviousFragment(AddPaymentFragment.class) || isPreviousFragment(UnpaidFragment.class)) {
            getSupportFragmentManager().popBackStackImmediate();
            PaymentFragment paymentFragment = (PaymentFragment) getFragment(R.id.rootView);
            paymentFragment.setPayment(payment);
            paymentFragment.setEventListener(this);
            return;
        }
        clearMenuBackstack();
        getSupportFragmentManager().executePendingTransactions();
        PaymentFragment paymentFragment = new PaymentFragment();
        paymentFragment.setPayment(payment);
        paymentFragment.setEventListener(this);
        NavigationDrawerActivity.addRootFragmentClass(paymentFragment.getClass().getName());
        replaceFragment(paymentFragment, R.id.rootView, false);
    }

    private void replacePaymentFragment() {
        replacePaymentFragment(null);
    }

    public void replacePaymentFragment(boolean addToBackstack) {
        if (isInvalid() || isCurrentFragment(AddPaymentFragment.class)) {
            return;
        }
        toggle.setDrawerIndicatorEnabled(false);
        PaymentFragment paymentFragment = new PaymentFragment();
        paymentFragment.setEventListener(this);
        removeRootFragmentClass(paymentFragment.getClass().getName());
        replaceFragment(paymentFragment, R.id.rootView, addToBackstack);
    }

    private void replaceAddPaymentFragment(boolean addToBackstack) {
        if (isInvalid() || isCurrentFragment(AddPaymentFragment.class)) {
            return;
        }
        toggle.setDrawerIndicatorEnabled(false);
        AddPaymentFragment addPaymentFragment = new AddPaymentFragment();
        addPaymentFragment.setEventListener(this);
        replaceFragment(addPaymentFragment, R.id.rootView, addToBackstack);
    }

    private void replaceEditPaymentFragment(final Payment payment) {
        if (isInvalid() || isCurrentFragment(EditPaymentFragment.class)) {
            return;
        }
        toggle.setDrawerIndicatorEnabled(false);
        EditPaymentFragment editPaymentFragment = new EditPaymentFragment();
        editPaymentFragment.setPayment(payment);
        replaceFragment(editPaymentFragment, R.id.rootView, true);
    }

    private void replaceUnpaidFragment() {
        if (isInvalid() || isCurrentFragment(UnpaidFragment.class)) {
            return;
        }
        toggle.setDrawerIndicatorEnabled(false);
        UnpaidFragment fragment = new UnpaidFragment();
        replaceFragment(fragment, R.id.rootView, true);
    }

    private void replaceTripHistoryFragment() {
        if (isInvalid() || isCurrentFragment(TripHistoryFragment.class)) {
            return;
        }
        clearMenuBackstack();
        toggle.setDrawerIndicatorEnabled(false);
        App.getDataManager().setTripHistoryModel(new TripHistoryModel(this));
        App.getDataManager().setSupportTopicsModel(new SupportTopicsModel(this));
        App.getDataManager().setSupportFormsModel(new SupportFormsModel(this));
        replaceFragment(new TripHistoryFragment(), R.id.rootView, false);
    }

    void replaceSplitFare() {
        if (!App.getDataManager().isSplitFareEnabled()) {
            RAToast.showShort(App.getDataManager().getSplitFareDisabledMessage());
            return;
        }

        if (isInvalid() || isCurrentFragment(FareSplitFragment.class)) {
            return;
        }

        toggle.setDrawerIndicatorEnabled(false);
        replaceFragment(new FareSplitFragment(), R.id.rootView, false);
    }

    public void replaceMapFragment(Transition transition) {
        if (isInvalid()) {
            return;
        }
        navigationDrawerHeaderBinding.getNavigationDrawerHeaderViewModel().setUser(App.getDataManager().getCurrentUser());
        setToolbarIndicatorEnabled(true);
        setToolbarTitle(App.getFormattedAppName());
        addRootFragmentClass(MainMapFragment.class.getName());
        if (mainMapFragment == null) {
            initMainMapFragment(new MainMapFragment());
            replaceFragment(mainMapFragment, R.id.mapRootView, true, transition);
        } else {
            Fragment currentFragment = getFragment(R.id.rootView);
            if (currentFragment != null) {
                removeFragment(currentFragment, transition);
            }
            showFragment(mainMapFragment, transition);
            currentFragmentTag = mainMapFragment.getClass().getName();
        }
        // destroy models, if for some reason they were not destroyed
        App.getDataManager().setTripHistoryModel(null);
        App.getDataManager().setSupportTopicsModel(null);
        App.getDataManager().setSupportFormsModel(null);
        // check round up after animation complete and map in foreground
        startWithDelay(() -> mainMapFragment.checkRoundUp(false), 1000);
    }

    private void restoreMainMapFragment() {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(MainMapFragment.class.getName());
        if (fragment instanceof MainMapFragment) {
            initMainMapFragment((MainMapFragment) fragment);
        }
    }

    private void initMainMapFragment(MainMapFragment fragment) {
        mainMapFragment = fragment;
        mainMapFragment.setListener(listener);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        if (fragments != null) {
            for (Fragment fragment : fragments) {
                fragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MainMapFragment.LOCATION_RESOLUTION_REQUEST) {
            if (mainMapFragment != null && mainMapFragment.isVisible()) {
                mainMapFragment.onLocationResolutionResult(resultCode, data);
            }
        }
    }

    private void replaceFingerprintedOnlyDriverFragment() {
        if (isInvalid() || isCurrentFragment(FingerprintedOnlyInfoFragment.class)) {
            return;
        }
        clearMenuBackstack();
        toggle.setDrawerIndicatorEnabled(false);
        FingerprintedOnlyInfoFragment fingerprintOnlyFragment = new FingerprintedOnlyInfoFragment();
        fingerprintOnlyFragment.setListener(enabled -> mainMapFragment.setFingerprintedOnlyEnabled(enabled));
        replaceFragment(fingerprintOnlyFragment, R.id.rootView, false);
    }

    private void replaceFemaleOnlyDriverFragment() {
        if (isInvalid() || isCurrentFragment(FemaleOnlyInfoFragment.class)) {
            return;
        }
        clearMenuBackstack();
        toggle.setDrawerIndicatorEnabled(false);
        FemaleOnlyInfoFragment femaleOnlyFragment = FemaleOnlyInfoFragment.getInstance();
        femaleOnlyFragment.setListener(enabled -> mainMapFragment.setFemaleOnlyEnabled(enabled));
        replaceFragment(femaleOnlyFragment, R.id.rootView, false);
    }


    private void clearMenuBackstack() {
        if (mainMapFragment != null) {
            clearBackstack(MainMapFragment.class.getName());
        }
    }

    @Override
    public void replaceFragment(Fragment f, int container, boolean addToStack) {
        super.replaceFragment(f, container, addToStack);
        if (isInvalid()) {
            return;
        }
        if (mainMapFragment != null && !MainMapFragment.class.getName().equals(f.getClass().getName())) {
            hideFragment(mainMapFragment);
        }
        currentFragmentTag = f.getClass().getName();
        KeyboardUtil.hideKeyBoard(this, binding.rootView);
    }

    @Override
    public void replaceFragment(Fragment f, int container, boolean addToStack, Transition transition) {
        super.replaceFragment(f, container, addToStack, transition);
        if (isInvalid()) {
            return;
        }
        if (mainMapFragment != null && !MainMapFragment.class.getName().equals(f.getClass().getName())) {
            hideFragment(mainMapFragment);
        }
        currentFragmentTag = f.getClass().getName();
        KeyboardUtil.hideKeyBoard(this, binding.rootView);
    }

    @Override
    public boolean navigateTo(@IdRes int id) {
        switch (id) {
            case R.id.navPayment:
                if (!App.getDataManager().hasPaymentMethods()) {
                    startWithDelay(() -> replaceAddPaymentFragment(false));
                } else {
                    startWithDelay(this::replacePaymentFragment);
                }
                break;
            case R.id.navDriveWithRideApp:
                Intent intent = new Intent(this, DriverActivity.class);
                intent.putExtra(DriverActivity.KEY_OPEN_VIA_NAVIGATION_DRAWER, true);
                startWithDelay(intent);
                break;
            case R.id.navDonate:
                startWithDelay(this::replaceDonateFragment);
                break;
            case R.id.navPromotions:
                startWithDelay(new Intent(this, PromotionsActivity.class));
                break;
            case R.id.navSettings:
                startWithDelay(this::replaceSettingsFragment);
                break;
            case R.id.navUnpaid:
                startWithDelay(this::replaceUnpaidFragment);
                break;
            case R.id.navAccessibility:
                startWithDelay(this::createCallAccessibilityDialog);
                break;
            case R.id.navFingerprintedOnly:
                startWithDelay(this::replaceFingerprintedOnlyDriverFragment);
                break;
            case R.id.navFemaleOnly:
                startWithDelay(this::replaceFemaleOnlyDriverFragment);
                break;
            case R.id.navHistory:
                startWithDelay(this::replaceTripHistoryFragment);
                break;
            case R.id.navDirectConnect:
                startWithDelay(new Intent(this, DirectConnectActivity.class));
                break;
            case R.id.navMap:
            default:
                startWithDelay(() -> replaceMapFragment(Transition.BACKWARD));
                break;
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private boolean navigateToCampaignProvider(String name) {
        binding.drawerLayout.closeDrawer(GravityCompat.START);
        return StreamSupport.stream(Optional.ofNullable(App.getConfigurationManager().getLastConfiguration())
                .map(GlobalConfig::getCampaignProviders)
                .orElse(Collections.emptyList()))
                .filter(provider -> ObjectUtils.equals(provider.getMenuTitle(), name))
                .findAny()
                .map(provider -> {
                    startWithDelay(CampaignDetailsActivity.getInstance(this, provider));
                    return true;
                })
                .orElse(false);
    }

    private void startWithDelay(Intent intent) {
        // add small delay to prevent animation twitch
        RxSchedulers.schedule(() -> startActivity(intent), CommonConstants.ANIMATION_DELAY_MS, TimeUnit.MILLISECONDS);
    }

    private void startWithDelay(Runnable runnable) {
        // add small delay to prevent animation twitch
        RxSchedulers.schedule(runnable, CommonConstants.ANIMATION_DELAY_MS, TimeUnit.MILLISECONDS);
    }

    private void startWithDelay(Runnable runnable, long delayMs) {
        RxSchedulers.schedule(runnable, delayMs, TimeUnit.MILLISECONDS);
    }

    @SuppressLint("MissingPermission")
    private void createCallAccessibilityDialog() {
        DialogUtils.dismiss(accessibilityDialog);
        Accessibility accessibility = App.getConfigurationManager().getLastConfiguration().getAccessibility();
        accessibilityDialog = MaterialDialogCreator.createDialogWithCallback(this,
                getString(R.string.accessibility_dialog_title),
                accessibility.getTitle(),
                R.string.btn_call, (dialog, which) -> makeAccessibilityCall())
                .show();
    }

    private void makeAccessibilityCall() {
        Accessibility accessibility = App.getConfigurationManager().getLastConfiguration().getAccessibility();
        String permission = Manifest.permission.CALL_PHONE;
        permissionSubscription.unsubscribe();
        permissionSubscription = new RxPermissions(this)
                .request(Manifest.permission.CALL_PHONE)
                .subscribe(granted -> {
                    if (granted) {
                        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse(getString(R.string.scheme_telephone) + accessibility.getPhoneNumber()));
                        if (intent.resolveActivity(getPackageManager()) != null) {
                            startActivity(intent);
                        } else {
                            RAToast.showShort(R.string.cant_find_app_to_call);
                            RAToast.showShort(accessibility.getPhoneNumber());
                        }
                        DialogUtils.dismiss(accessibilityDialog);
                    } else {
                        PermissionUtils.checkDeniedPermissions(this, permission);
                    }
                }, throwable -> {
                    Timber.e(throwable);
                    RAToast.showShort(R.string.error_unknown);
                    DialogUtils.dismiss(accessibilityDialog);
                });

    }

    private void readDeepLink(Intent intent) {
        Optional.ofNullable(intent)
                .map(Intent::getDataString)
                .filter(it -> it.startsWith("rideaustin://"))
                .ifPresent(it -> {
                    Pattern pattern = Pattern.compile("rideaustin://requestToken=(.+)$");
                    Matcher matcher = pattern.matcher(it);
                    if (matcher.find()) {
                        App.getDataManager().setRequestToken(matcher.group(1));
                    }
                });
    }

    private void executeDeepLink() {
        checkTokenSubscription.unsubscribe();
        checkTokenSubscription = App.getDataManager().checkRequestToken();
    }

    private void handleIntent(Intent intent) {
        Timber.d("::handleIntent::");
        if (intent.hasExtra(CommonConstants.EXTRA_KEY_NOTIFICATION_MESSAGE)) {
            InAppMessage message = SerializationHelper.deSerialize(intent.getStringExtra(CommonConstants.EXTRA_KEY_NOTIFICATION_MESSAGE), InAppMessage.class);
            if (message != null) {
                viewModel.showMessage(message);
            }
        } else if (intent.hasExtra(Constants.EXTRA_KEY_SPLIT_FARE_MESSAGE)) {
            SplitFareMessage message = SerializationHelper.deSerialize(intent.getStringExtra(Constants.EXTRA_KEY_SPLIT_FARE_MESSAGE), SplitFareMessage.class);
            if (message != null) {
                viewModel.showMessage(message);
            }
        } else if (intent.hasExtra(Constants.EXTRA_KEY_RETURN_TO_MAP)) {
            RxSchedulers.schedule(this::returnToMap);
        }
    }

    private void returnToMap() {
        if (isInvalid()) return;
        restoreMainMapFragment();
        if (mainMapFragment == null) {
            return;
        }
        getSupportFragmentManager().popBackStackImmediate(MainMapFragment.class.getName(), 0);
        replaceMapFragment(Transition.NONE);
    }

    @Override
    public void onGotoSplash() {
        // open splash activity and process StartupError
        Intent intent = new Intent(this, SplashActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    protected void onInAppMessage(InAppMessage message) {
        if (message instanceof SplitFareMessage) {
            SplitFareMessage splitFareMessage = (SplitFareMessage) message;
            switch (splitFareMessage.getType()) {
                case Constants.SplitFare.REQUESTED:
                    onSplitFareRequest(splitFareMessage);
                    break;
                case Constants.SplitFare.ACCEPTED:
                    onSplitFareAccepted(splitFareMessage);
                    break;
                case Constants.SplitFare.DECLINED:
                    onSplitFareDeclined(splitFareMessage);
                    break;
                default:
                    Exception e = new Exception(UNEXPECTED_STATE_KEY);
                    Timber.e(e, "Unknown split fare key: " + splitFareMessage.getType());
            }
        } else {
            super.onInAppMessage(message);
        }
    }

    private void onSplitFareRequest(SplitFareMessage message) {
        MaterialDialogCreator.createMessageWithRoundedPicDialog(this, message.getSourceUserPhoto(),
                getString(R.string.fare_split_accept_message, message.getSourceUser()))
                .positiveText(getString(R.string.fare_split_accept_btn))
                .negativeText(getString(R.string.fare_split_decline_btn))
                .onPositive((dialog, which) -> {
                    viewModel.acceptSplitFare(message, true);
                    dialog.dismiss();
                })
                .onNegative((dialog, which) -> {
                    viewModel.acceptSplitFare(message, false);
                    dialog.dismiss();
                })
                .dismissListener(dialog -> viewModel.readMessage(message))
                .cancelable(false)
                .canceledOnTouchOutside(false)
                .show();
    }

    private void onSplitFareAccepted(SplitFareMessage message) {
        MaterialDialogCreator.createMessageWithRoundedPicDialog(this, message.getTargetUserPhoto(),
                getString(R.string.fare_split_notification_accept_message, message.getTargetUser()))
                .title(R.string.fare_split_notification_accept_title)
                .positiveText(R.string.btn_ok)
                .autoDismiss(true)
                .dismissListener(dialog -> viewModel.readMessage(message))
                .show();
    }

    private void onSplitFareDeclined(SplitFareMessage message) {
        MaterialDialogCreator.createMessageWithRoundedPicDialog(this, message.getTargetUserPhoto(),
                getString(R.string.fare_split_notification_declined_message, message.getTargetUser()))
                .title(R.string.fare_split_notification_declined_title)
                .positiveText(R.string.btn_ok)
                .autoDismiss(true)
                .dismissListener(dialog -> viewModel.readMessage(message))
                .show();
    }

    MainMapFragment getMainMapFragment() {
        return mainMapFragment;
    }

    boolean isCurrentFragment(Class<? extends Fragment> fragmentClass) {
        Fragment fragment = getFragment(R.id.rootView);
        return fragment != null && fragmentClass.getName().equals(fragment.getTag());
    }

    boolean isPreviousFragment(Class<? extends Fragment> fragmentClass) {
        int count = getSupportFragmentManager().getBackStackEntryCount();
        if (count > 0) {
            String fragmentTag = getSupportFragmentManager().getBackStackEntryAt(count - 1).getName();
            return fragmentClass.getName().equals(fragmentTag);
        }
        return false;
    }

    @Override
    public void onAddPayment() {
        replaceAddPaymentFragment(true);
    }

    @Override
    public void onEditPayment(Payment payment) {
        replaceEditPaymentFragment(payment);
    }

    @Override
    public void onAddPaymentAdded(Payment payment) {
        replacePaymentFragment(payment);
    }
}
