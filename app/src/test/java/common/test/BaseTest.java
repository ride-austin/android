package common.test;

import com.rideaustin.schedulers.RxSchedulers;
import com.rideaustin.schedulers.TestRxSchedulers;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;

/**
 * Created by Viktor Kifer
 * On 25-Dec-2016.
 */

public class BaseTest {

    @BeforeClass
    public static void setupSuite() {
        TestRxSchedulers.initSchedulersForTests();
        RxSchedulers.init();
    }

    @Before
    public void setUp() {

    }

    @After
    public void tearDown() {
    }

}
