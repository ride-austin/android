package com.rideaustin.ui.feedback;

import android.animation.LayoutTransition;
import android.content.DialogInterface;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.rideaustin.App;
import com.rideaustin.R;
import com.rideaustin.api.model.RideCancellationReason;
import com.rideaustin.base.BaseDialogFragment;
import com.rideaustin.databinding.FragmentRideCancellationFeedbackBinding;
import com.rideaustin.schedulers.RxSchedulers;
import com.rideaustin.ui.utils.CommonMaterialDialogCreator;
import com.rideaustin.ui.utils.UIUtils;

import java.util.List;

import java8.util.Optional;
import timber.log.Timber;

/**
 * Created on 21/03/2018
 *
 * @author sdelaysam
 */

public class RideCancellationFeedbackFragment extends BaseDialogFragment<RideCancellationFeedbackViewModel> {

    private Long rideId;
    private Action action;
    private FragmentRideCancellationFeedbackBinding binding;
    private boolean shown = false;

    public void setup(long rideId, @Nullable Action action) {
        this.rideId = rideId;
        this.action = action;
    }

    public boolean isInProgress() {
        return getViewModel().isInProgress();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Window window = getDialog().getWindow();
        if (window != null) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setViewModel(obtainViewModel(RideCancellationFeedbackViewModel.class));
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_ride_cancellation_feedback, container, false);
        binding.setViewModel(getViewModel());
        binding.getRoot().setY(UIUtils.getDisplaySize(getActivity()).y);
        binding.btnSubmit.setOnClickListener(v -> submit());
        binding.btnCancel.setOnClickListener(v -> dismissAnimated());
        getDialog().requestWindowFeature(STYLE_NO_TITLE);
        getDialog().setCancelable(false);
        getDialog().setCanceledOnTouchOutside(false);
        getDialog().setOnKeyListener(this::onKeyAction);
        LayoutTransition layoutTransition = binding.container.getLayoutTransition();
        layoutTransition.enableTransitionType(LayoutTransition.CHANGING);
        return binding.getRoot();
    }

    private boolean onKeyAction(DialogInterface dialogInterface, int keyCode, KeyEvent keyEvent) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            dismissAnimated();
            return true;
        }
        return false;
    }

    @Override
    public void onStart() {
        super.onStart();
        untilStop(getViewModel().getReasons()
                .observeOn(RxSchedulers.main())
                .subscribe(this::doOnReasons, this::doOnError));
        untilStop(getViewModel().getComplete()
                .observeOn(RxSchedulers.main())
                .subscribe(this::doOnComplete, this::doOnError));
    }

    @Override
    public void onResume() {
        super.onResume();
        showAnimated();
    }

    private void doOnReasons(List<RideCancellationReason> reasons) {
        binding.reasons.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(getContext());
        for (RideCancellationReason reason : reasons) {
            TextView textView = (TextView) inflater.inflate(R.layout.view_cancellation_feedback, null, false);
            textView.setText(reason.getDescription());
            textView.setTag(R.id.data_tag, reason);
            textView.setTag(R.id.bool_tag, false);
            textView.setOnClickListener(this::doOnReasonClicked);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            binding.reasons.addView(textView, lp);
        }
        showSelectedReason();
    }

    private void showSelectedReason() {
        Optional<RideCancellationReason> selectedReason = getViewModel().getSelectedReason();
        for (int i = 0; i < binding.reasons.getChildCount(); i++) {
            TextView textView = (TextView) binding.reasons.getChildAt(i);
            boolean selected = false;
            if (selectedReason.isPresent()) {
                RideCancellationReason reason = (RideCancellationReason) textView.getTag(R.id.data_tag);
                selected = selectedReason.get().getCode().equals(reason.getCode());
            }
            boolean wasSelected = Boolean.TRUE.equals(textView.getTag(R.id.bool_tag));
            if (wasSelected != selected) {
                Drawable drawable = ContextCompat.getDrawable(getContext(),
                        selected ? R.drawable.ic_checked : R.drawable.ic_unchecked_white);
                textView.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
                textView.setTag(R.id.bool_tag, selected);
            }
        }
    }

    private void doOnReasonClicked(View view) {
        TextView textView = (TextView) view;
        RideCancellationReason reason = (RideCancellationReason) textView.getTag(R.id.data_tag);
        getViewModel().selectReason(reason);
        showSelectedReason();
    }

    private void doOnComplete(Boolean result) {
        dismissAnimated();
    }

    private void doOnError(Throwable throwable) {
        getViewModel().showErrorMessage(App.getInstance().getString(R.string.error_unknown));
        Timber.e(throwable);
    }

    private void submit() {
        if (rideId != null) {
            getViewModel().submit(rideId, action);
        } else {
            getViewModel().showErrorMessage(App.getInstance().getString(R.string.error_unknown));
        }
    }

    private void showAnimated() {
        if (!shown) {
            CommonMaterialDialogCreator.slideDialogUp(binding.getRoot(), null);
            shown = true;
        }
    }

    private void dismissAnimated() {
        CommonMaterialDialogCreator.slideDialogDown(binding.getRoot(), this::dismiss);
    }

    public interface Action {
        void execute(String code, @Nullable String reason);
    }
}
