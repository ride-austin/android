package com.rideaustin.ui.signup.driver;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rideaustin.R;
import com.rideaustin.api.model.driver.DriverRegistration;
import com.rideaustin.api.model.driver.DriverUserRegistration;
import com.rideaustin.base.ApiSubscriber2;
import com.rideaustin.databinding.RCRADisclosureCheckAuthorizationBinding;
import com.rideaustin.schedulers.RxSchedulers;
import com.rideaustin.ui.utils.MenuUtil;
import com.rideaustin.utils.DateHelper;
import com.rideaustin.utils.FileDirectoryUtil;
import com.rideaustin.utils.KeyboardUtil;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import java8.util.Optional;
import rx.Observable;
import rx.Subscription;
import rx.subscriptions.Subscriptions;

import static com.rideaustin.utils.Constants.TEMP_DIRECTORY;

/**
 * Created by RideClient on 8/17/16.
 */
public class FCRACheckAuthorizationFragment extends BaseDriverSignUpFragment {

    private MenuItem nextMenuItem;
    private RCRADisclosureCheckAuthorizationBinding frcaDisclosureBinding;
    private Subscription fileSubscription = Subscriptions.empty();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle
            savedInstanceState) {
        frcaDisclosureBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_fcra_disclosure_check_authorization, container, false);
        setHasOptionsMenu(true);
        setHasHelpWidget(true);
        setToolbarTitle(R.string.title_fcra_disclosure);
        return frcaDisclosureBinding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        KeyboardUtil.showKeyBoard(getContext(), frcaDisclosureBinding.fcraAuthorizationFull);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        fileSubscription.unsubscribe();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        nextMenuItem = MenuUtil.inflateNextMenu(menu, inflater, true);
        nextMenuItem.setEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.next:
                checkIsFieldCompleted();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void checkIsFieldCompleted() {
        if (!isSigned()) {
            frcaDisclosureBinding.fcraAuthorizationFull.requestFocus();
            frcaDisclosureBinding.fcraAuthorizationFull.setError(getString(R.string.valid_electronic_signature));
        } else {
            frcaDisclosureBinding.fcraAuthorizationFull.setError(null);
            createDriverFiles();
        }
    }

    private boolean isSigned() {
        String signStr = frcaDisclosureBinding.fcraAuthorizationFull.getText().toString();
        DriverUserRegistration user = getDriverData().getDriverRegistration().getUser();
        boolean checkMiddleName = !TextUtils.isEmpty(getValueOfNullable(user.getMiddleName()));

        String pattern = "^(\\s*)" + getValueOfNullable(user.getFirstName()).toLowerCase() +
                "(\\s+)" + (checkMiddleName ? getValueOfNullable(user.getMiddleName()).toLowerCase() + "(\\s+)" : "") +
                getValueOfNullable(user.getLastName()).toLowerCase() + "(\\s*)$";

        return !TextUtils.isEmpty(signStr) && signStr.trim().toLowerCase().matches(pattern);
    }

    private String getValueOfNullable(@Nullable String value) {
        return Optional.ofNullable(value).orElse("").trim();
    }

    private void createDriverFiles() {
        fileSubscription.unsubscribe();
        fileSubscription = createDriverFilesObservable()
                .subscribeOn(RxSchedulers.serializer())
                .observeOn(RxSchedulers.main())
                .subscribe(new ApiSubscriber2<Boolean>(getCallback()) {
                    @Override
                    public void onCompleted() {
                        super.onCompleted();
                        KeyboardUtil.hideKeyBoard(getContext(), frcaDisclosureBinding.fcraAuthorizationFull);
                        notifyCompleted();
                    }
                });
    }

    private Observable<Boolean> createDriverFilesObservable() {
        return Observable.defer(() -> {
            try {
                // create driver info file
                Gson gson = new GsonBuilder().create();
                DriverRegistration driver = getDriverData().getDriverRegistration();
                Date licenseExpiration = DateHelper.getUtcDateWithoutShift(getDriverData().getLicenseExpirationDate());
                Date insuranceExpiration = DateHelper.getUtcDateWithoutShift(getDriverData().getInsuranceExpirationDate());
                driver.setLicenseExpiryDate(DateHelper.dateToServerDateTimeFormat(licenseExpiration));
                driver.setInsuranceExpiryDate(DateHelper.dateToServerDateTimeFormat(insuranceExpiration));
                File driveJsonFile = FileDirectoryUtil.createOrRewriteFile(System.getProperty(TEMP_DIRECTORY), "driver.json", gson.toJson(getDriverData().getDriverRegistration()));
                getDriverData().setDriverFilePath(driveJsonFile.getAbsolutePath());

                // create insurance image file
                File insuranceFile = new File(getDriverData().getInsuranceFilePath());
                File newInsuranceFile = new File(System.getProperty(TEMP_DIRECTORY), getDriverData().getDriverRegistration().getLicenseNumber() + "-insurance.png");
                if (insuranceFile.getCanonicalPath().equals(newInsuranceFile.getCanonicalPath())
                        && newInsuranceFile.exists()) {
                    // file already processed
                    return Observable.just(true);
                }

                if (newInsuranceFile.exists()) {
                    newInsuranceFile.delete();
                }
                FileUtils.moveFile(insuranceFile, newInsuranceFile);
                getDriverData().setInsuranceFilePath(newInsuranceFile.getAbsolutePath());

                return Observable.just(true);
            } catch (IOException e) {
                return Observable.error(e);
            }
        });
    }

}
