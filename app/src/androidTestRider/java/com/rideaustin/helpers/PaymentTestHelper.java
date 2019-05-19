package com.rideaustin.helpers;

import com.google.gson.reflect.TypeToken;
import com.rideaustin.MockDelegate;
import com.rideaustin.R;
import com.rideaustin.RequestType;
import com.rideaustin.api.model.Payment;
import com.rideaustin.utils.ViewActionUtils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.RootMatchers.isPlatformPopup;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.rideaustin.utils.Matchers.condition;
import static com.rideaustin.utils.Matchers.waitFor;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.AllOf.allOf;

/**
 * Created by Sergey Petrov on 23/08/2017.
 */

public class PaymentTestHelper {

    public static void selectPayment(MockDelegate delegate, int index, String name) {
        Type cardsType = new TypeToken<ArrayList<Payment>>() {}.getType();
        List<Payment> cards = delegate.getResponse("CARDS_200", cardsType);
        for (int i = 0; i < cards.size(); i++) {
            cards.get(i).setPrimary(i == index);
        }
        delegate.atomicOnRequests(() -> {
            delegate.removeRequests(RequestType.RIDER_CARDS_200_GET,
                    RequestType.RIDER_SELECT_CARD_200_PUT);
            delegate.mockRequest(RequestType.RIDER_CARDS_200_GET, cards);
            delegate.mockRequests(RequestType.RIDER_SELECT_CARD_200_PUT);
        });

        onData(anything()).inAdapterView(withId(R.id.list_payments))
                .atPosition(index)
                .onChildView(withId(R.id.text_payment_card))
                .check(matches(isDisplayed()))
                .check(matches(withText(containsString(name))))
                .perform(click());
    }

    public static void checkPaymentSelected(int index, String name, boolean selected) {
        onData(anything()).inAdapterView(withId(R.id.list_payments))
                .atPosition(index)
                .onChildView(withId(R.id.text_payment_card))
                .check(matches(isDisplayed()))
                .check(matches(withText(containsString(name))));
        onData(anything()).inAdapterView(withId(R.id.list_payments))
                .atPosition(index)
                .onChildView(withId(R.id.icon_payment_primary))
                .check(matches(selected ? isDisplayed() : not(isDisplayed())));

    }

    public static void editPayment(int index) throws InterruptedException {
        onData(anything()).inAdapterView(withId(R.id.list_payments))
                .atPosition(index)
                .onChildView(withId(R.id.btn_payment_edit))
                .check(matches(isDisplayed()))
                .perform(click());

        waitFor(condition("Show payment item menu")
                .withView(onView(withText(R.string.edit)).inRoot(isPlatformPopup())));

        ViewActionUtils.waitFor("", 2000);

        onView(withText(R.string.edit)).perform(click());

    }
}
