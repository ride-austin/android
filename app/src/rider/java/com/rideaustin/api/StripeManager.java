package com.rideaustin.api;

import android.content.Context;
import android.support.annotation.VisibleForTesting;

import com.rideaustin.BuildConfig;
import com.rideaustin.schedulers.RxSchedulers;
import com.stripe.android.Stripe;
import com.stripe.android.TokenCallback;
import com.stripe.android.model.Card;
import com.stripe.android.model.Token;

import rx.Observable;
import rx.Subscriber;
import rx.observables.AsyncOnSubscribe;
import timber.log.Timber;

/**
 * Created by vharshyn on 11.07.2016.
 */
public class StripeManager {

    private Stripe stripe;

    public StripeManager(Context context) {
        stripe = new Stripe(context, BuildConfig.STRIPE_KEY);
    }

    /**
     * Make stripe token call via RxJava
     *
     * @param card
     */
    public Observable<Token> getStripeToken(final Card card) {
        return tokenProvider.getStripeToken(stripe, card)
                .subscribeOn(RxSchedulers.main());
    }

    @VisibleForTesting
    public interface StripeTokenProvider {
        Observable<Token> getStripeToken(Stripe stripe, Card card);
    }

    @VisibleForTesting
    public static StripeTokenProvider tokenProvider = (stripe, card) ->
            Observable.create((Observable.OnSubscribe<Token>) subscriber ->
                    stripe.createToken(card, new TokenCallback() {
                        public void onSuccess(Token token) {
                            if (!subscriber.isUnsubscribed()) {
                                subscriber.onNext(token);
                                subscriber.onCompleted();
                            }
                        }

                        public void onError(Exception error) {
                            subscriber.onError(error);
                            Timber.e(error, "Stripe:: create token problems");
                        }
                    }));
}