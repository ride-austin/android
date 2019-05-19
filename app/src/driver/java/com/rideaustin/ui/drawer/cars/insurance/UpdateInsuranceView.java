package com.rideaustin.ui.drawer.cars.insurance;

import com.rideaustin.base.BaseActivityCallback;
import com.rideaustin.base.BaseApiException;
import com.rideaustin.ui.common.BaseView;

import java.util.Date;

/**
 * Created by crossover on 02/02/2017.
 */

public interface UpdateInsuranceView extends BaseView {

    void onInsuranceSelected(String imagePath);

    void onInsuranceUpdated();

    void onInsuranceUploadFailed(BaseApiException e);

    void onInsuranceDownloaded(String insurancePictureUrl);

    void showInsuranceDate(Date date);

    void onInsuranceDownloadFailed();

    BaseActivityCallback getCallback();
}
