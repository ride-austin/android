package com.rideaustin.ui.payment;

import android.support.annotation.DrawableRes;
import android.text.TextUtils;

import com.devmarvel.creditcardentry.library.CardType;
import com.rideaustin.R;

/**
 * Created by vharshyn on 27.07.2016.
 */
public enum PaymentType {

    MASTERCARD(R.drawable.icn_mastercard),
    VISA(R.drawable.icn_visa),
    DISCOVER(R.drawable.icn_discover),
    AMERICAN_EXPRESS(R.drawable.icn_american_express),
    JCB(R.drawable.icn_jcb),
    DINERS_CLUB(R.drawable.icn_diners_club),
    BEVO_BUCKS(R.drawable.icn_bevobucks_logo),
    UNKNOWN(R.drawable.icn_stripe);

    private final int resId;

    PaymentType(@DrawableRes int resId) {
        this.resId = resId;
    }

    public int getIconResId() {
        return resId;
    }


    @Override
    public String toString() {
        return name();
    }

    public static PaymentType parse(String type) {
        if (TextUtils.isEmpty(type)) {
            return UNKNOWN;
        }
        switch (type) {
            case "MASTERCARD": return MASTERCARD;
            case "VISA": return VISA;
            case "DISCOVER": return DISCOVER;
            case "AMERICAN_EXPRESS": return AMERICAN_EXPRESS;
            case "JCB": return JCB;
            case "DINERS_CLUB": return DINERS_CLUB;
            case "BEVO_BUCKS":
                return BEVO_BUCKS;
            default: return UNKNOWN;
        }
    }

    public CardType getCardType() {
        switch (this) {
            case MASTERCARD: return CardType.MASTERCARD;
            case VISA: return CardType.VISA;
            case DISCOVER: return CardType.DISCOVER;
            case AMERICAN_EXPRESS: return CardType.AMEX;
            case JCB: return CardType.JCB;
            case DINERS_CLUB: return CardType.DINERS;
            default: return CardType.INVALID;
        }
    }


}
