package com.rideaustin.ui.drawer.triphistory;

import android.support.annotation.Nullable;
import android.util.SparseArray;

import com.rideaustin.App;
import com.rideaustin.CurrentAvatarType;
import com.rideaustin.api.model.SupportTopic;
import com.rideaustin.base.ApiSubscriber2;
import com.rideaustin.base.BaseActivityCallback;
import com.rideaustin.schedulers.RxSchedulers;

import java.util.List;

import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by Sergey Petrov on 17/03/2017.
 */

public class SupportTopicsModel {

    private final BaseActivityCallback callback;

    private BehaviorSubject<List<SupportTopic>> parentTopicsSubject;

    private SparseArray<BehaviorSubject<List<SupportTopic>>> childTopicsMap = new SparseArray<>();

    private CompositeSubscription listSubscriptions = new CompositeSubscription();

    public SupportTopicsModel(BaseActivityCallback callback) {
        this.callback = callback;
    }

    public Observable<List<SupportTopic>> getParentTopicsObservable() {
        if (parentTopicsSubject == null) {
            parentTopicsSubject = BehaviorSubject.create();
        }
        if (!parentTopicsSubject.hasValue()) {
            requestParentTopics();
        }
        return parentTopicsSubject.asObservable();
    }

    public Observable<List<SupportTopic>> getChildTopicsObservable(int parentTopicId) {
        BehaviorSubject<List<SupportTopic>> subject = childTopicsMap.get(parentTopicId);
        if (subject == null) {
            subject = BehaviorSubject.create();
            childTopicsMap.put(parentTopicId, subject);
        }
        if (!subject.hasValue()) {
            requestChildTopics(parentTopicId);
        }
        return subject.asObservable();
    }

    @Nullable
    public SupportTopic getTopicById(int topicId) {
        SupportTopic topic = findTopic(topicId, parentTopicsSubject);
        if (topic == null) {
            int size = childTopicsMap.size();
            for (int i = 0; i < size; i++) {
                BehaviorSubject<List<SupportTopic>> subject = childTopicsMap.get(childTopicsMap.keyAt(i));
                topic = findTopic(topicId, subject);
                if (topic != null) {
                    break;
                }
            }
        }
        return topic;
    }

    public void destroy() {
        listSubscriptions.clear();
        childTopicsMap.clear();
        if (parentTopicsSubject != null) {
            parentTopicsSubject.onCompleted();
        }
    }

    private void requestParentTopics() {
        listSubscriptions.add(App.getDataManager().getSupportService()
                .getSupportTopics(CurrentAvatarType.getAvatarType().avatarType)
                .subscribeOn(RxSchedulers.network())
                .subscribe(new ApiSubscriber2<List<SupportTopic>>(callback) {
                    @Override
                    public void onNext(List<SupportTopic> supportTopics) {
                        super.onNext(supportTopics);
                        parentTopicsSubject.onNext(supportTopics);
                    }
                }));
    }

    private void requestChildTopics(int parentTopicId) {
        listSubscriptions.add(App.getDataManager().getSupportService()
                .getSupportTopicsByParent(parentTopicId)
                .subscribeOn(RxSchedulers.network())
                .subscribe(new ApiSubscriber2<List<SupportTopic>>(callback) {
                    @Override
                    public void onNext(List<SupportTopic> supportTopics) {
                        super.onNext(supportTopics);
                        BehaviorSubject<List<SupportTopic>> subject = childTopicsMap.get(parentTopicId);
                        if (subject != null) {
                            subject.onNext(supportTopics);
                        }
                    }
                }));
    }

    @Nullable
    private SupportTopic findTopic(int topicId, @Nullable BehaviorSubject<List<SupportTopic>> subject) {
        if (subject != null) {
            List<SupportTopic> list = subject.getValue();
            if (list != null && !list.isEmpty()) {
                for (SupportTopic topic : list) {
                    if (topic.getId() == topicId) {
                        return topic;
                    }
                }
            }
        }
        return null;
    }

}
