package com.rideaustin.ui.map;

import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.graphics.drawable.Drawable;

import com.rideaustin.App;
import com.rideaustin.R;
import com.rideaustin.api.model.Ride;
import com.rideaustin.ui.common.RxBaseViewModel;
import com.rideaustin.ui.utils.UIUtils;
import com.rideaustin.utils.Constants;
import com.rideaustin.utils.LocalizeUtils;
import com.rideaustin.utils.RxImageLoader;
import com.rideaustin.utils.ViewUtils;

/**
 * Created on 28/03/2018
 *
 * @author sdelaysam
 */
public class PendingAcceptViewModel extends RxBaseViewModel {

    public final ObservableField<Drawable> avatar = new ObservableField<>();
    public final ObservableField<String> startAddress = new ObservableField<>();
    public final ObservableField<String> eta = new ObservableField<>();
    public final ObservableField<String> rating = new ObservableField<>();
    public final ObservableField<String> name = new ObservableField<>();
    public final ObservableInt categoryIcon = new ObservableInt();
    public final ObservableField<String> categoryName = new ObservableField<>();
    public final ObservableField<String> surgeFactor = new ObservableField<>();
    public final ObservableInt categoryFontSize = new ObservableInt();
    public final ObservableInt buttonLeft = new ObservableInt();
    public final ObservableInt buttonRight = new ObservableInt();
    public final ObservableBoolean isFemale = new ObservableBoolean();
    public final ObservableBoolean isSurge = new ObservableBoolean();

    void setRide(Ride ride) {
        if (ride == null) {
            // should never happen
            clear();
            return;
        }

        isFemale.set(ride.isFemaleDriverRequest());
        isSurge.set(ride.getSurgeFactor() > 1);

        eta.set(LocalizeUtils.formatDriverEta(App.getInstance(), ride.getEstimatedTimeArrive()));
        App.getNotificationManager().notifyRideRequest(ride.getRider(), eta.get());
        rating.set(UIUtils.formatRating(ride.getRider().getRating()));
        name.set(ride.getRider().getFirstname());

        String categoryStr = ride.getRequestedCarType().getTitle();
        int fontSize = 20;
        if (ride.isDirectConnectRequest()) {
            categoryIcon.set(R.drawable.nav_direct_connect);
            if (isSurge.get()) {
                categoryStr = App.getInstance().getString(R.string.direct_connect_request_car_type_w_surge, categoryStr);
                fontSize = 14;
            } else {
                categoryStr = App.getInstance().getString(R.string.direct_connect_request_car_type, categoryStr);
                fontSize = 16;
            }
        } else {
            categoryIcon.set(0);
        }
        categoryName.set(categoryStr);
        surgeFactor.set(UIUtils.formatSurgeFactor(ride.getSurgeFactor()));
        categoryFontSize.set(fontSize);

        String url = ride.getRider().getUser().getPhotoUrl();
        untilDestroy(RxImageLoader.execute(new RxImageLoader.Request(url)
                .progress(R.drawable.rotating_circle)
                .target(avatar)
                .error(R.drawable.ic_user_icon)
                .circular(true)));

        if (isFemale.get()) {
            buttonLeft.set(R.drawable.rounded_pink_button_left);
            buttonRight.set(R.drawable.rounded_pink_button_right);
        } else if (isSurge.get()) {
            buttonLeft.set(R.drawable.rounded_blue_button_left);
            buttonRight.set(R.drawable.rounded_blue_button_right);
        } else {
            buttonLeft.set(R.drawable.rounded_green_button_left);
            buttonRight.set(R.drawable.rounded_green_button_right);
        }
    }

    void clear() {
        avatar.set(null);
        eta.set(null);
        rating.set(null);
        name.set(null);
        categoryIcon.set(0);
        categoryName.set(null);
        isFemale.set(false);
        isSurge.set(false);
    }
}
