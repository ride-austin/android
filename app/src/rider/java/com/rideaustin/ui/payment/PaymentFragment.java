package com.rideaustin.ui.payment;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.rideaustin.App;
import com.rideaustin.R;
import com.rideaustin.api.config.UnpaidConfig;
import com.rideaustin.api.config.ut.UT;
import com.rideaustin.api.model.Payment;
import com.rideaustin.api.model.UnpaidBalance;
import com.rideaustin.base.ApiSubscriber;
import com.rideaustin.base.ApiSubscriber2;
import com.rideaustin.base.BaseApiException;
import com.rideaustin.base.BaseFragment;
import com.rideaustin.base.Transition;
import com.rideaustin.databinding.FragmentPaymentBinding;
import com.rideaustin.schedulers.RxSchedulers;
import com.rideaustin.ui.drawer.promotions.FreeRidesActivity;
import com.rideaustin.ui.utils.MaterialDialogCreator;
import com.rideaustin.ui.utils.infodialog.InfoDialog;
import com.rideaustin.utils.UnpaidHelper;
import com.rideaustin.utils.toast.RAToast;

import java.util.List;

import java8.util.Optional;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

import static com.rideaustin.App.getDataManager;

/**
 * Created by v.garshyn on 09.07.16.
 */
public class PaymentFragment extends BaseFragment implements PaymentListAdapter.PaymentInteractionListener {

    private Payment payment;
    private EventListener eventListener;
    private FragmentPaymentBinding binding;
    private PaymentListAdapter adapter;
    private CompositeSubscription compositeSubscription = new CompositeSubscription();

    public void setPayment(Payment payment) {
        this.payment = payment;
        if (isResumed()) {
            updatePayments();
        }
    }

    public void setEventListener(EventListener eventListener) {
        this.eventListener = eventListener;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setToolbarTitleAligned(R.string.title_payment, Gravity.LEFT);
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_payment, container, false);
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        compositeSubscription.clear();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        adapter = new PaymentListAdapter(this);
        binding.listPayments.setAdapter(adapter);
        binding.listPayments.setOnItemClickListener(adapter);
        binding.addPayment.setOnClickListener(v -> doPaymentAdd());

        boolean referralEnabled = App.getConfigurationManager().getLastConfiguration().getRiderReferFriend().isEnabled();
        binding.inviteFriends.setVisibility(referralEnabled ? View.VISIBLE : View.GONE);
        if (referralEnabled) {
            binding.inviteFriends.setOnClickListener(v -> onInviteFriendsClicked());
        }
        binding.unpaidItem.setOnClickListener(v -> onUnpaidClicked());
        subscribeToUnpaid();
    }

    private void onUnpaidClicked() {
        getCallback().navigateTo(R.id.navUnpaid);
    }

    private void onInviteFriendsClicked() {
        startActivity(new Intent(getActivity(), FreeRidesActivity.class));
    }

    public void onResume() {
        super.onResume();
        updatePayments();
    }

    private void updatePayments() {
        adapter.updatePaymentsData(getDataManager().getUserPaymentMethods());
        onNewCardAdded(payment);
    }

    private void subscribeToUnpaid() {
        if (isUnpaidEnabled()) {
            App.getDataManager().requestUnpaid();
            compositeSubscription.add(App.getDataManager()
                    .getUnpaidBalanceObservable()
                    .observeOn(RxSchedulers.main())
                    .subscribe(this::updateUnpaid, Timber::e));
        }
    }

    private void updateUnpaid(Optional<UnpaidBalance> optional) {
        boolean showUnpaid = UnpaidHelper.isUnpaid(optional);
        binding.setShowUnpaid(showUnpaid);
        if (showUnpaid) {
            Optional<UnpaidConfig> config = Optional.ofNullable(App.getConfigurationManager().getLastConfiguration().getUnpaidConfig());
            binding.unpaidTitle.setText(config.map(UnpaidConfig::getTitle).orElse(getString(R.string.unpaid_title)));
            binding.unpaidSubTitle.setText(config.map(UnpaidConfig::getSubTitle).orElse(getString(R.string.unpaid_subtitle)));
            String amount = App.getDataManager().getUnpaidBalance().map(UnpaidBalance::getAmount).orElse("0");
            binding.unpaidAmount.setText(App.getInstance().getString(R.string.money, amount));
        }
    }

    private boolean isUnpaidEnabled() {
        return Optional.ofNullable(App.getConfigurationManager().getLastConfiguration().getUnpaidConfig())
                .map(UnpaidConfig::isEnabled).orElse(false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.payment_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menuAddPayment) {
            doPaymentAdd();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPaymentEdit(Payment payment, View anchorView) {
        PopupMenu menu = new PopupMenu(getContext(), anchorView);
        menu.inflate(R.menu.payment_edit_menu);
        menu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.edit:
                    doPaymentEdit(payment);
                    break;
                case R.id.delete:
                    if (!payment.isPrimary()) {
                        showDeleteConfirmationDialog(payment);
                    } else {
                        RAToast.show(R.string.payment_delete_error, Toast.LENGTH_SHORT);
                    }
                    break;
            }
            return true;
        });
        menu.show();
    }

    @Override
    public void onPaymentClick(Payment payment) {
        if (!payment.isLocalPrimary()) {
            setCardPrimaryInternal(payment);
        }
    }

    @Override
    public void onPaymentInfo(Payment payment) {
        Optional.ofNullable(App.getConfigurationManager().getLastConfiguration().getUt())
                .map(UT::getPayWithBevoBucks)
                .ifPresent(payWithBevoBucks -> {
                    InfoDialog.create(R.drawable.icn_bevobucks_logo, getString(R.string.pay_with_bevobucks),
                            payWithBevoBucks.getDescription())
                            .show(getFragmentManager());
                });
    }

    private void setCardPrimaryInternal(Payment payment) {
        compositeSubscription.clear();
        compositeSubscription.add(App.getDataManager()
                .setCardPrimary(payment)
                .subscribeOn(RxSchedulers.network())
                .observeOn(RxSchedulers.main())
                .subscribe(new ApiSubscriber2<List<Payment>>(getCallback()) {
                    @Override
                    public void onNext(List<Payment> payments) {
                        adapter.updatePaymentsData(payments);
                    }
                }));
    }


    private void doPaymentAdd() {
        if (eventListener != null) {
            eventListener.onAddPayment();
        }
    }

    private void doPaymentEdit(Payment payment) {
        if (eventListener != null) {
            eventListener.onEditPayment(payment);
        }
    }

    private void processPaymentDelete(Payment payment) {
        getDataManager()
                .deletePayment(payment.getId())
                .observeOn(RxSchedulers.main())
                .subscribe(new ApiSubscriber<List<Payment>>(getCallback()) {
                    @Override
                    public void onNext(List<Payment> payments) {
                        if (adapter != null) {
                            adapter.updatePaymentsData(payments);
                        }
                    }

                    @Override
                    public void onError(BaseApiException e) {
                        super.onError(e);
                        RAToast.show(e.getMessage(), Toast.LENGTH_LONG);
                    }
                });
    }

    private void showDeleteConfirmationDialog(final Payment payment) {
        MaterialDialog.Builder confirmation = MaterialDialogCreator.createConfirmDeletePaymentDialog(getString(R.string.payment_delete_confirm), (AppCompatActivity) getActivity());
        confirmation
                .onPositive((dialog, which) -> processPaymentDelete(payment))
                .onNegative((dialog, which) -> dialog.hide())
                .build();
        confirmation.show();
    }


    private void onNewCardAdded(final Payment payment) {
        if (payment != null) {
            List<Payment> userPaymentMethods = getDataManager().getUserPaymentMethods();
            if (userPaymentMethods.size() > 1) {
                MaterialDialogCreator.createSimpleConfirmDialog(getString(R.string.make_credit_card_primary), getActivity())
                        .title(getString(R.string.make_credit_card_title))
                        .autoDismiss(true)
                        .onPositive((dialog, which) -> setCardPrimaryInternal(payment))
                        .show();
            } else {
                setCardPrimaryInternal(payment);
            }
            this.payment = null;
        }
    }

    public interface EventListener {
        void onAddPayment();
        void onEditPayment(Payment payment);
    }
}
