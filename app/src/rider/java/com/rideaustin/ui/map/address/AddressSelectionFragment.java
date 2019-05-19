package com.rideaustin.ui.map.address;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListAdapter;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.internal.MDButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.rideaustin.App;
import com.rideaustin.R;
import com.rideaustin.api.model.Ride;
import com.rideaustin.base.ApiSubscriber2;
import com.rideaustin.base.BaseApiException;
import com.rideaustin.base.BaseFragment;
import com.rideaustin.databinding.FragmentAddressSelectionBinding;
import com.rideaustin.entities.GeoPosition;
import com.rideaustin.schedulers.RxSchedulers;
import com.rideaustin.ui.drawer.NavigationDrawerActivity;
import com.rideaustin.ui.drawer.favorite.FavoritesMapFragment;
import com.rideaustin.ui.drawer.favorite.FavoritesViewModel;
import com.rideaustin.ui.map.MapViewModel;
import com.rideaustin.ui.utils.MaterialDialogCreator;
import com.rideaustin.utils.DialogUtils;
import com.rideaustin.utils.KeyboardUtil;
import com.rideaustin.utils.LocationHintHelper;
import com.rideaustin.utils.PickupHelper;
import com.rideaustin.utils.RecentPlacesHelper;
import com.rideaustin.utils.location.LocationHelper;
import com.rideaustin.utils.toast.RAToast;

import java8.util.Optional;
import rx.Subscription;
import rx.subscriptions.Subscriptions;
import timber.log.Timber;

import static com.rideaustin.utils.CommonConstants.ANIMATION_DELAY_MS;

/**
 * The purpose of this class is to extract address selection logic and UX into separate screen.
 * Its made off MainMapFragment/MapViewModel pieces, but still depends on it.
 * Further refactoring will require much more changes and affect sensitive parts of app.
 *
 * Reference: RA-13094
 * @author sdelaysam.
 */

public class AddressSelectionFragment extends BaseFragment implements AddressListener {

    private static final String IS_DESTINATION = "is_destination";

    private boolean isDestination;
    private boolean inPredictionsMode;
    private FragmentAddressSelectionBinding binding;
    private AddressSelectionAdapter selectionAdapter;
    private AddressAutocompleteAdapter autoCompleteAdapter;
    private GoogleApiClient apiClient;
    private Optional<MapViewModel> mapViewModel = Optional.empty();
    private Subscription addressSubscription = Subscriptions.empty();
    private Subscription farSubscription = Subscriptions.empty();
    private Subscription updateDestinationSubscription = Subscriptions.empty();
    private MaterialDialog dialog;

    public static AddressSelectionFragment newInstance(boolean isDestination) {
        AddressSelectionFragment fragment = new AddressSelectionFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean(IS_DESTINATION, isDestination);
        fragment.setArguments(bundle);
        return fragment;
    }

    public void setMapViewModel(MapViewModel mapViewModel) {
        this.mapViewModel = Optional.ofNullable(mapViewModel);
    }

    //----------------------------------------------------------------------------------------------
    // Lifecycle
    //----------------------------------------------------------------------------------------------

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);
        initApiClient();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        isDestination = getArguments() != null && getArguments().getBoolean(IS_DESTINATION, false);
        binding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_address_selection, container, false);
        binding.setIsDestination(isDestination);
        int titleRes = isDestination
                ? R.string.address_choose_destination
                : R.string.address_choose_pickup;
        setToolbarTitleAligned(titleRes, Gravity.CENTER);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        LatLngBounds bounds = mapViewModel.map(MapViewModel::getPickupLocation)
                .map(latLng -> LatLngBounds.builder().include(latLng).build())
                .orElse(App.getConfigurationManager().getCityBounds());
        autoCompleteAdapter = new AddressAutocompleteAdapter(getContext(), apiClient, bounds, this);
        selectionAdapter = new AddressSelectionAdapter(getActivity(), this);
        binding.listView.setAlpha(0f);
        binding.listView.animate().alpha(1f).setDuration(ANIMATION_DELAY_MS).start();
        addListeners();
    }

    @Override
    public void onStart() {
        super.onStart();
        apiClient.connect();
        restoreAddress();
        applyAdapter(selectionAdapter);
        inPredictionsMode = false;
    }

    @Override
    public void onStop() {
        super.onStop();
        apiClient.disconnect();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        addressSubscription.unsubscribe();
        farSubscription.unsubscribe();
        updateDestinationSubscription.unsubscribe();
        DialogUtils.dismiss(dialog);
    }

    @Override
    public boolean onBackPressed() {
        if (inPredictionsMode) {
            binding.addressInput.clearFocus();
            KeyboardUtil.hideKeyBoard(getContext(), binding.listView);
            applyAdapter(selectionAdapter);
            inPredictionsMode = false;
            return true;
        } else {
            return super.onBackPressed();
        }
    }

    private void goBack() {
        if (getActivity() == null) {
            // RA-13678: detached before onDestroyView?
            return;
        }
        getActivity().onBackPressed();
    }

    private void gotoMap() {
        if (getActivity() == null) {
            // RA-13678: detached before onDestroyView?
            return;
        }
        // this may happen when FavoritesMapFragment is opened
        // need to go back straight to map
        ((NavigationDrawerActivity) getActivity()).hideAddressSelection();
    }

    //----------------------------------------------------------------------------------------------
    // UI related logic
    //----------------------------------------------------------------------------------------------

    private void addListeners() {
        binding.addressInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                doOnUserInput(binding.addressInput.getText());
            }
        });
        binding.addressInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (binding.addressInput.getTag() == null) {
                    doOnUserInput(s);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                binding.clearButton.setVisibility(s != null && s.length() > 0
                        ? View.VISIBLE
                        : View.GONE);
            }
        });
        binding.clearButton.setOnClickListener(v -> binding.addressInput.setText(""));
    }

    private void restoreAddress() {
        if (isDestination) {
            pastText(binding.addressInput, mapViewModel.map(MapViewModel::getDestinationAddress)
                    .map(GeoPosition::getPlaceName).orElse(""));
        } else {
            pastText(binding.addressInput, mapViewModel.map(MapViewModel::getStartAddress)
                    .map(GeoPosition::getPlaceName).orElse(""));
        }
    }

    private void pastText(EditText editText, String text) {
        editText.setTag("pasting");
        editText.setText(text);
        editText.setTag(null);
    }

    private void doOnUserInput(CharSequence s) {
        if (s == null || s.length() == 0) {
            applyAdapter(selectionAdapter);
            onInputCleared();
            inPredictionsMode = false;
        } else {
            applyAdapter(autoCompleteAdapter);
            autoCompleteAdapter.getFilter().filter(s);
            inPredictionsMode = true;
        }
    }

    private void onInputCleared() {
        mapViewModel.ifPresent(m -> {
            if (isDestination && !m.isActiveRide()) {
                m.setDestinationAddress(null, true);
            }
        });
    }

    private void applyAdapter(ListAdapter adapter) {
        binding.listView.setAdapter(adapter);
        if (adapter instanceof AdapterView.OnItemClickListener) {
            binding.listView.setOnItemClickListener((AdapterView.OnItemClickListener) adapter);
        }
    }

    //----------------------------------------------------------------------------------------------
    // API client
    //----------------------------------------------------------------------------------------------

    private void initApiClient() {
        apiClient = new GoogleApiClient.Builder(getContext())
                .addOnConnectionFailedListener(connectionResult ->
                        Timber.e("Google api connection failed"))
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(@Nullable Bundle bundle) {
                        Timber.i("GooglePlayServices onConnected");
                    }

                    @Override
                    public void onConnectionSuspended(int code) {
                        Timber.i("GooglePlayServices connection suspended: %d", code);
                    }
                })
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .build();
    }

    //----------------------------------------------------------------------------------------------
    // Favorite places interaction
    //----------------------------------------------------------------------------------------------

    private void openFavoritesMap(FavoritesMapFragment fragment) {
        fragment.listener = this::setFavoritePlace;
        ((NavigationDrawerActivity) getActivity()).replaceFragment(fragment, R.id.rootView, true);
    }

    private void setFavoritePlace(@FavoritesViewModel.FavoriteType String favoriteType,
                                  GeoPosition selectedAddress) {
        switch (favoriteType) {
            case FavoritesViewModel.TYPE_HOME:
                App.getPrefs().setFavoritePlace(favoriteType, selectedAddress);
                goBack();
                break;
            case FavoritesViewModel.TYPE_WORK:
                App.getPrefs().setFavoritePlace(favoriteType, selectedAddress);
                goBack();
                break;
            case FavoritesViewModel.TYPE_PICKUP:
                applyPickupAddress(selectedAddress);
                break;
            case FavoritesViewModel.TYPE_DESTINATION:
                applyDestinationAddress(selectedAddress);
                break;
        }
    }

    //----------------------------------------------------------------------------------------------
    // Destination update
    //----------------------------------------------------------------------------------------------

    private void askDestinationUpdate(GeoPosition geoPosition) {
        DialogUtils.dismiss(dialog);
        dialog = MaterialDialogCreator.createUpdateRideDialog(
                App.getAppName(),
                App.getInstance().getString(R.string.update_ride_dialog_content), getActivity());
        dialog.setOnShowListener(dialogInterface -> {
            MDButton positiveButton = dialog.getActionButton(DialogAction.POSITIVE);
            positiveButton.setOnClickListener(v -> {
                dialogInterface.dismiss();
                sendDestinationUpdate(geoPosition);
            });
        });
    }

    // TODO: move to view model?
    private void sendDestinationUpdate(GeoPosition destination) {
        long rideId = App.getPrefs().getRideId();
        double lat = destination.getLat();
        double lng = destination.getLng();
        String address = destination.getAddressLine();
        String comment = mapViewModel.get().getOptionalComments().orElse(null);
        updateDestinationSubscription.unsubscribe();
        updateDestinationSubscription = LocationHelper.getZipCode(new LatLng(lat, lng))
                .flatMap(zipCode -> App.getDataManager().getRidesService().updateCurrentRide(rideId, lat, lng, address, zipCode.orElse(null), comment))
                .subscribeOn(RxSchedulers.network())
                .observeOn(RxSchedulers.main())
                .subscribe(new ApiSubscriber2<Ride>(getCallback()) {
                    @Override
                    public void onCompleted() {
                        super.onCompleted();
                        onDestinationAddressConfirmed(destination);
                    }

                    @Override
                    public void onNetworkError(BaseApiException e) {
                        super.onNetworkError(e);
                        onRideDestinationUpdateFailed(destination);
                    }
                });
    }

    private void onRideDestinationUpdateFailed(GeoPosition destination) {
        DialogUtils.dismiss(dialog);
        dialog = MaterialDialogCreator.createUpdateRideFailedDialog(
                App.getAppName(),
                App.getInstance().getString(R.string.update_ride_failed_dialog_content), getActivity());

        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setOnShowListener(dialogInterface -> {
            MDButton positiveButton = dialog.getActionButton(DialogAction.POSITIVE);
            positiveButton.setOnClickListener(v -> {
                dialogInterface.dismiss();
                sendDestinationUpdate(destination);
            });
        });
    }


    //----------------------------------------------------------------------------------------------
    // Place selection
    //----------------------------------------------------------------------------------------------

    private void onHomeSelected(Optional<GeoPosition> address) {
        address.ifPresentOrElse(a -> {
            if (isDestination) {
                checkDestinationAddressAllowed(a);
            } else {
                checkPickupAddressAllowed(a);
            }
        }, () -> openFavoritesMap(FavoritesMapFragment.newHomeInstance()));
    }

    private void onWorkSelected(Optional<GeoPosition> address) {
        address.ifPresentOrElse(a -> {
            if (isDestination) {
                checkDestinationAddressAllowed(a);
            } else {
                checkPickupAddressAllowed(a);
            }
        }, () -> openFavoritesMap(FavoritesMapFragment.newWorkInstance()));
    }

    private void onRecentSelected(Optional<GeoPosition> address) {
        if (address.isPresent()) {
            if (isDestination) {
                checkDestinationAddressAllowed(address.get());
            } else {
                checkPickupAddressAllowed(address.get());
            }
        }
    }

    private void checkPickupAddressAllowed(GeoPosition geoPosition) {
        if (!App.getDataManager().getLocationHintHelper().isLocationAllowed(geoPosition.getLatLng(), LocationHintHelper.AreaType.PICKUP)) {
            openFavoritesMap(FavoritesMapFragment.newPickupInstance(geoPosition));
            return;
        }
        applyPickupAddress(geoPosition);
    }

    private void checkDestinationAddressAllowed(GeoPosition geoPosition) {
        if (!App.getDataManager().getLocationHintHelper().isLocationAllowed(geoPosition.getLatLng(), LocationHintHelper.AreaType.DESTINATION)) {
            openFavoritesMap(FavoritesMapFragment.newDestinationInstance(geoPosition));
            return;
        }
        applyDestinationAddress(geoPosition);
    }

    // TODO: move to view model?
    private void applyPickupAddress(GeoPosition geoPosition) {
        farSubscription = PickupHelper.checkIsFar(geoPosition.getLatLng(),
                () -> onPickupAddressFar(geoPosition),
                () -> onPickupAddressConfirmed(geoPosition));
    }

    private void onPickupAddressFar(GeoPosition geoPosition) {
        DialogUtils.dismiss(dialog);
        dialog = MaterialDialogCreator.createSimpleConfirmDialog(
                getString(R.string.warning_location_distance,
                        PickupHelper.getDistantPickUpNotificationThreshold()),
                getActivity())
                .onPositive((dialog, which) -> onPickupAddressConfirmed(geoPosition))
                .show();
    }

    private void onPickupAddressConfirmed(GeoPosition geoPosition) {
        RecentPlacesHelper.saveAddress(geoPosition);
        mapViewModel.ifPresent(m -> {
            m.setStartAddress(geoPosition, true);
            m.setPickupDistanceVerified(true);
        });
        gotoMap();
    }

    private void applyDestinationAddress(GeoPosition geoPosition) {
        if (mapViewModel.map(MapViewModel::isActiveRide).orElse(false)) {
            askDestinationUpdate(geoPosition);
        } else {
            onDestinationAddressConfirmed(geoPosition);
        }
    }

    private void onDestinationAddressConfirmed(GeoPosition geoPosition) {
        RecentPlacesHelper.saveAddress(geoPosition);
        mapViewModel.ifPresent(m -> m.setDestinationAddress(geoPosition, true));
        gotoMap();
    }

    //----------------------------------------------------------------------------------------------
    // AddressListener impl
    //----------------------------------------------------------------------------------------------

    @Override
    public void onAddressSelected(AddressType addressType, Optional<GeoPosition> address) {
        switch (addressType) {
            case HOME:
                onHomeSelected(address);
                break;
            case WORK:
                onWorkSelected(address);
                break;
            case RECENT:
                onRecentSelected(address);
                break;
            case MAP:
                if (isDestination) {
                    openFavoritesMap(FavoritesMapFragment.newDestinationInstance(null));
                } else {
                    openFavoritesMap(FavoritesMapFragment.newPickupInstance(null));
                }
                break;
        }
    }

    // TODO: move to view model?
    @Override
    public void onAddressSelected(AutocompletePrediction prediction) {
        addressSubscription.unsubscribe();
        addressSubscription = LocationHelper.getPlaceById(apiClient, prediction.getPlaceId())
                .subscribeOn(RxSchedulers.computation())
                .flatMap(place -> LocationHelper.getZipCodeOrFail(place.getLatLng())
                        .map(zipCode -> {
                            zipCode.ifPresent(place::setZipCode);
                            return place;
                        }))
                .observeOn(RxSchedulers.main())
                .subscribe(new ApiSubscriber2<GeoPosition>(getCallback()) {
                    @Override
                    public void onNext(GeoPosition address) {
                        super.onNext(address);
                        address.setPlaceId(prediction.getPlaceId());
                        CharSequence primaryText = prediction.getPrimaryText(null);
                        if (!TextUtils.isEmpty(primaryText)) {
                            address.setAddressLine(String.valueOf(primaryText));
                        }
                        if (isDestination) {
                            checkDestinationAddressAllowed(address);
                        } else {
                            checkPickupAddressAllowed(address);
                        }
                    }

                    @Override
                    public void onUnknownError(BaseApiException e) {
                        super.onUnknownError(e);
                        Timber.e(e.getCause());
                        RAToast.showShort(R.string.error_unknown);
                    }
                });
    }

}
