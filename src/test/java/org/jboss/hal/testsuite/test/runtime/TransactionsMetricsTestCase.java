package org.jboss.hal.testsuite.test.runtime;

import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.fragment.MetricsAreaFragment;
import org.jboss.hal.testsuite.fragment.MetricsFragment;
import org.jboss.hal.testsuite.page.runtime.TransactionsMetricsPage;
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
public class TransactionsMetricsTestCase {

    public static final String NUMBER_OF_TRANSACTIONS = "Number of Transactions";
    public static final String COMMITTED = "Commited";
    public static final String ABORTED = "Aborted";
    public static final String TIMED_OUT = "Timed Out";
    public static final String APPLICATION_FAILURES = "Application Failures";
    public static final String RESOURCE_FAILURES = "Resource Failures";
    public static final int DELTA = 3;
    @Drone
    private WebDriver browser;

    @Page
    private TransactionsMetricsPage tmPage;

    @Before
    public void before(){
        browser.navigate().refresh();
        Graphene.goTo(TransactionsMetricsPage.class);
        browser.manage().window().maximize();
        Console.withBrowser(browser).waitUntilLoaded();
    }

    @Test
    public void successRationMetrics(){
        MetricsAreaFragment metricsArea = tmPage.getSuccessRationMetricsArea();
        double expectedCommittedPercentage = metricsArea.getPercentage(COMMITTED, NUMBER_OF_TRANSACTIONS);
        double expectedAbortedPercentage = metricsArea.getPercentage(ABORTED, NUMBER_OF_TRANSACTIONS);
        double expectedTimedOutPercentage = metricsArea.getPercentage(TIMED_OUT, NUMBER_OF_TRANSACTIONS);

        MetricsFragment committedMetrics = metricsArea.getMetricsFragment(COMMITTED);
        MetricsFragment abortedMetrics = metricsArea.getMetricsFragment(ABORTED);
        MetricsFragment timedOutMetrics = metricsArea.getMetricsFragment(TIMED_OUT);

        assertEquals(expectedCommittedPercentage, committedMetrics.getPercentage(), DELTA);
        assertEquals(expectedAbortedPercentage, abortedMetrics.getPercentage(), DELTA);
        assertEquals(expectedTimedOutPercentage, timedOutMetrics.getPercentage(), DELTA);
    }

    @Test
    public void failureOriginMetrics(){
        MetricsAreaFragment metricsArea = tmPage.getFailureOriginMetricsArea();
        MetricsFragment appFailuresMetrics = metricsArea.getMetricsFragment(APPLICATION_FAILURES);
        MetricsFragment resFailuresMetrics = metricsArea.getMetricsFragment(RESOURCE_FAILURES);

        assertEquals(metricsArea.getMetricNumber(APPLICATION_FAILURES), appFailuresMetrics.getCurrentValue(), DELTA);
        assertEquals(metricsArea.getMetricNumber(RESOURCE_FAILURES), resFailuresMetrics.getCurrentValue(), DELTA);
    }
}
