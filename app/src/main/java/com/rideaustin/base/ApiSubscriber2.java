package com.rideaustin.base;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

import com.rideaustin.App;
import com.rideaustin.R;
import com.rideaustin.base.retrofit.RetrofitException;
import com.rideaustin.utils.NetworkHelper;
import com.rideaustin.utils.toast.RAToast;

import java.net.HttpURLConnection;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import retrofit2.Response;
import rx.Subscriber;
import timber.log.Timber;

/**
 * Created by kshumelchyk on 7/13/16.
 */
public class ApiSubscriber2<C> extends Subscriber<C> {

    @Nullable
    private ProgressCallback callback;
    private boolean showErrorToasts;

    public ApiSubscriber2(boolean showErrorToasts) {
        this(null, showErrorToasts);
    }

    public ApiSubscriber2(@NonNull ProgressCallback callback) {
        this(callback, true);
    }

    public ApiSubscriber2(@Nullable ProgressCallback callback, boolean showErrorToasts) {
        this.callback = callback;
        this.showErrorToasts = showErrorToasts;
    }

    @Override
    public void onStart() {
        if (callback != null)
            callback.showProgress();
    }

    @Override
    public void onNext(C c) {}

    @Override
    public void onCompleted() {
        if (callback != null)
            callback.hideProgress();
    }

    @Override
    public final void onError(Throwable e) {
        if (callback != null)
            callback.hideProgress();

        if (e instanceof RetrofitException) {
            RetrofitException retrofitException = (RetrofitException) e;
            Response response = retrofitException.getResponse();
            if (response != null) {
                int code = response.code();
                switch (code) {
                    case HttpURLConnection.HTTP_UNAUTHORIZED:
                        onUnauthorized(new BaseApiException(code, e.getMessage(), e));
                        break;
                    case HttpURLConnection.HTTP_BAD_REQUEST:
                        onBadRequestError(new BaseApiException(code, e.getMessage(), e));
                        break;
                    default:
                        onHttpError(new BaseApiException(code, e.getMessage(), e));
                        break;
                }
            } else if (!NetworkHelper.isNetworkAvailable() || retrofitException.causedByNetwork()) {
                onNetworkError(new BaseApiException(0, App.getInstance().getString(R.string.network_error), e, true));
            } else {
                onUnknownError(new BaseApiException(0, App.getInstance().getString(R.string.error_unknown), e));
            }

        } else {
            Timber.e(e, "Error cause is not retrofit. Pay attention to this.");
            onUnknownError(new BaseApiException(0, e.getMessage(), e));
        }
    }

    public ProgressCallback getCallback() {
        return callback;
    }

    public void onUnauthorized(BaseApiException e) {
        App.getDataManager().logoutUserFromApp(e.getMessage());
        onAnyError(e);
    }

    public void onBadRequestError(BaseApiException e) {
        if (showErrorToasts) {
            showToast(e.getMessage());
        }
        onAnyError(e);
    }

    public void onHttpError(BaseApiException e) {
        if (showErrorToasts) {
            if (e.getCode() < 500) {
                showToast(e.getMessage());
            } else {
                showToast(R.string.error_unknown);
            }
        }
        onAnyError(e);
    }

    public void onNetworkError(BaseApiException e) {
        if (showErrorToasts) {
            showToast(R.string.network_error);
        }
        onAnyError(e);
    }

    public void onUnknownError(BaseApiException e) {
        onAnyError(e);
    }

    public void onAnyError(BaseApiException e) {
        // do nothing by default
    }

    public static RetrofitException createError(int httpCode, String message) {
        ResponseBody body = ResponseBody.create(MediaType.parse("text"), message);
        return RetrofitException.httpError("", Response.error(httpCode, body), null);
    }

    protected void showToast(@StringRes int resId) {
        showToast(App.getInstance().getString(resId));
    }

    protected void showToast(String message) {
        RAToast.showShort(message);
    }
}
