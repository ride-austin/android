package com.rideaustin.base;

import com.rideaustin.App;
import com.rideaustin.R;
import com.rideaustin.base.retrofit.RetrofitException;
import com.rideaustin.schedulers.RxSchedulers;
import com.rideaustin.utils.NetworkHelper;
import com.rideaustin.utils.toast.RAToast;

import java.net.HttpURLConnection;

import retrofit2.Response;
import rx.Subscriber;
import timber.log.Timber;

/**
 * Created by kshumelchyk on 7/13/16.
 */
@Deprecated
/**
 * Please not use this class in further development, use ApiSubscriber2
 */
public class ApiSubscriber<C> extends Subscriber<C> {

    private ProgressCallback callback;

    public ApiSubscriber() {

    }

    public ApiSubscriber(ProgressCallback callback) {
        this.callback = callback;
    }

    @Override
    public void onStart() {
        Timber.d("request start");
        if (callback != null)
            callback.showProgress();
    }

    @Override
    public void onNext(C c) {
        Timber.d("on next element : %s", c);
    }

    @Override
    public void onCompleted() {
        Timber.d("request completed");
        if (callback != null)
            callback.hideProgress();
    }

    @Override
    public final void onError(Throwable e) {
        Timber.e(e, "error executing request");

        if (callback != null) {
            callback.hideProgress();
            if (e instanceof RetrofitException) {
                RetrofitException retrofitException = (RetrofitException) e;
                Response response = retrofitException.getResponse();
                if (response != null) {
                    int code = response.code();
                    switch (code) {
                        case HttpURLConnection.HTTP_UNAUTHORIZED:
                            if (showDefaultErrors()) {
                                App.getDataManager().logoutUserFromApp(e.getMessage());
                            }
                            break;
                        case HttpURLConnection.HTTP_BAD_REQUEST:
                            if (showDefaultErrors()) {
                                RxSchedulers.schedule(() -> RAToast.showShort(e.getMessage()));
                            }
                            break;
                        default:
                            handleUnknownError();
                            break;
                    }
                    onError(new BaseApiException(code, e.getMessage(), e));
                } else if (!NetworkHelper.isNetworkAvailable() || retrofitException.causedByNetwork()) {
                    RAToast.showShort(R.string.network_error);
                } else {
                    Timber.e(e, "::onError:: Response == null");
                    onError(new BaseApiException(0, "We are experiencing technical difficulties", e));
                }
            } else {
                Timber.e(e, "Error cause is not retrofit. Pay attention to this.");
                onError(new BaseApiException(0, e.getMessage(), e));
            }
        } else {
            onError(new BaseApiException(0, e.getMessage(), e));
        }
    }

    public ProgressCallback getCallback() {
        return callback;
    }

    //Extracted it to a method so that implmentors can suppress toast by default and implement custom error dialogs
    protected void handleUnknownError() {
        RAToast.showShort(R.string.error_unknown);
    }

    // needed if we want to show custom dialogs during errors
    protected boolean showDefaultErrors() {
        return true;
    }

    public void onError(BaseApiException e) {
        //do nothing by default
    }

}
