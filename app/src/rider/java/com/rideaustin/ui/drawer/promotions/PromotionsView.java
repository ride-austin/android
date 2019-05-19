package com.rideaustin.ui.drawer.promotions;

import com.rideaustin.api.config.GlobalConfig;
import com.rideaustin.api.model.promocode.PromoCodeResponse;
import com.rideaustin.base.BaseActivityCallback;
import com.rideaustin.ui.common.BaseView;

/**
 * Created by Viktor Kifer
 * On 28-Dec-2016.
 */

public interface PromotionsView extends BaseView {

    /**
     * Called when application global configuration was updated
     *
     * @param config - new global configuration
     */
    void onConfigurationUpdated(GlobalConfig config);

    /**
     * Called when promo code successfully applied
     *
     * @param promoCode - promo code response
     */
    void onPromoCodeApplied(PromoCodeResponse promoCode);

    /**
     * Called when promo code verification failed
     *
     * @param literal - promo code litera
     * @param message - error message
     */
    void showInvalidRiderStateDialog(String literal, String message);

    BaseActivityCallback getCallback();

}
