package com.rideaustin.ui.drawer.cars.add;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.rideaustin.App;
import com.rideaustin.R;
import com.rideaustin.databinding.SetupVehicleListBinding;
import com.rideaustin.models.VehicleManager;
import com.rideaustin.models.VehicleModel;
import com.rideaustin.utils.Constants;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscription;
import rx.subscriptions.Subscriptions;

/**
 * Created by rost on 8/10/16.
 */
public class SetupVehicleListFragment extends BaseAddCarFragment {
    public static final String TYPE_KEY = "type_key";

    private SetupVehicleInteractor interactor;
    private SetupVehicleListBinding binding;
    private Subscription subscription = Subscriptions.empty();

    @Constants.CarPropertyType
    private String propertyType;

    public static SetupVehicleListFragment newInstance(AddCarActivity.AddCarSequence sequence, @Constants.CarPropertyType String propertyType) {
        SetupVehicleListFragment fragment = new SetupVehicleListFragment();
        Bundle args = new Bundle();
        args.putString(TYPE_KEY, propertyType);
        args.putSerializable(SEQUENCE_KEY, sequence);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_setup_vehicle_list, container, false);
        interactor = createInteractor();
        setToolbarTitle(interactor.getTitle());
        interactor.clear();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        VehicleManager manager = App.getDataManager().getVehicleManager();
        createHeaderView(binding.header, manager);

        @Constants.CarPropertyType
        final String type = getArguments().getString(TYPE_KEY);
        propertyType = type;
        final boolean isArrowsVisible = !Constants.CarPropertyType.COLOR.equals(propertyType);

        subscription = interactor.getListItems()
                .subscribe(strings -> {
                    List<String> data = new ArrayList<>(strings);
                    if (Constants.CarPropertyType.YEAR.equals(propertyType)) {
                        data = filterOutNotSupportedYears(data);
                    }
                    final ListAdapter adapter = new SetupVehicleAdapter(
                            getContext(),
                            R.layout.listitem_setup_vehicle,
                            R.id.text,
                            data,
                            isArrowsVisible);

                    binding.list.setAdapter(adapter);
                });

        binding.list.setOnItemClickListener((parent, view1, position, id) -> {
            final String item = (String) parent.getAdapter().getItem(position);
            interactor.onListItemSelected(item);
            notifyCompleted();
        });
    }

    private List<String> filterOutNotSupportedYears(List<String> data) {
        final Integer minCarYear = addCarViewModel.getDriverRegistration().getMinCarYear();
        return Observable.from(data)
                .filter(year -> Integer.parseInt(year) >= minCarYear)
                .toList()
                .toBlocking()
                .first();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        subscription.unsubscribe();
    }

    private void createHeaderView(TextView header, VehicleManager vehicleManager) {
        List filters = vehicleManager.getFilters();
        if (filters.size() > 0) {
            header.setText(String.format("%s %s %s", vehicleManager.getFilter(VehicleModel.FieldType.YEAR), vehicleManager.getFilter(VehicleModel.FieldType.MAKE), vehicleManager.getFilter(VehicleModel.FieldType.MODEL)));
        } else {
            header.setVisibility(View.GONE);
        }
    }

    private SetupVehicleInteractor createInteractor() {
        Bundle args = getArguments();
        final VehicleManager vehicleManager = App.getDataManager().getVehicleManager();
        final String type = args.getString(TYPE_KEY);

        switch (type) {
            case Constants.CarPropertyType.MAKE:
                return new VehicleYearMakeModelInteractor(getContext(), VehicleModel.FieldType.MAKE, vehicleManager, addCarViewModel);
            case Constants.CarPropertyType.MODEL:
                return new VehicleYearMakeModelInteractor(getContext(), VehicleModel.FieldType.MODEL, vehicleManager, addCarViewModel);
            case Constants.CarPropertyType.YEAR:
                return new VehicleYearMakeModelInteractor(getContext(), VehicleModel.FieldType.YEAR, vehicleManager, addCarViewModel);
            case Constants.CarPropertyType.COLOR:
                return new VehicleColorInteractor(getContext(), addCarViewModel.getCarRegistration());
            default:
                throw new IllegalArgumentException("unknown type " + type);
        }
    }
}
