package com.rideaustin.ui.utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.rideaustin.R;
import com.rideaustin.api.model.User;
import com.rideaustin.utils.CommonConstants;
import com.rideaustin.utils.Constants;
import com.rideaustin.utils.localization.AndroidLocalizer;
import com.rideaustin.utils.localization.Localizer;

import java.util.regex.Pattern;

/**
 * Created by office on 12/17/16.
 */

public class ProfileValidator {
    private static final Pattern PERSON_NAME = Pattern.compile("[A-Za-z][A-Za-z ,\\.'-]+");
    private Localizer localizer;

    public ProfileValidator(Localizer localizer) {
        this.localizer = localizer;
    }

    /**
     * Checks if firstName contains error
     * @param firstName
     * @return error string or null if no error
     */
    public @Nullable String checkFirstName(@NonNull String firstName) {
        if (firstName.isEmpty()) {
            return localizer.getString(R.string.first_name_error);
        } else if (firstName.length() < Constants.NAME_FIELD_LENGTH) {
            return localizer.getString(R.string.first_name_must_be, Constants.NAME_FIELD_LENGTH);
        } else if (!PERSON_NAME.matcher(firstName).matches()) {
            return localizer.getString(R.string.first_name_special_chars_error);
        }
        return null;
    }

    /**
     * Checks if lastName contains error
     * @param lastName
     * @return error string or null if no error
     */
    public @Nullable String checkLastName(@NonNull String lastName) {
        if (lastName.isEmpty()) {
            return localizer.getString(R.string.last_name_error);
        } else if (lastName.length() < Constants.NAME_FIELD_LENGTH) {
            return localizer.getString(R.string.last_name_must_be, Constants.NAME_FIELD_LENGTH);
        } else if (!PERSON_NAME.matcher(lastName).matches()) {
            return localizer.getString(R.string.last_name_special_chars_error);
        }
        return null;
    }

    /**
     * Checks if middleName contains error
     * @param middleName
     * @return error string or null if no error
     */
    public @Nullable String checkMiddleName(@NonNull String middleName) {
        if (middleName.isEmpty()) {
            return localizer.getString(R.string.confirm_middle_name);
        } else if (middleName.length() < Constants.NAME_FIELD_LENGTH) {
            return localizer.getString(R.string.middle_name_must_be, Constants.NAME_FIELD_LENGTH);
        } else if (!PERSON_NAME.matcher(middleName).matches()) {
            return localizer.getString(R.string.middle_name_special_chars_error);
        }
        return null;
    }

    /**
     * Checks if user data (firstName, lastName) is valid
     * @param user
     * @param context
     * @return true if data is valid, otherwise false
     */
    public static boolean validateUser(User user, Context context) {
        ProfileValidator validator = new ProfileValidator(new AndroidLocalizer(context));
        boolean firstNameValid = validator.checkFirstName(user.getFirstName()) == null;
        boolean lastNameValid = user.getLastName().equals(CommonConstants.FACEBOOK_EMPTY_LAST_NAME)
                || validator.checkLastName(user.getLastName()) == null;
        return firstNameValid && lastNameValid;
    }

}
