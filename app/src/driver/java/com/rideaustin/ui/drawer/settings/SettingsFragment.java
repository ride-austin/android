package com.rideaustin.ui.drawer.settings;


import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.rideaustin.App;
import com.rideaustin.R;
import com.rideaustin.api.model.Ride;
import com.rideaustin.api.model.driver.Driver;
import com.rideaustin.base.ApiSubscriber;
import com.rideaustin.base.BaseActivity;
import com.rideaustin.base.BaseFragment;
import com.rideaustin.databinding.FragmentSettingsBinding;
import com.rideaustin.engine.StateManager;
import com.rideaustin.schedulers.RxSchedulers;
import com.rideaustin.ui.base.EngineStatelessActivity;
import com.rideaustin.ui.drawer.cars.MyCarsActivity;
import com.rideaustin.ui.drawer.documents.DocumentsActivity;
import com.rideaustin.ui.drawer.editprofile.EditProfileFragment;
import com.rideaustin.ui.genericsupport.GenericContactSupportFragment;
import com.rideaustin.ui.stats.StatsActivity;
import com.rideaustin.utils.AppInfoUtil;
import com.rideaustin.utils.NavigatorShareUtils;
import com.rideaustin.utils.NetworkHelper;
import com.rideaustin.utils.toast.RAToast;

import java8.util.Optional;

public class SettingsFragment extends BaseFragment<SettingsFragmentViewModel> {


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setViewModel(obtainViewModel(SettingsFragmentViewModel.class));
        final FragmentSettingsBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_settings, container, false);
        binding.setSettingsFragmentViewModel(getViewModel());

        binding.defaultNavAppPref.setOnClickListener(v -> NavigatorShareUtils.showChooseDefaultNavigationApp((AppCompatActivity) getActivity(),
                preference -> {
                    App.getPrefs().setDriverNavigationActivity(App.getDataManager().getCurrentDriver(), preference);
                    binding.defaultNavigationApp.setText(preference.getAppName());
                }));

        binding.textViewVersionName.append(AppInfoUtil.getAppVersionName());
        View.OnClickListener onClickListener = v -> {
            Intent intent;
            switch (v.getId()) {
                case R.id.textViewLikeOnFacebook:
                    intent = new Intent(Intent.ACTION_VIEW, Uri.parse(App.getConfigurationManager()
                            .getLastConfiguration()
                            .getGeneralInformation()
                            .getFacebookUrl()));
                    break;
                case R.id.textViewLegal:
                    intent = new Intent(Intent.ACTION_VIEW, Uri.parse(App.getConfigurationManager()
                            .getLastConfiguration()
                            .getGeneralInformation()
                            .getLegalDriver()));
                    //TODO: this will change after https://issue-tracker.devfactory.com/browse/RA-6356
                    break;
                default:
                    intent = new Intent(Intent.ACTION_VIEW, Uri.parse(App.getConfigurationManager()
                            .getLastConfiguration()
                            .getGeneralInformation()
                            .getCompanyWebsite()));
                    break;
            }
            startActivity(intent);
        };
        binding.textViewRateOnGooglePlay.setOnClickListener(v -> AppInfoUtil.openPlayStoreForRating());
        binding.textViewVersionName.setOnClickListener(v -> AppInfoUtil.openPlayStoreForRating());
        binding.textViewLikeOnFacebook.setOnClickListener(onClickListener);
        binding.textViewLegal.setOnClickListener(onClickListener);
        binding.textViewCompanyDomain.setOnClickListener(onClickListener);
        binding.editAccount.setOnClickListener(v -> onEditAccountClicked());
        binding.signOut.setOnClickListener(v -> onSignOutClicked());
        binding.textViewMyCars.setOnClickListener(v -> onMyCarsClicked());
        binding.textViewMyDocuments.setOnClickListener(v -> onMyDocumentsClicked());
        binding.textViewCompanyDomain.setText(App.getConfigurationManager().getLastConfiguration().getGeneralInformation().getCompanyDomain());
        binding.textContactSupport.setOnClickListener(v -> onContactSupportClicked());
        binding.textViewMyStats.setOnClickListener(v -> onMyStatsClicked());
        getViewModel().refreshUser();
        return binding.getRoot();
    }

    private void onContactSupportClicked() {
        Optional<Driver> driver = App.getDataManager().getDriver();
        Optional<Ride> currentRide = App.getInstance().getStateManager().getCurrentRideIfAny();

        GenericContactSupportFragment messageFragment = GenericContactSupportFragment.newInstance(
                currentRide.map(Ride::getId),
                driver.map(Driver::getCityId));
        ((EngineStatelessActivity) getActivity()).replaceFragment(messageFragment, R.id.content_frame, true);
    }


    private void onMyDocumentsClicked() {
        startActivity(new Intent(getActivity(), DocumentsActivity.class));
    }

    private void onEditAccountClicked() {
        ((BaseActivity) getActivity()).replaceFragment(new EditProfileFragment(), R.id.content_frame, true);
    }

    private void onMyCarsClicked() {
        startActivity(new Intent(getActivity(), MyCarsActivity.class));
    }

    private void onMyStatsClicked() {
        startActivity(new Intent(getActivity(), StatsActivity.class));
    }

    private void onSignOutClicked() {
        if (!NetworkHelper.isNetworkAvailable()) {
            RAToast.show(R.string.network_error, Toast.LENGTH_SHORT);
        } else {
            logoutUser();
        }
    }

    private void logoutUser() {
        final StateManager stateManager = App.getInstance().getStateManager();
        if (stateManager.hasActiveRide()) {
            RAToast.showShort(R.string.cannot_logout_during_ride);
            return;
        }
        App.getDataManager().logoutDriver()
                .observeOn(RxSchedulers.main())
                .subscribe(new ApiSubscriber<Void>(getCallback()) {
                    @Override
                    public void onNext(Void o) {
                        super.onNext(o);
                        getActivity().finish();
                    }
                });
    }
}
