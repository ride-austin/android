package com.rideaustin.ui.rate;

import android.app.Activity;
import android.databinding.Bindable;
import android.databinding.BindingAdapter;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.graphics.Bitmap;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.rideaustin.App;
import com.rideaustin.BR;
import com.rideaustin.R;
import com.rideaustin.api.config.GlobalConfig;
import com.rideaustin.api.config.Rides;
import com.rideaustin.api.config.Tipping;
import com.rideaustin.api.config.ut.UT;
import com.rideaustin.api.model.AvatarType;
import com.rideaustin.api.model.CarTypeConfiguration;
import com.rideaustin.api.model.Map;
import com.rideaustin.api.model.PaymentProvider;
import com.rideaustin.api.model.Ride;
import com.rideaustin.api.model.driver.Driver;
import com.rideaustin.base.ApiSubscriber2;
import com.rideaustin.base.BaseActivityCallback;
import com.rideaustin.base.BaseApiException;
import com.rideaustin.databinding.FragmentRateDriverBinding;
import com.rideaustin.schedulers.RxSchedulers;
import com.rideaustin.ui.common.BaseViewModel;
import com.rideaustin.ui.payment.PaymentType;
import com.rideaustin.ui.utils.MaterialDialogCreator;
import com.rideaustin.utils.Constants;
import com.rideaustin.utils.ImageHelper;
import com.rideaustin.utils.KeyboardUtil;
import com.rideaustin.utils.RxponentialBackoffRetry;
import com.rideaustin.utils.TimeUtils;

import java.util.concurrent.TimeUnit;

import java8.util.Optional;
import rx.Observable;
import timber.log.Timber;

/**
 * Created by ysych on 7/27/16.
 */
public class RateDriverDialogViewModel extends BaseViewModel<RateDriverView> {

    private static final int MAX_ALLOWED_TIP_AMOUNT = 300;
    public static final int CONFIRMATION_THRESHOLD = 50;
    public static final int SKIP_RATING_ERROR_CODE = 400;

    private final BaseActivityCallback baseActivityCallback;
    private ObservableField<Ride> ride = new ObservableField<>();
    private ObservableField<String> fareString = new ObservableField<>();
    private ObservableField<Map> map = new ObservableField<>();
    private ObservableBoolean shouldShowTipOption = new ObservableBoolean(false);
    private ObservableBoolean shouldDisplayMapPreloading = new ObservableBoolean(false);
    private ObservableBoolean shouldDisplayMapError = new ObservableBoolean(false);
    private ObservableBoolean shouldDisplayBevoBucks = new ObservableBoolean(false);
    private ObservableBoolean bevoBucksChecked = new ObservableBoolean(false);
    private ObservableField<String> rideSummary = new ObservableField<>("");
    private FragmentRateDriverBinding binding;

    private int tip = 0;
    private int maxTip = MAX_ALLOWED_TIP_AMOUNT;
    private final long rideId;

    private String comment;
    private String riderFeedbackText;
    private Target<Bitmap> mapTarget;

    public RateDriverDialogViewModel(final FragmentRateDriverBinding binding, final BaseActivityCallback baseActivityCallback, final RateDriverView view, final long rideId) {
        super(view);
        this.binding = binding;
        this.baseActivityCallback = baseActivityCallback;
        this.rideId = rideId;
        binding.customTip.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus && tip == 0) {
                hideCustomTip();
                binding.tipsNo.setChecked(true);
            }
        });
    }

    @BindingAdapter({"viewModel", "map"})
    public static void loadRideMap(ImageView view, RateDriverDialogViewModel viewModel, Map map) {
        viewModel.loadMap(view, map != null ? map.getUrl() : null);
    }

    @BindingAdapter("usersAvatar")
    public static void loadUsersImage(ImageView view, String imageUrl) {
        if (!TextUtils.isEmpty(imageUrl)) { // as no placeholder provided, just ignore empty urls
            ImageHelper.loadRoundImageIntoView(view, imageUrl);
        }
    }

    @Bindable
    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Bindable
    public String getRiderFeedbackText() {
        return riderFeedbackText;
    }

    public void setRiderFeedbackText(String driverName) {
        String text = App.getInstance().getString(R.string.rating_feedback_message_text, driverName);
        this.riderFeedbackText = text;
        notifyPropertyChanged(BR.riderFeedbackText);
    }

    public ObservableField<Ride> getRide() {
        return ride;
    }

    public void setRide(Ride ride) {
        this.ride.set(ride);
        Driver driver = ride.getActiveDriver().getDriver();
        String driverName = driver.getUser().getNickName();
        if (TextUtils.isEmpty(driverName)) {
            driverName = driver.getFirstname();
        }
        setRiderFeedbackText(driverName);
        updateFareString(ride);
        checkTipOptions();
        checkRideSummary();

        Optional.ofNullable(App.getConfigurationManager().getLastConfiguration().getUt())
                .map(UT::getPayWithBevoBucks)
                .ifPresentOrElse(payWithBevoBucks -> {
                    Integer ridePaymentDelay = payWithBevoBucks.getRidePaymentDelay() * 1000;
                    boolean isInTimeRange = TimeUtils.currentTimeMillis() < (ride.completedOn + ridePaymentDelay);
                    boolean isPrimary = App.getDataManager().isPaymentSelected(PaymentType.BEVO_BUCKS);
                    shouldDisplayBevoBucks.set(payWithBevoBucks.getEnabled() && isInTimeRange && isPrimary);
                    bevoBucksChecked.set(isPrimary);
                }, () -> shouldDisplayBevoBucks.set(false));
    }

    public ObservableField<Map> getMap() {
        return map;
    }

    public void setMap(Map map) {
        this.map.set(map);
    }

    public void onSubmitClicked(Activity activity, float score) {
        if (binding.tipsOther.isChecked() && tip == 0) {
            performOnView(RateDriverView::onCustomFieldRequired);
        } else if (tip > CONFIRMATION_THRESHOLD) {
            MaterialDialogCreator.createTippingConfirmDialog(tip, activity)
                    .onPositive((dialog, which) -> sendRating(score))
                    .onNegative((dialog, which) -> {
                        tip = 0;
                        notifyPropertyChanged(BR.customTip);
                        binding.customTip.requestFocus();
                        KeyboardUtil.showKeyBoard(binding.customTip.getContext(), binding.customTip);
                    })
                    .show();
        } else {
            sendRating(score);
        }
    }

    void sendRating(float rating) {
        if (!TextUtils.isEmpty(comment) && comment.length() > Constants.MAX_COMMENT_LENGTH) {
            performOnView(RateDriverView::onLongComment);
            return;
        }
        if (ride.get() != null) {
            addSubscription(App.getDataManager().getRidesService()
                    .rateRide(ride.get().getId(), rating, tip, AvatarType.RIDER.name(), comment, getSelectedPaymentProvider().name())
                    .subscribeOn(RxSchedulers.network())
                    .observeOn(RxSchedulers.main())
                    .subscribe(new ApiSubscriber2<Void>(baseActivityCallback) {
                        @Override
                        public void onNext(Void aVoid) {
                            rideRated();
                        }

                        @Override
                        public void onHttpError(BaseApiException e) {
                            super.onHttpError(e);
                            if (SKIP_RATING_ERROR_CODE == e.getCode()) {
                                rideRated();
                            }
                        }
                    }));
        } else {
            rideRated();
        }
    }

    private PaymentProvider getSelectedPaymentProvider() {
        return shouldDisplayBevoBucks.get() && bevoBucksChecked.get() ? PaymentProvider.BEVO_BUCKS : PaymentProvider.CREDIT_CARD;
    }

    private void rideRated() {
        App.getPrefs().removeRideToRate();
        performOnView(RateDriverView::onDriverRatingSent);
        if (getSelectedPaymentProvider() == PaymentProvider.BEVO_BUCKS) {
            performOnView(rateDriverView -> rateDriverView.onBevoPayment(ride.get()));
        }
    }

    void getRideWithMapByRideId(long rideId) {
        addSubscription(App.getDataManager().checkRideAcceptanceStatus(rideId, AvatarType.RIDER.name())
                .subscribeOn(RxSchedulers.network())
                .observeOn(RxSchedulers.main())
                .subscribe(new ApiSubscriber2<Ride>(baseActivityCallback) {
                    @Override
                    public void onNext(Ride ride) {
                        setRide(ride);
                    }
                }));

        addSubscription(requestMap(rideId)
                .subscribeOn(RxSchedulers.network())
                .observeOn(RxSchedulers.main())
                .subscribe(new ApiSubscriber2<Map>(null) {
                    @Override
                    public void onNext(Map map) {
                        setMap(map);
                    }
                }));
    }

    private Observable<Map> requestMap(long rideId) {
        return App.getDataManager().getRideMap(rideId)
                .flatMap(this::checkMap)
                .retryWhen(new RxponentialBackoffRetry().getNotificationHandler());
    }

    private Observable<Map> checkMap(Map map) {
        if (map == null || TextUtils.isEmpty(map.getUrl())) {
            return Observable.error(new Throwable("Bad ride map response, should retry"));
        }
        return Observable.just(map);
    }

    public void onTipChecked(View v) {
        RadioButton radioGroup = (RadioButton) v;
        switch (radioGroup.getId()) {
            case R.id.tips_no:
                tip = 0;
                break;
            case R.id.tips_one:
                tip = 1;
                break;
            case R.id.tips_two:
                tip = 2;
                break;
            case R.id.tips_five:
                tip = 5;
                break;
            case R.id.tips_other:
                showCustomTip();
                break;
        }
        if (radioGroup.getId() != R.id.tips_other) {
            hideCustomTip();
            KeyboardUtil.hideKeyBoard(v.getContext(), v);
        }
        notifyPropertyChanged(BR.customTip);
    }

    private void hideCustomTip() {
        binding.customTip.setVisibility(View.GONE);
    }

    private void showCustomTip() {
        tip = 0;
        binding.customTip.setVisibility(View.VISIBLE);
        binding.customTip.requestFocus();
        KeyboardUtil.showKeyboardImplicit(binding.customTip.getContext(), binding.customTip);
    }

    @Bindable
    public String getCustomTip() {
        if (tip == 0) {
            return "";
        } else {
            return Integer.toString(tip);
        }
    }

    public void setCustomTip(String input) {
        if (input.isEmpty()) {
            tip = 0;
            return;
        }
        try {
            tip = Integer.valueOf(input);
        } catch (NumberFormatException e) {
            Timber.e(e, "Exception while parsing tip value: %s", input);
            tip = 0;
            notifyPropertyChanged(BR.customTip);
            return;
        }
        if (tip > maxTip) {
            tip = maxTip;
            notifyPropertyChanged(BR.customTip);
        } else if (tip < 0) {
            tip = 0;
            notifyPropertyChanged(BR.customTip);
        }
    }

    public ObservableField<String> getFareString() {
        return fareString;
    }

    public ObservableBoolean getShouldShowTipOption() {
        return shouldShowTipOption;
    }

    public ObservableBoolean getShouldDisplayMapPreloading() {
        return shouldDisplayMapPreloading;
    }

    public ObservableBoolean getShouldDisplayMapError() {
        return shouldDisplayMapError;
    }

    public ObservableBoolean getBevoBucksChecked() {
        return bevoBucksChecked;
    }

    public ObservableField<String> getSummaryDescription() {
        return rideSummary;
    }

    private void checkRideSummary() {
        addSubscription(App.getConfigurationManager().getLiveConfig()
                .timeout(10, TimeUnit.SECONDS)
                .map(this::getRidesSummary)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .subscribe(rideSummary::set, this::doOnSummaryError));
    }

    private Optional<String> getRidesSummary(GlobalConfig config) {
        return Optional.ofNullable(config)
                .filter(c -> !c.isEmbedded())
                .map(GlobalConfig::getRides)
                .flatMap(this::pickSummary);
    }

    private Optional<String> pickSummary(Rides rides) {
        if (ride.get() != null) {
            return Optional.ofNullable(ride.get().isFreeCreditsCharged()
                    ? rides.getRideSummaryDescriptionFreeCreditCharged()
                    : rides.getRideSummaryDescription());
        }
        return Optional.empty();
    }

    private void doOnSummaryError(Throwable throwable) {
        // fallback to embedded strings
        rideSummary.set(App.getInstance().getString(R.string.ride_summary_description));
        Timber.e(throwable);
    }

    private void checkTipOptions() {
        addSubscription(App.getConfigurationManager().getLiveConfig()
                .timeout(10, TimeUnit.SECONDS)
                .subscribe(this::calculateShowTipOption, throwable -> {
                    Timber.e(throwable);
                    calculateShowTipOption(App.getConfigurationManager().getLastConfiguration());
                }));
    }

    private void calculateShowTipOption(GlobalConfig config) {
        Tipping tipping = config.getTipping();
        if (!tipping.getEnabled() || !Optional.ofNullable(ride.get()).map(Ride::getTippingAllowed).filter(b -> b != null).orElse(true)) {
            resetTip();
            return;
        }


        long completedOnMillis = ride.get().getCompletedOn();
        long ridePaymentDelayMillis = tipping.getRidePaymentDelay() * 1000;
        long tipDeadline = completedOnMillis + ridePaymentDelayMillis;
        long tipUntil = Optional.ofNullable(ride.get()).map(Ride::getTippingUntil).filter(b -> b != null).orElse(tipDeadline);
        tipDeadline = Math.min(tipDeadline, tipUntil);
        long currentTimeMillis = TimeUtils.currentTimeMillis();
        long timeRemaining = Math.max(0, tipDeadline - currentTimeMillis);

        CarTypeConfiguration configuration = ride.get().getRequestedCarType().getConfiguration();
        boolean isEnabled = (configuration == null || !configuration.isDisableTipping())
                && timeRemaining > 0;
        if (!isEnabled) {
            resetTip();
            return;
        }

        shouldShowTipOption.set(true);
        maxTip = tipping.getRideTipLimit() > 0 ? tipping.getRideTipLimit() : MAX_ALLOWED_TIP_AMOUNT;

        addSubscription(RxSchedulers.schedule(() -> {
            resetTip();
            binding.tipsNo.setChecked(true);
            bevoBucksChecked.set(false);
            shouldDisplayBevoBucks.set(false);
        }, timeRemaining, TimeUnit.MILLISECONDS));
    }

    private void resetTip() {
        tip = 0;
        hideCustomTip();
        shouldShowTipOption.set(false);
    }

    private void updateFareString(Ride ride) {
        CarTypeConfiguration configuration = App.getDataManager().getRequestedCarType().getConfiguration();
        if (configuration != null && !TextUtils.isEmpty(configuration.getZeroFareLabel())) {
            fareString.set(configuration.getZeroFareLabel());
        } else {
            Double totalFare = ride.getTotalFare() != null ? ride.getTotalFare() : 0.0;
            fareString.set(App.getInstance().getString(R.string.money, totalFare.toString()));
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Ride currentRide = ride.get();
        if (currentRide == null) {
            getRideWithMapByRideId(rideId);
        } else {
            setRide(currentRide);
        }
    }

    public void onDestroy() {
        cancelMapLoading();
    }

    public void loadMap(ImageView imageView, @Nullable String imageUrl) {
        cancelMapLoading();
        if (!TextUtils.isEmpty(imageUrl)) { // as no placeholder provided, just ignore empty urls
            shouldDisplayMapPreloading.set(true);
            shouldDisplayMapError.set(false);
            mapTarget = ImageHelper.loadImageIntoView(imageView, imageUrl, 0, 0, new MapRequestListener());
        }
    }

    public void cancelMapLoading() {
        if (mapTarget != null) {
            Glide.with(App.getInstance()).clear(mapTarget);
            mapTarget = null;
            shouldDisplayMapPreloading.set(false);
            shouldDisplayMapError.set(false);
        }
    }

    public void selectBevo(boolean isSelected) {
        App.getDataManager().selectBevoBucksPayment(isSelected);
    }

    private class MapRequestListener implements RequestListener<Bitmap> {

        @Override
        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
            shouldDisplayMapPreloading.set(false);
            shouldDisplayMapError.set(true);
            Timber.e(e, "Unable to load map: " + model);
            return false;
        }

        @Override
        public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
            shouldDisplayMapPreloading.set(false);
            shouldDisplayMapError.set(false);
            getView().scrollDownToSubmit();
            return false;
        }
    }

    public ObservableBoolean getShouldDisplayBevoBucks() {
        return shouldDisplayBevoBucks;
    }
}
