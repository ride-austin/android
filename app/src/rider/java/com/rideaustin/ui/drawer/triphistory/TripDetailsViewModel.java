package com.rideaustin.ui.drawer.triphistory;

import android.app.Activity;
import android.databinding.Bindable;
import android.databinding.BindingAdapter;
import android.databinding.ObservableBoolean;
import android.text.TextUtils;
import android.widget.ImageView;

import com.rideaustin.App;
import com.rideaustin.BR;
import com.rideaustin.R;
import com.rideaustin.api.model.SupportTopic;
import com.rideaustin.api.model.paymenthistory.PaymentHistory;
import com.rideaustin.databinding.FragmentTripDetailsBinding;
import com.rideaustin.ui.drawer.triphistory.base.BaseSupportTopicsViewModel;
import com.rideaustin.ui.payment.PaymentType;
import com.rideaustin.ui.utils.ImagePopupHelper;
import com.rideaustin.utils.Constants;
import com.rideaustin.utils.ImageHelper;

import java.util.List;

/**
 * Created by Sergey Petrov on 15/03/2017.
 */

public class TripDetailsViewModel extends BaseSupportTopicsViewModel {

    private final View view;

    private final FragmentTripDetailsBinding binding;

    private PaymentHistory history;

    private ImagePopupHelper imagePopupHelper;

    private ObservableBoolean shouldShowHelpSection = new ObservableBoolean(false);

    public TripDetailsViewModel(View view, FragmentTripDetailsBinding binding) {
        super(view);
        this.view = view;
        this.binding = binding;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!App.getDataManager().isLoggedIn()) {
            // app would be sent to login on resume
            return;
        }
        if (App.getDataManager().getTripHistoryModel() == null || App.getDataManager().getSupportTopicsModel() == null) {
            // unexpected state
            view.onUnexpectedState();
            return;
        }
        history = App.getDataManager().getTripHistoryModel().getSelectedHistory();
        if (history == null) {
            // unexpected state
            view.onUnexpectedState();
            return;
        }

        if (history.getCardNumber() != null) {
            binding.textViewPaymentCard.setText(App.getInstance().getString(R.string.masked_credit_card_number, history.getCardNumber()));
            binding.paymentProviderLogo.setImageResource(PaymentType.parse(history.getUsedCardBrand()).getIconResId());
        } else {
            binding.textViewPaymentCard.setText(Constants.BEVO_BUCKS_CARD_NUMBER);
            binding.paymentProviderLogo.setImageResource(PaymentType.BEVO_BUCKS.getIconResId());
        }

        binding.imageRideMap.setOnClickListener(v -> showImagePopup(history.getMapUrl()));

        if (!history.isCancelled()) {
            String driverLabel = history.getDriverNickName();
            if (TextUtils.isEmpty(driverLabel)) {
                driverLabel = history.getDriverFirstName();
            }
            if (TextUtils.isEmpty(driverLabel)) {
                driverLabel = history.getDriverLastName();
            }
            if (history.getDriverRating() != null) {
                binding.ratingBar.setScore(history.getDriverRating());
                binding.ratingBar.setVisibility(android.view.View.VISIBLE);
                binding.textViewRate.setText(App.getInstance().getString(R.string.trip_history_rated_driver, driverLabel));
            } else {
                binding.ratingBar.setVisibility(android.view.View.GONE);
                binding.textViewRate.setText(App.getInstance().getString(R.string.trip_history_your_driver, driverLabel));
            }
            if (history.getDriverPicture() != null) {
                ImageHelper.loadRoundImageIntoView(binding.imageDriver, history.getDriverPicture(), R.drawable.ic_user_icon);
            }
            binding.imageDriver.setOnClickListener(v -> showImagePopup(history.getDriverPicture()));
        }

        notifyPropertyChanged(BR.history);

        shouldShowHelpSection.set(false);
        observeTopics(App.getDataManager().getSupportTopicsModel().getParentTopicsObservable());
    }


    public void onBackPressed() {
        if (App.getDataManager().getTripHistoryModel() != null) {
            App.getDataManager().getTripHistoryModel().selectMapForIndex(-1, null);
        }
    }

    @Bindable
    public PaymentHistory getHistory() {
        return history;
    }

    @Bindable
    public ObservableBoolean getShouldShowHelpSection() {
        return shouldShowHelpSection;
    }

    @BindingAdapter("selectedHistoryMapUrl")
    public static void loadImage(ImageView view, String imageUrl) {
        TripHistoryModel model = App.getDataManager().getTripHistoryModel();
        if (model != null) {
            if (model.getSelectedMapBitmap() != null && !model.getSelectedMapBitmap().isRecycled()) {
                view.setImageBitmap(model.getSelectedMapBitmap());
            } else {
                model.loadMapIntoImageView(view, imageUrl);
            }
        }
    }

    @Override
    protected long getRideId() {
        TripHistoryModel model = App.getDataManager().getTripHistoryModel();
        if (model != null && model.getSelectedHistory() != null) {
            return model.getSelectedHistory().getRideId();
        }
        return -1;
    }

    @Override
    protected void doOnTopicsLoaded(List<SupportTopic> topics) {
        super.doOnTopicsLoaded(topics);
        shouldShowHelpSection.set(topics != null && !topics.isEmpty());
    }

    private void showImagePopup(String url) {
        if (imagePopupHelper == null) {
            imagePopupHelper = new ImagePopupHelper(view.getActivity());
        }
        imagePopupHelper.show(url);
    }

    public interface View extends BaseSupportTopicsViewModel.View {
        Activity getActivity();

        void onUnexpectedState();
    }

}
