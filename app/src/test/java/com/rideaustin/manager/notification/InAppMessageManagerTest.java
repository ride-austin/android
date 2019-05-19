package com.rideaustin.manager.notification;

import com.rideaustin.manager.AppNotificationManager;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import rx.observers.TestSubscriber;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Created by Sergey Petrov on 03/03/2017.
 */

public class InAppMessageManagerTest {

    private AppNotificationManager notificationManager;
    private InAppMessageManager messageManager;
    private ArgumentCaptor<Integer> messageIdArg;

    @Before
    public void setup() {
        notificationManager = mock(AppNotificationManager.class);
        messageManager = spy(new InAppMessageManager(notificationManager));
        messageIdArg = ArgumentCaptor.forClass(Integer.class);
    }

    @Test
    public void shouldShowMessageOnce() {
        TestSubscriber<InAppMessage> subscriber = new TestSubscriber<>();
        messageManager.getNewMessages().subscribe(subscriber);
        subscriber.assertNoValues();

        InAppMessage m1 = new InAppMessage("Title 1", "Message 1");
        messageManager.show(m1);
        subscriber.assertValue(m1);
        assertFalse(m1.isConsumed());
        subscriber.assertValueCount(1);

        messageManager.read(m1);
        assertTrue(m1.isConsumed());
        verify(notificationManager, times(1)).cancelNotification(m1.getId());
        reset(notificationManager);

        messageManager.show(m1);
        subscriber.assertValueCount(1);
        verify(notificationManager, never()).cancelNotification(anyInt());
    }

    @Test
    public void shouldShowOnlyNewMessages() {
        TestSubscriber<InAppMessage> subscriber = new TestSubscriber<>();
        messageManager.getNewMessages().subscribe(subscriber);
        subscriber.assertNoValues();

        InAppMessage m1 = new InAppMessage("Title 1", "Message 1");
        messageManager.show(m1);
        subscriber.assertValueCount(1);
        subscriber.assertValuesAndClear(m1);
        assertFalse(m1.isConsumed());

        InAppMessage m2 = new InAppMessage("Title 2", "Message 2");
        messageManager.show(m2);
        subscriber.assertValueCount(1);
        subscriber.assertValuesAndClear(m2);
        assertFalse(m2.isConsumed());

        TestSubscriber<InAppMessage> subscriber2 = new TestSubscriber<>();
        messageManager.getNewMessages().subscribe(subscriber2);
        subscriber2.assertValues(m1, m2);

        verify(notificationManager, never()).cancelNotification(anyInt());
    }

    @Test
    public void shouldReadMessage() {
        InAppMessage m1 = new InAppMessage("Title 1", "Message 1");
        messageManager.read(m1);
        Assert.assertTrue(m1.isConsumed());
        verify(notificationManager, times(1)).cancelNotification(messageIdArg.capture());
        assertEquals(m1.getId(), messageIdArg.getValue().intValue());
    }

    @After
    public void tearDown() {
        notificationManager = null;
        messageManager = null;
        messageIdArg = null;
    }

}
