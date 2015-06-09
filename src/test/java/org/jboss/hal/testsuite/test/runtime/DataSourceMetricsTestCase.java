package org.jboss.hal.testsuite.test.runtime;

import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.fragment.MetricsAreaFragment;
import org.jboss.hal.testsuite.fragment.MetricsFragment;
import org.jboss.hal.testsuite.page.runtime.DataSourcesMetricsPage;
import org.jboss.hal.testsuite.test.category.Shared;
import org.jboss.hal.testsuite.util.Console;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;

import static org.junit.Assert.assertEquals;

/**
 * @author mkrajcov <mkrajcov@redhat.com>
 */
@RunWith(Arquillian.class)
@Category(Shared.class)
public class DataSourceMetricsTestCase {

    public static final String AVAILABLE_CONNECTIONS = "Available Connections";
    public static final String ACTIVE = "Active";
    public static final String MAX_USED = "Max Used";
    public static final String HIT_COUNT = "Hit Count";
    public static final String ACCESS_COUNT = "Access Count";
    public static final String MISS_COUNT = "Miss Count";
    public static final int DELTA = 3;
    @Drone
    private WebDriver browser;

    @Page
    private DataSourcesMetricsPage dsPage;

    @Before
    public void before(){
        browser.navigate().refresh();
        Graphene.goTo(DataSourcesMetricsPage.class);
        browser.manage().window().maximize();
        Console.withBrowser(browser).waitUntilLoaded();
    }

    @Test
    public void connectionPoolMetrics(){
        MetricsAreaFragment metricsArea = dsPage.getConnectionPoolMetricsArea();
        double expectedActivePercentage = metricsArea.getPercentage(ACTIVE, AVAILABLE_CONNECTIONS);
        double expectedMaxUsedPercentage = metricsArea.getPercentage(MAX_USED, AVAILABLE_CONNECTIONS);

        MetricsFragment activeMetrics = metricsArea.getMetricsFragment(ACTIVE);
        MetricsFragment maxUsedMetrics = metricsArea.getMetricsFragment(MAX_USED);

        assertEquals(expectedActivePercentage, activeMetrics.getPercentage(), DELTA);
        assertEquals(expectedMaxUsedPercentage, maxUsedMetrics.getPercentage(), DELTA);
    }

    @Test
    public void preparedStatementCacheMetrics(){
        MetricsAreaFragment metricsArea = dsPage.getPreparedStatementCacheMetricsArea();
        double expectedHitCountPercentage = metricsArea.getPercentage(HIT_COUNT, ACCESS_COUNT);
        double expectedMissCountPercentage = metricsArea.getPercentage(MISS_COUNT, ACCESS_COUNT);

        MetricsFragment hitCountMetrics = metricsArea.getMetricsFragment(HIT_COUNT);
        MetricsFragment missCountMetrics = metricsArea.getMetricsFragment(MISS_COUNT);

        assertEquals(expectedHitCountPercentage, hitCountMetrics.getPercentage(), DELTA);
        assertEquals(expectedMissCountPercentage, missCountMetrics.getPercentage(), DELTA);
    }
}
