package cases.s12_airport_queue;

import android.support.test.espresso.contrib.DrawerActions;
import android.support.test.espresso.contrib.NavigationViewActions;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.rideaustin.BaseUITest;
import com.rideaustin.DriverMockResponseFactory;
import com.rideaustin.R;
import com.rideaustin.RaActivityRule;
import com.rideaustin.RequestType;
import com.rideaustin.RequestWrapper;
import com.rideaustin.TestCases;
import com.rideaustin.ui.signin.SplashActivity;
import com.rideaustin.utils.NavigationUtils;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.rideaustin.RecyclerViewMatcher.withRecyclerView;
import static com.rideaustin.utils.MatcherUtils.exists;
import static com.rideaustin.utils.ViewActionUtils.waitFor;
import static com.rideaustin.utils.ViewActionUtils.waitForDisplayed;
import static com.rideaustin.utils.ViewActionUtils.waitForToast;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.AllOf.allOf;

/**
 * Created by hatak on 25.05.2017.
 */

@LargeTest
@RunWith(AndroidJUnit4.class)
public class AirportQueueSuite1 extends BaseUITest {

    @Rule
    public ActivityTestRule<SplashActivity> activityRule = new RaActivityRule<>(SplashActivity.class, false, false);

    @Override
    public void setUp() {
        super.setUp();
        initMockResponseFactory(DriverMockResponseFactory.class);
        mockRequests(RequestType.GLOBAL_APP_INFO_200_GET,
                RequestType.LOGIN_SUCCESS_200_POST,
                RequestType.CURRENT_USER_200_GET,
                RequestType.CONFIG_DRIVER_200_GET,
                RequestType.TOKENS_200_POST,
                RequestType.CURRENT_DRIVER_200_GET,
                RequestType.QUEUES_200_GET,
                RequestType.EVENTS_EMPTY_200_GET,
                RequestType.SURGE_AREA_EMPTY_200_GET,
                RequestType.ACTIVE_DRIVER_EMPTY_200_GET,
                RequestType.DRIVER_GO_OFFLINE_200_DELETE,
                RequestType.LOGOUT_200_POST,
                RequestType.DRIVER_GO_ONLINE_200_POST,
                RequestType.SURGE_AREA_EMPTY_200_GET,
                RequestType.DRIVER_TYPES_200_GET);
    }

    @Test
    @TestCases("C1929670")
    public void canSeeHisQueuePositionForEveryCarCategory() throws InterruptedException {
        NavigationUtils.startActivity(activityRule);
        NavigationUtils.throughLogin("driver@email.com", "whatever");

        waitFor("camera", 2000);

        removeRequests(RequestType.ACTIVE_DRIVER_EMPTY_200_GET);
        mockRequests(RequestType.ACTIVE_DRIVER_AVAILABLE_200_GET);
        mockRequests(RequestType.DRIVER_UPDATE_LOCATION_200_PUT);

        onView(allOf(withId(R.id.toolbarActionButton), withText(R.string.action_go_online))).perform(click());
        onView(allOf(withId(R.id.toolbarActionButton), withText(R.string.action_go_offline))).check(matches(isDisplayed()));

        mockRequests(RequestType.DRIVERS_CARTYPES_200_GET);
        mockRequests(RequestType.DRIVER_IN_RIDE_AUSTIN_AIRPORT_QUEUE_FIRST);

        removeRequests(RequestType.EVENTS_EMPTY_200_GET);
        chainRequests(RequestWrapper.wrap(RequestType.EVENT_QUEUED_AREA_ENTERING, 2000L),
                RequestWrapper.wrap(RequestType.EVENTS_EMPTY_200_GET, 2000L));

        waitForToast(withText(containsString("Welcome to the")));

        // open navigation drawer
        onView(withId(R.id.drawerLayout)).perform(DrawerActions.open());

        onView(allOf(withId(R.id.design_menu_item_text), withText("RideAustin Airport"))).perform(click());

        waitForDisplayed(R.id.queue_recycler_view);

        // check that he is in the queue
        onView(withId(R.id.queue_info)).check(matches(withText(R.string.queue_in_number_info)));


        // check categories
        onView(withRecyclerView(R.id.queue_recycler_view).atPosition(0))
                .check(matches(hasDescendant(withText("STANDARD"))))
                .check(matches(hasDescendant(withText(R.string.first_in_queue_text))));

        onView(withRecyclerView(R.id.queue_recycler_view).atPosition(1))
                .check(matches(hasDescendant(withText("SUV"))))
                .check(matches(hasDescendant(withText(R.string.first_in_queue_text))));

        onView(withRecyclerView(R.id.queue_recycler_view).atPosition(2))
                .check(matches(hasDescendant(withText("PREMIUM"))))
                .check(matches(hasDescendant(withText(R.string.first_in_queue_text))));

        onView(withRecyclerView(R.id.queue_recycler_view).atPosition(3))
                .check(matches(hasDescendant(withText("LUXURY"))))
                .check(matches(hasDescendant(withText(R.string.first_in_queue_text))));


        removeRequests(RequestType.DRIVER_IN_RIDE_AUSTIN_AIRPORT_QUEUE_FIRST);
        mockRequests(RequestType.DRIVER_IN_RIDE_AUSTIN_AIRPORT_QUEUE_SECOND);


        // check categories
        waitFor(allOf(withRecyclerView(R.id.queue_recycler_view).atPosition(0),
                hasDescendant(withText("STANDARD")),
                hasDescendant(withText("2"))), "update", 20000);

        onView(withRecyclerView(R.id.queue_recycler_view).atPosition(1))
                .check(matches(hasDescendant(withText("SUV"))))
                .check(matches(hasDescendant(withText("2"))));

        onView(withRecyclerView(R.id.queue_recycler_view).atPosition(2))
                .check(matches(hasDescendant(withText("PREMIUM"))))
                .check(matches(hasDescendant(withText("2"))));

        onView(withRecyclerView(R.id.queue_recycler_view).atPosition(3))
                .check(matches(hasDescendant(withText("LUXURY"))))
                .check(matches(hasDescendant(withText("2"))));

        removeRequests(RequestType.DRIVER_IN_RIDE_AUSTIN_AIRPORT_QUEUE_SECOND);
        mockRequests(RequestType.DRIVER_IN_RIDE_AUSTIN_AIRPORT_QUEUE_MIXED);


        // check categories
        waitFor(allOf(withRecyclerView(R.id.queue_recycler_view).atPosition(0),
                hasDescendant(withText("STANDARD")),
                hasDescendant(withText(R.string.first_in_queue_text))), "update", 20000);

        onView(withRecyclerView(R.id.queue_recycler_view).atPosition(1))
                .check(matches(hasDescendant(withText("SUV"))))
                .check(matches(hasDescendant(withText("2"))));

        onView(withRecyclerView(R.id.queue_recycler_view).atPosition(2))
                .check(matches(hasDescendant(withText("PREMIUM"))))
                .check(matches(hasDescendant(withText("2"))));

        onView(withRecyclerView(R.id.queue_recycler_view).atPosition(3))
                .check(matches(hasDescendant(withText("LUXURY"))))
                .check(matches(hasDescendant(withText(R.string.first_in_queue_text))));

        // go back
        pressBack();

        // close queue dialog
        closeQueueDialog();

        // open navigation drawer
        onView(withId(R.id.drawerLayout)).perform(DrawerActions.open());
        onView(withId(R.id.navigationView)).perform(NavigationViewActions.navigateTo(R.id.navRide));

        // disable car category
        waitFor("load", 2000);
        onView(allOf(withId(R.id.tv_car_type), withText("STANDARD"))).perform(click());

        // go back
        pressBack();

        // close queue dialog
        closeQueueDialog();

        // open navigation drawer
        onView(withId(R.id.drawerLayout)).perform(DrawerActions.open());
        onView(allOf(withId(R.id.design_menu_item_text), withText("RideAustin Airport"))).perform(click());

        // check categories
        onView(withRecyclerView(R.id.queue_recycler_view).atPosition(0))
                .check(matches(hasDescendant(withText("STANDARD"))))
                .check(matches(hasDescendant(withText(R.string.not_available))));

        onView(withRecyclerView(R.id.queue_recycler_view).atPosition(1))
                .check(matches(hasDescendant(withText("SUV"))))
                .check(matches(hasDescendant(withText("2"))));

        onView(withRecyclerView(R.id.queue_recycler_view).atPosition(2))
                .check(matches(hasDescendant(withText("PREMIUM"))))
                .check(matches(hasDescendant(withText("2"))));

        onView(withRecyclerView(R.id.queue_recycler_view).atPosition(3))
                .check(matches(hasDescendant(withText("LUXURY"))))
                .check(matches(hasDescendant(withText(R.string.first_in_queue_text))));

    }

    private void closeQueueDialog() {
        // not sure why, dialog is not always shown now - maybe bug in app
        if (exists(withText(containsString(getAppContext().getString(R.string.queue_zone_desc_you))))) {
            onView(allOf(withId(R.id.md_buttonDefaultPositive), withText(R.string.btn_ok))).perform(click());
        }
    }

}
