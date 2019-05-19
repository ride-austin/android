package com.rideaustin.ui.map;

import android.animation.LayoutTransition;
import android.animation.ValueAnimator;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.rideaustin.App;
import com.rideaustin.R;
import com.rideaustin.base.BaseFragment;
import com.rideaustin.databinding.PickupDestinationBinding;
import com.rideaustin.schedulers.RxSchedulers;
import com.rideaustin.utils.ViewUtils;

import java8.util.Optional;
import rx.Subscription;
import rx.subscriptions.Subscriptions;
import timber.log.Timber;


/**
 * Created by rost on 8/16/16.
 */
public class PickupDestinationFragment extends BaseFragment {

    private NavigationListener navigationListener;
    private String startAddress;
    private String finishAddress;
    private boolean isCancelVisible = true;
    private boolean isDestinationVisible = false;
    private boolean isNextVisible = false;
    private boolean isCommentVisible = true;
    private ValueAnimator rotateAnimation = ValueAnimator.ofInt(0, 180).setDuration(200);
    private ValueAnimator maxLinesAnimation = ValueAnimator.ofInt(1, 10).setDuration(200);
    private Subscription riderCommentsSubscription = Subscriptions.empty();
    private MapFragmentInterface.MapPaddingListener mapPaddingListener;
    private ViewTreeObserver.OnGlobalLayoutListener onGlobalLayoutListener;

    public interface NavigationListener {
        void onNavigateClicked();
        void onCanceledClicked();
        void onNextClicked();
    }

    public void setMapPaddingListener(MapFragmentInterface.MapPaddingListener mapPaddingListener) {
        this.mapPaddingListener = mapPaddingListener;
    }

    public void setNavigationListener(NavigationListener navigationListener) {
        this.navigationListener = navigationListener;
    }

    public void showCancelButton(boolean isVisible) {
        isCancelVisible = isVisible;
        if (binding != null) {
            binding.cancel.setVisibility(isCancelVisible ? View.VISIBLE : View.GONE);
        }
    }

    public void showDestination(boolean isVisible) {
        isDestinationVisible = isVisible;
        if (binding != null) {
            binding.finishAddress.setVisibility(isVisible ? View.VISIBLE : View.GONE);
        }
    }

    public void showNextButton(boolean isVisible) {
        isNextVisible = isVisible;
        if (binding != null) {
            binding.next.setVisibility(isNextVisible ? View.VISIBLE : View.GONE);
        }
    }

    public void setStartAddresses(String startAddress) {
        this.startAddress = startAddress;
        setupAddress();
    }

    public void setFinishAddresses(String finishAddresses) {
        this.finishAddress = finishAddresses;
        isDestinationVisible = true;
        setupAddress();
    }

    public void showComment(boolean isVisible) {
        isCommentVisible = isVisible;
        if (binding != null) {
            setupComment();
        }
    }

    public void clear() {
        navigationListener = null;
    }

    private PickupDestinationBinding binding;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_pickup_destination, container, false);
        onGlobalLayoutListener = this::updateMapPadding;
        binding.getRoot().getViewTreeObserver().addOnGlobalLayoutListener(onGlobalLayoutListener);
        ((ViewGroup) binding.getRoot()).getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupAddress();
        setupComment();
        binding.navigate.setOnClickListener(v -> {
            if (navigationListener != null) {
                navigationListener.onNavigateClicked();
            }
        });
        binding.cancel.setOnClickListener(v -> {
            if (navigationListener != null) {
                navigationListener.onCanceledClicked();
            }
        });
        binding.next.setOnClickListener(v -> {
            if (navigationListener != null) {
                navigationListener.onNextClicked();
            }
        });

        binding.next.setVisibility(isNextVisible ? View.VISIBLE : View.GONE);
        binding.cancel.setVisibility(isCancelVisible ? View.VISIBLE : View.GONE);

        rotateAnimation.addUpdateListener(animation -> binding.markImage.setRotation((int) animation.getAnimatedValue()));

        maxLinesAnimation.addUpdateListener(animation -> binding.comment.setMaxLines((int) animation.getAnimatedValue()));

        binding.commentsContainer.setOnClickListener(v -> {
            int maxLines = binding.comment.getMaxLines();
            if (maxLines == 1) {
                maxLinesAnimation.start();
                rotateAnimation.start();
            } else {
                maxLinesAnimation.reverse();
                rotateAnimation.reverse();
            }
        });

        updateMapPadding();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding.getRoot().getViewTreeObserver().removeOnGlobalLayoutListener(onGlobalLayoutListener);
    }

    private void setupAddress() {
        if (binding != null) {
            binding.startingAddress.setText(startAddress);
            binding.finishAddress.setText(finishAddress);
            binding.finishAddress.setVisibility(isDestinationVisible ? View.VISIBLE : View.GONE);
        }
    }

    private void setupComment() {
        riderCommentsSubscription.unsubscribe();
        if (!isCommentVisible) {
            binding.commentsContainer.setVisibility(View.GONE);
        } else {
            riderCommentsSubscription = App.getInstance().getStateManager().getRiderComments()
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .observeOn(RxSchedulers.main())
                    .subscribe(riderComments -> {
                        binding.comment.setText(riderComments);
                        if (TextUtils.isEmpty(riderComments)) {
                            binding.commentsContainer.setVisibility(View.GONE);
                        } else {
                            binding.commentsContainer.setVisibility(View.VISIBLE);
                        }
                    }, throwable -> Timber.w(throwable, "can't update rider comments"));
        }
    }

    private void updateMapPadding() {
        if (mapPaddingListener != null && binding != null) {
            mapPaddingListener.onTopPaddingUpdated(binding.getRoot().getBottom() + (int) ViewUtils.dpToPixels(10));
        }
    }

}
