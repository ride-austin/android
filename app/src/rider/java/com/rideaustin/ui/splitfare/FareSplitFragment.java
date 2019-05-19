package com.rideaustin.ui.splitfare;

import android.Manifest;
import android.content.Intent;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.MaterialDialog;
import com.rideaustin.R;
import com.rideaustin.api.model.faresplit.FareSplitResponse;
import com.rideaustin.base.BaseFragment;
import com.rideaustin.databinding.FragmentFareSplitBinding;
import com.rideaustin.databinding.ViewFareSplitItemBinding;
import com.rideaustin.schedulers.RxSchedulers;
import com.rideaustin.ui.utils.MaterialDialogCreator;
import com.rideaustin.utils.DialogUtils;
import com.rideaustin.utils.PermissionUtils;
import com.rideaustin.utils.toast.RAToast;
import com.tbruyelle.rxpermissions.RxPermissions;

import java.util.List;

import rx.Subscription;
import rx.subscriptions.Subscriptions;
import timber.log.Timber;

import static android.app.Activity.RESULT_OK;

/**
 * Created by supreethks on 02/10/16.
 */

public class FareSplitFragment extends BaseFragment<FareSplitViewModel> {

    private static final int PICK_CONTACT = 1001;

    private FragmentFareSplitBinding binding;
    private MaterialDialog dialog;
    private Subscription permissionSubscription = Subscriptions.empty();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setToolbarTitle(R.string.title_split_fare);
        setViewModel(obtainViewModel(FareSplitViewModel.class));
        getViewModel().initialize();
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_fare_split, container, false);
        binding.setViewModel(getViewModel());
        binding.btnShowContact.setOnClickListener(v -> onShowContactPicker());
        return binding.getRoot();
    }

    private void onShowContactPicker() {
        String permission = Manifest.permission.READ_CONTACTS;
        permissionSubscription.unsubscribe();
        permissionSubscription = new RxPermissions(getActivity())
                .request(permission)
                .subscribe(isGranted -> {
                    if (isGranted) {
                        Intent i = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
                        startActivityForResult(i, PICK_CONTACT);
                    } else {
                        PermissionUtils.checkDeniedPermissions(getActivity(), permission);
                    }
                }, throwable -> {
                    Timber.e(throwable);
                    RAToast.showShort(R.string.error_unknown);
                });
        untilDestroy(permissionSubscription);
    }

    @Override
    public void onStart() {
        super.onStart();
        untilStop(getViewModel().getCloseObservable()
                .observeOn(RxSchedulers.main())
                .subscribe(aVoid -> getActivity().onBackPressed(), Timber::e));
        untilStop(getViewModel().getListObservable()
                .observeOn(RxSchedulers.main())
                .subscribe(this::doOnList, Timber::e));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_CONTACT && resultCode == RESULT_OK) {
            Uri contactUri = data.getData();
            Cursor cursor = getContext().getContentResolver().query(contactUri, null, null, null, null);
            try {
                if (cursor != null) {
                    cursor.moveToFirst();
                    int columnNumber = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                    getViewModel().phone.set(cursor.getString(columnNumber));
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        DialogUtils.dismiss(dialog);
    }

    private void doOnList(List<FareSplitItemViewModel> list) {
        int numAccepted = 0;
        int numRequested = 0;
        int numDeclined = 0;
        binding.containerAccepted.removeAllViews();
        binding.containerRequested.removeAllViews();
        binding.containerDeclined.removeAllViews();
        for (FareSplitItemViewModel viewModel : list) {
            ViewFareSplitItemBinding itemBinding = DataBindingUtil.inflate(getLayoutInflater(),
                    R.layout.view_fare_split_item, null, false);
            itemBinding.setViewModel(viewModel);
            itemBinding.ivClose.setTag(viewModel);
            itemBinding.ivClose.setOnClickListener(this::doOnDelete);
            switch (viewModel.getStatus()) {
                case ACCEPTED:
                    binding.containerAccepted.addView(itemBinding.getRoot());
                    numAccepted++;
                    break;
                case REQUESTED:
                    binding.containerRequested.addView(itemBinding.getRoot());
                    numRequested++;
                    break;
                case DECLINED:
                    binding.containerDeclined.addView(itemBinding.getRoot());
                    numDeclined++;
                    break;
            }
        }
        if (numAccepted > 0) {
            binding.tvAccepted.setText(getString(R.string.fare_split_accepted_title, numAccepted + 1));
            binding.tvAccepted.setVisibility(View.VISIBLE);
        } else {
            binding.tvAccepted.setVisibility(View.GONE);
        }
        binding.tvRequested.setVisibility(numRequested > 0 ? View.VISIBLE : View.GONE);
        binding.tvDeclined.setVisibility(numDeclined > 0 ? View.VISIBLE : View.GONE);
    }

    private void doOnDelete(View view) {
        DialogUtils.dismiss(dialog);
        FareSplitItemViewModel viewModel = (FareSplitItemViewModel) view.getTag();
        dialog = MaterialDialogCreator.createConfirmDialog(getString(R.string.fare_split_delete_rider, viewModel.name.get()), (AppCompatActivity) getActivity())
                .positiveText(getString(R.string.btn_yes))
                .onPositive((dialog, which) -> getViewModel().deleteFareSplitRequest(viewModel.getId()))
                .negativeText(getString(R.string.btn_no))
                .show();
    }
}
