package com.rideaustin.ui.signup.driver;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rideaustin.R;
import com.rideaustin.databinding.DriverVehicleInformationSummaryBinding;
import com.rideaustin.schedulers.RxSchedulers;
import com.rideaustin.ui.utils.MaterialDialogCreator;
import com.rideaustin.ui.utils.MenuUtil;
import com.rideaustin.utils.FileDirectoryUtil;

import java.io.File;

import rx.Single;
import rx.subscriptions.CompositeSubscription;

import static com.rideaustin.utils.Constants.TEMP_DIRECTORY;

/**
 * Created by rost on 8/9/16.
 */
public class DriverVehicleInformationSummaryFragment extends BaseDriverSignUpFragment {

    private DriverVehicleInformationSummaryBinding binding;
    private CompositeSubscription subscriptions = new CompositeSubscription();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_driver_vehicle_information_summary, container, false);
        setToolbarTitle(R.string.title_driver_vehicle_information);
        setHasHelpWidget(true);

        binding.summary.setText(getSignUpInteractor().getDriverRegistrationConfiguration().getNewCarSuccessMessage());
        setHasOptionsMenu(true);
        return binding.getRoot();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        MenuUtil.inflateNextMenu(menu, inflater, true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.next:
                onNextClicked();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void onNextClicked() {
        subscriptions.add(createCarFile()
                .subscribeOn(RxSchedulers.computation())
                .observeOn(RxSchedulers.main())
                .subscribe(aBoolean -> notifyCompleted(), throwable ->
                        MaterialDialogCreator.createSimpleErrorDialog(throwable.getLocalizedMessage(), getActivity())));
    }

    @Override
    public void onStop() {
        super.onStop();
        subscriptions.clear();
    }

    private Single<Boolean> createCarFile() {
        return Single.fromCallable(() -> {
            Gson gson = new GsonBuilder().create();
            File driveJsonFile = FileDirectoryUtil.createOrRewriteFile(System.getProperty(TEMP_DIRECTORY), "car.json", gson.toJson(getDriverData().getDriverRegistration().getCars().get(0)));
            getDriverData().setCarJsonFilePath(driveJsonFile.getAbsolutePath());
            return true;
        });
    }
}
