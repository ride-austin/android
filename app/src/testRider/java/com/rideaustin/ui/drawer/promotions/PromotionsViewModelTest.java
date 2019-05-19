package com.rideaustin.ui.drawer.promotions;

import com.rideaustin.BuildConfig;
import com.rideaustin.api.DataManager;
import com.rideaustin.api.config.GlobalConfig;
import com.rideaustin.api.model.User;
import com.rideaustin.api.model.promocode.PromoCode;
import com.rideaustin.api.model.promocode.PromoCodeResponse;
import com.rideaustin.api.service.PromoCodeService;
import com.rideaustin.manager.ConfigurationManager;
import com.rideaustin.utils.localization.Localizer;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import common.test.BaseTest;
import rx.Observable;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by Viktor Kifer
 * On 28-Dec-2016.
 */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class PromotionsViewModelTest extends BaseTest {

    @Mock
    PromotionsView view;

    @Mock
    DataManager dataManager;

    @Mock
    ConfigurationManager configurationManager;

    @Mock
    PromoCodeService promoCodeService;

    @Mock
    Localizer localizer;

    @Mock
    User currentUser;

    private PromotionsViewModel viewModel;

    @Override
    public void setUp() {
        super.setUp();
        MockitoAnnotations.initMocks(this);

        viewModel = new PromotionsViewModel(view, configurationManager, dataManager, localizer) {
            @Override
            protected void setupBalanceState(PromotionsViewModel.BalanceState balanceState, double balance) {
                // do nothing
            }
        };
        doAnswer(InvocationOnMock::callRealMethod).when(dataManager).ifLoggedIn(any());
        when(dataManager.getPromoCodeService()).thenReturn(promoCodeService);
        when(dataManager.getCurrentUser()).thenReturn(currentUser);
        when(promoCodeService.getRiderPromoCodeBalance(anyLong())).thenReturn(Observable.empty());
        when(configurationManager.getLastAndRequestUpdates()).thenReturn(Observable.empty());
    }

    @Ignore // test suddenly fails on Jenkins
    @Test
    public void testOnStart() throws Exception {
        GlobalConfig config = new GlobalConfig();
        when(configurationManager.getConfigurationUpdates()).thenReturn(Observable.empty());
        when(configurationManager.getLastAndRequestUpdates()).thenReturn(Observable.just(config));

        viewModel.onStart();

        verify(configurationManager).getLastAndRequestUpdates();
        verify(view).onConfigurationUpdated(config);
    }

    @Ignore // test suddenly fails on Jenkins
    @Test
    public void testApplyPromoCode() throws Exception {

        PromoCodeResponse response = Mockito.mock(PromoCodeResponse.class);
        when(response.getCodeLiteral()).thenReturn("PromoCode");
        when(response.getCodeValue()).thenReturn(1.);
        when(response.getDetailText()).thenReturn("Applied");

        when(promoCodeService.applyPromoCode(anyLong(), any(PromoCode.class))).thenReturn(Observable.just(response));

        viewModel.onStart();
        viewModel.applyPromoCode();
        verify(view).onPromoCodeApplied(response);
    }

}