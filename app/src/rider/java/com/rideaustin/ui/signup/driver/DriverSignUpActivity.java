package com.rideaustin.ui.signup.driver;

import android.app.Activity;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.View;

import com.rideaustin.App;
import com.rideaustin.R;
import com.rideaustin.api.config.GlobalConfig;
import com.rideaustin.base.Transition;
import com.rideaustin.databinding.DriverSignUpBinding;
import com.rideaustin.models.AssetsVehicleDataProvider;
import com.rideaustin.models.VehicleManager;
import com.rideaustin.ui.base.BaseRiderActivity;
import com.rideaustin.ui.genericsupport.GenericContactSupportFragment;
import com.rideaustin.ui.utils.MaterialDialogCreator;
import com.rideaustin.utils.Constants;

import java8.util.Optional;

/**
 * Created by rost on 8/10/16.
 */
public class DriverSignUpActivity extends BaseRiderActivity implements DriverSignUpViewModel.DriverSignUpActivityCallback {

    private DriverSignUpBinding binding;
    private DriverSignUpFlowRouter router;
    private DriverSignUpViewModel viewModel;
    private DriverSignUpInteractor driverSignUpInteractor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // RA-8891: if activity is restored after app restart
        // fragments are also restored in super.onCreate(Bundle)
        // need to restore all data before this to prevent NPE
        initVehicleManager();
        driverSignUpInteractor = (DriverSignUpInteractor) getIntent().getExtras().getSerializable(Constants.INTERACTOR_KEY);
        viewModel = new DriverSignUpViewModel(this, driverSignUpInteractor);
        router = new DriverSignUpFlowRouter(savedInstanceState, driverSignUpInteractor);
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_driver_signup);
        binding.setViewModel(viewModel);
        setToolbar(binding.toolbar);
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
        binding.needHelp.setOnClickListener(v -> {
            int selectedCityId = (int) getViewModelInternal().getSignUpInteractor().getSelectedCityId();
            GenericContactSupportFragment messageFragment = GenericContactSupportFragment.newInstance(Optional.empty(), Optional.of(selectedCityId));
            replaceFragment(messageFragment, R.id.content_frame, true);
            binding.needHelp.setVisibility(View.GONE);
        });

        if (savedInstanceState == null) {
            switchToState(router.getInitialState());
        }
        // else proper state is restored from Bundle
        // and FragmentManager restored current Fragment
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        router.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isFinishing()) {
            App.getDataManager().setVehicleManager(null);
        }
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
    public void onBackPressed() {
        final FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentById(R.id.content_frame);
        if (fragment instanceof BaseDriverSignUpFragment) {
            BaseDriverSignUpFragment currentFragment = (BaseDriverSignUpFragment) fragment;
            if (fragmentManager.getBackStackEntryCount() > 0) {
                currentFragment.clearState();
                router.moveToPrevState(fragmentManager);
            } else {
                super.onBackPressed();
            }
        } else {
            super.onBackPressed();
        }
    }

    @Deprecated
    @Override
    // refactor to view models
    public DriverSignUpViewModel getViewModelInternal() {
        return viewModel;
    }

    @Override
    public VehicleManager getVehicleManager() {
        return App.getDataManager().getVehicleManager();
    }

    @Override
    public void onCompleted() {
        if (router.hasNextState()) {
            onRequestHelpWidget(false);
            switchToState(router.moveToNextState());
        } else {
            MaterialDialogCreator.createCenteredMessageDialog(getString(R.string.ride_app,
                    App.capitalizeFirstLetter(driverSignUpInteractor.getCityName())),
                    getString(R.string.register_driver_sucess), this)
                    .setOnDismissListener(dialogInterface -> onSignUpCompleted());
        }
    }

    private void onSignUpCompleted() {
        Intent returnIntent = new Intent();
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }

    @Override
    public void onRequestHelpWidget(boolean hasWidget) {
        binding.needHelp.setVisibility(hasWidget ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onConfigUpdated(final GlobalConfig globalConfig) {
        viewModel.loadLogo(binding.cityLogo, globalConfig.getGeneralInformation().getLogoUrl());
    }

    private void initVehicleManager() {
        VehicleManager.VehicleDataProvider provider = new AssetsVehicleDataProvider(this);
        VehicleManager manager = new VehicleManager(provider);
        App.getDataManager().setVehicleManager(manager);
    }

    private void switchToState(DriverSignUpFlowRouter.State target) {
        onRequestHelpWidget(false);
        boolean isInitial = target.getPosition() == 0;
        Transition transition = isInitial ? Transition.NONE : Transition.FORWARD;
        replaceFragment(target.createFragment(), R.id.content_frame, !isInitial, target.getTag(), transition);
    }
}
