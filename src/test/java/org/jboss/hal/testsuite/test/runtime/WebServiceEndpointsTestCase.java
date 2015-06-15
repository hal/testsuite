package org.jboss.hal.testsuite.test.runtime;

import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.fragment.MetricsAreaFragment;
import org.jboss.hal.testsuite.fragment.MetricsFragment;
import org.jboss.hal.testsuite.page.runtime.WebServiceEndpointsPage;
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
public class WebServiceEndpointsTestCase {
    public static final String RESPONSES = "Responses";
    public static final String NUMBER_OF_REQUEST = "Number of request";
    public static final String FAULTS = "Faults";
    public static final int DELTA = 3;
    @Drone
    private WebDriver browser;

    @Page
    private WebServiceEndpointsPage wsePage;

    @Before
    public void before(){
        browser.navigate().refresh();
        Graphene.goTo(WebServiceEndpointsPage.class);
        Console.withBrowser(browser).maximizeWindow().waitUntilLoaded();
    }

    @Test
    public void webServiceRequestsMetrics(){
        MetricsAreaFragment wsrMetricsArea = wsePage.getWebServiceRequestMetricsArea();
        double expectedResponsesPercentage = wsrMetricsArea.getPercentage(RESPONSES, NUMBER_OF_REQUEST);
        double expectedFaultsPercentage = wsrMetricsArea.getPercentage(FAULTS, NUMBER_OF_REQUEST);
        MetricsFragment responsesMetrics = wsrMetricsArea.getMetricsFragment(RESPONSES);
        MetricsFragment faultsMetrics = wsrMetricsArea.getMetricsFragment(FAULTS);

        assertEquals(expectedResponsesPercentage, responsesMetrics.getPercentage(), DELTA);
        assertEquals(expectedFaultsPercentage, faultsMetrics.getPercentage(), DELTA);
    }
}
