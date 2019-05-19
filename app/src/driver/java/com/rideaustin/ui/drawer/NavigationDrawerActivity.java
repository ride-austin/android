package com.rideaustin.ui.drawer;

import android.Manifest;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;
import com.rideaustin.App;
import com.rideaustin.BuildConfig;
import com.rideaustin.R;
import com.rideaustin.api.config.DirectConnectConfig;
import com.rideaustin.api.config.GlobalConfig;
import com.rideaustin.api.config.ReferFriend;
import com.rideaustin.api.model.QueueResponse;
import com.rideaustin.api.model.driver.Driver;
import com.rideaustin.base.Transition;
import com.rideaustin.databinding.ActivityNavigationDrawerBinding;
import com.rideaustin.databinding.NavigationDrawerHeaderBinding;
import com.rideaustin.engine.EngineService;
import com.rideaustin.manager.notification.InAppMessage;
import com.rideaustin.schedulers.RxSchedulers;
import com.rideaustin.ui.base.FloatNavigatorBaseActivity;
import com.rideaustin.ui.directconnect.DirectConnectActivity;
import com.rideaustin.ui.drawer.queue.QueueActivity;
import com.rideaustin.ui.drawer.refer.ReferFriendActivity;
import com.rideaustin.ui.drawer.riderequest.RideRequestTypeActivity;
import com.rideaustin.ui.drawer.settings.SettingsActivity;
import com.rideaustin.ui.earnings.WeeklyEarningsActivity;
import com.rideaustin.ui.map.MainMapFragment;
import com.rideaustin.utils.CommonConstants;
import com.rideaustin.utils.Constants;
import com.rideaustin.utils.ImageHelper;
import com.rideaustin.utils.KeyboardUtil;
import com.rideaustin.utils.PermissionUtils;
import com.rideaustin.utils.SerializationHelper;
import com.rideaustin.utils.file.LogManager;
import com.rideaustin.utils.toast.RAToast;
import com.tbruyelle.rxpermissions.RxPermissions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import java8.util.Optional;
import rx.Subscription;
import rx.subscriptions.Subscriptions;
import timber.log.Timber;

public class NavigationDrawerActivity extends FloatNavigatorBaseActivity implements NavigationView.OnNavigationItemSelectedListener, NavigationDrawerHeaderViewModel.View {

    private MainMapFragment mainMapFragment;
    private NavigationDrawerHeaderBinding navigationDrawerHeaderBinding;
    private ActionBarDrawerToggle toggle;
    private ActivityNavigationDrawerBinding binding;
    private NavigationDrawerHeaderViewModel viewModel;
    private List<Target> menuIconTargets = new ArrayList<>();
    private LogManager logManager = new LogManager(App.getInstance());
    private Subscription permissionSubscription = Subscriptions.empty();

    private static final String KEY_LAUNCH_WITH_RIDE_REQUEST = "ride_request_launch";

    public static Intent getStartIntentForRideRequestLaunch() {
        Intent launcher = new Intent(App.getInstance(), NavigationDrawerActivity.class);
        launcher.putExtra(KEY_LAUNCH_WITH_RIDE_REQUEST, KEY_LAUNCH_WITH_RIDE_REQUEST);
        launcher.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        App.getInstance().startActivity(launcher);
        return launcher;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EngineService.start(this);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_navigation_drawer);
        setToolbar(binding.toolbar);

        toggle = new ActionBarDrawerToggle(this, binding.drawerLayout, binding.toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        binding.drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        binding.navigationView.setNavigationItemSelectedListener(this);
        navigationDrawerHeaderBinding = DataBindingUtil.inflate(getLayoutInflater(), R.layout.navigation_drawer_header, binding.rootView, false);
        viewModel = new NavigationDrawerHeaderViewModel(this);
        viewModel.setUser(App.getDataManager().getCurrentDriver());
        navigationDrawerHeaderBinding.setNavigationDrawerHeaderViewModel(viewModel);
        binding.navigationView.addHeaderView(navigationDrawerHeaderBinding.getRoot());

        setToolbarIndicatorEnabled(true);
        setToolbarTitle(App.getFormattedAppName());

        if (!BuildConfig.BETA_TESTING) {
            NavigationView navigationView = (NavigationView) findViewById(R.id.navigationView);
            Menu navMenu = navigationView.getMenu();
            if (navMenu != null) {
                navMenu.findItem(R.id.navReport).setVisible(false);
            }
        }

        restoreMapFragment();
        KeyboardUtil.hideKeyBoard(this, binding.rootView);
        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (intent.hasExtra(CommonConstants.EXTRA_KEY_NOTIFICATION_MESSAGE)) {
            InAppMessage message =  SerializationHelper.deSerialize(intent.getStringExtra(CommonConstants.EXTRA_KEY_NOTIFICATION_MESSAGE), InAppMessage.class);
            if (message != null) {
                viewModel.showMessage(message);
            }
        }
    }

    public void setToolbarIndicatorEnabled(boolean enabled) {
        toggle.setDrawerIndicatorEnabled(enabled);
    }

    @Override
    public void clearToolbarTitles() {
        super.clearToolbarTitles();
        getToolbarActionButton().setVisibility(View.GONE);
    }

    public Button getToolbarActionButton() {
        return binding.toolbarActionButton;
    }

    public ProgressBar getToolbarActionProgress() {
        return binding.toolbarActionProgress;
    }

    @Override
    public void hideMenu(@IdRes int id) {
        NavigationView navigationView = (NavigationView) findViewById(R.id.navigationView);
        Menu navMenu = null;
        if (navigationView != null) {
            navMenu = navigationView.getMenu();
            if (navMenu != null && navMenu.findItem(id) != null) {
                navMenu.findItem(id).setVisible(false);
            }
        }
    }

    @Override
    public void showMenu(@IdRes int id) {
        NavigationView navigationView = (NavigationView) findViewById(R.id.navigationView);
        Menu navMenu = null;
        if (navigationView != null) {
            navMenu = navigationView.getMenu();
            if (navMenu != null && navMenu.findItem(id) != null) {
                navMenu.findItem(id).setVisible(true);
            }
        }
    }

    public void settleQueueList(List<QueueResponse> queues) {
        //TODO: a better way to distinguish queue menus might be implemented.
        while (binding.navigationView.getMenu().findItem(R.id.navQueue) != null) {
            binding.navigationView.getMenu().removeItem(R.id.navQueue);
        }

        for (int i = 0; i < queues.size(); i++) {

            QueueResponse queue = queues.get(i);
            MenuItem item = binding.navigationView.getMenu().add(Menu.NONE, R.id.navQueue, Menu.CATEGORY_SECONDARY, queue.getAreaQueueName());

            if (TextUtils.isEmpty(queue.getIconUrl())) {
                item.setIcon(R.drawable.nav_airport);
            } else {
                menuIconTargets.add(ImageHelper.loadImageIntoMenuItem(this, item, queue.getIconUrl()));
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            // TODO: reset exit animation instead
            // and use super.onBackPressed()
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewModel.setUser(App.getDataManager().getCurrentDriver());
        viewModel.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (viewModel != null) {
            viewModel.onDestroy();
        }
        permissionSubscription.unsubscribe();
    }

    @Override
    protected void onPause() {
        super.onPause();
        viewModel.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        for (Target target : menuIconTargets) {
            Glide.with(this).clear(target);
        }
        menuIconTargets.clear();
    }

    private void restoreMapFragment() {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(MainMapFragment.class.getName());
        if (fragment instanceof MainMapFragment) {
            mainMapFragment = (MainMapFragment) fragment;
        } else {
            mainMapFragment = new MainMapFragment();
            replaceFragment(mainMapFragment, R.id.mapRootView, false, Transition.NONE);
        }
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
    public boolean onNavigationItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.navRefer:
                startWithDelay(new Intent(this, ReferFriendActivity.class));
                break;
            case R.id.navRide:
                if (App.getInstance().getStateManager().hasActiveRide()) {
                    RAToast.showLong(R.string.ride_request_change_error);
                } else {
                    startWithDelay(new Intent(this, RideRequestTypeActivity.class));
                }
                break;
            case R.id.navEarning:
                startWithDelay(new Intent(this, WeeklyEarningsActivity.class));
                break;
            case R.id.navSettings:
                startWithDelay(new Intent(this, SettingsActivity.class));
                break;
            case R.id.navQueue:
                startWithDelay(new Intent(this, QueueActivity.class).putExtra(Constants.QUEUE_NAME, item.getTitle().toString()));
                break;
            case R.id.navReport:
                sendReport();
                break;
            case R.id.navDirectConnect:
                startWithDelay(new Intent(this, DirectConnectActivity.class));
                break;
            default:
                break;
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void startWithDelay(Intent intent) {
        // add small delay to prevent animation twitch
        RxSchedulers.main().createWorker().schedule(() -> startActivity(intent), CommonConstants.ANIMATION_DELAY_MS, TimeUnit.MILLISECONDS);
    }

    private void sendReport() {
        String[] permissions = new String[] {Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE};
        permissionSubscription.unsubscribe();
        permissionSubscription = new RxPermissions(this)
                .request(permissions)
                .observeOn(RxSchedulers.main())
                .subscribe(granted -> {
                    if (granted) {
                        sendReportPermissionGranted();
                    } else {
                        PermissionUtils.checkDeniedPermissions(this, permissions);
                    }
                }, throwable -> {
                    Timber.e(throwable);
                    RAToast.showShort(R.string.error_unknown);
                });
    }

    private void sendReportPermissionGranted() {
        showProgress();
        logManager.prepareReport()
                .subscribeOn(RxSchedulers.network())
                .observeOn(RxSchedulers.main())
                .doAfterTerminate(this::hideProgress)
                .subscribe(this::onReportReady, throwable -> Timber.e(throwable, "Cannot send filtered files"));
    }

    private void onReportReady(File reportFile) {
        String[] recipients = new String[]{
                App.getConfigurationManager().getLastConfiguration().getGeneralInformation().getSupportEmail()
        };
        Timber.d("Log files is ready. Sending...");
        Intent intent = logManager.prepareSendIntent(this, reportFile, recipients);
        startActivityForResult(intent, Constants.SEND_EMAIL_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Constants.SEND_EMAIL_REQUEST_CODE:
                logManager.clearLogs();
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onReferFriendChanged(@Nullable final ReferFriend referFriend) {

        final Menu menu = binding.navigationView.getMenu();
        final MenuItem item = menu.findItem(R.id.navRefer);
        if (item != null) {
            if (referFriend != null && (referFriend.getEmailEnabled() || referFriend.getSmsEnabled())) {
                item.setTitle(referFriend.getTitle());
                item.setVisible(true);
            } else {
                item.setVisible(false);
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        subscribeToConfigurationUpdates();
        subscribeToDriver();
    }

    private void subscribeToConfigurationUpdates() {
        untilStop(App.getConfigurationManager()
                .getConfigurationUpdates()
                .observeOn(RxSchedulers.main())
                .subscribe(this::onGlobalConfig, Timber::e));
    }

    private void subscribeToDriver() {
        untilStop(App.getDataManager()
                .getDriverObservable()
                .observeOn(RxSchedulers.main())
                .subscribe(this::onDriver, Timber::e));
    }

    private void onGlobalConfig(GlobalConfig config) {
        updateDirectConnect(config, App.getDataManager().getDriver());
    }

    private void onDriver(Driver driver) {
        updateDirectConnect(App.getConfigurationManager().getLastConfiguration(),
                Optional.ofNullable(driver));
    }

    private void updateDirectConnect(GlobalConfig config, Optional<Driver> driver) {
        final Menu menu = binding.navigationView.getMenu();
        final MenuItem menuItem = menu.findItem(R.id.navDirectConnect);
        final DirectConnectConfig directConnect = config.getDirectConnectConfig();
        if (directConnect != null && driver.isPresent()) {
            boolean enabled = directConnect.getEnabled()
                    && driver.get().isDirectConnectDriver()
                    && (!directConnect.isRequiresChauffeur() || driver.get().isChauffeurPermit());
            if (enabled) {
                menuItem.setTitle(directConnect.getTitle());
                menuItem.setVisible(true);
            } else {
                menuItem.setVisible(false);
            }
        } else {
            menuItem.setVisible(false);
        }
    }
}
