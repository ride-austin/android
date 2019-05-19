package com.rideaustin.ui.base;

import android.support.v4.app.DialogFragment;
import android.widget.Toast;

import com.rideaustin.App;
import com.rideaustin.R;
import com.rideaustin.api.model.ServerMessage;
import com.rideaustin.api.model.UpgradeRequest;
import com.rideaustin.api.model.UpgradeRequestStatus;
import com.rideaustin.base.BaseActivity;
import com.rideaustin.ui.common.RxBaseViewModel;
import com.rideaustin.ui.rideupgrade.RideUpgradeDialog;
import com.rideaustin.ui.utils.CommonMaterialDialogCreator;
import com.rideaustin.utils.DialogUtils;
import com.rideaustin.utils.toast.RAToast;

import java8.util.Optional;

/**
 * Created by hatak on 21.06.2017.
 */

public abstract class BaseRiderActivity<T extends RxBaseViewModel> extends BaseActivity<T> implements BaseRiderViewModel.RiderBaseView {

    private RideUpgradeDialog rideUpgradeDialog;
    BaseRiderViewModel viewModel = new BaseRiderViewModel();

    @Override
    public void onStart() {
        super.onStart();
        viewModel.startRideUpgradeListening(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        viewModel.stopRideUpgradeListening();
    }

    @Override
    public void onRideUpgradeFailed(UpgradeRequestStatus status) {
        onRideUpgradeDialogClose();
        App.getNotificationManager().cancelCarUpgradeNotification();
        int message = getRideUpgradeFailedMessage(status);
        if (message > 0) {
            RAToast.show(message, Toast.LENGTH_LONG);
        }
    }

    private int getRideUpgradeFailedMessage(UpgradeRequestStatus status) {
        switch (status) {
            case CANCELLED:
            case DECLINED:
                return R.string.upgrade_ride_cancelled_msg;
            case EXPIRED:
                return R.string.upgrade_ride_expired_msg;
        }
        return 0;
    }

    @Override
    public void onRideUpgradeRequested(UpgradeRequest upgradeRequest) {
        if (rideUpgradeDialog == null) {
            rideUpgradeDialog = RideUpgradeDialog.create(upgradeRequest,
                    () -> viewModel.acceptRideUpgrade(this, this),
                    () -> viewModel.declineRideUpgrade(this));
            rideUpgradeDialog.show(getSupportFragmentManager());
        }
    }

    @Override
    public void onRideUpgradeResponse(ServerMessage serverMessage) {
        DialogUtils.dismiss(Optional.ofNullable(rideUpgradeDialog)
                .map(DialogFragment::getDialog)
                .orElse(null));
        rideUpgradeDialog = null;
        CommonMaterialDialogCreator.showSupportSuccessDialog(this, Optional.of(serverMessage.getMessage()), false, () -> {});
    }

    @Override
    public void onRideUpgradeDialogClose() {
        DialogUtils.dismiss(Optional.ofNullable(rideUpgradeDialog)
                .map(DialogFragment::getDialog)
                .orElse(null));
        rideUpgradeDialog = null;
    }
}
