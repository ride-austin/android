package com.rideaustin.ui.drawer.triphistory.forms;

import android.databinding.Bindable;
import android.support.annotation.Nullable;
import android.view.View;

import com.rideaustin.App;
import com.rideaustin.BR;
import com.rideaustin.R;
import com.rideaustin.api.DataManager;
import com.rideaustin.api.model.ServerMessage;
import com.rideaustin.api.model.SupportField;
import com.rideaustin.api.model.SupportForm;
import com.rideaustin.base.ApiSubscriber2;
import com.rideaustin.base.BaseActivityCallback;
import com.rideaustin.base.BaseApiException;
import com.rideaustin.schedulers.RxSchedulers;
import com.rideaustin.ui.common.RxBaseObservable;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import rx.Observable;
import timber.log.Timber;

/**
 * Created by crossover on 24/05/2017.
 */

public class SupportFormViewModel extends RxBaseObservable implements DirtyListener {

    private final SupportFormView supportFormView;
    private final long rideId;
    private final int topicId;

    private SupportForm supportForm;
    private final List<SupportFieldViewModel> fieldViewModels = new ArrayList<>();

    private static final String CONTACT_PAIR = "lostandfound/contact";
    private static final String POST_LOST_ITEM = "lostandfound/lost";
    private static final String POST_FOUND_ITEM = "lostandfound/found";

    private boolean isActionEnabled = false;

    public SupportFormViewModel(SupportFormView supportFormView, long rideId, int topicId) {
        this.supportFormView = supportFormView;
        this.rideId = rideId;
        this.topicId = topicId;
    }

    protected long getRideId() {
        return rideId;
    }

    @Bindable
    public SupportForm getSupportForm() {
        return supportForm;
    }

    public void setSupportForm(SupportForm supportForm) {
        this.supportForm = supportForm;
        notifyPropertyChanged(BR.supportForm);
    }

    @Bindable
    public boolean getActionEnabled() {
        return isActionEnabled;
    }

    public void setActionEnabled(boolean enable) {
        isActionEnabled = enable;
        notifyPropertyChanged(BR.actionEnabled);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!App.getDataManager().isLoggedIn()) {
            // app would be sent to login on resume
            return;
        }
    }

    void setupForm() {
        App.getDataManager().ifLoggedIn(user -> {
            if (App.getDataManager().getSupportFormsModel() == null) {
                // unexpected state
                supportFormView.onUnexpectedState();
            } else {
                App.getDataManager().getSupportFormsModel().getFormByTopicId(topicId).ifPresentOrElse(supportForm -> {
                    setSupportForm(supportForm);
                    if (supportForm.getSupportFields() != null) {
                        supportFormView.onSupportFields(supportForm, supportForm.getSupportFields());
                    }
                }, () -> supportFormView.onError());
            }
        });
    }

    public final void onActionClicked(View view) {
        if (!verifyParameters()) {
            return;
        }
        view.setEnabled(false);
        addSubscription(postFormObservable()
                .subscribeOn(RxSchedulers.network())
                .observeOn(RxSchedulers.main())
                .subscribe(new ApiSubscriber2<ServerMessage>(supportFormView.getCallback()) {

                    @Override
                    public void onNext(ServerMessage response) {
                        view.setEnabled(true);
                        supportFormView.onActionClicked(response.getMessage());
                    }

                    @Override
                    public void onAnyError(BaseApiException e) {
                        view.setEnabled(true);
                    }
                }));
    }

    private Observable<ServerMessage> postFormObservable() {
        switch (supportForm.getActionType()) {
            case CONTACT_PAIR:
                return App.getDataManager().getSupportService().contactPair(rideId, getParamsStringMap());
            case POST_LOST_ITEM:
                return App.getDataManager().getSupportService().postLostItem(rideId, getParamsStringMap());
            case POST_FOUND_ITEM:
                try {
                    return App.getDataManager().getSupportService().postFoundItem(getParamsRequestBodyMap(rideId));
                } catch (JSONException e) {
                    Timber.e(e, "Error while creating request params for found items");
                    return Observable.error(new IllegalStateException(App.getInstance().getString(R.string.error_unknown)));
                }
            default:
                return Observable.error(new UnsupportedOperationException("This feature has not been implemented yet."));
        }
    }

    private Map<String, String> getParamsStringMap() {
        HashMap<String, String> params = new HashMap<>();
        for (SupportFieldViewModel value : fieldViewModels) {
            SupportField field = value.getSupportField();
            switch (field.getFieldType()) {
                case SupportFieldViewModel.FieldType.BOOL:
                    params.put(field.getVariable(), Boolean.toString(value.getBooleanInput()));
                    break;
                case SupportFieldViewModel.FieldType.PHONE:
                case SupportFieldViewModel.FieldType.TEXT:
                case SupportFieldViewModel.FieldType.DATE:
                    params.put(field.getVariable(), value.getStringInput());
                    break;
                default:
                    throw new IllegalArgumentException("Following type is not supported: " + field.getFieldType());
            }
        }
        return params;
    }

    private Map<String, RequestBody> getParamsRequestBodyMap(long rideId) throws JSONException {
        HashMap<String, RequestBody> params = new HashMap<>();
        JSONObject item = new JSONObject();
        item.put("rideId", rideId);
        for (SupportFieldViewModel value : fieldViewModels) {
            SupportField field = value.getSupportField();
            switch (field.getFieldType()) {
                case SupportFieldViewModel.FieldType.BOOL:
                    item.put(field.getVariable(), value.getBooleanInput());
                    break;
                case SupportFieldViewModel.FieldType.PHONE:
                case SupportFieldViewModel.FieldType.TEXT:
                case SupportFieldViewModel.FieldType.DATE:
                    item.put(field.getVariable(), value.getStringInput());
                    break;
                case SupportFieldViewModel.FieldType.PHOTO:
                    File photo = new File(value.getPhotoPath());
                    photo.deleteOnExit();
                    params.put(DataManager.toParam(field.getVariable(), photo.getName()), RequestBody.create(MediaType.parse("image/png"), photo));
                    break;
                default:
                    throw new IllegalArgumentException("Following type is not supported: " + field.getFieldType());
            }
        }
        params.put("item", RequestBody.create(MediaType.parse("application/json"), item.toString()));
        return params;
    }

    private boolean verifyParameters() {
        for (SupportFieldViewModel value : fieldViewModels) {
            if (!value.verify()) {
                return false;
            }
        }
        return true;
    }

    @Nullable
    public String getTitle() {
        if (App.getDataManager().getSupportTopicsModel() != null) {
            return App.getDataManager().getSupportFormsModel().getFormByTopicId(topicId).map(SupportForm::getHeaderText).orElse(null);
        }
        return null;
    }

    public void clearFieldViewModels() {
        fieldViewModels.clear();
    }

    public void putFieldViewModel(SupportFieldViewModel fieldViewModel) {
        fieldViewModels.add(fieldViewModel);
        fieldViewModel.setDirtyListener(this);
    }

    @Override
    public void onDirtyChange() {
        setActionEnabled(true);
    }

    /**
     * Created by crossover on 24/05/2017.
     */

    public interface SupportFormView {
        void onUnexpectedState();

        void onActionClicked(String message);

        BaseActivityCallback getCallback();

        void onError();

        void onSupportFields(SupportForm supportForm, List<SupportField> supportFields);
    }
}
