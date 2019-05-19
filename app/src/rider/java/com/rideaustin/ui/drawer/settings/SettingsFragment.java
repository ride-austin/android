package com.rideaustin.ui.drawer.settings;


import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rideaustin.App;
import com.rideaustin.R;
import com.rideaustin.base.ApiSubscriber2;
import com.rideaustin.base.BaseFragment;
import com.rideaustin.databinding.FragmentSettingsBinding;
import com.rideaustin.entities.GeoPosition;
import com.rideaustin.schedulers.RxSchedulers;
import com.rideaustin.ui.drawer.NavigationDrawerActivity;
import com.rideaustin.ui.drawer.editprofile.EditProfileFragment;
import com.rideaustin.ui.drawer.favorite.FavoritesMapFragment;
import com.rideaustin.ui.drawer.favorite.FavoritesViewModel;
import com.rideaustin.ui.genericsupport.GenericContactSupportFragment;
import com.rideaustin.utils.AppInfoUtil;
import com.rideaustin.utils.NetworkHelper;
import com.rideaustin.utils.toast.RAToast;

import java8.util.Optional;
import rx.Subscription;
import rx.subscriptions.CompositeSubscription;
import rx.subscriptions.Subscriptions;

public class SettingsFragment extends BaseFragment<SettingsFragmentViewModel> {

    private FragmentSettingsBinding binding;
    private CompositeSubscription subscriptions = new CompositeSubscription();
    private Subscription permissionSubscription = Subscriptions.empty();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setViewModel(obtainViewModel(SettingsFragmentViewModel.class));
        setToolbarTitleAligned(R.string.settings, Gravity.LEFT);
        NavigationDrawerActivity.addRootFragmentClass(this.getClass().getName());
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_settings, container, false);
        binding.setSettingsFragmentViewModel(getViewModel());
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
                            .getLegalRider()));
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
        binding.textViewWebsite.setOnClickListener(onClickListener);
        binding.textViewContactUs.setOnClickListener(v -> sendReport());
        binding.editAccount.setOnClickListener(v -> ((NavigationDrawerActivity) getActivity()).replaceFragment(new EditProfileFragment(), R.id.rootView, true));
        binding.signOut.setOnClickListener(v -> onSignOutClicked());
        binding.layoutFavoriteWork.setOnClickListener(this::onFavoritesClicked);
        binding.layoutFavoriteHome.setOnClickListener(this::onFavoritesClicked);
        getViewModel().refreshUser();
        return binding.getRoot();
    }

    private void onFavoritesClicked(View view) {
        FavoritesMapFragment fragment;
        switch (view.getId()) {
            case R.id.layoutFavoriteHome:
                fragment = FavoritesMapFragment.newHomeInstance();
                break;
            case R.id.layoutFavoriteWork:
                fragment = FavoritesMapFragment.newWorkInstance();
                break;
            default:
                throw new UnsupportedOperationException("This area of code should be completed");
        }
        fragment.listener = this::onFavoritePlace;
        if (getActivity() instanceof NavigationDrawerActivity) {
            ((NavigationDrawerActivity) getActivity()).replaceFragment(fragment, R.id.rootView, true);
        }
    }

    private void onFavoritePlace(@FavoritesViewModel.FavoriteType String favoriteType, GeoPosition selectedAddress) {
        App.getPrefs().setFavoritePlace(favoriteType, selectedAddress);
        if (getActivity() != null) {
            getActivity().onBackPressed();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        subscriptions.add(App.getConfigurationManager()
                .getConfigurationUpdates()
                .observeOn(RxSchedulers.main())
                .subscribe(globalConfig -> {
                    binding.textViewWebsite.setText(globalConfig.getGeneralInformation().getCompanyDomain());
                }));
    }

    @Override
    public void onStop() {
        super.onStop();
        subscriptions.clear();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        permissionSubscription.unsubscribe();
    }

    private void onSignOutClicked() {
        if (!NetworkHelper.isNetworkAvailable()) {
            RAToast.showShort(R.string.network_error);
        } else {
            logoutUser();
        }
    }

    private void sendReport() {
        Integer cityId = App.getConfigurationManager().getLastConfiguration().getCurrentCity().getCityId();

        GenericContactSupportFragment messageFragment = GenericContactSupportFragment.newInstance(Optional.empty(), Optional.ofNullable(cityId));
        ((NavigationDrawerActivity) getActivity()).replaceFragment(messageFragment, R.id.rootView, false);
    }

    private void logoutUser() {
        if (App.getPrefs().hasRideId()) {
            RAToast.showShort(R.string.cannot_logout_during_ride);
            return;
        }

        App.getDataManager().logout()
                .observeOn(RxSchedulers.main())
                .subscribe(new ApiSubscriber2<Void>(getCallback()) {
                    @Override
                    public void onNext(Void o) {
                        super.onNext(o);
                        getActivity().finish();
                    }
                });
    }
}
