package com.rideaustin.ui.drawer.queue;

import android.content.Context;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.text.TextUtils;

import com.rideaustin.App;
import com.rideaustin.R;
import com.rideaustin.api.model.QueueResponse;
import com.rideaustin.api.model.driver.RequestedCarType;
import com.rideaustin.base.ApiSubscriber2;
import com.rideaustin.base.BaseApiException;
import com.rideaustin.schedulers.RxSchedulers;
import com.rideaustin.ui.common.RxBaseViewModel;
import com.rideaustin.utils.Constants;
import com.rideaustin.utils.RetryWithDelay;
import com.rideaustin.utils.SingleSubject;
import com.rideaustin.utils.toast.RAToast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.subscriptions.Subscriptions;
import timber.log.Timber;

import static com.rideaustin.utils.CommonConstants.UNEXPECTED_STATE_KEY;

/**
 * Created by vokol on 26.08.2016.
 */
public class QueueViewModel extends RxBaseViewModel {

    public final ObservableField<String> header = new ObservableField<>();
    public final ObservableBoolean refreshing = new ObservableBoolean();

    private SingleSubject<List<QueueEntry>> queueEntriesSubject = SingleSubject.create();
    private Subscription updateQueueSubscription = Subscriptions.empty();

    private String queueName;
    private volatile boolean inQueue = false;
    private Map<String, RequestedCarType> category2CarTypeMap = new LinkedHashMap<>();

    public QueueViewModel(String queueName) {
        this.queueName = queueName;
        if (TextUtils.isEmpty(queueName)) {
            Exception e = new Exception(UNEXPECTED_STATE_KEY);
            Timber.e(e, "Queue name is empty, this should not happen");
        }
        initialize();
    }


    public Observable<List<QueueEntry>> getQueueEntriesEvents() {
        return queueEntriesSubject.asObservable().onBackpressureLatest();
    }

    public void initialize() {
        showProgress();
        untilDestroy(App.getInstance().getAirportQueueManager()
                .getCurrentQueue()
                .doOnSubscribe(this::showProgress)
                .observeOn(RxSchedulers.main())
                .subscribe(response -> {
                    updateInQueue(response != null
                            && response.getAreaQueueName() != null
                            && response.getAreaQueueName().equalsIgnoreCase(queueName));
                    updateHeader();
                    fetchQueueChanges();
                }, throwable -> {
                    updateInQueue(false);
                    updateHeader();
                    Timber.e(throwable);
                    fetchQueueChanges();
                }));

        untilDestroy(App.getInstance().getRideRequestManager()
                .getCarTypes()
                .subscribeOn(RxSchedulers.network())
                .observeOn(RxSchedulers.main())
                .retryWhen(new RetryWithDelay(1000))
                .subscribe(carTypes -> {
                    category2CarTypeMap.clear();
                    for (RequestedCarType carType : carTypes) {
                        category2CarTypeMap.put(carType.getCarCategory(), carType);
                    }
                    fetchQueueChanges();
                }, throwable -> Timber.e(throwable, throwable.getMessage())));
    }

    public void refresh() {
        updateQueueSubscription.unsubscribe();
        updateQueueSubscription = getUpdateObservable()
                .doOnSubscribe(() -> refreshing.set(true))
                .doOnUnsubscribe(() -> refreshing.set(false))
                .subscribeOn(RxSchedulers.network())
                .observeOn(RxSchedulers.main())
                .doOnCompleted(() -> fetchQueueChanges(Constants.LOAD_QUEUED_TIMEOUT_S))
                .subscribe(getUpdateSubscriber());
    }

    private void fetchQueueChanges() {
        fetchQueueChanges(0L);
    }

    private void fetchQueueChanges(long initialDelay) {
        updateQueueSubscription.unsubscribe();
        updateQueueSubscription = Observable
                .interval(initialDelay, Constants.LOAD_QUEUED_TIMEOUT_S, TimeUnit.SECONDS, RxSchedulers.computation())
                .flatMap(aLong -> getUpdateObservable())
                .observeOn(RxSchedulers.main())
                .retryWhen(new RetryWithDelay(Constants.BACKGROUND_ERROR_RETRY_DELAY_S * 1000L))
                .subscribe(getUpdateSubscriber());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        updateQueueSubscription.unsubscribe();
    }

    private Observable<QueueResponse> getUpdateObservable() {
        if (inQueue) {
            return App.getDataManager().getDriverQueue();
        } else {
            int cityId = App.getConfigurationManager().getLastConfiguration().getCurrentCity().getCityId();
            return App.getDataManager().getDriverService().getActiveAreaDetails(cityId)
                    .flatMap(queueResponses -> {
                        for (QueueResponse response : queueResponses) {
                            if (queueName.equals(response.getAreaQueueName())) {
                                return Observable.just(response);
                            }
                        }
                        return Observable.empty();
                    });
        }
    }

    private ApiSubscriber2<QueueResponse> getUpdateSubscriber() {
        return new ApiSubscriber2<QueueResponse>(false) {
            @Override
            public void onNext(QueueResponse queueResponse) {
                if (category2CarTypeMap.isEmpty()) {
                    return;
                }
                hideProgress();
                Set<String> preferences = App.getInstance().getRideRequestManager().getSelectedCategories();
                HashMap<String, Integer> requestMap;
                if (inQueue) {
                    requestMap = queueResponse.getPositions();
                } else {
                    requestMap = queueResponse.getLengths();
                }
                List<QueueEntry> queueEntries = new ArrayList<>();
                // first iterate categories from response, which can have interest for driver
                for (String carCategory : requestMap.keySet()) {
                    if (category2CarTypeMap.containsKey(carCategory)) {
                        boolean myCategory = preferences.contains(carCategory);
                        Integer queueValue = requestMap.get(carCategory);
                        RequestedCarType carType = category2CarTypeMap.get(carCategory);
                        queueEntries.add(new QueueEntry(carType.getTitle(), getQueueValueText(queueValue, myCategory), carType.getPlainIconUrl()));
                    }
                }
                // then include the rest
                for (String carCategory : category2CarTypeMap.keySet()) {
                    if (!requestMap.containsKey(carCategory)) {
                        boolean myCategory = preferences.contains(carCategory);
                        Integer queueValue = requestMap.containsKey(carCategory) ? requestMap.get(carCategory) : 0;
                        RequestedCarType carType = category2CarTypeMap.get(carCategory);
                        queueEntries.add(new QueueEntry(carType.getTitle(), getQueueValueText(queueValue, myCategory), carType.getPlainIconUrl()));
                    }
                }
                queueEntriesSubject.onNext(queueEntries);
            }

            @Override
            public void onAnyError(BaseApiException e) {
                RAToast.showShort(R.string.unable_to_fetch_queue_positions);
                Timber.e(e, e.getMessage());
            }
        };
    }

    private void updateInQueue(boolean inQueue) {
        if (this.inQueue != inQueue) {
            this.inQueue = inQueue;
            RAToast.showShort(inQueue ? R.string.you_are_in_this_queue : R.string.you_are_not_in_this_queue);
        }
    }

    private void updateHeader() {
        if (inQueue) {
            header.set(App.getInstance().getString(R.string.queue_in_number_info));
        } else {
            header.set(App.getInstance().getString(R.string.queue_out_number_info, queueName));
        }
    }

    private String getQueueValueText(final Integer queueValue, final boolean myCategory) {
        if (queueValue == null) {
            return "-";
        }
        if (inQueue) {
            final Context context = App.getInstance();
            if (myCategory) {
                return queueValue == 0 ? context.getString(R.string.first_in_queue_text) : String.valueOf(queueValue + 1);
            } else {
                return context.getString(R.string.not_available);
            }
        } else {
            return String.valueOf(queueValue);
        }
    }
}
