package com.rideaustin.ui.driver;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rideaustin.App;
import com.rideaustin.R;
import com.rideaustin.base.BaseFragment;
import com.rideaustin.databinding.FragmentFingerprintedOnlyBinding;
import com.rideaustin.schedulers.RxSchedulers;
import com.rideaustin.utils.toast.RAToast;

/**
 * Created on 11/11/18.
 *
 * @author sdelaysam
 */
public class FingerprintedOnlyInfoFragment extends BaseFragment<FingerprintedOnlyInfoViewModel> {

    private FragmentFingerprintedOnlyBinding binding;
    private OnFingerprintOnlyFeatureToggleListener listener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setViewModel(obtainViewModel(FingerprintedOnlyInfoViewModel.class));
        setToolbarTitleAligned(R.string.title_fingerprinted_only, Gravity.LEFT);
        untilDestroy(getViewModel()
                .observeTitle()
                .observeOn(RxSchedulers.main())
                .subscribe(title -> setToolbarTitleAligned(title, Gravity.LEFT)));
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_fingerprinted_only, container, false);
        binding.setViewModel(getViewModel());
        binding.switchLayout.setOnClickListener(v -> {
            if (binding.switchFingerprintedDrivers.isEnabled()) {
                binding.switchFingerprintedDrivers.setChecked(!binding.switchFingerprintedDrivers.isChecked());
            } else {
                getViewModel().onDisabledSwitch();
            }
        });
        binding.switchFingerprintedDrivers.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (listener == null) {
                // RA-13968: need to refactor listeners in fragments
                // better to communicate through hosting activity's view model
                RAToast.showShort(R.string.error_unknown);
                if (getActivity() != null) {
                    getActivity().onBackPressed();
                }
                return;
            }
            listener.onFingerprintOnlyFeatureToggle(isChecked);
        });
        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        binding.switchFingerprintedDrivers.setChecked(App.getDataManager().isFingerprintedOnlyEnabled());
    }


    public void setListener(OnFingerprintOnlyFeatureToggleListener listener) {
        this.listener = listener;
    }

    public interface OnFingerprintOnlyFeatureToggleListener {
        void onFingerprintOnlyFeatureToggle(boolean enabled);
    }

}
