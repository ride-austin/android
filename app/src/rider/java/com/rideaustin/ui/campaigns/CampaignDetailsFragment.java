package com.rideaustin.ui.campaigns;

import android.app.Activity;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.view.menu.ActionMenuItemView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;

import com.rideaustin.R;
import com.rideaustin.api.model.campaigns.CampaignDetails;
import com.rideaustin.base.BaseFragment;
import com.rideaustin.databinding.FragmentCampaignDetailsBinding;
import com.rideaustin.utils.toast.RAToast;

import java8.util.Optional;

/**
 * Created on 5/20/18.
 *
 * @author sdelaysam
 */
public class CampaignDetailsFragment extends BaseFragment {

    private static final String DATA_KEY = "data_key";
    private Optional<CampaignDetails> campaignDetails = Optional.empty();
    private ViewTreeObserver.OnGlobalLayoutListener menuItemObserver;

    public static CampaignDetailsFragment getInstance(CampaignDetails details) {
        CampaignDetailsFragment fragment = new CampaignDetailsFragment();
        Bundle args = new Bundle();
        args.putSerializable(DATA_KEY, details);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FragmentCampaignDetailsBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_campaign_details, container, false);
        Optional.ofNullable(getArguments())
                .filter(args -> args.containsKey(DATA_KEY))
                .map(args -> (CampaignDetails) args.getSerializable(DATA_KEY))
                .ifPresentOrElse(details -> {
                    setToolbarTitle(details.getHeaderTitle());
                    binding.setDetails(details);
                    campaignDetails = Optional.of(details);
                }, () -> {
                    RAToast.showLong(R.string.campaign_details_empty);
                    onBackPressed();
                });
        setHasOptionsMenu(true);
        tintMenuButton();
        return binding.getRoot();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.campaign_details_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menuViewMap) {
            campaignDetails.ifPresent(details -> ((CampaignDetailsActivity) getActivity()).onShowMap(details));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (menuItemObserver != null) {
            getRootViewObserver().ifPresent(o -> o.removeOnGlobalLayoutListener(menuItemObserver));
            menuItemObserver = null;
        }
    }

    private void tintMenuButton() {
        menuItemObserver = () -> {
            if (getActivity() == null) {
                // RA-14365
                return;
            }
            View menuButton = getActivity().findViewById(R.id.menuViewMap);
            if (menuButton instanceof ActionMenuItemView) {
                int color = ContextCompat.getColor(getActivity(), R.color.drawer_icon);
                ((ActionMenuItemView) menuButton).setTextColor(color);
                if (menuItemObserver != null) {
                    getRootViewObserver().ifPresent(o -> o.removeOnGlobalLayoutListener(menuItemObserver));
                    menuItemObserver = null;
                }
            }
        };
        getRootViewObserver().ifPresent(o -> o.addOnGlobalLayoutListener(menuItemObserver));
    }

    private Optional<ViewTreeObserver> getRootViewObserver() {
        return Optional.ofNullable(getActivity())
                .map(Activity::getWindow)
                .map(Window::getDecorView)
                .map(View::getViewTreeObserver);
    }



}
