package com.rideaustin.ui.drawer.cars.photos;

import com.rideaustin.models.CarPhoto;
import com.rideaustin.ui.common.BaseView;

/**
 * Created by crossover on 08/02/2017.
 */

public interface UpdatePhotoView extends BaseView {
    void onPhotoSelected(String filePath);

    void onPhotoUploaded(CarPhoto carPhoto);

    void onPhotoUploadFailed();
}
