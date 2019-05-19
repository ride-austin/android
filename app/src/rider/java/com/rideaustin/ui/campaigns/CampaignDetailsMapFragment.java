package com.rideaustin.ui.campaigns;

import android.annotation.SuppressLint;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.graphics.ColorUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolygonOptions;
import com.rideaustin.App;
import com.rideaustin.R;
import com.rideaustin.api.model.Boundary;
import com.rideaustin.api.model.campaigns.CampaignArea;
import com.rideaustin.api.model.campaigns.CampaignDetails;
import com.rideaustin.base.BaseFragment;
import com.rideaustin.databinding.FragmentCampaignDetailsMapBinding;
import com.rideaustin.ui.utils.SurgeAreaUtils;
import com.rideaustin.utils.PermissionUtils;
import com.rideaustin.utils.toast.RAToast;

import java8.util.Optional;
import timber.log.Timber;

/**
 * Created on 5/24/18.
 *
 * @author sdelaysam
 */
public class CampaignDetailsMapFragment extends BaseFragment implements OnMapReadyCallback {

    private static final String DATA_KEY = "data_key";

    public static CampaignDetailsMapFragment getInstance(CampaignDetails details) {
        CampaignDetailsMapFragment fragment = new CampaignDetailsMapFragment();
        Bundle args = new Bundle();
        args.putSerializable(DATA_KEY, details);
        fragment.setArguments(args);
        return fragment;
    }

    private GoogleMap gMap;
    private Optional<CampaignDetails> campaignDetails = Optional.empty();
    private FragmentCampaignDetailsMapBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_campaign_details_map, container, false);
        binding.mapView.onCreate(savedInstanceState);
        binding.mapView.getMapAsync(this);
        Optional.ofNullable(getArguments())
                .filter(args -> args.containsKey(DATA_KEY))
                .map(args -> (CampaignDetails) args.getSerializable(DATA_KEY))
                .ifPresentOrElse(details -> {
                    setToolbarTitle(details.getHeaderTitle());
                    campaignDetails = Optional.of(details);
                    checkMap();
                }, () -> {
                    RAToast.showLong(R.string.campaign_details_empty);
                    onBackPressed();
                });
        return binding.getRoot();
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;
        googleMap.getUiSettings().setMapToolbarEnabled(false);
        googleMap.getUiSettings().setScrollGesturesEnabled(true);
        googleMap.getUiSettings().setZoomGesturesEnabled(true);
        googleMap.setMyLocationEnabled(PermissionUtils.isLocationPermissionGranted(getActivity()));
        checkMap();
    }

    private void checkMap() {
        if (gMap != null) {
            gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(30.249279, -97.739173), 10.2345f));
            campaignDetails.map(CampaignDetails::getAreas).ifPresent(list -> {
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                for (CampaignArea area : list) {
                    int color;
                    try {
                        color = Color.parseColor(area.getColor());
                    } catch (Exception e) {
                        Timber.e(e);
                        color = SurgeAreaUtils.getSurgeAreaColor(4f);
                    }

                    PolygonOptions options = new PolygonOptions();
                    options.zIndex(1);
                    options.strokeWidth(5);
                    options.strokeColor(ColorUtils.setAlphaComponent(color, 255));
                    options.fillColor(ColorUtils.setAlphaComponent(color, 100));
                    for (Boundary boundary : area.getBoundary()) {
                        LatLng latLng = new LatLng(boundary.getLat(), boundary.getLng());
                        options.add(latLng);
                        builder.include(latLng);
                    }
                    gMap.addPolygon(options);
                    int padding = getResources().getDimensionPixelSize(R.dimen.padding_15dp);
                    gMap.setPadding(padding, padding, padding, padding);
                    gMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 0));
                }
            });
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!App.getDataManager().isLoggedIn()) {
            // no sense to proceed
            return;
        }

        binding.mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        binding.mapView.onResume();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (binding != null) {
            binding.mapView.onSaveInstanceState(outState);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        binding.mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        binding.mapView.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (binding != null) {
            binding.mapView.onDestroy();
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (binding != null) {
            binding.mapView.onLowMemory();
        }
    }

}