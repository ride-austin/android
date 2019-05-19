package com.rideaustin.ui.drawer.donate;

import android.databinding.ObservableArrayList;
import android.databinding.ObservableField;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.rideaustin.App;
import com.rideaustin.api.model.Charity;
import com.rideaustin.api.model.Rider;
import com.rideaustin.base.ApiSubscriber;
import com.rideaustin.base.BaseActivityCallback;
import com.rideaustin.schedulers.RxSchedulers;
import com.rideaustin.ui.common.BaseViewModel;

import java.util.List;

import timber.log.Timber;

/**
 * Created by ysych on 04.07.2016.
 */
public class DonateFragmentViewModel extends BaseViewModel<DonateView> {

    private ObservableArrayList<Charity> charities = new ObservableArrayList<>();
    private ObservableField<Rider> rider = new ObservableField<>();

    @Nullable
    private BaseActivityCallback callback;

    public DonateFragmentViewModel(@NonNull DonateView view) {
        super(view);
    }

    public void setCallback(@Nullable BaseActivityCallback callback) {
        this.callback = callback;
    }

    public ObservableArrayList<Charity> getCharities() {
        return charities;
    }

    public ObservableField<Rider> getRider() {
        return rider;
    }

    @Override
    public void onStart() {
        super.onStart();
        getRiderFromServer();
        getCharitiesFromServer();
    }

    private void getCharitiesFromServer() {
        addSubscription(
                App.getConfigurationManager()
                        .getConfigurationUpdates()
                        .first()
                        .flatMap(globalConfig -> {
                            final Integer cityId = globalConfig.getCurrentCity().getCityId();
                            return App.getDataManager().getCharitiesService().getCharities(cityId);
                        })
                        .subscribeOn(RxSchedulers.network())
                        .observeOn(RxSchedulers.main())
                        .subscribe(new ApiSubscriber<List<Charity>>(callback) {
                            @Override
                            public void onNext(List<Charity> charities) {
                                onCharitiesLoaded(charities);
                            }
                        })
        );
    }

    private void getRiderFromServer() {
        addSubscription(
                App.getDataManager().fetchCurrentRider()
                        .subscribeOn(RxSchedulers.network())
                        .observeOn(RxSchedulers.main())
                        .subscribe(new ApiSubscriber<Rider>(callback) {
                            @Override
                            public void onNext(Rider rider) {
                                onRiderLoaded(rider);
                            }
                        })
        );
    }

    public void updateRidersCharity(Charity charity) {
        Timber.d("::updateRidersCharity::" + "charity = [" + charity + "]");
        addSubscription(
                App.getDataManager().putCurrentRidersCharity(charity)
                        .subscribeOn(RxSchedulers.network())
                        .observeOn(RxSchedulers.main())
                        .subscribe(new ApiSubscriber<Rider>(callback) {
                            @Override
                            public void onNext(Rider rider) {
                                onRiderLoaded(rider);
                            }
                        })
        );
    }

    private void onRiderLoaded(Rider rider) {
        Timber.d("::Charity:: %s", rider.getCharity());
        this.rider.set(rider);
        App.getPrefs().setRoundUpEnabled(rider.charity != null);
        performOnView(DonateView::onRiderUpdated);
    }

    private void onCharitiesLoaded(List<Charity> charities) {
        this.charities.clear();
        this.charities.addAll(charities);
        performOnView(DonateView::onCharitiesUpdated);
    }

}
