package com.rideaustin.utils;

import android.app.Dialog;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import timber.log.Timber;

/**
 * Created by Sergey Petrov on 08/05/2017.
 */

public class DialogUtils {

    /**
     * Safely dismiss the dialog, catching all lifecycle exceptions.
     * Should be used wherever caller is not sure dialog is visible and host is in correct state.
     * Should not be used when dismissal is caused by user interaction inside the dialog.
     * Same as {@link DialogUtils#dismiss(Dialog, String)} with default error message
     * <p/>
     *
     * @param dialog dialog to dismiss
     * @return whether dialog was successfully dismissed
     */
    public static boolean dismiss(@Nullable Dialog dialog) {
        return dismiss(dialog, "Unable to dismiss dialog");
    }

    /**
     * Safely dismiss the dialog, catching all lifecycle exceptions.
     * Should be used wherever caller is not sure dialog is visible and host is in correct state.
     * Should not be used when dismissal is caused by user interaction inside the dialog.
     * On exception {@code errorMessage} will be logged.
     * <p/>
     *
     * @param dialog dialog to dismiss
     * @param errorMessage error message to log on exception
     * @return whether dialog was successfully dismissed
     */
    public static boolean dismiss(@Nullable Dialog dialog, @NonNull String errorMessage) {
        if (isShowing(dialog)) {
            try {
                dialog.dismiss();
                return true;
            } catch (Exception e) {
                Timber.e(e, errorMessage);
            }
        }
        return false;
    }

    /**
     * Checks whether {@code dialog} is showing
     *
     * @param dialog dialog to check
     * @return whether dialog is showing
     */
    public static boolean isShowing(@Nullable Dialog dialog) {
        return dialog != null && dialog.isShowing();
    }
}
