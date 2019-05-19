package com.rideaustin.ui.driver;

import android.app.Activity;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.view.menu.ActionMenuItemView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.rideaustin.App;
import com.rideaustin.R;
import com.rideaustin.api.config.Alert;
import com.rideaustin.api.config.GenderSelection;
import com.rideaustin.base.BaseFragment;
import com.rideaustin.databinding.FragmentFemaleOnlyBinding;
import com.rideaustin.ui.drawer.promotions.FreeRidesActivity;
import com.rideaustin.ui.utils.Fonts;
import com.rideaustin.utils.DialogUtils;
import com.rideaustin.utils.toast.RAToast;

import java8.util.Optional;

/**
 * Created by supreethks on 25/10/16.
 */

public class FemaleOnlyInfoFragment extends BaseFragment implements FemaleOnlyInfoView {

    private OnFemaleOnlyFeatureToggleListener listener;
    private FragmentFemaleOnlyBinding binding;
    private ViewTreeObserver.OnGlobalLayoutListener pinkifyObserver;
    private FemaleOnlyInfoViewModel viewModel;
    private MaterialDialog dialog;

    public static FemaleOnlyInfoFragment getInstance() {
        FemaleOnlyInfoFragment fragment = new FemaleOnlyInfoFragment();
        Bundle bundle = new Bundle();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean referralEnabled = App.getConfigurationManager().getLastConfiguration().getRiderReferFriend().isEnabled();
        setHasOptionsMenu(referralEnabled);
        if (referralEnabled) {
            pinkifyShareButton();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        binding.switchEnablePinkDrivers.setChecked(App.getDataManager().isFemaleOnlyEnabled());
    }

    @Override
    public void onStop() {
        super.onStop();
        if (pinkifyObserver != null) {
            getRootViewObserver().ifPresent(o -> o.removeOnGlobalLayoutListener(pinkifyObserver));
            pinkifyObserver = null;
        }
    }

    public void setListener(OnFemaleOnlyFeatureToggleListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_female_only, container, false);
        viewModel = new FemaleOnlyInfoViewModel(this);
        binding.setViewModel(viewModel);
        binding.switchLayout.setOnClickListener(v -> {
            if (binding.switchEnablePinkDrivers.isEnabled()) {
                binding.switchEnablePinkDrivers.setChecked(!binding.switchEnablePinkDrivers.isChecked());
            } else {
                viewModel.onDisabledSwitch();
            }
        });
        binding.switchEnablePinkDrivers.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (listener == null) {
                // RA-13968: need to refactor listeners in fragments
                // better to communicate through hosting activity's view model
                RAToast.showShort(R.string.error_unknown);
                if (getActivity() != null) {
                    getActivity().onBackPressed();
                }
                return;
            }
            listener.onFemaleOnlyFeatureToggle(isChecked);
        });
        return binding.getRoot();
    }

    private void pinkifyShareButton() {
        pinkifyObserver = () -> {
            if (getActivity() == null) {
                // RA-14365
                return;
            }
            View shareView = getActivity().findViewById(R.id.menuShare);
            if (shareView instanceof ActionMenuItemView) {
                int color = ContextCompat.getColor(getActivity(), R.color.female_only_pink);
                ((ActionMenuItemView) shareView).setTextColor(color);
                if (pinkifyObserver != null) {
                    getRootViewObserver().ifPresent(o -> o.removeOnGlobalLayoutListener(pinkifyObserver));
                    pinkifyObserver = null;
                }
            }
        };
        getRootViewObserver().ifPresent(o -> o.addOnGlobalLayoutListener(pinkifyObserver));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        viewModel.unsubscribe();
        DialogUtils.dismiss(dialog);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.female_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menuShare) {
            startActivity(new Intent(getContext(), FreeRidesActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTitleLoaded(final String title) {
        setToolbarTitleAligned(title, Gravity.LEFT);
    }

    @Override
    public void onGenderAlert(Alert alert) {
        DialogUtils.dismiss(dialog);
        dialog = new MaterialDialog.Builder(getActivity())
                .typeface(Fonts.MONTSERRAT_REGULAR_OTF, Fonts.MONTSERRAT_LIGHT_OTF)
                .content(alert.getMessage())
                .positiveText(alert.getActionTitle())
                .negativeText(alert.getCancelTitle())
                .onPositive((dialog, which) -> viewModel.onChooseGender())
                .onNegative((dialog, which) -> getActivity().onBackPressed())
                .show();
    }

    @Override
    public void onGenderSelection(final GenderSelection selection, final int index) {
        DialogUtils.dismiss(dialog);
        dialog = new MaterialDialog.Builder(getActivity())
                .typeface(Fonts.MONTSERRAT_REGULAR_OTF, Fonts.MONTSERRAT_LIGHT_OTF)
                .title(selection.getSubtitle())
                .items(selection.getOptions())
                .alwaysCallSingleChoiceCallback()
                .itemsCallbackSingleChoice(index, (dialog, view, which, text) -> {
                    checkSelectionAllowed(selection, which);
                    return true;
                })
                .onPositive((dialog, which) -> selectGender(selection))
                .onNegative((dialog, which) -> getActivity().onBackPressed())
                .positiveText(R.string.btn_ok)
                .negativeText(R.string.btn_cancel)
                .cancelable(false)
                .show();
        checkSelectionAllowed(selection, index);
    }

    private void selectGender(final GenderSelection selection) {
        viewModel.selectGender(selection.getOptions().get(dialog.getSelectedIndex()));
    }

    private void checkSelectionAllowed(final GenderSelection selection, int position) {
        boolean enabled = position > -1 && position < selection.getOptions().size();
        dialog.getActionButton(DialogAction.POSITIVE).setEnabled(enabled);
    }

    private Optional<ViewTreeObserver> getRootViewObserver() {
        return Optional.ofNullable(getActivity())
                .map(Activity::getWindow)
                .map(Window::getDecorView)
                .map(View::getViewTreeObserver);
    }

    public interface OnFemaleOnlyFeatureToggleListener {
        void onFemaleOnlyFeatureToggle(boolean enabled);
    }

}
