package com.rideaustin.ui.drawer.triphistory;

import android.graphics.Bitmap;
import android.support.annotation.Nullable;
import android.util.LongSparseArray;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.rideaustin.App;
import com.rideaustin.R;
import com.rideaustin.api.model.Map;
import com.rideaustin.api.model.paymenthistory.PaymentHistory;
import com.rideaustin.api.model.paymenthistory.PaymentHistoryResponse;
import com.rideaustin.base.ApiSubscriber2;
import com.rideaustin.base.BaseActivityCallback;
import com.rideaustin.base.BaseApiException;
import com.rideaustin.schedulers.RxSchedulers;
import com.rideaustin.ui.common.AdapterItemChange;
import com.rideaustin.utils.ImageHelper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by Sergey Petrov on 15/03/2017.
 */

public class TripHistoryModel {

    private static final int LIST_PAGE_SIZE = 25;

    private final BaseActivityCallback callback;

    private BehaviorSubject<List<PaymentHistory>> listSubject = BehaviorSubject.create();

    private PublishSubject<AdapterItemChange> rangeSubject = PublishSubject.create();

    private PublishSubject<AdapterItemChange> loadingMoreSubject = PublishSubject.create();

    private int currentPage = 0;

    private boolean canLoadMore = true;

    private boolean isLoadingMore = false;

    private PublishSubject<Boolean> historySelectedSubject = PublishSubject.create();

    private PublishSubject<Integer> mapSubject = PublishSubject.create();

    private int selectedIndex = -1; // could be saved into Bundle

    private Bitmap selectedMapBitmap;

    private LongSparseArray<Map> maps = new LongSparseArray<>();

    private Map loadingMap = new Map();

    private HashSet<String> loadedMaps = new HashSet<>();

    private List<Target<Bitmap>> glideTargets = new ArrayList<>();

    private CompositeSubscription listSubscriptions = new CompositeSubscription();

    private CompositeSubscription mapSubscriptions = new CompositeSubscription();

    public TripHistoryModel(BaseActivityCallback callback) {
        this.callback = callback;
        loadFirstPage();
    }

    public void loadFirstPage() {
        currentPage = 0;
        canLoadMore = false;
        setLoadingMore(false);
        listSubject.onNext(new ArrayList<>(0));
        listSubscriptions.clear(); // reset possible page loading
        listSubscriptions.add(App.getDataManager().getPaymentHistory(currentPage, LIST_PAGE_SIZE)
                .subscribeOn(RxSchedulers.network())
                .observeOn(RxSchedulers.main())
                .subscribe(new ApiSubscriber2<PaymentHistoryResponse>(callback) {
                    @Override
                    public void onNext(PaymentHistoryResponse response) {
                        super.onNext(response);
                        mapSubscriptions.clear();
                        canLoadMore = !response.isLastPage();
                        listSubject.onNext(response.getPaymentHistoryList());
                    }
                }));

    }

    public void loadNextPage() {
        if (!canLoadMore) {
            // Should never happen - just to be sure
            return;
        }
        canLoadMore = false;
        setLoadingMore(true);
        listSubscriptions.add(App.getDataManager().getPaymentHistory(currentPage + 1, LIST_PAGE_SIZE)
                .subscribeOn(RxSchedulers.network())
                .observeOn(RxSchedulers.main())
                .subscribe(new ApiSubscriber2<PaymentHistoryResponse>(true) {
                    @Override
                    public void onNext(PaymentHistoryResponse response) {
                        super.onNext(response);
                        canLoadMore = !response.isLastPage();
                        currentPage++; // increment current page only on success response
                        setLoadingMore(false);
                        if (!listSubject.hasValue()) {
                            // Should never happen - just to be sure
                            return;
                        }
                        int oldSize = getList().size();
                        int addAmount = response.getPaymentHistoryList().size();
                        // update list in place, no need to publish all the list
                        // but its super important to notify the adapter about the change
                        getList().addAll(response.getPaymentHistoryList());
                        rangeSubject.onNext(AdapterItemChange.addRange(oldSize, addAmount));
                    }

                    @Override
                    public void onAnyError(BaseApiException e) {
                        super.onAnyError(e);
                        canLoadMore = false;
                        setLoadingMore(false);
                        // enable it after 500 ms, to prevent multiple requests when list is on bottom
                        RxSchedulers.schedule(() -> canLoadMore = true, 500, TimeUnit.MILLISECONDS);
                    }
                }));
    }

    public void loadMapForIndex(int index) {
        PaymentHistory history = getHistoryByIndex(index);
        if (history != null) {
            Map map = maps.get(history.getRideId());
            if (map == loadingMap) {
                // request is not completed yet
                return;
            } else if (map != null) {
                // map was already loaded, just show it
                history.setMapUrl(map.getUrl());
                mapSubject.onNext(index);
                return;
            }
            // request the map
            mapSubscriptions.add(App.getDataManager().getRidesService()
                    .getRideMap(history.getRideId())
                    .subscribeOn(RxSchedulers.network())
                    .observeOn(RxSchedulers.main())
                    .subscribe(new ApiSubscriber2<Map>(false) {
                        @Override
                        public void onNext(Map map) {
                            super.onNext(map);
                            maps.put(history.getRideId(), map);
                            if(map.getUrl() != null) {
                                history.setMapUrl(map.getUrl());
                                mapSubject.onNext(index);
                            } else {
                                // API didn't give us an URL
                                // maybe retry?
                                history.setMapUrl("");
                            }
                        }
                    }));
        }
    }

    public void loadMapIntoImageView(ImageView imageView, String mapUrl) {
        loadedMaps.remove(mapUrl);
        glideTargets.add(ImageHelper.loadImageIntoView(imageView, mapUrl, R.drawable.map_placeholder, 0, new LoadMapListener()));
    }

    public boolean isMapLoadedForIndex(int index) {
        PaymentHistory history = getHistoryByIndex(index);
        return history != null && history.getMapUrl() != null && loadedMaps.contains(history.getMapUrl());
    }

    public void selectMapForIndex(int index, @Nullable Bitmap bitmap) {
        selectedIndex = index;
        selectedMapBitmap = bitmap;
        boolean isValid = isHistoryIndexValid(index);
        historySelectedSubject.onNext(isValid);
    }

    public List<PaymentHistory> getList() {
        return listSubject.getValue();
    }

    public Observable<List<PaymentHistory>> getListObservable() {
        return listSubject.asObservable();
    }

    public Observable<AdapterItemChange> getRangeObservable() {
        return rangeSubject.asObservable();
    }

    public Observable<AdapterItemChange> getLoadingMoreObservable() {
        // RA-14943: prevent back-pressure
        // no need to serialize as subject is changed from main thread only
        return loadingMoreSubject.asObservable().onBackpressureBuffer();
    }

    public boolean isLoadingMore() {
        return isLoadingMore;
    }

    public void setLoadingMore(boolean loading) {
        if (isLoadingMore != loading) {
            isLoadingMore = loading;
            if (loading) {
                int position = getList().size();
                // each change in list should be followed by adapter notification
                getList().add(PaymentHistory.empty());
                loadingMoreSubject.onNext(AdapterItemChange.add(position));
            } else {
                int position = findEmptyPaymentHistoryIndex();
                if (position > -1) {
                    // each change in list should be followed by adapter notification
                    getList().remove(position);
                    loadingMoreSubject.onNext(AdapterItemChange.remove(position));
                }
            }
        }
    }

    public boolean canLoadMore() {
        return canLoadMore;
    }

    public Observable<Boolean> getHistorySelectedObservable() {
        return historySelectedSubject.asObservable();
    }

    @Nullable
    public PaymentHistory getSelectedHistory() {
        return getHistoryByIndex(selectedIndex);
    }

    @Nullable
    public Bitmap getSelectedMapBitmap() {
        return selectedMapBitmap;
    }

    public Observable<Integer> getMapReadyObservable() {
        return mapSubject.asObservable();
    }

    public void destroy() {
        // clear requests
        listSubscriptions.clear();
        mapSubscriptions.clear();
        for (Target<Bitmap> target : glideTargets) {
            Glide.with(App.getInstance()).clear(target);
        }
        glideTargets.clear();

        // release bitmap
        if (selectedMapBitmap != null) {
            selectedMapBitmap.recycle();
            selectedMapBitmap = null;
        }

        // clear storage
        maps.clear();
        loadedMaps.clear();

        // complete subjects
        listSubject.onCompleted();
        historySelectedSubject.onCompleted();
        mapSubject.onCompleted();
    }

    @Nullable
    private PaymentHistory getHistoryByIndex(int index) {
        if (isHistoryIndexValid(index)) {
            return getList().get(index);
        }
        return null;
    }

    private boolean isHistoryIndexValid(int index) {
        List<PaymentHistory> list = getList();
        return list != null && !list.isEmpty() && index >= 0 && index < list.size();
    }

    private synchronized int findEmptyPaymentHistoryIndex() {
        List<PaymentHistory> list = getList();
        for (int i = list.size() - 1; i > 0; i--) {
            if (list.get(i) == PaymentHistory.empty()) {
                return i;
            }
        }
        return -1;
    }

    private class LoadMapListener implements RequestListener<Bitmap> {

        @Override
        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
            loadedMaps.remove((String)model);
            return false;
        }

        @Override
        public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
            loadedMaps.add((String)model);
            return false;
        }
    }

}
