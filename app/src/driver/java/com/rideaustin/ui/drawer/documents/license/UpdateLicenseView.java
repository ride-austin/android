package com.rideaustin.ui.drawer.documents.license;

import com.rideaustin.base.BaseActivityCallback;
import com.rideaustin.base.BaseApiException;
import com.rideaustin.ui.common.BaseView;

import java.util.Date;

/**
 * Created by crossover on 02/02/2017.
 */

public interface UpdateLicenseView extends BaseView {

    void onLicenseSelected(String imagePath);

    void onLicenseUpdated();

    void onLicenseUploadFailed(BaseApiException e);

    void onLicenseDownloaded(String licensePictureUrl);

    void showLicenseDate(Date date);

    void onLicenseDownloadFailed();

    BaseActivityCallback getCallback();
}
