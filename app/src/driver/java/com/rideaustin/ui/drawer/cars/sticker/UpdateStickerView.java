package com.rideaustin.ui.drawer.cars.sticker;

import com.rideaustin.base.BaseActivityCallback;
import com.rideaustin.base.BaseApiException;
import com.rideaustin.ui.common.BaseView;

import java.util.Date;

/**
 * Created by hatak on 2/6/17.
 */

public interface UpdateStickerView extends BaseView {

    void onStickerSelected(String imagePath);

    void onStickerUpdated();

    void onStickerUploadFailed(BaseApiException e);

    void onStickerDownloaded(String insurancePictureUrl);

    void onStickerDownloadFailed();

    void showStickerDate(Date date);

    BaseActivityCallback getCallback();
}
