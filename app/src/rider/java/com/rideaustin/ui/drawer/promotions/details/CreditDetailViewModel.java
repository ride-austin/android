package com.rideaustin.ui.drawer.promotions.details;

import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.text.Spanned;
import android.view.View;

import com.rideaustin.App;
import com.rideaustin.R;
import com.rideaustin.api.model.promocode.PromoCode;
import com.rideaustin.utils.MoneyUtils;
import com.rideaustin.utils.TimeUtils;

import org.joda.time.DateTime;
import org.joda.time.Days;

/**
 * Created by hatak on 04.09.2017.
 */

public class CreditDetailViewModel {

    private static final int VALID_CREDIT_TEXT_COLOR = R.color.teal_A400;
    private static final int VALID_CREDIT_ICON = R.drawable.icn_credits_available;


    public final ObservableField<Drawable> creditIcon = new ObservableField<>();
    public final ObservableField<String> creditCode = new ObservableField<>();
    public final ObservableInt creditCodeColor = new ObservableInt();
    public final ObservableField<String> creditRideQualifier = new ObservableField<>();
    public final ObservableField<Spanned> creditExpiration = new ObservableField<>();
    public final ObservableField<String> creditBalance = new ObservableField<>();
    public final ObservableInt creditExpirationVisibility = new ObservableInt();


    public void setCreditDetail(PromoCode promoCode) {
        int remainingUses = promoCode.getMaximumUses() - promoCode.getTimesUsed();
        promoCode.getExpiresOn().ifPresentOrElse(expirationDate -> {
            int daysLeft = Days.daysBetween(new DateTime(TimeUtils.currentTimeMillis()), new DateTime(expirationDate)).getDays();
            creditExpiration.set(Html.fromHtml(getOfDaysLeft(daysLeft)));
            creditExpirationVisibility.set(View.VISIBLE);
        }, () -> creditExpirationVisibility.set(View.INVISIBLE));

        creditIcon.set(ContextCompat.getDrawable(App.getInstance(), VALID_CREDIT_ICON));
        creditCodeColor.set(ContextCompat.getColor(App.getInstance(), VALID_CREDIT_TEXT_COLOR));

        creditCode.set(promoCode.getCodeLiteral());
        creditRideQualifier.set("For your next " + (remainingUses > 1 ? remainingUses + " rides" : "ride"));

        creditBalance.set(App.i().getString(R.string.money, MoneyUtils.format(promoCode.getRemainingValue())));
    }

    private String getOfDaysLeft(int daysLeft) {
        StringBuilder builder = new StringBuilder();
        if (daysLeft > 1) {
            builder.append("Expires");
            builder.append(" in <b>");
            builder.append(daysLeft);
            builder.append("</b> days");
        } else if (daysLeft == 1) {
            builder.append("Expires");
            builder.append(" in <b>");
            builder.append(daysLeft);
            builder.append("</b> day");
        } else if (daysLeft == 0) {
            builder.append("Expires <b>today</b>");
        } else {
            builder.append("<b>Expired</b>");
        }

        return builder.toString();
    }
}
