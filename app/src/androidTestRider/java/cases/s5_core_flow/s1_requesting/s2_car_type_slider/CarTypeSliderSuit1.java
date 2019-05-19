package cases.s5_core_flow.s1_requesting.s2_car_type_slider;

import android.support.test.espresso.Espresso;
import android.support.test.espresso.ViewInteraction;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiSelector;

import com.google.gson.reflect.TypeToken;
import com.rideaustin.BaseUITest;
import com.rideaustin.R;
import com.rideaustin.RaActivityRule;
import com.rideaustin.RequestType;
import com.rideaustin.RiderMockResponseFactory;
import com.rideaustin.TestCases;
import com.rideaustin.api.config.GlobalConfig;
import com.rideaustin.api.model.driver.RequestedCarType;
import com.rideaustin.ui.drawer.NavigationDrawerActivity;
import com.rideaustin.ui.map.MultipleCarViewModel;
import com.rideaustin.utils.Matchers;
import com.rideaustin.utils.NavigationUtils;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isClickable;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.rideaustin.utils.CarSliderUtils.carTypeSelected;
import static com.rideaustin.utils.CarSliderUtils.carTypeTitle;
import static com.rideaustin.utils.CarSliderUtils.carTypesCount;
import static com.rideaustin.utils.CarSliderUtils.selectCarType;
import static com.rideaustin.utils.MapTestUtil.assertCarMarkersCount;
import static com.rideaustin.utils.MapTestUtil.assertCarMarkersNotVisible;
import static com.rideaustin.utils.MatcherUtils.hasRotation;
import static com.rideaustin.utils.Matchers.condition;
import static com.rideaustin.utils.ViewActionUtils.swipeToOpenBottomSheet;
import static com.rideaustin.utils.ViewActionUtils.waitFor;
import static com.rideaustin.utils.ViewActionUtils.waitForCompletelyDisplayed;
import static com.rideaustin.utils.ViewActionUtils.waitForDisplayed;
import static com.rideaustin.utils.ViewActionUtils.waitForNotEmptyText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.not;

/**
 * Reference: RA-10522
 * Created by Sergey Petrov on 17/05/2017.
 */

@LargeTest
@RunWith(AndroidJUnit4.class)
public class CarTypeSliderSuit1 extends BaseUITest {

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
                RequestType.CAR_TYPES_SLIDER_200_GET,
                RequestType.SURGE_AREA_EMPTY_200_GET,
                RequestType.CONFIG_RIDER_200_GET,
                RequestType.EVENTS_EMPTY_200_GET,
                RequestType.CONFIG_ZIPCODES_200_GET,
                RequestType.LOGOUT_200_POST);
        useCarTypes("CAR_TYPES_SLIDER_200");
    }

    /**
     * C1930582: Rider can select car category before request (STANDARD / SUV / PREMIUM / LUXURY)
     * C1930583: When selecting car category, ETA is updated on [SET PICKUP LOCATION] pin
     * C1930584: When selecting car category, driver cars are updated on the map
     * C1930585: STANDARD car type is selected by default
     */
    @Test
    @TestCases({"C1930582", "C1930583", "C1930584", "C1930585"})
    public void sliderShouldWork() throws UiObjectNotFoundException, InterruptedException {
        removeRequests(RequestType.ACDR_REGULAR_200_GET,
                RequestType.ACDR_REGULAR_MOVED_200_GET,
                RequestType.ACDR_SUV_200_GET,
                RequestType.ACDR_PREMIUM_200_GET,
                RequestType.ACDR_LUXURY_200_GET,
                RequestType.ACDR_TOMMY_200_GET);
        mockRequests(RequestType.ACDR_EMPTY_200_GET);

        NavigationUtils.startActivity(activityRule);

        // login
        NavigationUtils.throughLogin("valid@email.com", "whatever");

        // map is displayed
        onView(withContentDescription(getString(R.string.google_map))).check(matches(isDisplayed()));

        // wait while pickup address is set
        waitForNotEmptyText(R.id.pickup_address);

        // wait for slider and pin loaded
        waitForDisplayed(R.id.set_pickup_location);
        waitForDisplayed(R.id.car_types_slider);

        ViewInteraction pinView = onView(allOf(withId(R.id.set_pickup_location), isDisplayed()));
        ViewInteraction pickupTime = onView(withId(R.id.time_to_pickup));
        ViewInteraction slider = onView(allOf(withId(R.id.car_types_slider), isDisplayed()));

        UiDevice device = UiDevice.getInstance(getInstrumentation());
        UiObject map = device.findObject(new UiSelector().descriptionContains(getString(R.string.google_map)));

        // zoom out to be sure all markers are visible
        // NOTE: can't pinch by 100% - it will trigger navigation drawer
        map.pinchIn(80, 10);
        map.pinchIn(80, 10);
        if (device.getDisplaySizeDp().x < 360) {
            // zoom out again for small screens
            map.pinchIn(80, 10);
        }

        // check slider has 5 car types (CAR_TYPES_SLIDER_200_GET)
        slider.check(matches(carTypesCount(5)));

        // check slider has STANDARD selected by default
        slider.check(matches(carTypeSelected(0)));
        slider.check(matches(carTypeTitle("STANDARD")));

        // pin view shows no available cars (ACDR_EMPTY_200_GET)
        pinView.check(matches(withText(R.string.no_available_cars)));
        pickupTime.check(matches(not(isDisplayed())));

        // check no car markers
        assertCarMarkersNotVisible();

        removeRequests(RequestType.ACDR_EMPTY_200_GET);
        mockRequests(RequestType.ACDR_SUV_200_GET);

        // select SUV category
        slider.perform(selectCarType(1));

        // check slider has SUV selected
        slider.check(matches(carTypeSelected(1)));
        slider.check(matches(carTypeTitle("SUV")));

        // pin view shows correct ETA from ACDR_SUV_200_GET
        pinView.check(matches(withText(R.string.set_pickup_location)));
        pickupTime.check(matches(allOf(withText("3"), isDisplayed())));

        // check car markers
        assertCarMarkersCount(3);

        removeRequests(RequestType.ACDR_SUV_200_GET);
        mockRequests(RequestType.ACDR_PREMIUM_200_GET);

        // select PREMIUM category
        slider.perform(selectCarType(2));

        // check slider has PREMIUM selected
        slider.check(matches(carTypeSelected(2)));
        slider.check(matches(carTypeTitle("PREMIUM")));

        // pin view shows correct ETA from ACDR_PREMIUM_200_GET
        pinView.check(matches(withText(R.string.set_pickup_location)));
        pickupTime.check(matches(allOf(withText("4"), isDisplayed())));

        // check car markers
        assertCarMarkersCount(3);

        removeRequests(RequestType.ACDR_PREMIUM_200_GET);
        mockRequests(RequestType.ACDR_LUXURY_200_GET);

        // select LUXURY category
        slider.perform(selectCarType(3));

        // check slider has LUXURY selected
        slider.check(matches(carTypeSelected(3)));
        slider.check(matches(carTypeTitle("LUXURY")));

        // pin view shows correct ETA from ACDR_LUXURY_200_GET
        pinView.check(matches(withText(R.string.set_pickup_location)));
        pickupTime.check(matches(allOf(withText("5"), isDisplayed())));

        // check car markers
        assertCarMarkersCount(1);

        removeRequests(RequestType.ACDR_LUXURY_200_GET);
        mockRequests(RequestType.ACDR_TOMMY_200_GET);

        // select TOMMY category
        slider.perform(selectCarType(4));

        // check slider has TOMMY selected
        slider.check(matches(carTypeSelected(4)));
        slider.check(matches(carTypeTitle("TOMMY")));

        // pin view shows correct ETA from ACDR_TOMMY_200_GET
        pinView.check(matches(withText(R.string.set_pickup_location)));
        pickupTime.check(matches(allOf(withText("25"), isDisplayed())));

        // check car markers
        assertCarMarkersCount(3);

        removeRequests(RequestType.ACDR_TOMMY_200_GET);
        mockRequests(RequestType.ACDR_REGULAR_200_GET);

        // select STANDARD category
        slider.perform(selectCarType(0));

        // check slider has STANDARD selected
        Matchers.waitFor(condition().withMatcher(carTypeSelected(0)));
        slider.check(matches(carTypeTitle("STANDARD")));

        // pin view shows correct ETA from ACDR_REGULAR_200_GET
        pinView.check(matches(withText(R.string.set_pickup_location)));
        pickupTime.check(matches(allOf(withText("2"), isDisplayed())));

        // check car markers
        assertCarMarkersCount(2);

    }

    /**
     * C1930586: Switching categories changes ETA and MAX SIZE
     * C1930587: Correct pricing details for each car category
     * C1930588: A '^' shown as a slide up indicator above the car categories
     */
    @Test
    @TestCases({"C1930586", "C1930587", "C1930588"})
    public void sliderDataShouldChangeOnCategorySelect() {
        removeRequests(RequestType.ACDR_REGULAR_200_GET,
                RequestType.ACDR_REGULAR_MOVED_200_GET,
                RequestType.ACDR_SUV_200_GET,
                RequestType.ACDR_PREMIUM_200_GET,
                RequestType.ACDR_LUXURY_200_GET,
                RequestType.ACDR_TOMMY_200_GET);
        mockRequests(RequestType.ACDR_EMPTY_200_GET,
                RequestType.SPECIALFEES_EMPTY_200_GET);

        NavigationUtils.startActivity(activityRule);

        // login
        NavigationUtils.throughLogin("valid@email.com", "whatever");

        // map is displayed
        onView(withContentDescription(getString(R.string.google_map))).check(matches(isDisplayed()));

        // wait while pickup address is set
        waitForNotEmptyText(R.id.pickup_address);

        // wait for slider and pin loaded
        waitForDisplayed(R.id.set_pickup_location);
        waitForDisplayed(R.id.car_types_slider);

        ViewInteraction slider = onView(allOf(withId(R.id.car_types_slider), isDisplayed()));
        ViewInteraction etaValue = onView(allOf(withId(R.id.eta_value), isDisplayed()));
        ViewInteraction maxSizeValue = onView(allOf(withId(R.id.max_size_value), isDisplayed()));

        // arrow is in closed state
        onView(allOf(withId(R.id.mark_image), isDisplayed())).check(matches(hasRotation(180)));

        // open slider details
        onView(allOf(withId(R.id.mark_image), isDisplayed())).perform(swipeToOpenBottomSheet());

        // wait for details are completely open
        waitForCompletelyDisplayed(R.id.main_content);

        // arrow is in opened state
        onView(allOf(withId(R.id.mark_image), isDisplayed())).check(matches(hasRotation(0)));

        // check slider has STANDARD selected
        slider.check(matches(carTypeSelected(0)));
        slider.check(matches(carTypeTitle("STANDARD")));

        // check ETA is empty according to ACDR_EMPTY_200_GET
        etaValue.check(matches(withText(MultipleCarViewModel.EMPTY_ETA)));

        // check MAX_PEOPLE according to CAR_TYPES_SLIDER_200_GET
        maxSizeValue.check(matches(withText("1 PEOPLE")));

        // check pricing details
        checkPricingDetails("STANDARD", 1, false);

        removeRequests(RequestType.ACDR_EMPTY_200_GET);
        mockRequests(RequestType.ACDR_SUV_200_GET);

        // select SUV category
        slider.perform(selectCarType(1));

        // check slider has SUV selected
        slider.check(matches(carTypeTitle("SUV")));

        // check ETA according to ACDR_SUV_200_GET
        etaValue.check(matches(withText("3 MINS")));

        // check MAX_PEOPLE according to CAR_TYPES_SLIDER_200_GET
        maxSizeValue.check(matches(withText("2 PEOPLE")));

        // check pricing details
        checkPricingDetails("SUV", 2, false);

        removeRequests(RequestType.ACDR_SUV_200_GET);
        mockRequests(RequestType.ACDR_PREMIUM_200_GET);

        // select PREMIUM category
        slider.perform(selectCarType(2));

        // check slider has PREMIUM selected
        slider.check(matches(carTypeTitle("PREMIUM")));

        // check ETA according to ACDR_PREMIUM_200_GET
        etaValue.check(matches(withText("4 MINS")));

        // check MAX_PEOPLE according to CAR_TYPES_SLIDER_200_GET
        maxSizeValue.check(matches(withText("3 PEOPLE")));

        // check pricing details
        checkPricingDetails("PREMIUM", 3, false);

        removeRequests(RequestType.ACDR_PREMIUM_200_GET);
        mockRequests(RequestType.ACDR_LUXURY_200_GET);

        // select LUXURY category
        slider.perform(selectCarType(3));

        // check slider has LUXURY selected
        slider.check(matches(carTypeTitle("LUXURY")));

        // check ETA according to ACDR_LUXURY_200_GET
        etaValue.check(matches(withText("5 MINS")));

        // check MAX_PEOPLE according to CAR_TYPES_SLIDER_200_GET
        maxSizeValue.check(matches(withText("4 PEOPLE")));

        // check pricing details
        checkPricingDetails("LUXURY", 4, false);

        removeRequests(RequestType.ACDR_LUXURY_200_GET,
                RequestType.SPECIALFEES_EMPTY_200_GET);
        mockRequests(RequestType.ACDR_TOMMY_200_GET,
                RequestType.SPECIALFEES_200_GET);

        // select TOMMY category
        slider.perform(selectCarType(4));

        // check slider has TOMMY selected
        slider.check(matches(carTypeTitle("TOMMY")));

        // check ETA according to ACDR_TOMMY_200_GET
        etaValue.check(matches(withText("25 MINS")));

        // check MAX_PEOPLE according to CAR_TYPES_SLIDER_200_GET
        maxSizeValue.check(matches(withText("5 PEOPLE")));

        // check pricing details
        checkPricingDetails("TOMMY", 5, true);

        removeRequests(RequestType.ACDR_TOMMY_200_GET);
        mockRequests(RequestType.ACDR_REGULAR_200_GET);

        // select STANDARD category
        slider.perform(selectCarType(0));

        // check slider has STANDARD selected
        slider.check(matches(carTypeTitle("STANDARD")));

        // check ETA according to ACDR_REGULAR_200_GET
        etaValue.check(matches(withText("2 MINS")));

        // check MAX_PEOPLE according to CAR_TYPES_SLIDER_200_GET
        maxSizeValue.check(matches(withText("1 PEOPLE")));

        // check pricing details
        checkPricingDetails("STANDARD", 1, true);

    }

    /**
     * Able to slide up the car categories to reveal more actions
     * Verify tapping ^ will expand car slider panel to show:
     * - ETA
     * - Max People
     * - View Pricing
     * - Get Fare Estimate by selecting a destination
     */
    @Test
    @TestCases("C1930589")
    public void checkHiddenActions() {
        removeRequests(RequestType.ACDR_REGULAR_200_GET,
                RequestType.ACDR_REGULAR_MOVED_200_GET,
                RequestType.ACDR_SUV_200_GET,
                RequestType.ACDR_PREMIUM_200_GET,
                RequestType.ACDR_LUXURY_200_GET,
                RequestType.ACDR_TOMMY_200_GET);
        mockRequests(RequestType.ACDR_EMPTY_200_GET);

        NavigationUtils.startActivity(activityRule);

        // login
        NavigationUtils.throughLogin("valid@email.com", "whatever");

        // map is displayed
        onView(withContentDescription(getString(R.string.google_map))).check(matches(isDisplayed()));

        // wait while pickup address is set
        waitForNotEmptyText(R.id.pickup_address);

        // wait for slider and pin loaded
        waitForDisplayed(R.id.set_pickup_location);
        waitForDisplayed(R.id.car_types_slider);

        // arrow is in closed state
        onView(allOf(withId(R.id.mark_image), isDisplayed())).check(matches(hasRotation(180)));

        // open slider details
        onView(allOf(withId(R.id.mark_image), isDisplayed())).perform(swipeToOpenBottomSheet());

        // wait for details are completely open
        waitForCompletelyDisplayed(R.id.main_content);

        // arrow is in opened state
        onView(allOf(withId(R.id.mark_image), isDisplayed())).check(matches(hasRotation(0)));

        // check ETA title
        onView(allOf(withId(R.id.eta_title), isDisplayed())).check(matches(withText(R.string.slider_label_eta)));

        // check ETA is empty according to ACDR_EMPTY_200_GET
        onView(allOf(withId(R.id.eta_value), isDisplayed())).check(matches(withText(MultipleCarViewModel.EMPTY_ETA)));

        // check MAX_PEOPLE title
        onView(allOf(withId(R.id.people_title), isDisplayed())).check(matches(withText(R.string.slider_label_people)));

        // check MAX_PEOPLE according to CAR_TYPES_SLIDER_200_GET
        onView(allOf(withId(R.id.max_size_value), isDisplayed())).check(matches(withText("1 PEOPLE")));

        // check view price text
        onView(allOf(withId(R.id.view_costs_text), isDisplayed(), isClickable())).check(matches(withText(R.string.slider_label_pricing)));

        // check fare estimate text
        onView(allOf(withId(R.id.fare_estimate_text), isDisplayed(), isClickable())).check(matches(withText(R.string.slider_label_estimate)));
    }

    private void checkPricingDetails(String title, int index, boolean hasSpecialFees) {
        // open dialog
        onView(allOf(withId(R.id.view_costs_text), isDisplayed())).perform(click());

        // check title
        onView(allOf(withId(R.id.title_text), isDisplayed())).check(matches(withText(R.string.pricing_details)));
        onView(allOf(withId(R.id.selected_category_text), isDisplayed())).check(matches(withText(title)));

        // check data
        onView(allOf(withId(R.id.base_fare_value), isDisplayed())).check(matches(withText("$ " + index + ".00")));
        onView(allOf(withId(R.id.per_mile_value), isDisplayed())).check(matches(withText("$ " + index + ".00")));
        onView(allOf(withId(R.id.per_min_value), isDisplayed())).check(matches(withText("$ " + index + ".00")));
        onView(allOf(withId(R.id.min_value), isDisplayed())).check(matches(withText("$ " + index + ".00")));
        onView(allOf(withId(R.id.booking_fee_value), isDisplayed())).check(matches(withText("$ " + index + ".00")));
        onView(allOf(withId(R.id.tnc_fee_value), isDisplayed())).check(matches(withText(index + "%")));

        if (hasSpecialFees) {
            // according to SPECIALFEES_200
            onView(allOf(withId(R.id.fee_value), withText("$ 1.00"))).check(matches(isDisplayed()));
            onView(allOf(withId(R.id.fee_text), withText("PICKUP SURCHARGE"))).check(matches(isDisplayed()));
            onView(allOf(withId(R.id.fee_description), withText(""))).check(matches(not(isDisplayed())));

            onView(allOf(withId(R.id.fee_value), withText("$ 2.25"))).check(matches(isDisplayed()));
            onView(allOf(withId(R.id.fee_text), withText("ANOTHER SURCHARGE"))).check(matches(isDisplayed()));
            onView(allOf(withId(R.id.fee_description), withText("DESCRIPTION"))).check(matches(isDisplayed()));
        } else {
            onView(withId(R.id.fee_value)).check(doesNotExist());
            onView(withId(R.id.fee_description)).check(doesNotExist());
            onView(withId(R.id.fee_text)).check(doesNotExist());
        }

        // close dialog
        Espresso.pressBack();
    }

    private void useCarTypes(String resourceName) {
        Type token = new TypeToken<ArrayList<RequestedCarType>>() {}.getType();
        List<RequestedCarType> carTypes = getResponse(resourceName, token);
        GlobalConfig config = getResponse("CONFIG_GLOBAL_200", GlobalConfig.class);
        config.setCarTypes(carTypes);
        removeRequests(RequestType.CONFIG_RIDER_200_GET);
        mockRequest(RequestType.CONFIG_RIDER_200_GET, config);
    }
}
