package cases.s5_core_flow.s3_driver_arrived.s1_main;

import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.rideaustin.BaseUITest;
import com.rideaustin.R;
import com.rideaustin.RaActivityRule;
import com.rideaustin.RequestType;
import com.rideaustin.RiderMockResponseFactory;
import com.rideaustin.TestCases;
import com.rideaustin.ui.drawer.NavigationDrawerActivity;
import com.rideaustin.ui.utils.UIUtils;
import com.rideaustin.utils.MapTestUtil;
import com.rideaustin.utils.Matchers;
import com.rideaustin.utils.NavigationUtils;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isClickable;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.rideaustin.utils.MatcherUtils.hasDrawable;
import static com.rideaustin.utils.Matchers.condition;
import static com.rideaustin.utils.ViewActionUtils.waitFor;
import static com.rideaustin.utils.ViewActionUtils.waitForCompletelyDisplayed;
import static com.rideaustin.utils.ViewActionUtils.waitForDisplayed;
import static com.rideaustin.utils.ViewActionUtils.waitForViewInWindow;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.not;

/**
 * Reference: RA-11290
 * Created by Sergey Petrov on 14/06/2017.
 */

@LargeTest
@RunWith(AndroidJUnit4.class)
public class DriverArrivedMainSuite extends BaseUITest {

    @Rule
    public ActivityTestRule<NavigationDrawerActivity> activityRule = new RaActivityRule<>(NavigationDrawerActivity.class, false, false);

    @Override
    public void setUp() {
        super.setUp();
        initMockResponseFactory(RiderMockResponseFactory.class);
        mockRequests(RequestType.GLOBAL_APP_INFO_200_GET,
                RequestType.LOGIN_SUCCESS_200_POST,
                RequestType.TOKENS_200_POST,
                RequestType.RIDER_DATA_NO_RIDE_200_GET,
                RequestType.DRIVER_TYPES_200_GET,
                RequestType.SURGE_AREA_EMPTY_200_GET,
                RequestType.CONFIG_RIDER_200_GET,
                RequestType.EVENTS_EMPTY_200_GET,
                RequestType.CONFIG_ZIPCODES_200_GET,
                RequestType.RIDE_CANCELLATION_SETTINGS_200_GET,
                RequestType.ACDR_REGULAR_200_GET); // has drivers
    }

    /**
     * C1930696: Route, driver position and ETA pin
     * C1930697: Driver bar - name, car name & license plate, ratings, car type
     * C1930700: Driver photo is correct and can be expanded
     * C1930699: Car photo is correct and can be expanded
     */
    @Test
    @TestCases({"C1930696", "C1930697", "C1930700", "C1930699"})
    public void showDriverInReachedState() throws InterruptedException {
        NavigationUtils.startActivity(activityRule);

        // login
        NavigationUtils.throughLogin("valid@email.com", "whatever");

        // request ride
        NavigationUtils.throughRideRequest(this);

        // simulate reached state
        NavigationUtils.toAssignedState(this);
        NavigationUtils.toReachedState(this);

        //------------------------------------------------------------------------------------------
        // C1930696: Route, driver position and ETA pin
        //------------------------------------------------------------------------------------------

        // wait for map animation
        waitFor("Map animation", 1000);

        // check car is visible
        MapTestUtil.assertCarMarkersVisible();
        MapTestUtil.assertCarMarkersCount(1);

        // check pickup is visible
        MapTestUtil.assertPickupMarkersVisible();

        //------------------------------------------------------------------------------------------
        // C1930697: Driver bar - name, car name & license plate, ratings, car type
        //------------------------------------------------------------------------------------------

        String name = "Ride15";
        String rating = UIUtils.formatRating(5.0);
        String category = "STANDARD";
        String car = "Blue Cadillac CTS Wagon";
        String plate = "CADILLAC";

        // check collapsed state
        onView(allOf(withId(R.id.driver_name_small), isDisplayed())).check(matches(withText(name)));
        onView(allOf(withId(R.id.driver_rate_small), isDisplayed())).check(matches(withText(rating)));
        onView(allOf(withId(R.id.car_category_small), isDisplayed())).check(matches(withText(category)));
        onView(allOf(withId(R.id.car_make_small), isDisplayed())).check(matches(withText(car)));
        onView(allOf(withId(R.id.car_plate_small), isDisplayed())).check(matches(withText(plate)));
        onView(allOf(withId(R.id.driver_image_small), isDisplayed())).check(matches(hasDrawable()));

        // tap on driver image should cause panel expand
        onView(withId(R.id.driver_image_small)).perform(click());
        waitForCompletelyDisplayed(R.id.ride_details);
        Matchers.waitFor(condition("Driver small image should be hidden")
                .withMatcher(withId(R.id.driver_image_small))
                .withCheck(not(isDisplayed())));

        onView(withId(R.id.driver_name_small)).check(matches(not(isDisplayed())));
        onView(withId(R.id.driver_rate_small)).check(matches(not(isDisplayed())));
        onView(withId(R.id.car_category_small)).check(matches(not(isDisplayed())));
        onView(withId(R.id.car_make_small)).check(matches(not(isDisplayed())));
        onView(withId(R.id.car_plate_small)).check(matches(not(isDisplayed())));

        // check expanded state
        onView(allOf(withId(R.id.tv_driver_name), isDisplayed())).check(matches(withText(name)));
        onView(allOf(withId(R.id.tv_driver_rate), isDisplayed())).check(matches(withText(rating)));
        onView(allOf(withId(R.id.car_category), isDisplayed())).check(matches(withText(category)));
        onView(allOf(withId(R.id.car_color_make_model), isDisplayed())).check(matches(withText(car)));
        onView(allOf(withId(R.id.car_plate), isDisplayed())).check(matches(withText(plate)));
        onView(allOf(withId(R.id.driver_image), isDisplayed())).check(matches(hasDrawable()));
        onView(allOf(withId(R.id.car_image), isDisplayed())).check(matches(hasDrawable()));

        //------------------------------------------------------------------------------------------
        // C1930700: Driver photo is correct and can be expanded
        //------------------------------------------------------------------------------------------

        waitForDisplayed(R.id.driver_image);

        // tap on driver photo
        onView(withId(R.id.driver_image)).perform(click());

        // check the dialog
        waitForViewInWindow(allOf(withId(R.id.dialogImageView), isDisplayed(), hasDrawable()));
        onView(allOf(withId(R.id.btnClose), isDisplayed(), isClickable())).perform(click());
        waitFor("Animation", 1000);

        //------------------------------------------------------------------------------------------
        // C1930699: Car photo is correct and can be expanded
        //------------------------------------------------------------------------------------------

        waitForDisplayed(R.id.car_image);

        // tap on car photo
        onView(withId(R.id.car_image)).perform(click());

        // check the dialog
        waitForViewInWindow(allOf(withId(R.id.dialogImageView), isDisplayed(), hasDrawable()));
        onView(allOf(withId(R.id.btnClose), isDisplayed(), isClickable())).perform(click());
    }


}
