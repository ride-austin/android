package com.rideaustin.ui;

import com.rideaustin.R;
import com.rideaustin.ui.utils.ProfileValidator;
import com.rideaustin.utils.Constants;
import com.rideaustin.utils.localization.Localizer;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

/**
 * Created by Viktor Kifer
 * On 17-Dec-2016.
 */
public class ProfileValidatorTest {

    @Mock
    Localizer localizer;

    private ProfileValidator validator;

    private static final String NO_NAME = "No name";
    private static final String SHORT_NAME = "Short name";
    private static final String INVALID_NAME = "Invalid name";

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        validator = new ProfileValidator(localizer);
        when(localizer.getString(R.string.first_name_error)).thenReturn(NO_NAME);
        when(localizer.getString(R.string.first_name_must_be, Constants.NAME_FIELD_LENGTH)).thenReturn(SHORT_NAME);
        when(localizer.getString(R.string.first_name_special_chars_error)).thenReturn(INVALID_NAME);

        when(localizer.getString(R.string.last_name_error)).thenReturn(NO_NAME);
        when(localizer.getString(R.string.last_name_must_be, Constants.NAME_FIELD_LENGTH)).thenReturn(SHORT_NAME);
        when(localizer.getString(R.string.last_name_special_chars_error)).thenReturn(INVALID_NAME);
    }

    @Test
    public void checkFirstName_emptyName_returnsError() throws Exception {
        String error = validator.checkFirstName("");

        assertEquals(NO_NAME, error);
    }

    @Test
    public void checkFirstName_shortName_returnsError() throws Exception {
        String error = validator.checkFirstName("J");

        assertEquals(SHORT_NAME, error);
    }

    @Test
    public void checkFirstName_numbersInName_returnsError() throws Exception {
        String error = validator.checkFirstName("John 12");

        assertEquals(INVALID_NAME, error);
    }

    @Test
    public void checkFirstName_specialCharsInName_returnsError() throws Exception {
        String error = validator.checkFirstName("John %@");

        assertEquals(INVALID_NAME, error);
    }

    @Test
    public void checkFirstName_normalName_returnsNull() throws Exception {
        String error = validator.checkFirstName("John");

        assertEquals(null, error);
    }

    @Test
    public void checkFirstName_nameWithDot_returnsNull() throws Exception {
        String error = validator.checkFirstName("Jr. Jhanny John");

        assertEquals(null, error);
    }

    @Test
    public void checkFirstName_nameWithHyphen_returnsNull() throws Exception {
        String error = validator.checkFirstName("John-Johny Doe");

        assertEquals(null, error);
    }

    @Test
    public void checkFirstName_nameWithApostrophe_returnsNull() throws Exception {
        String error = validator.checkFirstName("De'coupe Le Master");

        assertEquals(null, error);
    }

    @Test
    public void checkLastName_emptyName_returnsError() throws Exception {
        String error = validator.checkLastName("");

        assertEquals(NO_NAME, error);
    }

    @Test
    public void checkLastName_shortName_returnsError() throws Exception {
        String error = validator.checkLastName("J");

        assertEquals(SHORT_NAME, error);
    }

    @Test
    public void checkLastName_numbersInName_returnsError() throws Exception {
        String error = validator.checkLastName("John 12");

        assertEquals(INVALID_NAME, error);
    }

    @Test
    public void checkLastName_specialCharsInName_returnsError() throws Exception {
        String error = validator.checkLastName("John %@");

        assertEquals(INVALID_NAME, error);
    }

    @Test
    public void checkLastName_normalName_returnsNull() throws Exception {
        String error = validator.checkLastName("John");

        assertEquals(null, error);
    }

    @Test
    public void checkLastName_nameWithLabel_returnsNull() throws Exception {
        String error = validator.checkLastName("Mr. Johnson, the senior");

        assertEquals(null, error);
    }
}