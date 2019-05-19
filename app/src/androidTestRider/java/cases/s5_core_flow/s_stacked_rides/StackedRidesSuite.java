package cases.s5_core_flow.s_stacked_rides;

import android.support.test.rule.ActivityTestRule;
import android.support.test.uiautomator.UiObjectNotFoundException;

import com.rideaustin.BaseUITest;
import com.rideaustin.R;
import com.rideaustin.RaActivityRule;
import com.rideaustin.RequestType;
import com.rideaustin.RiderMockResponseFactory;
import com.rideaustin.TestCases;
import com.rideaustin.api.model.Ride;
import com.rideaustin.ui.drawer.NavigationDrawerActivity;
import com.rideaustin.ui.utils.UIUtils;
import com.rideaustin.utils.MapTestUtil;
import com.rideaustin.utils.NavigationUtils;
import com.rideaustin.utils.ViewActionUtils;

import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isClickable;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.rideaustin.utils.MatcherUtils.hasDrawable;
import static com.rideaustin.utils.Matchers.condition;
import static com.rideaustin.utils.Matchers.waitFor;
import static com.rideaustin.utils.ViewActionUtils.waitForCompletelyDisplayed;
import static com.rideaustin.utils.ViewActionUtils.waitForDisplayed;
import static com.rideaustin.utils.ViewActionUtils.waitForViewInWindow;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.allOf;

/**
 * Created on 22/12/2017
 *
 * @author sdelaysam
 */

public class StackedRidesSuite extends BaseUITest {

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
                RequestType.ACDR_REGULAR_200_GET, // has drivers
                RequestType.RIDE_CANCEL_200_DELETE);
    }

    @Test
    @TestCases({"C3502858", "C3502866", "C3502865", "C3502871", "C3502860",
            "C3502863", "C3502864", "C3502867", "C3502869"})
    public void testStackedRide() throws InterruptedException, UiObjectNotFoundException {
        NavigationUtils.startActivity(activityRule);

        // login
        NavigationUtils.throughLogin("valid@email.com", "whatever");

        // request ride
        NavigationUtils.throughRideRequest(this);

        // simulate assigned state
        NavigationUtils.toAssignedState(this, response -> {
            response.setPrevRide(getPrevRide());
            return response;
        });

        waitFor(condition("Should show stacked ride indicator")
                .withMatcher(withId(R.id.prevRide))
                .withMatcher(withText(R.string.stacked_ride_prev_ride)));

        MapTestUtil.zoomOut(200);
        MapTestUtil.assertPrevRideMarkersVisible(5000);

        // check eta is visible and correct (according to RIDE_DRIVER_ASSIGNED_200)
        MapTestUtil.assertEtaMarkerVisible(getString(R.string.zero_mins));

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

        onView(withId(R.id.driver_name_small)).check(matches(Matchers.not(isDisplayed())));
        onView(withId(R.id.driver_rate_small)).check(matches(Matchers.not(isDisplayed())));
        onView(withId(R.id.car_category_small)).check(matches(Matchers.not(isDisplayed())));
        onView(withId(R.id.car_make_small)).check(matches(Matchers.not(isDisplayed())));
        onView(withId(R.id.car_plate_small)).check(matches(Matchers.not(isDisplayed())));

        // check expanded state
        onView(allOf(withId(R.id.tv_driver_name), isDisplayed())).check(matches(withText(name)));
        onView(allOf(withId(R.id.tv_driver_rate), isDisplayed())).check(matches(withText(rating)));
        onView(allOf(withId(R.id.car_category), isDisplayed())).check(matches(withText(category)));
        onView(allOf(withId(R.id.car_color_make_model), isDisplayed())).check(matches(withText(car)));
        onView(allOf(withId(R.id.car_plate), isDisplayed())).check(matches(withText(plate)));
        onView(allOf(withId(R.id.driver_image), isDisplayed())).check(matches(hasDrawable()));
        onView(allOf(withId(R.id.car_image), isDisplayed())).check(matches(hasDrawable()));

        waitForDisplayed(R.id.driver_image);

        // tap on driver photo
        onView(withId(R.id.driver_image)).perform(click());

        // check the dialog
        waitForViewInWindow(allOf(withId(R.id.dialogImageView), isDisplayed(), hasDrawable()));
        onView(allOf(withId(R.id.btnClose), isDisplayed(), isClickable())).perform(click());
        ViewActionUtils.waitFor("Animation", 1000);

        waitForDisplayed(R.id.car_image);

        // tap on car photo
        onView(withId(R.id.car_image)).perform(click());

        // check the dialog
        waitForViewInWindow(allOf(withId(R.id.dialogImageView), isDisplayed(), hasDrawable()));
        onView(allOf(withId(R.id.btnClose), isDisplayed(), isClickable())).perform(click());

        // simulate reached state
        NavigationUtils.toReachedState(this, response -> {
            response.setPrevRide(getPrevRide());
            return response;
        });

        waitFor(condition("Should hide stacked ride indicator")
                .withMatcher(withId(R.id.prevRide))
                .withMatcher(withText(R.string.stacked_ride_prev_ride))
                .withCheck(not(isDisplayed())));

        MapTestUtil.zoomOut(200);
        MapTestUtil.assertPrevRideMarkersNotVisible();
    }


    private Ride getPrevRide() {
        Ride ride = getResponse("RIDE_DRIVER_ASSIGNED_200", Ride.class);

        Ride prev = getResponse("RIDE_ACTIVE_200", Ride.class);
        prev.setEndLocationLat(ride.getStartLocationLat() + 0.001);
        prev.setEndLocationLong(ride.getStartLocationLong() + 0.001);
        return prev;
    }

}
