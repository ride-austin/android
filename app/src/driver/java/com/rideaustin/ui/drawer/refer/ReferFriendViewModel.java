package com.rideaustin.ui.drawer.refer;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.view.View;

import com.rideaustin.App;
import com.rideaustin.BR;
import com.rideaustin.api.config.GlobalConfig;
import com.rideaustin.api.config.ReferFriend;
import com.rideaustin.schedulers.RxSchedulers;

import rx.subscriptions.CompositeSubscription;

/**
 * Created by hatak on 02.11.16.
 */

public class ReferFriendViewModel extends BaseObservable {

    private CompositeSubscription subscriptions = new CompositeSubscription();
    private String message;
    private String header;
    private int emailVisibility = View.VISIBLE;
    private int textVisibility = View.VISIBLE;

    public void onStop() {
        subscriptions.clear();
    }

    public void onStart() {
        subscriptions.add(App.getConfigurationManager()
                .getConfigurationUpdates()
                .subscribeOn(RxSchedulers.network())
                .observeOn(RxSchedulers.main())
                .subscribe(this::updateUi));
    }

    private void updateUi(final GlobalConfig configuration) {
        if(configuration != null){
            final ReferFriend referFriend = configuration.getReferFriend();
            if(referFriend != null){
                setMessage(referFriend.getBody());
                setHeader(referFriend.getHeader());
                if(!referFriend.getSmsEnabled()){
                    setTextVisibility(View.GONE);
                }
                if(!referFriend.getEmailEnabled()){
                    setEmailVisibility(View.GONE);
                }
            }
        }
    }

    @Bindable
    public String getHeader() {
        return header;
    }

    public void setHeader(final String header) {
        this.header = header;
        notifyPropertyChanged(BR.header);
    }

    public void setMessage(final String message) {
        this.message = message;
        notifyPropertyChanged(BR.message);
    }

    @Bindable
    public String getMessage() {
        return message;
    }

    @Bindable
    public int getEmailVisibility() {
        return emailVisibility;
    }

    public void setEmailVisibility(final int emailVisibility) {
        this.emailVisibility = emailVisibility;
        notifyPropertyChanged(BR.emailVisibility);
    }

    @Bindable
    public int getTextVisibility() {
        return textVisibility;
    }

    public void setTextVisibility(final int textVisibility) {
        this.textVisibility = textVisibility;
        notifyPropertyChanged(BR.textVisibility);
    }
}
