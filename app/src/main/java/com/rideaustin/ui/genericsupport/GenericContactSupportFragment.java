package com.rideaustin.ui.genericsupport;

import android.app.Dialog;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.OvershootInterpolator;

import com.rideaustin.R;
import com.rideaustin.base.BaseFragment;
import com.rideaustin.databinding.GenericContactSupportMessageBinding;
import com.rideaustin.schedulers.RxSchedulers;

import java.util.concurrent.TimeUnit;

import java8.util.Optional;
import timber.log.Timber;

/**
 * Created by Sergey Petrov on 17/03/2017.
 */

public class GenericContactSupportFragment extends BaseFragment implements GenericContactSupportViewModel.View {

    private static final String RIDE_KEY = "ride_key";
    private static final String CITY_KEY = "city_key";
    private static final int POPUP_SHOW_MS = 2200;
    private static final int POPUP_ANIMATION_MS = 350;
    private static final int POPUP_ANIMATION_DELAY_MS = 150;

    private GenericContactSupportViewModel viewModel;

    public static GenericContactSupportFragment newInstance(Optional<Long> rideId, Optional<Integer> cityId) {
        GenericContactSupportFragment fragment = new GenericContactSupportFragment();
        Bundle args = new Bundle();
        args.putLong(RIDE_KEY, rideId.orElse(-1L));
        args.putInt(CITY_KEY, cityId.orElse(-1));
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setToolbarTitleAligned(R.string.text_contact_support, Gravity.CENTER);
        GenericContactSupportMessageBinding binding = DataBindingUtil.inflate(inflater, R.layout.generic_contact_support_message, container, false);
        long rideId = getArguments() != null && getArguments().containsKey(RIDE_KEY) ? getArguments().getLong(RIDE_KEY) : -1;
        int cityId = getArguments() != null && getArguments().containsKey(CITY_KEY) ? getArguments().getInt(CITY_KEY) : -1;
        viewModel = new GenericContactSupportViewModel(this, rideId, cityId);
        binding.setViewModel(viewModel);
        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        viewModel.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        viewModel.onStop();
    }

    @Override
    public void onMessageSent() {
        if (isAttached()) {
            showDialog();
        }
    }

    private void showDialog() {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_support_message_sent, null);
        Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(view);
        final Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
            window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        dialog.show();

        View content = view.findViewById(R.id.viewContent);
        RxSchedulers.schedule(() -> animateContent(dialog, content), 100, TimeUnit.MILLISECONDS); // add small delay to prevent animation twitch
    }

    private void hideDialog(Dialog dialog, View content) {
        content.animate()
                .scaleX(0.0f).scaleY(0.0f)
                .setInterpolator(new AnticipateInterpolator())
                .setDuration(POPUP_ANIMATION_MS)
                .withEndAction(() -> dismissDialog(dialog))
                .start();
    }

    private void animateContent(Dialog dialog, View content) {
        content.setScaleX(0.0f);
        content.setScaleY(0.0f);
        content.setVisibility(View.VISIBLE);
        content.animate()
                .scaleX(1).scaleY(1)
                .setInterpolator(new OvershootInterpolator())
                .setStartDelay(POPUP_ANIMATION_DELAY_MS)
                .setDuration(POPUP_ANIMATION_MS)
                .start();
        RxSchedulers.schedule(() -> hideDialog(dialog, content), POPUP_SHOW_MS, TimeUnit.MILLISECONDS);
    }

    private void dismissDialog(Dialog dialog) {
        RxSchedulers.schedule(() -> {
            try {
                dialog.dismiss();
                getActivity().onBackPressed();
            } catch (Exception e) {
                Timber.e(e, "Unable to hide message sent dialog");
            }
        }, 100, TimeUnit.MILLISECONDS); // add small delay to prevent animation twitch

    }

}
