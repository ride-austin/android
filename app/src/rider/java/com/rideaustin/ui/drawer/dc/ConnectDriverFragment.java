package com.rideaustin.ui.drawer.dc;

import android.Manifest;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.internal.MDButton;
import com.rideaustin.App;
import com.rideaustin.R;
import com.rideaustin.base.BaseFragment;
import com.rideaustin.databinding.FragmentConnectDriverBinding;
import com.rideaustin.schedulers.RxSchedulers;
import com.rideaustin.ui.common.ViewModelsProvider;
import com.rideaustin.ui.drawer.NavigationDrawerActivity;
import com.rideaustin.ui.utils.MaterialDialogCreator;
import com.rideaustin.utils.Constants;
import com.rideaustin.utils.DialogUtils;
import com.rideaustin.utils.PermissionUtils;
import com.rideaustin.utils.toast.RAToast;
import com.tbruyelle.rxpermissions.RxPermissions;

import timber.log.Timber;


/**
 * Created by hatak on 24.10.2017.
 */

public class ConnectDriverFragment extends BaseFragment<ConnectDriverViewModel> {

    private FragmentConnectDriverBinding binding;
    private MaterialDialog cancelDialog;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setToolbarTitleAligned(R.string.connect_driver_title, Gravity.LEFT);
        setViewModel(obtainViewModel(ConnectDriverViewModel.class));
        getViewModel().setModel(ViewModelsProvider.of(DirectConnectActivity.class).get(DirectConnectViewModel.class));
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_connect_driver, container, false);
        binding.setViewModel(getViewModel());
        binding.requestDriver.setOnClickListener(v -> checkPermissions());
        binding.cancel.setOnClickListener(v -> cancel(null));
        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        untilStop(getViewModel().getRideStarted()
                .observeOn(RxSchedulers.main())
                .subscribe(result -> {
                    if (result) {
                        Intent intent = new Intent(getActivity(), NavigationDrawerActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.putExtra(Constants.EXTRA_KEY_RETURN_TO_MAP, true);
                        getActivity().overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                        getActivity().startActivity(intent);
                    }
                }));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        DialogUtils.dismiss(cancelDialog);
    }

    @Override
    public boolean onBackPressed() {
        if (getViewModel().requesting.get()) {
            cancel(() -> getActivity().onBackPressed());
            return true;
        }
        return false;
    }

    private void checkPermissions() {
        String[] permissions = {
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
        };
        untilDestroy(new RxPermissions(getActivity())
                .request(permissions)
                .subscribe(granted -> {
                    if (granted) {
                        requestDriver();
                    } else {
                        PermissionUtils.checkDeniedPermissions(getActivity(), permissions);
                    }
                }, throwable -> {
                    Timber.e(throwable);
                    RAToast.show(R.string.error_unknown, Toast.LENGTH_SHORT);
                }));
    }

    private void requestDriver() {
        untilDestroy(getViewModel()
                .getRequestIntent()
                .subscribeOn(RxSchedulers.computation())
                .observeOn(RxSchedulers.main())
                .subscribe(intent -> {
                    getActivity().startService(intent);
                }, throwable -> {
                    Timber.e(throwable);
                    RAToast.show(R.string.error_unknown, Toast.LENGTH_SHORT);
                }));
    }

    private void cancel(@Nullable Runnable onCancelled) {
        DialogUtils.dismiss(cancelDialog);
        cancelDialog = MaterialDialogCreator.createCancelRideDialog((AppCompatActivity) getActivity());
        cancelDialog.setContent(App.getInstance().getString(R.string.direct_connect_cancel_request));
        cancelDialog.setOnShowListener(dialogInterface -> {
            MDButton positiveButton = cancelDialog.getActionButton(DialogAction.POSITIVE);
            positiveButton.setOnClickListener(v -> {
                getViewModel().cancel();
                dialogInterface.dismiss();
                if (onCancelled != null) {
                    onCancelled.run();
                }
            });
        });
    }

}
