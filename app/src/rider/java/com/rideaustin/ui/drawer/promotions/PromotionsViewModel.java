package com.rideaustin.ui.drawer.promotions;

import android.databinding.ObservableField;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.v4.content.ContextCompat;

import com.rideaustin.App;
import com.rideaustin.R;
import com.rideaustin.api.DataManager;
import com.rideaustin.api.config.GlobalConfig;
import com.rideaustin.api.model.promocode.PromoCode;
import com.rideaustin.api.model.promocode.PromoCodeBalance;
import com.rideaustin.api.model.promocode.PromoCodeResponse;
import com.rideaustin.base.ApiSubscriber;
import com.rideaustin.base.ApiSubscriber2;
import com.rideaustin.base.BaseActivityCallback;
import com.rideaustin.base.BaseApiException;
import com.rideaustin.manager.ConfigurationManager;
import com.rideaustin.schedulers.RxSchedulers;
import com.rideaustin.ui.common.BaseViewModel;
import com.rideaustin.ui.common.DefaultErrorAction;
import com.rideaustin.utils.MoneyUtils;
import com.rideaustin.utils.localization.Localizer;
import com.rideaustin.utils.toast.RAToast;

import java.net.HttpURLConnection;

import java8.util.Optional;
import timber.log.Timber;

import static com.rideaustin.utils.CommonConstants.UNEXPECTED_STATE_KEY;

/**
 * Created by Viktor Kifer
 * On 28-Dec-2016.
 */
public class PromotionsViewModel extends BaseViewModel<PromotionsView> {

    private static final int EMPTY_BALANCE_TEXT_COLOR = R.color.grey_400;
    private static final int EMPTY_BALANCE_ICON = R.drawable.icn_no_balance_icon;
    public static final String EMPTY_BALANCE_VALUE = "$0";

    private static final int AVAILABLE_BALANCE_TEXT_COLOR = R.color.teal_A400;
    private static final int AVAILABLE_BALANCE_ICON = R.drawable.icn_credits_available;

    private static final int EXPIRED_BALANCE_TEXT_COLOR = R.color.red_400;
    private static final int EXPIRED_BALANCE_ICON = R.drawable.icn_credit_expired;

    public final ObservableField<Drawable> balanceIcon = new ObservableField<>();
    public final ObservableField<Integer> balanceValueColor = new ObservableField<>();
    public final ObservableField<String> balanceValue = new ObservableField<>();
    public final ObservableField<String> promoCode = new ObservableField<>("");

    @NonNull
    private ConfigurationManager configurationManager;
    @NonNull
    private DataManager dataManager;
    @NonNull
    private Localizer localizer;

    private Optional<PromoCodeBalance> promoCodeBalance = Optional.empty();


    public PromotionsViewModel(@NonNull PromotionsView view, @NonNull Localizer localizer) {
        super(view);
        this.dataManager = App.getDataManager();
        this.configurationManager = App.getConfigurationManager();
        this.localizer = localizer;
        setupBalanceState(BalanceState.EMPTY, 0);
    }

    @VisibleForTesting
    PromotionsViewModel(@NonNull PromotionsView view,
                        @NonNull ConfigurationManager configurationManager,
                        @NonNull DataManager dataManager,
                        @NonNull Localizer localizer) {
        super(view);
        this.configurationManager = configurationManager;
        this.dataManager = dataManager;
        this.localizer = localizer;
        setupBalanceState(BalanceState.EMPTY, 0);
    }

    protected void setupBalanceState(final BalanceState balanceState, double balance) {
        switch (balanceState) {
            case EMPTY:
                balanceValueColor.set(ContextCompat.getColor(App.getInstance(), EMPTY_BALANCE_TEXT_COLOR));
                balanceIcon.set(ContextCompat.getDrawable(App.getInstance(), EMPTY_BALANCE_ICON));
                balanceValue.set(EMPTY_BALANCE_VALUE);
                break;
            case EXPIRED:
                balanceValueColor.set(ContextCompat.getColor(App.getInstance(), EXPIRED_BALANCE_TEXT_COLOR));
                balanceIcon.set(ContextCompat.getDrawable(App.getInstance(), EXPIRED_BALANCE_ICON));
                break;
            case AVAILABLE:
                balanceValueColor.set(ContextCompat.getColor(App.getInstance(), AVAILABLE_BALANCE_TEXT_COLOR));
                balanceIcon.set(ContextCompat.getDrawable(App.getInstance(), AVAILABLE_BALANCE_ICON));
                balanceValue.set(App.i().getString(R.string.money, MoneyUtils.format(balance)));
                break;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        dataManager.ifLoggedIn(user -> {
            subscribeToConfigChanges();
            loadPromoCodeBalance();
        });
    }

    private void subscribeToConfigChanges() {
        addSubscription(
                configurationManager.getLastAndRequestUpdates()
                        .subscribeOn(RxSchedulers.computation())
                        .observeOn(RxSchedulers.main())
                        .subscribe(this::onConfigUpdated, new DefaultErrorAction())
        );
    }

    private void loadPromoCodeBalance() {
        addSubscription(dataManager.getPromoCodeService()
                .getRiderPromoCodeBalance(dataManager.getCurrentUser().getRiderId())
                .subscribeOn(RxSchedulers.network())
                .observeOn(RxSchedulers.main())
                .subscribe(new ApiSubscriber2<PromoCodeBalance>(getCallback()) {
                    @Override
                    public void onNext(PromoCodeBalance promoCodeBalance) {
                        if (promoCodeBalance.getRemainder() > 0) {
                            PromotionsViewModel.this.promoCodeBalance = Optional.of(promoCodeBalance);
                            setupBalanceState(BalanceState.AVAILABLE, promoCodeBalance.getRemainder());
                        } else {
                            setupBalanceState(BalanceState.EMPTY, 0);
                        }
                    }
                }));
    }

    private void onConfigUpdated(GlobalConfig config) {
        performOnView(view -> view.onConfigurationUpdated(config));
    }

    public Optional<PromoCodeBalance> getPromoCodeBalance() {
        return promoCodeBalance;
    }

    @Nullable
    private BaseActivityCallback getCallback() {
        if (isViewAttached()) {
            return getView().getCallback();
        }
        return null;
    }

    public void applyPromoCode() {
        PromoCode code = new PromoCode();
        code.setCodeLiteral(promoCode.get());
        if (dataManager.getCurrentUser() == null) {
            return;
        }

        final long riderId = dataManager.getCurrentUser().getRiderId();

        if (riderId == -1) {
            Exception e = new Exception(UNEXPECTED_STATE_KEY);
            Timber.e(e, "RiderId = -1. Promo Code can not be applied");
            RAToast.showShort(R.string.error_unknown);
            return;
        }

        addSubscription(dataManager.getPromoCodeService()
                .applyPromoCode(riderId, code)
                .subscribeOn(RxSchedulers.network())
                .observeOn(RxSchedulers.main())
                .subscribe(new ApiSubscriber<PromoCodeResponse>(getCallback()) {
                    @Override
                    public void onNext(PromoCodeResponse promoCodeResponse) {
                        onPromoCodeApplied(promoCodeResponse);
                        loadPromoCodeBalance();
                        promoCode.set("");
                    }

                    @Override
                    public void onError(BaseApiException e) {
                        onPromoCodeFailed(code, e);
                    }

                    @Override
                    protected boolean showDefaultErrors() {
                        return false;
                    }
                }));
    }

    private void onPromoCodeApplied(PromoCodeResponse response) {
        if (response == null) {
            RAToast.showShort(R.string.error_promocode);
            return;
        }

        performOnView(view -> view.onPromoCodeApplied(response));
    }

    private void onPromoCodeFailed(PromoCode code, BaseApiException e) {
        String message = e.getCode() == HttpURLConnection.HTTP_BAD_REQUEST ?
                e.getMessage() : localizer.getString(R.string.error_generic);
        performOnView(view -> {
            view.showInvalidRiderStateDialog(code.getCodeLiteral(), message);
        });
    }

    protected enum BalanceState {
        EMPTY, AVAILABLE, EXPIRED
    }
}
