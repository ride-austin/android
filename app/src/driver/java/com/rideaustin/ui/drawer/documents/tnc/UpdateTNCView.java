package com.rideaustin.ui.drawer.documents.tnc;

import android.content.Context;

import com.rideaustin.api.model.Document;
import com.rideaustin.base.BaseActivityCallback;
import com.rideaustin.ui.common.BaseView;
import com.rideaustin.ui.common.TakePhotoFragment;

/**
 * Created by crossover on 22/01/2017.
 */

public interface UpdateTNCView extends BaseView {
    void onPhotoTaken(TakePhotoFragment.Source source, String filePath);

    void onPhotoCanceled();

    void onTncCardsCombined();

    void onTncCardCombineFailed(String localizedMessage);

    Context getContext();

    void onDocumentUploaded();

    void onDocumentDownloaded(Document document);

    void onDocumentDownloadFailed();

    BaseActivityCallback getCallback();
}
