package com.rideaustin.ui.drawer.triphistory.forms;

import android.util.SparseArray;

import com.rideaustin.App;
import com.rideaustin.api.model.SupportForm;
import com.rideaustin.base.ApiSubscriber2;
import com.rideaustin.base.BaseActivityCallback;
import com.rideaustin.schedulers.RxSchedulers;

import java8.util.Optional;
import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subscriptions.CompositeSubscription;

public class SupportFormsModel {

    private final BaseActivityCallback callback;

    private SparseArray<BehaviorSubject<SupportForm>> supportForms = new SparseArray<>();

    private CompositeSubscription listSubscriptions = new CompositeSubscription();

    public SupportFormsModel(BaseActivityCallback callback) {
        this.callback = callback;
    }


    public Observable<SupportForm> getSupportFormObservable(int topicId) {
        BehaviorSubject<SupportForm> subject = supportForms.get(topicId);
        if (subject == null) {
            subject = BehaviorSubject.create();
            supportForms.put(topicId, subject);
        }
        if (!subject.hasValue()) {
            requestSupportForms(topicId);
        }
        return subject.asObservable();
    }

    public Optional<SupportForm> getFormByTopicId(int topicId) {
        return Optional.ofNullable(supportForms.get(topicId)).map(BehaviorSubject::getValue);
    }

    public void destroy() {
        listSubscriptions.clear();
        supportForms.clear();
    }

    private void requestSupportForms(int parentTopicId) {
        listSubscriptions.add(App.getDataManager().getSupportService()
                .getSupportFormByTopic(parentTopicId)
                .subscribeOn(RxSchedulers.network())
                .subscribe(new ApiSubscriber2<SupportForm>(callback) {
                    @Override
                    public void onNext(SupportForm supportForm) {
                        super.onNext(supportForm);
                        BehaviorSubject<SupportForm> subject = supportForms.get(parentTopicId);
                        if (subject != null) {
                            subject.onNext(supportForm);
                        }
                    }
                }));
    }
}
