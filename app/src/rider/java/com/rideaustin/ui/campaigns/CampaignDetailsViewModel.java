package com.rideaustin.ui.campaigns;

import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;

import com.rideaustin.App;
import com.rideaustin.R;
import com.rideaustin.api.config.CampaignProvider;
import com.rideaustin.api.model.campaigns.CampaignDetails;
import com.rideaustin.base.ApiSubscriber2;
import com.rideaustin.base.BaseApiException;
import com.rideaustin.ui.common.RxBaseViewModel;
import com.rideaustin.utils.RetryWhenNoNetwork;
import com.rideaustin.utils.toast.RAToast;

import java.util.List;

import rx.Observable;
import rx.Subscription;
import rx.subjects.BehaviorSubject;
import rx.subscriptions.Subscriptions;

/**
 * Created on 5/20/18.
 *
 * @author sdelaysam
 */
public class CampaignDetailsViewModel extends RxBaseViewModel {

    private BehaviorSubject<CampaignDetails> details = BehaviorSubject.create();

    private Subscription detailsSubscription = Subscriptions.unsubscribed();

    public final ObservableBoolean loading = new ObservableBoolean(false);

    public final ObservableField<String> title = new ObservableField<>("");

    Observable<CampaignDetails> getDetails() {
        return details.asObservable();
    }

    void setProvider(CampaignProvider provider) {
        if (detailsSubscription.isUnsubscribed()) {
            detailsSubscription = App.getDataManager()
                    .getCampaignDetails(provider)
                    .doOnSubscribe(() -> loading.set(true))
                    .doOnUnsubscribe(() -> loading.set(false))
                    .retryWhen(new RetryWhenNoNetwork(1000))
                    .subscribe(new ApiSubscriber2<List<CampaignDetails>>(true) {
                        @Override
                        public void onNext(List<CampaignDetails> list) {
                            super.onNext(list);
                            if (list.isEmpty()) {
                                RAToast.showLong(R.string.campaigns_empty_list);
                                details.onError(new RuntimeException("Empty campaigns list"));
                            } else {
                                CampaignDetails first = list.get(0);
                                title.set(first.getHeaderTitle());
                                details.onNext(first);
                            }
                        }

                        @Override
                        public void onAnyError(BaseApiException e) {
                            super.onAnyError(e);
                            details.onError(e);
                        }
                    });
            untilDestroy(detailsSubscription);
        }
    }

}
