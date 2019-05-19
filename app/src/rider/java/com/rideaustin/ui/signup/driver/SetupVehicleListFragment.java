package com.rideaustin.ui.signup.driver;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.rideaustin.R;
import com.rideaustin.api.model.driver.Car;
import com.rideaustin.api.model.driver.DriverRegistration;
import com.rideaustin.databinding.SetupVehicleListBinding;
import com.rideaustin.models.VehicleManager;
import com.rideaustin.models.VehicleModel;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscription;
import rx.subscriptions.Subscriptions;

/**
 * Created by rost on 8/10/16.
 */
public class SetupVehicleListFragment extends BaseDriverSignUpFragment {
    public static final String TYPE_KEY = "type_key";
    public static final String TYPE_MAKE = "make";
    public static final String TYPE_MODEL = "model";
    public static final String TYPE_YEAR = "year";
    public static final String TYPE_COLOR = "color";

    private SetupVehicleInteractor interactor;
    private SetupVehicleListBinding binding;
    private Subscription subscription = Subscriptions.empty();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        interactor = createInteractor();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_setup_vehicle_list, container, false);
        setToolbarTitle(interactor.getTitle());
        setHasHelpWidget(true);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        createHeaderView(binding.header, getVehicleManager());

        Bundle args = getArguments();
        String type = args.getString(TYPE_KEY);

        final boolean isArrowsVisible = !TYPE_COLOR.equals(type);

        interactor.clear();
        subscription = interactor.getListItems()
                .subscribe(strings -> {
                    List<String> data = new ArrayList<>(strings);
                    if (TYPE_YEAR.equals(type)) {
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
        final Integer minCarYear = getSignUpInteractor()
                .getDriverRegistrationConfiguration()
                .getMinCarYear();
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
            String text = String.format("%s %s %s",
                    vehicleManager.getFilter(VehicleModel.FieldType.YEAR),
                    vehicleManager.getFilter(VehicleModel.FieldType.MAKE),
                    vehicleManager.getFilter(VehicleModel.FieldType.MODEL));
            header.setText(text.trim());
        } else {
            header.setVisibility(View.GONE);
        }
    }

    private SetupVehicleInteractor createInteractor() {
        Bundle args = getArguments();
        final VehicleManager vehicleManager = getVehicleManager();
        final String type = args.getString(TYPE_KEY);
        final DriverRegistration driverRegistration = getDriverData().getDriverRegistration();

        if (driverRegistration.getCars().size() == 0) {
            driverRegistration.getCars().add(new Car());
        }

        switch (type) {
            case TYPE_MAKE:
                return new VehicleYearMakeModelInteractor(getContext(), VehicleModel.FieldType.MAKE, vehicleManager, driverRegistration.getCars().get(0));
            case TYPE_MODEL:
                return new VehicleYearMakeModelInteractor(getContext(), VehicleModel.FieldType.MODEL, vehicleManager, driverRegistration.getCars().get(0));
            case TYPE_YEAR:
                return new VehicleYearMakeModelInteractor(getContext(), VehicleModel.FieldType.YEAR, vehicleManager, driverRegistration.getCars().get(0));
            case TYPE_COLOR:
                return new VehicleColorInteractor(getContext(), driverRegistration.getCars().get(0));
            default:
                throw new IllegalArgumentException("unknown type " + type);
        }
    }

    @Override
    protected void clearState() {
        super.clearState();
        interactor.clear();
    }
}
