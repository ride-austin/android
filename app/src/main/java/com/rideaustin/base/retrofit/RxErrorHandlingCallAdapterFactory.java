package com.rideaustin.base.retrofit;

import android.support.annotation.NonNull;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import retrofit2.Call;
import retrofit2.CallAdapter;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.HttpException;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import rx.Observable;
import rx.subjects.PublishSubject;
import timber.log.Timber;


/**
 * Reference : https://gist.github.com/vipulshah2010/2178a46bbed3d7bf9ce2334c02079a36
 */
public class RxErrorHandlingCallAdapterFactory extends CallAdapter.Factory {
    private final RxJavaCallAdapterFactory original;
    private final PublishSubject<RetrofitException> httpErrorSubject;
    private final PublishSubject<RetrofitException> networkErrorSubject;

    public RxErrorHandlingCallAdapterFactory(PublishSubject<RetrofitException> httpErrorSubject, PublishSubject<RetrofitException> networkErrorSubject) {
        original = RxJavaCallAdapterFactory.create();
        this.httpErrorSubject = httpErrorSubject;
        this.networkErrorSubject = networkErrorSubject;
    }

    public static CallAdapter.Factory create(PublishSubject<RetrofitException> httpErrorSubject, PublishSubject<RetrofitException> networkErrorSubject) {
        return new RxErrorHandlingCallAdapterFactory(httpErrorSubject, networkErrorSubject);
    }

    @Override
    public CallAdapter<?, ?> get(Type returnType, Annotation[] annotations, Retrofit retrofit) {
        return new RxCallAdapterWrapper(retrofit, original.get(returnType, annotations, retrofit), httpErrorSubject, networkErrorSubject);
    }

    private static class RxCallAdapterWrapper<R> implements CallAdapter<R, Observable<R>> {
        private final Retrofit retrofit;
        private final CallAdapter<R, ?> wrapped;
        private final PublishSubject<RetrofitException> httpErrorSubject;
        private final PublishSubject<RetrofitException> networkErrorSubject;

        public RxCallAdapterWrapper(Retrofit retrofit, CallAdapter<R, ?> wrapped,
                                    PublishSubject<RetrofitException> httpErrorSubject,
                                    PublishSubject<RetrofitException> networkErrorSubject) {
            this.retrofit = retrofit;
            this.wrapped = wrapped;
            this.httpErrorSubject = httpErrorSubject;
            this.networkErrorSubject = networkErrorSubject;
        }

        @Override
        public Type responseType() {
            return wrapped.responseType();
        }

        @Override
        public Observable<R> adapt(@NonNull final Call<R> call) {
            return ((Observable<R>)wrapped.adapt(call)).onErrorResumeNext(throwable -> Observable.error(asRetrofitException(throwable)));
        }

        private RetrofitException asRetrofitException(Throwable throwable) {
            RetrofitException exception = null;
            try {
                // We had non-200 http error
                if (throwable instanceof HttpException) {
                    HttpException httpException = (HttpException) throwable;
                    Response response = httpException.response();
                    exception = RetrofitException.httpError(response.raw().request().url().toString(), response, retrofit);
                    httpErrorSubject.onNext(exception);
                }
                // A network error happened
                if (throwable instanceof IOException) {
                    exception = RetrofitException.networkError((IOException) throwable);
                    networkErrorSubject.onNext(exception);
                }
            } catch (Exception e) {
                // RA-9681: sometimes code crashes inside this block
                // probably HttpException.response is null sometimes
                // Need to monitor Crashlytics for this message.
                Timber.e(e, "Something went wrong with wrapping error");
            }
            if (exception == null) {
                // We don't know what happened. We need to simply convert to an unknown error
                exception = RetrofitException.unexpectedError(throwable);
            }
            return exception;
        }
    }
}