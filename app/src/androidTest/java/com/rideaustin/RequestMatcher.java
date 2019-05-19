package com.rideaustin;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by Sergey Petrov on 16/05/2017.
 */

public abstract class RequestMatcher {

    public abstract Result match(@NonNull RequestStats requestStats);

    protected static Result success() {
        return new Result(true, null);
    }

    protected static Result fail() {
        return fail(null);
    }

    protected static Result fail(@Nullable String message) {
        return new Result(false, message);
    }

    public static class Result {

        private boolean isSuccess;

        private String message;

        private Result(boolean isSuccess, @Nullable String message) {
            this.isSuccess = isSuccess;
            this.message = message;
        }

        public boolean isSuccess() {
            return isSuccess;
        }

        public String getMessage() {
            return message;
        }

    }
}
