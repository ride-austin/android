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
import android.widget.Toast;

import com.rideaustin.App;
import com.rideaustin.R;
import com.rideaustin.databinding.RCRADisclosureCheckInvestigationBinding;
import com.rideaustin.schedulers.RxSchedulers;
import com.rideaustin.ui.utils.MenuUtil;
import com.rideaustin.utils.toast.RAToast;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

import rx.Observable;
import rx.Subscription;
import rx.subscriptions.Subscriptions;
import timber.log.Timber;

import static com.rideaustin.utils.Constants.TEMP_DIRECTORY;

/**
 * Created by RideClient on 8/16/16.
 */
public class FCRACheckInvestigationFragment extends BaseDriverSignUpFragment {

    private MenuItem nextMenuItem;
    private RCRADisclosureCheckInvestigationBinding frcaDisclosureBinding;
    private Subscription fileSubscription = Subscriptions.empty();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle
            savedInstanceState) {
        frcaDisclosureBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_fcra_disclosure_check_investiagation, container, false);
        frcaDisclosureBinding.firstParagraph.setText(getString(R.string.the_fcra_background_check_investigation_first_paragraph, App.capitalizeFirstLetter(getSignUpInteractor().getCityName())));
        setHasOptionsMenu(true);
        setHasHelpWidget(true);
        setToolbarTitle(R.string.title_fcra_disclosure);
        frcaDisclosureBinding.acknowledgeReceipt.setOnCheckedChangeListener((compoundButton, b) -> {
            if (nextMenuItem != null) {
                nextMenuItem.setEnabled(b);
            }
        });
        return frcaDisclosureBinding.getRoot();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        fileSubscription.unsubscribe();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        nextMenuItem = MenuUtil.inflateNextMenu(menu, inflater, true);
        nextMenuItem.setEnabled(frcaDisclosureBinding.acknowledgeReceipt.isChecked());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.next:
                if (checkIsFieldCompleted()) {
                    createLicense();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean checkIsFieldCompleted() {
        if (!frcaDisclosureBinding.acknowledgeReceipt.isChecked()) {
            frcaDisclosureBinding.acknowledgeReceipt.setError(getString(R.string.valid_acknowledge_disclosure));
        } else {
            frcaDisclosureBinding.acknowledgeReceipt.setError(null);
        }

        return true;
    }

    private void createLicense() {
        fileSubscription.unsubscribe();
        fileSubscription = createLicenseObservable()
                .subscribeOn(RxSchedulers.serializer())
                .observeOn(RxSchedulers.main())
                .subscribe(aBoolean -> notifyCompleted(), throwable -> {
                    Timber.e(throwable, "Error while creating license");
                    if (throwable instanceof IOException) {
                        RAToast.show(R.string.error_io_write_disk, Toast.LENGTH_SHORT);
                    } else {
                        RAToast.show(R.string.error_unknown, Toast.LENGTH_SHORT);
                    }
                });
    }

    private Observable<Boolean> createLicenseObservable() {
        return Observable.defer(() -> {
            try {
                File oldLicenseFile = new File(getDriverData().getLicenseFilePath());
                File newLicenseFile = new File(System.getProperty(TEMP_DIRECTORY), getDriverData().getDriverRegistration().getLicenseNumber() + "-licensephoto.png");
                if (oldLicenseFile.getCanonicalPath().equals(newLicenseFile.getCanonicalPath())
                        && newLicenseFile.exists()) {
                    // file already processed
                    return Observable.just(true);
                }

                if (newLicenseFile.exists()) {
                    newLicenseFile.delete();
                }
                FileUtils.moveFile(oldLicenseFile, newLicenseFile);
                getDriverData().setLicenseFilePath(newLicenseFile.getAbsolutePath());
                return Observable.just(true);
            } catch (IOException e) {
                return Observable.error(e);
            }
        });
    }
}
