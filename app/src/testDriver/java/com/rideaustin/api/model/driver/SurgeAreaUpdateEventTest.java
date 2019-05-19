package com.rideaustin.api.model.driver;

import com.google.android.gms.maps.model.LatLng;
import com.rideaustin.api.model.Event;
import com.rideaustin.api.model.RideStatus;
import com.rideaustin.api.model.surgearea.SurgeArea;
import com.rideaustin.api.model.surgearea.SurgeAreas;
import com.rideaustin.ui.utils.SurgeAreaUtils;
import com.rideaustin.utils.SerializationHelper;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.Arrays;
import java.util.List;

import common.test.BaseTest;
import common.test.JavaResourceHelper;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;

/**
 * Created by Viktor Kifer
 * On 13-Jan-2017.
 */
@RunWith(RobolectricTestRunner.class)
public class SurgeAreaUpdateEventTest extends BaseTest {

    @Test
    public void testSurgeAreaEventParsing() throws Exception {
        String eventJson = JavaResourceHelper.getResourceAsString("sample_surge_area_update_event.json");
        Event event = SerializationHelper.deSerialize(eventJson, Event.class);

        assertNotNull(event);
        assertEquals(11977, event.getId());
        assertEquals(RideStatus.SURGE_AREA_UPDATE, event.getEventType());
        assertNotNull(event.getParameters());

        SurgeArea surgeArea = SerializationHelper.deSerialize(event.getParameters(), SurgeArea.class);

        assertNotNull(surgeArea);
        assertEquals(18, surgeArea.getId().intValue());
        assertEquals(1f, surgeArea.getSurgeFactor());
        List<String> carCategories = Arrays.asList("REGULAR", "SUV");
        assertEquals(carCategories, surgeArea.getCarCategories());
        assertNotNull(surgeArea.getCsvGeometry());

        List<LatLng> points = SurgeAreaUtils.parseCSVGeometry(surgeArea.getCsvGeometry());
        assertNotNull(points);
        assertFalse(points.isEmpty());

        LatLng firstPoint = points.get(0);
        assertEquals(30.218506, firstPoint.latitude);
        assertEquals(-97.673092, firstPoint.longitude);
    }

    @Test
    public void testSurgeAreasEventParsing() throws Exception {
        String eventJson = JavaResourceHelper.getResourceAsString("sample_surge_area_updates_event.json");
        Event event = SerializationHelper.deSerialize(eventJson, Event.class);

        assertNotNull(event);
        assertEquals(11977, event.getId());
        assertEquals(RideStatus.SURGE_AREA_UPDATES, event.getEventType());
        assertNotNull(event.getParameters());

        SurgeAreas surgeAreas = SerializationHelper.deSerialize(event.getParameters(), SurgeAreas.class);
        assertNotNull(surgeAreas.getSurgeAreas());
        assertEquals(2, surgeAreas.getSurgeAreas().size());

        SurgeArea surgeArea = surgeAreas.getSurgeAreas().get(0);
        assertNotNull(surgeArea);
        assertEquals(18, surgeArea.getId().intValue());
        assertEquals(1f, surgeArea.getSurgeFactor());
        List<String> carCategories = Arrays.asList("REGULAR", "SUV");
        assertEquals(carCategories, surgeArea.getCarCategories());
        assertNotNull(surgeArea.getCsvGeometry());

        List<LatLng> points = SurgeAreaUtils.parseCSVGeometry(surgeArea.getCsvGeometry());
        assertNotNull(points);
        assertFalse(points.isEmpty());

        LatLng firstPoint = points.get(0);
        assertEquals(30.218506, firstPoint.latitude);
        assertEquals(-97.673092, firstPoint.longitude);
    }
}