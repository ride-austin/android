package com.rideaustin.base.retrofit;

import com.rideaustin.App;
import com.rideaustin.R;

import java.io.IOException;
import java.lang.annotation.Annotation;

import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Response;
import retrofit2.Retrofit;
import timber.log.Timber;

/**
 * Reference: https://gist.github.com/vipulshah2010/2178a46bbed3d7bf9ce2334c02079a36
 */
public class RetrofitException extends RuntimeException {

    private int statusCode = -1;

    public static RetrofitException httpError(String url, Response response, Retrofit retrofit) {
        String message = response.code() + " " + response.message();
        return new RetrofitException(message, url, response, Kind.HTTP, null, retrofit);
    }

    public static RetrofitException networkError(IOException exception) {
        return new RetrofitException(exception.getMessage(), null, null, Kind.NETWORK, exception, null);
    }

    public static RetrofitException unexpectedError(Throwable exception) {
        return new RetrofitException(exception.getMessage(), null, null, Kind.UNEXPECTED, exception, null);
    }

    /**
     * Identifies the event kind which triggered a {@link RetrofitException}.
     */
    public enum Kind {
        /**
         * An {@link IOException} occurred while communicating to the server.
         */
        NETWORK,
        /**
         * A non-200 HTTP status code was received from the server.
         */
        HTTP,
        /**
         * An internal error occurred while attempting to execute a request. It is best practice to
         * re-throw this exception so your application crashes.
         */
        UNEXPECTED
    }

    private final String url;
    private final Kind kind;
    private String errorMessage;
    private final transient Response response;
    private final transient Retrofit retrofit;

    RetrofitException(String message, String url, Response response, Kind kind, Throwable exception, Retrofit retrofit) {
        super(message, exception);
        this.url = url;
        this.response = response;
        this.kind = kind;
        this.retrofit = retrofit;
        Timber.w("::RetrofitException:: kind: %s Response: %s, URL: %s message: %s", kind, response, url, message);
        if (kind == Kind.HTTP) {
            statusCode = response.code();
            Timber.d("::RetrofitException:: StatusCode: %d", statusCode);
            try {
                errorMessage = response.errorBody().string();
                Timber.v("::RetrofitException:: Message: %s StatusCode: %d", errorMessage, statusCode);
            } catch (Exception e) {
                e.printStackTrace();
                Timber.e(e, "::RetrofitException:: %s", e.getMessage());
                errorMessage = App.getInstance().getString(R.string.error_unknown);
            }
        } else if (kind == Kind.NETWORK) {
            errorMessage = App.getInstance().getString(R.string.network_error);
        } else {
            errorMessage = App.getInstance().getString(R.string.error_unknown);
        }
    }

    /**
     * The request URL which produced the error.
     */
    public String getUrl() {
        return url;
    }

    /**
     * Response object containing status code, headers, body, etc.
     */
    public Response getResponse() {
        return response;
    }

    @Override
    public String getMessage() {
        if (errorMessage == null) {
            errorMessage = super.getMessage();
        }
        return errorMessage;
    }

    /**
     * The event kind which triggered this error.
     */
    public Kind getKind() {
        return kind;
    }

    /**
     * The Retrofit this request was executed on
     */
    public Retrofit getRetrofit() {
        return retrofit;
    }

    /**
     * HTTP response body converted to specified {@code type}. {@code null} if there is no
     * response.
     *
     * @throws IOException if unable to convert the body to the specified {@code type}.
     */
    public <T> T getErrorBodyAs(Class<T> type) throws IOException {
        if (response == null || response.errorBody() == null) {
            return null;
        }
        Converter<ResponseBody, T> converter = retrofit.responseBodyConverter(type, new Annotation[0]);
        return converter.convert(response.errorBody());
    }

    public int getStatusCode() {
        return statusCode;
    }

    public boolean causedByNetwork() {
        return kind == Kind.NETWORK;
    }
}