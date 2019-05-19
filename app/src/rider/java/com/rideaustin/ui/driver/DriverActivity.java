package com.rideaustin.ui.driver;

import android.app.Activity;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.internal.MDButton;
import com.rideaustin.App;
import com.rideaustin.R;
import com.rideaustin.api.config.GlobalConfig;
import com.rideaustin.api.config.SupportedCity;
import com.rideaustin.api.converter.ConverterUtil;
import com.rideaustin.api.model.User;
import com.rideaustin.base.BaseActivity;
import com.rideaustin.base.BaseActivityCallback;
import com.rideaustin.databinding.DriverBinding;
import com.rideaustin.ui.drawer.NavigationDrawerActivity;
import com.rideaustin.ui.drawer.editprofile.EditProfileActivity;
import com.rideaustin.ui.signup.driver.DriverSignUpActivity;
import com.rideaustin.ui.utils.CommonMaterialDialogCreator;
import com.rideaustin.ui.utils.MaterialDialogCreator;
import com.rideaustin.ui.utils.ProfileValidator;
import com.rideaustin.utils.Constants;

import java.util.List;

/**
 * Created by kshumelchyk on 6/22/16.
 */
public class DriverActivity extends BaseActivity implements DriverViewModel.DriverViewModelListener {

    public static final String KEY_OPEN_VIA_NAVIGATION_DRAWER = "key_open_via_navigation_drawer";
    public static final int REQUEST_DRIVER_CREATION = 0;
    private DriverBinding binding;
    private DriverViewModel viewModel;
    private CityAdapter cityAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_driver);
        viewModel = new DriverViewModel(this);
        binding.setViewModel(viewModel);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        boolean fromDrawer = getIntent().getExtras() != null && getIntent().getExtras().getBoolean(KEY_OPEN_VIA_NAVIGATION_DRAWER);
        if (fromDrawer) {
            binding.skip.setText(getString(R.string.btn_cancel));
        }

        binding.skip.setOnClickListener(view -> {
            if (fromDrawer) {
                onBackPressed();
            } else {
                if (isUserDataValid()) {
                    startActivity(new Intent(this, NavigationDrawerActivity.class));
                } else {
                    startActivity(new Intent(this, EditProfileActivity.class));
                }
                finish();
            }
        });

        binding.registerDriver.setOnClickListener(view -> {
            showLicenseDialog();
        });

        binding.cityList.setLayoutManager(new LinearLayoutManager(this));
        binding.cityList.setHasFixedSize(true);

        cityAdapter = new CityAdapter(binding);
        binding.cityList.setAdapter(cityAdapter);

        binding.citySelector.setOnClickListener(view -> binding.setSpinnerShown(true));
        binding.gradient.setOnClickListener(view -> binding.setSpinnerShown(false));
    }

    private void loadRegistrationConfiguration() {
        CityModel cityModel = viewModel.getSelectedCity().get();
        if (cityModel == null) {
            MaterialDialogCreator.createSimpleDialog(getString(R.string.city_required), this);
        } else {
            viewModel.loadDriverRegistrationConfiguration(cityModel.getCityId());
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        viewModel.onStart();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_DRIVER_CREATION) {
            if (resultCode == Activity.RESULT_OK) {
                startActivity(new Intent(DriverActivity.this, NavigationDrawerActivity.class));
                finish();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isFinishing()) {
            App.getDataManager().clearDriverRegistrationData();
        }
    }

    @Override
    public void onConfigChanged(final GlobalConfig globalConfig) {
        List<SupportedCity> supportedCities = globalConfig.getSupportedCities();
        cityAdapter.setCities(ConverterUtil.convertToList(supportedCities, CityModel::fromSupportedCity));
        binding.getViewModel().setSelectedCity(CityModel.fromCurrentCity(globalConfig.getCurrentCity(), globalConfig.getGeneralInformation().getLogoBlackUrl()));
    }

    @Override
    public void onRegistrationConfigurationLoaded() {
        startActivityForResult(new Intent(DriverActivity.this, DriverSignUpActivity.class)
                .putExtra(Constants.INTERACTOR_KEY, viewModel.getSignUpInteractor()), REQUEST_DRIVER_CREATION);
    }

    @Override
    public void onRegistrationConfigurationFailed() {
        final MaterialDialog dialog = CommonMaterialDialogCreator.createYesNoDialog(getString(R.string.general_server_comunication_error), this)
                .build();
        dialog.setOnShowListener(dialogInterface -> {
            final MDButton positiveButton = dialog.getActionButton(DialogAction.POSITIVE);
            positiveButton.setOnClickListener(v -> {
                dialog.dismiss();
                loadRegistrationConfiguration();
            });
        });
        dialog.show();
    }

    private void showLicenseDialog() {
        final MaterialDialog dialog = MaterialDialogCreator
                .createConfirmLicenseDialog(getString(R.string.license_confirm), this);
        dialog.setOnShowListener(dialogInterface -> {
            final MDButton positiveButton = dialog.getActionButton(DialogAction.POSITIVE);
            positiveButton.setOnClickListener(v -> {
                dialog.dismiss();
                loadRegistrationConfiguration();
            });
            final MDButton negativeButton = dialog.getActionButton(DialogAction.NEGATIVE);
            negativeButton.setOnClickListener(v -> {
                dialog.dismiss();
                finish();
            });
        });
    }

    private boolean isUserDataValid() {
        User user = App.getDataManager().getCurrentUser();
        return ProfileValidator.validateUser(user, this);
    }

    @Override
    public BaseActivityCallback getCallback() {
        return this;
    }
}
