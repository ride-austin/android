package com.rideaustin.ui.terms;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.MaterialDialog;
import com.rideaustin.App;
import com.rideaustin.R;
import com.rideaustin.base.BaseActivity;
import com.rideaustin.base.BaseFragment;
import com.rideaustin.databinding.TermsBinding;
import com.rideaustin.models.TermsResponse;
import com.rideaustin.schedulers.RxSchedulers;
import com.rideaustin.ui.common.RxBaseViewModel;
import com.rideaustin.ui.genericsupport.GenericContactSupportFragment;
import com.rideaustin.ui.utils.MaterialDialogCreator;
import com.rideaustin.utils.DialogUtils;

import java8.util.Optional;
import rx.Subscription;
import rx.subscriptions.Subscriptions;

/**
 * Fragment to show new T&C acceptance UI.
 * Reference: RA-9658
 * Created by Sergey Petrov on 23/05/2017.
 */

public class TermsFragment extends BaseFragment<TermsViewModel> {

    private TermsViewModel viewModel;
    private MaterialDialog termsDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        TermsBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_terms, null, false);
        binding.setViewModel(getViewModelInternal());
        binding.needHelp.setOnClickListener(v -> {
            int selectedCityId = App.getConfigurationManager().getLastConfiguration().getCurrentCity().getCityId();
            GenericContactSupportFragment messageFragment = GenericContactSupportFragment.newInstance(Optional.empty(), Optional.of(selectedCityId));
            ((BaseActivity) getActivity()).replaceFragment(messageFragment, R.id.content_frame, true);
        });
        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        untilStop(viewModel.getBadResponseObservable()
                .observeOn(RxSchedulers.main())
                .subscribe(this::doOnBadResponse));
        untilStop(viewModel.getFinishObservable()
                .observeOn(RxSchedulers.main())
                .subscribe(value -> getCallback().finish()));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        hideTermsDialog();
    }

    private void doOnBadResponse(@Nullable TermsResponse response) {
        hideTermsDialog();
        if (response != null) {
            String title = getString(R.string.text_oops);
            if (response.shouldRetry()) {
                termsDialog = MaterialDialogCreator.createDialogWithCallback(getActivity(), title, response.getError(), R.string.retry,
                        (dialog, which) -> viewModel.loadTerms())
                        .cancelable(false).negativeText("")
                        .show();
            } else {
                termsDialog = MaterialDialogCreator.createErrorDialog(title, response.getError(), (AppCompatActivity) getActivity());
            }
        }
    }

    private void hideTermsDialog() {
        DialogUtils.dismiss(termsDialog, "Unable to hide terms dialog");
        termsDialog = null;
    }

    private TermsViewModel getViewModelInternal() {
        if (viewModel == null) {
            viewModel = new TermsViewModel();
        }
        return viewModel;
    }
}
