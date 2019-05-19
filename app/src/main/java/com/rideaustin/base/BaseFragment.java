package com.rideaustin.base;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.rideaustin.schedulers.RxSchedulers;
import com.rideaustin.ui.BackPressable;
import com.rideaustin.ui.common.BaseView;
import com.rideaustin.ui.common.LifecycleSubscriptions;
import com.rideaustin.ui.common.RxBaseViewModel;
import com.rideaustin.ui.common.ViewModelsProvider;

import java.util.List;

import rx.Subscription;
import timber.log.Timber;

/**
 * @author shumelchyk
 */
public class BaseFragment<T extends RxBaseViewModel> extends Fragment implements BackPressable, FragmentHost, BaseView {

    protected boolean attached;
    private LifecycleSubscriptions subscriptions = new LifecycleSubscriptions();
    private T viewModel;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        attached = true;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        attached = false;
    }

    public boolean isAttached() {
        return attached;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        List<Fragment> fragments = getChildFragmentManager().getFragments();
        if (fragments != null) {
            for (Fragment f : fragments) {
                if (f != null) {
                    f.onActivityResult(requestCode, resultCode, data);
                }
            }
        }
    }

    public void showProgress() {
        getCallback().showProgress();
    }

    public void showProgress(String message) {
        getCallback().showProgress(message);
    }

    public void hideProgress() {
        getCallback().hideProgress();
    }

    protected void replaceFragment(@NonNull Fragment f, int containerId, boolean addToBackStack, String backStackName) {
        FragmentHelper.replaceFragment(this, f, containerId, addToBackStack, backStackName, Transition.NONE);
    }

    protected void replaceFragment(@NonNull Fragment f, int containerId, boolean addToBackStack) {
        FragmentHelper.replaceFragment(this, f, containerId, addToBackStack, Transition.NONE);
    }

    protected void showFragment(@NonNull Fragment fragment) {
        FragmentHelper.showFragment(this, fragment, Transition.NONE);
    }

    protected void hideFragment(@NonNull Fragment fragment) {
        FragmentHelper.hideFragment(this, fragment, Transition.NONE);
    }

    protected void removeFragment(@Nullable Fragment fragment) {
        FragmentHelper.removeFragment(this, fragment, Transition.NONE);
    }

    public void setToolbarTitle(String title) {
        if (isAdded() && !isHidden()) {
            getCallback().setToolbarTitle(title);
        }
    }

    public void setToolbarTitle(@StringRes int stringId) {
        if (isAdded() && !isHidden()) {
            getCallback().setToolbarTitle(stringId);
        }
    }

    protected void setToolbarTitleAligned(String title, int gravity) {
        if (isAdded() && !isHidden()) {
            getCallback().setToolbarTitleAligned(title, gravity);
        }
    }

    protected void setToolbarTitleAligned(@StringRes int stringId, int gravity) {
        if (isAdded() && !isHidden()) {
            getCallback().setToolbarTitleAligned(stringId, gravity);
        }
    }

    public BaseActivityCallback getCallback() {
        return (BaseActivityCallback) getActivity();
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }

    @Override
    public boolean isInvalid() {
        return !attached || isRemoving();
    }

    @Override
    public FragmentManager getSupportFragmentManager() {
        return getChildFragmentManager();
    }

    @Override
    public void commitTransaction(FragmentTransaction transaction) {
        try {
            transaction.commitNowAllowingStateLoss();
        } catch (IllegalStateException e) {
            Timber.e(e, e.getMessage());
        }
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
                                getCallback().showProgress();
                                break;
                            case SHOW_PROGRESS_WITH_MESSAGE:
                                getCallback().showProgress(progressEvent.getMessage().orElse(""));
                                break;
                            case SHOW_LOADING_WHEEL:
                                getCallback().showLoadingWheel();
                                break;
                            case HIDE_PROGRESS:
                                getCallback().hideProgress();
                                break;
                        }
                    }));
        }
    }

}
