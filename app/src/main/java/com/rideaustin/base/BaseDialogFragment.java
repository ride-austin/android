package com.rideaustin.base;

import android.content.Context;
import android.support.annotation.CallSuper;
import android.support.v4.app.DialogFragment;

import com.rideaustin.schedulers.RxSchedulers;
import com.rideaustin.ui.common.BaseView;
import com.rideaustin.ui.common.LifecycleSubscriptions;
import com.rideaustin.ui.common.RxBaseViewModel;
import com.rideaustin.ui.common.ViewModelsProvider;

import rx.Subscription;

/**
 * Created on 21/12/2017
 *
 * @author sdelaysam
 */

public class BaseDialogFragment<T extends RxBaseViewModel> extends DialogFragment implements BaseView {

    private boolean attached;
    private BaseActivityCallback callback;
    private LifecycleSubscriptions subscriptions = new LifecycleSubscriptions();
    private T viewModel;

    public boolean isAttached() {
        return attached;
    }

    public void showProgress() {
        callback.showProgress();
    }

    public void showProgress(String message) {
        callback.showProgress(message);
    }

    public void hideProgress() {
        callback.hideProgress();
    }

    /**
     * In most cases {@code VM} is {@code T}.
     * Just leaving to ability to obtain view model of other types as well.
     */
    protected final <VM extends RxBaseViewModel> VM obtainViewModel(Class<VM> viewModelClass) {
        return ViewModelsProvider.of(getClass()).get(viewModelClass);
    }

    @CallSuper
    protected void clearViewModels() {
        ViewModelsProvider.clear(getClass());
        viewModel = null;
    }

    protected final T getViewModel() {
        return viewModel;
    }

    protected final void setViewModel(T viewModel) {
        this.viewModel = viewModel;
    }

    protected final void untilPause(Subscription subscription) {
        subscriptions.untilPause(subscription);
    }

    protected final void untilStop(Subscription subscription) {
        subscriptions.untilStop(subscription);
    }

    protected final void untilDestroy(Subscription subscription) {
        subscriptions.untilDestroy(subscription);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            this.callback = (BaseActivityCallback) context;
            attached = true;
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("BaseFragment can be attached only to BaseActivity", e);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        attached = false;
    }

    @Override
    public void onStart() {
        super.onStart();
        subscribeToProgressEvents();
        if (viewModel != null) {
            viewModel.onStart();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (viewModel != null) {
            viewModel.onResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        subscriptions.onPause();
        if (viewModel != null) {
            viewModel.onPause();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        subscriptions.onStop();
        if (viewModel != null) {
            viewModel.onStop();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        subscriptions.onDestroy();
        if (viewModel != null) {
            viewModel.onDestroy();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        clearViewModels();
    }

    private void subscribeToProgressEvents() {
        if (viewModel != null) {
            untilStop(viewModel.getProgressEvents()
                    .subscribeOn(RxSchedulers.main())
                    .subscribe(progressEvent -> {
                        switch (progressEvent.getType()) {
                            case SHOW_PROGRESS:
                                callback.showProgress();
                                break;
                            case SHOW_PROGRESS_WITH_MESSAGE:
                                callback.showProgress(progressEvent.getMessage().orElse(""));
                                break;
                            case SHOW_LOADING_WHEEL:
                                callback.showLoadingWheel();
                                break;
                            case HIDE_PROGRESS:
                                callback.hideProgress();
                                break;
                        }
                    }));
        }
    }


}
