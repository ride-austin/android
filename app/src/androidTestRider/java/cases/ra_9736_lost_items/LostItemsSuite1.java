package cases.ra_9736_lost_items;

import android.os.RemoteException;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiObjectNotFoundException;

import com.rideaustin.BaseUITest;
import com.rideaustin.R;
import com.rideaustin.RaActivityRule;
import com.rideaustin.RequestType;
import com.rideaustin.RiderMockResponseFactory;
import com.rideaustin.TestCases;
import com.rideaustin.ui.drawer.NavigationDrawerActivity;
import com.rideaustin.utils.DateHelper;
import com.rideaustin.utils.DeviceTestUtils;
import com.rideaustin.utils.NavigationUtils;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.swipeUp;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.rideaustin.RecyclerViewMatcher.withRecyclerView;
import static com.rideaustin.utils.DeviceTestUtils.restoreFromRecentApps;
import static org.hamcrest.core.AllOf.allOf;

/**
 * Created by crossover on 26/05/2017.
 */
@LargeTest
@RunWith(AndroidJUnit4.class)
public class LostItemsSuite1 extends BaseUITest {

    private static final String LOST_ITEM = "I lost an item";
    private static final String CONTACT_DRIVER = "Contact my driver about a lost item";
    private static final String COULD_NOT_REACH_DRIVER = "I couldn't reach my driver about a lost item";
    private static final String JEEP_WRANGLER = "Jeep Wrangler";
    private static final String PICKUP_ADDRESS = "8972-8998 Shoal Creek Boulevard";
    private static final String DESTINATION_ADDRESS = "6601-6603 Shoal Creek Blvd, Austin";
    private static final String PAYMENT_CARD = "**** **** **** 0005";
    private static final String RIDE_DATE = DateHelper.dateToUiDateTimeAtFormat(new Date(1234567890000L));
    private static final String COST = "$14.00";


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
                RequestType.ACDR_REGULAR_200_GET,
                RequestType.SURGE_AREA_EMPTY_200_GET,
                RequestType.CONFIG_RIDER_200_GET,
                RequestType.EVENTS_EMPTY_200_GET,
                RequestType.CONFIG_ZIPCODES_200_GET,
                RequestType.RIDE_PAYMENT_HISTORY_200_GET,
                RequestType.RIDER_SUPPORT_TOPICS_200_GET,
                RequestType.RIDER_SUPPORT_TOPICS_LOST_ITEM_200_GET);
    }

    @Test
    @TestCases("C1931127")
    public void optionAccessibleFromTripDetails() {
        NavigationUtils.startActivity(activityRule);
        NavigationUtils.throughLogin("austinrider@xo.com", "test123");

        // open navigation drawer
        onView(allOf(withContentDescription(R.string.navigation_drawer_open), isDisplayed())).perform(click());

        // Open menu tap trip history
        onView(allOf(withId(R.id.design_menu_item_text), withText(R.string.history), isDisplayed())).perform(click());

        // Trip history opened and previous rides loaded
        onView(allOf(withId(R.id.toolbarTitle), withText(R.string.trip_history_title))).check(matches(isDisplayed()));
        onView(withRecyclerView(R.id.recyclerTripHistory).atPositionOnView(0, R.id.imageRideMap)).check(matches(isDisplayed()));

        // Tap the trip to report lost item
        onView(allOf(withRecyclerView(R.id.recyclerTripHistory).atPositionOnView(0, R.id.imageRideMap), isDisplayed())).perform(click());

        // Trip details loaded
        // Check Title
        onView(allOf(withId(R.id.toolbarTitle), withText(R.string.trip_history_details_title))).check(matches(isDisplayed()));

        // Ride info is filled
        onView(allOf(withId(R.id.textViewDriversName), withText(JEEP_WRANGLER))).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.textViewPickupAddress), withText(PICKUP_ADDRESS))).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.textViewDestinationAddress), withText(DESTINATION_ADDRESS))).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.textViewPaymentCard), withText(PAYMENT_CARD))).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.textViewDriveDate), withText(RIDE_DATE))).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.textViewDriveCost), withText(COST))).check(matches(isDisplayed()));

        // mapView is shown
        onView(withId(R.id.imageRideMap)).check(matches(isDisplayed()));

        // Scroll up
        onView(allOf(withId(R.id.scrollView), isDisplayed())).perform(swipeUp());

        // validate Lost Items menu shown
        onView(allOf(withId(R.id.textView), withText(LOST_ITEM))).check(matches(isDisplayed()));

        // Under help topics, select lost an item
        onView(allOf(withId(R.id.textView), withText(LOST_ITEM))).perform(click());

        onView(allOf(withId(R.id.toolbarTitle), withText(LOST_ITEM))).check(matches(isDisplayed()));
    }

    @Test
    @TestCases("C1931128")
    public void lookAndFeel() {
        optionAccessibleFromTripDetails();

        onView(allOf(withId(R.id.toolbarTitle), withText(LOST_ITEM))).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.textView), withText(CONTACT_DRIVER))).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.textView), withText(COULD_NOT_REACH_DRIVER))).check(matches(isDisplayed()));
    }

    @Test
    @TestCases("C1931129")
    public void recovery() throws UiObjectNotFoundException, InterruptedException, RemoteException {
        lookAndFeel();
        // Switch put the app in the background and resume
        DeviceTestUtils.pressHome();
        restoreFromRecentApps(2000);

        // Lost items screen opened and the options still accessible
        onView(allOf(withId(R.id.toolbarTitle), withText(LOST_ITEM))).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.textView), withText(CONTACT_DRIVER))).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.textView), withText(COULD_NOT_REACH_DRIVER))).check(matches(isDisplayed()));

        // kill the internet
        setNetworkError(true);
        // Switch put the app in the background
        DeviceTestUtils.pressHome();
        // Disable internet connection then enable after a minute
        restoreFromRecentApps(2000);

        // internet restored
        setNetworkError(false);

        // Lost items screen opened and the options still accessible
        onView(allOf(withId(R.id.toolbarTitle), withText(LOST_ITEM))).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.textView), withText(CONTACT_DRIVER))).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.textView), withText(COULD_NOT_REACH_DRIVER))).check(matches(isDisplayed()));

    }
}
