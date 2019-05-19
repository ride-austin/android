package com.rideaustin.ui.drawer.promotions.details;

import android.databinding.ObservableField;

import com.rideaustin.App;
import com.rideaustin.R;
import com.rideaustin.api.model.promocode.PromoCode;
import com.rideaustin.base.ApiSubscriber2;
import com.rideaustin.base.BaseActivityCallback;
import com.rideaustin.schedulers.RxSchedulers;
import com.rideaustin.utils.MoneyUtils;
import com.rideaustin.utils.SingleSubject;

import java.util.List;

import rx.Observable;

/**
 * Created by hatak on 01.09.2017.
 */

public class PromotionsDetailsViewModel {

    public ObservableField<String> totalBalance = new ObservableField<>();
    private SingleSubject<List<PromoCode>> promoCodesSubject = SingleSubject.create();

    public void setTotalBalance(final double balance) {
        totalBalance.set(App.i().getString(R.string.money, MoneyUtils.format(balance)));
    }

    public void loadPromoCodes(final BaseActivityCallback callback) {
        App.getDataManager().getPromoCodeService()
                .getRiderPromoCodes(App.getDataManager().getCurrentUser().getRiderId())
                .subscribeOn(RxSchedulers.network())
                .observeOn(RxSchedulers.main())
                .subscribe(new ApiSubscriber2<List<PromoCode>>(callback) {
                    @Override
                    public void onNext(List<PromoCode> promoCodes) {
                        promoCodesSubject.onNext(promoCodes);
                    }
                });
    }

    public Observable<List<PromoCode>> getPromoCodes() {
        return promoCodesSubject.asObservable();
    }

}
